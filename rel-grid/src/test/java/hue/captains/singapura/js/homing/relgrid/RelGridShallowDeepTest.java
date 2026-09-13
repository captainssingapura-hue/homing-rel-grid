package hue.captains.singapura.js.homing.relgrid;

import hue.captains.singapura.js.homing.ssjs.test.JsModuleTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * RFC 0050 · Episode 2 — the shallow/deep discipline, enforced by the grid.
 *
 * <p>A single click or an arrow key is shallow: it moves the cursor and the
 * cell is only told. Deep is entered only through the grid — Enter or a
 * double-click on the cursor's cell — and offered in <b>two stages</b>: the
 * column's declared constraint, then the cell's own judgement. The handover
 * itself answers a promise, and the grid holds deep until it settles.</p>
 *
 * <p><b>Why some tests are split in two.</b> A promise settles on a microtask,
 * and GraalVM drains those <i>between</i> evals rather than within one. So a
 * test that must see the grid AFTER it resumed acts in one eval and asserts in
 * the next — which is also a truer test, since it observes the resume the way a
 * browser would rather than inside the call that caused it.</p>
 */
class RelGridShallowDeepTest extends JsModuleTestBase {

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

    /** Act. Anything the cell settled drains before the next eval begins. */
    private void act(String script) { js.eval("js", script); }

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
                    var input = f.overlay().children[0];
                    if (!(f.grid.isDeep() && input && input.tagName === 'input')) return false;
                    if (f.td(0, 0).children[0].children.length !== 0) return false;   // NOT in the slot
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
    void theEditorLivesOutsideTheTableEntirely() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture({ editable: true });
                    var slot = f.td(0, 0), host = slot.children[0];
                    if (f.overlay() !== null) return false;
                    f.key('Enter');
                    var ov = f.overlay();
                    // A sibling of the TABLE, in the wrapper — not a descendant of the
                    // slot. Out there it cannot widen a column, stretch a row, or be
                    // clipped by the cell, and it may be larger than the cell it edits.
                    if (!ov || !/hrg-edit/.test(ov.className)) return false;
                    if (ov.parentNode !== f.wrap()) return false;
                    if (f.wrap().children[0] !== f.table()) return false;
                    if (ov.children[0].tagName !== 'input') return false;
                    // Placed at the slot and sized to at least it.
                    if (ov.style.getPropertyValue('--hrg-left') === '') return false;
                    if (ov.style.getPropertyValue('--hrg-width') === '') return false;
                    // The cell's own element is untouched — still showing what it showed.
                    if (host.children.length !== 0 || host.textContent !== 'tofu') return false;
                    // And a resize cannot move the column out from under it.
                    if (f.grid.setColumnWidth('ingredient', 300) !== false) return false;
                    return true;
                })()"""), "the editor is minted outside the table, over the slot, and the slot is untouched");
    }

    @Test
    void theOverlayComesDownWithTheSession() {
        act("""
                var E = fixture({ editable: true });
                E.key('Enter');
                E.overlay().children[0].dispatch('keydown', { key: 'Escape' });
                """);
        assertTrue(evalBool("""
                (() => {
                    return E.overlay() === null && !E.grid.isDeep()
                        && E.td(0, 0).children[0].textContent === 'tofu'
                        && E.wrap().children.length === 1;          // just the table again
                })()"""), "when the cell settles the grid takes its overlay down");
    }

    @Test
    void theCellSettlesOnceAndTheGridResumesWithoutLearningWhatHappened() {
        act("""
                var F = fixture({ editable: true });
                F.key('Enter');
                var HOST = F.td(0, 0).children[0];
                var INPUT = F.overlay().children[0];
                INPUT.value = 'TOFU-EDITED';
                INPUT.dispatch('keydown', { key: 'Enter' });               // the CELL ends its own edit
                """);
        // The commit is synchronous — it is the cell's own business, done before
        // it settles — but the RESUME waits for the microtask this eval boundary
        // drains.
        assertTrue(evalBool("""
                (() => {
                    return F.commits.join() === 'mapo ingredient TOFU-EDITED'
                        && !F.grid.isDeep()                                // the grid resumed
                        && F.ended.join() === 'mapo ingredient'            // told it ENDED, and once
                        && document.activeElement === F.table()            // the keyboard host has focus again
                        && F.relation.cell('mapo', 'ingredient').mode() === 'shallow'
                        && HOST.children.length === 0                      // the input is gone, exactly once
                        && HOST.textContent === 'TOFU-EDITED'              // shown because the OWNER set() it
                        && !/hrg-deep/.test(F.table().className)
                        && (F.key('ArrowDown'), F.grid.cursor().pk === 'coq');   // and the arrows are back
                })()"""), "the cell settles once; the grid resumes and never learns commit from cancel");
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
                        && f.overlay().children[0].tagName === 'input'
                })()"""), "a double-click is the pointer's way into deep, on the cell it landed on");
    }

    @Test
    void aCellWithNowhereToCommitAnswersNoAndIsNeverOffered() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture();                                     // no onCommit anywhere
                    if (f.grid.mayTakeControlAtCursor() !== false) return false;   // asked WITHOUT opening
                    f.key('Enter');
                    f.td(1, 1).dispatch('dblclick');
                    return !f.grid.isDeep() && f.started.length === 0 && f.ended.length === 0
                        && f.overlay() === null                        // nothing was even minted
                        && f.td(0, 0).children[0].children.length === 0
                        && f.td(1, 1).children[0].children.length === 0
                        && /hrg-text-ro/.test(f.td(0, 0).children[0].className)   // the cell named the property (law 116)
                        && f.grid.cursor().pk === 'coq';                   // the dblclick still moved the cursor
                })()"""), "stage one answers no, so the handover is never offered and nothing changes hands");
    }

    @Test
    void escapeCancelsInsideTheCellAndHandsControlBack() {
        act("""
                var F = fixture({ editable: true });
                F.key('Enter');
                var IN1 = F.overlay().children[0];
                IN1.value = 'never';
                IN1.dispatch('keydown', { key: 'Escape' });
                """);
        assertTrue(evalBool("""
                (() => {
                    return !F.grid.isDeep() && F.ended.length === 1
                        && F.td(0, 0).children[0].textContent === 'tofu'
                        && F.commits.length === 0;
                })()"""), "Escape cancels inside the cell, and the settle hands control back");
    }

    @Test
    void losingFocusCancelsAndHandsControlBackToo() {
        act("""
                var G = fixture({ editable: true });
                G.key('Enter');
                G.overlay().children[0].value = 'never either';
                G.table().focus();                                          // focus leaves the input
                """);
        assertTrue(evalBool("""
                (() => {
                    return !G.grid.isDeep() && G.ended.length === 1
                        && G.td(0, 0).children[0].textContent === 'tofu'
                        && G.commits.length === 0;
                })()"""), "a blur cancels inside the cell, and that settles the handover as well");
    }

    @Test
    void clickingAnotherCellWhileDeepEndsTheEditFirstThenMovesTheCursor() {
        act("""
                var H = fixture({ editable: true });
                H.key('Enter');
                // In a browser, mousedown on another slot moves focus to the table
                // (the nearest focusable ancestor) BEFORE the click fires.
                H.table().focus();
                """);
        // The blur settled the handover; the grid resumes across this boundary.
        act("H.click(2, 0);");
        assertTrue(evalBool("""
                (() => {
                    var c = H.grid.cursor();
                    return !H.grid.isDeep() && H.ended.length === 1
                        && c.pk === 'fish' && c.column === 'ingredient'
                        && H.moves.join() === 'fish ingredient';
                })()"""), "the cell ends on blur and settles; the click then lands as a shallow move");
    }

    @Test
    void aConstrainedColumnsCellIsNeverAsked() {
        assertTrue(evalBool("""
                (() => {
                    // The relation declares calories read-only. Its cells are perfectly
                    // willing — same stock cells, same onCommit — and are never consulted.
                    var f = fixture({ editable: true, readOnly: ['calories'] });
                    var cell = f.relation.cell('mapo', 'calories');
                    if (cell.mayTakeControl() !== true) return false;       // the CELL would say yes
                    f.click(0, 1);                                          // cursor onto calories
                    if (f.grid.mayTakeControlAtCursor() !== false) return false;   // structure says no first
                    f.key('Enter');
                    f.td(0, 1).dispatch('dblclick');
                    if (f.grid.isDeep() || f.started.length !== 0) return false;
                    // And the unconstrained column is unaffected.
                    f.click(0, 0);
                    return f.grid.mayTakeControlAtCursor() === true
                        && f.grid.takeControlAtCursor() === true && f.grid.isDeep();
                })()"""), "law 112: a constrained column's cell is never asked, and the constraint is the relation's");
    }

    @Test
    void onlyAnExplicitYesOpensAndOnlyAThenableHoldsControl() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture({ editable: true });
                    var cell = f.relation.cell('mapo', 'ingredient');

                    // A cell that FORGETS to answer stage one declines — the safe direction.
                    cell.mayTakeControl = function () { return undefined; };
                    if (f.grid.takeControlAtCursor() !== false || f.grid.isDeep()) return false;

                    // One that says yes but answers the handover with nothing breaks the
                    // contract. Recorded and refused: waiting for a settle that cannot come
                    // would leave the grid inert with nothing to show for it.
                    cell.mayTakeControl = function () { return true; };
                    cell.takeControl = function () { return undefined; };
                    if (f.grid.takeControlAtCursor() !== false || f.grid.isDeep()) return false;

                    // One whose editor is not an element breaks it the same way.
                    cell.takeControl = function () { return new Promise(function () {}); };
                    var editor = cell.editorElement;
                    cell.editorElement = function () { return undefined; };
                    if (f.grid.takeControlAtCursor() !== false || f.grid.isDeep()) return false;
                    cell.editorElement = function () { throw new Error('nope'); };
                    if (f.grid.takeControlAtCursor() !== false || f.grid.isDeep()) return false;
                    cell.editorElement = editor;
                    // One that throws in either stage is refused, not fatal.
                    cell.mayTakeControl = function () { throw new Error('nope'); };
                    if (f.grid.takeControlAtCursor() !== false || f.grid.isDeep()) return false;
                    cell.mayTakeControl = function () { return true; };
                    cell.takeControl = function () { throw new Error('nope'); };
                    return f.grid.takeControlAtCursor() === false && !f.grid.isDeep()
                        && f.started.length === 0 && f.ended.length === 0;
                })()"""), "stage one needs an explicit true; stage two needs a thenable; anything else fails safe");
    }

    @Test
    void aRejectedHandoverStillResumesTheGrid() {
        act("""
                var R = fixture({ editable: true });
                var RCELL = R.relation.cell('mapo', 'ingredient');
                RCELL.mayTakeControl = function () { return true; };
                RCELL.takeControl = function () { return Promise.reject(new Error('the edit blew up')); };
                R.took = R.grid.takeControlAtCursor();
                R.deepAtOnce = R.grid.isDeep();
                """);
        assertTrue(evalBool("""
                (() => {
                    return R.took === true && R.deepAtOnce === true         // control WAS taken
                        && !R.grid.isDeep()                                 // and given back by the rejection
                        && R.ended.join() === 'mapo ingredient'
                        && R.relation.cell('mapo', 'ingredient').mode() === 'shallow';
                })()"""), "an edit that blew up still ended: a rejection resumes the grid exactly as a resolve does");
    }

    @Test
    void aSettleAfterDestroyDoesNotResurrectTheGrid() {
        act("""
                var D = fixture({ editable: true });
                var DCELL = D.relation.cell('mapo', 'ingredient');
                var SETTLE;
                DCELL.mayTakeControl = function () { return true; };
                DCELL.takeControl = function () { return new Promise(function (r) { SETTLE = r; }); };
                D.grid.takeControlAtCursor();
                D.grid.destroy();
                SETTLE();
                """);
        assertTrue(evalBool("""
                (() => {
                    // The settle arrived after the grid was gone. It must report nothing and
                    // touch nothing — a late promise is exactly the case a handed-out
                    // callback could not have guarded.
                    return D.ended.length === 0;
                })()"""), "a settle after destroy resumes nothing and reports nothing");
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
