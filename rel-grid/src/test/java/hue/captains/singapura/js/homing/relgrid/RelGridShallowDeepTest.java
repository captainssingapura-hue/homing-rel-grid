package hue.captains.singapura.js.homing.relgrid;

import hue.captains.singapura.js.homing.ssjs.test.JsModuleTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * RFC 0050 · Episode 2, round 1 — the shallow/deep discipline, enforced by the
 * grid from the beginning.
 *
 * <p>A single click or an arrow key is shallow: it moves the cursor and the
 * cell is only told. Deep is entered only through the grid — Enter or a
 * double-click on the cursor's cell — which hands the cell control and goes
 * inert until the cell hands it back. The grid never learns what happened
 * inside. And no focused node is ever removed with a live listener on it.
 * Runs on {@link RelGridTestDom}, whose focus and blur behave like a browser's.</p>
 */
class RelGridShallowDeepTest extends JsModuleTestBase {

    @BeforeEach
    void setup() {
        js = buildContext();
        js.eval("js", RelGridTestDom.DOM_STUB);
        loadModule(RelGridTestDom.SELECTION);
        for (String m : RelGridTestDom.MODULES) loadModule(RelGridTestDom.DIR + m);
        js.eval("js", RelGridTestDom.FIXTURE);
    }

    private boolean evalBool(String expr) { return js.eval("js", expr).asBoolean(); }

