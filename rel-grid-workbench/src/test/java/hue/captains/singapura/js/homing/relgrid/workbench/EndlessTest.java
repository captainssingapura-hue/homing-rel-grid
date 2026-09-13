package hue.captains.singapura.js.homing.relgrid.workbench;

import hue.captains.singapura.js.homing.relgrid.RelGridTestDom;
import hue.captains.singapura.js.homing.ssjs.test.JsModuleTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The Endless Table bench: a relation whose View is a window over a space too
 * large to list, and the grid over it. The relation alone first — the window,
 * its ends, the stranger, the retention rule that frees a row outside two
 * Views, an edit that outlives its cell — and then the grid on top, with the
 * bench's own invariants read off the party: the grid's branch a constant on
 * the slots it started with, the domain's bounded at two windows, the
 * registry exactly the window, every slot holding one cell, after hundreds
 * of steps in every direction. Runs on the grid's own test DOM and the real
 * party.
 */
class EndlessTest extends JsModuleTestBase {

    private static final String BENCH_DIR = RelGridTestDom.DIR + "workbench/";

    private static final String HELPERS = """
            // The party's own count of a subtree: elements on the branch, and on every branch under it.
            function countTree(b) {
                var n = b.elementCount;
                b.listBranches().forEach(function (name) { n += countTree(b.getBranch(name)); });
                return n;
            }
            // A bench: exactly two branches under a host's, a relation on the domain's, a grid on its own.
            function bench(opts) {
                opts = opts || {};
                var hostB = hostBranch();
                var gridB = hostB.createBranch('grid'), domainB = hostB.createBranch('domain');
                domainB.activate({ toString: function () { return 'bench'; } });
                var relation = createEndlessRelation({ branch: domainB.createBranch('cells'), rows: opts.rows || 1000, window: opts.window || 5 });
                var container = makeEl('div'), edges = [];
                var grid = new RelGrid({ container: container, branch: gridB, relation: relation, onEdge: function (d) { edges.push(d); } });
                var W = relation.window(), cols = relation.columns().length;
                var slots0 = gridB.getBranch('slots'), grid0 = countTree(gridB);
                // The bench's invariants, as the widget reads them.
                function invariants() {
                    var bad = [];
                    if (countTree(gridB) !== grid0) bad.push('grid ' + countTree(gridB) + ' != ' + grid0);
                    if (gridB.getBranch('slots') !== slots0) bad.push('slots re-minted');
                    if (relation.held() > 2 * W) bad.push('held ' + relation.held());
                    if (grid.cells().size() !== W * cols) bad.push('registry ' + grid.cells().size());
                    var slots = gridB.getBranch('slots');
                    for (var i = 0; i < W; i++) for (var j = 0; j < cols; j++) {
                        var td = slots.getElement('td-' + i + '-' + j);
                        if (!td || td.children.length !== 1) bad.push('slot ' + i + ',' + j + ' has ' + (td ? td.children.length : 'no') + ' children');
                    }
                    // Every presented cell shows its own row: the cell in slot (i, id) reads the row's id.
                    for (var r = 0; r < W; r++) {
                        var pk = grid.viewMaps().pkAt(r), id = slots.getElement('td-' + r + '-0').children[0].textContent;
                        if (String(pk) !== 'r' + id) bad.push('slot ' + r + ' shows r' + id + ' for ' + pk);
                    }
                    return bad;
                }
                function table() { return container.children[0].children[0]; }
                function key(k) { var ev = { key: k, prevented: false }; ev.preventDefault = function () { ev.prevented = true; }; table().dispatch('keydown', ev); return ev.prevented; }
                function wheel(dy, mode) { var ev = { deltaY: dy, deltaMode: mode || 0, prevented: false }; ev.preventDefault = function () { ev.prevented = true; }; table().dispatch('wheel', ev); return ev.prevented; }
                function td(i, j) { return gridB.getBranch('slots').getElement('td-' + i + '-' + j); }
                function click(i, j) { td(i, j).dispatch('mousedown', {}); document.dispatch('mouseup', {}); td(i, j).dispatch('click', {}); }
                return { hostB: hostB, gridB: gridB, domainB: domainB, relation: relation, grid: grid, W: W, cols: cols,
                         invariants: invariants, key: key, wheel: wheel, td: td, click: click, edges: edges,
                         shown: function () { return grid.viewMaps().rowView().join(','); } };
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
        loadModule(BENCH_DIR + "EndlessRelation.js");
        js.eval("js", HELPERS);
    }

    private boolean evalBool(String expr) { return js.eval("js", expr).asBoolean(); }

    @Test
    void theRelationAnswersAWindowMovesItAndFreesARowOutsideTwoViews() {
        assertTrue(evalBool("""
                (() => {
                    var rel = createEndlessRelation({ branch: testBranch(), rows: 100, window: 5 });
                    if (typeof rel.pks !== 'undefined') return false;                    // one root: nothing lists
                    if (rel.view().join(',') !== 'r0,r1,r2,r3,r4' || rel.at() !== 0) return false;
                    if (rel.view({ by: -1 }) !== null) return false;                      // an end: nothing, the rows stay
                    if (rel.view({ by: 3 }).join(',') !== 'r3,r4,r5,r6,r7') return false;
                    if (rel.view({ by: 1000 }).join(',') !== 'r95,r96,r97,r98,r99') return false;   // clamped
                    if (rel.view({ by: 1 }) !== null) return false;
                    if (rel.columns().join(',') !== 'id,name,qty,price,note' || rel.readOnlyColumns().join() !== 'id') return false;
                    // A stranger, either way, is refused where a relation refuses.
                    try { rel.cellFor('r100', 'id'); return false; } catch (e) { if (!/no such row/.test(String(e))) return false; }
                    try { rel.cellFor('r1', 'nope'); return false; } catch (e) { if (!/no such column/.test(String(e))) return false; }
                    // Values are a function of the identity, minted on a sub-branch per row.
                    var c = rel.cellFor('r97', 'id');
                    if (c !== rel.cellFor('r97', 'id') || c.value() !== 97 || rel.cellFor('r97', 'name').value() !== 'Basil 0') return false;
                    if (rel.held() !== 1 || rel.cellCount() !== 2 || rel.mints() !== 2) return false;
                    // THE RETENTION RULE. Rows asked for while the window stands at 95: r95..r99.
                    for (var k = 95; k < 100; k++) rel.cellFor('r' + k, 'id');
                    if (rel.held() !== 5) return false;
                    // Back to the start: r95..r99 were in the View answered LAST, so they stay — the
                    // arranger still has them placed until it arranges what was just answered.
                    if (rel.view({ by: -1000 }).join(',') !== 'r0,r1,r2,r3,r4' || rel.held() !== 5 || rel.frees() !== 0) return false;
                    for (var k2 = 0; k2 < 5; k2++) rel.cellFor('r' + k2, 'id');
                    if (rel.held() !== 10) return false;                                 // two windows: the bound
                    // One more View, and r95..r99 are in neither: freed — cells disposed, branches gone.
                    var c95 = rel.cellFor('r95', 'id'), el95 = c95.cellElement();
                    if (rel.view({ by: 1 }).join(',') !== 'r1,r2,r3,r4,r5' || rel.held() !== 5 || rel.frees() !== 5) return false;
                    if (rel.heldRows().join() !== '0,1,2,3,4') return false;
                    if (rel.cellFor('r95', 'id') === c95) return false;                  // asked again: a new cell; the old is gone
                    return true;
                })()"""), "a window, its ends, a stranger, and a row freed the moment it is outside two Views");
    }

    @Test
    void anEditOutlivesItsCell() {
        assertTrue(evalBool("""
                (() => {
                    var rel = createEndlessRelation({ branch: testBranch(), rows: 100, window: 5 });
                    var qty = rel.cellFor('r2', 'qty'), before = qty.value();
                    qty.cellElement();                                        // placed, as far as the cell knows
                    if (qty.mayTakeControl() !== true) return false;
                    if (rel.cellFor('r2', 'id').cellElement() && rel.cellFor('r2', 'id').mayTakeControl() !== false) return false;   // id: no commit target
                    // The cell's own road: its editor, Enter. The relation keeps the commit by identity.
                    var input = qty.editorElement();
                    qty.takeControl();
                    input.value = '42';
                    input.dispatch('keydown', { key: 'Enter' });
                    if (qty.value() !== '42' || rel.edited('r2', 'qty') !== '42' || rel.edited('r3', 'qty') !== null) return false;
                    // The row is freed — two Views without it — and asked for again: the edit is still there.
                    rel.view({ by: 50 }); rel.view({ by: 1 });
                    if (rel.heldRows().indexOf(2) >= 0) return false;
                    var again = rel.cellFor('r2', 'qty');
                    return again !== qty && again.value() === '42' && before !== '42';
                })()"""), "a commit is the domain's, kept by identity, and shows again after the row was freed and re-asked");
    }

    @Test
    void theGridOverItHoldsAConstantOnItsSideAndABoundOnTheDomainsThroughHundredsOfSteps() {
        assertTrue(evalBool("""
                (() => {
                    var b = bench({ rows: 1000, window: 5 }), g = b.grid, rel = b.relation;
                    if (b.shown() !== 'r0,r1,r2,r3,r4' || b.invariants().length) return false;
                    if (b.hostB.listBranches().sort().join() !== 'domain,grid') return false;   // exactly two
                    var grid0 = countTree(b.gridB);
                    // Three hundred rows down, one at a time, checked every step.
                    for (var k = 0; k < 300; k++) {
                        if (!g.scrollRows(1)) return false;
                        var bad = b.invariants();
                        if (bad.length) { console.error('step ' + k + ': ' + bad.join('; ')); return false; }
                    }
                    if (rel.at() !== 300 || b.shown() !== 'r300,r301,r302,r303,r304') return false;
                    if (rel.mints() !== 305 * 5 || rel.frees() !== 299) return false;    // one row in, one out, per step; the last out is still within two Views
                    // A hundred pages, alternating; then random jumps; then to both ends.
                    for (var p = 0; p < 100; p++) { g.scrollRows(p % 2 ? -5 : 5); if (b.invariants().length) return false; }
                    for (var r = 0; r < 100; r++) { g.scrollRows(Math.floor((Math.random() - 0.5) * 3000)); if (b.invariants().length) return false; }
                    g.scrollRows(-100000); if (b.shown() !== 'r0,r1,r2,r3,r4' || b.invariants().length) return false;
                    g.scrollRows(100000);  if (b.shown() !== 'r995,r996,r997,r998,r999' || b.invariants().length) return false;
                    // The grid's side never changed by a single element; the domain's holds at most two windows.
                    return countTree(b.gridB) === grid0 && rel.held() <= 10;
                })()"""), "the grid's branch is a constant on the slots it started with; the domain's is bounded; the registry is the window");
    }

    @Test
    void theKeysAndTheWheelDriveItAndTheCursorRidesTheWindow() {
        assertTrue(evalBool("""
                (() => {
                    var b = bench({ rows: 1000, window: 5 }), g = b.grid;
                    b.click(4, 1);                                            // r4 / name: the bottom row
                    if (!b.key('ArrowDown') || b.shown() !== 'r1,r2,r3,r4,r5' || g.cursor().pk !== 'r5') return false;
                    if (b.invariants().length) return false;
                    if (!b.wheel(3, 1) || b.shown() !== 'r4,r5,r6,r7,r8' || g.cursor().pk !== 'r5') return false;   // its row is still shown: the cursor stays on it
                    if (!b.key('PageDown') || b.shown() !== 'r9,r10,r11,r12,r13') return false;
                    if (g.cursor().pk !== 'r10') return false;                // its row left: the position stands in
                    if (b.invariants().length) return false;
                    // Back to the start and past it: the true edge is reported, once.
                    g.scrollRows(-1000); b.click(0, 0);
                    if (b.key('ArrowUp') !== true || b.edges.join() !== 'up' || g.cursor().pk !== 'r0') return false;
                    if (b.wheel(-3, 1) !== false) return false;               // nothing answered: the browser's
                    // Deep on a cell: the window is refused (law 221), the edit goes in through
                    // the editor, and the settle — a microtask — is checked in the next breath.
                    b.click(3, 2); if (g.cursor().pk !== 'r3' || g.cursor().column !== 'qty') return false;
                    if (!g.takeControlAtCursor() || !g.isDeep()) return false;
                    if (g.scrollRows(5) !== false || b.wheel(3, 1) !== false) return false;
                    var input = b.relation.cellFor('r3', 'qty').editorElement();
                    input.value = '7';
                    input.dispatch('keydown', { key: 'Enter' });
                    E = b;
                    return b.relation.edited('r3', 'qty') === '7';
                })()"""), "arrows at the edge, the wheel and the page keys move the window; the cursor rides by identity");
        assertTrue(evalBool("""
                (() => {
                    var b = E, g = b.grid;
                    if (g.isDeep()) return false;
                    // The edit rides the window out and back: the row is freed in between, and the
                    // value is the relation's, not the cell's.
                    g.scrollRows(5); g.scrollRows(5);
                    if (b.relation.heldRows().indexOf(3) >= 0) return false;
                    g.scrollRows(-10);
                    return b.shown() === 'r0,r1,r2,r3,r4' && b.td(3, 2).children[0].textContent === '7' && b.invariants().length === 0;
                })()"""), "an edit survives its row being scrolled away, freed, and scrolled back");
    }
}
