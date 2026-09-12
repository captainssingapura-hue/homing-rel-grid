package hue.captains.singapura.js.homing.relgrid;

import hue.captains.singapura.js.homing.ssjs.test.JsModuleTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * RFC 0050 · Episode 2 — the view handover: the rows' arrangement asked of
 * the domain, and its View presented exactly as given.
 *
 * <p>What is proved. The control is <b>off by default</b> and withheld
 * without a channel; the header's menu, Alt+Enter and the verb ask
 * {@code RelGridViewHandover} with the column, and the header wears
 * {@code hrg-menu-open} while the question is out; the answer is presented
 * <b>as given</b> — placed, not re-rendered; the cursor keeps its identity
 * and the ranges clear (map 1 laws 1–3); absence leaves the rows alone; a
 * stranger is refused whole and an empty View empties the space (map 14 law
 * 100); and, being a pending question, it shares the lock with copy and the
 * deep session (ext6).</p>
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
        loadModule(RelGridTestDom.PROTOCOL);
        loadModule(RelGridTestDom.SELECTION);
        for (String m : RelGridTestDom.MODULES) loadModule(RelGridTestDom.DIR + m);
        js.eval("js", RelGridTestDom.FIXTURE);
    }

    private boolean evalBool(String expr) { return js.eval("js", expr).asBoolean(); }
    private void act(String src) { js.eval("js", src); }

    @Test
    void offByDefaultTheSlotIsThereAndTheControlIsNot() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture();
                    // Every header ends in an ops slot — the footprint is constant — and
                    // with nothing asked for, the slot is empty.
                    for (var j = 0; j < 2; j++) {
                        var th = f.thAt(j), slot = null;
                        for (var k = 0; k < th.children.length; k++)
                            if (th.children[k].className === 'hrg-th-ops') slot = th.children[k];
                        if (!slot || slot.children.length !== 0) return false;
                        if (f.menuAt(j) !== null) return false;
                    }
                    // And the chord is not the grid's: nothing asked, nothing consumed.
                    f.click(0, 0);
                    var ev = { key: 'Enter', altKey: true, prevented: false };
                    ev.preventDefault = function () { ev.prevented = true; };
                    f.table().dispatch('keydown', ev);
                    return !ev.prevented && f.handoverAsked() === null && !f.grid.isPending();
                })()"""), "with no columnOps the header offers no menu and Alt+Enter is left alone");
    }

    @Test
    void withoutAChannelTheControlIsWithheld() {
        assertTrue(evalBool("""
                (() => {
                    var warned = [];
                    var was = console.warn;
                    console.warn = function () { warned.push(Array.prototype.slice.call(arguments).join(' ')); };
                    var f = fixture({ noAsk: true, columnOps: { handover: true } });
                    console.warn = was;
                    // A control that could do nothing is not offered — and said once.
                    if (f.menuAt(0) !== null || f.menuAt(1) !== null) return false;
                    if (!warned.some(function (w) { return /handover needs an ask channel/.test(w); })) return false;
                    // The verb is channel-gated too.
                    return f.grid.handoverView('calories') === false && !f.grid.isPending();
                })()"""), "handover without a channel offers no control and the verb answers false");
    }

    @Test
    void theMenuAsksWithItsColumnAndMarksTheHeader() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture({ columnOps: { handover: true } });
                    var menu = f.menuAt(1);
                    if (!menu || menu.textContent !== '\\u25BE') return false;
                    menu.dispatch('click', {});
                    var q = f.handoverAsked();
                    if (!q || !(q instanceof RelGridViewHandover)) return false;
                    if (q.column !== 'calories') return false;
                    if (Object.keys(q).join(',') !== 'column') return false;         // a column, and nothing else
                    if (!f.grid.isPending()) return false;
                    // The header whose question is out says so; the other does not.
                    var has = function (el, c) { return (el.className || '').split(' ').indexOf(c) >= 0; };
                    return has(f.thAt(1), 'hrg-menu-open') && !has(f.thAt(0), 'hrg-menu-open');
                })()"""), "the menu asks a handover naming its column, and the header wears hrg-menu-open");
    }

    @Test
    void aViewIsPresentedExactlyAsGivenPlacedNotReRendered() {
        act("""
                var V = fixture({ columnOps: { handover: true }, ask: function (q) {
                    if (!(q instanceof RelGridViewHandover)) return Promise.resolve();
                    return Promise.resolve(new RelGridView(['fish', 'mapo']));    // a filter and a permutation at once
                }});
                var V_fish = V.cellEl('fish', 'ingredient'), V_coqCell = V.relation.cell('coq', 'ingredient');
                V.menuAt(0).dispatch('click', {});
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
                    // The mark is gone with the question, and the grid is itself again.
                    var has = function (el, c) { return (el.className || '').split(' ').indexOf(c) >= 0; };
                    if (has(V.thAt(0), 'hrg-menu-open') || has(V.thAt(1), 'hrg-menu-open')) return false;
                    V.key('ArrowDown');
                    return V.grid.cursor().pk === 'mapo';
                })()"""), "a View is presented as given: the same cells in new slots, the rest detached and alive");
    }

    @Test
    void theCursorKeepsItsIdentityAndTheRangesClear() {
        act("""
                var C = fixture({ columnOps: { handover: true }, ask: function (q) {
                    if (!(q instanceof RelGridViewHandover)) return Promise.resolve();
                    return Promise.resolve(new RelGridView(['fish', 'coq', 'mapo']));  // reversed
                }});
                C.click(2, 1);                                   // the cursor on fish/calories, at the bottom
                C.click(0, 0, { shift: true });                  // and a range up to the corner
                C.grid.handoverView('ingredient');
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
                var A = fixture({ columnOps: { handover: true }, ask: function (q, mask) {
                    if (!(q instanceof RelGridViewHandover)) return Promise.resolve();
                    mask.panel();                                              // the domain drew something...
                    return new Promise(function (r) { A_answer = r; });
                }});
                var A_answer = null;
                A.click(1, 0);
                var A_before = A.arranged.length;
                A.menuAt(1).dispatch('click', {});
                A_answer(undefined);                                           // ...and then cancelled
                """);
        assertTrue(evalBool("""
                (() => {
                    if (A.grid.isPending()) return false;
                    if (A.rowsShown() !== 'mapo,coq,fish') return false;
                    if (A.arranged.length !== A_before) return false;            // no arrangement ran
                    var has = function (el, c) { return (el.className || '').split(' ').indexOf(c) >= 0; };
                    if (has(A.thAt(1), 'hrg-menu-open')) return false;
                    runTimers();                                               // the hold
                    if (A.mask() !== null) return false;
                    A.key('ArrowDown');
                    return A.grid.cursor().pk === 'fish';
                })()"""), "cancel is the domain answering absence: nothing moves, the mark comes off, the grid resumes");
    }

    @Test
    void aStrangerIsRefusedWholeAndAnEmptyViewEmptiesTheSpace() {
        act("""
                var errors = [];
                console.error = function () { errors.push(Array.prototype.slice.call(arguments).join(' ')); };
                var S = fixture({ columnOps: { handover: true }, ask: function (q) {
                    if (!(q instanceof RelGridViewHandover)) return Promise.resolve();
                    return Promise.resolve(new RelGridView(['fish', 'nobody']));
                }});
                S.grid.handoverView('calories');
                """);
        assertTrue(evalBool("""
                (() => {
                    // Half a View is not a View: nothing moved, the failure is on record,
                    // and the grid is unlocked.
                    if (S.rowsShown() !== 'mapo,coq,fish') return false;
                    if (!errors.some(function (e) { return /applying an answer threw/.test(e); })) return false;
                    return !S.grid.isPending() && S.grid.handoverView('calories') === true;
                })()"""), "a View naming an undeclared pk is refused whole and recorded");
        act("""
                var P = fixture({ columnOps: { handover: true }, ask: function (q) {
                    if (!(q instanceof RelGridViewHandover)) return Promise.resolve();
                    return Promise.resolve({ pks: ['fish'] });                 // a plain object is not an answer
                }});
                P.grid.handoverView('calories');
                var E = fixture({ columnOps: { handover: true }, ask: function (q) {
                    if (!(q instanceof RelGridViewHandover)) return Promise.resolve();
                    return Promise.resolve(new RelGridView([]));
                }});
                E.click(1, 1);
                E.grid.handoverView('calories');
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
    void altEnterHandsOverTheCursorsColumn() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture({ columnOps: { handover: true } });
                    f.click(1, 1);
                    var ev = { key: 'Enter', altKey: true, prevented: false };
                    ev.preventDefault = function () { ev.prevented = true; };
                    f.table().dispatch('keydown', ev);
                    var q = f.handoverAsked();
                    if (!q || q.column !== 'calories' || !ev.prevented) return false;
                    var has = function (el, c) { return (el.className || '').split(' ').indexOf(c) >= 0; };
                    return f.grid.isPending() && has(f.thAt(1), 'hrg-menu-open');
                })()"""), "Alt+Enter asks a handover for the cursor's column and marks its header");
    }

    @Test
    void theVerbWorksWithoutTheControlAndRefusesAStranger() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture();                                         // a channel, no control
                    if (f.menuAt(0) !== null) return false;
                    if (f.grid.handoverView('calories') !== true) return false;
                    if (f.handoverAsked().column !== 'calories') return false;
                    var g = fixture();
                    try { g.grid.handoverView('nope'); return false; }
                    catch (e) { if (!/not a column/.test(String(e))) return false; }
                    return !g.grid.isPending();
                })()"""), "the option gates the affordance, the channel gates the question; an undeclared column throws");
    }

    @Test
    void whileLockedTheHandoverIsRefused() {
        assertTrue(evalBool("""
                (() => {
                    // Pending on a copy: one question at a time.
                    var f = fixture({ columnOps: { handover: true } });
                    f.click(0, 0);
                    if (!f.grid.copy()) return false;
                    f.menuAt(0).dispatch('click', {});
                    if (f.grid.handoverView('calories') !== false) return false;
                    if (f.handoverAsked() !== null) return false;
                    // Deep: the keyboard is the cell's, and so is the moment.
                    var g = fixture({ editable: true, columnOps: { handover: true } });
                    g.click(0, 0);
                    if (!g.grid.takeControlAtCursor() || !g.grid.isDeep()) return false;
                    g.menuAt(1).dispatch('click', {});
                    g.key('Enter', { alt: true });
                    return g.grid.handoverView('calories') === false && g.handoverAsked() === null && g.grid.isDeep();
                })()"""), "a handover shares the lock: refused while a question is out or a cell is deep");
    }

    @Test
    void theMaskAndItsPanelServeTheHandover() {
        act("""
                var M = fixture({ columnOps: { handover: true }, ask: function (q, mask) {
                    if (!(q instanceof RelGridViewHandover)) return Promise.resolve();
                    var panel = mask.panel();
                    var list = document.createElement('ul');                   // the domain's controls
                    panel.appendChild(list);
                    M_panel = panel;
                    return new Promise(function (r) { M_answer = r; });
                }});
                var M_answer = null, M_panel = null;
                M.menuAt(0).dispatch('click', {});
                """);
        assertTrue(evalBool("""
                (() => {
                    // The mask went up at once, with the panel in it, and the domain's list in that.
                    if (M.mask() === null || M.panel() !== M_panel) return false;
                    if (M_panel.children.length !== 1 || M_panel.children[0].tagName !== 'ul') return false;
                    if (!M.grid.isPending()) return false;
                    M_answer(new RelGridView(['coq']));
                    return true;
                })()"""), "the handover lends the domain the mask's panel, as copy does");
        assertTrue(evalBool("""
                (() => {
                    if (M.rowsShown() !== 'coq' || M.grid.isPending()) return false;
                    runTimers();                                               // the hold
                    return M.mask() === null;
                })()"""), "and the View is presented when the panel's answer comes");
    }
}
