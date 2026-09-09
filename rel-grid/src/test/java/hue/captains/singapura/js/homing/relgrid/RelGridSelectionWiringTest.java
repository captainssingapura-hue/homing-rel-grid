package hue.captains.singapura.js.homing.relgrid;

import hue.captains.singapura.js.homing.ssjs.test.JsModuleTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * RFC 0050 · Episode 2, map 5 — the selection WIRED: the gestures the grid
 * routes, the predicate the layout paints, and the two places the cursor and
 * the list meet.
 *
 * <p>The algebra itself is proved next door, in a module that imports nothing.
 * What is proved here is everything the algebra cannot know about: that a bare
 * move clears and a modified one does not, that extension leaves the cursor
 * where it is, that an arrangement takes the list with it, that the paint
 * matches the resolved list — and that <b>nothing consumes a selection yet</b>,
 * which is this round's boundary rather than an omission.</p>
 *
 * <p>The fixture is 3 rows × 2 columns: mapo/coq/fish × ingredient/calories.</p>
 */
class RelGridSelectionWiringTest extends JsModuleTestBase {

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
    void withNoListTheSelectionIsTheCursorsOwnOneByOne() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture();
                    if (f.grid.selectionCount() !== 0) return false;        // nothing was MADE
                    var r = f.grid.selectedRanges();                        // but something is selected
                    if (!(r.length === 1 && r[0].i0 === 0 && r[0].j0 === 0 && r[0].i1 === 0 && r[0].j1 === 0)) return false;
                    if (f.painted() !== '0,0') return false;                // and it is painted
                    f.click(1, 1);
                    return f.grid.selectionCount() === 0 && f.painted() === '1,1'
                        && f.grid.selectedRanges()[0].i0 === 1;             // it follows the cursor
                })()"""), "law 40: an empty list means the cursor's 1x1, and that is what is painted");
    }

    @Test
    void shiftClickExtendsAndDoesNotMoveTheCursor() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture();
                    f.click(0, 0);
                    var before = f.moves.length;
                    f.click(2, 1, { shift: true });
                    var c = f.grid.cursor();
                    if (!(c.pk === 'mapo' && c.column === 'ingredient')) return false;   // law 39: it stayed
                    if (f.moves.length !== before) return false;                          // and did not report
                    if (f.grid.selectionCount() !== 1) return false;
                    var r = f.grid.selectedRanges()[0];
                    if (!(r.i0 === 0 && r.j0 === 0 && r.i1 === 2 && r.j1 === 1)) return false;
                    if (f.painted() !== '0,0 0,1 1,0 1,1 2,0 2,1') return false;
                    // A second shift-click moves the SAME range's far corner — it does not add one.
                    f.click(1, 0, { shift: true });
                    return f.grid.selectionCount() === 1 && f.painted() === '0,0 1,0'
                        && f.grid.cursor().pk === 'mapo';
                })()"""), "shift extends the last range's far corner; the cursor never moves");
    }

    @Test
    void ctrlClickAddsARangeAndGoesThere() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture();
                    f.click(0, 0);
                    f.click(2, 1, { ctrl: true });
                    var c = f.grid.cursor();
                    if (!(c.pk === 'fish' && c.column === 'calories')) return false;   // ctrl DOES move it
                    if (f.grid.selectionCount() !== 1) return false;
                    if (f.painted() !== '2,1') return false;
                    // Another ctrl-click appends rather than replacing, and shift then extends
                    // the one just added.
                    f.click(0, 1, { ctrl: true });
                    if (f.grid.selectionCount() !== 2 || f.painted() !== '0,1 2,1') return false;
                    f.click(1, 1, { shift: true });
                    return f.grid.selectionCount() === 2 && f.painted() === '0,1 1,1 2,1'
                        && f.grid.cursor().pk === 'mapo';                   // the ctrl-click's cursor stayed put
                })()"""), "ctrl appends a 1x1 and moves the cursor there; shift then extends that range");
    }

    @Test
    void aBareMoveClearsTheListAndAModifiedOneDoesNot() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture();
                    f.click(0, 0);
                    f.click(2, 1, { shift: true });
                    if (f.grid.selectionCount() !== 1) return false;
                    f.click(1, 0);                                          // a bare click: start over
                    if (!(f.grid.selectionCount() === 0 && f.painted() === '1,0')) return false;
                    f.click(2, 1, { shift: true });
                    f.key('ArrowUp');                                       // a bare arrow: likewise
                    if (f.grid.selectionCount() !== 0) return false;
                    if (f.grid.cursor().pk !== 'mapo') return false;
                    // And the programmatic cursor is a bare move too.
                    f.grid.extendSelection('fish', 'calories');
                    if (f.grid.selectionCount() !== 1) return false;
                    f.grid.selectCell('coq', 'calories');
                    return f.grid.selectionCount() === 0 && f.painted() === '1,1';
                })()"""), "law 40: a bare cursor move clears the list, whether by click, key or API");
    }

    @Test
    void shiftArrowsWalkTheFarCornerAndClampAtTheEdges() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture();
                    f.click(1, 0);                                          // cursor at coq/ingredient
                    f.key('ArrowDown', { shift: true });
                    if (f.painted() !== '1,0 2,0') return false;
                    f.key('ArrowRight', { shift: true });
                    if (f.painted() !== '1,0 1,1 2,0 2,1') return false;
                    f.key('ArrowDown', { shift: true });                    // clamped at the last row
                    if (f.painted() !== '1,0 1,1 2,0 2,1') return false;
                    // Walking back past the anchor keeps the anchor and flips the rectangle.
                    f.key('ArrowUp', { shift: true }); f.key('ArrowUp', { shift: true });
                    if (f.painted() !== '0,0 0,1 1,0 1,1') return false;
                    return f.grid.cursor().pk === 'coq' && f.grid.selectionCount() === 1;
                })()"""), "shift+arrow steps the corner extension moves, clamped, with the cursor unmoved");
    }

    @Test
    void ctrlAndATakeTheWholePresentedSpace() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture();
                    f.click(1, 1);
                    f.key('a', { ctrl: true });
                    if (f.grid.selectionCount() !== 1) return false;
                    if (f.painted() !== '0,0 0,1 1,0 1,1 2,0 2,1') return false;
                    if (f.grid.cursor().pk !== 'coq') return false;         // the cursor is not touched
                    var r = f.grid.selectedRanges()[0];
                    return r.i0 === 0 && r.j0 === 0 && r.i1 === 2 && r.j1 === 1;
                })()"""), "Ctrl+A is one range over the presented space, and moves nothing");
    }

    @Test
    void overlappingRangesArePaintedOnceAndKeptAsTwo() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture();
                    f.click(0, 0); f.click(1, 1, { shift: true });          // a block
                    f.click(1, 0, { ctrl: true }); f.click(2, 1, { shift: true });  // overlapping it
                    if (f.grid.selectionCount() !== 2) return false;         // never merged
                    // A slot covered twice wears the predicate once — the layout resolves nothing.
                    if (f.painted() !== '0,0 0,1 1,0 1,1 2,0 2,1') return false;
                    var r = f.grid.selectedRanges();
                    return r.length === 2 && r[0].i1 === 1 && r[1].i1 === 2;
                })()"""), "law 42: overlap is kept in the list and painted once on the slot");
    }

    @Test
    void anArrangementTakesTheListWithIt() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture();
                    f.click(0, 0); f.key('a', { ctrl: true });
                    if (f.grid.selectionCount() !== 1) return false;
                    f.grid.reapply();                                       // the slots are rebuilt
                    if (f.grid.selectionCount() !== 0) return false;         // law 43
                    if (f.painted() !== '0,0') return false;                 // back to the cursor's 1x1
                    // The CURSOR survives, because it is an identity and the list is not.
                    return f.grid.cursor().pk === 'mapo' && f.grid.cursor().column === 'ingredient';
                })()"""), "law 43: the presented space was rebuilt, so every range goes; the cursor stays");
    }

    @Test
    void aDoubleClickIsABareMoveOfItsOwn() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture({ editable: true });
                    f.click(0, 0); f.click(2, 1, { shift: true });
                    if (f.grid.selectionCount() !== 1) return false;
                    // Dispatched alone, as a test may: it must clear on its own account and
                    // not lean on the click a browser would have sent first.
                    f.td(1, 0).dispatch('dblclick');
                    return f.grid.selectionCount() === 0 && f.painted() === '1,0'
                        && f.grid.isDeep() && f.grid.cursor().pk === 'coq';
                })()"""), "a double-click clears the list itself rather than relying on the click before it");
    }

    @Test
    void everySelectionGestureIsInertWhileDeep() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture({ editable: true });
                    f.click(1, 0); f.key('a', { ctrl: true });
                    var before = f.painted();
                    f.key('Enter');
                    if (!f.grid.isDeep()) return false;
                    // Blocked, not deferred: none of these does anything at all.
                    f.click(2, 1, { shift: true });
                    f.click(0, 1, { ctrl: true });
                    f.key('ArrowDown', { shift: true });
                    f.key('a', { ctrl: true });
                    f.key('ArrowUp');
                    if (f.grid.extendSelection('fish', 'calories') !== false) return false;
                    if (f.grid.addToSelection('fish', 'calories') !== false) return false;
                    if (f.grid.selectAll() !== false) return false;
                    return f.grid.selectionCount() === 1 && f.painted() === before
                        && f.grid.cursor().pk === 'coq' && f.grid.isDeep();
                })()"""), "selection intents are BLOCKED while a cell is deep, not queued");
    }

    @Test
    void theSelectionIsReportedAndNothingConsumesIt() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture();
                    var seen = f.selections.length;                          // the first arrangement reported
                    if (seen === 0) return false;
                    f.click(0, 0); f.click(2, 1, { shift: true });
                    if (f.selections[f.selections.length - 1] !== 1) return false;
                    f.click(0, 1, { ctrl: true });
                    if (f.selections[f.selections.length - 1] !== 2) return false;
                    f.grid.clearSelection();
                    if (f.selections[f.selections.length - 1] !== 1) return false;   // the cursor's own
                    // THE BOUNDARY OF THIS ROUND: the grid has no verb that reads a selection.
                    // Copy, clear and bulk arrive later; their absence is deliberate.
                    var verbs = ['copy', 'copySelection', 'clearCells', 'deleteRows', 'bulkEdit', 'fill'];
                    for (var k = 0; k < verbs.length; k++) if (typeof f.grid[verbs[k]] === 'function') return false;
                    return true;
                })()"""), "the resolved selection is reported; no verb on the grid consumes one yet");
    }
}
