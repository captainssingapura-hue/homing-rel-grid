package hue.captains.singapura.js.homing.relgrid;

import hue.captains.singapura.js.homing.ssjs.test.JsModuleTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * RFC 0050 · Episode 2, appendix The Copied Range: the copied state, its behaviour and its showing. A copy written is
 * kept with the cursor and the selection - the blocks, by identity - and shown as an overlay over each block, its area
 * and its border each wearing their leaf of the frame. Escape cancels in two stages - the copy, the selection kept;
 * then the selection, the cursor's cell remaining - and with nothing to cancel is not the grid's. A copy answered
 * with nothing leaves the copy as it was; a cell given control cancels it; a View under which its blocks no longer
 * stand as rectangles cancels it, and one under which they stand moves it.
 *
 * <p>GraalVM drains microtasks between evals, so a copy is asked in one eval and its writing seen in the next.</p>
 */
class RelGridCopiedTest extends JsModuleTestBase {

    @BeforeEach
    void setup() {
        js = buildContext();
        js.eval("js", RelGridTestDom.DOM_STUB);
        js.eval("js", RelGridTestDom.STYLES);
        for (String m : RelGridTestDom.PARTY) loadModule(m);
        loadModule(RelGridTestDom.CHANNEL);
        loadModule(RelGridTestDom.PROTOCOL);
        loadModule(RelGridTestDom.SELECTION);
        for (String m : RelGridTestDom.MODULES) loadModule(RelGridTestDom.DIR + m);
        js.eval("js", RelGridTestDom.FIXTURE);
        js.eval("js", """
                function has(el, c) { return (el.className || '').split(' ').indexOf(c) >= 0; }
                // A copy answered with content every time, unless a test answers otherwise.
                function copying(opts) {
                    opts = opts || {};
                    opts.ask = opts.ask || function (q) {
                        return Promise.resolve(q instanceof RelGridCopyRequested ? new RelGridClipboardContent('x', undefined) : undefined);
                    };
                    return fixture(opts);
                }
                // The overlays the copy is shown by: the wrapper's children wearing hrg-copied.
                function shown(f) {
                    var w = f.wrap(), out = [];
                    for (var k = 0; k < w.children.length; k++) if (has(w.children[k], 'hrg-copied')) out.push(w.children[k]);
                    return out;
                }
                function copiedSays(f) {
                    return f.grid.copied().map(function (b) { return b.pks.join(',') + ' / ' + b.columns.join(','); }).join(' | ');
                }
                """);
    }

    private boolean evalBool(String expr) { return js.eval("js", expr).asBoolean(); }
    private void act(String src) { js.eval("js", src); }

    @Test
    void aCopyWritten_isKeptByIdentity_andShownAsAnOverlay_itsAreaAndItsBorderEachWearingTheirLeaf() {
        act("var F = copying(); F.click(0, 0); F.click(1, 1, { shift: true }); F.grid.copy();");
        assertTrue(evalBool("""
                (() => {
                    if (copiedSays(F) !== 'mapo,coq / ingredient,calories') return false;
                    var boxes = shown(F);
                    if (boxes.length !== 1) return false;
                    var area = boxes[0], svg = area.children[0], line = svg && svg.children[0];
                    if (!has(area, 'frame-copied-area') || area.getAttribute('aria-hidden') !== 'true') return false;
                    if (area.style.getPropertyValue('--hrg-left') === '') return false;           // placed over the block's slots
                    if (!has(svg, 'hrg-copied-line') || svg.namespaceURI !== 'http://www.w3.org/2000/svg') return false;
                    if (!has(line, 'hrg-copied-rect') || !has(line, 'frame-copied-border')) return false;   // a line, never filled; the design's
                    if (line.getAttribute('width') !== '100%' || line.namespaceURI !== 'http://www.w3.org/2000/svg') return false;
                    return F.copied.length === 1;                                                  // and the host told, as ever
                })()"""), "the blocks by identity; an overlay over each, its area and its vector border wearing their leaves");
    }

