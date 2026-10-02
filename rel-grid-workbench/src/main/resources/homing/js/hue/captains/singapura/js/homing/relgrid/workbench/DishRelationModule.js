// =============================================================================
// DishRelation — a root relation over a DishStore: identities, columns, and
// a CELL MANAGER. This is the domain's whole side of the seam, and the word
// "grid" does not appear in it.
//
//   new DishRelation(store, { role, branch })     role: chef | nutritionist | manager | follower;
//                                                  branch: the relation's OWN (unactivated when handed;
//                                                  it activates, and dispose() dissolves) — a sub-branch per cell
//   DishRelation.roles()                    the policy: which columns each role may edit
//
//   · cellFor(pk, col) builds a cell ONCE per identity and keeps it — a
//     DishStarsCell for the rating column, a RelGridTextCell for the rest.
//     Which kind a column gets is the domain's business; the grid asks every
//     cell the same two questions and cannot tell them apart.
//   · WHO MAY EDIT is decided here, per cell, at the moment the cell is built:
//     a cell gets a commit target only if this relation's role may write its
//     column AND the store lets anybody write it. A cell without one declines
//     beginEdit — the situational "no" of RFC 0050 · E2 map 15 (law 105) and
//     map 16 (law 113): not structure, so not declared to the grid; the grid
//     asks, the cell answers, the grid stays shallow.
//   · The relation subscribes to the store and set()s its own cells when a
//     value changes — the grid that placed them is never told. A derived
//     column arrives through the same channel as an edited one.
//   · An editable cell commits to the store on Enter; the store notifies every
//     subscriber, this relation included, so the editor's own cell shows what
//     the store accepted rather than what was typed.
//   · dispose() unsubscribes and disposes the cells — the OWNER's call.
// =============================================================================

var _DISH_ROLES = {
    chef:         ["ingredient", "style"],
    nutritionist: ["calories", "stars"],
    manager:      ["price"],
    follower:     []
};
var _DISH_NUMERIC = { calories: true, price: true, stars: true };

class DishRelation {
    constructor(store, opts) {
        var o = opts || {}, self = this;
        this._role = o.role || "follower";
        if (!_DISH_ROLES[this._role]) throw new Error("[DishRelation] unknown role: " + this._role);
        if (!o.branch) throw new Error("[DishRelation] opts.branch is required: the relation's own");
        this._store = store;
        this._branch = o.branch;
        this._cellSeq = 0;
        var role = this._role;
        this._branch.activate({ toString: function () { return "DishRelation " + role; } });   // its own: unactivated when handed
        // The role says who; the store says what may be written at all. Both must agree.
        var writable = store.writableColumns();
        this._editable = _DISH_ROLES[role].filter(function (c) { return writable.indexOf(c) >= 0; });
        this._cells = new Map();
        this._unsubscribe = store.subscribe(function (pk, col, v) {
            var c = self._cells.get(pk + " " + col);
            if (c) c.set(v);
        });
    }

    /** The policy, copied: which writable columns each role may edit. `sold` and `popularity` are nobody's. */
    static roles() { return JSON.parse(JSON.stringify(_DISH_ROLES)); }

    /** Domain rule: numeric columns take numbers; anything else stays text. */
    static coerce(col, text) {
        if (!_DISH_NUMERIC[col]) return text;
        var n = Number(text);
        return (text !== "" && isFinite(n)) ? n : text;
    }

    // ONE ROOT: the View is answered, never listed. The whole store, in its own
    // order; a movement goes nowhere, so the answer is nothing and the rows stay.
    view(intent) { return intent ? null : this._store.pks(); }

    columns() { return this._store.columns(); }

    // THE COLUMN CONSTRAINT (map 16, law 112). sold moves only by selling
    // and popularity is derived from it, so no cell in either column is
    // ever asked — the grid does not consult them, and they are not asked
    // to refuse. That is the STRUCTURAL no, beside the situational one the
    // roles make below; neither substitutes for the other (law 113).
    readOnlyColumns() {
        var out = [], all = this._store.columns(), may = this._store.writableColumns();
        for (var k = 0; k < all.length; k++)
            if (may.indexOf(all[k]) < 0) out.push(all[k]);
        return out;
    }

    cellFor(pk, col) {
        // The relation is the authority on what it owns: a stranger is refused here,
        // and the grid — which keeps no list — refuses the View whole on it.
        var store = this._store;
        if (store.pks().indexOf(pk) < 0) throw new Error("[DishRelation] no such dish: " + pk);
        var k = pk + " " + col, c = this._cells.get(k);
        if (!c) {
            // The cell KIND is the domain's choice, made per column and
            // invisible to the grid: a rating gets a dropdown, everything
            // else gets text, and the grid asks both the same two questions.
            var commit = this._editable.indexOf(col) >= 0
                ? function (text) { store.commit(pk, col, DishRelation.coerce(col, text)); }
                : undefined;
            var own = this._branch.createBranch("c" + (++this._cellSeq));      // the cell's own branch, to mint its element on
            c = (col === "stars")
                ? new DishStarsCell({ branch: own, value: store.get(pk, col), onCommit: commit })
                : new RelGridTextCell({ branch: own, value: store.get(pk, col), onCommit: commit });
            this._cells.set(k, c);
        }
        return c;
    }

    role()            { return this._role; }
    editableColumns() { return this._editable.slice(); }
    cellCount()       { return this._cells.size; }

    dispose() {
        this._unsubscribe();
        this._cells.forEach(function (c) { c.dispose(); });
        this._cells.clear();
        this._branch.dissolve();
    }
}
