package hue.captains.singapura.js.homing.relgrid.workbench;

import hue.captains.singapura.js.homing.studio.base.Bootstrap;
import hue.captains.singapura.js.homing.studio.base.DefaultRuntimeParams;
import hue.captains.singapura.js.homing.studio.base.Umbrella;
import hue.captains.singapura.js.homing.reltree.RelTreeStockStyles;
import hue.captains.singapura.js.homing.studio.starter.StudioStarterFixtures;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The solo studio composes cleanly, and the bench list registers what it names. */
class GridWorkbenchBootsTest {

    @Test
    void theSoloStudioComposesCleanly() {
        // Mirrors GridWorkbenchServer.main's umbrella exactly.
        var umbrella = new Umbrella.Solo<>(GridWorkbenchStudio.INSTANCE);
        assertDoesNotThrow(() ->
                new Bootstrap<>(new WorkbenchFixtures<>(new StudioStarterFixtures<>(umbrella)), new DefaultRuntimeParams(0)).compose());
    }

    @Test
    void theBenchListIsTheRegistration() {
        assertEquals("replicatingTables", GridWorkbenchStudio.landingKind());
        assertEquals(7, GridWorkbenchStudio.benches().size());
    }

    /** The specimens each bench registers; the keyframes half of the busy ring is parked until the framework types a movement (RFC 0066 retired the globals it was joined in). */
    @Test
    void everyBenchRegistersItsSpecimens() {
        var fixtures = new WorkbenchFixtures<>(new StudioStarterFixtures<>(new Umbrella.Solo<>(GridWorkbenchStudio.INSTANCE)));
        assertTrue(fixtures.themeRegistry().themes().size() >= 2, "the starter's themes are the registry's");
        // Three editors with different rights, a follower, and the outlets group — five specimens.
        assertEquals(5, ReplicatingTablesSpec.INSTANCE.widgetEntries().size());
        // An editor, a display, a stress table and the articles group — four specimens.
        assertEquals("hanArticle", HanArticleSpec.INSTANCE.kind());
        assertEquals(4, HanArticleSpec.INSTANCE.widgetEntries().size());
        // The endless table — one specimen, docked twice to compare.
        assertEquals("endlessTable", EndlessTableSpec.INSTANCE.kind());
        assertEquals(1, EndlessTableSpec.INSTANCE.widgetEntries().size());
        // The games catalogue — one specimen, sorting and filtering for itself.
        assertEquals("gamesCatalogue", GamesCatalogueSpec.INSTANCE.kind());
        assertEquals(1, GamesCatalogueSpec.INSTANCE.widgetEntries().size());
        // The JSON tree — an input and a display over one document.
        assertEquals("jsonTree", JsonTreeSpec.INSTANCE.kind());
        assertEquals(2, JsonTreeSpec.INSTANCE.widgetEntries().size());
    }
}
