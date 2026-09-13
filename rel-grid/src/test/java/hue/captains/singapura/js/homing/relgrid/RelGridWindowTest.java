package hue.captains.singapura.js.homing.relgrid;

import hue.captains.singapura.js.homing.ssjs.test.JsModuleTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * RFC 0050 · Episode 2 — the WINDOW: a relation whose View is a window of a
 * larger space, and a grid that never learns there is anything beyond what it
 * shows. The relation answers {@code view()} with W rows and {@code view({ by })}
 * with the window moved — or nothing at its ends. The grid asks at the edges
 * of its own space — an arrow with nowhere to go, the wheel, the page keys —
 * and arranges what comes back on the same slots. Everything here is
 * {@code view(intent)}, intent in and keys out; the grid holds W keys and
 * W × columns cells whatever it has scrolled past, and a static relation
 * (the fixture) answers nothing and is exactly as it was.
 */
class RelGridWindowTest extends JsModuleTestBase {

    /** A window of W rows over N: view() is the window, view({ by }) moves it, clamped; nothing at an end. */
    private static final String WINDOWED = """
            function windowed(opts) {
                opts = opts || {};
                var N = opts.rows || 100, W = opts.window || 5, at = 0, asked = [], moves = [];
                var cellsB = hostBranch(), seq = 0, cells = new Map();
                function keys() { var o = []; for (var r = at; r < at + W; r++) o.push('r' + r); return o; }
                var relation = {
                    view: function (intent) {
                        if (!intent) return keys();
                        moves.push(intent.by);
                        var next = Math.max(0, Math.min(N - W, at + intent.by));
                        if (next === at) return null;                     // nowhere to go: the rows stay
                        at = next;
                        return keys();
                    },
                    columns: function () { return ['a', 'b']; },
                    cellFor: function (pk, col) {
                        var n = Number(String(pk).slice(1));
                        if (!/^r\\d+$/.test(pk) || n >= N) throw new Error('no such row: ' + pk);
                        asked.push(pk + ' ' + col);
                        var k = pk + ' ' + col, c = cells.get(k);
                        if (!c) { c = new RelGridTextCell({ branch: cellsB.createBranch('c' + (++seq)), value: pk + col }); cells.set(k, c); }
                        return c;
                    }
                };
                var container = makeEl('div'), branch = testBranch(), edges = [];
                var grid = new RelGrid({ container: container, branch: branch, relation: relation, onEdge: function (d) { edges.push(d); } });
                function table() { return container.children[0].children[0]; }
                function tbody() { var t = table(); for (var k = 0; k < t.children.length; k++) if (t.children[k].tagName === 'tbody') return t.children[k]; return null; }
                function td(i, j) { return tbody().children[i].children[j]; }
                function shown() { return grid.viewMaps().rowView().join(','); }
                function key(k, mods) { mods = mods || {}; var ev = { key: k, shiftKey: !!mods.shift, prevented: false }; ev.preventDefault = function () { ev.prevented = true; }; table().dispatch('keydown', ev); return ev.prevented; }
                function wheel(dy, mode) { var ev = { deltaY: dy, deltaMode: mode || 0, prevented: false }; ev.preventDefault = function () { ev.prevented = true; }; table().dispatch('wheel', ev); return ev.prevented; }
                function click(i, j) { td(i, j).dispatch('mousedown', {}); document.dispatch('mouseup', {}); td(i, j).dispatch('click', {}); }
                return { grid: grid, relation: relation, branch: branch, cellsBranch: cellsB, container: container,
                         table: table, tbody: tbody, td: td, shown: shown, key: key, wheel: wheel, click: click, edges: edges,
                         asked: function () { return asked.length; }, moves: moves, at: function () { return at; } };
            }
            """;

