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

/** The Replicating Tables bench's manager: {@code new DishManager(container, params)} - the shop manager's table - edits price, and runs a day of trade. */
public record DishManagerModule() implements DomModule<DishManagerModule> {

    public record DishManager() implements SelfContainedWidget<DishManagerModule>, NeedKeyboard {
        @Override public String summary() { return "The shop manager's table - edits price, and runs a day of trade"; }
        @Override public List<KeyBinding> keys() { return List.of(KeyBinding.of(Key.ESCAPE, "the keys given back, when the grid did not take it")); }
    }

    public static final DishManagerModule INSTANCE = new DishManagerModule();

    @Override
    public ImportsFor<DishManagerModule> imports() {
        return ImportsFor.<DishManagerModule>builder()
                .add(new ModuleImports<>(List.of(new DishTableModule.DishTable()), DishTableModule.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<DishManagerModule> exports() { return new ExportsOf<>(INSTANCE, List.of(new DishManager())); }
}
