package hue.captains.singapura.js.homing.relgrid.workbench;

import hue.captains.singapura.js.homing.component.keyboard.Key;
import hue.captains.singapura.js.homing.component.keyboard.KeyBinding;
import hue.captains.singapura.js.homing.component.keyboard.NeedKeyboard;
import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.workspace.widgets.SelfContainedWidget;

import java.util.List;

/** The Replicating Tables bench's nutritionist: {@code new DishNutritionist(container, params)} - the nutritionist's table - edits calories and the rating. */
public record DishNutritionistModule() implements DomModule<DishNutritionistModule> {

    public record DishNutritionist() implements SelfContainedWidget<DishNutritionistModule>, NeedKeyboard {
        @Override public String summary() { return "The nutritionist's table - edits calories and the rating"; }
        @Override public List<KeyBinding> keys() { return List.of(KeyBinding.of(Key.ESCAPE, "the keys given back, when the grid did not take it")); }
    }

    public static final DishNutritionistModule INSTANCE = new DishNutritionistModule();

    @Override
    public ImportsFor<DishNutritionistModule> imports() {
        return ImportsFor.<DishNutritionistModule>builder()
                .add(new ModuleImports<>(List.of(new DishTableModule.DishTable()), DishTableModule.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<DishNutritionistModule> exports() { return new ExportsOf<>(INSTANCE, List.of(new DishNutritionist())); }
}
