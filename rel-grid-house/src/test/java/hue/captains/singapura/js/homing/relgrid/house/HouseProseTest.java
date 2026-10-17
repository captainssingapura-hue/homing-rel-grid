package hue.captains.singapura.js.homing.relgrid.house;

import hue.captains.singapura.js.homing.design.semantic.DesignStyle;
import hue.captains.singapura.js.homing.design.semantic.LocalStyleRead;
import hue.captains.singapura.js.homing.design.styling.InstalledStyling;
import hue.captains.singapura.js.homing.relgrid.frame.GridFrame;
import hue.captains.singapura.js.homing.relgrid.frame.GridFrameComponentModule;
import hue.captains.singapura.js.homing.ui.styles.HouseStyles;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.List;

/**
 * The house's document table: complete over the grid's frame in every house style, its rules and its head its own -
 * none taken from a cell, a container - measured per style as the house's document tables were, and what a document has
 * no word for the house grid's.
 */
class HouseProseTest {

    /** The house's styling, installed as a site installs it at bootstrap: the frame's local styles are drawn in its styles. */
    @BeforeAll
    static void theHousesStyling_installed() { InstalledStyling.install(HouseStyles.INSTANCE.styling(List.of(GridFrameBaseModule.INSTANCE))); }

    @Test
    void theHouseProse_isCompleteOverTheFrame_inEveryHouseStyle() {
        LocalStyleRead prose = InstalledStyling.get().localStyle(HouseProse.INSTANCE);   // drawn in every house style as the styling was made
        assertEquals(GridFrame.INSTANCE, prose.branch());
        assertEquals(GridFrameComponentModule.INSTANCE.components(), prose.concretes().stream().map(c -> c.virtual()).toList(),
                "a concrete leaf for every leaf of the frame, in the frame's order");
        assertEquals("house-prose", prose.token());
    }

    @Test
    void itsRules_areItsOwn_measuredAsTheHousesDocumentTablesWere_inEveryHouseStyle() {
        for (String slug : styles()) {
            String css = HouseProseSheet.INSTANCE.css(slug);
            assertFalse(css.contains("/* the theme's "), slug + ": nothing taken from a cell - a cell is a container, its room and no line:\n" + css);
            assertTrue(css.contains(".house-prose___lattice__color-border {\n    border-color: "), slug + ": its rows ruled in the hairline");
            assertTrue(css.contains(".house-prose___header-band__shape-border {"), slug);
            assertFalse(css.contains(".house-prose___grid-edge__"), slug + ": a document's table has no edge:\n" + css);
            assertFalse(css.contains(".house-prose___cursor-border__"), slug + ": no cursor while the keys are elsewhere");
            assertTrue(css.contains("/* from house-grid___held-cursor-border__color-outline */\n.house-prose___held-cursor-border__color-outline {"),
                    slug + ": where the keys are is the house grid's");
        }
        String editorial = HouseProseSheet.INSTANCE.css("editorial");
        assertTrue(editorial.contains(".house-prose___lattice__shape-border {\n    border-width: 0 0 1px 0; border-style: solid;\n}"),
                "each row ruled underneath, as the editorial style rules a document's table:\n" + editorial);
        assertTrue(editorial.contains(".house-prose___header-band__shape-border {\n    border-width: 0;\n}"), "and its head unruled:\n" + editorial);
        assertTrue(HouseProseSheet.INSTANCE.css("neo-brutalism").contains(".house-prose___lattice__shape-border {\n    border-width: 0 0 2px 0;"),
                "a heavy style rules its document's tables heavier");
        assertTrue(HouseProseSheet.INSTANCE.css("sketchy").contains(".house-prose___header-band__shape-border {\n    border-width: 0 0 2px 0;"),
                "a style that rules its tables' heads rules this one's");
    }

    @Test
    void theCrateHoldsTheDocumentTable_besideTheGrid() {
        assertTrue(RelGridHouseCrate.INSTANCE.entries().stream().anyMatch(e -> e.module() == HouseProseModule.INSTANCE));
        assertTrue(RelGridHouseCrate.INSTANCE.entries().stream().anyMatch(e -> e.module() == HouseProseSheet.INSTANCE));
    }

    /** Every style the house serves, by its token: a local style's sheet is one a style, the same in every palette. */
    private static List<String> styles() { return InstalledStyling.get().styles().stream().map(DesignStyle::token).toList(); }
}
