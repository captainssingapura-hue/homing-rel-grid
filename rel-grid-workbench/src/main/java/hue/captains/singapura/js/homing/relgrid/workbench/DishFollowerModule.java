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

/** The Replicating Tables bench's follower: {@code new DishFollower(container, params)} - a follower - a read-only replica of the bench's dishes; dock several. */
public record DishFollowerModule() implements DomModule<DishFollowerModule> {

    public record DishFollower() implements SelfContainedWidget<DishFollowerModule>, NeedKeyboard {
        @Override public String summary() { return "A follower - a read-only replica of the bench's dishes; dock several"; }
        @Override public List<KeyBinding> keys() { return List.of(KeyBinding.of(Key.ESCAPE, "the keys given back, when the grid did not take it")); }
    }

    public static final DishFollowerModule INSTANCE = new DishFollowerModule();

    @Override
    public ImportsFor<DishFollowerModule> imports() {
        return ImportsFor.<DishFollowerModule>builder()
                .add(new ModuleImports<>(List.of(new DishTableModule.DishTable()), DishTableModule.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<DishFollowerModule> exports() { return new ExportsOf<>(INSTANCE, List.of(new DishFollower())); }
}
