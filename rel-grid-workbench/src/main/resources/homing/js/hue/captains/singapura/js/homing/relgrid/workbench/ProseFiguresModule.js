// =============================================================================
// ProseFigures — a widget of the Document Table bench: a table wider than its page, scrolling across inside its box - a page of the
// house's words and document tables (ProseFiguresPage), shown whole. Given the keys by
// a call, they go on to the page's first table; a press in any table claims
// them for its own grid, as a grid does.
//
//   new ProseFigures(container, params)   params: none
//   (the rest is a BenchWidget's)
// =============================================================================

class ProseFigures extends BenchWidget {
    constructor(container, params) {
        super(container, "proseFigures", "A wide table");
        this._page = new ProseFiguresPage(this.branch.createBranch("page"));
        this.body.appendChild(this._page.root);
        this.keysTo = this._page.grid();
    }
    disposed() { this._page.dispose(); }
}
