// =============================================================================
// DishRelation — a root relation over a DishStore: identities, columns, and
// a CELL MANAGER. This is the domain's whole side of the seam, and the word
// "grid" does not appear in it.
//
//   createDishRelation(store, { role })     role: chef | nutritionist | manager | follower
//   dishRoles()                             the policy: which columns each role may edit
//
//   · cellFor(pk, col) builds a RelGridTextCell ONCE per identity and keeps it.
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
    nutritionist: ['calories'],
    manager:      ['price'],
    follower:     []
};

/** The policy, copied: which writable columns each role may edit. `sold` and `popularity` are nobody's. */
function dishRoles() { return JSON.parse(JSON.stringify(DISH_ROLES)); }

function createDishRelation(store, opts) {
    opts = opts || {};
    var role = opts.role || 'follower';
    if (!DISH_ROLES[role]) throw new Error('[DishRelation] unknown role: ' + role);
    // The role says who; the store says what may be written at all. Both must agree.
    var writable = store.writableColumns();
    var editable = DISH_ROLES[role].filter(function (c) { return writable.indexOf(c) >= 0; });
    var NUMERIC = { calories: true, price: true };
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
        pks:     function () { return store.pks(); },
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
            var k = pk + ' ' + col, c = cells.get(k);
            if (!c) {
                c = new RelGridTextCell({
                    value: store.get(pk, col),
                    onCommit: mayEdit(col)
                        ? function (text) { store.commit(pk, col, coerce(col, text)); }
                        : undefined
                });
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
        }
    };
}
