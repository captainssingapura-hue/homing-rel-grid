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
 * One outlet's book as a root relation over the {@code SalesStore} — every
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
public record OutletRelationModule() implements DomModule<OutletRelationModule> {

    public record OutletRelation() implements Exportable._Class<OutletRelationModule> {}
    public record OutletFence() implements Exportable._Class<OutletRelationModule> {}
    public record LedgerFence() implements Exportable._Class<OutletRelationModule> {}

    public static final OutletRelationModule INSTANCE = new OutletRelationModule();

    @Override
    public ImportsFor<OutletRelationModule> imports() {
        return ImportsFor.<OutletRelationModule>builder()
                .add(new ModuleImports<>(List.of(new RelGridStockCellsModule.RelGridTextCell()), RelGridStockCellsModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new RelGridProtocolModule.RelGridGroupFold()), RelGridProtocolModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new OutletFenceStyles.wb_fence(), new OutletFenceStyles.wb_fence_name(),
                        new OutletFenceStyles.wb_fence_fold(), new OutletFenceStyles.wb_fence_fold_hot(),
                        new OutletFenceStyles.wb_fence_totals(), new OutletFenceStyles.wb_fence_ledger()), OutletFenceStyles.INSTANCE))
                .build();
    }

    @Override public ExportsOf<OutletRelationModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new OutletRelation(), new OutletFence(), new LedgerFence()));
    }
}
