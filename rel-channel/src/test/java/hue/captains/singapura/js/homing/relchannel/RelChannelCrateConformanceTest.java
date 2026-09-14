package hue.captains.singapura.js.homing.relchannel;

import hue.captains.singapura.js.homing.conformance.rules.CrateDependencyRule;
import hue.captains.singapura.js.homing.conformance.rules.DefaultJsRulePolicy;
import hue.captains.singapura.js.homing.conformance.rules.Finding;
import hue.captains.singapura.js.homing.conformance.rules.OrphanCheck;
import hue.captains.singapura.js.homing.conformance.rules.ServedModule;
import hue.captains.singapura.js.homing.core.CrateEntry;
import hue.captains.singapura.js.homing.core.JsModuleType;
import hue.captains.singapura.js.homing.core.StandardJsModuleType;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
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

    /** The rule sweep with no ledger: a crate begun clean stays clean. */
    @Test
    void theSweepFindsNothing() {
        var findings = new ArrayList<String>();
        for (CrateEntry entry : RelChannelCrate.INSTANCE.entries()) {
            String cls = entry.moduleClass();
            String text = resource("/homing/js/" + cls.replace('.', '/') + ".js");
            JsModuleType type = entry.declaredType() != null ? entry.declaredType() : StandardJsModuleType.CONSUMER;
            for (Finding f : DefaultJsRulePolicy.INSTANCE.rulesFor(type).checkAll(ServedModule.of(cls, type, text)))
                findings.add(f.fingerprint());
        }
        assertEquals(List.of(), findings, "rel-channel has no ledger: nothing may be found");
    }

    private static String resource(String path) {
        try (InputStream in = RelChannelCrateConformanceTest.class.getResourceAsStream(path)) {
            if (in == null) throw new IllegalStateException("no such resource: " + path);
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
