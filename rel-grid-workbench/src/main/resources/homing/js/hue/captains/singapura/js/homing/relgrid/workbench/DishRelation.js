// =============================================================================
// DishRelation — a root relation over a DishStore: identities, columns, and
// a CELL MANAGER. This is the domain's whole side of the seam, and the word
// "grid" does not appear in it.
//
//   createDishRelation(store, { editable })
//
//   · cellFor(pk, col) builds a RelGridTextCell ONCE per identity and keeps it.
//   · The relation subscribes to the store and set()s its own cells when a
//     value changes — the grid that placed them is never told.
//   · An editable relation's cells commit to the store on Enter; the store
//     notifies every subscriber, this relation included, so the editor's own
//     cell shows what the store accepted rather than what was typed.
//   · dispose() unsubscribes and disposes the cells — the OWNER's call.
// =============================================================================

function createDishRelation(store, opts) {
    opts = opts || {};
    var editable = !!opts.editable;
    var NUMERIC = { calories: true, price: true, popularity: true };
    var cells = new Map();

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
        cellFor: function (pk, col) {
            var k = pk + ' ' + col, c = cells.get(k);
            if (!c) {
                c = new RelGridTextCell({
                    value: store.get(pk, col),
                    onCommit: editable
                        ? function (text) { store.commit(pk, col, coerce(col, text)); }
                        : undefined
                });
                cells.set(k, c);
            }
            return c;
        },
        editable: function () { return editable; },
        cellCount: function () { return cells.size; },
        dispose: function () {
            unsubscribe();
            cells.forEach(function (c) { c.dispose(); });
            cells.clear();
        }
    };
}
