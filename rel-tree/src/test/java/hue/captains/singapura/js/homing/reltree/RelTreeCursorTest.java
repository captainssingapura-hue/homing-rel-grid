package hue.captains.singapura.js.homing.reltree;

import hue.captains.singapura.js.homing.ssjs.test.JsModuleTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * RFC 0050 · Episode 3-ext1, step 3 — the cursor, the keys, the pointer: the
 * table of §5 as a test, one row per line; the cell told its mode; the two
 * notifications; the cursor across a fold and a re-ask; {@code selectNode}.
 */
class RelTreeCursorTest extends JsModuleTestBase {

    @BeforeEach
    void setup() {
        js = buildContext();
        js.eval("js", RelTreeTestDom.DOM_STUB);
        js.eval("js", RelTreeTestDom.DOM_PARSER);
        js.eval("js", RelTreeTestDom.STYLES);
        js.eval("js", RelTreeTestDom.SVGS);
        for (String m : RelTreeTestDom.PARTY) loadModule(m);
        loadModule(RelTreeTestDom.CHANNEL);
        loadModule(RelTreeTestDom.PROTOCOL);
        for (String m : RelTreeTestDom.MODULES) loadModule(RelTreeTestDom.DIR + m);
        js.eval("js", RelTreeTestDom.FIXTURE);
    }

    private boolean evalBool(String expr) { return js.eval("js", expr).asBoolean(); }
    private void act(String code) { js.eval("js", code); }

    @Test
    void aPressLandsTheCursorAndTheCellAndTheDomainAreTold() {
        assertTrue(evalBool("""
                (() => {
                    var F = fixture();
                    if (F.current() !== -1 || F.tree.cursor() !== null) return false;
                    F.click(1);
                    if (F.tree.cursor() !== 'b' || F.current() !== 1 || F.moves.join() !== 'b') return false;
                    // The cell is told 'shallow' and wears it; the one left is told 'none'.
                    if (!/hrt-text-cell-current/.test(F.cellEl(1).className)) return false;
                    F.click(2);
                    if (/hrt-text-cell-current/.test(F.cellEl(1).className) || !/hrt-text-cell-current/.test(F.cellEl(2).className)) return false;
                    // The domain hears every move on the channel, and a press on the same row says nothing new.
                    var told = F.sent.filter(function (q) { return q instanceof RelTreeCursorChanged; }).map(function (q) { return q.key; });
                    if (told.join() !== 'b,c') return false;
                    F.click(2);
                    if (F.sent.length !== 2 || F.moves.length !== 2) return false;
                    // A double-click lands and activates: told on the channel, and reported.
                    F.dblclick(0);
                    if (F.tree.cursor() !== 'a' || F.activated.join() !== 'a' || !(F.lastSent(RelTreeActivated) instanceof RelTreeActivated)) return false;
                    // Enter activates the current node; the programmatic twin does the same; a stranger is refused.
                    if (!F.key('Enter') || F.activated.join() !== 'a,a' || !F.tree.activate('c') || F.tree.activate('zz')) return false;
                    return F.activated.join() === 'a,a,c';
                })()"""), "a press lands the cursor; the cell, the host and the domain are told; a double-click and Enter activate");
    }

