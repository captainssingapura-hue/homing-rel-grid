// =============================================================================
// ProsePagesModule — the Document Table bench's pages: a document's tables, as a
// document has them, each page a heading, its ledes, and ProseTables between -
// the house's words throughout. Components, made on the branch each is handed:
// the bench's widgets place them, and the page is what they show.
//
//   new ProseNotesPage(branch)       release notes, and what each surface offers
//   new ProseReferencePage(branch)   the table's own options, its code drawn inline
//   new ProseFiguresPage(branch)     a table wider than its page: twelve months across
//   page.root   page.grid()   the first table's grid, for the keys   page.dispose()
// =============================================================================

const _prosePages = Object.freeze({ toString: () => "prose bench page" });

/** What every page is: a column of the house's words and tables, on its own branch. */
class ProsePage {

    constructor(branch) {
        this._branch = branch;
        branch.activate(_prosePages);
        this.root = branch.createElement("page", "div");
        css.addClass(this.root, wb_page);
        this._tables = [];
        this._n = 0;
    }

    heading(text) {
        var h = new Heading2(this._branch.elementMaker("h" + (++this._n)), { text: text }, ROLE.STANDALONE);
        this.root.appendChild(h.el);
    }

    lede(text) {
        var l = new Lede(this._branch.elementMaker("p" + (++this._n)), { text: text }, ROLE.STANDALONE);
        this.root.appendChild(l.el);
    }

    /** A table: what it says, judged as a DocTableRelation when made, drawn as the options say. */
    table(says, opts) {
        var t = new ProseTable(this._branch.createBranch("t" + (++this._n)), new DocTableRelation(says), opts);
        this.root.appendChild(t.root);
        this._tables.push(t);
        return t;
    }

    /** The first table's grid: where the keys go when the page is given them. */
    grid() { return this._tables.length ? this._tables[0].grid : null; }

    dispose() {
        this._tables.forEach(function (t) { t.dispose(); });
        this._tables = [];
        this._branch.dissolve();
    }
}

class ProseNotesPage extends ProsePage {
    constructor(branch) {
        super(branch);
        this.heading("Release notes");
        this.lede("Every release of Lantern, what it shipped and how widely it was taken up. The tables are the document's own - "
            + "drawn as the house draws a table in a document - and grids all the same: click one, walk it with the arrow keys, "
            + "select a range with Shift and copy it.");
        this.table({
            caption: "Releases, newest first",
            columns: ["Version", "Released", { title: "State", align: "center" }, { title: "Downloads", align: "right" }, "Notes"],
            rows: [
                ["2.4.0", "12 Oct 2026", { text: "beta", badge: "warning" }, "-", "Tables in documents drawn by the document's own design; a copy carries what the cells drew, as tab-separated text."],
                ["2.3.2", "2 Oct 2026", { text: "current", badge: "success" }, "18,204", "Workspaces open what they are asked, by name or by id."],
                ["2.3.1", "21 Sep 2026", { text: "stable", badge: "success" }, "9,877", "Floating panes are tabs in transit; a desk holds them all."],
                ["2.3.0", "28 Aug 2026", { text: "withdrawn", badge: "danger" }, "1,342", { text: "Pulled for a focus regression: take 2.3.1.", emphasis: "muted" }],
                ["2.2.0", "19 Aug 2026", { text: "stable", badge: "success" }, "12,560", "Cells hold control on Enter; a question waits on the channel."]
            ]
        });
        this.lede("What each surface offers, at a glance. A row that spans the table heads the rows under it.");
        this.table({
            caption: "What each surface offers",
            columns: ["Feature", { title: "Grid", align: "center" }, { title: "Tree", align: "center" }, { title: "Group", align: "center" }],
            rows: [
                [{ text: "Navigation", emphasis: "strong", colSpan: 4 }],
                ["Arrow keys, Home and End", "yes", "yes", "yes"],
                ["Page Up and Page Down", "yes", "yes", "-"],
                ["A step over a fence", "-", "-", "yes"],
                [{ text: "Editing", emphasis: "strong", colSpan: 4 }],
                ["A cell takes control on Enter", "yes", "-", "yes"],
                ["A question waits on the channel", "yes", "yes", "yes"],
                [{ text: "Out of the box", emphasis: "strong", colSpan: 4 }],
                ["Stock cells", "yes", "yes", { text: "its members'", emphasis: "muted" }],
                ["A design of its frame", "yes", { text: "to come", emphasis: "muted" }, { text: "to come", emphasis: "muted" }]
            ]
        });
    }
}

