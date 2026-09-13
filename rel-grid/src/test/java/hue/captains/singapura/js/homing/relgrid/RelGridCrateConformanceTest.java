package hue.captains.singapura.js.homing.relgrid;

import hue.captains.singapura.js.homing.conformance.rules.CrateDependencyRule;
import hue.captains.singapura.js.homing.conformance.rules.OrphanCheck;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** RFC 0044 — every served module is crated, and every import stays inside the crate. */
class RelGridCrateConformanceTest {

    @Test
    void everyServedModuleIsCrated() {
        assertEquals(List.of(), OrphanCheck.check(RelGridCrate.INSTANCE),
                "every served JS module in homing-rel-grid must be declared in its crate");
    }

    @Test
    void importsRespectCrateBoundaries() {
        assertEquals(List.of(), CrateDependencyRule.check(RelGridCrate.INSTANCE),
                "every JS import must resolve to the crate itself or one it directly requires");
    }

    /**
     * RFC 0044 — the rule sweep, under each module's declared type, against the
     * committed ledger of pre-existing violations. A NEW finding fails; so does a
     * ledger line no longer found, so the ledger only ever shrinks.
     */
    @Test
    void ruleSweepAgainstTheLedger() throws IOException {
        RelGridConformanceSweep.assertLedger(RelGridCrate.INSTANCE, Path.of("src/test/resources/rfc0044-ledger.txt"));
    }
}
