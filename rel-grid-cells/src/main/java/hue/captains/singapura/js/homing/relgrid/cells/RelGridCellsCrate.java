package hue.captains.singapura.js.homing.relgrid.cells;

import hue.captains.singapura.js.homing.core.Crate;
import hue.captains.singapura.js.homing.core.CrateEntry;
import hue.captains.singapura.js.homing.ui.look.UiLookCrate;

import java.util.List;

/**
 * The stock cells - a text cell, and a header cell that shows a label - out of the box, in a crate of their own
 * (RFC 0050 · Episode 2 — Appendix: The Frame). Nothing in the grid's crate privileges them: a page whose relation
 * builds its cells from these requires this crate beside the grid's. It requires the house's look, whose cell
 * leaves the cells wear.
 */
public final class RelGridCellsCrate implements Crate {

    public static final RelGridCellsCrate INSTANCE = new RelGridCellsCrate();

    private RelGridCellsCrate() {}

    @Override public String name() { return "rel-grid-cells"; }

    @Override public List<Crate> requires() { return List.of(UiLookCrate.INSTANCE); }

    @Override public List<CrateEntry> entries() {
        return List.of(CrateEntry.of(RelGridStockStyles.INSTANCE),
                       CrateEntry.of(RelGridStockCellsModule.INSTANCE),
                       CrateEntry.of(RelGridLabelsModule.INSTANCE));
    }
}
