package hue.captains.singapura.js.homing.jsonkit;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The document alone — what a tree asks its domain for, answered from a JSON
 * value: places by pointer, lazily under the fold; cells once per node,
 * printed by kind; the channel answered at once; a new value keeping the
 * folds where a container still stands and the cells that still stand.
 */
class JsonDocumentTest extends JsonKitTestBase {

    private static final String HELPERS = """
            function outline(D) { return D.view().map(function (p) { return p.key + ':' + p.depth + ':' + p.fold; }).join(' '); }
            function doc(value, opts) { opts = opts || {}; opts.branch = testBranch(); return createJsonDocument(value, opts); }
            """;

    @Test
    void placesAreTheValuesOutlineByPointerLazilyUnderTheFold() {
        act(HELPERS);
        assertEquals(":0:open /name:1:leaf /n:1:leaf /ok:1:leaf /none:1:leaf /list:1:closed /a~1b:1:leaf /empty:1:leaf",
                evalString("var D = doc(SAMPLE); outline(D)"),
                "the root open, every member a place by pointer, a container closed, an empty container a leaf");
        assertEquals(":0:open /name:1:leaf /n:1:leaf /ok:1:leaf /none:1:leaf /list:1:open /list/0:2:leaf /list/1:2:closed /a~1b:1:leaf /empty:1:leaf",
                evalString("D.open('/list'); outline(D)"), "an opened container presents its elements, closed");
        assertTrue(evalBool("D.open('/list/1'); outline(D).indexOf('/list/1:2:open /list/1/deep:3:leaf') > 0"), "three deep");
        // Closing keeps what was open beneath: the node that returns is as it was.
        assertTrue(evalBool("D.close('/list'); outline(D).indexOf('/list:1:closed /a~1b') > 0 && D.isOpen('/list/1')"));
        assertTrue(evalBool("D.open('/list'); outline(D).indexOf('/list/1:2:open /list/1/deep:3:leaf') > 0"));
        // The pointer arithmetic: escapes, indices, and what is not there.
        assertTrue(evalBool("""
                Object.keys(D.valueAt('/a~1b')).length === 0 && D.valueAt('/list/1/deep') === 'x' && D.valueAt('/list/0') === 1
                && D.valueAt('') === SAMPLE && D.valueAt('/zz') === undefined && D.valueAt('/list/9') === undefined
                && D.valueAt('/list/01') === undefined && D.valueAt('name') === undefined && D.valueAt('/none/x') === undefined"""));
        // Open at first, by depth; a scalar or an empty root is one leaf.
        assertEquals(":0:closed", evalString("outline(doc(SAMPLE, { openDepth: 0 }))"));
        assertTrue(evalBool("outline(doc(SAMPLE, { openDepth: 3 })).indexOf('/list/1:2:open /list/1/deep:3:leaf') > 0"));
        assertEquals(":0:leaf", evalString("outline(doc(42))"));
        assertEquals(":0:leaf", evalString("outline(doc({}))"));
        assertEquals(":0:leaf", evalString("outline(doc(undefined))"));
    }

    @Test
    void cellsOncePerPointerPrintedByKindAndAStrangerRefused() {
        act(HELPERS + "var D = doc(SAMPLE); D.openAll();");
        assertEquals("$: {7} | name: \"kit\" | n: 3 | ok: true | none: null | list: [2] | 0: 1 | 1: {1} | deep: \"x\" | a/b: {} | empty: []",
                evalString("['', '/name', '/n', '/ok', '/none', '/list', '/list/0', '/list/1', '/list/1/deep', '/a~1b', '/empty']"
                         + ".map(function (p) { return D.cellFor(p).text(); }).join(' | ')"),
                "every kind printed: strings quoted, literals bare, containers as counts, an index as its number");
        assertTrue(evalBool("D.cellFor('/list/1/deep') === D.cellFor('/list/1/deep') && D.cellCount() === 11"), "a cell once per pointer");
        assertTrue(evalBool("""
                ['/zz', '/list/5', 'name', '/name/0'].every(function (p) { try { D.cellFor(p); return false; } catch (e) { return String(e).indexOf('no such node') > 0; } })"""),
                "a stranger is refused by throwing");
        assertEquals("s: \"abcd…\"", evalString("doc({ s: 'abcdefghij' }, { maxString: 4 }).cellFor('/s').text()"), "a string cut at maxString");
        assertEquals("doc: {7}", evalString("doc(SAMPLE, { title: 'doc' }).cellFor('').text()"), "the root row is called by the title");
    }

