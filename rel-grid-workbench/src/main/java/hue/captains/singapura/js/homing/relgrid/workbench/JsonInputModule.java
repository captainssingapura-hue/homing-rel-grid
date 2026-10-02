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
 * The JSON Tree bench's input: {@code new JsonInput(container, params)} - a plain textarea over
 * the bench's one document, every keystroke told to the {@code json-text} party.
 */
public record JsonInputModule() implements DomModule<JsonInputModule> {

    public record JsonInput() implements SelfContainedWidget<JsonInputModule>, NeedKeyboard {
        @Override public String summary() { return "A plain textarea over the bench's one JSON document; every keystroke told to the bench."; }
        @Override public List<KeyBinding> keys() {
            return List.of(KeyBinding.of(Key.ESCAPE, "the keys given back, when the text did not take it"));
        }
    }

    public static final JsonInputModule INSTANCE = new JsonInputModule();

    @Override
    public ImportsFor<JsonInputModule> imports() {
        return ImportsFor.<JsonInputModule>builder()
                .add(new ModuleImports<>(List.of(new BenchWidgetModule.BenchWidget()), BenchWidgetModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new JsonDocStoreModule.JsonDocStore()), JsonDocStoreModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new JsonTextModule.JSON_TEXT()), JsonTextModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new WorkbenchStyles.wb_hint(), new WorkbenchStyles.wb_bar(),
                        new WorkbenchStyles.wb_json_text(), new WorkbenchStyles.wb_status()), WorkbenchStyles.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<JsonInputModule> exports() { return new ExportsOf<>(INSTANCE, List.of(new JsonInput())); }
}
