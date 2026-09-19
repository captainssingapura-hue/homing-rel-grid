package hue.captains.singapura.js.homing.relgrid.workbench;

import hue.captains.singapura.js.homing.core.Importable;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.workspace.LifecycleHint;
import hue.captains.singapura.js.homing.workspace.WorkspaceWidget;

import java.util.List;

/**
 * The table of the Colour Extents bench. The body is one line: the pane is
 * {@code mountExtentTable} in {@link ExtentBench}, JavaScript in a file.
 */
public final class ExtentTableWidget extends WorkspaceWidget<WorkspaceWidget._None, ExtentTableWidget> {

    public static final ExtentTableWidget INSTANCE = new ExtentTableWidget();

    private ExtentTableWidget() {}

    private record construct() implements WorkspaceWidget._Construct<_None, ExtentTableWidget> {}

    @Override protected _Construct<_None, ExtentTableWidget> construct() { return new construct(); }
    @Override public Class<_None> paramsType() { return _None.class; }
    @Override public String title() { return "Extent table"; }
    @Override public LifecycleHint lifecycleHint() { return LifecycleHint.MULTI; }

    @Override
    protected List<ModuleImports<? extends Importable>> bodyImports() {
        return List.of(new ModuleImports<>(List.of(new ExtentBench.mountExtentTable()), ExtentBench.INSTANCE));
    }

    @Override
    protected List<String> constructBodyJs() {
        return List.of("    return mountExtentTable(branch);");
    }
}
