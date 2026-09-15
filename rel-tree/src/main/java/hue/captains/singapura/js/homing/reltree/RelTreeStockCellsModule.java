package hue.captains.singapura.js.homing.reltree;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;

import java.util.List;

/**
 * A stock cell for a tree node: a line of text on the domain's branch. Ships
 * with the tree as the grid's text cell ships with the grid — domain-side,
 * holding what it shows, never read by the tree.
 */
public record RelTreeStockCellsModule() implements DomModule<RelTreeStockCellsModule> {
    public record RelTreeTextCell() implements Exportable._Constant<RelTreeStockCellsModule> {}
    public static final RelTreeStockCellsModule INSTANCE = new RelTreeStockCellsModule();
    @Override public ImportsFor<RelTreeStockCellsModule> imports() {
        return ImportsFor.<RelTreeStockCellsModule>builder()
                .add(new ModuleImports<>(List.of(new RelTreeStockStyles.hrt_text_cell(), new RelTreeStockStyles.hrt_text_cell_current(),
                        new RelTreeStockStyles.hrt_text_cell_busy()),
                        RelTreeStockStyles.INSTANCE))
                .build();
    }
    @Override public ExportsOf<RelTreeStockCellsModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new RelTreeTextCell()));
    }
}
