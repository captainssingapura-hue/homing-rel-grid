package hue.captains.singapura.js.homing.relgrid.workbench;

import hue.captains.singapura.js.homing.core.Importable;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.workspace.LifecycleHint;
import hue.captains.singapura.js.homing.workspace.WorkspaceWidget;

import java.util.List;

/**
 * Only a nutritionist may say what a dish weighs in: calories is this
 * table's one editable column. Price is not theirs, and popularity is
 * nobody's — Enter on either opens nothing, because the cell declined.
 */
public final class DishNutritionistWidget extends WorkspaceWidget<WorkspaceWidget._None, DishNutritionistWidget> {

    public static final DishNutritionistWidget INSTANCE = new DishNutritionistWidget();

    private DishNutritionistWidget() {}

    private record construct() implements WorkspaceWidget._Construct<_None, DishNutritionistWidget> {}

    @Override protected _Construct<_None, DishNutritionistWidget> construct() { return new construct(); }
    @Override public Class<_None> paramsType() { return _None.class; }
    @Override public String title() { return "Nutritionist"; }
    @Override public LifecycleHint lifecycleHint() { return LifecycleHint.SINGLETON; }

    @Override
    protected List<ModuleImports<? extends Importable>> bodyImports() { return ReplicaTable.imports(); }

    @Override
    protected List<String> constructBodyJs() { return ReplicaTable.bodyJs("nutritionist"); }
}
