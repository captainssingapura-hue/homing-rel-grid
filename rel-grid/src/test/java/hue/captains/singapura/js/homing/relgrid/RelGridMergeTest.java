package hue.captains.singapura.js.homing.relgrid;

import hue.captains.singapura.js.homing.ssjs.test.JsModuleTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Merged cells, first phase: a <b>logical</b> overlay on a matrix that stays
 * whole. A cell answering {@code colSpan() > 1} is a leading cell; the grid
 * unclips its slot and drops the grid lines it reaches across, and the cell
 * draws itself that wide. Nothing else changes — every position keeps its
 * slot and its own cell, and the cursor, the selection and copy are exactly
 * as they were. Behind {@code mergedCells}, off by default.
 */
class RelGridMergeTest extends JsModuleTestBase {

    /** A relation whose middle cell on the first row reaches over the next two. */
    private static final String SPANNING = """
            function spanning(opts) {
                opts = opts || {};
                var asked = [], spans = { 'r0 b': 3 };          // r0/b reaches over c and d
                var relation = {
                    pks:     function () { return ['r0', 'r1']; },
                    columns: function () { return ['a', 'b', 'c', 'd', 'e']; },
                    cellFor: function (pk, col) {
                        asked.push(pk + ' ' + col);
                        var key = pk + ' ' + col;
                        return {
                            _el: null,
                            render:  function (host) { this._el = host; host.textContent = key; },
                            onSelect: function () {},
                            dispose: function () {},
                            colSpan: function () { return spans[key] || 1; }
                        };
                    }
                };
                var branch = { createElement: function (n, t) { return makeEl(t); } };
                var container = makeEl('div');
                var grid = new RelGrid({ container: container, branch: branch, relation: relation,
                                         mergedCells: opts.mergedCells === true });
                function table() { return container.children[0].children[0]; }
                function tbody() { var t = table(); for (var k = 0; k < t.children.length; k++) if (t.children[k].tagName === 'tbody') return t.children[k]; }
                function td(i, j) { return tbody().children[i].children[j]; }
                function has(el, c) { return (el.className || '').split(' ').indexOf(c) >= 0; }
                function key(k, mods) { mods = mods || {}; table().dispatch('keydown', { key: k, shiftKey: !!mods.shift, ctrlKey: !!mods.ctrl }); }
                return { grid: grid, asked: asked, spans: spans, td: td, has: has, key: key, table: table };
            }
            """;

    @BeforeEach
    void setup() {
        js = buildContext();
        js.eval("js", RelGridTestDom.DOM_STUB);
        loadModule(RelGridTestDom.PROTOCOL);
        loadModule(RelGridTestDom.SELECTION);
        for (String m : RelGridTestDom.MODULES) loadModule(RelGridTestDom.DIR + m);
        js.eval("js", SPANNING);
    }

    private boolean evalBool(String expr) { return js.eval("js", expr).asBoolean(); }

    @Test
    void offByDefaultAColSpanIsNeverEvenRead() {
        assertTrue(evalBool("""
                (() => {
                    var f = spanning();                                     // mergedCells not given
                    for (var j = 0; j < 5; j++) if (f.has(f.td(0, j), 'hrg-lead') || f.has(f.td(0, j), 'hrg-covered')) return false;
                    return f.td(0, 1).style.getPropertyValue('--hrg-span') === '';
                })()"""), "without mergedCells a cell's colSpan changes nothing");
    }

