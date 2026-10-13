// =============================================================================
// ProseNotes — a widget of the Document Table bench: release notes, and what each surface offers - a page of the
// house's words and document tables (ProseNotesPage), shown whole. Given the keys by
// a call, they go on to the page's first table; a press in any table claims
// them for its own grid, as a grid does.
//
//   new ProseNotes(container, params)   params: none
//   (the rest is a BenchWidget's)
// =============================================================================

class ProseNotes extends BenchWidget {
    constructor(container, params) {
        super(container, "proseNotes", "Release notes");
        this._page = new ProseNotesPage(this.branch.createBranch("page"));
        this.body.appendChild(this._page.root);
        this.keysTo = this._page.grid();
    }
    disposed() { this._page.dispose(); }
}
