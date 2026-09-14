package hue.captains.singapura.js.homing.reltree;

import hue.captains.singapura.js.homing.conformance.rules.CrateDependencyRule;
import hue.captains.singapura.js.homing.conformance.rules.OrphanCheck;
import hue.captains.singapura.js.homing.relgrid.RelGridConformanceSweep;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** RFC 0044 — every served module is crated, every import stays inside the crate, and the sweep finds nothing. */
class RelTreeCrateConformanceTest {

    @Test
    void everyServedModuleIsCrated() {
        assertEquals(List.of(), OrphanCheck.check(RelTreeCrate.INSTANCE),
                "every served JS module in rel-tree must be declared in its crate");
    }

    @Test
    void importsRespectCrateBoundaries() {
        assertEquals(List.of(), CrateDependencyRule.check(RelTreeCrate.INSTANCE),
                "every JS import must resolve to the crate itself or one it directly requires");
    }

    /** The sweep against a ledger that is EMPTY and stays so: a crate begun clean never acquires a violation. */
    @Test
    void ruleSweepAgainstAnEmptyLedger() throws IOException {
        RelGridConformanceSweep.assertLedger(RelTreeCrate.INSTANCE, Path.of("src/test/resources/rfc0044-ledger.txt"));
    }
}
