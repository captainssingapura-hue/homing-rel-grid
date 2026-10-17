package hue.captains.singapura.js.homing.relgrid.house;

import hue.captains.singapura.js.homing.core.Crate;
import hue.captains.singapura.js.homing.core.CrateEntry;

import java.util.List;

/**
 * The house's local styles of the grid's frame - its grid, and its document table - each a sheet and an object. It requires
 * nothing - an object wears its own classes, and its sheet is its own - so a page that makes a grid with one requires
 * this crate beside the grid's.
 */
public final class RelGridHouseCrate implements Crate {

    public static final RelGridHouseCrate INSTANCE = new RelGridHouseCrate();

    private RelGridHouseCrate() {}

    @Override public String name() { return "rel-grid-house"; }

    @Override public List<Crate> requires() { return List.of(); }

    @Override public List<CrateEntry> entries() {
        return List.of(CrateEntry.of(HouseGridSheet.INSTANCE), CrateEntry.of(HouseGridModule.INSTANCE),
                       CrateEntry.of(HouseProseSheet.INSTANCE), CrateEntry.of(HouseProseModule.INSTANCE));
    }
}
