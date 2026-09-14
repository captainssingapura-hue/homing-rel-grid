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
// ring is CSS: a class that plays an animation, and a @keyframes the class
// names (RelTreeStockStyles.KEYFRAMES) that the deployment installs in its
// themes' globals until the typed sheet can declare one. No clock here.
//
//   new RelTreeTextCell({ branch, text })
//   cellElement()        the span, minted once on the cell's branch
//   onSelect(mode)       'none' | 'shallow' — the cell wears the mode as a class, and may ignore it
//   set(text)            the domain updates its own cell; nobody tells the tree
//   setBusy(on)          a spinning ring after the text while on
//   text() / isBusy()
//   dispose()            the domain's to call: dissolves the cell's branch
// =============================================================================

class RelTreeTextCell {

    constructor(opts) {
        opts = opts || {};
        if (!opts.branch) throw new Error("[RelTreeTextCell] opts.branch is required: a cell mints on the domain's branch");
        this._branch = opts.branch;
        this._branch.activate(this);
        this._text = (opts.text === undefined || opts.text === null) ? "" : String(opts.text);
        this._el = null;
        this._busy = null;                       // the ring, minted on first use and kept
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
    isBusy() { return !!(this._busy && this._el && this._busy.parentNode === this._el); }

    /** The text is the cell's own; the ring, if shown, stays after it. */
    set(text) {
        this._text = (text === undefined || text === null) ? "" : String(text);
        if (this._el) {
            var busy = this.isBusy();
            this._el.textContent = this._text;
            if (busy) this._el.appendChild(this._busy);
        }
        return this;
    }

    /** The ring is a class that plays the animation; showing and hiding it is all the cell does. */
    setBusy(on) {
        if (on) {
            var el = this.cellElement();
            if (!this._busy) {
                this._busy = this._branch.createElement("busy", "span");
                css.addClass(this._busy, hrt_text_cell_busy);
            }
            if (this._busy.parentNode !== el) el.appendChild(this._busy);
        } else if (this._busy && this._busy.parentNode) {
            this._busy.parentNode.removeChild(this._busy);
        }
        return this;
    }

    dispose() {
        this._branch.dissolve();
        this._el = null;
        this._busy = null;
    }
}
