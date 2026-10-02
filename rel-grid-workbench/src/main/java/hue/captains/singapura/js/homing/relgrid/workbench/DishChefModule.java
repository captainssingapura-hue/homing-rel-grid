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

/** The Replicating Tables bench's chef: {@code new DishChef(container, params)} - the chef's table - edits ingredient and style. */
public record DishChefModule() implements DomModule<DishChefModule> {

    public record DishChef() implements SelfContainedWidget<DishChefModule>, NeedKeyboard {
        @Override public String summary() { return "The chef's table - edits ingredient and style"; }
        @Override public List<KeyBinding> keys() { return List.of(KeyBinding.of(Key.ESCAPE, "the keys given back, when the grid did not take it")); }
    }

    public static final DishChefModule INSTANCE = new DishChefModule();

    @Override
    public ImportsFor<DishChefModule> imports() {
        return ImportsFor.<DishChefModule>builder()
                .add(new ModuleImports<>(List.of(new DishTableModule.DishTable()), DishTableModule.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<DishChefModule> exports() { return new ExportsOf<>(INSTANCE, List.of(new DishChef())); }
}
