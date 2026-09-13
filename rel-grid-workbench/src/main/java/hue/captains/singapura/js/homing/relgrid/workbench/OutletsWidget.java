package hue.captains.singapura.js.homing.relgrid.workbench;

import hue.captains.singapura.js.homing.core.Importable;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.relgrid.group.RelGridGroupModule;
import hue.captains.singapura.js.homing.workspace.LifecycleHint;
import hue.captains.singapura.js.homing.workspace.WorkspaceWidget;

import java.util.List;

/**
 * Sales across outlets: one {@code RelGrid} per outlet, stacked in a
 * {@code RelGridGroup}, over the shared {@link SalesStore}. Every table is
 * wired exactly as it would be alone — its own relation, its own cells kept
 * current by the store — and knows nothing of the group. What the group adds
 * is the one thing the tables cannot agree on by themselves, column widths,
 * and the fences: each outlet's name and published totals above its book, the
 * ledger's below the last.
 *
 * <p>Nothing here is edited. Sales move the books, and the round that feeds
 * them live will feed this store.</p>
 */
public final class OutletsWidget extends WorkspaceWidget<WorkspaceWidget._None, OutletsWidget> {

    public static final OutletsWidget INSTANCE = new OutletsWidget();

    private OutletsWidget() {}

    private record construct() implements WorkspaceWidget._Construct<_None, OutletsWidget> {}

    @Override protected _Construct<_None, OutletsWidget> construct() { return new construct(); }
    @Override public Class<_None> paramsType() { return _None.class; }
    @Override public String title() { return "Outlets"; }
    @Override public LifecycleHint lifecycleHint() { return LifecycleHint.MULTI; }

    @Override
    protected List<ModuleImports<? extends Importable>> bodyImports() {
        return List.of(
                new ModuleImports<>(List.of(new RelGridGroupModule.RelGridGroup()), RelGridGroupModule.INSTANCE),
                new ModuleImports<>(List.of(new SalesStore.salesStoreShared()), SalesStore.INSTANCE),
                new ModuleImports<>(
                        List.of(new OutletRelation.createOutletRelation(), new OutletRelation.createOutletFence(),
                                new OutletRelation.createLedgerFence()),
                        OutletRelation.INSTANCE),
                new ModuleImports<>(
                        List.of(new WorkbenchStyles.wb_root(), new WorkbenchStyles.wb_host(),
                                new WorkbenchStyles.wb_hint(), new WorkbenchStyles.wb_bar(),
                                new WorkbenchStyles.wb_btn(), new WorkbenchStyles.wb_status()),
                        WorkbenchStyles.INSTANCE));
    }

