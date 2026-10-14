package hue.captains.singapura.js.homing.relgrid.house;

import hue.captains.singapura.js.homing.design.semantic.DesignRead;
import hue.captains.singapura.js.homing.relgrid.frame.GridFrame;
import hue.captains.singapura.js.homing.relgrid.frame.GridFrameExtension;
import hue.captains.singapura.js.homing.ui.look.HouseDesigns;
import hue.captains.singapura.js.homing.ui.styles.HouseStyles;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The house's document table: complete over the grid's frame in every house theme, its rules and its head its own -
 * none taken from a cell, a container - measured per style as the house's document tables were, and what a document has
 * no word for the house grid's.
 */
class HouseProseTest {

    @Test
    void theHouseProse_isCompleteOverTheFrame_inEveryHouseTheme() {
        DesignRead prose = HouseDesigns.INSTANCE.of(HouseProse.INSTANCE);   // read and judged in every house theme
        assertEquals(GridFrame.INSTANCE, prose.branch());
        assertEquals(GridFrameExtension.INSTANCE.components(), prose.concretes().stream().map(c -> c.virtual()).toList(),
                "a concrete leaf for every leaf of the frame, in the frame's order");
        assertEquals("house-prose", prose.token());
    }

    @Test
    void itsRules_areItsOwn_measuredAsTheHousesDocumentTablesWere_inEveryHouseTheme() {
        for (String slug : HouseStyles.THEMES.keySet()) {
            String css = HouseProseSheet.INSTANCE.css(slug);
            assertFalse(css.contains("/* the theme's "), slug + ": nothing taken from a cell - a cell is a container, its room and no line:\n" + css);
            assertTrue(css.contains(".house-prose___lattice__color-border {\n    border-color: "), slug + ": its rows ruled in the hairline");
            assertTrue(css.contains(".house-prose___header-band__shape-border {"), slug);
            assertFalse(css.contains(".house-prose___grid-edge__"), slug + ": a document's table has no edge:\n" + css);
            assertFalse(css.contains(".house-prose___cursor-border__"), slug + ": no cursor while the keys are elsewhere");
            assertTrue(css.contains("/* from house-grid___held-cursor-border__color-outline */\n.house-prose___held-cursor-border__color-outline {"),
                    slug + ": where the keys are is the house grid's");
        }
        String editorial = HouseProseSheet.INSTANCE.css(slug("editorial"));
        assertTrue(editorial.contains(".house-prose___lattice__shape-border {\n    border-width: 0 0 1px 0; border-style: solid;\n}"),
                "each row ruled underneath, as the editorial style rules a document's table:\n" + editorial);
        assertTrue(editorial.contains(".house-prose___header-band__shape-border {\n    border-width: 0;\n}"), "and its head unruled:\n" + editorial);
        assertTrue(HouseProseSheet.INSTANCE.css(slug("neo-brutalism")).contains(".house-prose___lattice__shape-border {\n    border-width: 0 0 2px 0;"),
                "a heavy style rules its document's tables heavier");
        assertTrue(HouseProseSheet.INSTANCE.css(slug("sketchy")).contains(".house-prose___header-band__shape-border {\n    border-width: 0 0 2px 0;"),
                "a style that rules its tables' heads rules this one's");
    }

    @Test
    void theCrateHoldsTheDocumentTable_besideTheGrid() {
        assertTrue(RelGridHouseCrate.INSTANCE.entries().stream().anyMatch(e -> e.module() == HouseProseModule.INSTANCE));
        assertTrue(RelGridHouseCrate.INSTANCE.entries().stream().anyMatch(e -> e.module() == HouseProseSheet.INSTANCE));
    }

    /** A style in its own palette, by the theme's slug. */
    private static String slug(String style) {
        for (var e : HouseStyles.THEMES.entrySet()) if (e.getValue().style().token().equals(style)) return e.getKey();
        throw new IllegalArgumentException(style);
    }
}
