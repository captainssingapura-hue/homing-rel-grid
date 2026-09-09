package hue.captains.singapura.js.homing.relgrid.selection;

import hue.captains.singapura.js.homing.ssjs.test.JsModuleTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * RFC 0050 · Episode 2, map 5 — the selection's laws, one test each.
 *
 * <p>There is no DOM stub in this class and no fixture. The module imports
 * nothing and touches nothing, so its whole suite runs on a bare JS context —
 * which is the cheapest possible demonstration of law 215, and the reason the
 * selection is a module of its own.</p>
 */
class RelGridSelectionTest extends JsModuleTestBase {

    private static final String MODULE =
            "/homing/js/hue/captains/singapura/js/homing/relgrid/selection/RelGridSelectionModule.js";

    @BeforeEach
    void setup() {
        js = buildContext();
        loadModule(MODULE);
    }

    private boolean evalBool(String expr) { return js.eval("js", expr).asBoolean(); }

    @Test
    void aSelectionIsAnOrderedListOfRectanglesOwningBothCorners() {
        assertTrue(evalBool("""
                (() => {
                    var s = new RelGridSelection();
                    if (!(s.isEmpty() && s.count() === 0 && s.ranges().length === 0)) return false;
                    s.add({ i: 2, j: 1 });                                 // the smallest range is 1x1
                    var a = s.ranges()[0];
                    if (!(a.i0 === 2 && a.i1 === 2 && a.j0 === 1 && a.j1 === 1)) return false;
                    s.extend({ i: 4, j: 3 });                              // grows, owning both corners
                    var b = s.ranges()[0];
                    return b.i0 === 2 && b.j0 === 1 && b.i1 === 4 && b.j1 === 3
                        && s.count() === 1 && !s.isEmpty();
                })()"""), "a range is a rectangle of positions owning both corners; the smallest is 1x1");
    }

    @Test
    void extensionMovesTheLastRangesFarCornerAndThereIsNoFocus() {
        assertTrue(evalBool("""
                (() => {
                    var s = new RelGridSelection();
                    s.add({ i: 1, j: 1 });                                 // range 0
                    s.add({ i: 5, j: 5 });                                 // range 1 — now the last
                    s.extend({ i: 7, j: 6 });
                    var r = s.ranges();
                    if (r.length !== 2) return false;
                    if (!(r[0].i0 === 1 && r[0].i1 === 1)) return false;    // the earlier range is untouched
                    if (!(r[1].i0 === 5 && r[1].j0 === 5 && r[1].i1 === 7 && r[1].j1 === 6)) return false;
                    // The ANCHOR does not move, however far the far corner is dragged — including
                    // back past it, which normalises on report without disturbing what is held.
                    s.extend({ i: 2, j: 3 });
                    var back = s.ranges()[1];
                    if (!(back.i0 === 2 && back.j0 === 3 && back.i1 === 5 && back.j1 === 5)) return false;
                    s.extend({ i: 9, j: 9 });                              // still anchored at 5,5
                    var fwd = s.ranges()[1];
                    return fwd.i0 === 5 && fwd.j0 === 5 && fwd.i1 === 9 && fwd.j1 === 9;
                })()"""), "extension moves the last range's far corner; the anchor is never disturbed");
    }

    @Test
    void anEmptyListMeansTheCursorsImplicitOneByOne() {
        assertTrue(evalBool("""
                (() => {
                    var s = new RelGridSelection();
                    var r = s.resolve({ i: 3, j: 2 });
                    if (!(r.length === 1 && r[0].i0 === 3 && r[0].i1 === 3 && r[0].j0 === 2 && r[0].j1 === 2)) return false;
                    if (s.ranges().length !== 0) return false;              // the RAW list is still empty
                    if (!s.covers(3, 2, { i: 3, j: 2 })) return false;
                    if (s.covers(3, 1, { i: 3, j: 2 })) return false;
                    // No list and no cursor is the only way to select nothing.
                    if (s.resolve(null).length !== 0) return false;
                    if (s.covers(0, 0, null)) return false;
                    // Once there is a list the cursor stops standing in for it.
                    s.add({ i: 8, j: 8 });
                    var r2 = s.resolve({ i: 3, j: 2 });
                    return r2.length === 1 && r2[0].i0 === 8 && !s.covers(3, 2, { i: 3, j: 2 });
                })()"""), "law 40: an empty list resolves to the cursor's 1x1, so no reader needs a special case");
    }

