// =============================================================================
// MinesweeperRelation — the board as a relation (RFC 0050, appendix Keyboard
// Minesweeper): a row per board row 'r0'…, a column per board column 'c0'…, a
// MineCell per position. The degenerate relation: one View, never moved, never
// remapped - nothing sorts a minefield. DOMAIN CODE; the word "grid" does not
// appear in it.
//
//   new MinesweeperRelation({ branch, rows, cols })
//
//   · branch is the relation's OWN - unactivated when handed; it activates, and
//     dispose() dissolves - a sub-branch per tile under it, the tile's own.
//   · view() answers every row; view({ by }) answers nothing: the board stands.
//   · cellFor(pk, col) mints a tile once and keeps it while the board lives.
//     Every column is read-only: a move is the game's, never an edit.
//   · at(pk, col) → { r, c }, the board's position of an identity, or null.
//     atElement(node) → the position of the tile a node is, or is inside of.
//   · show(changed, board) shows each position changed as the board says it now.
// =============================================================================

class MinesweeperRelation {

    constructor(opts) {
        var o = opts || {};
        if (!o.branch) throw new Error("[MinesweeperRelation] opts.branch is required: the relation's own");
        if (!(o.rows > 0) || !(o.cols > 0)) throw new Error("[MinesweeperRelation] a board has rows and columns");
        this._branch = o.branch;
        this._branch.activate(Object.freeze({ toString: function () { return "MinesweeperRelation"; } }));
        this._R = o.rows;
        this._C = o.cols;
        this._tiles = new Map();                       // "r c" → MineCell
        this._els = new Map();                         // a tile's element → { r, c }
        this._columns = [];
        for (var c = 0; c < this._C; c++) this._columns.push("c" + c);
    }

    view(intent) {
        if (intent) return null;                       // the board stands
        var out = [];
        for (var r = 0; r < this._R; r++) out.push("r" + r);
        return out;
    }

    columns() { return this._columns.slice(); }

    readOnlyColumns() { return this._columns.slice(); }

    cellFor(pk, col) {
        var at = this.at(pk, col);
        if (!at) throw new Error("[MinesweeperRelation] no such tile: " + pk + "/" + col);
        var key = at.r + " " + at.c, tile = this._tiles.get(key);
        if (!tile) {
            tile = new MineCell({ branch: this._branch.createBranch("t-" + at.r + "-" + at.c) });
            this._tiles.set(key, tile);
            this._els.set(tile.cellElement(), at);
        }
        return tile;
    }

    at(pk, col) {
        var r = MinesweeperRelation._index(pk, "r", this._R), c = MinesweeperRelation._index(col, "c", this._C);
        return (r < 0 || c < 0) ? null : { r: r, c: c };
    }

    /** The position of the tile a node is, or is inside of - what a press landed on - or null. */
    atElement(node) {
        for (var n = node; n; n = n.parentNode) { var at = this._els.get(n); if (at) return at; }
        return null;
    }

    show(changed, board) {
        for (var k = 0; k < changed.length; k++) {
            var tile = this._tiles.get(changed[k].r + " " + changed[k].c);
            if (tile) tile.show(board.tile(changed[k].r, changed[k].c));
        }
    }

    dispose() {
        this._tiles.forEach(function (t) { t.dispose(); });
        this._tiles.clear();
        this._els.clear();
        this._branch.dissolve();
    }

    static _index(key, prefix, n) {
        if (typeof key !== "string" || key.charAt(0) !== prefix || !/^\d+$/.test(key.slice(1))) return -1;
        var k = Number(key.slice(1));
        return k < n ? k : -1;
    }
}
