package hue.captains.singapura.js.homing.relgrid.workbench;

import hue.captains.singapura.js.homing.studio.base.Bootstrap;
import hue.captains.singapura.js.homing.studio.base.DefaultRuntimeParams;
import hue.captains.singapura.js.homing.studio.base.Umbrella;
import hue.captains.singapura.js.homing.studio.starter.StudioStarterFixtures;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

/** The solo studio composes cleanly, and the bench list registers what it names. */
class GridWorkbenchBootsTest {

    @Test
    void theSoloStudioComposesCleanly() {
        // Mirrors GridWorkbenchServer.main's umbrella exactly.
        var umbrella = new Umbrella.Solo<>(GridWorkbenchStudio.INSTANCE);
        assertDoesNotThrow(() ->
                new Bootstrap<>(new StudioStarterFixtures<>(umbrella), new DefaultRuntimeParams(0)).compose());
    }

    @Test
    void theBenchListIsTheRegistration() {
        assertEquals("replicatingTables", GridWorkbenchStudio.landingKind());
        assertEquals(2, GridWorkbenchStudio.benches().size());
        // Three editors with different rights, a follower, and the outlets group — five specimens.
        assertEquals(5, ReplicatingTablesSpec.INSTANCE.widgetEntries().size());
        // An editor, a display, a stress table and the articles group — four specimens.
        assertEquals("hanArticle", HanArticleSpec.INSTANCE.kind());
        assertEquals(4, HanArticleSpec.INSTANCE.widgetEntries().size());
    }
}
