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
 * The Colour Extents bench's table: {@code new ExtentTable(container, params)} - six scaled words
 * at nine extents each, every cell an element wearing one pair at one number, nothing named a
 * colour.
 */
public record ExtentTableModule() implements DomModule<ExtentTableModule> {

    public record ExtentTable() implements SelfContainedWidget<ExtentTableModule>, NeedKeyboard {
        @Override public String summary() { return "Six scaled words at nine extents each: every cell wears one pair at one number, and nothing names a colour."; }
        @Override public List<KeyBinding> keys() { return List.of(KeyBinding.of(Key.ESCAPE, "the keys given back")); }
    }

    public static final ExtentTableModule INSTANCE = new ExtentTableModule();

    @Override
    public ImportsFor<ExtentTableModule> imports() {
        return ImportsFor.<ExtentTableModule>builder()
                .add(new ModuleImports<>(List.of(new BenchWidgetModule.BenchWidget()), BenchWidgetModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new WorkbenchStyles.wb_hint()), WorkbenchStyles.INSTANCE))
                .add(new ModuleImports<>(
                        List.of(new ExtentStyles.wb_extent_table(), new ExtentStyles.wb_extent_head(), new ExtentStyles.wb_extent_label(), new ExtentStyles.wb_extent_cell(),
                                new ExtentStyles.wb_x_success_ink(), new ExtentStyles.wb_x_danger_surface(), new ExtentStyles.wb_x_primary_surface(),
                                new ExtentStyles.wb_x_primary_ink(), new ExtentStyles.wb_x_raised_surface(), new ExtentStyles.wb_x_warning_edge()),
                        ExtentStyles.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<ExtentTableModule> exports() { return new ExportsOf<>(INSTANCE, List.of(new ExtentTable())); }
}