    @Test
    void escape_cancelsTheCopyFirst_thenTheSelection_thenIsNotTheGrids() {
        act("var F = copying(); F.click(0, 0); F.click(1, 1, { shift: true }); F.grid.copy();");
        assertTrue(evalBool("""
                (() => {
                    if (!F.key('Escape')) return false;                                            // the first: the copy
                    if (F.grid.copied().length !== 0 || shown(F).length !== 0) return false;
                    if (F.grid.selectionCount() !== 1) return false;                                // the selection kept
                    if (!F.key('Escape')) return false;                                            // the second: the selection
                    if (F.grid.selectionCount() !== 0) return false;
                    var c = F.grid.cursor();
                    if (c.pk !== 'mapo' || c.column !== 'ingredient') return false;                // the cursor's cell remains
                    return F.key('Escape') === false;                                               // the third: not the grid's
                })()"""), "two stages, each consumed; with nothing to cancel the key bubbles");
    }

    @Test
    void escape_withNoCopy_cancelsTheSelection_andWithOnlyTheCursorsCell_isNotTheGrids() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture();
                    f.click(0, 0); f.click(2, 1, { shift: true });
                    if (!f.key('Escape') || f.grid.selectionCount() !== 0) return false;
                    f.click(1, 1, { ctrl: true });                                                  // a range of the cursor's own cell alone
                    return f.grid.selectionCount() === 1 && f.key('Escape') === false;
                })()"""), "a selection beyond the cursor's cell is cancelled; the cursor's cell alone is nothing to cancel");
    }

    @Test
    void aCopyAnsweredWithNothing_leavesTheCopyAsItWas() {
        act("""
                var n = 0;
                var F = fixture({ ask: function (q) {
                    if (!(q instanceof RelGridCopyRequested)) return Promise.resolve();
                    return Promise.resolve(++n === 1 ? new RelGridClipboardContent('x', undefined) : undefined);
                }});
                F.click(0, 0); F.grid.copy();
                """);
        act("F.click(2, 1); F.grid.copy();");
        assertTrue(evalBool("copiedSays(F) === 'mapo / ingredient' && shown(F).length === 1"),
                "absence writes nothing (law 49), and the copy before it stands");
    }

    @Test
    void aCellGivenControl_cancelsTheCopy() {
        act("var F = copying({ editable: true }); F.click(0, 0); F.grid.copy();");
        assertTrue(evalBool("""
                (() => {
                    if (shown(F).length !== 1) return false;
                    F.key('Enter');
                    return F.grid.isDeep() && F.grid.copied().length === 0 && shown(F).length === 0;
                })()"""), "an edit may change what was copied: the copy goes as the cell takes control");
    }

    @Test
    void aViewUnderWhichTheBlockStands_movesTheCopy_andOneUnderWhichItDoesNot_cancelsIt() {
        act("var F = copying(); F.click(0, 0); F.click(1, 0, { shift: true }); F.grid.copy();");   // mapo, coq of the ingredient
        assertTrue(evalBool("""
                (() => {
                    var maps = F.grid.viewMaps();
                    maps.setRowView(['fish', 'mapo', 'coq']);                                       // still side by side, in order
                    if (copiedSays(F) !== 'mapo,coq / ingredient' || shown(F).length !== 1) return false;
                    maps.setRowView(['coq', 'fish']);                                               // a window: mapo out, at the block's end
                    if (copiedSays(F) !== 'mapo,coq / ingredient' || shown(F).length !== 1) return false;
                    maps.setRowView(['fish']);                                                      // wholly out: kept, drawn nowhere
                    if (copiedSays(F) !== 'mapo,coq / ingredient' || shown(F).length !== 0) return false;
                    maps.setRowView(['coq', 'mapo', 'fish']);                                       // sorted apart: it no longer stands
                    return F.grid.copied().length === 0 && shown(F).length === 0;
                })()"""), "a block that still stands as a rectangle is shown where it stands; one that does not, cancels the copy");
    }

    @Test
    void aBlockPartlyOutOfTheWindow_isMarkedOverExactlyTheRowsStillShown() {
        act("var F = copying(); F.click(1, 0); F.click(2, 1, { shift: true }); F.grid.copy();");   // coq and fish, both columns
        assertEquals("true", js.eval("js", """
                (() => {
                    // The slots laid out as a browser would: a row 20 high, a column 100 wide; then a width held, so the
                    // overlay is placed again over the slots as they are.
                    var px = 100;
                    function laidOut() {
                        for (var i = 0; i < F.grid.viewMaps().rows(); i++)
                            for (var j = 0; j < 2; j++) {
                                var t = F.td(i, j);
                                t._rl = j * 100; t._rr = j * 100 + 100; t._rt = i * 20; t._rb = i * 20 + 20;
                            }
                        F.grid.setColumnWidth('calories', ++px);
                        var b = shown(F);
                        if (b.length !== 1) return 'boxes ' + b.length;
                        var st = b[0].style;
                        return ['--hrg-left', '--hrg-top', '--hrg-width', '--hrg-height'].map(function (p) { return st.getPropertyValue(p); }).join(' ');
                    }
                    var whole = laidOut();
                    if (whole !== '0px 20px 200px 40px') return 'whole: ' + whole;                   // rows 1 and 2, both columns
                    var maps = F.grid.viewMaps();
                    maps.setRowView(['mapo', 'coq']);                                               // fish below the window
                    var lower = laidOut();
                    if (lower !== '0px 20px 200px 20px') return 'fish out: ' + lower;                // coq alone, where it stands
                    maps.setRowView(['fish']);                                                      // coq above the window
                    var upper = laidOut();
                    if (upper !== '0px 0px 200px 20px') return 'coq out: ' + upper;                  // fish alone, at the top
                    return copiedSays(F) === 'coq,fish / ingredient,calories';                     // the copy itself, whole
                })()""").toString(),
                "half the block out of sight: the half still shown marked exactly, the copy kept whole");
    }

    @Test
    void theWindowMoving_placesTheMarkOverTheSlotsAsTheyStandAtTheEndOfThePass() {
        act("var F = copying(); F.click(1, 0); F.click(1, 1, { shift: true }); F.grid.copy();");   // coq, both columns
        assertEquals("0px 0px 200px 20px", js.eval("js", """
                (() => {
                    var maps = F.grid.viewMaps();
                    maps.setRowView(['mapo', 'coq']);
                    // A slot as tall as what it holds, as a browser lays it: a cell the window let go still standing in
                    // its slot beside the one that replaced it makes the row twice as tall, until it is detached.
                    for (var i = 0; i < 2; i++) for (var j = 0; j < 2; j++) (function (t, i, j) {
                        t.getBoundingClientRect = function () {
                            var h = 20 * Math.max(1, t.children.length);
                            return { left: j * 100, right: j * 100 + 100, top: i * 20, bottom: i * 20 + h, width: 100, height: h };
                        };
                    })(F.td(i, j), i, j);
                    maps.setRowView(['coq', 'fish']);                                               // the same slots: the window moved a row
                    var st = shown(F)[0].style;
                    return ['--hrg-left', '--hrg-top', '--hrg-width', '--hrg-height'].map(function (p) { return st.getPropertyValue(p); }).join(' ');
                })()""").toString(), "the mark measured once the cells the window let go have left their slots");
    }

    @Test
    void aRowGoneFromTheMiddle_orAColumnHidden_cancelsTheCopy() {
        act("var F = copying(); F.click(0, 0); F.click(2, 1, { shift: true }); F.grid.copy();");   // every row, both columns
        assertTrue(evalBool("""
                (() => {
                    F.grid.viewMaps().setRowView(['mapo', 'fish']);                                 // coq gone from between them
                    return F.grid.copied().length === 0;
                })()"""), "a filter through the block");
        act("var G = copying(); G.click(0, 0); G.click(0, 1, { shift: true }); G.grid.copy();");
        assertTrue(evalBool("""
                (() => {
                    G.grid.viewMaps().setColumnView(['calories']);                                   // the ingredient hidden
                    return G.grid.copied().length === 0 && shown(G).length === 0;
                })()"""), "a column of the block hidden");
    }

    @Test
    void theCopiedState_aloneDecidesWhereABlockStands() {
        assertTrue(evalBool("""
                (() => {
                    var maps = new RelGridViewMaps({ rowView: ['a', 'b', 'c', 'd'], columns: ['x', 'y', 'z'] });
                    var s = new RelGridCopied();
                    if (s.has() || s.rects(maps).length !== 0 || s.clear() !== false) return false;
                    s.set([new RelGridBlock(['b', 'c'], ['y', 'z'])]);
                    var r = s.rects(maps)[0];
                    if (r.i0 !== 1 || r.i1 !== 2 || r.j0 !== 1 || r.j1 !== 2) return false;
                    if (new RelGridCopied().set([new RelGridBlock(['a'], ['z', 'y'])]).rects(maps) !== null) return false;   // columns out of order
                    return s.clear() === true && !s.has();
                })()"""), "identities to the rectangle they stand as now, or none");
    }
}
