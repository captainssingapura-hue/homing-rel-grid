package hue.captains.singapura.js.homing.relgrid.workbench;

import hue.captains.singapura.js.homing.core.Importable;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.relgrid.RelGridModule;
import hue.captains.singapura.js.homing.workspace.LifecycleHint;
import hue.captains.singapura.js.homing.workspace.WorkspaceWidget;

import java.util.List;

/**
 * The endless table: a grid over an {@link EndlessRelation} of a million
 * rows, showing a window of twenty, and never told there is anything beyond
 * them. The wheel, an arrow at the top or bottom, PageUp and PageDown ask the
 * relation for the View a few rows on — {@code view({ by })} — and the grid
 * arranges what comes back on the same twenty rows of slots. Nothing scrolls
 * natively; the table is exactly the window.
 *
 * <p>It measures itself, through the party rather than the DOM. After every
 * step: the grid's branch holds a <b>constant</b> number of elements and its
 * slots branch is the one it started with — nothing minted, nothing released
 * however far the window travels; the domain's branch holds at most 2W rows;
 * the grid's registry is exactly W × columns; every slot holds exactly one
 * cell; and the step's cost in milliseconds. A burst runs a few hundred steps
 * and reports the average. That is the whole claim of the two branches, read
 * off two numbers.</p>
 */
public final class EndlessWidget extends WorkspaceWidget<WorkspaceWidget._None, EndlessWidget> {

    public static final EndlessWidget INSTANCE = new EndlessWidget();

    private EndlessWidget() {}

    private record construct() implements WorkspaceWidget._Construct<_None, EndlessWidget> {}

    @Override protected _Construct<_None, EndlessWidget> construct() { return new construct(); }
    @Override public Class<_None> paramsType() { return _None.class; }
    @Override public String title() { return "Endless"; }
    @Override public LifecycleHint lifecycleHint() { return LifecycleHint.MULTI; }

    @Override
    protected List<ModuleImports<? extends Importable>> bodyImports() {
        return List.of(
                new ModuleImports<>(List.of(new RelGridModule.RelGrid()), RelGridModule.INSTANCE),
                new ModuleImports<>(List.of(new EndlessRelation.createEndlessRelation()), EndlessRelation.INSTANCE),
                new ModuleImports<>(
                        List.of(new WorkbenchStyles.wb_root(), new WorkbenchStyles.wb_window_host(),
                                new WorkbenchStyles.wb_hint(), new WorkbenchStyles.wb_bar(),
                                new WorkbenchStyles.wb_btn(), new WorkbenchStyles.wb_status()),
                        WorkbenchStyles.INSTANCE));
    }