    @Override
    protected List<String> constructBodyJs() {
        return List.of(
                "    var owner = Object.freeze({ toString: function () { return 'outlets'; } });",
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
                "    hint.textContent = 'OUTLETS \\u2014 the same six dishes sold at three outlets, one table each, stacked in a GROUP. Every table is an ordinary grid wired as it would be alone: its own relation over the shared ledger, its own cells kept current by the store, its own cursor and selection. The group adds the one thing separate tables cannot agree on by themselves \\u2014 column widths: drag a header edge on the first table, or Alt+\\u2190/\\u2192 on any, and every table follows \\u2014 and the fences between them, which are the domain\\u2019s: a name and its totals above each book, the ledger below the last. Trade at one outlet and only that book and that fence move. The \u25BE on a fence folds its book: the group hides the box and the table inside never knows \u2014 the toggle is the domain\u2019s, and it TELLS the group through the handle its fence was given.';",
                "",
                "    // The domain: one shared ledger; a relation per outlet over it, each",
                "    // keeping its own cells current; a fence per outlet, kept current the",
                "    // same way. The group is handed relations and fences and told nothing else.",
                "    var store = salesStoreShared();",
                "    var LABELS = { dish: 'dish', sold: 'sold', revenue: 'revenue', lastSale: 'last sale' };",
                "",
                "    // THE GROUP IS A VALUE: its header mode is fixed for its life, so the toggle",
                "    // below tears the group down and builds it again the other way — new relations,",
                "    // new fences, new cells — carrying over only what the host keeps of a group's",
                "    // state: its widths and which members were folded. The store is untouched.",
                "    var current = null;                    // { group, relations, fences, mode }",
                "    function build(mode, kept) {",
                "        var relations = {}, fences = [];",
                "        var members = store.outlets().map(function (o) {",
                "            var relation = createOutletRelation(store, o.id);",
                "            relations[o.id] = relation;",
                "            var cellsB = branch.createBranch('cells-' + o.id);",
                "            cellsB.activate(owner);",
                "            var fence = createOutletFence(store, o.id);",
                "            fences.push(fence);",
                "            return {",
                "                id: o.id,",
                "                fence: fence,",
                "                // Ordinary grid options — the same a table alone would take.",
                "                grid: { branch: cellsB, relation: relation, header: { labels: LABELS }, label: 'Outlet \\u2014 ' + o.name }",
                "            };",
                "        });",
                "        var ledger = createLedgerFence(store);",
                "        fences.push(ledger);",
                "        var group = new RelGridGroup({",
                "            container: host,",
                "            members: members,",
                "            fence: ledger,",
                "            header: mode,                      // 'group': one header at the top; 'each': one per table",
                "            columnWidths: kept.widths,",
                "            folded: kept.folded,",
                "            // REPORTS: widths once per change however many tables moved; a fold per member.",
                "            onColumnResized: function () { report(); },",
                "            onFolded: function () { report(); },",
                "            label: 'Outlets'",
                "        });",
                "        current = { group: group, relations: relations, fences: fences, mode: mode };",
                "    }",
                "    // What the host keeps of a group's state, and the rest taken down: the group",
                "    // destroyed (it disposes no fence), the fences and relations disposed by their",
                "    // owner, which is this widget, and the cells' branches dissolved so the names",
                "    // are free again.",
                "    function teardown() {",
                "        var c = current;",
                "        if (!c) return { widths: { dish: 140, sold: 80, revenue: 110, lastSale: 110 }, folded: [] };",
                "        var kept = { widths: c.group.columnWidths(), folded: c.group.members().filter(function (id) { return c.group.folded(id); }) };",
                "        current = null;",
                "        c.group.destroy();",
                "        c.fences.forEach(function (f) { f.dispose(); });",
                "        Object.keys(c.relations).forEach(function (id) { c.relations[id].dispose(); });",
                "        store.outlets().forEach(function (o) { branch.dissolveBranch('cells-' + o.id); });",
                "        return kept;",
                "    }",
                "    build('group', teardown());",
                "",
                "    function report() {",
                "        if (!current) return;",
                "        var group = current.group, relations = current.relations;",
                "        var w = group.columnWidths(), parts = [];",
                "        Object.keys(w).forEach(function (c) { parts.push(c + ' ' + w[c]); });",
                "        var cells = 0; Object.keys(relations).forEach(function (id) { cells += relations[id].cellCount(); });",
                "        var folded = group.members().filter(function (id) { return group.folded(id); });",
                "        status.textContent = 'ledger revision ' + store.revision() + '   |   header: ' + current.mode",
                "                           + '   |   tables ' + group.members().join(', ')",
                "                           + '   |   folded ' + (folded.length ? folded.join(', ') : 'none')",
                "                           + '   |   cells owned ' + cells + '   |   widths ' + parts.join(' \\u00b7 ');",
                "        if (modeBtn) modeBtn.textContent = 'header: ' + current.mode + ' \\u2192 ' + (current.mode === 'group' ? 'each' : 'group');",
                "    }",
                "    var unsub = store.subscribe(function () { report(); });",
                "",
                "    var btnSeq = 0;",
                "    function btn(text, fn) {",
                "        var x = branch.createElement('btn' + (++btnSeq), 'button');",
                "        css.addClass(x, wb_btn);",
                "        x.textContent = text;",
                "        x.addEventListener('click', function () { fn(); report(); });",
                "        bar.appendChild(x);",
                "        return x;",
                "    }",
                "    btn('a burst of trade (every outlet)', function () { store.trade(); });",
                "    store.outlets().forEach(function (o) { btn('trade at ' + o.name, function () { store.trade(o.id); }); });",
                "    btn('reset ledger', function () { store.reset(); });",
                "    // The host's own road to the same fold: the group's verbs, no fence involved.",
                "    btn('fold all', function () { if (current) current.group.foldAll(true); });",
                "    btn('unfold all', function () { if (current) current.group.foldAll(false); });",
                "    // The header mode is the group's for life: toggling is a new group, the old one's",
                "    // widths and folds carried over.",
                "    var modeBtn = btn('header: group \\u2192 each', function () {",
                "        var next = (current && current.mode === 'group') ? 'each' : 'group';",
                "        build(next, teardown());",
                "    });",
                "    report();",
                "",
                "    return { root: root, setActive: function (active) {} };"
        );
    }
}
