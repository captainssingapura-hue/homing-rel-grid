package hue.captains.singapura.js.homing.relgrid.frame;

import hue.captains.singapura.js.homing.component.taxonomy.Component;
import hue.captains.singapura.js.homing.component.taxonomy.Taxonomy;
import hue.captains.singapura.js.homing.component.taxonomy.VirtualComponent;
import hue.captains.singapura.js.homing.design.Carrier;
import hue.captains.singapura.js.homing.design.Owns;
import hue.captains.singapura.js.homing.design.Target;
import hue.captains.singapura.js.homing.relgrid.frame.GridFrame.CursorBorder;
import hue.captains.singapura.js.homing.relgrid.frame.GridFrame.HeldCursorBorder;
import hue.captains.singapura.js.homing.relgrid.frame.GridFrame.Lattice;
import hue.captains.singapura.js.homing.relgrid.frame.GridFrame.LentCursorBorder;
import hue.captains.singapura.js.homing.relgrid.frame.GridFrame.SelectionFill;
import hue.captains.singapura.js.homing.ui.taxonomy.HouseTaxonomy;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The grid's frame in the house's taxonomy (RFC 0050 · Episode 2 — Appendix: The Frame): a design branch whose
 * leaves are all virtual, never cast, each owning targets a sheet carries - and those worn together on a slot owning
 * different ones.
 */
class GridFrameTest {

    private static final Taxonomy HOUSE = HouseTaxonomy.INSTANCE.read();

    @Test
    void theFrameIsADesignBranchOfTheHouse_itsLeavesTheExtensions() {
        assertTrue(HOUSE.branches().contains(GridFrame.INSTANCE), "in the house's tree wherever the grid is on the classpath");
        var leaves = HOUSE.children(GridFrame.INSTANCE);
        assertEquals(GridFrameExtension.INSTANCE.components(), leaves, "every leaf the extension lists, in its order, and nothing else");
        assertTrue(leaves.stream().allMatch(l -> l instanceof VirtualComponent<?>), "every leaf virtual");
    }

    @Test
    void noLeafOfTheFrameIsCast() {
        assertFalse(HOUSE.castings().stream().anyMatch(c -> c.component() instanceof VirtualComponent<?>),
                "a design's concrete leaf is worn, never the leaf");
    }

    @Test
    void everyLeafOwnsTargetsASheetCarries_eachOnce() {
        for (Component<?> leaf : GridFrameExtension.INSTANCE.components()) {
            List<Target> owned = ((Owns) leaf).owns();
            assertFalse(owned.isEmpty(), leaf.token() + " owns nothing");
            assertEquals(owned.size(), new HashSet<>(owned).size(), leaf.token() + " owns a target twice");
            assertTrue(owned.stream().allMatch(t -> t.carrier() == Carrier.CSS), leaf.token() + " owns a target no sheet carries");
        }
    }

    @Test
    void theLeavesWornOnABodySlot_ownDifferentTargets() {
        for (Component<?> cursor : List.of(CursorBorder.INSTANCE, HeldCursorBorder.INSTANCE, LentCursorBorder.INSTANCE)) {
            var worn = List.of(Lattice.INSTANCE, SelectionFill.INSTANCE, cursor);
            var seen = new HashSet<Target>();
            for (Component<?> leaf : worn)
                for (Target t : ((Owns) leaf).owns())
                    assertTrue(seen.add(t), t.token() + " is owned twice on a body slot, by " + leaf.token() + " among " + worn);
        }
    }
}
