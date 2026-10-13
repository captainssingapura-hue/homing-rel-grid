// =============================================================================
// ProseTableModule — a document's table, out of the box (RFC 0050 · Episode 2,
// Appendix: The Frame; rel-grid-prose): a DocTableRelation handed in, and a
// grid that reads as a document's table - drawn in the house's document table
// (HouseProse), its cells the house's data and header cells. Read only: no
// cell is ever offered control. The keys, when it holds them, walk it, and a
// copy carries what its cells say.
//
//   new ProseTable(branch, relation, {
//       inline?,     // (text, el) - how a cell's text is drawn; el.textContent by default.
//                    //   A document hands in its own: its links, its code, its emphasis.
//       label?,      // the grid's aria-label; the relation's caption by default
//       design?      // the design of the grid's frame; the house's document table by default
//   })
//   relation: a DocTableRelation, and nothing else - what the table may say is
//   judged there, when it is made; the table takes it as given.
//   table.root   the element to place     table.grid   the grid, for the keys
//   table.dispose()
//
// The branch is the table's OWN, handed unactivated: it activates it, mints its
// root and caption on it, and hands the grid, the cells and the header cells a
// sub-branch each; dispose() dissolves it, and with it all of theirs. The grid's
// relation - its cells - is the table's own, made from the one handed in.
// =============================================================================

/** A leaf of the house worn whole on one of the table's own elements: one look per leaf, made once. */
function _proseTableWear(leaf, el) {
    var looks = _proseTableWear._looks || (_proseTableWear._looks = {});
    (looks[leaf] || (looks[leaf] = new Look(Object.freeze({ LEAF: leaf }), ROLE.STANDALONE))).wear(el);
    return el;
}

/** What a cell's text becomes when the table is told nothing: the text itself. */
function _proseText(text, el) { el.textContent = text; }

class ProseTable {

    constructor(branch, relation, opts) {
        if (!branch) throw new Error("[ProseTable] a branch is required: the table's own");
        if (!(relation instanceof DocTableRelation))
            throw new Error("[ProseTable] a DocTableRelation is required: what a doc's table says, judged when it is made - not "
                    + (relation && relation.constructor ? relation.constructor.name : String(relation)));
        opts = opts || {};
        var self = this, inline = (opts.inline === undefined) ? _proseText : opts.inline;
        if (typeof inline !== "function") throw new Error("[ProseTable] inline is (text, el) - how a cell's text is drawn");
        this._branch = branch;
        this._branch.activate(this);
        this._relation = relation;
        var root = this.root = branch.createElement("table", "div");
        css.addClass(root, prose_table);
        _proseTableWear("table", root);
        if (relation.caption() !== null) {
            var caption = branch.createElement("caption", "div");
            css.addClass(caption, prose_caption);
            _proseTableWear("caption", caption);
            inline(relation.caption(), caption);
            root.appendChild(caption);
        }
        var box = branch.createElement("box", "div");
        css.addClass(box, prose_box);
        root.appendChild(box);

        // the cells and their heads, the table's own: made now, each on a sub-branch of its own
        var cellsB = branch.createBranch("cells"), headsB = branch.createBranch("heads");
        cellsB.activate(this);
        headsB.activate(this);
        var heads = this._heads = new Map(), cells = this._cells = new Map(), seq = 0;
        relation.columns().forEach(function (col, j) {
            var c = relation.column(col);
            heads.set(col, new ProseHeader({ branch: headsB.createBranch("h" + j), title: c.title, align: c.align, inline: inline }));
        });
        relation.view().forEach(function (pk) {
            relation.columns().forEach(function (col) {
                var c = relation.cell(pk, col);
                cells.set(pk + " " + col, new ProseCell({ branch: cellsB.createBranch("c" + (++seq)), text: c.text, align: c.align,
                                                          emphasis: c.emphasis, badge: c.badge, colSpan: c.colSpan, inline: inline }));
            });
        });
        var grid = {
            view: function (intent) { return intent ? null : relation.view(); },
            columns: function () { return relation.columns(); },
            readOnlyColumns: function () { return relation.columns(); },     // a document's table: no cell is ever asked
            headerFor: function (col) { return heads.get(col); },
            cellFor: function (pk, col) {
                var cell = cells.get(pk + " " + col);
                if (!cell) throw new Error("[ProseTable] no cell " + pk + " " + col);
                return cell;
            }
        };
        this.grid = new RelGrid({
            container: box, branch: branch.createBranch("grid"), relation: grid,
            design: opts.design || HouseProse,
            header: { show: relation.titled() },
            mergedCells: relation.spans(),
            label: opts.label || relation.caption(),
            ask: function (question) { return Promise.resolve(self._answer(question)); }
        });
    }

    /**
     * A copy, what the relation says: one cell, its value alone; two or more, each block its columns' titles over its
     * rows - tab-separated lines, a blank line between blocks, and a table with its head, a span a colspan. A relation
     * with no titles copies its rows alone. Any other question: nothing.
     */
    _answer(question) {
        if (!(question instanceof RelGridCopyRequested)) return undefined;
        var relation = this._relation, blocks = question.blocks;
        if (blocks.length === 1 && blocks[0].pks.length === 1 && blocks[0].columns.length === 1) {
            var one = relation.cell(blocks[0].pks[0], blocks[0].columns[0]);
            return new RelGridClipboardContent(one ? one.text : "", undefined);
        }
        var esc = ProseTable._escaped, texts = [], html = [];
        blocks.forEach(function (block) {
            var lines = [], rows = [];
            if (relation.titled()) {
                var titles = block.columns.map(function (col) { return relation.column(col).title; });
                lines.push(titles.join("\t"));
                rows.push("<thead><tr>" + titles.map(function (t) { return "<th>" + esc(t) + "</th>"; }).join("") + "</tr></thead>");
            }
            var body = [];
            block.pks.forEach(function (pk) {
                var said = [], tds = [];
                for (var k = 0; k < block.columns.length; k++) {
                    var c = relation.cell(pk, block.columns[k]);
                    said.push(c ? c.text : "");
                    var span = c ? Math.min(c.colSpan, block.columns.length - k) : 1;
                    tds.push("<td" + (span > 1 ? " colspan=\"" + span + "\"" : "") + ">" + esc(c ? c.text : "") + "</td>");
                    for (var s = 1; s < span; s++) said.push(c ? relation.cell(pk, block.columns[++k]).text : "");   // what it reaches over: in the text, empty
                }
                lines.push(said.join("\t"));
                body.push("<tr>" + tds.join("") + "</tr>");
            });
            rows.push("<tbody>" + body.join("") + "</tbody>");
            texts.push(lines.join("\n"));
            html.push("<table>" + rows.join("") + "</table>");
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

    static _escaped(text) {
        return String(text).replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;");
    }
}
