package hue.captains.singapura.js.homing.relgrid.protocol;

import hue.captains.singapura.js.homing.codec.ObjectDefinition;
import hue.captains.singapura.js.homing.conformance.rules.CrateDependencyRule;
import hue.captains.singapura.js.homing.conformance.rules.OrphanCheck;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** RFC 0044 and ext5's laws 205 and 210: the crate, the manifest and the exports are one thing. */
class RelGridProtocolCrateConformanceTest {

    @Test
    void everyServedModuleIsCrated() {
        assertEquals(List.of(), OrphanCheck.check(RelGridProtocolCrate.INSTANCE),
                "every served JS module in rel-grid-protocol must be declared in its crate");
    }

    @Test
    void importsRespectCrateBoundaries() {
        assertEquals(List.of(), CrateDependencyRule.check(RelGridProtocolCrate.INSTANCE),
                "the protocol is data: it imports nothing, so nothing can cross a boundary");
    }

    @Test
    void theManifestAndTheExportsAreOneList() {
        var fromManifest = new TreeSet<String>();
        for (ObjectDefinition<?> def : RelGridProtocolManifest.ENTRIES)
            fromManifest.add(def.type().getSimpleName());

        var fromExports = new TreeSet<String>();
        for (var e : RelGridProtocolModule.INSTANCE.exports().exports())
            fromExports.add(e.getClass().getSimpleName());

        assertEquals(fromManifest, fromExports,
                "law 210: every manifest entry has its Exportable._Class, and every export has an entry");
    }

    @Test
    void generatedCodeIsNotCommitted() {
        // Law 205 — Generated and Hand-Written Live Apart. The module's own
        // resources directory must not exist, or must not hold the generated body.
        Path resources = Path.of("src", "main", "resources");
        var offences = new ArrayList<String>();
        if (Files.isDirectory(resources)) {
            try (var walk = Files.walk(resources)) {
                walk.filter(Files::isRegularFile).forEach(p -> offences.add(p.toString()));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        assertEquals(List.of(), offences,
                "generated JS lives in target/classes; this module's src/main/resources stays empty");
        assertTrue(RelGridProtocolGen.FILE_NAME.equals(RelGridProtocolModule.class.getSimpleName() + ".js"),
                "the generated filename must equal the DomModule's simple name, or the body is not found");
    }
}
