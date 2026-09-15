package hue.captains.singapura.js.homing.relgrid.workbench;

import hue.captains.singapura.js.homing.core.Importable;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.relgrid.protocol.RelGridProtocolModule;
import hue.captains.singapura.js.homing.reltree.RelTreeModule;
import hue.captains.singapura.js.homing.workspace.LifecycleHint;
import hue.captains.singapura.js.homing.workspace.WorkspaceWidget;

import java.util.List;

/**
 * The Games Tree: the catalogue's 724 releases as a tree, <i>type → series →
 * title</i>, lazily — the types closed at first, a series asked for when its
 * type unfolds, a title when its series does. The tree view owns the rows, the
 * indent, the carets, the cursor and the keys; the relation owns every cell and
 * its own fold state; the two speak on the ask channel, where an unfold and a
 * fold are questions answered with the whole View. Three types answer three
 * ways on purpose: most at once, Strategy late under the mask with a note on
 * the panel, Puzzle with nothing and then a tell.
 *
 * <p>Two branches under this widget's, as everywhere: {@code tree}, the tree's
 * to activate; {@code domain}, this widget's to divide — the relation's cells
 * on one part.</p>
 */
public final class GamesTreeWidget extends WorkspaceWidget<WorkspaceWidget._None, GamesTreeWidget> {

    public static final GamesTreeWidget INSTANCE = new GamesTreeWidget();

    private GamesTreeWidget() {}

    private record construct() implements WorkspaceWidget._Construct<_None, GamesTreeWidget> {}

    @Override protected _Construct<_None, GamesTreeWidget> construct() { return new construct(); }
    @Override public Class<_None> paramsType() { return _None.class; }
    @Override public String title() { return "Games tree"; }
    @Override public LifecycleHint lifecycleHint() { return LifecycleHint.MULTI; }

    @Override
    protected List<ModuleImports<? extends Importable>> bodyImports() {
        return List.of(
                new ModuleImports<>(List.of(new RelTreeModule.RelTree()), RelTreeModule.INSTANCE),
                new ModuleImports<>(List.of(new RelGridProtocolModule.RelTreeViewChanged()), RelGridProtocolModule.INSTANCE),
                new ModuleImports<>(List.of(new GamesStore.createGamesStore()), GamesStore.INSTANCE),
                new ModuleImports<>(List.of(new GamesTreeRelation.createGamesTreeRelation()), GamesTreeRelation.INSTANCE),
                new ModuleImports<>(
                        List.of(new WorkbenchStyles.wb_root(), new WorkbenchStyles.wb_host(),
                                new WorkbenchStyles.wb_hint(), new WorkbenchStyles.wb_bar(),
                                new WorkbenchStyles.wb_btn(), new WorkbenchStyles.wb_status()),
                        WorkbenchStyles.INSTANCE));
    }

