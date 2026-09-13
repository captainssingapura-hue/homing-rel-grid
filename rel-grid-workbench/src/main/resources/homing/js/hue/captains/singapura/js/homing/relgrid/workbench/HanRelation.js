// =============================================================================
// HanRelation — a root relation over a HanStore: the article laid out by
// HanLayout into rows of square slots, and a CELL MANAGER that owns one
// HanCell per slot. DOMAIN CODE; the word "grid" does not appear in it.
//
//   createHanRelation(store, { branch, cols? })
//
//   · branch is the relation's OWN — unactivated when handed; it activates,
//     and dispose() dissolves — and every cell is given a sub-branch of it to
//     own. The grid that places the cells never sees it.
//   · view() answers the ROWS the layout uses right now, 'r0'…'r{rows-1}' —
//     ONE ROOT: the View is answered, never listed, so there is no capacity
//     to declare and no prefix of it to present. A movement goes nowhere: an
//     article is the whole of itself, so the answer is nothing and the rows
//     stay. columns() are 'lead', the squares 'c0'…'c{cols-1}', and 'trail'
//     — the two HALF-SQUARE columns declared always and shown only when some
//     row squeezes a mark into one. Identity here is POSITIONAL — a square —
//     and a glyph is what a square currently shows. That is the right way
//     round for a manuscript grid: the squares stay put, the ink moves.
//   · presented() is view()'s answer by its domain name, for the owner's own
//     use; presentedColumns() is the columns in use — the squares, and
//     whichever half-squares the layout put a mark in. What an article does
//     as it grows and shrinks is change which rows and columns are SHOWN,
//     and that is a view, not a new relation: the owner hands both to
//     whoever arranges, as the row view and the column view.
//   · A row the layout does not have, and never had a cell made for, is a
//     STRANGER, refused at cellFor: rows come and go with the text, the rows
//     there now are owned, and so is every cell already made — a row that
//     shrank away leaves its cells alive and showing nothing.
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

    function presented() {
        var out = [];
        for (var r = 0; r < layout.rows.length; r++) out.push("r" + r);
        return out;
    }

    return {
        view:      function (intent) { return intent ? null : presented(); },
        presented: presented,
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
                // What this relation OWNS: the rows the layout has now, and every cell it
                // has already made — a row the text shrank away leaves its cells alive,
                // showing nothing, and they are still its own. Anything else is a stranger.
                if (!/^r\d+$/.test(pk) || Number(pk.slice(1)) >= layout.rows.length)
                    throw new Error("[HanRelation] no such row: " + pk);
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
