package hue.captains.singapura.js.homing.relgrid.workbench;

import hue.captains.singapura.js.homing.core.Importable;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.workspace.LifecycleHint;
import hue.captains.singapura.js.homing.workspace.WorkspaceWidget;

import java.util.List;

/** A read-only replica. Dock as many as you like; each moves on every commit and every sale, and none of their grids is told. */
public final class DishFollowerWidget extends WorkspaceWidget<WorkspaceWidget._None, DishFollowerWidget> {

    public static final DishFollowerWidget INSTANCE = new DishFollowerWidget();

    private DishFollowerWidget() {}

    private record construct() implements WorkspaceWidget._Construct<_None, DishFollowerWidget> {}

    @Override protected _Construct<_None, DishFollowerWidget> construct() { return new construct(); }
    @Override public Class<_None> paramsType() { return _None.class; }
    @Override public String title() { return "Follower"; }
    @Override public LifecycleHint lifecycleHint() { return LifecycleHint.MULTI; }

    @Override
    protected List<ModuleImports<? extends Importable>> bodyImports() { return ReplicaTable.imports(); }

    @Override
    protected List<String> constructBodyJs() { return ReplicaTable.bodyJs("follower"); }
}