    @Override
    protected List<String> constructBodyJs() {
        return List.of(
                "    var owner = Object.freeze({ toString: function () { return 'games tree'; } });",
                "",
                "    var root = branch.createElement('root', 'div');",
                "    css.addClass(root, wb_root);",
                "    var hint = branch.createElement('hint', 'div');",
                "    css.addClass(hint, wb_hint);",
                "    root.appendChild(hint);",
                "    var bar = branch.createElement('bar', 'div');",
                "    css.addClass(bar, wb_bar);",
                "    root.appendChild(bar);",
                "    var host = branch.createElement('host', 'div');",
                "    css.addClass(host, wb_host);",
                "    root.appendChild(host);",
                "    var status = branch.createElement('status', 'div');",
                "    css.addClass(status, wb_status);",
                "    root.appendChild(status);",
                "",
                "    // EXACTLY TWO BRANCHES under this widget's: 'tree', handed whole to the tree; 'domain',",
                "    // this widget's to divide — 'cells', the relation's own.",
                "    var treeB = branch.createBranch('tree');",
                "    var domainB = branch.createBranch('domain');",
                "    domainB.activate(owner);",
                "    var cellsB = domainB.createBranch('cells');",
                "",
                "    hint.textContent = 'GAMES TREE \\u2014 the catalogue\\u2019s " + GamesDataset.rows() + " releases as TYPE \\u2192 SERIES \\u2192 TITLE, lazily: the types are closed at first, and nothing under a closed node is asked for. The tree view owns the rows, the indent, the carets, the cursor and the keys (\\u2191\\u2193, \\u2192 to unfold or step in, \\u2190 to fold or step out, Space, Enter, Home, End); the relation owns every cell and its own fold state. An unfold and a fold are QUESTIONS on the ask channel, answered with the whole View. Three types answer three ways: most at once; STRATEGY late, with the tree locked and washed and the domain\\u2019s note on the panel; PUZZLE with nothing, then a tell when its series arrive \\u2014 the tree never locks.';",
                "",
                "    var tree = null;",
                "    var store = createGamesStore(" + HanStressWidget.jsString(GamesDataset.psv()) + ");",
                "    var relation = createGamesTreeRelation(store, {",
                "        branch: cellsB,",
                "        // The relation fetched what an unfold asked for and answered nothing at the time:",
                "        // the common parent tells the tree so, unasked, and the tree asks view() again.",
                "        onViewChanged: function () { if (tree) tree.tell(new RelTreeViewChanged()); report(); }",
                "    });",
                "",
                "    tree = new RelTree({",
                "        container: host,",
                "        branch: treeB,",
                "        relation: relation,",
                "        label: 'Games tree',",
                "        // THE CHANNEL: the relation answers; this widget only wires the two together.",
                "        // Reported after this turn: a question answered at once has settled by then and never reads as pending.",
                "        ask: function (question, mask) { var out = relation.answer(question, mask); setTimeout(report, 0); return out; },",
                "        onArranged: function () { report(); },",
                "        onCursorMoved: function () { report(); },",
                "        onActivated: function (key) { activated = key; report(); }",
                "    });",
                "    var activated = null;",
                "",
                "    function report() {",
                "        var c = tree ? tree.cursor() : null;",
                "        status.textContent = relation.describe()",
                "                           + '   |   cursor ' + (c === null ? '\\u2014' : c)",
                "                           + (activated === null ? '' : '   |   activated ' + activated)",
                "                           + (tree && tree.isPending() ? '   |   PENDING \\u2014 locked' : '');",
                "    }",
                "    report();",
                "",
                "    var btnSeq = 0;",
                "    function btn(label, fn) {",
                "        var x = branch.createElement('btn' + (++btnSeq), 'button');",
                "        css.addClass(x, wb_btn);",
                "        x.textContent = label;",
                "        x.addEventListener('click', function () { fn(); report(); tree.focus(); });",
                "        bar.appendChild(x);",
                "    }",
                "    // The navigator's move: the DOMAIN opens the ancestors — it owns the fold state — tells,",
                "    // and asks for the cursor. The tree opens nothing on its behalf.",
                "    btn('reveal Need for Speed: Most Wanted (2005)', function () {",
                "        var t = relation.typeKey('Racing'), s = relation.seriesKey('Racing', 'Need for Speed');",
                "        relation.open(t); relation.open(s);",
                "        tree.tell(new RelTreeViewChanged());",
                "        var pk = store.pks().filter(function (k) { return store.get(k, 'title') === 'Need for Speed: Most Wanted' && store.get(k, 'year') === 2005; })[0];",
                "        if (pk) tree.selectNode(pk);",
                "    });",
                "    btn('open every type', function () { relation.types().forEach(function (t) { relation.open(relation.typeKey(t)); }); tree.tell(new RelTreeViewChanged()); });",
                "    btn('close all', function () { relation.closeAll(); tree.tell(new RelTreeViewChanged()); });",
                "",
                "    return { root: root, setActive: function (active) {} };"
        );
    }
}
