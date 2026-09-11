package hue.captains.singapura.js.homing.relgrid.workbench;

import hue.captains.singapura.js.homing.core.Importable;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.workspace.LifecycleHint;
import hue.captains.singapura.js.homing.workspace.WorkspaceWidget;

import java.util.List;

/**
 * The shop's editor: price is its column, and a day of trade is its button.
 * Sales are the one thing that moves popularity, and no editor can touch
 * either — sold and popularity are the store's to refuse.
 */
public final class DishManagerWidget extends WorkspaceWidget<WorkspaceWidget._None, DishManagerWidget> {

    public static final DishManagerWidget INSTANCE = new DishManagerWidget();

    private DishManagerWidget() {}

    private record construct() implements WorkspaceWidget._Construct<_None, DishManagerWidget> {}

    @Override protected _Construct<_None, DishManagerWidget> construct() { return new construct(); }
    @Override public Class<_None> paramsType() { return _None.class; }
    @Override public String title() { return "Shop manager"; }
    @Override public LifecycleHint lifecycleHint() { return LifecycleHint.SINGLETON; }

    @Override
    protected List<ModuleImports<? extends Importable>> bodyImports() { return ReplicaTable.imports(); }

    @Override
    protected List<String> constructBodyJs() { return ReplicaTable.bodyJs("manager"); }
}
