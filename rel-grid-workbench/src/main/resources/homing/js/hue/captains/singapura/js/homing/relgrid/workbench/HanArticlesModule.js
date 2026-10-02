// =============================================================================
// HanArticles — two poems down one page with an illustration between, three
// members of a GROUP. Each poem is an ordinary display over its own article,
// wired as it would be alone. The illustration is a member with nothing to
// present - a relation whose View is empty - that keeps its identity and its
// fence, and the fence is where the picture is drawn: no special row, no
// special cell, nothing the table knows. Titles and the colophon are fences
// too, the domain's.
//
// EXACTLY TWO BRANCHES under the widget's. 'grid' is handed whole to the
// group, which activates it and gives each member's grid a sub-branch of it.
// 'domain' is the widget's to divide: a part per relation for its cells'
// squares, and one per fence for what it draws.
//
//   new HanArticles(container, params)   params: none
//   (the rest is a BenchWidget's)
// =============================================================================

var _HAN_POEMS = [
    { id: "fengqiao", title: "楓橋夜泊", author: "張繼", text: "月落乌啼霜满天，\n江枫渔火对愁眠。\n姑苏城外寒山寺，\n夜半钟声到客船。" },
    { id: "jingyesi", title: "靜夜思", author: "李白", text: "床前明月光，\n疑是地上霜。\n举头望明月，\n低头思故乡。" }
];

class HanArticles extends BenchWidget {
    constructor(container, params) {
        super(container, "hanArticles", "Han articles");
        var self = this, body = this.body, COLS = 9, SIDE = 48;
        this.el("hint", "div", wb_hint, body,
            "ARTICLES — two poems down one page with an illustration between, three members of a GROUP. Each poem is an ordinary display over its own article, wired as it would be alone. The illustration is a member with nothing to present — a relation whose View is empty — that keeps its identity and its fence, and the fence is where the picture is drawn: no special row, no special cell, nothing the table knows. Titles and the colophon are fences too, the domain’s.");
        var host = this.el("host", "div", wb_han_host, body);
        var status = this.el("status", "div", wb_status, body);
        var gridB = this.branch.createBranch("grid");
        var domainB = this._domainB = this.branch.createBranch("domain");
        domainB.activate(Object.freeze({ toString: function () { return "han-articles"; } }));
        this._relations = {};

        var members = [this._poemMember(_HAN_POEMS[0], COLS, SIDE), this._illustrationMember(COLS), this._poemMember(_HAN_POEMS[1], COLS, SIDE)];

        // Squares: every column one width, the half-squares half of it - the group's widths,
        // which every member takes. The host is as wide as the nine squares; neither poem
        // squeezes a mark, so neither presents a half-square column.
        var widths = { lead: SIDE / 2, trail: SIDE / 2 };
        for (var k = 0; k < COLS; k++) widths["c" + k] = SIDE;
        var group = this._group = new RelGridGroup({
            container: host,
            branch: gridB,
            members: members,
            fence: new HanColophonFence({ branch: domainB.createBranch("fence-colophon"), text: "— 唐詩二首 —" }),
            columnWidths: widths,
            header: "each",                    // a manuscript has no column heads: every member says none
            label: "Articles"
        });
        host.style.setProperty("--wb-han-width", (COLS * SIDE + 2) + "px");
        this.keysTo = group;

        var glyphs = 0, rows = 0, relations = this._relations;
        Object.keys(relations).forEach(function (id) { glyphs += relations[id].glyphs(); rows += relations[id].rows(); });
        status.textContent = "members " + group.members().join(", ") + "   |   " + glyphs + " glyphs in " + rows + " rows"
                           + "   |   the illustration presents " + group.member("moon").viewMaps().rows() + " rows";
    }

    /** A poem: its own article, in memory; a relation over it; a title fence above it. */
    _poemMember(p, cols, side) {
        var store = new HanStore(p.text);
        var relation = new HanRelation(store, { cols: cols, branch: this._domainB.createBranch("cells-" + p.id) });
        this._relations[p.id] = relation;
        return {
            id: p.id,
            fence: new HanTitleFence({ branch: this._domainB.createBranch("fence-" + p.id), title: p.title, author: p.author }),
            grid: { relation: relation, header: { show: false }, columnView: relation.presentedColumns(),
                    minColumnWidth: side / 2, mergedCells: true, label: "Han article — " + p.id }
        };
    }

    /**
     * The illustration: a member with NOTHING to present, and a relation that says exactly that -
     * the poems' columns, so the group's widths fit it; an empty View, so no square is ever asked
     * for; and no row of its own, so a cell asked for is a stranger. Its fence carries the picture.
     */
    _illustrationMember(cols) {
        var squares = [];
        for (var k = 0; k < cols; k++) squares.push("c" + k);
        var relation = {
            view:    function () { return []; },
            columns: function () { return ["lead"].concat(squares, ["trail"]); },
            cellFor: function (pk) { throw new Error("the illustration owns no row: " + pk); }
        };
        return {
            id: "moon",
            fence: new HanOrnamentFence({ branch: this._domainB.createBranch("fence-moon"), glyph: "☾", note: "寒山寺 · 夜半鐘聲" }),
            grid: { relation: relation, header: { show: false }, columnView: squares, label: "illustration" }
        };
    }

    disposed() {
        if (typeof this._group.destroy === "function") this._group.destroy();
        var relations = this._relations;
        Object.keys(relations).forEach(function (id) { relations[id].dispose(); });
    }
}
