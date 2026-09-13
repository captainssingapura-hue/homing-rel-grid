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
 * <p>A fence is a noun, as a cell is: it owns its element, minted on the
 * branch it was handed, and answers {@code fenceElement()} to whoever places
 * it. The outlet's fence carries a fold toggle: pressed, it <i>tells</i> a
 * {@code RelGridGroupFold} down the closure the host built it with — the
 * channel's other direction, host-wired — and draws itself from what it is
 * told: {@code folded} at construction, then every {@code onFolded}. Imports
 * the stock text cell and the protocol's group kind, and nothing of the grid
 * or the group.</p>
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