    @Test
    void theChannelAnswersUnfoldAndFoldAtOnceAndHearsANotification() {
        act(HELPERS + "var D = doc(SAMPLE), got = []; D.answer(new RelTreeUnfold('/list'), null).then(function (v) { got.push(v); });");
        assertTrue(evalBool("got.length === 1 && got[0] instanceof RelTreeView && D.isOpen('/list')"
                          + " && got[0].places.some(function (p) { return p.key === '/list/0'; })"),
                "an unfold is answered with the whole View, the node opened");
        act("D.answer(new RelTreeFold('/list'), null).then(function (v) { got.push(v); });");
        assertTrue(evalBool("got.length === 2 && got[1] instanceof RelTreeView && !D.isOpen('/list')"
                          + " && !got[1].places.some(function (p) { return p.key === '/list/0'; })"),
                "a fold is answered with the whole View, the node closed");
        act("D.answer(new RelTreeCursorChanged('/n'), null).then(function (v) { got.push(v); });");
        assertTrue(evalBool("got.length === 3 && got[2] === undefined"), "a notification is heard and answered with nothing");
    }

    @Test
    void setKeepsTheFoldsWhereAContainerStillStandsSetsTheCellsThatStandAndLetsTheRestGo() {
        act(HELPERS + "var D = doc(SAMPLE); D.open('/list'); D.open('/list/1'); var c = D.cellFor('/list/1/deep'); c.cellElement(); var l = D.cellFor('/list'); l.cellElement();");
        act("D.set({ list: [{ deep: 'y' }], gone: 1 });");
        assertTrue(evalBool("D.isOpen('') && D.isOpen('/list') && !D.isOpen('/list/1') && D.cellCount() === 1"),
                "the folds kept where a container still stands, the one at a vanished pointer dropped; one cell survives");
        assertTrue(evalBool("D.cellFor('/list') === l && l.text() === 'list: [1]'"),
                "a cell whose pointer still resolves is the same cell, set to the new value — the tree keeps it, so the document must");
        assertEquals(":0:open /list:1:open /list/0:2:closed /gone:1:leaf", evalString("outline(D)"));
        assertTrue(evalBool("""
                (function () { try { D.cellFor('/list/1/deep'); return false; } catch (e) { return true; } })()
                && D.cellFor('/list/0/deep') !== c && D.cellFor('/list/0/deep').text() === 'deep: "y"'"""),
                "a cell asked for again is a new one, over the new value");
        // A root that stops being a container, then is one again: meant open, so open again.
        assertEquals(":0:leaf", evalString("D.set(7); outline(D)"));
        assertEquals(":0:open /a:1:closed", evalString("D.set({ a: { b: 1 } }); outline(D)"));
        // Closed on purpose, it stays closed through a new value.
        assertEquals(":0:closed", evalString("D.close(''); D.set({ b: 2 }); outline(D)"));
    }

    @Test
    void openToOpensTheAncestorsOpenAllEveryContainerToADepthAndDisposeLetsGo() {
        act(HELPERS + "var D = doc(SAMPLE);");
        assertTrue(evalBool("D.openTo('/list/1/deep') && D.isOpen('') && D.isOpen('/list') && D.isOpen('/list/1') && !D.isOpen('/list/1/deep')"),
                "the ancestors opened, the node itself left as it is");
        assertTrue(evalBool("outline(D).indexOf('/list/1/deep:3:leaf') > 0 && !D.openTo('/zz') && !D.openTo('/list/1/deep/x')"));
        assertEquals(":0:closed", evalString("D.closeAll(); outline(D)"));
        assertTrue(evalBool("D.openAll(1); D.isOpen('') && D.isOpen('/list') && !D.isOpen('/list/1')"), "to a depth");
        assertTrue(evalBool("D.openAll(); D.isOpen('/list/1') && !D.isOpen('/a~1b') && !D.isOpen('/empty')"), "all of them; an empty container never");
        assertTrue(evalBool("D.cellFor('/list/0'); D.cellCount() === 1 && (D.dispose(), D.cellCount() === 0)"), "dispose lets the cells go");
    }
}
