package hue.captains.singapura.js.homing.relgrid.workbench;

import hue.captains.singapura.js.homing.core.Importable;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.workspace.LifecycleHint;
import hue.captains.singapura.js.homing.workspace.WorkspaceWidget;

import java.util.List;

/**
 * The card of the Colour Extents bench. The body is one line: the pane is
 * {@code mountExtentCard} in {@link ExtentBench}, JavaScript in a file.
 */
public final class ExtentCardWidget extends WorkspaceWidget<WorkspaceWidget._None, ExtentCardWidget> {

    public static final ExtentCardWidget INSTANCE = new ExtentCardWidget();

    private ExtentCardWidget() {}

    private record construct() implements WorkspaceWidget._Construct<_None, ExtentCardWidget> {}

    @Override protected _Construct<_None, ExtentCardWidget> construct() { return new construct(); }
    @Override public Class<_None> paramsType() { return _None.class; }
    @Override public String title() { return "Extent card"; }
    @Override public LifecycleHint lifecycleHint() { return LifecycleHint.MULTI; }

    @Override
    protected List<ModuleImports<? extends Importable>> bodyImports() {
        return List.of(new ModuleImports<>(List.of(new ExtentBench.mountExtentCard()), ExtentBench.INSTANCE));
    }

    @Override
    protected List<String> constructBodyJs() {
        return List.of("    return mountExtentCard(branch);");
    }
}
