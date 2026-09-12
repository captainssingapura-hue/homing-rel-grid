package hue.captains.singapura.js.homing.relgrid.workbench;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.relgrid.RelGridStockCellsModule;

import java.util.List;

/**
 * One outlet's book as a root relation over the {@link SalesStore} — every
 * column read-only, cells kept current by the store — and the two
 * <b>fences</b> the Outlets bench puts between its tables: an outlet's name
 * and published totals above each, the ledger's below the last.
 *
 * <p>A fence is a domain object handed a host, as a cell is. Imports the
 * stock text cell and nothing of the grid or the group.</p>
 */
public record OutletRelation() implements DomModule<OutletRelation> {

    public record createOutletRelation() implements Exportable._Constant<OutletRelation> {}
    public record createOutletFence()    implements Exportable._Constant<OutletRelation> {}
    public record createLedgerFence()    implements Exportable._Constant<OutletRelation> {}

    public static final OutletRelation INSTANCE = new OutletRelation();

    @Override
    public ImportsFor<OutletRelation> imports() {
        return ImportsFor.<OutletRelation>builder()
                .add(new ModuleImports<>(List.of(new RelGridStockCellsModule.RelGridTextCell()), RelGridStockCellsModule.INSTANCE))
                .build();
    }

    @Override public ExportsOf<OutletRelation> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new createOutletRelation(), new createOutletFence(), new createLedgerFence()));
    }
}
