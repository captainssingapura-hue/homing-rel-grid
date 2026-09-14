package hue.captains.singapura.js.homing.relgrid;

import hue.captains.singapura.js.homing.ssjs.test.JsModuleTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * RFC 0050 · Episode 2, round 1 — arrangement over a relation that exposes
 * identities, columns and a cell manager, and <b>no way to read a value</b>.
 * The fixture relation has no {@code get}; the domain changes its own cells
 * directly and the grid is never told. If the grid rendered, it did so
 * through {@code cellFor} alone. Runs on {@link RelGridTestDom}.
 */
class RelGridArrangementTest extends JsModuleTestBase {

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
    }

    private boolean evalBool(String expr) { return js.eval("js", expr).asBoolean(); }

    @Test
    void rendersEveryHeaderAndCellThroughCellForAlone() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture(), maps = f.grid.viewMaps();
                    if (typeof f.relation.get !== 'undefined') return false;   // there is nothing to read
                    var headerRow = f.headerRow();
                    if (headerRow.children.length !== 2) return false;
                    if (headerRow.children[0].textContent !== 'ingredient') return false;
                    if (f.tbody().children.length !== 3) return false;
                    for (var i = 0; i < maps.rows(); i++) for (var j = 0; j < maps.cols(); j++) {
                        var td = f.td(i, j);
                        if (td.children.length !== 1) return false;
                        var id = maps.resolve(i, j);
                        if (td.children[0].textContent !== String(f.data[id.pk][id.column])) return false;
                    }
                    return f.asked() === 6 && f.mints() === 6 && f.arranged.join() === 'base';
                })()"""), "the grid must render through cellFor alone: one ask, one host per identity");
    }

    @Test
    void reapplyKeepsTheSlotsAndAsksForNoCellTwice() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture();
                    var before = f.cellEl('coq', 'calories'), oldBody = f.tbody(), oldTd = f.td(1, 1);
                    var minted = f.branch.getBranch('slots').elementCount;
                    f.grid.reapply();
                    var after = f.cellEl('coq', 'calories');
                    return f.tbody() === oldBody          // the same shape: the same slots, nothing minted, nothing released
                        && f.td(1, 1) === oldTd
                        && f.branch.getBranch('slots').elementCount === minted
                        && after === before               // the cell's element kept its identity, in its slot
                        && after.parentNode === oldTd
                        && f.asked() === 6                // cellFor was NOT asked again
                        && f.mints() === 6                // no host re-minted
                        && f.arranged.join() === 'base,reapply';
                })()"""), "re-arrangement over an unchanged shape re-places the same cells into the same slots");
    }

    @Test
    void anUnchangedShapeKeepsTheSlotsAndThePaintFollowsTheIdentities() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture(), g = f.grid, maps = g.viewMaps();
                    f.click(0, 0);                                           // the cursor on mapo / ingredient
                    var td00 = f.td(0, 0), td20 = f.td(2, 0), slots = f.branch.getBranch('slots');
                    if (!css.hasClass(td00, hrg_cursor)) return false;
                    // A PERMUTATION is the same shape: the slots stay, the cells move between
                    // them, and the cursor — an identity — is painted where its cell went.
                    maps.setRowView(['fish', 'coq', 'mapo']);
                    if (f.branch.getBranch('slots') !== slots || f.td(0, 0) !== td00 || f.td(2, 0) !== td20) return false;
                    if (f.cellEl('mapo', 'ingredient').parentNode !== td20) return false;
                    if (css.hasClass(td00, hrg_cursor) || !css.hasClass(td20, hrg_cursor)) return false;
                    if (g.cursor().pk !== 'mapo') return false;
                    // A selection is positions and goes with the arrangement (law 43): the
                    // old paint came off the kept slots, and what is painted is the cursor's
                    // own 1x1 — fish / ingredient, where that cell went.
                    f.click(0, 0); f.click(1, 1, { shift: true });
                    if (f.painted() !== '0,0 0,1 1,0 1,1') return false;
                    maps.setRowView(['mapo', 'coq', 'fish']);
                    if (f.painted() !== '2,0' || g.selectionCount() !== 0 || g.cursor().pk !== 'fish') return false;
                    // A different NUMBER of rows is a new shape: minted fresh, the old released.
                    maps.setRowView(['mapo', 'coq']);
                    if (f.branch.getBranch('slots') === slots || slots.elementCount !== 0 || f.td(0, 0) === td00) return false;
                    return f.tbody().children.length === 2;
                })()"""), "the same shape keeps its slots and the paint follows identities; a new shape mints fresh");
    }

    @Test
    void aDetachedCellIsForgottenAndTheDomainsOwnChangeShowsWhenItReturns() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture(), maps = f.grid.viewMaps();
                    var before = f.cellEl('coq', 'calories');
                    maps.setRowView(['mapo']);                       // narrow the presented space
                    var shown = f.tbody().children.length;
                    // The registry is exactly the presented cells: the rows that left are
                    // FORGOTTEN — out of the tree, and out of the grid's hands entirely.
                    if (f.grid.cells().size() !== 2 || f.grid.cells().get('coq', 'calories') !== null) return false;
                    if (before.parentNode !== null) return false;
                    f.relation.change('coq', 'calories', 999);       // the domain updates a cell the grid has forgotten
                    maps.setRowView(['mapo', 'coq', 'fish']);        // widen again: the two rows are asked for AGAIN
                    var el = f.cellEl('coq', 'calories');
                    return shown === 1
                        && el === before                            // the relation kept the cell: the same element comes back
                        && el.textContent === '999'                 // current, with no grid involvement
                        && f.grid.cells().size() === 6
                        && f.asked() === 10 && f.mints() === 10;    // once per PRESENTATION: four cells re-asked, re-placed
                })()"""), "a row that leaves is forgotten; a row that returns is asked for again, and the domain's own change shows");
    }

    @Test
    void destroyDetachesEverythingAndDisposesNothing() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture();
                    var el = f.cellEl('mapo', 'ingredient');
                    var cell = f.relation.cellFor('mapo', 'ingredient');   // the domain still holds it
                    f.grid.destroy();
                    return f.container.children.length === 0     // the table is gone
                        && el.parentNode === null                // the cell's element is detached
                        && cell.value() === 'tofu'               // and the cell is alive and current
                        && f.grid.cells().size() === 0;
                })()"""), "destroy is a detach, never a dispose: the cells outlive the grid");
    }

    @Test
    void theRowAxisIsTheViewAndAStrangerIsRefusedByTheRelationWhole() {
        assertTrue(evalBool("""
                (() => {
                    // A relation that could present fifty rows presents three: the grid holds
                    // the three — the row axis is the View, there is no base — and no cell
                    // beyond them is asked for, let alone minted. The relation, not the grid,
                    // knows what it owns: a stranger is refused at cellFor.
                    var asked = [], cellsB = hostBranch(), seq = 0;              // the domain's branch, a sub-branch per cell
                    var shown = ['r0', 'r1', 'r2'];                                // what the relation presents right now
                    var relation = {
                        view:    function (intent) { return intent ? null : shown.slice(); },
                        columns: function () { return ['a', 'b']; },
                        cellFor: function (pk, col) {
                            if (!/^r([0-9]|[1-4][0-9])$/.test(pk)) throw new Error('no such row: ' + pk);
                            asked.push(pk + ' ' + col);
                            return new RelGridTextCell({ branch: cellsB.createBranch('c' + (++seq)), value: pk + col });
                        }
                    };
                    var container = makeEl('div');
                    var grid = new RelGrid({ container: container, branch: testBranch(), relation: relation });
                    if (grid.viewMaps().rows() !== 3 || typeof grid.viewMaps().basePks === 'function') return false;
                    if (typeof relation.pks !== 'undefined') return false;         // nothing enumerates
                    if (asked.length !== 6) return false;                          // three rows, two columns
                    // The article grows a row: the host presents one more, through the seam.
                    grid.viewMaps().setRowView(['r0', 'r1', 'r2', 'r3']);
                    if (grid.viewMaps().rows() !== 4 || asked.length !== 8) return false;
                    // And shrinks: nothing is asked, nothing is disposed, the row is just not shown.
                    grid.viewMaps().setRowView(['r0', 'r1']);
                    if (grid.viewMaps().rows() !== 2 || asked.length !== 8) return false;
                    // A View naming an identity the relation does not own is refused WHOLE, by the
                    // relation, before a slot moves: the rows stay as they were, the maps too,
                    // and the caller hears it. A duplicate is the one thing the maps refuse alone.
                    var tbody = container.children[0].children[0].children[2], before = tbody;
                    var refused = false;
                    try { grid.viewMaps().setRowView(['r0', 'nope']); }
                    catch (e) { refused = /no such row: nope/.test(String(e)); }
                    if (!refused || grid.viewMaps().rows() !== 2 || grid.viewMaps().rowView().join() !== 'r0,r1') return false;
                    if (container.children[0].children[0].children[2] !== before || tbody.children.length !== 2) return false;
                    if (asked.length !== 8) return false;                          // r0 was in the registry; nope was refused before b
                    try { grid.viewMaps().setRowView(['r0', 'r0']); refused = false; }
                    catch (e) { refused = /duplicate/.test(String(e)); }
                    if (!refused || grid.viewMaps().rows() !== 2) return false;
                    // And at construction, the same refusal, out of the constructor.
                    refused = false; shown = ['r0', 'nope'];
                    try { new RelGrid({ container: makeEl('div'), branch: testBranch(), relation: relation }); }
                    catch (e) { refused = /no such row/.test(String(e)); }
                    if (!refused) return false;
                    // A host that still says rowView is told there is none.
                    refused = false; shown = ['r0'];
                    try { new RelGrid({ container: makeEl('div'), branch: testBranch(), relation: relation, rowView: ['r0'] }); }
                    catch (e) { refused = /no rowView/.test(String(e)); }
                    if (!refused) return false;
                    // And the same for columns: a relation may declare a column it shows only
                    // sometimes — a half-square for a squeezed mark — and present it later.
                    var narrow = new RelGrid({ container: makeEl('div'), branch: testBranch(), relation: relation,
                                               columnView: ['b'], minColumnWidth: 24 });
                    if (narrow.viewMaps().cols() !== 1 || narrow.viewMaps().columnAt(0) !== 'b') return false;
                    narrow.viewMaps().setColumnView(['a', 'b']);
                    if (narrow.viewMaps().cols() !== 2) return false;
                    // The width floor is the host's to lower, within reason: 24 is held as
                    // 24 here, where the default grid would have held 40; 2 is still 8.
                    if (!narrow.setColumnWidth('a', 24) || narrow.columnWidth('a') !== 24) return false;
                    if (!narrow.setColumnWidth('b', 2) || narrow.columnWidth('b') !== 24) return false;
                    var floorless = new RelGrid({ container: makeEl('div'), branch: testBranch(), relation: relation,
                                                  minColumnWidth: 2 });
                    floorless.setColumnWidth('a', 2);
                    return floorless.columnWidth('a') === 8;
                })()"""), "rowView and columnView present part of a larger relation; a half-square column may be held");
    }
    @Test
    void aBareArrowAtTheEdgeGoesNowhereAndIsReported() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture(), g = f.grid;
                    f.click(0, 0);                                           // mapo / ingredient: top-left
                    f.key('ArrowUp');
                    f.key('ArrowLeft');
                    if (g.cursor().pk !== 'mapo' || g.cursor().column !== 'ingredient') return false;
                    if (f.edges.join(',') !== 'up,left') return false;
                    // Room to move: no report, and the cursor moves.
                    f.key('ArrowDown'); f.key('ArrowRight');
                    if (g.cursor().pk !== 'coq' || g.cursor().column !== 'calories' || f.edges.length !== 2) return false;
                    f.key('ArrowDown'); f.key('ArrowDown');                  // the bottom-right, and one past it
                    f.key('ArrowRight');
                    if (g.cursor().pk !== 'fish' || f.edges.join(',') !== 'up,left,down,right') return false;
                    // A bare move at the edge still clears the ranges — it is a bare move.
                    f.click(0, 0); f.click(2, 1, { shift: true });
                    if (g.selectionCount() !== 1) return false;
                    f.click(2, 1); f.key('ArrowDown');
                    if (g.selectionCount() !== 0) return false;
                    // Shift+arrow at the edge is an extension, not a report; and a locked grid reports nothing.
                    f.key('ArrowDown', { shift: true });
                    return f.edges.length === 5 && f.edges[4] === 'down';
                })()"""), "an arrow with nowhere to go is reported as the edge it met, and consumed");
    }
}
