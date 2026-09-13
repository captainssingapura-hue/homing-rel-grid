package hue.captains.singapura.js.homing.relgrid.workbench;

import hue.captains.singapura.js.homing.core.Importable;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.relgrid.group.RelGridGroupModule;
import hue.captains.singapura.js.homing.workspace.LifecycleHint;
import hue.captains.singapura.js.homing.workspace.WorkspaceWidget;

import java.util.List;

/**
 * Several Chinese articles down one page: a poem, an illustration, a poem —
 * three members of a {@code RelGridGroup}, each an ordinary Han display over
 * its own article, with the domain's fences between them: a title above each
 * poem, the illustration itself, a colophon after the last.
 *
 * <p>The illustration is a member with nothing to present — a relation whose
 * View is empty and that owns no row — that keeps its identity and its fence. That is the
 * whole of how a picture stands between two tables: no special row, no
 * special cell, nothing the table has to know. The group shares nothing here
 * but the squares' widths, which every member already agrees on.</p>
 */
public final class HanArticlesWidget extends WorkspaceWidget<WorkspaceWidget._None, HanArticlesWidget> {

    public static final HanArticlesWidget INSTANCE = new HanArticlesWidget();

    private HanArticlesWidget() {}

    private record construct() implements WorkspaceWidget._Construct<_None, HanArticlesWidget> {}

    @Override protected _Construct<_None, HanArticlesWidget> construct() { return new construct(); }
    @Override public Class<_None> paramsType() { return _None.class; }
    @Override public String title() { return "Articles"; }
    @Override public LifecycleHint lifecycleHint() { return LifecycleHint.MULTI; }

    @Override
    protected List<ModuleImports<? extends Importable>> bodyImports() {
        return List.of(
                new ModuleImports<>(List.of(new RelGridGroupModule.RelGridGroup()), RelGridGroupModule.INSTANCE),
                new ModuleImports<>(List.of(new HanStore.createHanStore()), HanStore.INSTANCE),
                new ModuleImports<>(List.of(new HanRelation.createHanRelation()), HanRelation.INSTANCE),
                new ModuleImports<>(
                        List.of(new HanFences.createHanTitleFence(), new HanFences.createHanOrnamentFence(),
                                new HanFences.createHanColophonFence()),
                        HanFences.INSTANCE),
                new ModuleImports<>(
                        List.of(new WorkbenchStyles.wb_root(), new WorkbenchStyles.wb_han_host(),
                                new WorkbenchStyles.wb_hint(), new WorkbenchStyles.wb_status()),
                        WorkbenchStyles.INSTANCE));
    }

    /** Two poems; each its own article, in memory. */
    static final String FENGQIAO = "月落乌啼霜满天，\n江枫渔火对愁眠。\n姑苏城外寒山寺，\n夜半钟声到客船。";
    static final String JINGYESI = "床前明月光，\n疑是地上霜。\n举头望明月，\n低头思故乡。";

