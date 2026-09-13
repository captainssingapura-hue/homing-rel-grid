// =============================================================================
// HanRelation — a root relation over a HanStore: the article laid out by
// HanLayout into rows of square slots, and a CELL MANAGER that owns one
// HanCell per slot. DOMAIN CODE; the word "grid" does not appear in it.
//
//   createHanRelation(store, { branch, cols?, capacity? })
//
//   · branch is the relation's OWN — unactivated when handed; it activates,
//     and dispose() dissolves — and every cell is given a sub-branch of it to
//     own. The grid that places the cells never sees it.
//   · pks() are ROWS — all of them, up to the CAPACITY, 'r0'…'r{capacity-1}';
//     columns() are 'lead', the squares 'c0'…'c{cols-1}', and 'trail' — the
//     two HALF-SQUARE columns declared always and shown only when some row
//     squeezes a mark into one. Identity here is POSITIONAL — a square — and
//     a glyph is what a square currently shows. That is the right way round
//     for a manuscript grid: the squares stay put, the ink moves.
//   · presented() is the rows the layout USES right now — a prefix of pks();
//     presentedColumns() is the columns in use — the squares, and whichever
//     half-squares the layout put a mark in. A root relation's identity set
//     is read once, when a grid is built over it, and stays; what an article
//     does as it grows and shrinks is change which rows and columns are
//     SHOWN, and that is a view, not a new relation. The owner hands both to
//     whoever arranges, as the row view and the column view.
//   · narrowColumns() names the two, so the owner can give them their width.
//   · cellFor(pk, col) builds a HanCell once per slot and keeps it. Display
//     cells: the article is edited as text, and nothing here commits. A run
//     of narrow characters is one cell that answers colSpan() with its reach;
//     the squares it reaches over have cells of their own, showing nothing.
//   · spanKey() is the spans of every row as one string. When it changes,
//     the arrangement has, and the owner tells whoever arranges to go again.
//   · The relation subscribes to the store, re-lays the article out, and
//     set()s every cell it owns to what its slot now shows. A row that
//     disappeared leaves its cells alive with nothing in them; a row that
//     appears is asked for when something wants it.
// =============================================================================

function createHanRelation(store, opts) {
    opts = opts || {};
    if (!opts.branch) throw new Error("[HanRelation] opts.branch is required: the relation's own");
    var branch = opts.branch, cellSeq = 0;
    branch.activate({ toString: function () { return "HanRelation"; } });         // its own: unactivated when handed
    var cols = (opts.cols > 0) ? opts.cols : 9;
    var capacity = (opts.capacity > 0) ? opts.capacity : 200;   // rows the article may grow to
    var cells = new Map();
    var layout = hanLayout(store.text(), cols);
    var squares = [];
    for (var c = 0; c < cols; c++) squares.push("c" + c);
    var columns = ["lead"].concat(squares, ["trail"]);

    function slotOf(pk, col) {
        var r = Number(String(pk).slice(1));
        var row = layout.rows[r];
        if (!row) return null;
        if (col === "lead")  return row.lead;
        if (col === "trail") return row.trail;
        var k = Number(String(col).slice(1));
        return row.cells[k] || null;
    }

    var unsubscribe = store.subscribe(function () {
        layout = hanLayout(store.text(), cols);
        cells.forEach(function (cell, key) {
            var sp = key.indexOf(" ");
            var s = slotOf(key.slice(0, sp), key.slice(sp + 1));
            cell.set(s ? s.glyph : null, s ? s.span : 1);
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
        presentedColumns: function () {
            var out = layout.usesLead ? ["lead"] : [];
            out = out.concat(squares);
            if (layout.usesTrail) out.push("trail");
            return out;
        },
        narrowColumns: function () { return ["lead", "trail"]; },
        cellFor: function (pk, col) {
            var key = pk + " " + col, cell = cells.get(key);
            if (!cell) {
                var s = slotOf(pk, col);
                cell = new HanCell({ branch: branch.createBranch("c" + (++cellSeq)),
                                     glyph: s ? s.glyph : null, span: s ? s.span : 1,
                                     narrow: col === "lead" || col === "trail" });
                cells.set(key, cell);
            }
            return cell;
        },
        rows:      function () { return layout.rows.length; },
        spanKey:   function () { return layout.spanKey; },
        cols:      function () { return cols; },
        capacity:  function () { return capacity; },
        glyphs:    function () { return layout.glyphs; },
        layout:    function () { return layout; },
        cellCount: function () { return cells.size; },
        dispose: function () {
            unsubscribe();
            cells.forEach(function (c) { c.dispose(); });
            cells.clear();
            branch.dissolve();
        }
    };
}
