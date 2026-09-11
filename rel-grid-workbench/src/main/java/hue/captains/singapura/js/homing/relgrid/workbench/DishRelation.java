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
 * directly; its cells commit to the store. Nothing here mentions a grid — this
 * is the domain's whole side of the seam.
 *
 * <p>Built for a <b>role</b>. Who may edit which column is the relation's
 * decision, made per cell as the cell is built: a cell whose column this role
 * may not write is built without a commit target, and such a cell declines
 * when the grid asks it to go deep. That is the situational "no" of
 * RFC 0050 · Episode 2 map 15 (law 105) and map 16 (law 113) — decided by the
 * cell when asked, never declared to the grid — which is why adding roles
 * changed no grid code at all.</p>
 */
public record DishRelation() implements DomModule<DishRelation> {

    public record createDishRelation() implements Exportable._Constant<DishRelation> {}
    public record dishRoles() implements Exportable._Constant<DishRelation> {}

    public static final DishRelation INSTANCE = new DishRelation();

    @Override
    public ImportsFor<DishRelation> imports() {
        // The relation builds its manager from two cells — both domain-side.
        // One ships with the grid; one is the bench's own, and the grid cannot
        // tell which is which.
        return ImportsFor.<DishRelation>builder()
                .add(new ModuleImports<>(List.of(new RelGridStockCellsModule.RelGridTextCell()), RelGridStockCellsModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new DishStarsCellModule.DishStarsCell()), DishStarsCellModule.INSTANCE))
                .build();
    }

    @Override public ExportsOf<DishRelation> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new createDishRelation(), new dishRoles()));
    }
}