    @Test
    void theCursorStartsOnTheFirstCellAndASingleClickOnlyMovesIt() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture({ editable: true });
                    var c0 = f.grid.cursor();
                    if (!(c0 && c0.pk === 'mapo' && c0.column === 'ingredient')) return false;
                    f.td(1, 0).dispatch('click');                          // a SHALLOW gesture
                    var c1 = f.grid.cursor();
                    return c1.pk === 'coq' && c1.column === 'ingredient'
                        && f.moves.join() === 'coq ingredient'
                        && f.relation.cell('coq', 'ingredient').mode() === 'shallow'
                        && f.relation.cell('mapo', 'ingredient').mode() === 'none'
                        && /hrg-cursor/.test(f.td(1, 0).className)        // painted on the SLOT
                        && !/hrg-cursor/.test(f.td(0, 0).className)
                        && f.td(1, 0).children[0].children.length === 0    // no input opened
                        && !/hrg-text-ro/.test(f.td(1, 0).children[0].className)   // an editable cell is not marked
                        && !f.grid.isDeep() && f.started.length === 0;
                })()"""), "a single click is shallow: the cursor moves, the cell is told, nothing opens");
    }

    @Test
    void arrowKeysMoveTheCursorWhileShallowAndClampAtTheEdges() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture();
                    f.key('ArrowDown'); f.key('ArrowRight');
                    var a = f.grid.cursor();
                    f.key('ArrowRight'); f.key('ArrowDown'); f.key('ArrowDown'); f.key('ArrowDown');
                    var b = f.grid.cursor();
                    f.key('ArrowUp'); f.key('ArrowUp'); f.key('ArrowUp'); f.key('ArrowLeft'); f.key('ArrowLeft');
                    var c = f.grid.cursor();
                    return a.pk === 'coq'  && a.column === 'calories'
                        && b.pk === 'fish' && b.column === 'calories'      // clamped at the corner
                        && c.pk === 'mapo' && c.column === 'ingredient';
                })()"""), "arrows are shallow moves over the presented space, clamped, never wrapping");
    }

    @Test
    void enterGoesDeepHandsTheCellTheKeyboardAndMakesCaptureInert() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture({ editable: true });
                    f.key('Enter');                                        // ONLY the grid opens deep
                    var cell = f.relation.cell('mapo', 'ingredient');
                    var input = f.td(0, 0).children[0].children[0];
                    if (!(f.grid.isDeep() && input && input.tagName === 'input')) return false;
                    if (document.activeElement !== input) return false;     // the cell took the keyboard
                    if (cell.mode() !== 'deep') return false;
                    if (!/hrg-deep/.test(f.table().className)) return false;
                    if (f.started.join() !== 'mapo ingredient') return false;
                    f.key('ArrowDown');                                    // the grid's capture is INERT
                    f.td(2, 1).dispatch('click');
                    var c = f.grid.cursor();
                    return c.pk === 'mapo' && c.column === 'ingredient'
                        && f.moves.length === 0 && f.grid.isDeep();
                })()"""), "Enter hands control to the cell; while deep the grid ignores its own gestures");
    }

    @Test
    void theCellReleasesOnceAndTheGridResumesWithoutLearningWhatHappened() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture({ editable: true });
                    f.key('Enter');
                    var host = f.td(0, 0).children[0], input = host.children[0];
                    input.value = 'TOFU-EDITED';
                    input.dispatch('keydown', { key: 'Enter' });           // the CELL ends its edit
                    return f.commits.join() === 'mapo ingredient TOFU-EDITED'
                        && !f.grid.isDeep()                                // the grid resumed
                        && f.ended.join() === 'mapo ingredient'            // told it ENDED, and once
                        && document.activeElement === f.table()            // the keyboard host has focus again
                        && f.relation.cell('mapo', 'ingredient').mode() === 'shallow'
                        && host.children.length === 0                      // the input is gone, exactly once
                        && host.textContent === 'TOFU-EDITED'              // shown because the OWNER set() it
                        && !/hrg-deep/.test(f.table().className)
                        && (f.key('ArrowDown'), f.grid.cursor().pk === 'coq');   // and the arrows are back
                })()"""), "release hands control back once; the grid resumes and never learns commit from cancel");
    }

    @Test
    void doubleClickGoesDeepOnTheClickedCell() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture({ editable: true });
                    f.td(2, 1).dispatch('click');
                    f.td(2, 1).dispatch('dblclick');
                    var c = f.grid.cursor();
                    return c.pk === 'fish' && c.column === 'calories' && f.grid.isDeep()
                        && f.started.join() === 'fish calories'
                        && f.td(2, 1).children[0].children[0].tagName === 'input';
                })()"""), "a double-click is the pointer's way into deep, on the cell it landed on");
    }

    @Test
    void aReadOnlyCellDeclinesAndTheGridStaysShallow() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture();                                     // no onCommit anywhere
                    f.key('Enter');
                    f.td(1, 1).dispatch('dblclick');
                    return !f.grid.isDeep() && f.started.length === 0 && f.ended.length === 0
                        && f.td(0, 0).children[0].children.length === 0
                        && f.td(1, 1).children[0].children.length === 0
                        && /hrg-text-ro/.test(f.td(0, 0).children[0].className)   // the cell named the property (law 116)
                        && f.grid.cursor().pk === 'coq';                   // the dblclick still moved the cursor
                })()"""), "a cell without a commit target declines beginEdit; nothing changes hands");
    }

    @Test
    void escapeAndLosingFocusCancelThroughTheCellAndReleaseTheGrid() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture({ editable: true });
                    f.key('Enter');
                    var input = f.td(0, 0).children[0].children[0];
                    input.value = 'never';
                    input.dispatch('keydown', { key: 'Escape' });
                    var afterEscape = !f.grid.isDeep() && f.ended.length === 1
                                   && f.td(0, 0).children[0].textContent === 'tofu';
                    f.key('Enter');                                        // deep again
                    var input2 = f.td(0, 0).children[0].children[0];
                    input2.value = 'never either';
                    f.table().focus();                                     // focus leaves the input — a click elsewhere
                    return afterEscape
                        && !f.grid.isDeep() && f.ended.length === 2
                        && f.td(0, 0).children[0].textContent === 'tofu'
                        && f.commits.length === 0;
                })()"""), "Escape and blur both cancel inside the cell, and both hand control back");
    }

    @Test
    void clickingAnotherCellWhileDeepEndsTheEditFirstThenMovesTheCursor() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture({ editable: true });
                    f.key('Enter');
                    // In a browser, mousedown on another slot moves focus to the table
                    // (the nearest focusable ancestor) BEFORE the click fires.
                    f.table().focus();
                    f.td(2, 0).dispatch('click');
                    var c = f.grid.cursor();
                    return !f.grid.isDeep() && f.ended.length === 1
                        && c.pk === 'fish' && c.column === 'ingredient'
                        && f.moves.join() === 'fish ingredient';
                })()"""), "the cell ends on blur and releases; the click then lands as a shallow move");
    }

    @Test
    void theCursorKeepsItsIdentityAcrossReArrangementAndFallsBackToPositionWhenItLeaves() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture(), maps = f.grid.viewMaps();
                    f.td(1, 1).dispatch('click');                          // coq / calories
                    maps.setRowView(['fish', 'coq', 'mapo']);              // reorder: identity survives
                    var a = f.grid.cursor(), atA = maps.locate('coq', 'calories');
                    if (!(a.pk === 'coq' && /hrg-cursor/.test(f.td(atA.i, atA.j).className))) return false;
                    maps.setRowView(['fish', 'mapo']);                     // coq leaves: fall back to position (1, 1)
                    var b = f.grid.cursor();
                    if (!(b.pk === 'mapo' && b.column === 'calories')) return false;
                    maps.setRowView([]);                                   // nothing presented: no cursor
                    return f.grid.cursor() === null;
                })()"""), "the cursor is an identity while it is presented, a position when it is not, and none when nothing is");
    }
}
