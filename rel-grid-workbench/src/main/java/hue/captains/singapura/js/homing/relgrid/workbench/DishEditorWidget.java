package hue.captains.singapura.js.homing.relgrid.workbench;

import hue.captains.singapura.js.homing.core.Importable;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.workspace.LifecycleHint;
import hue.captains.singapura.js.homing.workspace.WorkspaceWidget;

import java.util.List;

/** The one table that edits. Its cells commit to the shared store; the grid is not involved. */
public final class DishEditorWidget extends WorkspaceWidget<WorkspaceWidget._None, DishEditorWidget> {

    public static final DishEditorWidget INSTANCE = new DishEditorWidget();

    private DishEditorWidget() {}

    private record construct() implements WorkspaceWidget._Construct<_None, DishEditorWidget> {}

    @Override protected _Construct<_None, DishEditorWidget> construct() { return new construct(); }
    @Override public Class<_None> paramsType() { return _None.class; }
    @Override public String title() { return "Dish editor"; }
    @Override public LifecycleHint lifecycleHint() { return LifecycleHint.SINGLETON; }

    @Override
    protected List<ModuleImports<? extends Importable>> bodyImports() { return ReplicaTable.imports(); }

    @Override
    protected List<String> constructBodyJs() { return ReplicaTable.bodyJs("editor", true); }
}
