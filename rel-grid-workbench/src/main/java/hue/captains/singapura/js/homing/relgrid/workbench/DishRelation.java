package hue.captains.singapura.js.homing.relgrid.workbench;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.relgrid.RelGridStockCellsModule;

import java.util.List;

/**
 * A {@code RootRelationContract}-shaped relation over the {@link DishStore}:
 * identities, columns, and a <b>cell manager</b> that builds each cell once and
 * keeps it. The relation subscribes to the store and updates its own cells
 * directly; an editable relation's cells commit to the store. Nothing here
 * mentions a grid — this is the domain's whole side of the seam.
 */
public record DishRelation() implements DomModule<DishRelation> {

    public record createDishRelation() implements Exportable._Constant<DishRelation> {}

    public static final DishRelation INSTANCE = new DishRelation();

    @Override
    public ImportsFor<DishRelation> imports() {
        // The relation builds its manager from the stock cell — domain-side code.
        return ImportsFor.<DishRelation>builder()
                .add(new ModuleImports<>(List.of(new RelGridStockCellsModule.RelGridTextCell()), RelGridStockCellsModule.INSTANCE))
                .build();
    }

    @Override public ExportsOf<DishRelation> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new createDishRelation()));
    }
}
