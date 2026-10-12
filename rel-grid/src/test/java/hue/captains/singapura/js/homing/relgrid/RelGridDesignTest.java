package hue.captains.singapura.js.homing.relgrid;

import hue.captains.singapura.js.homing.ssjs.test.JsModuleTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * RFC 0050 · Episode 2, Appendix: The Frame — the grid is made with a design, and every look it shows is the design's.
 * The grid asks the design to wear a leaf of the frame on each element it makes and never learns a class; the test
 * design wears a leaf as {@code frame-<token>}, so a test sees what was asked, and where. A grid told no design, a
 * design of another branch, or the old {@code frame} option, refuses to be made.
 */
class RelGridDesignTest extends JsModuleTestBase {

    @BeforeEach
    void setup() {
        js = buildContext();
        js.eval("js", RelGridTestDom.DOM_STUB);
        js.eval("js", RelGridTestDom.STYLES);
        for (String m : RelGridTestDom.PARTY) loadModule(m);
        loadModule(RelGridTestDom.SELECTION);
        loadModule(RelGridTestDom.CHANNEL);
        loadModule(RelGridTestDom.PROTOCOL);
        for (String m : RelGridTestDom.MODULES) loadModule(RelGridTestDom.DIR + m);
        js.eval("js", RelGridTestDom.FIXTURE);
        js.eval("js", """
                function wears(el, token) { return el.classList.contains('frame-' + token); }
                function refusal(opts) {
                    var relation = { view: function () { return []; }, columns: function () { return ['a']; }, cellFor: function () { return null; } };
                    var o = { container: makeEl('div'), branch: testBranch(), relation: relation };
                    for (var k in opts) o[k] = opts[k];
                    try { new RelGrid(o); return null; } catch (e) { return String(e.message); }
                }
                """);
    }

    private boolean evalBool(String expr) { return js.eval("js", expr).asBoolean(); }

    private void act(String script) { js.eval("js", script); }

    @Test
    void theGridRefusesToBeMadeWithoutADesignOfItsFrame() {
        assertTrue(evalBool("""
                (() => {
                    var none = refusal({});
                    var other = refusal({ design: { branch: 'button', name: 'x', wear: function () {}, shed: function () {} } });
                    var bare = refusal({ design: { branch: 'grid-frame' } });
                    var frame = refusal({ design: TEST_DESIGN, frame: false });
                    return none !== null && none.indexOf('opts.design is required') >= 0
                        && other !== null && other.indexOf('a design of button') >= 0
                        && bare !== null && bare.indexOf('opts.design is required') >= 0
                        && frame !== null && frame.indexOf('frame is gone') >= 0
                        && refusal({ design: TEST_DESIGN }) === null;
                })()"""), "no design, another branch's, one that cannot wear, or the old frame option: refused");
    }

