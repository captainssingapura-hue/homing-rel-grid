// =============================================================================
// MinesweeperGame — Minesweeper on the grid, in Vista's design of its frame
// (RFC 0050 · Episode 2, appendices Keyboard Minesweeper and The Frame). The
// grid is the substrate - the cursor, the selection, the keys that move them -
// and knows nothing of mines; the board is the game, pure; the tiles are the
// game's cells, each in its own design. This widget is the glue between them.
//
// THE MOVES ARE THE GAME'S, not the grid's: the grid has no verb for "reveal".
// The grid has the keys first, as it always does, and consumes a key only when
// it did something with it; the keys it leaves bubble to the board's host, and
// a move's are taken there - Enter among them, which a tile, taking no control,
// leaves to bubble. A move applies to every tile selected - the cursor's alone,
// when nothing more is - so a region is flagged in one keystroke.
//
//   Enter / Space   reveal       F   flag (again: unflag)       C   chord
//   R               restart      Escape   never the game's: the grid's, then the bench's
//
// A press is the grid's too - it moves the cursor - and the click that follows
// is the game's: a hidden tile revealed, a revealed one chorded. A right press
// flags. A press held with Shift or Ctrl selects, as the grid says, and moves
// nothing.
//
//   new MinesweeperGame(container, params)   params: none
//   (the rest is a BenchWidget's)
// =============================================================================

var _MS_SIDE = 28;                                       // a tile and the air round it: one column, one row

class MinesweeperGame extends BenchWidget {
    constructor(container, params) {
        super(container, "minesweeper", "Minesweeper");
        var R = 9, C = 9, self = this, body = this.body;
        var hint = this.el("hint", "div", wb_hint, body);
        var band = this.el("band", "div", ms_band, body);
        this._left = this.el("left", "span", null, band);
        this._said = this.el("said", "span", null, band);
        this.button("restart", "Restart (R)", function () { self._play("restart"); self._grid.focus(); }, band);
        var host = this._host = this.el("board", "div", ms_board, body);
        host.style.setProperty("--ms-board-width", (C * _MS_SIDE) + "px");

        hint.textContent = "MINESWEEPER — Vista’s design of the grid’s frame: the board’s edge, the cursor’s ring, the selection in the air between the tiles; the tiles are the game’s cells, in a design of their own. Arrows move and Shift+arrows select; Enter or Space reveals what is selected, F flags it, C chords a number whose flags are all down, R restarts. A click reveals, or chords a number; a right click flags. Escape is never the game’s.";

        this._board = new MinesweeperBoard({ rows: R, cols: C, mines: 10 });
        var domainB = this.branch.createBranch("domain");
        domainB.activate(Object.freeze({ toString: function () { return "minesweeper"; } }));
        this._relation = new MinesweeperRelation({ branch: domainB.createBranch("tiles"), rows: R, cols: C });
        this._grid = new RelGrid({
            container: host,
            branch: this.branch.createBranch("grid"),
            design: MinesweeperVista,
            relation: this._relation,
            box: host,                                   // the edge round the board, the tiles clear of it
            header: { show: false },
            minColumnWidth: _MS_SIDE,
            label: "Minesweeper"
        });
        var widths = {};
        this._relation.columns().forEach(function (c) { widths[c] = _MS_SIDE; });
        this._grid.setColumnWidths(widths);
        this.keysTo = this._grid;

        this._onKey = function (e) { if (self._key(e)) e.preventDefault(); };
        this._onClick = function (e) { self._press(e, false); };
        this._onMenu = function (e) { self._press(e, true); };
        host.addEventListener("keydown", this._onKey);
        host.addEventListener("click", this._onClick);
        host.addEventListener("contextmenu", this._onMenu);
        this._report();
    }

    /** A key the grid left: a move, taken here - or not the game's, and on its way. */
    _key(e) {
        if (e.defaultPrevented || e.ctrlKey || e.metaKey || e.altKey || this._grid.isDeep() || this._grid.isPending()) return false;
        var key = e.key.length === 1 ? e.key.toLowerCase() : e.key;
        if (key === "Enter" || key === " ") this._play("reveal");
        else if (key === "f") this._play("flag");
        else if (key === "c") this._play("chord");
        else if (key === "r") this._play("restart");
        else return false;
        return true;
    }

    /** A press on a tile: the left reveals a hidden one and chords a number, the right flags. */
    _press(e, right) {
        var at = this._relation.atElement(e.target);
        if (right) e.preventDefault();
        if (!at || e.shiftKey || e.ctrlKey || e.metaKey || this._grid.isPending()) return;
        if (right) this._play("flag", [at]);
        else this._play(this._board.tile(at.r, at.c).shown === "revealed" ? "chord" : "reveal", [at]);
    }

    /** A move, on the tiles given or on every tile selected; each tile it changed shown as the board says. */
    _play(move, tiles) {
        var board = this._board, changed = [];
        if (move === "restart") changed = board.restart();
        else (tiles || this._selected()).forEach(function (t) { changed.push.apply(changed, board[move](t.r, t.c)); });
        this._relation.show(changed, board);
        this._report();
    }

    /** The positions selected, through the grid's maps: the cursor's alone when nothing more is. */
    _selected() {
        var maps = this._grid.viewMaps(), rel = this._relation, out = [];
        this._grid.selectedRanges().forEach(function (g) {
            for (var i = g.i0; i <= g.i1; i++) for (var j = g.j0; j <= g.j1; j++) {
                var id = maps.resolve(i, j), at = id && rel.at(id.pk, id.column);
                if (at) out.push(at);
            }
        });
        return out;
    }

    _report() {
        var b = this._board, s = b.state();
        this._left.textContent = "Mines left: " + b.minesLeft();
        this._said.textContent = s === "ready" ? "Reveal a tile to start: the first is never a mine."
                               : s === "playing" ? "Playing."
                               : s === "won" ? "Won - every mine found. R plays again."
                               : "Lost - R plays again.";
    }

    disposed() {
        this._host.removeEventListener("keydown", this._onKey);
        this._host.removeEventListener("click", this._onClick);
        this._host.removeEventListener("contextmenu", this._onMenu);
        this._grid.destroy();
        this._relation.dispose();
    }
}
