// =============================================================================
// HanRelation — a root relation over a HanStore: the article laid out by
// HanLayout into rows of square slots, and a CELL MANAGER that owns one
// HanCell per slot. DOMAIN CODE; the word "grid" does not appear in it.
//
//   new HanRelation(store, { branch, cols? })
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


class HanRelation {
    constructor(store, opts) {
        var o = opts || {}, self = this;
        if (!o.branch) throw new Error("[HanRelation] opts.branch is required: the relation's own");
        this._store = store;
        this._branch = o.branch;
        this._branch.activate({ toString: function () { return "HanRelation"; } });     // its own: unactivated when handed
        this._cellSeq = 0;
        this._cols = (o.cols > 0) ? o.cols : 9;
        this._cells = new Map();
        this._layout = HanLayout.layout(store.text(), this._cols);
        this._squares = [];
        for (var c = 0; c < this._cols; c++) this._squares.push("c" + c);
        this._columns = ["lead"].concat(this._squares, ["trail"]);
        this._unsubscribe = store.subscribe(function () {
            self._layout = HanLayout.layout(store.text(), self._cols);
            self._cells.forEach(function (cell, key) {
                var sp = key.indexOf(" ");
                var s = self._slotOf(key.slice(0, sp), key.slice(sp + 1));
                cell.set(s ? s.glyph : null, s ? s.span : 1);
            });
        });
    }

    view(intent) { return intent ? null : this.presented(); }

    presented() {
        var out = [];
        for (var r = 0; r < this._layout.rows.length; r++) out.push("r" + r);
        return out;
    }

    columns() { return this._columns.slice(); }

    // What a column is called, when a header is shown at all: the squares by number,
    // the half-squares by the mark they hold (law 86: the relation's, not the host's).
    labels() {
        var out = { lead: "\u2039", trail: "\u203a" };
        for (var k = 0; k < this._cols; k++) out["c" + k] = String(k + 1);
        return out;
    }

    presentedColumns() {
        var out = this._layout.usesLead ? ["lead"] : [];
        out = out.concat(this._squares);
        if (this._layout.usesTrail) out.push("trail");
        return out;
    }

    narrowColumns() { return ["lead", "trail"]; }

    cellFor(pk, col) {
        var key = pk + " " + col, cell = this._cells.get(key);
        if (!cell) {
            // What this relation OWNS: the rows the layout has now, and every cell it
            // has already made — a row the text shrank away leaves its cells alive,
            // showing nothing, and they are still its own. Anything else is a stranger.
            if (!/^r\d+$/.test(pk) || Number(pk.slice(1)) >= this._layout.rows.length)
                throw new Error("[HanRelation] no such row: " + pk);
            var s = this._slotOf(pk, col);
            cell = new HanCell({ branch: this._branch.createBranch("c" + (++this._cellSeq)),
                                 glyph: s ? s.glyph : null, span: s ? s.span : 1,
                                 narrow: col === "lead" || col === "trail" });
            this._cells.set(key, cell);
        }
        return cell;
    }

    rows()      { return this._layout.rows.length; }
    spanKey()   { return this._layout.spanKey; }
    cols()      { return this._cols; }
    glyphs()    { return this._layout.glyphs; }
    layout()    { return this._layout; }
    cellCount() { return this._cells.size; }

    dispose() {
        this._unsubscribe();
        this._cells.forEach(function (c) { c.dispose(); });
        this._cells.clear();
        this._branch.dissolve();
    }

    _slotOf(pk, col) {
        var r = Number(String(pk).slice(1));
        var row = this._layout.rows[r];
        if (!row) return null;
        if (col === "lead")  return row.lead;
        if (col === "trail") return row.trail;
        var k = Number(String(col).slice(1));
        return row.cells[k] || null;
    }
}
