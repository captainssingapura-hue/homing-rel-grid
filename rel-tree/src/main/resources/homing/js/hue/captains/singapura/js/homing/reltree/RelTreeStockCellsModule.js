// =============================================================================
// RelTreeStockCellsModule — a STOCK CELL for a tree node: a line of text on the
// domain's branch. Domain-side code that ships with the tree, exactly as the
// grid's text cell ships with the grid: it holds what it shows, which is
// where a value belongs, and the tree never reads it. A relation with
// nothing more to say about a node than its name answers one of these;
// everything else — an icon, a badge, a link — is a cell of the domain's
// own, answering the same contract.
//
// BUSY is the domain's to show, in its own cell. A relation that must fetch
// a node's children marks the node busy when the question arrives and clear
// when the children land — whether it answers late under the tree's mask or
// answers nothing and tells later — and the tree knows nothing of it. The
// glyph is cycled on a clock rather than a CSS animation: the typed sheet
// has no keyframes, and a cell's clock is the cell's own to stop.
//
//   new RelTreeTextCell({ branch, text })
//   cellElement()        the span, minted once on the cell's branch
//   onSelect(mode)       'none' | 'shallow' — the cell wears the mode as a class, and may ignore it
//   set(text)            the domain updates its own cell; nobody tells the tree
//   setBusy(on)          a spinning glyph after the text while on; the clock stops when off
//   text() / isBusy()
//   dispose()            the domain's to call: stops the clock, dissolves the cell's branch
// =============================================================================

var _HRT_BUSY_FRAMES = ["⠋", "⠙", "⠹", "⠸", "⠼", "⠴", "⠦", "⠧", "⠇", "⠏"];
var _HRT_BUSY_TICK = 90;

class RelTreeTextCell {

    constructor(opts) {
        opts = opts || {};
        if (!opts.branch) throw new Error("[RelTreeTextCell] opts.branch is required: a cell mints on the domain's branch");
        this._branch = opts.branch;
        this._branch.activate(this);
        this._text = (opts.text === undefined || opts.text === null) ? "" : String(opts.text);
        this._el = null;
        this._busy = null;                       // the glyph, minted on first use and kept
        this._clock = null;                      // the busy clock, while spinning
        this._frame = 0;
    }

    cellElement() {
        if (this._el) return this._el;
        var el = this._branch.createElement("cell", "span");
        css.addClass(el, hrt_text_cell);
        el.textContent = this._text;
        this._el = el;
        return el;
    }

    onSelect(mode) {
        if (this._el) css.toggleClass(this._el, hrt_text_cell_current, mode === "shallow");
    }

    text() { return this._text; }
    isBusy() { return this._clock !== null; }

    /** The text is the cell's own; the glyph, if spinning, stays after it. */
    set(text) {
        this._text = (text === undefined || text === null) ? "" : String(text);
        if (this._el) {
            this._el.textContent = this._text;
            if (this._busy && this._clock !== null) this._el.appendChild(this._busy);
        }
        return this;
    }

    setBusy(on) {
        if (on) {
            if (this._clock !== null) return this;
            var el = this.cellElement();
            if (!this._busy) {
                this._busy = this._branch.createElement("busy", "span");
                css.addClass(this._busy, hrt_text_cell_busy);
            }
            this._busy.textContent = _HRT_BUSY_FRAMES[this._frame];
            el.appendChild(this._busy);
            this._tick();
        } else {
            if (this._clock !== null) { clearTimeout(this._clock); this._clock = null; }
            if (this._busy && this._busy.parentNode) this._busy.parentNode.removeChild(this._busy);
        }
        return this;
    }

    _tick() {
        var self = this;
        this._clock = setTimeout(function () {
            self._frame = (self._frame + 1) % _HRT_BUSY_FRAMES.length;
            self._busy.textContent = _HRT_BUSY_FRAMES[self._frame];
            self._tick();
        }, _HRT_BUSY_TICK);
    }

    dispose() {
        this.setBusy(false);
        this._branch.dissolve();
        this._el = null;
        this._busy = null;
    }
}
