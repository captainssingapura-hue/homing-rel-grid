// =============================================================================
// ProseReference — a widget of the Document Table bench: ProseTable's options, its code drawn inline, and a table with no head - a page of the
// house's words and document tables (ProseReferencePage), shown whole. Given the keys by
// a call, they go on to the page's first table; a press in any table claims
// them for its own grid, as a grid does.
//
//   new ProseReference(container, params)   params: none
//   (the rest is a BenchWidget's)
// =============================================================================

class ProseReference extends BenchWidget {
    constructor(container, params) {
        super(container, "proseReference", "ProseTable");
        this._page = new ProseReferencePage(this.branch.createBranch("page"));
        this.body.appendChild(this._page.root);
        this.keysTo = this._page.grid();
    }
    disposed() { this._page.dispose(); }
}
