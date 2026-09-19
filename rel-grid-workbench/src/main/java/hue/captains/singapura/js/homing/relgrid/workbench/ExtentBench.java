package hue.captains.singapura.js.homing.relgrid.workbench;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;

import java.util.List;

/**
 * The Colour Extents bench's two panes, as JS in a file: {@code
 * mountExtentTable} — six scaled words at nine extents each — and {@code
 * mountExtentCard} — one card wearing three scaled words, one slider. The
 * widgets' Java bodies are one line each.
 */
public record ExtentBench() implements DomModule<ExtentBench> {

    public record mountExtentTable() implements Exportable._Constant<ExtentBench> {}
    public record mountExtentCard()  implements Exportable._Constant<ExtentBench> {}

    public static final ExtentBench INSTANCE = new ExtentBench();

    @Override public ImportsFor<ExtentBench> imports() {
        return ImportsFor.<ExtentBench>builder()
                .add(new ModuleImports<>(
                        List.of(new WorkbenchStyles.wb_root(), new WorkbenchStyles.wb_hint(), new WorkbenchStyles.wb_bar(), new WorkbenchStyles.wb_btn()),
                        WorkbenchStyles.INSTANCE))
                .add(new ModuleImports<>(
                        List.of(new ExtentStyles.wb_extent_table(), new ExtentStyles.wb_extent_head(), new ExtentStyles.wb_extent_label(), new ExtentStyles.wb_extent_cell(),
                                new ExtentStyles.wb_x_success_ink(), new ExtentStyles.wb_x_danger_surface(), new ExtentStyles.wb_x_primary_surface(),
                                new ExtentStyles.wb_x_primary_ink(), new ExtentStyles.wb_x_raised_surface(), new ExtentStyles.wb_x_warning_edge(),
                                new ExtentStyles.wb_extent_card(), new ExtentStyles.wb_extent_card_title(), new ExtentStyles.wb_extent_slider(), new ExtentStyles.wb_extent_range()),
                        ExtentStyles.INSTANCE))
                .build();
    }

    @Override public ExportsOf<ExtentBench> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new mountExtentTable(), new mountExtentCard()));
    }
}
