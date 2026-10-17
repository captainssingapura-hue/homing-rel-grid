// =============================================================================
// DocTableRelationModule — what a doc's table says, as a relation (RFC 0050 ·
// Episode 2, Appendix: The Frame; rel-grid-prose): its columns, its rows by
// identity, a value for every cell, and its caption. PURE: no element, no
// branch - data, judged whole when made and frozen, so every table made from
// it may take its invariants as given. What a doc's table may say is said
// here, and only here: a new invariant is a new check in this constructor,
// and neither the caller nor the grid changes.
//
//   new DocTableRelation({ columns, rows, caption? })
//       columns   [ title | { title, align? } ] - at least one
//       rows      [ [ text | { text, align?, emphasis?, badge?, colSpan? } ] ]
//                 a row may say fewer cells than there are columns: the rest are
//                 empty; a cell reaching past the last column is refused, and so
//                 is a row span - the grid spans columns only
//       caption   the table's caption, or none
//   align: 'left' | 'center' | 'right'   emphasis: 'strong' | 'muted'
//   badge: 'success' | 'warning' | 'danger'
//
//   relation.columns()        the columns' identities: c0, c1, ...
//   relation.column(id)       { title, align } - align null when none is said
//   relation.titled()         whether any column has a title: a head to show
//   relation.view()           the rows' identities, in order: r0, r1, ...
//   relation.cell(pk, col)    { text, align, emphasis, badge, colSpan }, or null for
//                             a stranger - align its own, else its column's;
//                             a slot a span reaches over is an empty cell
//   relation.spans()          whether any cell spans columns
//   relation.caption()        the caption, or null
// =============================================================================

var _DOC_TABLE_ALIGNS = Object.freeze(["left", "center", "right"]);
var _DOC_TABLE_EMPHASES = Object.freeze(["strong", "muted"]);
var _DOC_TABLE_BADGES = Object.freeze(["success", "warning", "danger"]);

class DocTableRelation {

    constructor(spec) {
        spec = spec || {};
        var columns = DocTableRelation._columns(spec.columns);
        var rows = DocTableRelation._rows(spec.rows, columns);
        this._ids = Object.freeze(columns.map(function (c, j) { return "c" + j; }));
        this._columns = Object.freeze(columns);
        this._pks = Object.freeze(rows.map(function (r, i) { return "r" + i; }));
        this._rows = Object.freeze(rows);
        this._titled = columns.some(function (c) { return c.title !== ""; });
        this._spans = rows.some(function (r) { return r.some(function (c) { return c.colSpan > 1; }); });
        var caption = spec.caption;
        this._caption = (caption === undefined || caption === null || String(caption) === "") ? null : String(caption);
        Object.freeze(this);
    }

    columns() { return this._ids.slice(); }

    column(id) {
        var j = this._ids.indexOf(id);
        return j < 0 ? null : this._columns[j];
    }

    titled() { return this._titled; }

    view() { return this._pks.slice(); }

    cell(pk, col) {
        var i = this._pks.indexOf(pk), j = this._ids.indexOf(col);
        return (i < 0 || j < 0) ? null : this._rows[i][j];
    }

    spans() { return this._spans; }

    caption() { return this._caption; }

    static _columns(columns) {
        if (!Array.isArray(columns) || columns.length === 0) throw new Error("[DocTableRelation] columns is a list of at least one column");
        return columns.map(function (c, j) {
            var col = (typeof c === "string") ? { title: c } : c;
            if (!col || typeof col !== "object") throw new Error("[DocTableRelation] column " + j + " is a title or { title, align }, not " + JSON.stringify(c));
            var title = (col.title === undefined || col.title === null) ? "" : String(col.title);
            return Object.freeze({ title: title, align: DocTableRelation._one("column " + j + "'s align", col.align, _DOC_TABLE_ALIGNS) });
        });
    }

    static _rows(rows, columns) {
        if (!Array.isArray(rows)) throw new Error("[DocTableRelation] rows is a list of rows, each a list of cells");
        var n = columns.length;
        return rows.map(function (row, i) {
            if (!Array.isArray(row)) throw new Error("[DocTableRelation] row " + i + " is not a list of cells");
            var out = [], j = 0;
            row.forEach(function (c) {
                var cell = (c === null || c === undefined || typeof c !== "object") ? { text: c } : c;
                var at = "cell " + i + "," + j;
                if (cell.rowSpan > 1) throw new Error("[DocTableRelation] " + at + " spans " + cell.rowSpan + " rows: the grid spans columns only");
                var span = (cell.colSpan > 1) ? Math.floor(cell.colSpan) : 1;
                if (j + span > n) throw new Error("[DocTableRelation] row " + i + " says more cells than the " + n + " columns");
                var align = DocTableRelation._one(at + "'s align", cell.align, _DOC_TABLE_ALIGNS);
                out.push(Object.freeze({
                    text: (cell.text === undefined || cell.text === null) ? "" : String(cell.text),
                    align: align || columns[j].align,                                  // its own, else its column's
                    emphasis: DocTableRelation._one(at + "'s emphasis", cell.emphasis, _DOC_TABLE_EMPHASES),
                    badge: DocTableRelation._one(at + "'s badge", cell.badge, _DOC_TABLE_BADGES),
                    colSpan: span
                }));
                for (var k = 1; k < span; k++) out.push(DocTableRelation._empty(columns[j + k]));   // what a span reaches over
                j += span;
            });
            while (j < n) { out.push(DocTableRelation._empty(columns[j])); j++; }              // a row that says fewer
            return Object.freeze(out);
        });
    }

    static _empty(column) {
        return Object.freeze({ text: "", align: column.align, emphasis: null, badge: null, colSpan: 1 });
    }

    /** One of the words allowed, or none; anything else refused, by name. */
    static _one(what, value, allowed) {
        if (value === undefined || value === null || value === "") return null;
        if (allowed.indexOf(value) < 0) throw new Error("[DocTableRelation] " + what + " is one of " + allowed.join(", ") + ", not " + JSON.stringify(value));
        return value;
    }
}
