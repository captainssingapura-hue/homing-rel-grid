package hue.captains.singapura.js.homing.relgrid.cells;

import hue.captains.singapura.js.homing.conformance.rules.CrateDependencyRule;
import hue.captains.singapura.js.homing.conformance.rules.OrphanCheck;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** RFC 0044 — every served module of the stock cells is crated, and every import stays inside the crate. */
class RelGridCellsCrateConformanceTest {

    @Test
    void everyServedModuleIsCrated() {
        assertEquals(List.of(), OrphanCheck.check(RelGridCellsCrate.INSTANCE),
                "every served JS module in rel-grid-cells must be declared in its crate");
    }

    @Test
    void importsRespectCrateBoundaries() {
        assertEquals(List.of(), CrateDependencyRule.check(RelGridCellsCrate.INSTANCE),
                "every JS import must resolve to the crate itself or one it directly requires");
    }
}
