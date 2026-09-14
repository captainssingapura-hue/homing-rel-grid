// =============================================================================
// RelTreeStockCellsModule — a STOCK CELL for a tree node: a line of text on the
// domain's branch. Domain-side code that ships with the tree, exactly as the
// grid's text cell ships with the grid: it holds what it shows, which is
// where a value belongs, and the tree never reads it. A relation with
// nothing more to say about a node than its name answers one of these;
// everything else — an icon, a badge, a link — is a cell of the domain's
// own, answering the same contract.
//
//   new RelTreeTextCell({ branch, text })
//   cellElement()        the span, minted once on the cell's branch
//   onSelect(mode)       'none' | 'shallow' — the cell wears the mode as a class, and may ignore it
//   set(text)            the domain updates its own cell; nobody tells the tree
//   text()
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

    set(text) {
        this._text = (text === undefined || text === null) ? "" : String(text);
        if (this._el) this._el.textContent = this._text;
        return this;
    }

    dispose() {
        this._branch.dissolve();
        this._el = null;
    }
}
