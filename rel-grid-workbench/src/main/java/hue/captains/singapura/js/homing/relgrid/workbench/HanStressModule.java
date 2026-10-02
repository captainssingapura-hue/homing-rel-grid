package hue.captains.singapura.js.homing.relgrid.workbench;

import hue.captains.singapura.js.homing.component.keyboard.Key;
import hue.captains.singapura.js.homing.component.keyboard.KeyBinding;
import hue.captains.singapura.js.homing.component.keyboard.NeedKeyboard;
import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.relgrid.RelGridModule;
import hue.captains.singapura.js.homing.workspace.widgets.SelfContainedWidget;

import java.util.List;

/**
 * The Han Article bench's stress: {@code new HanStress(container, params)} - hypothetical text
 * over the same engine, every column resizable, the merged cells and half-squares measured
 * against the slots beneath them after every change.
 */
public record HanStressModule() implements DomModule<HanStressModule> {

    public record HanStress() implements SelfContainedWidget<HanStressModule>, NeedKeyboard {
        @Override public String summary() { return "Hypothetical text over the Han engine, every column resizable; the merged cells measured against their slots."; }
        @Override public List<KeyBinding> keys() {
            return List.of(KeyBinding.of(Key.ESCAPE, "the keys given back, when the grid did not take it"));
        }
    }

    public static final HanStressModule INSTANCE = new HanStressModule();

    @Override
    public ImportsFor<HanStressModule> imports() {
        return ImportsFor.<HanStressModule>builder()
                .add(new ModuleImports<>(List.of(new BenchWidgetModule.BenchWidget()), BenchWidgetModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new RelGridModule.RelGrid()), RelGridModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new HanStoreModule.HanStore()), HanStoreModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new HanRelationModule.HanRelation()), HanRelationModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new WorkbenchStyles.wb_hint(), new WorkbenchStyles.wb_bar(),
                        new WorkbenchStyles.wb_han_host(), new WorkbenchStyles.wb_status()), WorkbenchStyles.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<HanStressModule> exports() { return new ExportsOf<>(INSTANCE, List.of(new HanStress())); }
}
