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

/**
 * The Colour Extents bench's card: {@code new ExtentCard(container, params)} - one card wearing
 * its surface, its edge and its ink at an extent a slider sets; the colours tween because the
 * extent is a registered number.
 */
public record ExtentCardModule() implements DomModule<ExtentCardModule> {

    public record ExtentCard() implements SelfContainedWidget<ExtentCardModule>, NeedKeyboard {
        @Override public String summary() { return "One card wearing three scaled words at the extent a slider sets; the colours tween."; }
        @Override public List<KeyBinding> keys() { return List.of(KeyBinding.of(Key.ESCAPE, "the keys given back, when the slider did not take it")); }
    }

    public static final ExtentCardModule INSTANCE = new ExtentCardModule();

    @Override
    public ImportsFor<ExtentCardModule> imports() {
        return ImportsFor.<ExtentCardModule>builder()
                .add(new ModuleImports<>(List.of(new BenchWidgetModule.BenchWidget()), BenchWidgetModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new WorkbenchStyles.wb_hint(), new WorkbenchStyles.wb_bar()), WorkbenchStyles.INSTANCE))
                .add(new ModuleImports<>(
                        List.of(new ExtentStyles.wb_extent_card(), new ExtentStyles.wb_extent_card_title(), new ExtentStyles.wb_extent_slider(), new ExtentStyles.wb_extent_range()),
                        ExtentStyles.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<ExtentCardModule> exports() { return new ExportsOf<>(INSTANCE, List.of(new ExtentCard())); }
}
