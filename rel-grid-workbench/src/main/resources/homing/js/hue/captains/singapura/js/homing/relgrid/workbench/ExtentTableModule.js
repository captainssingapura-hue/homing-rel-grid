// =============================================================================
// ExtentTable — the Colour Extents bench's table: rows are scaled words,
// columns are extents from -1 to 1; every cell is an element wearing one pair,
// with css.extent(el, x) and nothing else.
//
// Nothing here names a colour. The design owns every anchor; the bench owns
// the numbers - and by claiming six pairs in extents(), obliges every design
// registered to anchor them, which is the point of the table.
//
//   new ExtentTable(container, params)   params: none
//   (the rest is a BenchWidget's)
// =============================================================================

var _WB_EXTENTS = [-1, -0.75, -0.5, -0.25, 0, 0.25, 0.5, 0.75, 1];

class ExtentTable extends BenchWidget {
    constructor(container, params) {
        super(container, "extentTable", "Colour extents");
        var self = this, body = this.body;
        this.el("hint", "div", wb_hint, body,
            "COLOUR EXTENTS — six words that scale, at nine extents each. A row is one design pair; a cell is an element wearing it with " +
            "css.extent(el, x) and nothing else. The design owns the three anchors — the meaning turned the other way at −1, neutral at 0, " +
            "the word itself at 1 — and the sheet interpolates between them, the pole by the sign and then from neutral by the magnitude. Each word is its own axis: success at −1 is failure, danger at −1 is safety, and no word is another turned around. " +
            "Switch the theme: the numbers stay, every colour changes. The table has no empty cell because the completeness test does not let a design leave one.");

        var table = this.el("table", "div", wb_extent_table, body);
        // the head row: a blank corner, then the extents
        this.el("corner", "div", null, table);
        _WB_EXTENTS.forEach(function (x, j) { self.el("head" + j, "div", wb_extent_head, table, String(x)); });

        // The six rows: a label, the pair's class, and what a cell shows.
        var rows = [
            { label: "success · ink",     cls: wb_x_success_ink,     text: "★★★" },
            { label: "danger · surface",  cls: wb_x_danger_surface,  text: "" },
            { label: "primary · surface", cls: wb_x_primary_surface, text: "" },
            { label: "primary · ink",     cls: wb_x_primary_ink,     text: "Aa" },
            { label: "raised · surface",  cls: wb_x_raised_surface,  text: "" },
            { label: "warning · edge",    cls: wb_x_warning_edge,    text: "" }
        ];
        rows.forEach(function (row, i) {
            self.el("label" + i, "div", wb_extent_label, table, row.label);
            _WB_EXTENTS.forEach(function (x, j) {
                var cell = self.el("cell" + i + "_" + j, "div", wb_extent_cell, table, row.text);
                css.addClass(cell, row.cls);
                cell.title = row.label + " at " + x;
                css.extent(cell, x);                      // the one number the bench sets
            });
        });
    }
}
