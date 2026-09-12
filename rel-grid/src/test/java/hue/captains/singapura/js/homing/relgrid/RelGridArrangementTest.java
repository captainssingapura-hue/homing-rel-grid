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
        loadModule(RelGridTestDom.SELECTION);
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
    void reapplyMintsFreshSlotsButAsksForNoCellTwice() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture();
                    var before = f.cellEl('coq', 'calories'), oldBody = f.tbody();
                    f.grid.reapply();
                    var after = f.cellEl('coq', 'calories');
                    return f.tbody() !== oldBody          // slots were rebuilt
                        && after === before               // the cell's element kept its identity
                        && f.asked() === 6                // cellFor was NOT asked again
                        && f.mints() === 6                // no host re-minted
                        && f.arranged.join() === 'base,reapply';
                })()"""), "re-arrangement re-places the same cells: created rarely, retrieved from the registry");
    }

    @Test
    void aDetachedCellChangedByItsDomainReplacesCurrent() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture(), maps = f.grid.viewMaps();
                    maps.setRowView(['mapo']);                       // narrow the presented space
                    var shown = f.tbody().children.length;
                    f.relation.change('coq', 'calories', 999);       // the domain updates a DETACHED cell
                    maps.resetRowView();                             // widen again
                    var el = f.cellEl('coq', 'calories');
                    return shown === 1
                        && el.textContent === '999'                 // current, with no grid involvement
                        && f.mints() === 6 && f.asked() === 6;      // never re-minted, never re-asked
                })()"""), "detach keeps the domain's cell alive; the domain's own change shows on re-place");
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
    void aRelationMayDeclareMoreThanItPresentsAndTheGridArrangesOnlyThePresented() {
        assertTrue(evalBool("""
                (() => {
                    // A relation with a CAPACITY: it declares more identities than it shows,
                    // and the host presents a prefix at construction. The first arrangement
                    // is the prefix — no cell beyond it is asked for, let alone minted.
                    var asked = [];
                    var relation = {
                        pks:     function () { var o = []; for (var r = 0; r < 50; r++) o.push('r' + r); return o; },
                        columns: function () { return ['a', 'b']; },
                        cellFor: function (pk, col) { asked.push(pk + ' ' + col); return new RelGridTextCell({ value: pk + col }); }
                    };
                    var branch = { createElement: function (n, t) { return makeEl(t); } };
                    var container = makeEl('div');
                    var grid = new RelGrid({ container: container, branch: branch, relation: relation,
                                             rowView: ['r0', 'r1', 'r2'] });
                    if (grid.viewMaps().rows() !== 3 || grid.viewMaps().basePks().length !== 50) return false;
                    if (asked.length !== 6) return false;                          // three rows, two columns
                    // The article grows a row: the host presents one more, through the seam.
                    grid.viewMaps().setRowView(['r0', 'r1', 'r2', 'r3']);
                    if (grid.viewMaps().rows() !== 4 || asked.length !== 8) return false;
                    // And shrinks: nothing is asked, nothing is disposed, the row is just not shown.
                    grid.viewMaps().setRowView(['r0', 'r1']);
                    if (grid.viewMaps().rows() !== 2 || asked.length !== 8) return false;
                    // A view naming an identity the relation never declared is refused.
                    var refused = false;
                    try { new RelGrid({ container: makeEl('div'), branch: branch, relation: relation, rowView: ['r0', 'nope'] }); }
                    catch (e) { refused = /unknown key/.test(String(e)); }
                    if (!refused) return false;
                    // And the same for columns: a relation may declare a column it shows only
                    // sometimes — a half-square for a squeezed mark — and present it later.
                    var narrow = new RelGrid({ container: makeEl('div'), branch: branch, relation: relation,
                                               rowView: ['r0'], columnView: ['b'], minColumnWidth: 24 });
                    if (narrow.viewMaps().cols() !== 1 || narrow.viewMaps().columnAt(0) !== 'b') return false;
                    narrow.viewMaps().setColumnView(['a', 'b']);
                    if (narrow.viewMaps().cols() !== 2) return false;
                    // The width floor is the host's to lower, within reason: 24 is held as
                    // 24 here, where the default grid would have held 40; 2 is still 8.
                    if (!narrow.setColumnWidth('a', 24) || narrow.columnWidth('a') !== 24) return false;
                    if (!narrow.setColumnWidth('b', 2) || narrow.columnWidth('b') !== 24) return false;
                    var floorless = new RelGrid({ container: makeEl('div'), branch: branch, relation: relation,
                                                  rowView: ['r0'], minColumnWidth: 2 });
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
