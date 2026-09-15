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
                new Bootstrap<>(new StudioStarterFixtures<>(umbrella), new DefaultRuntimeParams(0)).compose());
    }

    @Test
    void theBenchListIsTheRegistration() {
        assertEquals("replicatingTables", GridWorkbenchStudio.landingKind());
        assertEquals(6, GridWorkbenchStudio.benches().size());
    }

    /** The class that plays hrt-spin renders from the crate; the keyframes it names reach every theme through the fixtures. */
    @Test
    void everyThemesGlobalsCarryTheKeyframesTheCratesName() {
        var fixtures = new WorkbenchFixtures<>(new StudioStarterFixtures<>(new Umbrella.Solo<>(GridWorkbenchStudio.INSTANCE)));
        var registry = fixtures.themeRegistry();
        assertTrue(registry.themes().size() >= 2, "the starter's themes are the registry's");
        for (var theme : registry.themes()) {
            var globals = registry.globalsForSlug(theme.slug());
            assertNotNull(globals, "globals for " + theme.slug());
            String rendered = globals.chunks().isEmpty() ? globals.css() : globals.chunks().get(hue.captains.singapura.js.homing.core.Component.class);
            assertTrue(rendered != null && rendered.contains("@keyframes " + RelTreeStockStyles.SPIN),
                    theme.slug() + " carries @keyframes " + RelTreeStockStyles.SPIN + " in its component layer");
        }
        // And the class half names exactly that movement.
        assertTrue(new RelTreeStockStyles.hrt_text_cell_busy().body().contains("animation: " + RelTreeStockStyles.SPIN + " "),
                "the ring plays the keyframes the crate declares");
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
