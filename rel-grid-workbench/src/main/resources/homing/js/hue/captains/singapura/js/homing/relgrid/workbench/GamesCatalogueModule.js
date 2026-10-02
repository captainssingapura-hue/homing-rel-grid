// =============================================================================
// GamesCatalogue — the Games Catalogue bench: sorting and filtering from header
// cells that are the relation's own, over seven hundred releases. Its own
// data: a GamesStore over the catalogue, and a GamesRelation over it.
//
// EXACTLY TWO BRANCHES under the widget's: 'grid', handed whole to the grid;
// 'domain', the widget's to divide - 'cells', the relation's own, its cells
// AND its header cells. When the relation's View changes the widget tells the
// grid so, unasked, and the grid asks view() again; the relation never learns
// there is a grid.
//
//   new GamesCatalogue(container, params)   params: none
//   (the rest is a BenchWidget's)
// =============================================================================

class GamesCatalogue extends BenchWidget {
    constructor(container, params) {
        super(container, "gamesCatalogue", "Games catalogue");
        var self = this, body = this.body;
        var hint = this.el("hint", "div", wb_hint, body);
        var bar = this.el("bar", "div", wb_bar, body);
        // The frame goes round the SCROLLPORT - a non-scrolling wrapper the port fills - so the
        // light and the hairline sit outside the scrollbar; the grid inside draws no frame of its own.
        var frame = this.el("frame", "div", null, body);
        css.addClass(frame, wb_frame, hrg_frame, hrg_lit);
        var host = this.el("host", "div", wb_port, frame);
        this._status = this.el("status", "div", wb_status, body);
        var gridB = this.branch.createBranch("grid");
        var domainB = this.branch.createBranch("domain");
        domainB.activate(Object.freeze({ toString: function () { return "games"; } }));

        this._grid = null;
        this._store = new GamesStore(GAMES_CATALOGUE);
        this._relation = new GamesRelation(this._store, {
            branch: domainB.createBranch("cells"),
            menuHost: this.root,                       // a header cell's column menu goes here, placed by the header's rectangle
            // The relation's View changed underneath the grid: the common parent tells the grid,
            // unasked, and the grid asks view() again. The relation never learns there is a grid.
            onViewChanged: function () { if (self._grid) self._grid.tell(new RelGridViewChanged()); self._report(); }
        });

        hint.textContent = "CATALOGUE \u2014 " + this._store.size() + " releases, 1990 to 2020, every FIFA and Madden and Need for Speed a row of its own. The header cells are the RELATION\u2019S, placed in the grid\u2019s header slots as cells are placed in its body. A header\u2019s caret and number only SAY how it sorts; its \u25BE opens the column\u2019s menu, where the sorting and the filtering are done: sort at the top, then a search over the column\u2019s values, each with a count and a box, a range for a number \u2014 staged until OK. The grid knows none of it. When the relation\u2019s View changes this widget tells the grid so, unasked, and the grid asks view() again \u2014 the cursor keeps its identity, the header stays put. Absent sales and scores sort last either way, on purpose.";

        this._grid = new RelGrid({
            container: host,
            branch: gridB,
            relation: this._relation,
            header: { show: true, sticky: true },
            frame: false,                              // the host frames the scrollport; see above
            rowNumbers: true,                          // the gutter: a position each, the grid's own, locked left
            label: "Games catalogue",
            onCursorMoved: function () { self._report(); }
        });
        this._grid.setColumnWidths({ title: 260, series: 150, year: 64, platform: 100, type: 130, developer: 160, publisher: 140, sales: 80, score: 64 });
        this.keysTo = this._grid;
        this._report();

        // Presets: conditions as data, set on the relation as a header cell would set them.
        var r = this._relation, n = 0;
        var preset = function (label, fn) { self.button("preset" + (++n), label, function () { fn(); self._report(); self._grid.focus(); }, bar); };
        preset("as catalogued", function () { r.clear(); });
        preset("best rated", function () { r.setConditions({ sort: [{ column: "score", dir: "desc" }, { column: "year", dir: "asc" }], filters: {} }); });
        preset("best sellers", function () { r.setConditions({ sort: [{ column: "sales", dir: "desc" }], filters: {} }); });
        preset("Nintendo, newest first", function () { r.setConditions({ sort: [{ column: "year", dir: "desc" }, { column: "title", dir: "asc" }], filters: { publisher: { contains: "Nintendo" } } }); });
        preset("shooters of the 2000s", function () { r.setConditions({ sort: [{ column: "score", dir: "desc" }], filters: { type: { in: ["Shooter"] }, year: { min: 2000, max: 2009 } } }); });
        preset("every FIFA", function () { r.setConditions({ sort: [{ column: "year", dir: "asc" }], filters: { series: { contains: "FIFA" } } }); });
    }

    _report() {
        var c = this._grid ? this._grid.cursor() : null, r = this._relation;
        this._status.textContent = r.count() + " of " + this._store.size() + " releases   |   " + r.describe()
                                 + "   |   cursor " + (c ? c.pk + "/" + c.column : "\u2014")
                                 + "   |   cells owned " + r.cellCount() + ", header cells " + r.headerCount();
    }

    disposed() {
        this._grid.destroy();
        this._relation.dispose();
    }
}
