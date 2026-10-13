// =============================================================================
// ProseTableModule — a document's table, out of the box (RFC 0050 · Episode 2,
// Appendix: The Frame; rel-grid-prose): columns and rows of text handed in,
// and a grid that reads as a document's table - drawn in the house's document
// table (HouseProse), its cells the house's data and header cells. Read only:
// no cell is ever offered control. The keys, when it holds them, walk it, and
// a copy carries what its cells say.
//
//   new ProseTable(branch, {
//       columns,     // [ title | { title, align? } ] - a title empty in every column: no header
//       rows,        // [ [ text | { text, align?, emphasis?, badge?, colSpan? } ] ]
//                    //   a row may say fewer cells than there are columns; the rest stand empty.
//                    //   rowSpan > 1 is refused: the grid spans columns only.
//       caption?,    // the table's caption, above it, in the house's caption
//       inline?,     // (text, el) - how a cell's text is drawn; el.textContent by default.
//                    //   A document hands in its own: its links, its code, its emphasis.
//       label?,      // the grid's aria-label; the caption's text by default
//       design?      // the design of the grid's frame; the house's document table by default
//   })
//   table.root   the element to place     table.grid   the grid, for the keys
//   table.dispose()
//
// The branch is the table's OWN, handed unactivated: it activates it, mints its
// root and caption on it, and hands the grid, the cells and the header cells a
// sub-branch each; dispose() dissolves it, and with it all of theirs.
// =============================================================================

var _PROSE_ALIGNS = Object.freeze(["left", "center", "right"]);
var _PROSE_EMPHASES = Object.freeze(["strong", "muted"]);
var _PROSE_BADGES = Object.freeze(["success", "warning", "danger"]);

/** A leaf of the house worn whole on one of the table's own elements: one look per leaf, made once. */
function _proseTableWear(leaf, el) {
    var looks = _proseTableWear._looks || (_proseTableWear._looks = {});
    (looks[leaf] || (looks[leaf] = new Look(Object.freeze({ LEAF: leaf }), ROLE.STANDALONE))).wear(el);
    return el;
}

/** What a cell's text becomes when the table is told nothing: the text itself. */
function _proseText(text, el) { el.textContent = text; }

class ProseTable {

    constructor(branch, opts) {
        opts = opts || {};
        if (!branch) throw new Error("[ProseTable] a branch is required: the table's own");
        var self = this, columns = ProseTable._columns(opts.columns), rows = ProseTable._rows(opts.rows, columns.length);
        var inline = (opts.inline === undefined) ? _proseText : opts.inline;
        if (typeof inline !== "function") throw new Error("[ProseTable] inline is (text, el) - how a cell's text is drawn");
        this._branch = branch;
        this._branch.activate(this);
        var root = this.root = branch.createElement("table", "div");
        css.addClass(root, prose_table);
        _proseTableWear("table", root);
        if (opts.caption !== undefined && opts.caption !== null && String(opts.caption) !== "") {
            var caption = branch.createElement("caption", "div");
            css.addClass(caption, prose_caption);
            _proseTableWear("caption", caption);
            inline(String(opts.caption), caption);
            root.appendChild(caption);
        }
        var box = branch.createElement("box", "div");
        css.addClass(box, prose_box);
        root.appendChild(box);

        // the cells and their heads, the table's own: made now, each on a sub-branch of its own
        var cellsB = this._cellsB = branch.createBranch("cells"), headsB = this._headsB = branch.createBranch("heads");
        cellsB.activate(this);
        headsB.activate(this);
        var ids = this._ids = columns.map(function (c, j) { return "c" + j; }), seq = 0;
        var heads = this._heads = new Map(), cells = this._cells = new Map(), pks = this._pks = [];
        columns.forEach(function (c, j) {
            heads.set(ids[j], new ProseHeader({ branch: headsB.createBranch("h" + j), title: c.title, align: c.align, inline: inline }));
        });
        var spans = false;
        rows.forEach(function (row, i) {
            var pk = "r" + i;
            pks.push(pk);
            row.forEach(function (cell, j) {
                if (cell.colSpan > 1) spans = true;
                var align = cell.align || columns[j].align;   // a cell's own alignment, else its column's
                cells.set(pk + " " + ids[j], new ProseCell({ branch: cellsB.createBranch("c" + (++seq)), text: cell.text, align: align,
                                                             emphasis: cell.emphasis, badge: cell.badge, colSpan: cell.colSpan, inline: inline }));
            });
        });
        var titled = columns.some(function (c) { return c.title !== ""; });
        var relation = {
            view: function (intent) { return intent ? null : pks.slice(); },
            columns: function () { return ids.slice(); },
            readOnlyColumns: function () { return ids.slice(); },        // a document's table: no cell is ever asked
            headerFor: function (col) { return heads.get(col); },
            cellFor: function (pk, col) {
                var cell = cells.get(pk + " " + col);
                if (!cell) throw new Error("[ProseTable] no cell " + pk + " " + col);
                return cell;
            }
        };
        this.grid = new RelGrid({
            container: box, branch: branch.createBranch("grid"), relation: relation,
            design: opts.design || HouseProse,
            header: { show: titled },
            mergedCells: spans,
            label: opts.label || (opts.caption ? String(opts.caption) : null),
            ask: function (question) { return Promise.resolve(self._answer(question)); }
        });
    }