    @BeforeEach
    void setup() {
        js = buildContext();
        js.eval("js", RelGridTestDom.DOM_STUB);
        js.eval("js", RelGridTestDom.STYLES);
        for (String m : RelGridTestDom.PARTY) loadModule(m);
        loadModule(RelGridTestDom.SELECTION);
        loadModule(RelGridTestDom.PROTOCOL);
        for (String m : RelGridTestDom.MODULES) loadModule(RelGridTestDom.DIR + m);
        js.eval("js", RelGridTestDom.FIXTURE);
        js.eval("js", WINDOWED);
    }

    private boolean evalBool(String expr) { return js.eval("js", expr).asBoolean(); }

    @Test
    void theWindowMovesByAskingTheRelationAndTheGridHoldsOnlyWhatItShows() {
        assertTrue(evalBool("""
                (() => {
                    var w = windowed(), g = w.grid, b = w.branch;
                    if (w.shown() !== 'r0,r1,r2,r3,r4' || w.asked() !== 10 || g.cells().size() !== 10) return false;
                    var tbody = w.tbody(), td = w.td(0, 0), slots = b.getBranch('slots'), minted = slots.elementCount;
                    // Two rows on: the relation answered the window moved, the grid presented it on
                    // the SAME slots, asked for the two rows that came in, and forgot the two that went.
                    if (g.scrollRows(2) !== true || w.shown() !== 'r2,r3,r4,r5,r6') return false;
                    if (w.tbody() !== tbody || w.td(0, 0) !== td || b.getBranch('slots') !== slots || slots.elementCount !== minted) return false;
                    if (w.asked() !== 14 || g.cells().size() !== 10) return false;
                    if (g.cells().get('r0', 'a') !== null || g.cells().get('r6', 'b') === null) return false;
                    if (td.children.length !== 1 || td.children[0].textContent !== 'r2a') return false;
                    // Far back is clamped by the relation; at the start, nothing: the rows stay.
                    if (g.scrollRows(-100) !== true || w.shown() !== 'r0,r1,r2,r3,r4') return false;
                    if (g.scrollRows(-1) !== false || w.shown() !== 'r0,r1,r2,r3,r4') return false;
                    if (g.scrollRows(0) !== false) return false;
                    // The relation was asked exactly these movements, and nothing else about its space.
                    if (w.moves.join() !== '2,-100,-1') return false;
                    // r0 came back: asked for again — the relation kept the cell, the same one returns.
                    return w.asked() === 18 && g.cells().size() === 10 && typeof w.relation.pks === 'undefined';
                })()"""), "scrollRows asks view({ by }); the window is arranged on the same slots and the grid holds W x columns");
    }

    @Test
    void anArrowAtTheWindowsEdgeMovesTheWindowAndStepsOntoTheNewRow() {
        assertTrue(evalBool("""
                (() => {
                    var w = windowed(), g = w.grid;
                    w.click(4, 0);                                       // r4 / a: the bottom row
                    if (g.cursor().pk !== 'r4') return false;
                    // Down at the bottom: the window moves one row, the cursor's cell is one row
                    // in, and the arrow steps onto the new bottom row. No edge was met.
                    if (!w.key('ArrowDown')) return false;
                    if (w.shown() !== 'r1,r2,r3,r4,r5' || g.cursor().pk !== 'r5' || w.edges.length !== 0) return false;
                    if (!css.hasClass(w.td(4, 0), hrg_cursor) || css.hasClass(w.td(3, 0), hrg_cursor)) return false;
                    // Up from the top: the same, backwards.
                    w.click(0, 0);
                    if (!w.key('ArrowUp') || w.shown() !== 'r0,r1,r2,r3,r4' || g.cursor().pk !== 'r0' || w.edges.length !== 0) return false;
                    if (w.key('ArrowUp') !== true || w.edges.join() !== 'up' || g.cursor().pk !== 'r0') return false;   // the true top: reported, consumed
                    // A side edge never asks the relation.
                    var before = w.moves.length;
                    w.key('ArrowLeft');
                    if (w.edges.join() !== 'up,left' || w.moves.length !== before) return false;
                    // At the very end the relation answers nothing and the edge is reported.
                    g.scrollRows(1000);
                    if (w.shown() !== 'r95,r96,r97,r98,r99') return false;
                    w.click(4, 1);
                    if (!w.key('ArrowDown') || w.edges.join() !== 'up,left,down' || g.cursor().pk !== 'r99') return false;
                    // Shift+arrow at the edge is an extension, never a movement of the window.
                    w.click(0, 0); w.key('ArrowUp', { shift: true });
                    return w.shown() === 'r95,r96,r97,r98,r99' && g.selectionCount() === 1;
                })()"""), "an arrow at the top or bottom edge moves the window and steps onto the new row; the true ends are reported");
    }

