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
    @Test
    void aFullySizedTableIsAtLeastItsColumnsSumAndItsLastColumnIsElastic() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture(), g = f.grid, ts = f.table().style;
                    // Nothing held: the sheet's 100% stands (no inline width).
                    if (ts.getPropertyValue('width') !== '') return false;
                    // One held, one free: still the box's width — the free column shares the remainder.
                    g.setColumnWidth('ingredient', 150);
                    if (ts.getPropertyValue('width') !== '' || f.colWidth(0) !== '150px') return false;
                    // Every column held: at least their sum, and the LAST column takes what the box
                    // has over — no property of its own, so a fixed layout gives it the remainder.
                    g.setColumnWidth('calories', 90);
                    if (ts.getPropertyValue('width') !== 'max(100%, 240px)') return false;
                    if (f.colWidth(0) !== '150px' || f.colWidth(1) !== '') return false;
                    if (g.columnWidth('calories') !== 90) return false;                   // held all the same
                    g.setColumnWidth('calories', 60);
                    if (ts.getPropertyValue('width') !== 'max(100%, 210px)') return false;
                    // The elastic one is whichever is presented last: hide calories and ingredient is.
                    g.viewMaps().setColumnView(['ingredient']);
                    if (ts.getPropertyValue('width') !== 'max(100%, 150px)' || f.colWidth(0) !== '') return false;
                    g.viewMaps().setColumnView(['calories', 'ingredient']);
                    return ts.getPropertyValue('width') === 'max(100%, 210px)' && f.colWidth(0) === '60px' && f.colWidth(1) === '';
                })()"""), "with every presented column sized the table is at least their sum and the last column stretches; with any free it keeps the box");
    }

    @Test
    void aDragStartsFromTheWidthHeldNotTheWidthSeen() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture(), g = f.grid;
                    g.setColumnWidths({ ingredient: 140, calories: 140 });
                    var th = f.thAt(0);
                    th._rl = 100; th._rr = 290;                                  // seen: 190 — a box wider than the sum once stretched it
                    var handle = th.children[th.children.length - 1];
                    handle.dispatch('mousedown', { clientX: 288 });
                    document.dispatch('mousemove', { clientX: 268 });            // 20 to the LEFT
                    document.dispatch('mouseup', {});
                    // Narrower, as asked: 140 - 20, not 190 - 20.
                    if (g.columnWidth('ingredient') !== 120) return false;
                    // A column that holds nothing still starts from what is seen.
                    var h = fixture();
                    var th2 = h.thAt(1); th2._rl = 300; th2._rr = 400;
                    var handle2 = th2.children[th2.children.length - 1];
                    handle2.dispatch('mousedown', { clientX: 398 });
                    document.dispatch('mousemove', { clientX: 428 });
                    document.dispatch('mouseup', {});
                    return h.grid.columnWidth('calories') === 130;
                })()"""), "a drag moves the width that is held, in the direction the pointer went");
    }

    @Test
    void theGuideSpansTheExtentTheHostNames() {
        assertTrue(evalBool("""
                (() => {
                    // A host stacking tables names the stack; here, any box with a rect of its own.
                    var stack = makeEl('div');
                    stack.getBoundingClientRect = function () { return { left: 0, right: 500, top: 40, bottom: 640, width: 500, height: 600 }; };
                    var f = fixture({ resizeGuide: stack });
                    var th = f.thAt(1); th._rl = 300; th._rr = 400;
                    var handle = th.children[th.children.length - 1];
                    handle.dispatch('mousedown', { clientX: 398 });
                    var guide = document.body.children[document.body.children.length - 1];
                    if (!guide || guide.className !== 'hrg-resize-guide') return false;
                    var top = guide.style.getPropertyValue('--hrg-guide-top'), h = guide.style.getPropertyValue('--hrg-guide-h');
                    document.dispatch('keydown', { key: 'Escape' });
                    if (top !== '40px' || h !== '600px') return false;
                    // Without one, the table's own box.
                    var g = fixture();
                    var th2 = g.thAt(1); th2._rl = 300; th2._rr = 400;
                    var handle2 = th2.children[th2.children.length - 1];
                    handle2.dispatch('mousedown', { clientX: 398 });
                    var guide2 = document.body.children[document.body.children.length - 1];
                    var top2 = guide2.style.getPropertyValue('--hrg-guide-top'), h2 = guide2.style.getPropertyValue('--hrg-guide-h');
                    document.dispatch('keydown', { key: 'Escape' });
                    return top2 === '0px' && h2 === '20px';
                })()"""), "the guide line runs down the extent the host names, else down the table");
    }

    @Test
    void anExtentOfSeveralBoxesIsASegmentOfLineEach() {
        assertTrue(evalBool("""
                (() => {
                    // Three tables' boxes with captions between: 40..240, 300..500, 560..760.
                    var boxes = [makeEl('div'), makeEl('div'), makeEl('div')];
                    boxes[0]._rt = 40; boxes[0]._rb = 240; boxes[1]._rt = 300; boxes[1]._rb = 500; boxes[2]._rt = 560; boxes[2]._rb = 760;
                    var f = fixture({ resizeGuide: boxes });
                    var th = f.thAt(1); th._rl = 300; th._rr = 400;
                    var handle = th.children[th.children.length - 1];
                    var was = document.body.children.length;
                    handle.dispatch('mousedown', { clientX: 398 });
                    var made = document.body.children.slice(was);
                    if (made.length !== 3) return false;
                    var spans = made.map(function (g) { return g.style.getPropertyValue('--hrg-guide-top') + '+' + g.style.getPropertyValue('--hrg-guide-h'); });
                    if (spans.join(' ') !== '40px+200px 300px+200px 560px+200px') return false;
                    // Every segment follows the pointer, and every one goes on release.
                    document.dispatch('mousemove', { clientX: 420 });
                    if (made.some(function (g) { return g.style.getPropertyValue('--hrg-guide-x') !== '420px'; })) return false;
                    document.dispatch('mouseup', {});
                    if (document.body.children.length !== was) return false;
                    // A box out of view draws no segment at all.
                    boxes[1]._rt = -300; boxes[1]._rb = -100;
                    if (typeof window === 'undefined') globalThis.window = { innerHeight: 1000 };
                    handle.dispatch('mousedown', { clientX: 398 });
                    var again = document.body.children.slice(was).length;
                    document.dispatch('keydown', { key: 'Escape' });
                    return again === 2;
                })()"""), "a list of boxes is a segment of line each, none across what lies between them");
    }

    @Test
    void theGuideStopsWhereThePaneCutsTheExtentOff() {
        assertTrue(evalBool("""
                (() => {
                    // A pane that scrolls, 100..400 tall, holding a stack 40..640: only 100..400 is seen.
                    var pane = makeEl('div'); pane._rt = 100; pane._rb = 400; pane._scrolls = true;
                    var stack = makeEl('div'); stack._rt = 40; stack._rb = 640;
                    pane.appendChild(stack);
                    getComputedStyle = function (el) { return { overflowY: el._scrolls ? 'auto' : 'visible', overflow: 'visible' }; };
                    var f = fixture({ resizeGuide: stack });
                    var th = f.thAt(1); th._rl = 300; th._rr = 400;
                    var handle = th.children[th.children.length - 1];
                    handle.dispatch('mousedown', { clientX: 398 });
                    var guide = document.body.children[document.body.children.length - 1];
                    var top = guide.style.getPropertyValue('--hrg-guide-top'), h = guide.style.getPropertyValue('--hrg-guide-h');
                    document.dispatch('keydown', { key: 'Escape' });
                    if (top !== '100px' || h !== '300px') return false;
                    // An ancestor that does not scroll clips nothing.
                    pane._scrolls = false;
                    handle.dispatch('mousedown', { clientX: 398 });
                    var g2 = document.body.children[document.body.children.length - 1];
                    var top2 = g2.style.getPropertyValue('--hrg-guide-top'), h2 = g2.style.getPropertyValue('--hrg-guide-h');
                    document.dispatch('keydown', { key: 'Escape' });
                    delete globalThis.getComputedStyle;
                    return top2 === '40px' && h2 === '600px';
                })()"""), "the guide runs down what is SEEN of the extent: a scrolling ancestor cuts it, a plain one does not");
    }
}
