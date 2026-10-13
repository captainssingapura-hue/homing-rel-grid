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

    /** A copy: what the cells say, as text a block apart, and as a table. Any other question: nothing. */
    _answer(question) {
        if (!(question instanceof RelGridCopyRequested)) return undefined;
        var relation = this._relation, texts = [], html = [];
        question.blocks.forEach(function (block) {
            var lines = [], trs = [];
            block.pks.forEach(function (pk) {
                var said = block.columns.map(function (col) { var c = relation.cell(pk, col); return c ? c.text : ""; });
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

    static _escaped(text) {
        return String(text).replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;");
    }
}