    @Override
    protected List<String> constructBodyJs() {
        return List.of(
                "    var COLS = " + HanArticle.COLS + ", SIDE = " + HanArticle.SIDE + ";",
                "    var owner = Object.freeze({ toString: function () { return 'han-articles'; } });",
                "",
                "    var root = branch.createElement('root', 'div');",
                "    css.addClass(root, wb_root);",
                "    var hint = branch.createElement('hint', 'div');",
                "    css.addClass(hint, wb_hint);",
                "    root.appendChild(hint);",
                "    var host = branch.createElement('host', 'div');",
                "    css.addClass(host, wb_han_host);",
                "    root.appendChild(host);",
                "    var status = branch.createElement('status', 'div');",
                "    css.addClass(status, wb_status);",
                "    root.appendChild(status);",
                "",
                "    hint.textContent = 'ARTICLES \\u2014 two poems down one page with an illustration between, three members of a GROUP. Each poem is an ordinary display over its own article, wired as it would be alone. The illustration is a member with nothing to present \\u2014 a relation whose View is empty \\u2014 that keeps its identity and its fence, and the fence is where the picture is drawn: no special row, no special cell, nothing the table knows. Titles and the colophon are fences too, the domain\\u2019s.';",
                "",
                "    var POEMS = [",
                "        { id: 'fengqiao', title: '\\u6953\\u6a4b\\u591c\\u6cca', author: '\\u5f35\\u7e7c', text: " + HanStressWidget.jsString(FENGQIAO) + " },",
                "        { id: 'jingyesi', title: '\\u975c\\u591c\\u601d', author: '\\u674e\\u767d', text: " + HanStressWidget.jsString(JINGYESI) + " }",
                "    ];",
                "    // EXACTLY TWO BRANCHES under this widget's. 'grid' is handed whole to the group,",
                "    // which activates it and gives each member's grid a sub-branch of it. 'domain' is",
                "    // this widget's to divide: a part per relation for its cells' squares, and one per",
                "    // fence for what it draws. Neither side ever sees the other's.",
                "    var gridB = branch.createBranch('grid');",
                "    var domainB = branch.createBranch('domain');",
                "    domainB.activate(owner);",
                "    var relations = {};",
                "    function poemMember(p) {",
                "        var store = createHanStore(p.text, { key: null });",
                "        var relation = createHanRelation(store, { cols: COLS, branch: domainB.createBranch('cells-' + p.id) });",
                "        relations[p.id] = relation;",
                "        return {",
                "            id: p.id,",
                "            fence: createHanTitleFence({ branch: domainB.createBranch('fence-' + p.id), title: p.title, author: p.author }),",
                "            grid: {",
                "                relation: relation,",
                "                header: { show: false },",
                "                columnView: relation.presentedColumns(),",
                "                minColumnWidth: SIDE / 2,",
                "                mergedCells: true,",
                "                label: 'Han article \\u2014 ' + p.id",
                "            }",
                "        };",
                "    }",
                "    // The illustration: a member with NOTHING to present, and a relation that",
                "    // says exactly that — the poems' columns, so the group's widths fit it; an",
                "    // empty View, so no square is ever asked for; and no row of its own, so a",
                "    // cell asked for is a stranger. Its fence carries the picture.",
                "    function illustrationMember() {",
                "        var squares = [];",
                "        for (var k = 0; k < COLS; k++) squares.push('c' + k);",
                "        var relation = {",
                "            view:    function () { return []; },",
                "            columns: function () { return ['lead'].concat(squares, ['trail']); },",
                "            cellFor: function (pk) { throw new Error('the illustration owns no row: ' + pk); }",
                "        };",
                "        return {",
                "            id: 'moon',",
                "            fence: createHanOrnamentFence({ branch: domainB.createBranch('fence-moon'), glyph: '\\u263E', note: '\\u5bd2\\u5c71\\u5bfa \\u00b7 \\u591c\\u534a\\u9418\\u8072' }),",
                "            grid: { relation: relation, header: { show: false }, columnView: squares, label: 'illustration' }",
                "        };",
                "    }",
                "    var members = [poemMember(POEMS[0]), illustrationMember(), poemMember(POEMS[1])];",
                "",
                "    // Squares: every column one width, the half-squares half of it — the",
                "    // group's widths, which every member takes. The host is as wide as the",
                "    // nine squares; neither poem squeezes a mark, so neither presents a",
                "    // half-square column.",
                "    var widths = { lead: SIDE / 2, trail: SIDE / 2 };",
                "    for (var k = 0; k < COLS; k++) widths['c' + k] = SIDE;",
                "    var group = new RelGridGroup({",
                "        container: host,",
                "        branch: gridB,",
                "        members: members,",
                "        fence: createHanColophonFence({ branch: domainB.createBranch('fence-colophon'), text: '\\u2014 \\u5510\\u8a69\\u4e8c\\u9996 \\u2014' }),",
                "        columnWidths: widths,",
                "        header: 'each',                    // a manuscript has no column heads: every member says none",
                "        label: 'Articles'",
                "    });",
                "    host.style.width = (COLS * SIDE + 2) + 'px';",
                "",
                "    var glyphs = 0, rows = 0;",
                "    Object.keys(relations).forEach(function (id) { glyphs += relations[id].glyphs(); rows += relations[id].rows(); });",
                "    status.textContent = 'members ' + group.members().join(', ') + '   |   ' + glyphs + ' glyphs in ' + rows + ' rows'",
                "                       + '   |   the illustration presents ' + group.member('moon').viewMaps().rows() + ' rows';",
                "",
                "    return { root: root, setActive: function (active) {} };"
        );
    }
}
