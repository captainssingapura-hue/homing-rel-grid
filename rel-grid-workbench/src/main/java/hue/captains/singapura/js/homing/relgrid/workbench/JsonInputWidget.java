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
 *
 * <p>The body is one line: the pane is {@code mountJsonInput} in
 * {@link JsonBench}, JavaScript in a file.</p>
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
        return List.of(new ModuleImports<>(List.of(new JsonBench.mountJsonInput()), JsonBench.INSTANCE));
    }

    @Override
    protected List<String> constructBodyJs() {
        return List.of("    return mountJsonInput(branch);");
    }
}