    @Test
    void rangesMayOverlapAndTheListIsNeverMergedOrDeduplicated() {
        assertTrue(evalBool("""
                (() => {
                    var s = new RelGridSelection();
                    s.add({ i: 0, j: 0 }); s.extend({ i: 4, j: 4 });        // a block
                    s.add({ i: 2, j: 2 }); s.extend({ i: 6, j: 6 });        // overlapping it
                    s.add({ i: 2, j: 2 });                                  // and a duplicate corner
                    var r = s.ranges();
                    if (r.length !== 3 || s.count() !== 3) return false;     // three, in creation order
                    if (!(r[0].i1 === 4 && r[1].i1 === 6 && r[2].i0 === 2 && r[2].i1 === 2)) return false;
                    // The overlapped cell is covered; being covered twice is not the model's concern.
                    return s.covers(3, 3, null) && s.covers(5, 5, null) && !s.covers(7, 7, null);
                })()"""), "law 42: overlap is permitted and preserved; nothing is merged or deduplicated");
    }

    @Test
    void noShapeRuleIsImposedAndNothingIsReordered() {
        assertTrue(evalBool("""
                (() => {
                    var s = new RelGridSelection();
                    s.add({ i: 9, j: 9 });                                  // deliberately out of order
                    s.add({ i: 0, j: 5 }); s.extend({ i: 0, j: 7 });        // a strip
                    s.add({ i: 4, j: 0 });                                  // an irregular, disjoint shape
                    var r = s.ranges();
                    if (r.length !== 3) return false;
                    // Creation order, never sorted, and no bounding box is ever taken: the hole
                    // between the pieces is still a hole.
                    if (!(r[0].i0 === 9 && r[1].i0 === 0 && r[2].i0 === 4)) return false;
                    return !s.covers(5, 5, null) && !s.covers(2, 2, null)
                        && s.covers(9, 9, null) && s.covers(0, 6, null) && s.covers(4, 0, null);
                })()"""), "law 44: no bounding box, no reordering, no refusal of an irregular shape");
    }

    @Test
    void selectAllTakesItsExtentsAndClearEmptiesTheList() {
        assertTrue(evalBool("""
                (() => {
                    var s = new RelGridSelection();
                    s.add({ i: 1, j: 1 }); s.add({ i: 2, j: 2 });
                    s.all(3, 2);                                            // REPLACES the list
                    var r = s.ranges();
                    if (!(r.length === 1 && r[0].i0 === 0 && r[0].j0 === 0 && r[0].i1 === 2 && r[0].j1 === 1)) return false;
                    if (!(s.covers(2, 1, null) && !s.covers(3, 1, null) && !s.covers(2, 2, null))) return false;
                    // An empty presented space selects nothing — not a degenerate rectangle.
                    s.all(0, 5);
                    if (!s.isEmpty()) return false;
                    s.all(4, 4);
                    s.clear();
                    return s.isEmpty() && s.count() === 0 && s.ranges().length === 0;
                })()"""), "select-all is built from the extents it is handed; clear empties the list");
    }

    @Test
    void theFarCornerIsReadableBecauseExtensionHasToStepIt() {
        assertTrue(evalBool("""
                (() => {
                    var s = new RelGridSelection();
                    if (s.far() !== null) return false;                     // nothing to move yet
                    s.add({ i: 3, j: 3 });
                    var f0 = s.far();
                    if (!(f0.i === 3 && f0.j === 3)) return false;
                    s.extend({ i: 1, j: 5 });
                    var f1 = s.far();
                    if (!(f1.i === 1 && f1.j === 5)) return false;          // the far corner, not the anchor
                    s.add({ i: 8, j: 8 });
                    if (s.far().i !== 8) return false;                      // always the LAST range's
                    f1.i = 99;                                              // a reader scribbles
                    return s.far().i === 8;
                })()"""), "far() reports the corner extension moves, copied — a range owns its corners");
    }

    @Test
    void theListIsHandedOutAsCopiesSoNoReaderCanEditIt() {
        assertTrue(evalBool("""
                (() => {
                    var s = new RelGridSelection();
                    s.add({ i: 1, j: 1 }); s.extend({ i: 3, j: 3 });
                    var taken = s.ranges();
                    taken[0].i1 = 99;                                       // a reader scribbles
                    taken.push({ i0: 0, j0: 0, i1: 0, j1: 0 });
                    var again = s.ranges();
                    return again.length === 1 && again[0].i1 === 3;
                })()"""), "what a reader is handed is a copy; the list is the selection's own");
    }

    @Test
    void malformedInputFailsAtTheCallSite() {
        assertTrue(evalBool("""
                (() => {
                    function throws(fn) { try { fn(); return false; } catch (e) { return true; } }
                    var s = new RelGridSelection();
                    return throws(function () { s.add({ i: -1, j: 0 }); })
                        && throws(function () { s.add({ i: 1.5, j: 0 }); })
                        && throws(function () { s.add({ i: 0 }); })
                        && throws(function () { s.add(null); })
                        && throws(function () { s.all(-1, 3); })
                        && throws(function () { s.extend({ i: 1, j: 1 }); })   // empty list, no anchor
                        && s.isEmpty()                                          // and nothing was stored
                        && !s.covers('a', 0, null);
                })()"""), "a malformed position throws where it was written, and nothing is stored");
    }
}
