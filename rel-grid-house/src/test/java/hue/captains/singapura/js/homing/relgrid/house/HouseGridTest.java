package hue.captains.singapura.js.homing.relgrid.house;

import hue.captains.singapura.js.homing.conformance.rules.CrateDependencyRule;
import hue.captains.singapura.js.homing.conformance.rules.OrphanCheck;
import hue.captains.singapura.js.homing.design.semantic.DesignLeaves;
import hue.captains.singapura.js.homing.design.semantic.DesignRead;
import hue.captains.singapura.js.homing.relgrid.frame.GridFrame;
import hue.captains.singapura.js.homing.relgrid.frame.GridFrameExtension;
import hue.captains.singapura.js.homing.ssjs.test.JsModuleTestBase;
import hue.captains.singapura.js.homing.ui.look.HouseDesigns;
import hue.captains.singapura.js.homing.ui.styles.HouseStyles;
import hue.captains.singapura.js.homing.ui.taxonomy.HouseTaxonomy;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The house's design of the grid's frame: complete over the branch in every house theme, a sheet of its own in each,
 * and an object that wears a leaf of the frame on an element.
 */
class HouseGridTest extends JsModuleTestBase {

    @Test
    void theHouseGrid_isCompleteOverTheFrame_inEveryHouseTheme() {
        DesignRead grid = HouseDesigns.INSTANCE.of(HouseGrid.INSTANCE);   // read and judged in every house theme
        assertEquals(GridFrame.INSTANCE, grid.branch());
        assertEquals(GridFrameExtension.INSTANCE.components(), grid.concretes().stream().map(c -> c.virtual()).toList(),
                "a concrete leaf for every leaf of the frame, in the frame's order");
        assertEquals("house-grid", grid.token());
    }

    @Test
    void itsSheet_isFlattenedInEveryHouseTheme_ruledAsTheStyleRules() {
        for (String slug : HouseStyles.THEMES.keySet()) {
            String css = HouseGridSheet.INSTANCE.css(slug);
            assertTrue(css.contains(".house-grid___lattice__shape-border {"), slug + " rules no lattice:\n" + css);
            assertTrue(css.contains(".house-grid___grid-edge__color-outline {"), slug);
        }
        String editorial = HouseGridSheet.INSTANCE.css(slug("editorial"));
        assertTrue(editorial.contains(".house-grid___lattice__shape-border {\n    border-width: 0 1px 1px 0; border-style: solid;\n}"), editorial);
        assertTrue(editorial.contains("&[data-keys=\"held\"] {\n        outline-color: "), "the edge lights while the keys are with the grid:\n" + editorial);
        assertTrue(HouseGridSheet.INSTANCE.css(slug("neo-brutalism")).contains("border-width: 0 2px 2px 0;"), "a heavy style rules heavy");
        assertFalse(editorial.contains(".house-grid___grid-edge__shape-border {"), "the edge draws no border: its line is a ring inside the box");
    }

    @Test
    void itsObject_wearsALeafOfTheFrame_andRefusesAnotherBranchs() {
        js = buildContext();
        js.eval("js", "var house_grid_sheet = 'the sheet';"
                + " function El() { var s = new Set(); this.classList = { add: function (c) { s.add(c); }, remove: function (c) { s.delete(c); } };"
                + " this.classes = function () { return Array.from(s).sort().join(' '); }; }");
        js.eval("js", String.join("\n", DesignLeaves.INSTANCE.lines(HouseTaxonomy.INSTANCE.read(), GridFrame.INSTANCE)));
        js.eval("js", String.join("\n", HouseGridModule.INSTANCE.selfContent(null)));
        assertEquals("grid-frame house-grid", js.eval("js", "HouseGrid.branch + ' ' + HouseGrid.name").asString());
        assertEquals("house-grid___lattice__color-border house-grid___lattice__shape-border",
                js.eval("js", "var el = new El(); HouseGrid.wear(GRID_FRAME.LATTICE, el); el.classes()").asString());
        assertEquals("house-grid___held-cursor-border__color-outline house-grid___held-cursor-border__shape-outline",
                js.eval("js", "var c = new El(); HouseGrid.wear(GRID_FRAME.HELD_CURSOR_BORDER, c); c.classes()").asString());
        assertTrue(js.eval("js", "(function () { try { HouseGrid.wear({ branch: 'tree-frame', token: 'lattice' }, new El()); return ''; } catch (e) { return e.message; } })()")
                .asString().contains("a leaf of grid-frame is asked"));
    }

    @Test
    void theCrateHoldsEveryServedModule_andImportsOnlyWhatItHolds() {
        assertEquals(List.of(), OrphanCheck.check(RelGridHouseCrate.INSTANCE));
        assertEquals(List.of(), CrateDependencyRule.check(RelGridHouseCrate.INSTANCE));
    }

    /** A style in its own palette, by the theme's slug. */
    private static String slug(String style) {
        for (var e : HouseStyles.THEMES.entrySet()) if (e.getValue().style().token().equals(style)) return e.getKey();
        throw new IllegalArgumentException(style);
    }
}
