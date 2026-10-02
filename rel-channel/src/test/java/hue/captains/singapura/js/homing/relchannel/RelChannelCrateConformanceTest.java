package hue.captains.singapura.js.homing.relchannel;

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

/** RFC 0044 — every served module is crated, this crate requires nothing, and the sweep finds nothing. */
class RelChannelCrateConformanceTest {

    @Test
    void everyServedModuleIsCrated() {
        assertEquals(List.of(), OrphanCheck.check(RelChannelCrate.INSTANCE),
                "every served JS module in rel-channel must be declared in its crate");
    }

    @Test
    void importsRespectCrateBoundaries() {
        assertEquals(List.of(), CrateDependencyRule.check(RelChannelCrate.INSTANCE),
                "every JS import must resolve to the crate itself or one it directly requires");
    }

    @Test
    void theCrateRequiresNothing() {
        assertTrue(RelChannelCrate.INSTANCE.requires().isEmpty(),
                "the crate every presentation's channel stands on may reach none of them");
    }

    /** The rule sweep with no ledger, over the text the server serves: a crate begun clean stays clean. */
    @Test
    void theSweepFindsNothing() {
        var findings = new ConformanceEngine(DefaultJsRulePolicy.INSTANCE, new ServedModuleRenderer())
                .checkCrates(List.of(RelChannelCrate.INSTANCE)).stream().map(Finding::fingerprint).toList();
        assertEquals(List.of(), findings, "rel-channel has no ledger: nothing may be found");
    }
}
