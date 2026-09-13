package hue.captains.singapura.js.homing.relgrid;

import hue.captains.singapura.js.homing.ssjs.test.JsModuleTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * RFC 0050 · Episode 2 — the view handover: the rows' arrangement asked of
 * the domain, and its View presented exactly as given.
 *
 * <p>What is proved. The verb and Alt+Enter ask {@code RelGridViewHandover},
 * which carries nothing, and the grid attaches no control of its own to it —
 * the header is untouched; the answer is presented <b>as given</b> — placed,
 * not re-rendered; the cursor keeps its identity and the ranges clear (map 1
 * laws 1–3); absence leaves the rows alone; a stranger is refused whole and
 * an empty View empties the space (map 14 law 100); without a channel nothing
 * is asked and the chord is left alone; and, being a pending question, it
 * shares the lock with copy and the deep session (ext6).</p>
 *
 * <p>The same two harness facts as the copy tests: microtasks drain between
 * evals, so an answer acts in one eval and is asserted in the next; and
 * timers are fake.</p>
 */
class RelGridViewHandoverTest extends JsModuleTestBase {

    @BeforeEach
    void setup() {
        js = buildContext();
        js.eval("js", RelGridTestDom.DOM_STUB);
        js.eval("js", RelGridTestDom.STYLES);
        for (String m : RelGridTestDom.PARTY) loadModule(m);
        loadModule(RelGridTestDom.PROTOCOL);
        loadModule(RelGridTestDom.SELECTION);
        for (String m : RelGridTestDom.MODULES) loadModule(RelGridTestDom.DIR + m);
        js.eval("js", RelGridTestDom.FIXTURE);
    }

    private boolean evalBool(String expr) { return js.eval("js", expr).asBoolean(); }
    private void act(String src) { js.eval("js", src); }

