package hue.captains.singapura.js.homing.relgrid.workbench;

import hue.captains.singapura.js.homing.conformance.rules.CrateDependencyRule;
import hue.captains.singapura.js.homing.conformance.rules.OrphanCheck;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** RFC 0044 — every served bench module is crated, and every import stays inside the crate or its requires. */
class RelGridWorkbenchConformanceTest {

    @Test
    void everyServedModuleIsCrated() {
        assertEquals(List.of(), OrphanCheck.check(RelGridWorkbenchCrate.INSTANCE),
                "every served JS module in rel-grid-workbench must be declared in its crate");
    }

    @Test
    void importsRespectCrateBoundaries() {
        assertEquals(List.of(), CrateDependencyRule.check(RelGridWorkbenchCrate.INSTANCE),
                "every JS import must resolve to the crate itself or one it directly requires");
    }
}
