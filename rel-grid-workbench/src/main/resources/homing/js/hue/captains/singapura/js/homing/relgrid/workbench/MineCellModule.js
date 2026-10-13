// =============================================================================
// MineCell — a tile of the Minesweeper board, as a cell the grid places (RFC
// 0050, appendix Keyboard Minesweeper). A NOUN: it shows what it is told -
// hidden, flagged, revealed with its count, a mine, the boom, a wrong flag - in
// its own design, and has no behaviour towards the grid: the game moves it, by
// show(tile). Its element is minted once on the branch it was handed.
//
//   new MineCell({ branch })   cell.cellElement()   cell.show({ shown, count })   cell.dispose()
// =============================================================================

var _MS_SHOWN = Object.freeze({ hidden: ms_hidden, flagged: ms_flagged, revealed: ms_revealed, mine: ms_mine, boom: ms_boom, wrong: ms_wrong });
var _MS_INK = Object.freeze([null, ms_n1, ms_n2, ms_n3, ms_n4, ms_n5, ms_n6, ms_n7, ms_n8]);
var _MS_GLYPH = Object.freeze({ flagged: "⚑", mine: "●", boom: "✹", wrong: "✕" });

class MineCell {

    constructor(opts) {
        if (!opts || !opts.branch) throw new Error("[MineCell] opts.branch is required: the tile's own");
        this._branch = opts.branch;
        this._branch.activate(this);
        this._el = null;
        this._tile = Object.freeze({ shown: "hidden", count: 0 });
        this._worn = [];
    }

    /** The tile, minted once: a rounded square in its own design. */
    cellElement() {
        if (!this._el) {
            this._el = this._branch.createElement("tile", "div");
            css.addClass(this._el, ms_tile);
            this._paint();
        }
        return this._el;
    }

    /** What the tile is now, as the board says it. */
    show(tile) {
        if (!_MS_SHOWN[tile.shown]) throw new Error("[MineCell] a tile is shown hidden, flagged, revealed, mine, boom or wrong, not " + tile.shown);
        this._tile = tile;
        this._paint();
    }

    shown() { return this._tile.shown; }

    _paint() {
        if (!this._el) return;
        var t = this._tile, worn = [_MS_SHOWN[t.shown]];
        if (t.shown === "revealed" && t.count > 0) worn.push(_MS_INK[t.count]);
        if (this._worn.length) css.removeClass.apply(css, [this._el].concat(this._worn));
        css.addClass.apply(css, [this._el].concat(worn));
        this._worn = worn;
        this._el.textContent = t.shown === "revealed" ? (t.count > 0 ? String(t.count) : "") : (_MS_GLYPH[t.shown] || "");
    }

    onSelect(mode) {}

    /** The owner's to call: the game, when it is done. */
    dispose() {
        this._el = null;
        this._branch.dissolve();
    }
}