    @Test
    void theWheelAndThePageKeysMoveTheWindowAndAreLeftToTheBrowserWhenNothingAnswers() {
        assertTrue(evalBool("""
                (() => {
                    var w = windowed(), g = w.grid;
                    // Lines: three rows, consumed.
                    if (w.wheel(3, 1) !== true || w.shown() !== 'r3,r4,r5,r6,r7') return false;
                    // Pixels against the row height — the slot's, 20 here: 30px is a row and a
                    // half; the half is carried, and the next 30px make three rows in all.
                    if (w.wheel(30, 0) !== true || w.shown() !== 'r4,r5,r6,r7,r8') return false;
                    if (w.wheel(30, 0) !== true || w.shown() !== 'r6,r7,r8,r9,r10') return false;
                    // A sub-row tick, once the window has moved under the wheel, is consumed and carried.
                    if (w.wheel(5, 0) !== true || w.shown() !== 'r6,r7,r8,r9,r10') return false;
                    // Back past the start: clamped by the relation.
                    if (w.wheel(-50, 1) !== true || w.shown() !== 'r0,r1,r2,r3,r4') return false;
                    // At the start, up answers nothing: not consumed — the browser scrolls what it will.
                    if (w.wheel(-3, 1) !== false || w.shown() !== 'r0,r1,r2,r3,r4') return false;
                    // Page keys: the window's height of rows.
                    w.click(2, 0);
                    if (!w.key('PageDown') || w.shown() !== 'r5,r6,r7,r8,r9') return false;
                    if (g.cursor().pk !== 'r7') return false;            // its row left: the position stands in
                    if (!w.key('PageUp') || w.shown() !== 'r0,r1,r2,r3,r4') return false;
                    if (w.key('PageUp') !== false) return false;         // nothing answered: not consumed
                    // A static relation is exactly as it was: nothing moves, nothing is consumed,
                    // and the arrow at the bottom is the edge it always was.
                    var f = fixture();
                    var ev = { deltaY: 3, deltaMode: 1, prevented: false }; ev.preventDefault = function () { ev.prevented = true; };
                    f.table().dispatch('wheel', ev);
                    if (ev.prevented || f.grid.scrollRows(1) !== false) return false;
                    f.click(2, 0); f.key('ArrowDown');
                    return f.edges.join() === 'down' && f.grid.cursor().pk === 'fish';
                })()"""), "the wheel and the page keys ask the relation; a static relation answers nothing and keeps the browser's own scrolling");
    }

    @Test
    void theWindowIsRefusedWhileLocked() {
        assertTrue(evalBool("""
                (() => {
                    var w = windowed(), g = w.grid;
                    var cell = w.relation.cellFor('r0', 'a');
                    cell.mayTakeControl = function () { return true; };
                    cell.editorElement = function () { return makeEl('input'); };
                    cell.takeControl = function () { return new Promise(function () {}); };   // never settles: deep for good
                    w.click(0, 0);
                    if (!g.takeControlAtCursor() || !g.isDeep()) return false;
                    if (g.scrollRows(1) !== false || w.wheel(3, 1) !== false || w.key('PageDown') !== false) return false;
                    return w.shown() === 'r0,r1,r2,r3,r4' && w.moves.length === 0;
                })()"""), "law 221: the window is an intent, refused while a cell is deep");
    }
}