    @Test
    void theArrowsHomeEndAndThePageKeysMoveAmongTheRowsAndReportTheEdges() {
        assertTrue(evalBool("""
                (() => {
                    var F = fixture({ open: ['a', 'a2'] });         // a a1 a2 a2x a2y b c — seven rows
                    if (F.shown() !== 'a:0:open a1:1:leaf a2:1:open a2x:2:leaf a2y:2:leaf b:0:leaf c:0:closed') return false;
                    // No cursor yet: the first key lands on the first row.
                    if (!F.key('ArrowDown') || F.tree.cursor() !== 'a') return false;
                    if (!F.key('ArrowDown') || F.tree.cursor() !== 'a1' || !F.key('ArrowUp') || F.tree.cursor() !== 'a') return false;
                    // At the top, up is an edge: reported, consumed, the cursor stays.
                    if (!F.key('ArrowUp') || F.edges.join() !== 'up' || F.tree.cursor() !== 'a') return false;
                    if (!F.key('End') || F.tree.cursor() !== 'c' || !F.key('ArrowDown') || F.edges.join() !== 'up,down') return false;
                    if (!F.key('Home') || F.tree.cursor() !== 'a') return false;
                    // A page is ten rows when nothing has a height: clamped to the last, then the first.
                    if (!F.key('PageDown') || F.tree.cursor() !== 'c' || !F.key('PageUp') || F.tree.cursor() !== 'a') return false;
                    // A key the tree does not own is left alone.
                    return !F.key('x') && F.tree.cursor() === 'a';
                })()"""), "the vertical keys move among the presented rows, clamped, and the edges are reported");
    }

    @Test
    void theTreeOwnsLeftAndRightByWhatThePlaceIs() {
        act("var F = fixture({ open: ['a', 'a2'] });   // a a1 a2 a2x a2y b c");
        assertTrue(evalBool("""
                (() => {
                    F.click(0);                                                  // a: open
                    // → on an open node: to the first child. ← on a leaf: to the parent.
                    if (!F.key('ArrowRight') || F.tree.cursor() !== 'a1' || !F.key('ArrowLeft') || F.tree.cursor() !== 'a') return false;
                    // ← at the root level with nowhere to go: consumed, nothing moves, nothing asked.
                    F.click(5);                                                  // b: a leaf at the root
                    if (!F.key('ArrowLeft') || F.tree.cursor() !== 'b' || !F.key('ArrowRight') || F.tree.cursor() !== 'b') return false;
                    if (F.sent.some(function (q) { return q instanceof RelTreeFold || q instanceof RelTreeUnfold; })) return false;
                    // ← on a2y (a leaf under a2): to a2. ← on a2 (open): asks the fold.
                    F.click(4);
                    if (!F.key('ArrowLeft') || F.tree.cursor() !== 'a2' || !F.key('ArrowLeft')) return false;
                    return F.lastSent(RelTreeFold).key === 'a2';
                })()"""), "left and right are the tree's: child, parent, and the fold questions");
        assertTrue(evalBool("""
                (() => {
                    // Folded: a a1 a2 b c; the cursor stayed on a2, now closed. → asks the unfold.
                    if (F.shown() !== 'a:0:open a1:1:leaf a2:1:closed b:0:leaf c:0:closed' || F.tree.cursor() !== 'a2') return false;
                    if (!F.key('ArrowRight') || F.lastSent(RelTreeUnfold).key !== 'a2') return false;
                    return true;
                })()"""), "right on a closed node asks the unfold");
        assertTrue(evalBool("""
                (() => {
                    if (F.shown() !== 'a:0:open a1:1:leaf a2:1:open a2x:2:leaf a2y:2:leaf b:0:leaf c:0:closed') return false;
                    // Space toggles: open → asks the fold.
                    F.click(6);                                                  // c: closed
                    if (!F.key(' ') || F.lastSent(RelTreeUnfold).key !== 'c') return false;
                    return true;
                })()"""), "space asks the fold the place would take next");
        assertTrue(evalBool("""
                (() => {
                    if (F.shown().indexOf('c:0:open c1:1:leaf') < 0 || F.tree.cursor() !== 'c') return false;
                    // A press on the caret lands the cursor and asks; the row's own press is not reported twice.
                    var before = F.sent.length;
                    F.clickCaret(0);                                             // a: open → fold
                    return F.tree.cursor() === 'a' && F.lastSent(RelTreeFold).key === 'a' && F.sent.length === before + 2;   // the cursor's notification, and the fold
                })()"""), "a press on the caret lands the cursor and asks the fold");
        assertTrue(evalBool("""
                (() => F.shown() === 'a:0:closed b:0:leaf c:0:open c1:1:leaf' && F.current() === 0)()"""),
                "the caret's fold applied");
    }

