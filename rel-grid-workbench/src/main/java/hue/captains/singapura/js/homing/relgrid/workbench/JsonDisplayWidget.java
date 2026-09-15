package hue.captains.singapura.js.homing.relgrid.workbench;

import hue.captains.singapura.js.homing.core.Importable;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.workspace.LifecycleHint;
import hue.captains.singapura.js.homing.workspace.WorkspaceWidget;

import java.util.List;

/**
 * The JSON Tree bench's DISPLAY: the kit's {@code JsonTreeView} over the shared
 * document, live. One line builds it; one line per change feeds it. What the
 * pane adds is the frame round the scrollport, a bar of the view's verbs,
 * and a readout of the cursor's pointer and the value under it — a host that
 * wants a JSON value on screen needs none of that.
 *
 * <p>The body is one line: the pane is {@code mountJsonDisplay} in
 * {@link JsonBench}, JavaScript in a file.</p>
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
        return List.of(new ModuleImports<>(List.of(new JsonBench.mountJsonDisplay()), JsonBench.INSTANCE));
    }

    @Override
    protected List<String> constructBodyJs() {
        return List.of("    return mountJsonDisplay(branch);");
    }
}