class ProseReferencePage extends ProsePage {
    constructor(branch) {
        super(branch);
        var self = this, n = 0, code = new Look(Object.freeze({ LEAF: "code" }), ROLE.STANDALONE);
        // An inline drawing, as a document hands one in: what is between backticks is the house's code.
        var inline = function (text, el) {
            String(text).split("`").forEach(function (part, i) {
                if (part === "") return;
                var span = self._branch.createElement("i" + (++n), i % 2 ? "code" : "span");
                if (i % 2) code.wear(span);
                span.textContent = part;
                el.appendChild(span);
            });
        };
        this.heading("ProseTable");
        this.lede("A document's table out of the box: what it says is a DocTableRelation - columns and rows of text, judged "
            + "whole when it is made and frozen - and a ProseTable draws it, taking nothing else. A new rule for what a table may "
            + "say is the relation's to check: neither a caller nor the grid changes. This page's cells are drawn by an inline "
            + "drawing that sets what is between backticks in the house's code - as a document hands in its own.");
        this.table({
            caption: "What each is handed",
            columns: ["Option", "Takes", "What it says"],
            rows: [
                [{ text: "`new DocTableRelation({ columns, rows, caption })` - what the table says", emphasis: "strong", colSpan: 3 }],
                ["`columns`", "titles, or `{ title, align }`", "The table's columns. A title empty in every column: no head."],
                ["`rows`", "lists of text, or `{ text, align, emphasis, badge, colSpan }`",
                    "Each row's cells. A row may say fewer than there are columns; the rest stand empty. A row span is refused."],
                ["`caption`", "text", "Above the table, in the house's caption; the grid's label unless one is given."],
                [{ text: "`new ProseTable(branch, relation, { inline, label, design })` - how it is drawn", emphasis: "strong", colSpan: 3 }],
                ["`relation`", "a `DocTableRelation`", "What it draws - and nothing else: a lookalike is refused."],
                ["`inline`", "`(text, el)`", "How a cell's text is drawn: `el.textContent` unless a document says otherwise."],
                ["`label`", "text", "The grid's label, for those who cannot see the caption."],
                ["`design`", "a design of `grid-frame`", "What its frame is drawn in: `HouseProse` unless another is handed."]
            ]
        }, { inline: inline });
        this.lede("And a table with no head, as a document sets out facts by name.");
        this.table({
            caption: "At a glance",
            columns: ["", ""],
            rows: [
                [{ text: "Module", emphasis: "strong" }, "`rel-grid-prose`"],
                [{ text: "Relation", emphasis: "strong" }, "`DocTableRelation`: what a table may say, its own to check"],
                [{ text: "Crate", emphasis: "strong" }, "`RelGridProseCrate`, beside the grid's and the house's"],
                [{ text: "Design", emphasis: "strong" }, "`HouseProse`: a data cell's rules, a header cell's head - the theme's own"],
                [{ text: "Keys", emphasis: "strong" }, "The grid's: walk, select, copy. No cell is ever offered control."]
            ]
        }, { inline: inline });
    }
}

class ProseFiguresPage extends ProsePage {
    constructor(branch) {
        super(branch);
        var months = ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"];
        var regions = [["North", 412], ["East", 386], ["South", 298], ["West", 455]];
        var rows = regions.map(function (r, i) {
            return [r[0]].concat(months.map(function (m, k) {
                var v = Math.round(r[1] * (1 + 0.04 * k) + ((i * 7 + k * 13) % 23));
                return k >= 10 ? { text: String(v), emphasis: "muted" } : String(v);   // the last two months estimated
            }));
        });
        rows.push([{ text: "All", emphasis: "strong" }].concat(months.map(function (m, k) {
            var sum = rows.reduce(function (s, row) { return s + Number(typeof row[k + 1] === "string" ? row[k + 1] : row[k + 1].text); }, 0);
            return { text: String(sum), emphasis: "strong" };
        })));
        this.heading("A table wider than its page");
        this.lede("Thirteen columns in a narrow pane: each column as wide as it needs, and the table scrolling across inside its box. "
            + "The figures stand to the right; the last two months are estimates, set back; the totals are strong.");
        this.table({
            caption: "Orders by region, 2026",
            columns: ["Region"].concat(months.map(function (m) { return { title: m, align: "right" }; })),
            rows: rows
        });
    }
}
