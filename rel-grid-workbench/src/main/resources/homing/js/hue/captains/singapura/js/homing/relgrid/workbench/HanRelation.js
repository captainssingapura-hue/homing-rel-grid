// =============================================================================
// HanRelation — a root relation over a HanStore: the article laid out by
// HanLayout into rows of square slots, and a CELL MANAGER that owns one
// HanCell per slot. DOMAIN CODE; the word "grid" does not appear in it.
//
//   createHanRelation(store, { cols?, editable?, capacity? })
//
//   · pks() are ROWS — all of them, up to the CAPACITY, 'r0'…'r{capacity-1}';
//     columns() are the slots across, 'c0'…'c{cols-1}'. Identity here is
//     POSITIONAL — a square — and a glyph is what a square currently shows.
//     That is the right way round for a manuscript grid: the squares stay
//     put, the ink moves.
//   · presented() is the rows the layout USES right now — a prefix of pks().
//     A root relation's identity set is read once, when a grid is built over
//     it, and stays; what an article does as it grows and shrinks is change
//     which of those rows are SHOWN, and that is a view, not a new relation.
//     The owner hands presented() to whoever arranges, as the row view.
//   · cellFor(pk, col) builds a HanCell once per slot and keeps it. A cell
//     in an editable relation gets a commit target that looks the slot up AT
//     COMMIT TIME and splices the store there — replace, insert or delete
//     according to HanLayout's `at` and `len` — so a slot whose meaning moved
//     since it was built still commits to the right place.
//   · The relation subscribes to the store, re-lays the article out, and
//     set()s every cell it owns to what its slot now shows. A row that
//     disappeared leaves its cells alive with nothing in them; a row that
//     appears is asked for when something wants it.
//   · rows() is how many rows the layout has NOW. When that changes the
//     arrangement changes — more identities, or fewer — and that is the one
//     thing the owner has to tell whoever arranges: this relation cannot.
// =============================================================================

function createHanRelation(store, opts) {
    opts = opts || {};
    var cols = (opts.cols > 0) ? opts.cols : 9;
    var editable = opts.editable === true;
    var capacity = (opts.capacity > 0) ? opts.capacity : 200;   // rows the article may grow to
    var cells = new Map();
    var layout = hanLayout(store.text(), cols);
    var columns = [];
    for (var c = 0; c < cols; c++) columns.push("c" + c);

    function slotOf(pk, col) {
        var r = Number(String(pk).slice(1)), k = Number(String(col).slice(1));
        var row = layout.rows[r];
        return (row && row[k]) ? row[k] : null;
    }

    var unsubscribe = store.subscribe(function () {
        layout = hanLayout(store.text(), cols);
        cells.forEach(function (cell, key) {
            var sp = key.indexOf(" ");
            var s = slotOf(key.slice(0, sp), key.slice(sp + 1));
            cell.set(s ? s.glyph : null);
        });
    });

    return {
        pks: function () {
            var out = [];
            for (var r = 0; r < capacity; r++) out.push("r" + r);
            return out;
        },
        presented: function () {
            var out = [], n = Math.min(layout.rows.length, capacity);
            for (var r = 0; r < n; r++) out.push("r" + r);
            return out;
        },
        columns: function () { return columns.slice(); },
        cellFor: function (pk, col) {
            var key = pk + " " + col, cell = cells.get(key);
            if (!cell) {
                var s = slotOf(pk, col);
                cell = new HanCell({
                    glyph: s ? s.glyph : null,
                    onCommit: editable ? function (text) {
                        var now = slotOf(pk, col);
                        if (!now) return;
                        store.splice(now.at, now.len, text);
                    } : undefined
                });
                cells.set(key, cell);
            }
            return cell;
        },
        rows:      function () { return layout.rows.length; },
        cols:      function () { return cols; },
        capacity:  function () { return capacity; },
        glyphs:    function () { return layout.glyphs; },
        layout:    function () { return layout; },
        editable:  function () { return editable; },
        cellCount: function () { return cells.size; },
        dispose: function () {
            unsubscribe();
            cells.forEach(function (c) { c.dispose(); });
            cells.clear();
        }
    };
}
