package hue.captains.singapura.js.homing.jsonkit;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The viewer, whole: a value drawn as rows through the tree; the fold by
 * caret, key and verb; a new value keeping the folds and the cursor by
 * pointer; a navigator's move; the reports; a clean teardown.
 *
 * <p>GraalVM drains microtasks between evals, not within one, so a fold the
 * tree asked for acts in one eval and is asserted in the next.</p>
 */
class JsonTreeViewTest extends JsonKitTestBase {

    @Test
    void aValueIsDrawnAsRowsTheRootOpenAndEveryContainerClosed() {
        act("var F = jsonFixture();");
        assertEquals("▾$: {7}@0 | name: \"kit\"@1 | n: 3@1 | ok: true@1 | none: null@1 | ▸list: [2]@1 | a/b: {}@1 | empty: []@1",
                evalString("F.drawn()"), "every member a row after the root, indented by depth, the caret by fold");
        assertEquals("jk-string jk-number jk-literal jk-literal jk-container jk-container",
                evalString("[1, 2, 3, 4, 5, 7].map(F.valueClass).join(' ')"), "the value wears its kind");
        assertEquals("jk-key", evalString("F.cellEl(6).children[0].className"));
        // The view is the tree's host: exactly two branches beneath its own, and nothing minted on it.
        assertEquals("document,tree", evalString("F.branch.listBranches().slice().sort().join(',')"));
        assertEquals(0, js.eval("js", "F.branch.elementCount").asInt());
    }

    @Test
    void unfoldByCaretByKeyAndByVerbAndTheCursorByPointer() {
        act("var F = jsonFixture(); F.clickCaret(5);");                       // asked of the document; answered on a microtask
        assertTrue(evalBool("F.drawn().indexOf('▾list: [2]@1 | 0: 1@2 | ▸1: {1}@2 | a/b') > 0 && F.view.cursor() === '/list'"),
                "a caret press lands the cursor and unfolds; the elements are rows, indices as names");
        assertEquals("jk-index", evalString("F.cellEl(6).children[0].className"));
        act("F.click(7); F.key('ArrowRight');");
        assertTrue(evalBool("F.shown().indexOf('/list/1 /list/1/deep /a~1b') > 0 && F.view.cursor() === '/list/1'"),
                "→ on a closed node asks the unfold");
        act("F.key('ArrowLeft');");
        assertTrue(evalBool("F.shown().indexOf('/list/1 /a~1b') > 0 && F.view.cursor() === '/list/1'"), "← on an open node folds it");
        // The verb: the document changed and the tree told, at once — the cursor by pointer where it can be.
        act("F.view.close('/list');");
        assertEquals(" /name /n /ok /none /list /a~1b /empty", evalString("F.shown()"));
        assertEquals("/empty", evalString("F.view.cursor()"), "the row the cursor was on is gone: the tree falls back to the position");
        act("F.view.open('/list'); F.click(7);");
        assertEquals("/list/1", evalString("F.view.cursor()"));
    }

    @Test
    void setKeepsTheFoldsAndTheCursorByPointerAndRedrawsTheRest() {
        act("var F = jsonFixture(); F.view.open('/list'); F.click(6);");
        assertEquals("/list/0", evalString("F.view.cursor()"));
        act("F.view.set({ name: 'kit2', list: [1, 2, 3], extra: true });");
        assertEquals("▾$: {3}@0 | name: \"kit2\"@1 | ▾list: [3]@1 | 0: 1@2 | 1: 2@2 | 2: 3@2 | extra: true@1", evalString("F.drawn()"),
                "a new value: the list still open, every row drawn from the new value");
        assertEquals("/list/0", evalString("F.view.cursor()"), "the cursor kept by pointer");
        assertTrue(evalBool("F.arranged.length >= 3"), "every presentation pass is reported: the first, the open, the set");
        act("F.view.set('just a string');");
        assertEquals("$: \"just a string\"@0", evalString("F.drawn()"), "a scalar root is one leaf row");
        assertEquals("", evalString("F.view.cursor()"), "the pointer gone, the cursor falls back to the row");
        act("F.view.set({ a: { b: 1 } });");
        assertEquals("▾$: {1}@0 | ▸a: {1}@1", evalString("F.drawn()"), "a container again: the root open as it was meant");
        assertTrue(evalBool("F.view.value().a.b === 1 && F.view.valueAt('/a/b') === 1 && F.view.valueAt('/zz') === undefined"));
    }

    @Test
    void selectPointerOpensThePathAndOpenAllCloseAllAreTheDocumentsFolds() {
        act("var F = jsonFixture(); var ok = F.view.selectPointer('/list/1/deep');");
        assertTrue(evalBool("ok && F.shown().indexOf('/list /list/0 /list/1 /list/1/deep /a~1b') > 0 && F.view.cursor() === '/list/1/deep'"
                          + " && F.moves[F.moves.length - 1] === '/list/1/deep'"),
                "the ancestors opened, the tree told, the cursor on the node and reported");
        assertTrue(evalBool("!F.view.selectPointer('/zz') && F.view.cursor() === '/list/1/deep'"), "no such node: false, nothing moved");
        assertEquals("▸$: {7}@0", evalString("F.view.closeAll(); F.drawn()"));
        assertTrue(evalBool("F.view.openAll(1); F.shown() === ' /name /n /ok /none /list /list/0 /list/1 /a~1b /empty'"));
        assertTrue(evalBool("F.view.openAll(); F.shown().indexOf('/list/1 /list/1/deep') > 0"));
    }

    @Test
    void enterAndADoubleClickReportThePointerAndDestroyLeavesNothingOfTheViews() {
        act("var F = jsonFixture(); F.click(1); F.key('Enter'); F.dblclick(2);");
        assertEquals("/name,/n", evalString("F.activated.join(',')"), "Enter and a double-click reach the node, reported by pointer");
        act("F.view.destroy();");
        assertTrue(evalBool("F.container.children.length === 0 && F.branch.listBranches().length === 0"),
                "the tree gone from the container; both branches beneath the view's dissolved");
    }
}
