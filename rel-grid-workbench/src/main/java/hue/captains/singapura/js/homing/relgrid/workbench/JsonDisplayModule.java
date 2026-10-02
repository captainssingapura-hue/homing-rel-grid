package hue.captains.singapura.js.homing.relgrid.workbench;

import hue.captains.singapura.js.homing.component.keyboard.Key;
import hue.captains.singapura.js.homing.component.keyboard.KeyBinding;
import hue.captains.singapura.js.homing.component.keyboard.NeedKeyboard;
import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.jsonkit.JsonTreeViewModule;
import hue.captains.singapura.js.homing.relgrid.RelGridStyles;
import hue.captains.singapura.js.homing.workspace.widgets.SelfContainedWidget;

import java.util.List;

/**
 * The JSON Tree bench's display: {@code new JsonDisplay(container, params)} - the kit's tree view
 * over the bench's one document, fed the last value that parsed of what the {@code json-text}
 * party says.
 */
public record JsonDisplayModule() implements DomModule<JsonDisplayModule> {

    public record JsonDisplay() implements SelfContainedWidget<JsonDisplayModule>, NeedKeyboard {
        @Override public String summary() { return "The JSON kit's tree view over the bench's one document, live: the last value that parsed."; }
        @Override public List<KeyBinding> keys() {
            return List.of(KeyBinding.of(Key.ESCAPE, "the keys given back, when the tree did not take it"));
        }
    }

    public static final JsonDisplayModule INSTANCE = new JsonDisplayModule();

    @Override
    public ImportsFor<JsonDisplayModule> imports() {
        return ImportsFor.<JsonDisplayModule>builder()
                .add(new ModuleImports<>(List.of(new BenchWidgetModule.BenchWidget()), BenchWidgetModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new JsonTreeViewModule.JsonTreeView()), JsonTreeViewModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new JsonDocStoreModule.JsonDocStore()), JsonDocStoreModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new JsonTextModule.JSON_TEXT()), JsonTextModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new RelGridStyles.hrg_frame(), new RelGridStyles.hrg_lit()), RelGridStyles.INSTANCE))
                .add(new ModuleImports<>(List.of(new WorkbenchStyles.wb_hint(), new WorkbenchStyles.wb_bar(), new WorkbenchStyles.wb_input(),
                        new WorkbenchStyles.wb_frame(), new WorkbenchStyles.wb_port(), new WorkbenchStyles.wb_status()), WorkbenchStyles.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<JsonDisplayModule> exports() { return new ExportsOf<>(INSTANCE, List.of(new JsonDisplay())); }
}
