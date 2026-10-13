package hue.captains.singapura.js.homing.relgrid;

import hue.captains.singapura.js.homing.ssjs.test.JsModuleTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
