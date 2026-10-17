package hue.captains.singapura.js.homing.relgrid.house;

import hue.captains.singapura.js.homing.conformance.rules.CrateDependencyRule;
import hue.captains.singapura.js.homing.conformance.rules.OrphanCheck;
import hue.captains.singapura.js.homing.core.DerivedCss;
import hue.captains.singapura.js.homing.design.semantic.DesignStyle;
import hue.captains.singapura.js.homing.design.semantic.LocalStyleLeaves;
import hue.captains.singapura.js.homing.design.semantic.LocalStyleRead;
import hue.captains.singapura.js.homing.design.styling.InstalledStyling;
import hue.captains.singapura.js.homing.relgrid.frame.GridFrame;
import hue.captains.singapura.js.homing.relgrid.frame.GridFrameComponentModule;
import hue.captains.singapura.js.homing.ssjs.test.JsModuleTestBase;
import hue.captains.singapura.js.homing.ui.styles.HouseStyles;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The house's local style of the grid's frame: complete over the branch in every house style, a sheet of its own in each
 * - the same in every palette - and an object that wears a leaf of the frame on an element.
 */
class HouseGridTest extends JsModuleTestBase {

    /** The house's styling, installed as a site installs it at bootstrap: the frame's local styles are drawn in its styles. */
    @BeforeAll
    static void theHousesStyling_installed() { InstalledStyling.install(HouseStyles.INSTANCE.styling(List.of(GridFrameBaseModule.INSTANCE))); }

    @Test
    void theHouseGrid_isCompleteOverTheFrame_inEveryHouseStyle() {
        LocalStyleRead grid = InstalledStyling.get().localStyle(HouseGrid.INSTANCE);   // drawn in every house style as the styling was made
        assertEquals(GridFrame.INSTANCE, grid.branch());
        assertEquals(GridFrameComponentModule.INSTANCE.components(), grid.concretes().stream().map(c -> c.virtual()).toList(),
                "a concrete leaf for every leaf of the frame, in the frame's order");
        assertEquals("house-grid", grid.token());
    }

    @Test
    void itsSheet_isFlattenedInEveryHouseStyle_ruledAsTheStyleRules() {
        for (String slug : styles()) {
            String css = HouseGridSheet.INSTANCE.css(slug);
            assertTrue(css.contains(".house-grid___lattice__shape-border {"), slug + " rules no lattice:\n" + css);
            assertTrue(css.contains(".house-grid___grid-edge__color-outline {"), slug);
        }
        String editorial = HouseGridSheet.INSTANCE.css("editorial");
        assertTrue(editorial.contains(".house-grid___lattice__shape-border {\n    border-width: 0 1px 1px 0; border-style: solid;\n}"), editorial);
        assertTrue(editorial.contains("&[data-keys=\"held\"] {\n        outline-color: "), "the edge lights while the keys are with the grid:\n" + editorial);
        assertTrue(HouseGridSheet.INSTANCE.css("neo-brutalism").contains("border-width: 0 2px 2px 0;"), "a heavy style rules heavy");
        assertFalse(editorial.contains(".house-grid___grid-edge__shape-border {"), "the edge draws no border: its line is a ring inside the box");
    }

    @Test
    void itsSheet_variesWithTheStyleAlone_itsPaletteColoursThePalettesVariables() {
        assertEquals(DerivedCss.VariesWith.STYLE, HouseGridSheet.INSTANCE.variesWith(), "a switch of palette repaints it; only a switch of style fetches it again");
        assertEquals(InstalledStyling.get().defaultStyle().token(), HouseGridSheet.INSTANCE.fallback().orElseThrow());
        String editorial = HouseGridSheet.INSTANCE.css("editorial");
        assertTrue(editorial.contains(".house-grid___copied-border__color-stroke {\n    stroke: var(--fp-accent);\n}"),
                "the ants in the accent, as the palette worn defines it:\n" + editorial);
        for (String style : styles())
            assertTrue(InstalledStyling.get().paletteSheet(InstalledStyling.get().own(InstalledStyling.get().style(style).orElseThrow()).token()).contains("--fp-accent:"),
                    style + "'s own palette defines the accent");
    }

