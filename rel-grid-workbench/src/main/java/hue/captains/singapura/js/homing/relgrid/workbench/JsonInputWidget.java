package hue.captains.singapura.js.homing.relgrid.workbench;

import hue.captains.singapura.js.homing.core.Importable;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.workspace.LifecycleHint;
import hue.captains.singapura.js.homing.workspace.WorkspaceWidget;

import java.util.List;

/**
 * The JSON Tree bench's INPUT: a plain textarea over the shared document.
 * Every keystroke writes the store, which parses; the status says whether
 * the text parsed and, if not, what the parser said. Nothing here knows a
 * tree exists — the Display widget shows whatever value last parsed.
 */
public final class JsonInputWidget extends WorkspaceWidget<WorkspaceWidget._None, JsonInputWidget> {

    public static final JsonInputWidget INSTANCE = new JsonInputWidget();

    private JsonInputWidget() {}

    private record construct() implements WorkspaceWidget._Construct<_None, JsonInputWidget> {}

    @Override protected _Construct<_None, JsonInputWidget> construct() { return new construct(); }
    @Override public Class<_None> paramsType() { return _None.class; }
    @Override public String title() { return "JSON input"; }
    @Override public LifecycleHint lifecycleHint() { return LifecycleHint.SINGLETON; }

    @Override
    protected List<ModuleImports<? extends Importable>> bodyImports() {
        return List.of(
                new ModuleImports<>(List.of(new JsonDocStore.jsonDocStoreShared()), JsonDocStore.INSTANCE),
                new ModuleImports<>(
                        List.of(new WorkbenchStyles.wb_root(), new WorkbenchStyles.wb_hint(), new WorkbenchStyles.wb_bar(),
                                new WorkbenchStyles.wb_btn(), new WorkbenchStyles.wb_json_text(), new WorkbenchStyles.wb_status()),
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
                "    var text = branch.createElement('text', 'textarea');",
                "    css.addClass(text, wb_json_text);",
                "    text.setAttribute('spellcheck', 'false');",
                "    text.setAttribute('aria-label', 'JSON text');",
                "    root.appendChild(text);",
                "    var status = branch.createElement('status', 'div');",
                "    css.addClass(status, wb_status);",
                "    root.appendChild(status);",
                "",
                "    hint.textContent = 'JSON INPUT \\u2014 a plain textarea over the bench\\u2019s one document. Every keystroke writes the store, which parses; the Display pane shows the last value that parsed and keeps it while the text is broken. Type inside a string, add a member, delete a whole array: the tree keeps its folds and its cursor by pointer wherever the node still stands.';",
                "",
                "    var store = jsonDocStoreShared();",
                "    text.value = store.text();",
                "",
                "    function report() {",
                "        var err = store.error();",
                "        status.textContent = (err ? '\\u2717 ' + err : '\\u2713 parsed') + '   |   revision ' + store.revision() + '   |   ' + store.text().length + ' characters';",
                "    }",
                "    // THE SEAM: the textarea writes the store, live; the store parses and tells its subscribers.",
                "    text.addEventListener('input', function () { store.set(text.value); });",
                "    var unsub = store.subscribe(function () { if (text.value !== store.text()) text.value = store.text(); report(); });",
                "",
                "    var btnSeq = 0;",
                "    function btn(label, fn) {",
                "        var x = branch.createElement('btn' + (++btnSeq), 'button');",
                "        css.addClass(x, wb_btn);",
                "        x.textContent = label;",
                "        x.addEventListener('click', function () { fn(); });",
                "        bar.appendChild(x);",
                "    }",
                "    btn('the sample', function () { store.set(store.sample()); });",
                "    btn('pretty', function () { if (store.error() === null) store.set(JSON.stringify(store.value(), null, 2)); });",
                "    btn('minify', function () { if (store.error() === null) store.set(JSON.stringify(store.value())); });",
                "    btn('a big array (10,000)', function () { var a = []; for (var i = 0; i < 10000; i++) a.push({ i: i, sq: i * i, even: i % 2 === 0 }); store.set(JSON.stringify({ rows: a, count: a.length })); });",
                "    btn('clear', function () { store.set(''); });",
                "    report();",
                "",
                "    return { root: root, setActive: function (active) {}, partyDeregister: function () { unsub(); } };"
        );
    }
}
