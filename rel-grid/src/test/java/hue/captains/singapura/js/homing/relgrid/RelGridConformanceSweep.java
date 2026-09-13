package hue.captains.singapura.js.homing.relgrid;

import hue.captains.singapura.js.homing.conformance.rules.Baseline;
import hue.captains.singapura.js.homing.conformance.rules.DefaultJsRulePolicy;
import hue.captains.singapura.js.homing.conformance.rules.Finding;
import hue.captains.singapura.js.homing.conformance.rules.FindingGrader;
import hue.captains.singapura.js.homing.conformance.rules.GradedFinding;
import hue.captains.singapura.js.homing.conformance.rules.ServedModule;
import hue.captains.singapura.js.homing.core.Crate;
import hue.captains.singapura.js.homing.core.CrateEntry;
import hue.captains.singapura.js.homing.core.JsModuleType;
import hue.captains.singapura.js.homing.core.StandardJsModuleType;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * RFC 0044 — the JS rule sweep over a crate's own modules, with a committed
 * <b>ledger</b> of pre-existing violations that can only shrink.
 *
 * <p>Every crate entry backed by a {@code .js} resource is swept under the rule
 * set its declared type owes ({@link DefaultJsRulePolicy}). A finding not in the
 * ledger is NEW and fails the build; a ledger line that no longer matches any
 * finding is STALE and fails the build too — a violation that was fixed must
 * leave the ledger, so the ledger is an honest count of what is left. Run with
 * {@code -Dhoming.conformance.record=true} to rewrite the ledger from the
 * current findings — deliberately, never to silence a fresh violation.</p>
 *
 * <p>Modules with no {@code .js} resource (a widget's Java-emitted body) are
 * not swept here; that is the module server's renderer's to produce.</p>
 */
public final class RelGridConformanceSweep {

    private RelGridConformanceSweep() {}

    public static final String RECORD_PROPERTY = "homing.conformance.record";

    /** Every finding over the crate's own resource-backed modules, under each module's declared rule set. */
    public static List<Finding> findings(Crate crate) {
        var out = new ArrayList<Finding>();
        for (CrateEntry entry : crate.entries()) {
            String cls = entry.moduleClass();
            String text = resource(cls);
            if (text == null) continue;                          // Java-emitted: not swept here
            JsModuleType type = entry.declaredType() != null ? entry.declaredType() : StandardJsModuleType.CONSUMER;   // undeclared = the full discipline
            out.addAll(DefaultJsRulePolicy.INSTANCE.rulesFor(type).checkAll(ServedModule.of(cls, type, text)));
        }
        return out;
    }

    /** Fail unless every finding is in the ledger and every ledger line is a finding. Records instead when asked. */
    public static void assertLedger(Crate crate, Path ledger) throws IOException {
        var findings = findings(crate);
        if (Boolean.getBoolean(RECORD_PROPERTY)) {
            Files.createDirectories(ledger.getParent());
            var lines = new ArrayList<String>();
            lines.add("# RFC 0044 ledger for crate '" + crate.name() + "': pre-existing violations, one fingerprint per line.");
            lines.add("# It can only shrink. Regenerate with -D" + RECORD_PROPERTY + "=true, deliberately — never to silence a fresh violation.");
            lines.addAll(Baseline.record(findings));
            Files.write(ledger, lines, StandardCharsets.UTF_8);
            return;
        }
        var baseline = Files.exists(ledger) ? Baseline.of(Files.readAllLines(ledger, StandardCharsets.UTF_8)) : Baseline.EMPTY;
        var graded = new FindingGrader(List.of(), baseline, true).grade(findings);
        var errors = new ArrayList<String>();
        for (GradedFinding g : graded)
            if (g.isError()) errors.add("NEW  " + g.finding().fingerprint());
        var seen = new TreeSet<String>();
        for (Finding f : findings) seen.add(f.fingerprint());
        for (String fp : baseline.fingerprints())
            if (!seen.contains(fp)) errors.add("STALE (fixed: remove it from the ledger)  " + fp);
        if (!errors.isEmpty())
            fail("crate '" + crate.name() + "' against " + ledger.getFileName() + " (" + baseline.size() + " ledgered, "
                 + findings.size() + " found):\n  " + String.join("\n  ", errors));
    }

    /** The module's authored JS, from the classpath; null when the module is not resource-backed. */
    static String resource(String moduleClass) {
        String path = "/homing/js/" + moduleClass.replace('.', '/') + ".js";
        try (InputStream in = RelGridConformanceSweep.class.getResourceAsStream(path)) {
            if (in == null) return null;
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("could not read " + path, e);
        }
    }
}