    @Test
    void whatWasCopied_marchesInEveryStyle_andSweepsInTheStylesOfLightAndMotion() {
        for (String slug : styles()) {
            String css = HouseGridSheet.INSTANCE.css(slug);
            assertTrue(css.contains("@keyframes effect-marching-ants {"), slug + " keyframes no ants:\n" + css);
            assertTrue(css.indexOf("@keyframes effect-marching-ants {") < css.indexOf(".house-grid___"), slug + ": the keyframes ahead of the classes");
            assertTrue(css.contains(".house-grid___copied-border__animation-effect {\n    stroke-width: 1.5px;\n    stroke-dasharray: 5 4;\n"
                    + "    animation: effect-marching-ants 0.6s linear infinite;"), slug + ":\n" + css);
            assertTrue(css.contains(".house-grid___copied-border__animation-effect-still {\n    stroke-width: 1.5px;\n    stroke-dasharray: 5 4;\n    animation: none;\n}"),
                    slug + ": the ants' still dual, the dashes standing");
            assertTrue(css.contains("prefers-reduced-motion: reduce"), slug + ": still where motion is reduced");
            assertTrue(css.contains(".house-grid___copied-border__color-stroke {\n    stroke: "), slug + ": in the accent");
        }
        String futurism = HouseGridSheet.INSTANCE.css("neo-futurism"), editorial = HouseGridSheet.INSTANCE.css("editorial");
        assertTrue(futurism.contains("animation: effect-moving-gradient 1.6s linear infinite;"), "a band sweeps across what was copied:\n" + futurism);
        assertTrue(futurism.contains("@keyframes effect-moving-gradient {"), futurism);
        assertFalse(editorial.contains("effect-moving-gradient"), "editorial's ants march alone:\n" + editorial);
        assertTrue(editorial.contains(".house-grid___copied-area__animation-effect {\n    animation: none;\n}"), editorial);
    }

    @Test
    void itsObject_wearsALeafOfTheFrame_andRefusesAnotherBranchs() {
        js = buildContext();
        js.eval("js", "var house_grid_sheet = 'the sheet';"
                + " var css = { cls: function (n) { return { name: n }; },"
                + " addClass: function (el, c) { if (!c || typeof c.name !== 'string') throw new Error('not a handle: ' + c); el.classList.add(c.name); },"
                + " removeClass: function (el, c) { if (!c || typeof c.name !== 'string') throw new Error('not a handle: ' + c); el.classList.remove(c.name); } };"
                + " function El() { var s = new Set(); this.classList = { add: function (c) { s.add(c); }, remove: function (c) { s.delete(c); } };"
                + " this.classes = function () { return Array.from(s).sort().join(' '); }; }");
        js.eval("js", String.join("\n", LocalStyleLeaves.INSTANCE.lines(InstalledStyling.get().taxonomy(), GridFrame.INSTANCE)));
        js.eval("js", String.join("\n", HouseGridModule.INSTANCE.selfContent(null)));
        assertEquals("grid-frame house-grid", js.eval("js", "HouseGrid.branch + ' ' + HouseGrid.name").asString());
        assertEquals("house-grid___lattice__color-border house-grid___lattice__shape-border",
                js.eval("js", "var el = new El(); HouseGrid.wear(GRID_FRAME.LATTICE, el); el.classes()").asString());
        assertEquals("house-grid___held-cursor-border__color-outline house-grid___held-cursor-border__shape-outline",
                js.eval("js", "var c = new El(); HouseGrid.wear(GRID_FRAME.HELD_CURSOR_BORDER, c); c.classes()").asString());
        assertTrue(js.eval("js", "(function () { try { HouseGrid.wear({ branch: 'tree-frame', token: 'lattice' }, new El()); return ''; } catch (e) { return e.message; } })()")
                .asString().contains("a leaf of grid-frame is asked"));
        assertEquals("house-grid___copied-border__animation-effect-still house-grid___copied-border__color-stroke",
                js.eval("js", "var b = new El(); HouseGrid.wear(GRID_FRAME.COPIED_BORDER, b); HouseGrid.still(GRID_FRAME.COPIED_BORDER, b); b.classes()").asString(),
                "the ants switched off: their class swapped for their still dual's");
    }

    @Test
    void theCrateHoldsEveryServedModule_andImportsOnlyWhatItHolds() {
        assertEquals(List.of(), OrphanCheck.check(RelGridHouseCrate.INSTANCE));
        assertEquals(List.of(), CrateDependencyRule.check(RelGridHouseCrate.INSTANCE));
    }

    /** Every style the house serves, by its token: a local style's sheet is one a style, the same in every palette. */
    private static List<String> styles() { return InstalledStyling.get().styles().stream().map(DesignStyle::token).toList(); }
}
