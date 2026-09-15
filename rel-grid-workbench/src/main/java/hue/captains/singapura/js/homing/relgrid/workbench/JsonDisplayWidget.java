package hue.captains.singapura.js.homing.relgrid.workbench;

import hue.captains.singapura.js.homing.core.Importable;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.jsonkit.JsonTreeViewModule;
import hue.captains.singapura.js.homing.relgrid.RelGridStyles;
import hue.captains.singapura.js.homing.workspace.LifecycleHint;
import hue.captains.singapura.js.homing.workspace.WorkspaceWidget;

import java.util.List;

/**
 * The JSON Tree bench's DISPLAY: the kit's {@code JsonTreeView} over the shared
 * document, live. One line builds it; one line per change feeds it. What the
 * widget adds is the frame round the scrollport, a bar of the view's verbs,
 * and a readout of the cursor's pointer and the value under it — a host that
 * wants a JSON value on screen needs none of that.
 */
public final class JsonDisplayWidget extends WorkspaceWidget<WorkspaceWidget._None, JsonDisplayWidget> {

    public static final JsonDisplayWidget INSTANCE = new JsonDisplayWidget();

    private JsonDisplayWidget() {}

    private record construct() implements WorkspaceWidget._Construct<_None, JsonDisplayWidget> {}

    @Override protected _Construct<_None, JsonDisplayWidget> construct() { return new construct(); }
    @Override public Class<_None> paramsType() { return _None.class; }
    @Override public String title() { return "JSON tree"; }
    @Override public LifecycleHint lifecycleHint() { return LifecycleHint.MULTI; }

    @Override
    protected List<ModuleImports<? extends Importable>> bodyImports() {
        return List.of(
                new ModuleImports<>(List.of(new JsonTreeViewModule.JsonTreeView()), JsonTreeViewModule.INSTANCE),
                new ModuleImports<>(List.of(new JsonDocStore.jsonDocStoreShared()), JsonDocStore.INSTANCE),
                new ModuleImports<>(List.of(new RelGridStyles.hrg_frame(), new RelGridStyles.hrg_lit()), RelGridStyles.INSTANCE),
                new ModuleImports<>(
                        List.of(new WorkbenchStyles.wb_root(), new WorkbenchStyles.wb_hint(), new WorkbenchStyles.wb_bar(),
                                new WorkbenchStyles.wb_btn(), new WorkbenchStyles.wb_input(), new WorkbenchStyles.wb_frame(),
                                new WorkbenchStyles.wb_port(), new WorkbenchStyles.wb_status()),
                        WorkbenchStyles.INSTANCE));
    }

    @Override
    protected List<String> constructBodyJs() {
        return List.of(
                "    var root = branch.createElement('root', 'div');",
                "    css.addClass(root, wb_root);",
                "    var hint = branch.createElement('hint', 'div');",
                "    css.addClass(hint, wb_hint);",
                "    root.appendChild(hint);",
                "    var bar = branch.createElement('bar', 'div');",
                "    css.addClass(bar, wb_bar);",
                "    root.appendChild(bar);",
                "    // The frame goes round the SCROLLPORT: the tree draws none of its own.",
                "    var frame = branch.createElement('frame', 'div');",
                "    css.addClass(frame, wb_frame, hrg_frame, hrg_lit);",
                "    root.appendChild(frame);",
                "    var port = branch.createElement('port', 'div');",
                "    css.addClass(port, wb_port);",
                "    frame.appendChild(port);",
                "    var status = branch.createElement('status', 'div');",
                "    css.addClass(status, wb_status);",
                "    root.appendChild(status);",
                "",
                "    hint.textContent = 'JSON TREE \\u2014 the kit\\u2019s viewer over the bench\\u2019s one document, live: a JSON value is already an outline, so the document answers the tree\\u2019s places by JSON pointer and a cell per node, and the tree does the tree. Every container is closed at first under an open root; \\u2192, Space and the caret unfold. Edit in the Input pane and watch: a member typed appears, a member deleted goes, the folds and the cursor stay by pointer.';",
                "",
                "    var store = jsonDocStoreShared();",
                "    // ONE LINE: the view is the tree\\u2019s host, and makes its two branches under the one it is handed.",
                "    var view = new JsonTreeView({",
                "        container: port,",
                "        branch: branch.createBranch('json'),",
                "        value: store.value(),",
                "        label: 'JSON tree',",
                "        onCursorMoved: function () { report(); },",
                "        onArranged:    function () { report(); },",
                "        onActivated:   function (pointer) { activated = pointer; report(); }",
                "    });",
                "    var activated = null, note = '';",
                "    // ONE LINE per change: the last value that parsed, whenever the text parsed.",
                "    var unsub = store.subscribe(function () { if (store.error() === null) view.set(store.value()); report(); });",
                "",
                "    function report() {",
                "        var p = view.cursor(), v = (p === null) ? undefined : view.valueAt(p);",
                "        var rows = view.rows();",
                "        status.textContent = rows + ' row' + (rows === 1 ? '' : 's')",
                "                           + '   |   cursor ' + (p === null ? '\\u2014' : (p === '' ? '(root)' : p))",
                "                           + (p === null ? '' : '   |   value ' + (typeof v === 'string' ? JSON.stringify(v) : (v !== null && typeof v === 'object' ? (Array.isArray(v) ? 'array[' + v.length + ']' : 'object{' + Object.keys(v).length + '}') : String(v))))",
                "                           + (activated === null ? '' : '   |   activated ' + activated)",
                "                           + (store.error() === null ? '' : '   |   showing the last value that parsed')",
                "                           + (note ? '   |   ' + note : '');",
                "    }",
                "",
                "    var btnSeq = 0;",
                "    function btn(label, fn) {",
                "        var x = branch.createElement('btn' + (++btnSeq), 'button');",
                "        css.addClass(x, wb_btn);",
                "        x.textContent = label;",
                "        x.addEventListener('click', function () { fn(); report(); view.focus(); });",
                "        bar.appendChild(x);",
                "    }",
                "    btn('open all', function () { view.openAll(); });",
                "    btn('open two deep', function () { view.openAll(1); });",
                "    btn('close all', function () { view.closeAll(); });",
                "    // A navigator's move: a pointer typed, the path opened by the document, the cursor put on it.",
                "    var pointer = branch.createElement('pointer', 'input');",
                "    css.addClass(pointer, wb_input);",
                "    pointer.setAttribute('placeholder', '/releases/0/notes/2');",
                "    pointer.setAttribute('aria-label', 'a JSON pointer to reveal');",
                "    pointer.value = '/releases/0/notes/2';",
                "    bar.appendChild(pointer);",
                "    btn('reveal', function () { note = view.selectPointer(pointer.value) ? '' : 'no such node: ' + pointer.value; });",
                "    report();",
                "",
                "    return { root: root, setActive: function (active) {}, partyDeregister: function () { unsub(); view.destroy(); branch.dissolveBranch('json'); } };"
        );
    }
}
