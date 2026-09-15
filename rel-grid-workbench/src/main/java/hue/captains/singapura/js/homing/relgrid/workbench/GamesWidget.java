package hue.captains.singapura.js.homing.relgrid.workbench;

import hue.captains.singapura.js.homing.core.Importable;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.relgrid.RelGridModule;
import hue.captains.singapura.js.homing.relgrid.RelGridStyles;
import hue.captains.singapura.js.homing.relgrid.protocol.RelGridProtocolModule;
import hue.captains.singapura.js.homing.workspace.LifecycleHint;
import hue.captains.singapura.js.homing.workspace.WorkspaceWidget;

import java.util.List;

/**
 * The Games Catalogue: seven hundred-odd releases in one table, sorted and
 * filtered from its <b>header cells</b> — which are the relation's, not the
 * grid's. Click a header to sort, shift-click to sort by it after what is
 * already sorted, open its funnel to choose rows by that column. The grid
 * knows none of this: it placed the relation's header cells in its header
 * slots as it places cells in its body slots, and when the relation's View
 * changes this widget — the common parent — tells the grid so, unasked, and
 * the grid asks {@code view()} again.
 *
 * <p>Two branches under this widget's, as everywhere: {@code grid}, the grid's
 * to activate; {@code domain}, this widget's to divide — the relation's cells
 * and header cells on one part, the popovers wherever a header cell puts them
 * in this widget's root.</p>
 */
public final class GamesWidget extends WorkspaceWidget<WorkspaceWidget._None, GamesWidget> {

    public static final GamesWidget INSTANCE = new GamesWidget();

    private GamesWidget() {}

    private record construct() implements WorkspaceWidget._Construct<_None, GamesWidget> {}

    @Override protected _Construct<_None, GamesWidget> construct() { return new construct(); }
    @Override public Class<_None> paramsType() { return _None.class; }
    @Override public String title() { return "Catalogue"; }
    @Override public LifecycleHint lifecycleHint() { return LifecycleHint.MULTI; }

    @Override
    protected List<ModuleImports<? extends Importable>> bodyImports() {
        return List.of(
                new ModuleImports<>(List.of(new RelGridModule.RelGrid()), RelGridModule.INSTANCE),
                new ModuleImports<>(List.of(new RelGridStyles.hrg_frame(), new RelGridStyles.hrg_lit()), RelGridStyles.INSTANCE),
                new ModuleImports<>(List.of(new RelGridProtocolModule.RelGridViewChanged()), RelGridProtocolModule.INSTANCE),
                new ModuleImports<>(List.of(new GamesStore.createGamesStore()), GamesStore.INSTANCE),
                new ModuleImports<>(List.of(new GamesRelation.createGamesRelation()), GamesRelation.INSTANCE),
                new ModuleImports<>(
                        List.of(new WorkbenchStyles.wb_root(), new WorkbenchStyles.wb_frame(), new WorkbenchStyles.wb_port(),
                                new WorkbenchStyles.wb_hint(), new WorkbenchStyles.wb_bar(),
                                new WorkbenchStyles.wb_btn(), new WorkbenchStyles.wb_status()),
                        WorkbenchStyles.INSTANCE));
    }

