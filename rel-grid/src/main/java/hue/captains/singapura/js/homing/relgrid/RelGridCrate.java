package hue.captains.singapura.js.homing.relgrid;

import hue.captains.singapura.js.homing.core.Crate;
import hue.captains.singapura.js.homing.core.CrateEntry;
import hue.captains.singapura.js.homing.core.StandardJsModuleType;
import hue.captains.singapura.js.homing.relgrid.protocol.RelGridProtocolCrate;
import hue.captains.singapura.js.homing.relgrid.selection.RelGridSelectionCrate;

import java.util.List;

/**
 * RFC 0044 — the {@link Crate} for {@code homing-rel-grid}. One crate
 * per module; every served JS module declared with its type, so conformance is
 * ambient from the first line.
 */
public final class RelGridCrate implements Crate {

    public static final RelGridCrate INSTANCE = new RelGridCrate();

    private RelGridCrate() {}

    @Override public String name() { return "homing-rel-grid"; }

    /** The selection and the protocol. Neither requires anything itself — map 5's
     *  law 215 and ext4's law 202 — so the grid's dependencies run one way only. */
    @Override public List<Crate> requires() {
        return List.of(RelGridSelectionCrate.INSTANCE, RelGridProtocolCrate.INSTANCE);
    }

    @Override
    public List<CrateEntry> entries() {
        return List.of(
                // PURE_LOGIC touches no DOM; everything else is undeclared, i.e. a
                // CONSUMER under the full DOM-owner discipline. Nothing here is a
                // structural primitive: a grid is a component a widget composes, not
                // a pane the shell is made of.
                CrateEntry.of(RelGridViewMapsModule.INSTANCE, StandardJsModuleType.PURE_LOGIC),
                CrateEntry.of(RelGridHeaderDragModule.INSTANCE),
                CrateEntry.of(RelGridLayoutModule.INSTANCE),
                CrateEntry.of(RelGridCellsModule.INSTANCE),
                CrateEntry.of(RelGridStockCellsModule.INSTANCE),
                CrateEntry.of(RelGridModule.INSTANCE));
    }
}
