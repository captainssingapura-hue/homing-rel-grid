package hue.captains.singapura.js.homing.relgrid.workbench;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.relgrid.RelGridStockCellsModule;
import hue.captains.singapura.js.homing.relgrid.protocol.RelGridProtocolModule;

import java.util.List;

/**
 * One outlet's book as a root relation over the {@link SalesStore} — every
 * column read-only, cells kept current by the store — and the two
 * <b>fences</b> the Outlets bench puts between its tables: an outlet's name
 * and published totals above each, the ledger's below the last.
 *
 * <p>A fence is a domain object handed a host, as a cell is. The outlet's
 * fence carries a fold toggle: pressed, it <i>tells</i> the group a
 * {@code RelGridGroupFold} through the handle it was given — the channel's
 * other direction — and draws itself from {@code onFolded}. Imports the stock
 * text cell and the protocol's group kind, and nothing of the grid or the
 * group.</p>
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
                .add(new ModuleImports<>(List.of(new RelGridProtocolModule.RelGridGroupFold()), RelGridProtocolModule.INSTANCE))
                .build();
    }

    @Override public ExportsOf<OutletRelation> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new createOutletRelation(), new createOutletFence(), new createLedgerFence()));
    }
}