    @Override
    protected List<String> constructBodyJs() {
        return List.of(
                "    var owner = Object.freeze({ toString: function () { return 'games'; } });",
                "",
                "    var root = branch.createElement('root', 'div');",
                "    css.addClass(root, wb_root);",
                "    var hint = branch.createElement('hint', 'div');",
                "    css.addClass(hint, wb_hint);",
                "    root.appendChild(hint);",
                "    var bar = branch.createElement('bar', 'div');",
                "    css.addClass(bar, wb_bar);",
                "    root.appendChild(bar);",
                "    // The frame goes round the SCROLLPORT — a non-scrolling wrapper the port fills — so the",
                "    // light and the hairline sit outside the scrollbar; the grid inside draws no frame of its own.",
                "    var frame = branch.createElement('frame', 'div');",
                "    css.addClass(frame, wb_frame, hrg_frame, hrg_lit);",
                "    root.appendChild(frame);",
                "    var host = branch.createElement('host', 'div');",
                "    css.addClass(host, wb_port);",
                "    frame.appendChild(host);",
                "    var status = branch.createElement('status', 'div');",
                "    css.addClass(status, wb_status);",
                "    root.appendChild(status);",
                "",
                "    // EXACTLY TWO BRANCHES under this widget's: 'grid', handed whole to the grid; 'domain',",
                "    // this widget's to divide — 'cells', the relation's own, its cells AND its header cells.",
                "    var gridB = branch.createBranch('grid');",
                "    var domainB = branch.createBranch('domain');",
                "    domainB.activate(owner);",
                "    var cellsB = domainB.createBranch('cells');",
                "",
                "    hint.textContent = 'CATALOGUE \\u2014 " + GamesDataset.rows() + " releases, 1990 to 2020, every FIFA and Madden and Need for Speed a row of its own. The header cells are the RELATION\\u2019S, placed in the grid\\u2019s header slots as cells are placed in its body. A header\\u2019s caret and number only SAY how it sorts; its \\u25BE opens the column\\u2019s menu, where the sorting and the filtering are done: sort at the top, then a search over the column\\u2019s values, each with a count and a box, a range for a number \\u2014 staged until OK. The grid knows none of it. When the relation\\u2019s View changes this widget tells the grid so, unasked, and the grid asks view() again \\u2014 the cursor keeps its identity, the header stays put. Absent sales and scores sort last either way, on purpose.';",
                "",
                "    var grid = null;",
                "    var store = createGamesStore(" + HanStressWidget.jsString(GamesDataset.psv()) + ");",
                "    var relation = createGamesRelation(store, {",
                "        branch: cellsB,",
                "        menuHost: root,                        // a header cell's column menu goes here, placed by the header's rectangle",
                "        // The relation's View changed underneath the grid: the common parent tells the grid,",
                "        // unasked, and the grid asks view() again. The relation never learns there is a grid.",
                "        onViewChanged: function () { if (grid) grid.tell(new RelGridViewChanged()); report(); }",
                "    });",
                "",
                "    grid = new RelGrid({",
                "        container: host,",
                "        branch: gridB,",
                "        relation: relation,",
                "        header: { show: true, sticky: true },",
                "        frame: false,                          // the host frames the scrollport; see above",
                "        label: 'Games catalogue',",
                "        onCursorMoved: function () { report(); }",
                "    });",
                "    grid.setColumnWidths({ title: 260, series: 150, year: 64, platform: 100, type: 130, developer: 160, publisher: 140, sales: 80, score: 64 });",
                "",
                "    function report() {",
                "        var c = grid ? grid.cursor() : null;",
                "        status.textContent = relation.count() + ' of ' + store.size() + ' releases   |   ' + relation.describe()",
                "                           + '   |   cursor ' + (c ? c.pk + '/' + c.column : '\\u2014')",
                "                           + '   |   cells owned ' + relation.cellCount() + ', header cells ' + relation.headerCount();",
                "    }",
                "    report();",
                "",
                "    var btnSeq = 0;",
                "    function btn(label, fn) {",
                "        var x = branch.createElement('btn' + (++btnSeq), 'button');",
                "        css.addClass(x, wb_btn);",
                "        x.textContent = label;",
                "        x.addEventListener('click', function () { fn(); report(); grid.focus(); });",
                "        bar.appendChild(x);",
                "    }",
                "    // Presets: conditions as data, set on the relation as a header cell would set them.",
                "    btn('as catalogued', function () { relation.clear(); });",
                "    btn('best rated', function () { relation.setConditions({ sort: [{ column: 'score', dir: 'desc' }, { column: 'year', dir: 'asc' }], filters: {} }); });",
                "    btn('best sellers', function () { relation.setConditions({ sort: [{ column: 'sales', dir: 'desc' }], filters: {} }); });",
                "    btn('Nintendo, newest first', function () { relation.setConditions({ sort: [{ column: 'year', dir: 'desc' }, { column: 'title', dir: 'asc' }], filters: { publisher: { contains: 'Nintendo' } } }); });",
                "    btn('shooters of the 2000s', function () { relation.setConditions({ sort: [{ column: 'score', dir: 'desc' }], filters: { type: { in: ['Shooter'] }, year: { min: 2000, max: 2009 } } }); });",
                "    btn('every FIFA', function () { relation.setConditions({ sort: [{ column: 'year', dir: 'asc' }], filters: { series: { contains: 'FIFA' } } }); });",
                "",
                "    return { root: root, setActive: function (active) {} };"
        );
    }
}
