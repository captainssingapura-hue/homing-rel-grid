package hue.captains.singapura.js.homing.relgrid.group;

import hue.captains.singapura.js.homing.conformance.rules.CrateDependencyRule;
import hue.captains.singapura.js.homing.conformance.rules.OrphanCheck;
import hue.captains.singapura.js.homing.relgrid.RelGridCrate;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** RFC 0044 — every served module is crated, and the group requires the grid and nothing else. */
class RelGridGroupCrateConformanceTest {

    @Test
    void everyServedModuleIsCrated() {
        assertEquals(List.of(), OrphanCheck.check(RelGridGroupCrate.INSTANCE),
                "every served JS module in rel-grid-group must be declared in its crate");
    }

    @Test
    void importsRespectCrateBoundaries() {
        assertEquals(List.of(), CrateDependencyRule.check(RelGridGroupCrate.INSTANCE),
                "every JS import must resolve to the crate itself or one it directly requires");
    }

    @Test
    void theGroupIsBuiltOnTheGridAndTheGridKnowsNothingOfIt() {
        assertEquals(List.of(RelGridCrate.INSTANCE), RelGridGroupCrate.INSTANCE.requires(),
                "the group requires the grid's crate and nothing else");
        assertEquals(false, RelGridCrate.INSTANCE.requires().contains(RelGridGroupCrate.INSTANCE),
                "and the dependency runs one way only");
    }
}
