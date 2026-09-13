// =============================================================================
// DishRelation — a root relation over a DishStore: identities, columns, and
// a CELL MANAGER. This is the domain's whole side of the seam, and the word
// "grid" does not appear in it.
//
//   createDishRelation(store, { role, branch })   role: chef | nutritionist | manager | follower;
//                                                  branch: the relation's OWN (unactivated when handed;
//                                                  it activates, and dispose() dissolves) — a sub-branch per cell
//   dishRoles()                             the policy: which columns each role may edit
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

var DISH_ROLES = {
    chef:         ['ingredient', 'style'],
    nutritionist: ['calories', 'stars'],
    manager:      ['price'],
    follower:     []
};

/** The policy, copied: which writable columns each role may edit. `sold` and `popularity` are nobody's. */
function dishRoles() { return JSON.parse(JSON.stringify(DISH_ROLES)); }

function createDishRelation(store, opts) {
    opts = opts || {};
    var role = opts.role || 'follower';
    if (!DISH_ROLES[role]) throw new Error('[DishRelation] unknown role: ' + role);
    if (!opts.branch) throw new Error("[DishRelation] opts.branch is required: the relation's own");
    var branch = opts.branch, cellSeq = 0;
    branch.activate({ toString: function () { return 'DishRelation ' + role; } });   // its own: unactivated when handed
    // The role says who; the store says what may be written at all. Both must agree.
    var writable = store.writableColumns();
    var editable = DISH_ROLES[role].filter(function (c) { return writable.indexOf(c) >= 0; });
    var NUMERIC = { calories: true, price: true, stars: true };
    var cells = new Map();

    function mayEdit(col) { return editable.indexOf(col) >= 0; }

    /** Domain rule: numeric columns take numbers; anything else stays text. */
    function coerce(col, text) {
        if (!NUMERIC[col]) return text;
        var n = Number(text);
        return (text !== '' && isFinite(n)) ? n : text;
    }

    var unsubscribe = store.subscribe(function (pk, col, v) {
        var c = cells.get(pk + ' ' + col);
        if (c) c.set(v);
    });

    return {
        // ONE ROOT: the View is answered, never listed. The whole store, in its own
        // order; a movement goes nowhere, so the answer is nothing and the rows stay.
        view:    function (intent) { return intent ? null : store.pks(); },
        columns: function () { return store.columns(); },
        // THE COLUMN CONSTRAINT (map 16, law 112). sold moves only by selling
        // and popularity is derived from it, so no cell in either column is
        // ever asked — the grid does not consult them, and they are not asked
        // to refuse. That is the STRUCTURAL no, beside the situational one the
        // roles make below; neither substitutes for the other (law 113).
        readOnlyColumns: function () {
            var out = [], all = store.columns(), may = store.writableColumns();
            for (var k = 0; k < all.length; k++)
                if (may.indexOf(all[k]) < 0) out.push(all[k]);
            return out;
        },
        cellFor: function (pk, col) {
            // The relation is the authority on what it owns: a stranger is refused here,
            // and the grid — which keeps no list — refuses the View whole on it.
            if (store.pks().indexOf(pk) < 0) throw new Error('[DishRelation] no such dish: ' + pk);
            var k = pk + ' ' + col, c = cells.get(k);
            if (!c) {
                // The cell KIND is the domain's choice, made per column and
                // invisible to the grid: a rating gets a dropdown, everything
                // else gets text, and the grid asks both the same two questions.
                var commit = mayEdit(col)
                    ? function (text) { store.commit(pk, col, coerce(col, text)); }
                    : undefined;
                var own = branch.createBranch('c' + (++cellSeq));      // the cell's own branch, to mint its element on
                c = (col === 'stars')
                    ? new DishStarsCell({ branch: own, value: store.get(pk, col), onCommit: commit })
                    : new RelGridTextCell({ branch: own, value: store.get(pk, col), onCommit: commit });
                cells.set(k, c);
            }
            return c;
        },
        role:            function () { return role; },
        editableColumns: function () { return editable.slice(); },
        cellCount:       function () { return cells.size; },
        dispose: function () {
            unsubscribe();
            cells.forEach(function (c) { c.dispose(); });
            cells.clear();
            branch.dissolve();
        }
    };
}
