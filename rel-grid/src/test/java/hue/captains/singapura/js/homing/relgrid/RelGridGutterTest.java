package hue.captains.singapura.js.homing.relgrid;

import hue.captains.singapura.js.homing.ssjs.test.JsModuleTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The GUTTER: row numbers as a leading column that is the grid's own — a
 * position each, painted when the slot is minted and kept by the slot across
 * a remap; not in the column view, so nothing addressed by column sees it; a
 * press on a number selects the row.
 *
 * <p>The fixture's slot helpers count children from the first cell of a row;
 * with a gutter that is the number, so this test reads the slots itself.</p>
 */
class RelGridGutterTest extends JsModuleTestBase {

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
                function gutterOf(F, i) { return F.tbody().children[i].children[0]; }
                function numbers(F) { var out = []; for (var i = 0; i < F.tbody().children.length; i++) out.push(gutterOf(F, i).textContent); return out.join(','); }
                function slotOf(F, i, j) { return F.tbody().children[i].children[j + 1]; }
                function hasClass(el, c) { return (el.className || '').split(' ').indexOf(c) >= 0; }
                function clickSlot(F, i, j) { slotOf(F, i, j).dispatch('mousedown', {}); document.dispatch('mouseup', {}); slotOf(F, i, j).dispatch('click', {}); }
                function paintedWithGutter(F) {
                    var out = [], rows = F.grid.viewMaps().rows(), cols = F.grid.viewMaps().cols();
                    for (var a = 0; a < rows; a++) for (var b = 0; b < cols; b++) if (hasClass(slotOf(F, a, b), 'hrg-sel')) out.push(a + ',' + b);
                    return out.join(' ');
                }
                """);
    }

    private boolean evalBool(String expr) { return js.eval("js", expr).asBoolean(); }

    @Test
    void offByDefaultAndOnItIsALeadingColumnOfPositionsTheGridOwns() {
        assertTrue(evalBool("""
                (() => {
                    var P = fixture();
                    if (P.tbody().children[0].children.length !== 2 || P.headerRow().children.length !== 2) return false;
                    var F = fixture({ rowNumbers: true });
                    // A number cell before the slots, in every row and in the header; the column view untouched.
                    if (F.tbody().children[0].children.length !== 3 || F.headerRow().children.length !== 3) return false;
                    if (F.grid.viewMaps().cols() !== 2 || F.grid.viewMaps().rows() !== 3) return false;
                    if (numbers(F) !== '1,2,3') return false;
                    var g = gutterOf(F, 0), h = F.headerRow().children[0];
                    if (g.tagName !== 'th' || !hasClass(g, 'hrg-gutter') || hasClass(g, 'hrg-td')) return false;
                    if (!hasClass(h, 'hrg-gutter') || !hasClass(h, 'hrg-gutter-head') || h.textContent !== '') return false;
                    // The slots are where they were and hold the cells: the gutter is not a slot.
                    if (slotOf(F, 0, 0).children[0] !== F.relation.cell('mapo', 'ingredient').cellElement()) return false;
                    // The number is a POSITION: slots kept across a remap keep their numbers; the keys move.
                    var g0 = gutterOf(F, 0);
                    F.relation.show(['fish', 'mapo', 'coq']); F.grid.tell(new RelGridViewChanged());
                    if (numbers(F) !== '1,2,3' || F.rowsShown() !== 'fish,mapo,coq' || gutterOf(F, 0) !== g0) return false;
                    if (slotOf(F, 0, 0).children[0] !== F.relation.cell('fish', 'ingredient').cellElement()) return false;
                    // A shorter View: the matrix minted afresh, the numbers 1 to n again.
                    F.relation.show(['coq']); F.grid.tell(new RelGridViewChanged());
                    return numbers(F) === '1' && F.tbody().children[0].children.length === 3;
                })()"""), "the gutter is the grid's own leading column of positions, off by default");
    }

    @Test
    void thePartyCountsTheGutterAndNothingAddressedByColumnSeesIt() {
        assertTrue(evalBool("""
                (() => {
                    var F = fixture({ rowNumbers: true }), b = F.branch;
                    var slots = b.getBranch('slots'), rows = F.grid.viewMaps().rows(), cols = F.grid.viewMaps().cols();
                    // The matrix as before, plus the gutter's col, its corner, and a number cell a row.
                    if (slots.elementCount !== (cols + cols + 1 + rows + rows * cols + cols) + (1 + 1 + rows)) return false;
                    // The cursor never enters the gutter: the leftmost slot is the edge.
                    clickSlot(F, 0, 0);
                    F.key('ArrowLeft');
                    if (F.edges.join() !== 'left' || F.grid.cursor().column !== 'ingredient') return false;
                    // Widths, header cells and labels are by column; the gutter is none — the fixture's
                    // helpers count from the first cell, which is the number here, so the header is read by hand.
                    if (F.headerRow().children[1].textContent !== 'ingredient' || F.grid.columnWidth('ingredient') !== null) return false;
                    F.grid.setColumnWidth('ingredient', 120);
                    var cols_ = F.table().children[0];                                 // the colgroup: the gutter's col, then the columns'
                    if (cols_.children.length !== 3 || cols_.children[1].style.getPropertyValue('--hrg-col-w') !== '120px') return false;
                    return F.grid.columnWidth('ingredient') === 120 && hasClass(F.headerRow().children[0], 'hrg-gutter');
                })()"""), "the gutter is counted by the party and invisible to everything addressed by column");
    }

    @Test
    void aPressOnANumberSelectsTheRow() {
        assertTrue(evalBool("""
                (() => {
                    var F = fixture({ rowNumbers: true });
                    gutterOf(F, 1).dispatch('click', {});
                    // The cursor on the row's first slot, and one range across the row.
                    if (F.grid.cursor().pk !== 'coq' || F.grid.cursor().column !== 'ingredient') return false;
                    if (F.grid.selectionCount() !== 1 || paintedWithGutter(F) !== '1,0 1,1') return false;
                    if (F.told() !== '1,0..1,1') return false;
                    // Another number: a new row, the old range gone.
                    gutterOf(F, 2).dispatch('click', {});
                    return F.grid.cursor().pk === 'fish' && paintedWithGutter(F) === '2,0 2,1';
                })()"""), "a press on a row's number lands the cursor on the row and selects it across");
    }
}
