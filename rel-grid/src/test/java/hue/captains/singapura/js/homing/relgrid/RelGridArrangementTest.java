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
}
