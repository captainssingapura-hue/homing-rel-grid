package hue.captains.singapura.js.homing.relgrid;

import hue.captains.singapura.js.homing.core.Crate;
import hue.captains.singapura.js.homing.core.CrateEntry;
import hue.captains.singapura.js.homing.core.StandardJsModuleType;
import hue.captains.singapura.js.homing.relchannel.RelChannelCrate;
import hue.captains.singapura.js.homing.relgrid.protocol.RelGridProtocolCrate;
import hue.captains.singapura.js.homing.relgrid.selection.RelGridSelectionCrate;

import hue.captains.singapura.js.homing.relgrid.frame.GridFrameModule;

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

    /** The selection, the protocol and the channel's core. None requires anything
     *  itself — map 5's law 215, ext4's law 202 — so the grid's dependencies run one way only. */
    @Override public List<Crate> requires() {
        return List.of(RelGridSelectionCrate.INSTANCE, RelGridProtocolCrate.INSTANCE, RelChannelCrate.INSTANCE);
    }

    @Override
    public List<CrateEntry> entries() {
        return List.of(
                // PURE_LOGIC touches no DOM; everything else is undeclared, i.e. a
                // CONSUMER under the full DOM-owner discipline. Nothing here is a
                // structural primitive: a grid is a component a widget composes, not
                // a pane the shell is made of.
                new CrateEntry(RelGridViewMapsModule.INSTANCE, StandardJsModuleType.PURE_LOGIC),
                // The copied state: the blocks last copied, by identity, and where they stand.
                new CrateEntry(RelGridCopiedModule.INSTANCE, StandardJsModuleType.PURE_LOGIC),
                // The frame's leaves, generated from the house's taxonomy: what the grid asks its design to wear.
                new CrateEntry(GridFrameModule.INSTANCE, StandardJsModuleType.PURE_LOGIC),
                // The looks, typed: generated CSS modules, one for the grid's chrome and one for the stock cell.
                new CrateEntry(RelGridStyles.INSTANCE),
                new CrateEntry(RelGridHeaderDragModule.INSTANCE),
                new CrateEntry(RelGridRevealModule.INSTANCE),
                new CrateEntry(RelGridSlotsModule.INSTANCE),
                new CrateEntry(RelGridOverlaysModule.INSTANCE),
                new CrateEntry(RelGridCopiedOverlayModule.INSTANCE),
                new CrateEntry(RelGridMarksModule.INSTANCE),
                new CrateEntry(RelGridLayoutModule.INSTANCE),
                new CrateEntry(RelGridClipboardModule.INSTANCE),
                new CrateEntry(RelGridWidthsModule.INSTANCE),
                new CrateEntry(RelGridHeadersModule.INSTANCE),
                new CrateEntry(RelGridSpansModule.INSTANCE),
                new CrateEntry(RelGridWindowModule.INSTANCE),
                new CrateEntry(RelGridCursorModule.INSTANCE),
                new CrateEntry(RelGridControlModule.INSTANCE),
                new CrateEntry(RelGridChannelModule.INSTANCE),
                new CrateEntry(RelGridGesturesModule.INSTANCE),
                new CrateEntry(RelGridCellsModule.INSTANCE),
                new CrateEntry(RelGridModule.INSTANCE));
    }
}
