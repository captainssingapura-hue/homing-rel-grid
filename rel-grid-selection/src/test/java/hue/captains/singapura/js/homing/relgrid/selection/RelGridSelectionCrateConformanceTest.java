package hue.captains.singapura.js.homing.relgrid.selection;

import hue.captains.singapura.js.homing.conformance.engine.ConformanceEngine;
import hue.captains.singapura.js.homing.conformance.engine.ServedModuleRenderer;
import hue.captains.singapura.js.homing.conformance.rules.CrateDependencyRule;
import hue.captains.singapura.js.homing.conformance.rules.DefaultJsRulePolicy;
import hue.captains.singapura.js.homing.conformance.rules.Finding;
import hue.captains.singapura.js.homing.conformance.rules.OrphanCheck;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** RFC 0044 — every served module is crated, and this crate requires nothing. */
class RelGridSelectionCrateConformanceTest {

    @Test
    void everyServedModuleIsCrated() {
        assertEquals(List.of(), OrphanCheck.check(RelGridSelectionCrate.INSTANCE),
                "every served JS module in rel-grid-selection must be declared in its crate");
    }

    @Test
    void importsRespectCrateBoundaries() {
        assertEquals(List.of(), CrateDependencyRule.check(RelGridSelectionCrate.INSTANCE),
                "every JS import must resolve to the crate itself or one it directly requires");
    }

    @Test
    void theCrateRequiresNothing() {
        assertTrue(RelGridSelectionCrate.INSTANCE.requires().isEmpty(),
                "a crate that requires nothing cannot acquire an import by accident — law 215");
    }

    /** The rule sweep with no ledger, over the text the server serves: a crate begun clean stays clean. */
    @Test
    void theSweepFindsNothing() {
        var findings = new ConformanceEngine(DefaultJsRulePolicy.INSTANCE, new ServedModuleRenderer())
                .checkCrates(List.of(RelGridSelectionCrate.INSTANCE)).stream().map(Finding::fingerprint).toList();
        assertEquals(List.of(), findings, "rel-grid-selection has no ledger: nothing may be found");
    }
}
