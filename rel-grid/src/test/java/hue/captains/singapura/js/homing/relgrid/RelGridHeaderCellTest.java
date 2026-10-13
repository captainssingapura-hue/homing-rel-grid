package hue.captains.singapura.js.homing.relgrid;

import hue.captains.singapura.js.homing.ssjs.test.JsModuleTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * RFC 0050 · Episode 2 — the HEADER is the relation's too. A header cell is a
 * noun as a cell is: the relation answers {@code headerFor(column)}, the grid
 * asks it once for {@code headerElement()} and places it in the {@code <th>}
 * its column maps to, and what the slot says is the domain's from then on.
 * The grid writes no text of its own there: a relation whose header is only
 * labels answers the stock label headers ({@code RelGridLabels}), and one with
 * no header cells is refused while the header shows; the host says nothing
 * about what a column is called (map 12, law 86). And the grid can be TOLD, unasked, that the View changed — the first
 * message the channel's other direction carries to a grid — and asks again.
 */
class RelGridHeaderCellTest extends JsModuleTestBase {

    /** Header cells the domain owns: a span per column on its own sub-branch, with a click of its own. */
    private static final String HEADERS = """
            function headerCells(labels) {
                var branch = hostBranch(), made = [], asked = [], clicks = [], cells = new Map();
                return {
                    headerFor: function (col) {
                        asked.push(col);                                     // every ask; made counts creations
                        var c = cells.get(col);
                        if (!c) {
                            made.push(col);
                            var own = branch.createBranch('h-' + col);
                            own.activate({ toString: function () { return 'header ' + col; } });
                            var el = own.createElement('head', 'span');
                            el.textContent = labels[col] || col;
                            el.addEventListener('click', function () { clicks.push(col); });   // the header cell's own gesture
                            c = { headerElement: function () { return el; }, dispose: function () { own.dissolve(); }, el: el };
                            cells.set(col, c);
                        }
                        return c;
                    },
                    made: made, asked: asked, clicks: clicks, branch: branch, cells: cells
                };
            }
            """;

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
        js.eval("js", HEADERS);
    }

    private boolean evalBool(String expr) { return js.eval("js", expr).asBoolean(); }

    @Test
    void aHeaderCellIsANounPlacedInTheHeaderSlotAndTheGridWritesNoLabel() {
        assertTrue(evalBool("""
                (() => {
                    var h = headerCells({ ingredient: 'Dish', calories: 'kcal' });
                    var f = fixture({ headerFor: h.headerFor, labels: { ingredient: 'ignored: the header cell says' } });
                    // One header cell per presented column, asked once, its element in the <th> —
                    // and nothing else in the <th> but the grid's own handle.
                    if (h.made.join() !== 'ingredient,calories') return false;
                    var th0 = f.thAt(0), th1 = f.thAt(1);
                    if (th0.children.indexOf(h.cells.get('ingredient').el) < 0 || th1.children.indexOf(h.cells.get('calories').el) < 0) return false;
                    if (th0.ownText !== '' || th0.children.length !== 2) return false;   // the handle, and the domain's element; no text of the grid's
                    if (h.cells.get('ingredient').el.textContent !== 'Dish') return false;
                    // The header cell's element is on the DOMAIN's branch, not the grid's.
                    if (h.branch.getBranch('h-ingredient').getElement('head') !== h.cells.get('ingredient').el) return false;
                    if (f.branch.getBranch('slots').getElement('th-0') !== th0) return false;
                    // A click on it is the header cell's own: the grid captures nothing, the cursor does not move.
                    f.click(1, 0);
                    h.cells.get('calories').el.dispatch('click', {});
                    if (h.clicks.join() !== 'calories' || f.grid.cursor().pk !== 'coq') return false;
                    // Re-arranging keeps the same header cells in the same slots; nothing re-asked.
                    f.grid.reapply();
                    return h.asked.length === 2 && th1.children.indexOf(h.cells.get('calories').el) >= 0;
                })()"""), "headerFor answers a noun; the grid places its element in the th and writes no label");
    }

    @Test
    void aColumnThatLeavesTheViewForgetsItsHeaderCellAndOneThatReturnsIsAskedAgain() {
        assertTrue(evalBool("""
                (() => {
                    var h = headerCells({});
                    var f = fixture({ headerFor: h.headerFor }), maps = f.grid.viewMaps();
                    var kcal = h.cells.get('calories').el;
                    maps.setColumnView(['ingredient']);
                    if (f.headerRow().children.length !== 1 || kcal.parentNode !== null) return false;   // out of the slot, alive
                    if (f.grid.cells().size() !== 3) return false;                                        // the body followed the column view
                    maps.setColumnView(['calories', 'ingredient']);
                    if (h.asked.join() !== 'ingredient,calories,calories' || h.made.length !== 2) return false;   // asked again; the relation kept it
                    if (f.thAt(0).children.indexOf(kcal) < 0) return false;                               // the same element, in its new slot
                    // Destroy detaches every header cell and disposes none.
                    f.grid.destroy();
                    return kcal.parentNode === null && h.branch.getBranch('h-calories') !== null && h.branch.getBranch('h-calories').getElement('head') === kcal;
                })()"""), "the header registry is the cell registry's rule: once per presentation, forgotten on leaving, never disposed");
    }

    @Test
    void aPlainRelationNamesItsColumnsAndTheHostMayNot() {
        assertTrue(evalBool("""
                (() => {
                    // Labels are a stock header cell's: RelGridLabels answers headerFor; a column not named has its name.
                    var l = fixture({ labels: { ingredient: 'Dish' } });
                    if (l.headText(0) !== 'Dish' || l.headText(1) !== 'calories') return false;
                    if (l.thAt(0).ownText !== '') return false;                    // the grid wrote nothing of its own
                    var plain = fixture();
                    if (plain.headText(0) !== 'ingredient') return false;
                    // A relation with no header cells is refused while the header shows - labels() is not read -
                    // and made without a header, it needs none.
                    var cells = hostBranch(), seq = 0;
                    var bare = { view: function (i) { return i ? null : ['mapo']; }, columns: function () { return ['ingredient']; },
                                 labels: function () { return { ingredient: 'Dish' }; },
                                 cellFor: function () { return new RelGridTextCell({ branch: cells.createBranch('c' + (++seq)), value: 'tofu' }); } };
                    var refused = false;
                    try { new RelGrid({ design: TEST_DESIGN, container: makeEl('div'), branch: testBranch(), relation: bare }); }
                    catch (e) { refused = /a header shown is the relation's header cells/.test(String(e)) && String(e).indexOf('labels() is no longer read') >= 0; }
                    if (!refused) return false;
                    var headless = new RelGrid({ design: TEST_DESIGN, container: makeEl('div'), branch: testBranch(), relation: bare, header: { show: false } });
                    if (headless.viewMaps().rows() !== 1) return false;
                    // What a cell does with text too wide for it is the cell's: the grid refuses to be told.
                    refused = false;
                    try { new RelGrid({ design: TEST_DESIGN, container: makeEl('div'), branch: testBranch(), relation: plain.relation, overflow: 'wrap' }); }
                    catch (e) { refused = /overflow is gone/.test(String(e)); }
                    if (!refused) return false;
                    // The host has no say in what a column is called (law 86).
                    refused = false;
                    try { new RelGrid({ design: TEST_DESIGN, container: makeEl('div'), branch: testBranch(), relation: plain.relation, header: { labels: { ingredient: 'x' } } }); }
                    catch (e) { refused = /labels is gone/.test(String(e)); }
                    if (!refused) return false;
                    // A header cell without an element is refused — before anything moves, as a cell is.
                    var half = function (col) {
                        if (col === 'calories') return { headerElement: function () { return null; } };
                        var el = makeEl('span'); el.textContent = 'ok';
                        return { headerElement: function () { return el; } };
                    };
                    refused = false;
                    try { fixture({ headerFor: half }); } catch (e) { refused = /must answer an element/.test(String(e)); }
                    return refused;
                })()"""), "labels are a stock header cell's; no header cells, no header; overflow and the host's header.labels are refused");
    }

    @Test
    void theStockCellsOwnTheirOverflowAndWearTheHousesCells() {
        assertTrue(evalBool("""
                (() => {
                    function has(el, c) { return (el.className || '').split(' ').indexOf(c) >= 0; }
                    var f = fixture({ labels: { ingredient: 'Dish' } });
                    var cell = f.cellEl('mapo', 'ingredient'), label = null, th = f.thAt(0);
                    for (var k = 0; k < th.children.length; k++) if (has(th.children[k], 'hrg-label')) label = th.children[k];
                    // ellipsis unless a cell says otherwise; the house's data cell and header cell worn
                    if (!has(cell, 'hrg-text') || !has(cell, 'hrg-text-ellipsis') || !has(cell, 'look-data-cell')) return false;
                    if (!label || !has(label, 'hrg-text-ellipsis') || !has(label, 'look-header-cell')) return false;
                    // and the grid's table says nothing of overflow
                    if (/hrg-ov-/.test(f.table().className)) return false;
                    var b = hostBranch();
                    var wrap = new RelGridTextCell({ branch: b.createBranch('w'), value: 'a long line', overflow: 'wrap' });
                    var clip = new RelGridLabelHeader({ branch: b.createBranch('l'), label: 'x', overflow: 'clip' });
                    if (!has(wrap.cellElement(), 'hrg-text-wrap') || !has(clip.headerElement(), 'hrg-text-clip')) return false;
                    var refused = false;
                    try { new RelGridTextCell({ branch: b.createBranch('s'), overflow: 'scroll' }); }
                    catch (e) { refused = /overflow is 'ellipsis', 'clip' or 'wrap'/.test(String(e)); }
                    // a label renamed by its owner
                    var heads = new RelGridLabels({ branch: b.createBranch('heads'), labels: { a: 'A' } });
                    if (heads.headerFor('a') !== heads.headerFor('a') || heads.headerFor('b').label() !== 'b') return false;
                    heads.headerFor('a').set('Alpha');
                    return refused && heads.headerFor('a').headerElement().textContent === 'Alpha';
                })()"""), "the stock cells say what they do with text too wide for them, and wear the house's cells");
    }

    @Test
    void theGridIsToldItsViewChangedAndAsksAgain() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture(), g = f.grid;
                    f.click(2, 0);                                            // the cursor on fish / ingredient
                    var fishEl = f.cellEl('fish', 'ingredient');
                    // The relation's View moved underneath the grid; the host tells the grid, which asks again.
                    f.relation.show(['fish', 'mapo']);
                    if (g.tell(new RelGridViewChanged()) !== true) return false;
                    if (f.rowsShown() !== 'fish,mapo' || f.arranged[f.arranged.length - 1] !== 'rows') return false;
                    if (g.cursor().pk !== 'fish' || f.cellEl('fish', 'ingredient') !== fishEl) return false;   // identity kept; the same cell, re-placed
                    if (g.cells().size() !== 4 || g.cells().get('coq', 'ingredient') !== null) return false;   // coq left, and was forgotten
                    if (f.asked() !== 6) return false;
                    // The same View again is still a remap — harmless — and a stranger is refused, loudly.
                    if (g.tell(new RelGridViewChanged()) !== true || f.rowsShown() !== 'fish,mapo') return false;
                    var errors = [];
                    console.error = function () { errors.push(Array.prototype.slice.call(arguments).join(' ')); };
                    if (g.tell({ kind: 'nonsense' }) !== false || !/does not understand/.test(errors.join())) return false;
                    if (g.tell(new RelGridGroupFold('a', true)) !== false) return false;
                    // After destroy, told nothing.
                    g.destroy();
                    return g.tell(new RelGridViewChanged()) === false;
                })()"""), "tell(RelGridViewChanged) asks view() again and presents it as a remap; strangers are refused");
    }
}
