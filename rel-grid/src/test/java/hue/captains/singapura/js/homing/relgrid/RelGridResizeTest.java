package hue.captains.singapura.js.homing.relgrid;

import hue.captains.singapura.js.homing.ssjs.test.JsModuleTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * RFC 0050 · Episode 2, round 1 — column widths, Map 7's laws 53–60 as tests.
 * A width is geometry the grid answers alone: held by identity, applied by
 * position, in place; bounded where it is held; snapshotted as held, never
 * measured; restored drift-tolerantly and idempotently; persisted by nobody
 * here. Runs on {@link RelGridTestDom}, whose headers carry geometry and whose
 * {@code document} takes the pointer listeners a drag needs.
 */
class RelGridResizeTest extends JsModuleTestBase {

    @BeforeEach
    void setup() {
        js = buildContext();
        js.eval("js", RelGridTestDom.DOM_STUB);
        loadModule(RelGridTestDom.SELECTION);
        loadModule(RelGridTestDom.PROTOCOL);
        for (String m : RelGridTestDom.MODULES) loadModule(RelGridTestDom.DIR + m);
        js.eval("js", RelGridTestDom.FIXTURE);
    }

    private boolean evalBool(String expr) { return js.eval("js", expr).asBoolean(); }

    @Test
    void aWidthIsHeldByIdentityAppliedByPositionAndNeverRunsTheCycle() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture(), g = f.grid, maps = g.viewMaps();
                    g.setColumnWidth('calories', 150);
                    var applied = f.colWidth(1) === '150px' && /hrg-fixed/.test(f.table().className)
                               && f.arranged.join() === 'base';                  // in place: no cycle ran
                    maps.setColumnView(['calories', 'ingredient']);               // reorder: the width FOLLOWS
                    var followed = f.colWidth(0) === '150px' && f.colWidth(1) === '';
                    maps.setColumnView(['ingredient']);                           // hide it: nothing to apply
                    var hidden = f.colWidth(0) === '' && !/hrg-fixed/.test(f.table().className);
                    maps.resetColumnView();                                       // back: still held
                    var back = f.colWidth(1) === '150px' && g.columnWidth('calories') === 150;
                    return applied && followed && hidden && back
                        && f.resized.join() === 'calories 150';                  // reported once
                })()"""), "law 53/57: geometry alone, applied in place, riding identity across reorder and hide");
    }

    @Test
    void theRequestIsBoundedAtNormalisationAndTheBoundedRequestIsWhatIsHeld() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture(), g = f.grid;
                    var low  = g.setColumnWidth('calories', 3)    && g.columnWidth('calories') === 40   && f.colWidth(1) === '40px';
                    var high = g.setColumnWidth('calories', 9999) && g.columnWidth('calories') === 2000 && f.colWidth(1) === '2000px';
                    var drift = g.setColumnWidth('GHOST', 100) === false && g.columnWidth('GHOST') === null;
                    var junk  = g.setColumnWidth('calories', 'wide') === false && g.columnWidth('calories') === 2000;
                    return low && high && drift && junk && f.resized.join() === 'calories 40,calories 2000';
                })()"""), "law 55/59: bounded to [40, 2000] where it is held; an unknown column is refused");
    }

    @Test
    void aStagedDragMintsOnceOnReleaseAndEscapeMintsNothing() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture(), g = f.grid;
                    var th = f.thAt(1);                                          // calories
                    th._rl = 300.4; th._rr = 400.6;                              // rect: width 100.2 — fractional, as a browser's is
                    var handle = th.children[th.children.length - 1];
                    if (handle.className !== 'hrg-resize-handle') return false;
                    handle.dispatch('mousedown', { clientX: 398 });
                    var midDrag = f.colWidth(1) === '';                          // nothing applied mid-gesture
                    document.dispatch('mousemove', { clientX: 448 });
                    document.dispatch('mouseup', {});                            // ONE request: 100 + 50
                    var committed = f.colWidth(1) === '150px' && f.resized.join() === 'calories 150';
                    handle.dispatch('mousedown', { clientX: 399 });
                    document.dispatch('mousemove', { clientX: 500 });
                    document.dispatch('keydown', { key: 'Escape' });             // ABANDON
                    document.dispatch('mouseup', {});                            // stale: listeners are gone
                    var abandoned = f.colWidth(1) === '150px' && f.resized.length === 1;
                    th.dispatch('mousedown', { clientX: 350 });                  // the header body arms nothing
                    document.dispatch('mouseup', {});
                    return midDrag && committed && abandoned && f.colWidth(1) === '150px'
                        && document.body.children.length === 0;                  // no guide left behind
                })()"""), "law 54: a staged gesture mints one intent on release; Escape mints nothing; only the handle arms");
    }

    @Test
    void altArrowsResizeTheCursorsColumnWithoutMovingItAndAreInertWhileDeep() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture({ editable: true }), g = f.grid;
                    f.key('ArrowRight');                                         // cursor → calories
                    f.key('ArrowRight', { alt: true });
                    f.key('ArrowRight', { alt: true });
                    var grew = f.colWidth(1) === '140px';                        // 120 default + 2 × 10
                    f.key('ArrowLeft', { alt: true });
                    var back = f.colWidth(1) === '130px';
                    var stayed = g.cursor().column === 'calories' && f.moves.join() === 'mapo calories';
                    f.key('Enter');                                              // deep: the keyboard is the cell's
                    f.key('ArrowRight', { alt: true });
                    var inert = f.colWidth(1) === '130px' && g.isDeep();
                    return grew && back && stayed && inert;
                })()"""), "Alt+arrows are the pointer-free resize of the cursor's column; the cursor stays; deep makes them inert");
    }

    @Test
    void aSnapshotReturnsWhatIsHeldAndARestoreDropsDriftAndIsIdempotent() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture(), g = f.grid;
                    g.setColumnWidth('calories', 150);
                    var snap = g.columnWidths();
                    var held = JSON.stringify(snap) === JSON.stringify({ calories: 150 });
                    g.setColumnWidths({ GHOST: 200, ingredient: 90 });          // drift dropped, the rest apply
                    var restored = JSON.stringify(g.columnWidths()) === JSON.stringify({ calories: 150, ingredient: 90 })
                                && f.colWidth(0) === '90px' && g.columnWidth('GHOST') === null;
                    var reports = f.resized.length;
                    g.setColumnWidths(g.columnWidths());                         // apply a snapshot of itself
                    var idempotent = JSON.stringify(g.columnWidths()) === JSON.stringify({ calories: 150, ingredient: 90 })
                                  && f.resized.length === reports;               // nothing changed, nothing reported
                    return held && restored && idempotent && f.arranged.join() === 'base';
                })()"""), "law 56/58/59/60: a snapshot is what is held; restore is drift-tolerant and idempotent; no cycle");
    }
}