    @Test
    void everyElementTheGridMakesWearsItsLeaf() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture({ rowNumbers: true });
                    var body = f.tbody(), head = f.headerRow();
                    if (!wears(f.wrap(), 'grid-edge')) return false;                 // the box: the wrapper, by default
                    if (!wears(head.children[0], 'gutter-band') || !wears(body.children[0].children[0], 'gutter-band')) return false;
                    if (!wears(head.children[1], 'header-band') || wears(head.children[1], 'lattice')) return false;
                    for (var i = 0; i < body.children.length; i++)
                        for (var j = 1; j < body.children[i].children.length; j++) {
                            var td = body.children[i].children[j];
                            if (!wears(td, 'lattice') || !wears(td, 'selection-fill')) return false;
                        }
                    // the grid's own classes are mechanics and facts: none of them is a leaf's
                    return !wears(f.table(), 'grid-edge') && f.table().className.indexOf('frame-') < 0;
                })()"""), "the box, the bands, and every slot wear the frame's leaves, as the design is asked");
    }

    @Test
    void aBoxTheHostNamesWearsTheEdgeAndIsGivenBackBare() {
        assertTrue(evalBool("""
                (() => {
                    var box = makeEl('div');
                    var f = fixture({ box: box });
                    if (!wears(box, 'grid-edge') || wears(f.wrap(), 'grid-edge')) return false;
                    f.grid.destroy();
                    return !wears(box, 'grid-edge');
                })()"""), "a host that scrolls the table names its box: the edge is worn there, and shed on destroy");
    }

    @Test
    void theCursorWearsTheLeafForWhereTheKeysAre() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture({ rowNumbers: true });
                    function slot(i, j) { return f.tbody().children[i].children[j + 1]; }
                    function only(el, token) {
                        var all = ['cursor-border', 'held-cursor-border', 'lent-cursor-border'];
                        for (var k = 0; k < all.length; k++) if (wears(el, all[k]) !== (all[k] === token)) return false;
                        return true;
                    }
                    // the keys elsewhere: the quiet leaf, and the row's number is current
                    if (!only(slot(0, 0), 'cursor-border') || f.tbody().children[0].children[0].getAttribute('aria-current') !== 'true') return false;
                    f.table().dispatch('focusin', {});                                // the keys come to the grid
                    if (!only(slot(0, 0), 'held-cursor-border')) return false;
                    f.key('ArrowDown');                                                // the leaf moves with the cursor
                    if (!only(slot(0, 0), null) || !only(slot(1, 0), 'held-cursor-border')) return false;
                    if (f.tbody().children[0].children[0].getAttribute('aria-current') !== null) return false;
                    if (f.tbody().children[1].children[0].getAttribute('aria-current') !== 'true') return false;
                    f.table().dispatch('focusout', { relatedTarget: f.td(0, 0) });     // within the wrapper: still held
                    if (!only(slot(1, 0), 'held-cursor-border')) return false;
                    f.table().dispatch('focusout', { relatedTarget: makeEl('input') });   // away
                    return only(slot(1, 0), 'cursor-border');
                })()"""), "the cursor's slot wears one leaf for where the keys are, and swaps it as they move");
    }

    @Test
    void aCellHoldingControlLendsTheCursorItsLeaf() {
        act("""
                var D = fixture({ editable: true });
                D.table().dispatch('focusin', {});
                D.key('Enter');
                """);
        assertTrue(evalBool("""
                (() => {
                    var td = D.td(0, 0);
                    if (!D.grid.isDeep() || !wears(td, 'lent-cursor-border') || wears(td, 'held-cursor-border')) return false;
                    var ov = D.overlay();
                    return ov && wears(ov, 'editor-anchor');
                })()"""), "deep: the cursor wears the lent leaf and the editor's anchor wears its own");
        act("D.overlay().children[0].dispatch('keydown', { key: 'Escape' });");
        assertTrue(evalBool("""
                (() => !D.grid.isDeep() && wears(D.td(0, 0), 'held-cursor-border') && !wears(D.td(0, 0), 'lent-cursor-border'))()"""),
                "control given back: the cursor wears the held leaf again");
    }

    @Test
    void theSelectionIsAFactAndAnAriaSelectedTheFillShowsBy() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture({ rowNumbers: true });
                    function slot(i, j) { return f.tbody().children[i].children[j + 1]; }
                    function number(i) { return f.tbody().children[i].children[0]; }
                    f.grid.selectCell('mapo', 'ingredient');
                    f.grid.extendSelection('coq', 'calories');                       // rows 0 and 1, whole
                    if (slot(0, 0).getAttribute('aria-selected') !== 'true' || slot(1, 1).getAttribute('aria-selected') !== 'true') return false;
                    if (slot(2, 0).getAttribute('aria-selected') !== null) return false;
                    if (number(0).getAttribute('aria-selected') !== 'true' || number(2).getAttribute('aria-selected') !== null) return false;
                    f.grid.selectCell('fish', 'calories');                           // one slot: no row whole
                    return slot(0, 0).getAttribute('aria-selected') === null && number(0).getAttribute('aria-selected') === null
                        && slot(2, 1).getAttribute('aria-selected') === 'true' && number(2).getAttribute('aria-selected') === null;
                })()"""), "a selected slot is aria-selected; a row selected whole has its number selected too");
    }
}
