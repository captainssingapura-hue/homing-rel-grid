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
        js.eval("js", RelGridTestDom.STYLES);
        for (String m : RelGridTestDom.PARTY) loadModule(m);
        loadModule(RelGridTestDom.SELECTION);
        loadModule(RelGridTestDom.PROTOCOL);
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
    void aPressDragMakesARangeAndTheCursorStaysWhereItStarted() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture();
                    f.click(2, 1);                                          // somewhere else first
                    f.drag([0, 0], [[1, 0], [2, 1]]);
                    if (f.grid.selectionCount() !== 1) return false;
                    if (f.painted() !== '0,0 0,1 1,0 1,1 2,0 2,1') return false;
                    // The press was a BARE one, so it cleared and moved the cursor to where
                    // the drag began — and the drag itself never moved it again.
                    var c = f.grid.cursor();
                    return c.pk === 'mapo' && c.column === 'ingredient'
                        && f.moves.join() === 'fish calories,mapo ingredient';
                })()"""), "a bare press-drag clears, anchors at the press, and extends as the pointer travels");
    }

    @Test
    void aDragThatComesBackShrinksToWhereItStarted() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture();
                    f.drag([1, 1], [[2, 1], [2, 0], [1, 1]]);               // out, across, and back
                    if (f.grid.selectionCount() !== 1) return false;
                    if (f.painted() !== '1,1') return false;                // a 1x1 again
                    return f.grid.cursor().pk === 'coq' && f.grid.cursor().column === 'calories';
                })()"""), "returning to the press's own slot reports too, so a drag can shrink back to 1x1");
    }

    @Test
    void aModifiedPressDragMeansWhatTheModifiedClickMeans() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture();
                    f.click(0, 0);
                    // ctrl: the press appends a 1x1 and goes there, then the drag grows THAT range.
                    f.drag([2, 0], [[2, 1]], { ctrl: true });
                    if (f.grid.selectionCount() !== 1) return false;
                    if (f.painted() !== '2,0 2,1') return false;
                    if (f.grid.cursor().pk !== 'fish') return false;         // ctrl moved it
                    // shift: the press adds nothing; the drag extends from the cursor, which stays.
                    f.click(0, 0);
                    f.drag([1, 1], [[2, 1]], { shift: true });
                    if (f.grid.selectionCount() !== 1) return false;
                    if (f.painted() !== '0,0 0,1 1,0 1,1 2,0 2,1') return false;
                    return f.grid.cursor().pk === 'mapo';
                })()"""), "ctrl and shift mean the same during a drag as they do on a click");
    }

    @Test
    void theClickThatFollowsADragDoesNotUndoIt() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture();
                    // A ctrl press-drag that ends in the slot it began: the browser sends a
                    // click too, and handling it would append a SECOND range.
                    f.drag([1, 0], [[2, 0], [1, 0]], { ctrl: true });
                    if (f.grid.selectionCount() !== 1) return false;
                    if (f.painted() !== '1,0') return false;
                    // And the swallow does not outlive its gesture: the very next plain click
                    // works normally, even though the drag ended off the table.
                    f.drag([0, 0], [[1, 1]]);
                    f.click(2, 1);
                    return f.grid.selectionCount() === 0 && f.painted() === '2,1'
                        && f.grid.cursor().pk === 'fish';
                })()"""), "a drag swallows exactly one following click, and never a later one");
    }

    @Test
    void aPressThatNeverTravelsIsJustAClick() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture();
                    f.click(0, 0); f.click(2, 1, { shift: true });
                    var before = f.painted();
                    // Down and up in one slot, with a mousemove inside it: no drag, and the
                    // click is NOT swallowed — it is the whole gesture.
                    f.td(1, 0).dispatch('mousedown', {});
                    f.td(1, 0).dispatch('mousemove', {});
                    document.dispatch('mouseup', {});
                    if (f.painted() !== before) return false;               // nothing yet
                    f.click(1, 0);
                    return f.grid.selectionCount() === 0 && f.painted() === '1,0';
                })()"""), "a press that never leaves its slot changes nothing until the click arrives");
    }

    @Test
    void aDragIsInertWhileDeep() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture({ editable: true });
                    f.click(1, 0);
                    f.key('Enter');
                    if (!f.grid.isDeep()) return false;
                    var before = f.painted();
                    f.drag([0, 0], [[2, 1]]);
                    return f.grid.isDeep() && f.painted() === before && f.grid.selectionCount() === 0
                        && f.grid.cursor().pk === 'coq';
                })()"""), "a press-drag is blocked while a cell is deep, like every other selection gesture");
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
    void theSelectionIsToldThroughTheChannelAndOnlyCopyConsumesIt() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture();
                    if (f.sent.length === 0) return false;                  // the first arrangement told
                    if (!(f.sent[0] instanceof RelGridSelectionChanged)) return false;
                    if (f.told() !== '0,0..0,0') return false;               // the cursor's own 1x1
                    f.click(0, 0); f.click(2, 1, { shift: true });
                    if (f.told() !== '0,0..2,1') return false;
                    f.click(0, 1, { ctrl: true });
                    if (f.told() !== '0,0..2,1 0,1..0,1') return false;      // both, in creation order
                    f.grid.clearSelection();
                    if (f.told() !== '0,1..0,1') return false;               // back to the cursor's own
                    // THE BOUNDARY OF THIS ROUND: copy is the ONE verb that reads a selection.
                    // Clear and bulk arrive later; their absence is deliberate.
                    if (typeof f.grid.copy !== 'function') return false;
                    var verbs = ['copySelection', 'clearCells', 'deleteRows', 'bulkEdit', 'fill'];
                    for (var k = 0; k < verbs.length; k++) if (typeof f.grid[verbs[k]] === 'function') return false;
                    return true;
                })()"""), "the resolved selection is told through the channel; copy is the one verb that reads it");
    }

    @Test
    void whatTravelsIsAGeneratedValueObjectTheGridCannotTakeBack() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture();
                    f.click(0, 0); f.click(1, 1, { shift: true });
                    var q = f.sent[f.sent.length - 1];
                    if (!(q instanceof RelGridSelectionChanged)) return false;
                    if (!(q.ranges[0] instanceof RelGridRange)) return false;
                    // Deeply frozen, so the domain cannot be handed something that changes
                    // under it, and cannot change what the grid still holds.
                    if (!Object.isFrozen(q) || !Object.isFrozen(q.ranges) || !Object.isFrozen(q.ranges[0])) return false;
                    try { q.ranges[0].i1 = 99; } catch (e) {}
                    return q.ranges[0].i1 === 1 && q.ranges.length === 1;
                })()"""), "the payload is a frozen value object, generated from the Java record");
    }

    @Test
    void theGridNeitherWaitsForANotificationNorLosesItsFailure() {
        assertTrue(evalBool("""
                (() => {
                    var settled = [];
                    // A domain that answers LATE, and one that fails. Neither may stop the grid,
                    // and neither may make it wait: the paint below happens before either runs.
                    var f = fixture({ ask: function () {
                        return new Promise(function (res) { settled.push('later'); res(); });
                    } });
                    f.click(1, 1);
                    if (f.painted() !== '1,1') return false;                 // painted, not pending
                    if (f.grid.isDeep()) return false;

                    var g = fixture({ ask: function () { return Promise.reject(new Error('domain said no')); } });
                    g.click(0, 1);                                            // a rejected notification
                    if (g.painted() !== '0,1') return false;                  // the grid carried on

                    var h = fixture({ ask: function () { throw new Error('domain threw'); } });
                    h.click(1, 0);                                            // and a thrown one
                    return h.painted() === '1,0' && h.grid.cursor().pk === 'coq';
                })()"""), "a notification is fire-and-forget: not waited on, and never able to stop the grid");
    }

    @Test
    void aHostWithNoChannelIsUnchanged() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture({ noAsk: true });
                    f.click(0, 0); f.click(2, 1, { shift: true });
                    // Everything still works; there is simply nobody to tell.
                    return f.sent.length === 0 && f.painted() === '0,0 0,1 1,0 1,1 2,0 2,1'
                        && f.grid.selectionCount() === 1;
                })()"""), "the channel is optional: without an ask the grid behaves exactly as before");
    }
}
