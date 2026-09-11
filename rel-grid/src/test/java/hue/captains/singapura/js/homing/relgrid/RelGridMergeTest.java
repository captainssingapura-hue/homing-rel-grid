package hue.captains.singapura.js.homing.relgrid;

import hue.captains.singapura.js.homing.ssjs.test.JsModuleTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Merged cells: a matrix that stays whole, and a cell laid over part of it.
 * A cell answering {@code colSpan() > 1} is a leading cell; the layout mints a
 * host over the n slots it reaches across and the cell is placed there. The
 * host mirrors the slots' state, so the group reads as one cell while the
 * tracker keeps the exact square: vertical moves pass through, horizontal
 * moves jump out, a selection is never widened, Enter offers the leading cell.
 * Behind {@code mergedCells}, off by default.
 */
class RelGridMergeTest extends JsModuleTestBase {

    /** A relation whose middle cell on the first row reaches over the next two; every cell can take control. */
    private static final String SPANNING = """
            function spanning(opts) {
                opts = opts || {};
                var asked = [], taken = [], spans = { 'r0 b': 3 };          // r0/b reaches over c and d
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
                            colSpan: function () { return spans[key] || 1; },
                            mayTakeControl: function () { return true; },
                            takeControl: function (host) { taken.push(key); host.textContent = 'editing ' + key; return new Promise(function () {}); }
                        };
                    }
                };
                var branch = { createElement: function (n, t) { return makeEl(t); } };
                var container = makeEl('div');
                var grid = new RelGrid({ container: container, branch: branch, relation: relation,
                                         mergedCells: opts.mergedCells === true });
                function wrap() { return container.children[0]; }
                function table() { return wrap().children[0]; }
                function tbody() { var t = table(); for (var k = 0; k < t.children.length; k++) if (t.children[k].tagName === 'tbody') return t.children[k]; }
                function td(i, j) { return tbody().children[i].children[j]; }
                function has(el, c) { return (el.className || '').split(' ').indexOf(c) >= 0; }
                function kids(el, c) { var out = []; for (var k = 0; k < el.children.length; k++) if (has(el.children[k], c)) out.push(el.children[k]); return out; }
                function groups() { return kids(wrap(), 'hrg-merge'); }
                function editor() { var e = kids(wrap(), 'hrg-edit'); return e.length ? e[0] : null; }
                function key(k, mods) { mods = mods || {}; table().dispatch('keydown', { key: k, shiftKey: !!mods.shift, ctrlKey: !!mods.ctrl }); }
                function at() { var c = grid.cursor(); return c ? c.pk + ' ' + c.column : null; }
                return { grid: grid, asked: asked, taken: taken, spans: spans, wrap: wrap, td: td, has: has,
                         groups: groups, editor: editor, key: key, at: at, table: table, container: container };
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
                    if (f.groups().length !== 0) return false;
                    for (var j = 0; j < 5; j++) if (f.has(f.td(0, j), 'hrg-lead') || f.has(f.td(0, j), 'hrg-covered')) return false;
                    return f.td(0, 1).children[0].textContent === 'r0 b';    // in its own slot, like any cell
                })()"""), "without mergedCells a cell's colSpan changes nothing");
    }

    @Test
    void aLeadingCellIsPlacedInAHostOverItsSlotsAndTheMatrixStaysWhole() {
        assertTrue(evalBool("""
                (() => {
                    var f = spanning({ mergedCells: true });
                    // One host in the wrapper, and the leading cell lives in it — not in its slot,
                    // which stays, empty. The covered slots keep their own cells.
                    var g = f.groups();
                    if (g.length !== 1 || g[0].parentNode !== f.wrap()) return false;
                    if (g[0].children.length !== 1 || g[0].children[0].textContent !== 'r0 b') return false;
                    if (f.td(0, 1).children.length !== 0) return false;
                    if (f.td(0, 2).children[0].textContent !== 'r0 c' || f.td(0, 3).children[0].textContent !== 'r0 d') return false;
                    if (f.asked.length !== 10) return false;                        // every identity, once
                    // The slots are marked as before.
                    if (!f.has(f.td(0, 1), 'hrg-lead') || !f.has(f.td(0, 2), 'hrg-covered') || !f.has(f.td(0, 3), 'hrg-group-end')) return false;
                    // Sized from the slots: the union of the first and the last, in the wrapper's
                    // coordinates. The stub's rects are driven by _rl / _rr.
                    f.td(0, 1)._rl = 100; f.td(0, 1)._rr = 200;
                    f.td(0, 3)._rl = 300; f.td(0, 3)._rr = 400;
                    f.grid.setColumnWidth('a', 50);                                  // any resize re-measures
                    var st = f.groups()[0].style;
                    return st.getPropertyValue('left') === '100px' && st.getPropertyValue('width') === '300px'
                        && st.getPropertyValue('height') === '20px';
                })()"""), "the leading cell is placed in a host over its slots; every slot keeps its cell");
    }

    @Test
    void theCursorPassesThroughVerticallyAndJumpsOutHorizontally() {
        assertTrue(evalBool("""
                (() => {
                    var f = spanning({ mergedCells: true });
                    var host = f.groups()[0];
                    // Down into the group keeps the column, onto a covered slot — the tracker
                    // is exact, and the whole group wears the cursor.
                    f.grid.selectCell('r1', 'd');
                    f.key('ArrowUp');
                    if (f.at() !== 'r0 d' || !f.has(host, 'hrg-cursor') || !f.has(f.td(0, 3), 'hrg-cursor')) return false;
                    f.key('ArrowDown');
                    if (f.at() !== 'r1 d' || f.has(host, 'hrg-cursor')) return false;
                    // Right from inside the group jumps to the slot after it; left from outside
                    // lands on the exact slot reached — the group's last — and left again jumps
                    // to the slot before the group.
                    f.grid.selectCell('r0', 'c');
                    f.key('ArrowRight');
                    if (f.at() !== 'r0 e') return false;
                    f.key('ArrowLeft');
                    if (f.at() !== 'r0 d' || !f.has(host, 'hrg-cursor')) return false;
                    f.key('ArrowLeft');
                    if (f.at() !== 'r0 a') return false;
                    f.key('ArrowRight');
                    return f.at() === 'r0 b' && f.has(host, 'hrg-cursor');
                })()"""), "vertical moves keep the column; horizontal moves leave the group whole");
    }

    @Test
    void aSelectionIsNeverWidenedAndTheGroupIsPaintedWhenTouched() {
        assertTrue(evalBool("""
                (() => {
                    var f = spanning({ mergedCells: true });
                    var host = f.groups()[0];
                    // From outside, Shift+Left lands on the group's last slot: the rectangle is
                    // d..e, not b..e, and the group is painted because one of its slots is in.
                    f.grid.selectCell('r0', 'e');
                    f.key('ArrowLeft', { shift: true });
                    var r = f.grid.selectedRanges();
                    if (r.length !== 1 || r[0].j0 !== 3 || r[0].j1 !== 4) return false;
                    if (!f.has(host, 'hrg-sel') || f.has(f.td(0, 1), 'hrg-sel') || !f.has(f.td(0, 3), 'hrg-sel')) return false;
                    // Extending again from inside the group jumps out of it: a..e.
                    f.key('ArrowLeft', { shift: true });
                    r = f.grid.selectedRanges();
                    if (r[0].j0 !== 0 || r[0].j1 !== 4) return false;
                    // A rectangle on the other row touches no group.
                    f.grid.selectCell('r1', 'c');
                    f.key('ArrowRight', { shift: true });
                    return !f.has(host, 'hrg-sel') && f.grid.selectedRanges()[0].i0 === 1;
                })()"""), "a selection is the rectangle it is; the group wears the wash when any slot of it is in");
    }

    @Test
    void enterInsideAGroupOffersTheLeadingCellOverTheWholeGroup() {
        assertTrue(evalBool("""
                (() => {
                    var f = spanning({ mergedCells: true });
                    f.td(0, 1)._rl = 100; f.td(0, 1)._rr = 200;
                    f.td(0, 3)._rl = 300; f.td(0, 3)._rr = 400;
                    // The cursor on a covered slot; Enter asks the LEADING cell, and the editor's
                    // anchor is the whole group's box.
                    f.grid.selectCell('r0', 'd');
                    if (!f.grid.mayTakeControlAtCursor()) return false;
                    if (!f.grid.takeControlAtCursor()) return false;
                    if (f.taken.join() !== 'r0 b' || !f.grid.isDeep()) return false;
                    var e = f.editor();
                    if (!e || e.style.getPropertyValue('left') !== '100px' || e.style.getPropertyValue('width') !== '300px') return false;
                    // And the cursor stayed where it was: the tracker is exact even now.
                    return f.at() === 'r0 d';
                })()"""), "Enter anywhere in a group offers the leading cell, over the group's whole box");
    }

    @Test
    void aHostThatMovesTheSlotsInTheResizeReportIsMeasuredAgain() {
        assertTrue(evalBool("""
                (() => {
                    // The stress table found this: a host that sizes its container to the
                    // widths now held does so INSIDE onColumnResized, after the grid measured
                    // the merged cells' hosts against the old geometry. So the grid measures
                    // again once the report has been made.
                    var f = spanning({ mergedCells: true });
                    f.td(0, 1)._rl = 100; f.td(0, 1)._rr = 200;
                    f.td(0, 3)._rl = 300; f.td(0, 3)._rr = 400;
                    f.grid.setColumnWidth('a', 50);
                    if (f.groups()[0].style.getPropertyValue('left') !== '100px') return false;
                    // Now a host whose report moves everything 40px to the left.
                    var g2 = spanning({ mergedCells: true });
                    g2.grid.destroy();
                    var moved = false;
                    var relation = { pks: function () { return ['r0']; }, columns: function () { return ['a', 'b', 'c']; },
                                     cellFor: function (pk, col) { return { render: function (h) { h.textContent = col; }, onSelect: function () {}, dispose: function () {},
                                                                              colSpan: function () { return col === 'a' ? 2 : 1; } }; } };
                    var container = makeEl('div');
                    var grid = new RelGrid({ container: container, branch: { createElement: function (n, t) { return makeEl(t); } },
                                             relation: relation, mergedCells: true,
                                             onColumnResized: function () {
                                                 // the container narrows; every slot shifts
                                                 var t = container.children[0].children[0], tb = null;
                                                 for (var k = 0; k < t.children.length; k++) if (t.children[k].tagName === 'tbody') tb = t.children[k];
                                                 var tds = tb.children[0].children;
                                                 tds[0]._rl = 60; tds[0]._rr = 110; tds[1]._rl = 110; tds[1]._rr = 160; moved = true;
                                             } });
                    var host = container.children[0].children[1];
                    if (!/hrg-merge/.test(host.className)) return false;
                    grid.setColumnWidth('c', 50);
                    // Measured after the host moved the slots: left 60, width 100 — not the
                    // stub's default 0 and 100 that stood before the report.
                    return moved && host.style.getPropertyValue('left') === '60px' && host.style.getPropertyValue('width') === '100px';
                })()"""), "the merged cells' hosts are measured again after the resize report, in case the host moved the slots");
    }

    @Test
    void groupsFollowEveryArrangementAndGoWithTheGrid() {
        assertTrue(evalBool("""
                (() => {
                    var f = spanning({ mergedCells: true });
                    var first = f.groups()[0];
                    // The span moves: the old host is gone, the cell is back in its slot, a new
                    // host stands over the new group, clamped to the row's end.
                    f.spans['r0 b'] = 1;
                    f.spans['r1 d'] = 9;
                    f.grid.reapply();
                    var g = f.groups();
                    if (g.length !== 1 || g[0] === first || first.parentNode !== null) return false;
                    if (f.td(0, 1).children[0].textContent !== 'r0 b') return false;
                    if (g[0].children[0].textContent !== 'r1 d' || f.td(1, 3).children.length !== 0) return false;
                    if (!f.has(f.td(1, 4), 'hrg-group-end')) return false;
                    // A covered cell's own span is ignored; nonsense is a plain cell.
                    f.spans['r1 e'] = 4; f.spans['r0 a'] = 'wide'; f.grid.reapply();
                    if (f.groups().length !== 1 || f.has(f.td(0, 0), 'hrg-lead')) return false;
                    if (f.asked.length !== 10) return false;
                    f.grid.destroy();
                    return f.container.children.length === 0;
                })()"""), "a span is arrangement: the hosts are re-minted with the slots, and go with the grid");
    }
}