    @Test
    void theVerbAsksAQuestionThatCarriesNothingAndTheHeaderIsUntouched() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture();
                    // The header is the grid's geometry, and the domain's arrangement is
                    // not a property of any column: a label, a resize handle, nothing else.
                    for (var j = 0; j < 2; j++) {
                        var th = f.thAt(j);
                        for (var k = 0; k < th.children.length; k++)
                            if (th.children[k].className !== 'hrg-resize-handle') return false;
                    }
                    if (f.grid.handoverView() !== true) return false;
                    var q = f.handoverAsked();
                    if (!q || !(q instanceof RelGridViewHandover)) return false;
                    if (Object.keys(q).length !== 0) return false;                 // the kind is the message
                    if (!f.grid.isPending()) return false;
                    // Still nothing on the header while the question is out.
                    return f.thAt(0).children.length === 1 && f.thAt(1).children.length === 1;
                })()"""), "handoverView() asks a RelGridViewHandover with no payload, and no header wears anything");
    }

    @Test
    void altEnterIsTheTablesRoadToTheSameQuestion() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture();
                    f.click(1, 1);
                    var ev = { key: 'Enter', altKey: true, prevented: false };
                    ev.preventDefault = function () { ev.prevented = true; };
                    f.table().dispatch('keydown', ev);
                    var q = f.handoverAsked();
                    if (!q || !ev.prevented) return false;
                    if (!f.grid.isPending()) return false;
                    // And the cursor is where it was: the chord is not a move.
                    return f.grid.cursor().pk === 'coq' && f.grid.cursor().column === 'calories';
                })()"""), "Alt+Enter asks the handover and is consumed");
    }

    @Test
    void withoutAChannelNothingIsAskedAndTheChordIsLeftAlone() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture({ noAsk: true });
                    if (f.grid.handoverView() !== false || f.grid.isPending()) return false;
                    f.click(0, 0);
                    var ev = { key: 'Enter', altKey: true, prevented: false };
                    ev.preventDefault = function () { ev.prevented = true; };
                    f.table().dispatch('keydown', ev);
                    return !ev.prevented && f.handoverAsked() === null && !f.grid.isPending();
                })()"""), "no channel: the verb answers false, and Alt+Enter is not the grid's to consume");
    }

    @Test
    void aViewIsPresentedExactlyAsGivenPlacedNotReRendered() {
        act("""
                var V = fixture({ ask: function (q) {
                    if (!(q instanceof RelGridViewHandover)) return Promise.resolve();
                    return Promise.resolve(new RelGridView(['fish', 'mapo']));    // a filter and a permutation at once
                }});
                var V_fish = V.cellEl('fish', 'ingredient'), V_coqCell = V.relation.cell('coq', 'ingredient');
                V.grid.handoverView();
                """);
        assertTrue(evalBool("""
                (() => {
                    if (V.grid.isPending()) return false;
                    // Exactly these rows, in exactly this order: the grid reordered nothing.
                    if (V.rowsShown() !== 'fish,mapo') return false;
                    if (V.arranged[V.arranged.length - 1] !== 'rows') return false;
                    // The same cell element, moved: re-placed, not re-rendered (law 2).
                    if (V.cellEl('fish', 'ingredient') !== V_fish) return false;
                    if (V.td(0, 0).children[0] !== V_fish) return false;
                    // The row that left is detached and alive — not disposed, not re-asked.
                    if (V.cellEl('coq', 'ingredient') !== null) return false;
                    if (V.relation.cell('coq', 'ingredient') !== V_coqCell) return false;
                    if (V.asked() !== 6) return false;                                  // one ask per identity, ever
                    // And the grid is itself again.
                    V.key('ArrowDown');
                    return V.grid.cursor().pk === 'mapo';
                })()"""), "a View is presented as given: the same cells in new slots, the rest detached and alive");
    }

    @Test
    void theCursorKeepsItsIdentityAndTheRangesClear() {
        act("""
                var C = fixture({ ask: function (q) {
                    if (!(q instanceof RelGridViewHandover)) return Promise.resolve();
                    return Promise.resolve(new RelGridView(['fish', 'coq', 'mapo']));  // reversed
                }});
                C.click(2, 1);                                   // the cursor on fish/calories, at the bottom
                C.click(0, 0, { shift: true });                  // and a range up to the corner
                C.grid.handoverView();
                """);
        assertTrue(evalBool("""
                (() => {
                    if (C.rowsShown() !== 'fish,coq,mapo') return false;
                    // The cursor followed fish to the top (law 3)...
                    var cur = C.grid.cursor();
                    if (cur.pk !== 'fish' || cur.column !== 'calories') return false;
                    var at = C.grid.viewMaps().locate('fish', 'calories');
                    if (at.i !== 0 || at.j !== 1) return false;
                    // ...and the range did not: positions are not the same positions (law 43).
                    if (C.grid.selectionCount() !== 0) return false;
                    var r = C.grid.selectedRanges();
                    return r.length === 1 && r[0].i0 === 0 && r[0].i1 === 0 && r[0].j0 === 1 && r[0].j1 === 1;
                })()"""), "the cursor is an identity and survives; a range is positions and does not");
    }

    @Test
    void absenceLeavesTheRowsAsTheyAre() {
        act("""
                var A = fixture({ ask: function (q, mask) {
                    if (!(q instanceof RelGridViewHandover)) return Promise.resolve();
                    mask.panel(makeEl('div'));                                 // the domain drew something...
                    return new Promise(function (r) { A_answer = r; });
                }});
                var A_answer = null;
                A.click(1, 0);
                var A_before = A.arranged.length;
                A.grid.handoverView();
                A_answer(undefined);                                           // ...and then cancelled
                """);
        assertTrue(evalBool("""
                (() => {
                    if (A.grid.isPending()) return false;
                    if (A.rowsShown() !== 'mapo,coq,fish') return false;
                    if (A.arranged.length !== A_before) return false;            // no arrangement ran
                    runTimers();                                               // the hold
                    if (A.mask() !== null) return false;
                    A.key('ArrowDown');
                    return A.grid.cursor().pk === 'fish';
                })()"""), "cancel is the domain answering absence: nothing moves, and the grid resumes");
    }

    @Test
    void aStrangerIsRefusedWholeAndAnEmptyViewEmptiesTheSpace() {
        act("""
                var errors = [];
                console.error = function () { errors.push(Array.prototype.slice.call(arguments).join(' ')); };
                var S = fixture({ ask: function (q) {
                    if (!(q instanceof RelGridViewHandover)) return Promise.resolve();
                    return Promise.resolve(new RelGridView(['fish', 'nobody']));
                }});
                S.grid.handoverView();
                """);
        assertTrue(evalBool("""
                (() => {
                    // Half a View is not a View: nothing moved, the failure is on record,
                    // and the grid is unlocked.
                    if (S.rowsShown() !== 'mapo,coq,fish') return false;
                    if (!errors.some(function (e) { return /applying an answer threw/.test(e); })) return false;
                    return !S.grid.isPending() && S.grid.handoverView() === true;
                })()"""), "a View naming an undeclared pk is refused whole and recorded");
        act("""
                var P = fixture({ ask: function (q) {
                    if (!(q instanceof RelGridViewHandover)) return Promise.resolve();
                    return Promise.resolve({ pks: ['fish'] });                 // a plain object is not an answer
                }});
                P.grid.handoverView();
                var E = fixture({ ask: function (q) {
                    if (!(q instanceof RelGridViewHandover)) return Promise.resolve();
                    return Promise.resolve(new RelGridView([]));
                }});
                E.click(1, 1);
                E.grid.handoverView();
                """);
        assertTrue(evalBool("""
                (() => {
                    if (P.rowsShown() !== 'mapo,coq,fish') return false;
                    if (!errors.some(function (e) { return /not a View/.test(e); })) return false;
                    // Everything filtered out is legal: no rows, no cursor, and the cells live on.
                    if (E.grid.viewMaps().rows() !== 0 || E.grid.cursor() !== null) return false;
                    if (E.relation.cell('coq', 'calories') === null) return false;
                    return !E.grid.isPending();
                })()"""), "a non-protocol answer is recorded and ignored; an empty View is presented as empty");
    }

    @Test
    void whileLockedTheHandoverIsRefused() {
        assertTrue(evalBool("""
                (() => {
                    // Pending on a copy: one question at a time.
                    var f = fixture();
                    f.click(0, 0);
                    if (!f.grid.copy()) return false;
                    if (f.grid.handoverView() !== false) return false;
                    f.key('Enter', { alt: true });
                    if (f.handoverAsked() !== null) return false;
                    // Deep: the keyboard is the cell's, and so is the moment.
                    var g = fixture({ editable: true });
                    g.click(0, 0);
                    if (!g.grid.takeControlAtCursor() || !g.grid.isDeep()) return false;
                    g.key('Enter', { alt: true });
                    return g.grid.handoverView() === false && g.handoverAsked() === null && g.grid.isDeep();
                })()"""), "a handover shares the lock: refused while a question is out or a cell is deep");
    }

    @Test
    void theMaskAndItsPanelServeTheHandover() {
        act("""
                var M_list = makeEl('ul');                                     // the domain's controls, its own element
                var M = fixture({ ask: function (q, mask) {
                    if (!(q instanceof RelGridViewHandover)) return Promise.resolve();
                    mask.panel(M_list);
                    return new Promise(function (r) { M_answer = r; });
                }});
                var M_answer = null;
                M.grid.handoverView();
                """);
        assertTrue(evalBool("""
                (() => {
                    // The mask went up at once, with the panel in it, and the domain's list placed in that.
                    var p = M.panel();
                    if (M.mask() === null || !p) return false;
                    if (p.children.length !== 1 || p.children[0] !== M_list) return false;
                    if (!M.grid.isPending()) return false;
                    M_answer(new RelGridView(['coq']));
                    return true;
                })()"""), "the handover places the domain's element in the mask's panel, as copy does");
        assertTrue(evalBool("""
                (() => {
                    if (M.rowsShown() !== 'coq' || M.grid.isPending()) return false;
                    // The answer came: the panel is down at once — the domain's list out of the
                    // mask with it, alive and the domain's to dissolve — while the wash holds.
                    if (M.panel() !== null || M.mask() === null || M_list.parentNode.parentNode !== null) return false;
                    runTimers();                                               // the hold
                    return M.mask() === null;
                })()"""), "and the View is presented when the panel's answer comes; the panel goes with the answer, the wash after its hold");
    }
}