    /** A copy: what the cells say, as text a block apart, and as a table. Any other question: nothing. */
    _answer(question) {
        if (!(question instanceof RelGridCopyRequested)) return undefined;
        var cells = this._cells, texts = [], html = [];
        question.blocks.forEach(function (block) {
            var lines = [], trs = [];
            block.pks.forEach(function (pk) {
                var said = block.columns.map(function (col) { var c = cells.get(pk + " " + col); return c ? c.text() : ""; });
                lines.push(said.join("\t"));
                trs.push("<tr>" + said.map(function (t) { return "<td>" + ProseTable._escaped(t) + "</td>"; }).join("") + "</tr>");
            });
            texts.push(lines.join("\n"));
            html.push("<table>" + trs.join("") + "</table>");
        });
        return new RelGridClipboardContent(texts.join("\n\n"), html.join(""));
    }

    /** The table's own, dissolved: the grid first, then its cells and heads, then its branch. */
    dispose() {
        if (this.grid) { this.grid.destroy(); this.grid = null; }
        this._cells.forEach(function (c) { c.dispose(); });
        this._heads.forEach(function (h) { h.dispose(); });
        this._cells.clear();
        this._heads.clear();
        this._branch.dissolve();
    }

    static _columns(columns) {
        if (!Array.isArray(columns) || columns.length === 0) throw new Error("[ProseTable] columns is a list of at least one column");
        return columns.map(function (c, j) {
            var col = (typeof c === "string") ? { title: c } : (c || {});
            var title = (col.title === undefined || col.title === null) ? "" : String(col.title);
            return { title: title, align: ProseTable._one("column " + j + "'s align", col.align, _PROSE_ALIGNS) };
        });
    }

    static _rows(rows, n) {
        if (!Array.isArray(rows)) throw new Error("[ProseTable] rows is a list of rows, each a list of cells");
        return rows.map(function (row, i) {
            if (!Array.isArray(row)) throw new Error("[ProseTable] row " + i + " is not a list of cells");
            var out = [], j = 0;
            row.forEach(function (c) {
                var cell = (c === null || c === undefined || typeof c !== "object") ? { text: c } : c;
                var at = "cell " + i + "," + j;
                if (cell.rowSpan > 1) throw new Error("[ProseTable] " + at + " spans " + cell.rowSpan + " rows: the grid spans columns only");
                var span = (cell.colSpan > 1) ? Math.floor(cell.colSpan) : 1;
                if (j + span > n) throw new Error("[ProseTable] row " + i + " says more cells than the " + n + " columns");
                out.push({ text: cell.text, colSpan: span, align: ProseTable._one(at + "'s align", cell.align, _PROSE_ALIGNS),
                           emphasis: ProseTable._one(at + "'s emphasis", cell.emphasis, _PROSE_EMPHASES),
                           badge: ProseTable._one(at + "'s badge", cell.badge, _PROSE_BADGES) });
                for (var k = 1; k < span; k++) out.push({ text: "", colSpan: 1 });   // the slots it reaches over: empty
                j += span;
            });
            while (j < n) { out.push({ text: "", colSpan: 1 }); j++; }               // a row that says fewer: the rest empty
            return out;
        });
    }

    /** One of the words allowed, or none; anything else refused, by name. */
    static _one(what, value, allowed) {
        if (value === undefined || value === null || value === "") return null;
        if (allowed.indexOf(value) < 0) throw new Error("[ProseTable] " + what + " is one of " + allowed.join(", ") + ", not " + JSON.stringify(value));
        return value;
    }

    static _escaped(text) {
        return String(text).replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;");
    }
}
