package hue.captains.singapura.js.homing.relgrid.workbench;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.jsonkit.JsonTreeViewModule;
import hue.captains.singapura.js.homing.relgrid.RelGridStyles;

import java.util.List;

/**
 * The JSON Tree bench's two panes, as JS in a file: {@code mountJsonInput}
 * and {@code mountJsonDisplay}, each answering what a widget's body must
 * return. The widgets' Java bodies are one line each — a call in here — so
 * the JavaScript is read, swept and counted where JavaScript lives.
 */
public record JsonBench() implements DomModule<JsonBench> {

    public record mountJsonInput()   implements Exportable._Constant<JsonBench> {}
    public record mountJsonDisplay() implements Exportable._Constant<JsonBench> {}

    public static final JsonBench INSTANCE = new JsonBench();

    @Override public ImportsFor<JsonBench> imports() {
        return ImportsFor.<JsonBench>builder()
                .add(new ModuleImports<>(List.of(new JsonTreeViewModule.JsonTreeView()), JsonTreeViewModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new JsonDocStore.jsonDocStoreShared()), JsonDocStore.INSTANCE))
                .add(new ModuleImports<>(List.of(new RelGridStyles.hrg_frame(), new RelGridStyles.hrg_lit()), RelGridStyles.INSTANCE))
                .add(new ModuleImports<>(
                        List.of(new WorkbenchStyles.wb_root(), new WorkbenchStyles.wb_hint(), new WorkbenchStyles.wb_bar(),
                                new WorkbenchStyles.wb_btn(), new WorkbenchStyles.wb_input(), new WorkbenchStyles.wb_json_text(),
                                new WorkbenchStyles.wb_frame(), new WorkbenchStyles.wb_port(), new WorkbenchStyles.wb_status()),
                        WorkbenchStyles.INSTANCE))
                .build();
    }

    @Override public ExportsOf<JsonBench> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new mountJsonInput(), new mountJsonDisplay()));
    }
}
