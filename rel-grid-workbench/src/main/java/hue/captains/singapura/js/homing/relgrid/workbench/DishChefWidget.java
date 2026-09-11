package hue.captains.singapura.js.homing.relgrid.workbench;

import hue.captains.singapura.js.homing.core.Importable;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.workspace.LifecycleHint;
import hue.captains.singapura.js.homing.workspace.WorkspaceWidget;

import java.util.List;

/**
 * The kitchen's editor: its cells for ingredient and style commit to the
 * shared store, and every other cell declines when the grid asks. The grid
 * is not involved in either.
 */
public final class DishChefWidget extends WorkspaceWidget<WorkspaceWidget._None, DishChefWidget> {

    public static final DishChefWidget INSTANCE = new DishChefWidget();

    private DishChefWidget() {}

    private record construct() implements WorkspaceWidget._Construct<_None, DishChefWidget> {}

    @Override protected _Construct<_None, DishChefWidget> construct() { return new construct(); }
    @Override public Class<_None> paramsType() { return _None.class; }
    @Override public String title() { return "Chef"; }
    @Override public LifecycleHint lifecycleHint() { return LifecycleHint.SINGLETON; }

    @Override
    protected List<ModuleImports<? extends Importable>> bodyImports() { return ReplicaTable.imports(); }

    @Override
    protected List<String> constructBodyJs() { return ReplicaTable.bodyJs("chef"); }
}