    @Test
    void aLeadingCellIsMarkedWithItsReachAndTheMatrixStaysWhole() {
        assertTrue(evalBool("""
                (() => {
                    var f = spanning({ mergedCells: true });
                    // The leading slot carries its span; the two it reaches over are marked,
                    // the last of them as the group's end. Nothing else on the row is touched.
                    if (!f.has(f.td(0, 1), 'hrg-lead') || f.td(0, 1).style.getPropertyValue('--hrg-span') !== '3') return false;
                    if (!f.has(f.td(0, 2), 'hrg-covered') || f.has(f.td(0, 2), 'hrg-group-end')) return false;
                    if (!f.has(f.td(0, 3), 'hrg-covered') || !f.has(f.td(0, 3), 'hrg-group-end')) return false;
                    if (f.has(f.td(0, 0), 'hrg-lead') || f.has(f.td(0, 4), 'hrg-covered') || f.has(f.td(1, 1), 'hrg-lead')) return false;
                    // The matrix is WHOLE: every position has its slot, every identity was
                    // asked for and placed — the covered ones included.
                    if (f.asked.length !== 10) return false;
                    if (f.td(0, 2).children.length !== 1 || f.td(0, 2).children[0].textContent !== 'r0 c') return false;
                    return f.td(0, 3).children[0].textContent === 'r0 d';
                })()"""), "a leading cell's slot is marked with its reach; the covered slots keep their own cells");
    }

    @Test
    void everyFunctionIsExactlyAsBefore() {
        assertTrue(evalBool("""
                (() => {
                    var f = spanning({ mergedCells: true });
                    // The cursor steps through the covered slots one by one, and vertical
                    // moves keep the column: a covered slot is a slot.
                    f.grid.selectCell('r0', 'b');
                    f.key('ArrowRight');
                    if (f.grid.cursor().column !== 'c') return false;
                    f.key('ArrowRight');
                    if (f.grid.cursor().column !== 'd') return false;
                    f.key('ArrowDown');
                    if (f.grid.cursor().pk !== 'r1' || f.grid.cursor().column !== 'd') return false;
                    f.key('ArrowUp');
                    if (f.grid.cursor().column !== 'd') return false;
                    // A selection is the rectangle it is: extending from a covered slot widens
                    // to nothing else, and the covered slot paints its own wash.
                    f.key('ArrowLeft', { shift: true });
                    var r = f.grid.selectedRanges();
                    if (r.length !== 1 || r[0].j0 !== 2 || r[0].j1 !== 3) return false;
                    if (!f.has(f.td(0, 2), 'hrg-sel') || !f.has(f.td(0, 3), 'hrg-sel') || f.has(f.td(0, 1), 'hrg-sel')) return false;
                    // And the cursor stayed where it was (law 39), painted on its covered slot.
                    return f.has(f.td(0, 3), 'hrg-cursor') && f.grid.cursor().column === 'd';
                })()"""), "cursor, selection and painting are untouched: a covered slot is a slot");
    }

    @Test
    void spansAreReadAfreshOnEveryArrangementAndClampedToTheRow() {
        assertTrue(evalBool("""
                (() => {
                    var f = spanning({ mergedCells: true });
                    // The span moves: the cell now says 1, and another cell says 9 — more
                    // than the row has left, so it is clamped to the row's end.
                    f.spans['r0 b'] = 1;
                    f.spans['r1 d'] = 9;
                    f.grid.reapply();
                    if (f.has(f.td(0, 1), 'hrg-lead') || f.has(f.td(0, 2), 'hrg-covered')) return false;
                    if (!f.has(f.td(1, 3), 'hrg-lead') || f.td(1, 3).style.getPropertyValue('--hrg-span') !== '2') return false;
                    if (!f.has(f.td(1, 4), 'hrg-covered') || !f.has(f.td(1, 4), 'hrg-group-end')) return false;
                    // Nonsense is a plain cell: nothing, a string, one, a throw.
                    f.spans['r1 d'] = 'wide'; f.grid.reapply();
                    if (f.has(f.td(1, 3), 'hrg-lead')) return false;
                    f.spans['r0 a'] = 1; f.grid.reapply();
                    if (f.has(f.td(0, 0), 'hrg-lead')) return false;
                    return f.asked.length === 10;                                // still asked once each
                })()"""), "a span is arrangement: read on every pass, clamped, and nonsense is one");
    }
}