    @Test
    void theCursorLandsOnTheFolderWhenItsRowFoldsAwayAndKeepsItsKeyAcrossAReask() {
        act("var F = fixture({ open: ['a', 'a2'] }); F.click(3);   // a2x, under a2 under a");
        assertTrue(evalBool("(() => F.tree.cursor() === 'a2x' && F.tree.fold('a'))()"),
                "a fold of an ancestor is asked");
        assertTrue(evalBool("""
                (() => {
                    // a2x folded away with a: the cursor lands on a — the node the tree asked to fold — not on row 3.
                    if (F.shown() !== 'a:0:closed b:0:leaf c:0:closed') return false;
                    if (F.tree.cursor() !== 'a' || F.current() !== 0 || F.moves.join() !== 'a2x,a') return false;
                    // Across a re-ask that keeps the key, the cursor keeps it wherever it moved to.
                    F.click(1);                                                  // b
                    F.relation.close('a2'); F.relation.open('a'); F.tree.tell(new RelTreeViewChanged());   // a a1 a2 b c
                    if (F.tree.cursor() !== 'b' || F.current() !== 3) return false;
                    // Across a re-ask that drops the key, the position stands in, clamped.
                    F.click(4);                                                  // c, the last row
                    var view = ['a', 'b'];
                    F.relation.view = function () { return view; };
                    F.tree.tell(new RelTreeViewChanged());
                    if (F.tree.cursor() !== 'b' || F.current() !== 1 || F.moves.join() !== 'a2x,a,b,c') return false;
                    // An empty View: no cursor at all; the next View gives none back until a press.
                    view = [];
                    F.tree.tell(new RelTreeViewChanged());
                    if (F.tree.cursor() !== null || F.current() !== -1) return false;
                    view = ['a'];
                    F.tree.tell(new RelTreeViewChanged());
                    return F.tree.cursor() === null && F.key('ArrowDown') && F.tree.cursor() === 'a';
                })()"""), "the cursor is identity-first, lands on the folder after a fold, and falls back to its position");
    }

    @Test
    void selectNodeIsTheProgrammaticCursorAndTheCaretMayBeTheDomains() {
        assertTrue(evalBool("""
                (() => {
                    var F = fixture({ open: ['a'] });
                    if (!F.tree.selectNode('a2') || F.tree.cursor() !== 'a2' || F.current() !== 2 || F.moves.join() !== 'a2') return false;
                    // Not presented: false, and the cursor stays. The tree opens nothing on the domain's behalf.
                    if (F.tree.selectNode('a2x') || F.tree.cursor() !== 'a2' || F.sent.some(function (q) { return q instanceof RelTreeUnfold; })) return false;
                    // A domain that draws its own caret: no caret of the tree's in the row; the cell is the row's only child.
                    var G = fixture({ caret: false, open: ['a'] });
                    if (G.row(0).children.length !== 1 || G.drawn() !== 'A@0 A1@1 A2@1 B@0 C@0') return false;
                    // Its fold state flipped by its own cell, and told: the same result, no question asked.
                    G.relation.close('a'); G.tree.tell(new RelTreeViewChanged());
                    return G.drawn() === 'A@0 B@0 C@0' && G.sent.length === 0;
                })()"""), "selectNode reaches a presented key only; a domain may draw its own caret and tell");
    }

    @Test
    void withoutAChannelTheCaretsAskNothing() {
        assertTrue(evalBool("""
                (() => {
                    var F = fixture({ noAsk: true });
                    F.click(0);
                    if (F.tree.unfold('a') || F.key('ArrowRight') !== true || F.shown() !== 'a:0:closed b:0:leaf c:0:closed') return false;
                    F.clickCaret(0);
                    return F.shown() === 'a:0:closed b:0:leaf c:0:closed' && F.tree.cursor() === 'a' && !F.tree.isPending();
                })()"""), "a tree without a channel never unfolds: lazy by default, at the limit");
    }
}