    @Override
    protected List<String> constructBodyJs() {
        return List.of(
                "    var N = 1000000, W = 20;",
                "    var owner = Object.freeze({ toString: function () { return 'endless'; } });",
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
                "    css.addClass(host, wb_window_host);",
                "    root.appendChild(host);",
                "    var status = branch.createElement('status', 'div');",
                "    css.addClass(status, wb_status);",
                "    root.appendChild(status);",
                "    var check = branch.createElement('check', 'div');",
                "    css.addClass(check, wb_status);",
                "    root.appendChild(check);",
                "",
                "    // EXACTLY TWO BRANCHES under this widget's: 'grid', handed whole to the grid, which",
                "    // activates it and mints everything it makes on it; 'domain', this widget's to divide",
                "    // — here one part, 'cells', the relation's own. Dissolving both is this widget's.",
                "    var gridB = branch.createBranch('grid');",
                "    var domainB = branch.createBranch('domain');",
                "    domainB.activate(owner);",
                "    var cellsB = domainB.createBranch('cells');",
                "",
                "    hint.textContent = 'ENDLESS \\u2014 a million rows, twenty shown, and the grid never told there are more. The wheel over the table, \\u2191/\\u2193 at the top or bottom row, PageUp/PageDown, and the buttons ask the relation for the View a few rows on; the grid arranges what comes back on the same twenty rows of slots. Every column but id edits (Enter), and an edit survives its row being scrolled away and back: the value is the relation\\u2019s. The readouts below are the party\\u2019s numbers, not the DOM\\u2019s: the grid\\u2019s branch must hold a constant count on the slots it started with, the domain\\u2019s at most two windows of rows, and the registry exactly the window.';",
                "",
                "    var relation = createEndlessRelation({ branch: cellsB, rows: N, window: W });",
                "    var cols = relation.columns().length;",
                "    var edges = 0, steps = 0, lastMs = 0, burst = null, grid = null;",
                "    var now = (typeof performance !== 'undefined' && performance.now) ? function () { return performance.now(); } : function () { return Date.now(); };",
                "",
                "    grid = new RelGrid({",
                "        container: host,",
                "        branch: gridB,",
                "        relation: relation,",
                "        header: { show: true, sticky: true },",
                "        label: 'Endless',",
                "        onArranged: function () { afterChange(); },",
                "        onCursorMoved: function () { report(); },",
                "        onEdge: function () { edges++; report(); }",
                "    });",
                "    grid.setColumnWidths({ id: 70, name: 140, qty: 64, price: 70, note: 130 });",
                "    var slots0 = gridB.getBranch('slots'), gridCount0 = countTree(gridB);",
                "",
                "    // The party's own count of a subtree: elements on the branch, and on every branch under it.",
                "    function countTree(b) {",
                "        var n = b.elementCount;",
                "        b.listBranches().forEach(function (name) { n += countTree(b.getBranch(name)); });",
                "        return n;",
                "    }",
                "",
                "    // THE SELF-CHECK, on the party's numbers. The grid's side is a constant; the",
                "    // domain's is bounded; the registry is the window; every slot holds one cell.",
                "    function measure() {",
                "        var gridCount = countTree(gridB), domainCount = countTree(domainB);",
                "        var held = relation.held(), registry = grid.cells().size();",
                "        var slots = gridB.getBranch('slots'), kept = slots === slots0, one = true;",
                "        for (var i = 0; i < W && one; i++) for (var j = 0; j < cols; j++) {",
                "            var td = slots.getElement('td-' + i + '-' + j);",
                "            if (!td || td.children.length !== 1) { one = false; break; }",
                "        }",
                "        var bad = [];",
                "        if (gridCount !== gridCount0) bad.push('grid elements ' + gridCount + ', were ' + gridCount0);",
                "        if (!kept) bad.push('slots re-minted');",
                "        if (held > 2 * W) bad.push('rows held ' + held + ' > ' + (2 * W));",
                "        if (registry !== W * cols) bad.push('registry ' + registry + ' != ' + (W * cols));",
                "        if (!one) bad.push('a slot without exactly one cell');",
                "        check.textContent = 'grid elements ' + gridCount + ' (constant)   |   slots ' + (kept ? 'kept' : 'RE-MINTED')",
                "                          + '   |   domain elements ' + domainCount + '   |   rows held ' + held + ' (bound ' + (2 * W) + ')'",
                "                          + '   |   registry ' + registry + ' (= ' + (W * cols) + ')'",
                "                          + '   |   ' + (bad.length ? 'OFF: ' + bad.join(', ') : 'every invariant holds')",
                "                          + '   |   last step ' + lastMs.toFixed(2) + 'ms' + (burst ? '   |   ' + burst : '');",
                "    }",
                "    function report() {",
                "        status.textContent = 'window ' + W + ' of ' + N + ' at row ' + relation.at()",
                "                           + '   |   steps ' + steps + '   |   edges ' + edges",
                "                           + '   |   cells minted ' + relation.mints() + ', rows freed ' + relation.frees()",
                "                           + '   |   cursor ' + (grid.cursor() ? grid.cursor().pk + '/' + grid.cursor().column : '\\u2014');",
                "    }",
                "    function afterChange() {",
                "        if (!grid) return;                       // the first arrangement runs inside the constructor",
                "        steps++; report(); measure();",
                "    }",
                "    report(); measure();",
                "",
                "    // Every step through the grid's own twin of the wheel, timed.",
                "    function step(by) {",
                "        var t0 = now(); var moved = grid.scrollRows(by); lastMs = now() - t0;",
                "        if (!moved) { report(); measure(); }     // an end: nothing arranged, still measured",
                "        return moved;",
                "    }",
                "    function run(label, times, byAt) {",
                "        var t0 = now(), moved = 0;",
                "        for (var k = 0; k < times; k++) if (grid.scrollRows(byAt(k))) moved++;",
                "        var ms = now() - t0;",
                "        burst = label + ': ' + moved + ' of ' + times + ' steps in ' + ms.toFixed(0) + 'ms, ' + (ms / times).toFixed(2) + 'ms each';",
                "        report(); measure();",
                "    }",
                "    var btnSeq = 0;",
                "    function btn(label, fn) {",
                "        var x = branch.createElement('btn' + (++btnSeq), 'button');",
                "        css.addClass(x, wb_btn);",
                "        x.textContent = label;",
                "        x.addEventListener('click', function () { fn(); grid.focus(); });",
                "        bar.appendChild(x);",
                "    }",
                "    btn('\\u2193 1 row', function () { step(1); });",
                "    btn('\\u2191 1 row', function () { step(-1); });",
                "    btn('\\u2193 page', function () { step(W); });",
                "    btn('\\u2191 page', function () { step(-W); });",
                "    btn('to start', function () { step(-N); });",
                "    btn('to end', function () { step(N); });",
                "    btn('burst: 300 rows down', function () { run('300 x 1 row', 300, function () { return 1; }); });",
                "    btn('burst: 100 pages', function () { run('100 x page', 100, function (k) { return (k % 2) ? -W : W; }); });",
                "    btn('burst: 100 random jumps', function () { run('100 random jumps', 100, function () { return Math.floor((Math.random() - 0.5) * 2 * N); }); });",
                "",
                "    return { root: root, setActive: function (active) {} };"
        );
    }
}
