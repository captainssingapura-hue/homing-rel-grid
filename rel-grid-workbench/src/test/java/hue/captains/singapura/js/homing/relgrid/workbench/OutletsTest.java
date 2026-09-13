package hue.captains.singapura.js.homing.relgrid.workbench;

import hue.captains.singapura.js.homing.ssjs.test.JsModuleTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The Outlets bench's domain, tested without a grid or a group: the ledger
 * sells and derives, a relation per outlet keeps its own cells current and
 * hears nothing of another outlet's trade, and the fences draw what the
 * store publishes and follow it.
 */
class OutletsTest extends JsModuleTestBase {

    private static final String GRID_DIR = "/homing/js/hue/captains/singapura/js/homing/relgrid/";
    private static final String BENCH_DIR = GRID_DIR + "workbench/";

    private static final String HELPERS = """
            // A ledger with a fixed clock and loaded dice, so a test can say what a burst sells.
            function ledger(rolls) {
                var i = 0, t = 0;
                return createSalesStore({
                    now: function () { t += 61; return new Date(2026, 8, 12, 9, 0, t); },
                    random: function () { var r = rolls ? rolls[i % rolls.length] : 0.5; i++; return r; }
                });
            }
            function byClass(root, c) {
                var out = [];
                (function walk(el) {
                    if ((el.className || '').split(' ').indexOf(c) >= 0) out.push(el);
                    (el.children || []).forEach(walk);
                })(root);
                return out;
            }
            """;

    /** The policy test's DOM, plus the attributes the fence's toggle sets. */
    private static final String DOM_STUB = DishPolicyTest.DOM_STUB + """
            var __makeEl = makeEl;
            makeEl = function (tag) {
                var el = __makeEl(tag); el._a = {};
                el.setAttribute = function (k, v) { this._a[k] = String(v); };
                el.getAttribute = function (k) { return (this._a[k] == null) ? null : this._a[k]; };
                return el;
            };
            """;

    @BeforeEach
    void setup() {
        js = buildContext();
        js.eval("js", DOM_STUB);
        for (String m : DishPolicyTest.PARTY) loadModule(m);
        js.eval("js", DishPolicyTest.STYLES);
        loadModule(GRID_DIR + "protocol/RelGridProtocolModule.js");
        loadModule(GRID_DIR + "RelGridStockCellsModule.js");
        loadModule(BENCH_DIR + "SalesStore.js");
        loadModule(BENCH_DIR + "OutletRelation.js");
        js.eval("js", HELPERS);
    }

    private boolean evalBool(String expr) { return js.eval("js", expr).asBoolean(); }

    @Test
    void theLedgerSellsDerivesAndTellsOnlyWhatMoved() {
        assertTrue(evalBool("""
                (() => {
                    var s = ledger();
                    if (s.outlets().map(function (o) { return o.id; }).join(',') !== 'downtown,airport,harbour') return false;
                    if (s.dishes().length !== 6 || s.columns().join(',') !== 'dish,sold,revenue,lastSale') return false;
                    if (s.get('downtown', 'mapo', 'sold') !== 24 || s.get('downtown', 'mapo', 'revenue') !== 24 * 9.5) return false;
                    if (s.get('downtown', 'mapo', 'dish') !== 'tofu' || s.get('downtown', 'mapo', 'lastSale') !== '\\u2014') return false;
                    var t0 = s.totals('downtown');
                    if (t0.sold !== 24 + 11 + 17 + 9 + 31 + 22) return false;
                    var heard = [];
                    s.subscribe(function (o, pk, col, v) { heard.push(o + '/' + pk + '/' + col + '=' + (col === 'totals' ? v.sold : v)); });
                    if (!s.sell('harbour', 'fish', 2)) return false;
                    // Three cells and one totals, at Harbour, and nothing at any other outlet.
                    if (heard.join(' ') !== 'harbour/fish/sold=31 harbour/fish/revenue=372 harbour/fish/lastSale=09:01:01 harbour/null/totals=92') return false;
                    if (s.totals('harbour').sold !== 92 || s.totals(null).sold !== t0.sold + 99 + 92) return false;
                    if (s.revision() !== 1) return false;
                    // Refusals: nothing of nobody, and no sale of nothing.
                    return s.sell('nowhere', 'fish', 1) === false && s.sell('harbour', 'fish', 0) === false && heard.length === 4;
                })()"""), "a sale moves three cells and one totals at one outlet, and no other outlet hears");
    }

    @Test
    void aBurstIsLoadedDiceAndResetPutsEverythingBack() {
        assertTrue(evalBool("""
                (() => {
                    var s = ledger([0.99, 0.1, 0.6, 0.3, 0.99, 0.1]);       // 3,0,2,1,3,0 of the six dishes
                    s.trade('airport');
                    var sold = s.dishes().map(function (pk) { return s.get('airport', pk, 'sold'); });
                    if (sold.join(',') !== '11,19,8,15,43,12') return false;
                    if (s.get('downtown', 'mapo', 'sold') !== 24) return false;   // one outlet only
                    var heard = 0; s.subscribe(function () { heard++; });
                    s.reset();
                    if (s.get('airport', 'mapo', 'sold') !== 8 || s.get('airport', 'mapo', 'lastSale') !== '\\u2014') return false;
                    // Every cell of every outlet is told on a reset, and the totals that moved.
                    return heard === 3 * 6 * 3 + 1;
                })()"""), "a burst sells what the dice say, at the one outlet named; reset tells every cell");
    }

    @Test
    void aRelationIsOneOutletsBookAndKeepsItsOwnCellsCurrent() {
        assertTrue(evalBool("""
                (() => {
                    var s = ledger();
                    var h = createOutletRelation(s, 'harbour', { branch: testBranch() }), d = createOutletRelation(s, 'downtown', { branch: testBranch() });
                    if (h.pks().join(',') !== s.dishes().join(',') || h.columns().join(',') !== 'dish,sold,revenue,lastSale') return false;
                    if (h.readOnlyColumns().join(',') !== 'dish,sold,revenue,lastSale') return false;   // a book is read
                    if (h.name() !== 'Harbour' || h.outlet() !== 'harbour') return false;
                    var c = h.cellFor('fish', 'revenue'), el = c.cellElement();   // the cell's own element, as the grid asks for it
                    if (h.cellFor('fish', 'revenue') !== c || c.cellElement() !== el) return false;   // once per identity
                    if (el.textContent !== '348.00') return false;             // 29 × 12, formatted for reading
                    var dl = d.cellFor('fish', 'revenue').cellElement();
                    s.sell('harbour', 'fish', 2);
                    if (el.textContent !== '372.00') return false;             // Harbour's cell moved
                    if (dl.textContent !== '204.00') return false;             // Downtown's did not
                    if (h.totals().sold !== 92) return false;
                    try { createOutletRelation(s, 'nowhere', { branch: testBranch() }); return false; } catch (e) { if (!/unknown outlet/.test(String(e))) return false; }
                    h.dispose();
                    s.sell('harbour', 'fish', 1);
                    return el.textContent === '372.00' && h.cellCount() === 0;   // disposed: deaf, and empty
                })()"""), "a relation is one outlet's read-only book, its cells set by the store, deaf to other outlets");
    }

    @Test
    void theFencesDrawWhatTheStorePublishesAndFollowIt() {
        assertTrue(evalBool("""
                (() => {
                    var s = ledger();
                    var f = createOutletFence(s, 'harbour', { branch: testBranch() }), host = makeEl('div');
                    host.appendChild(f.fenceElement());                          // placed, as the group places it
                    if (f.fenceElement() !== host.children[0]) return false;     // a noun: the same element every time
                    if (byClass(host, 'wb-fence-name')[0].textContent !== 'Harbour') return false;
                    var totals = byClass(host, 'wb-fence-totals')[0];
                    if (totals.textContent !== '90 sold \\u00b7 1092.50 taken') return false;
                    var g = createLedgerFence(s, { branch: testBranch() }), ghost = makeEl('div');
                    ghost.appendChild(g.fenceElement());
                    if (byClass(ghost, 'wb-fence-name')[0].textContent !== 'All outlets') return false;
                    var all = byClass(ghost, 'wb-fence-totals')[0], before = all.textContent;
                    s.sell('harbour', 'fish', 2);
                    if (totals.textContent !== '92 sold \\u00b7 1116.50 taken') return false;
                    if (all.textContent === before) return false;
                    s.sell('downtown', 'mapo', 1);
                    if (totals.textContent !== '92 sold \\u00b7 1116.50 taken') return false;   // not Harbour's
                    f.dispose();
                    s.sell('harbour', 'fish', 1);
                    return totals.textContent === '92 sold \\u00b7 1116.50 taken';              // disposed: deaf
                })()"""), "an outlet's fence shows its totals and follows them; the ledger's follows every outlet");
    }
    @Test
    void theFenceToggleTellsThroughTheHostsClosureAndDrawsItselfFromWhatItIsTold() {
        assertTrue(evalBool("""
                (() => {
                    var s = ledger();
                    // The closure a HOST builds a fence with: onto the group's tell. It records what it is told.
                    var told = [];
                    var tell = function (m) { told.push(m); return true; };
                    var f = createOutletFence(s, 'airport', { branch: testBranch(), tell: tell }), host = makeEl('div');
                    host.appendChild(f.fenceElement());
                    var toggle = byClass(host, 'wb-fence-fold')[0];
                    if (!toggle || toggle.textContent !== '▾' || toggle.getAttribute('aria-expanded') !== 'true') return false;
                    // Pressed: a RelGridGroupFold for THIS outlet, the other way round from what the fence was last told.
                    toggle.dispatch('click', {});
                    if (told.length !== 1 || !(told[0] instanceof RelGridGroupFold)) return false;
                    if (told[0].member !== 'airport' || told[0].folded !== true) return false;
                    // The fence draws from what it is TOLD, not from having pressed: nothing changed yet...
                    if (toggle.textContent !== '▾' || f.folded() !== false) return false;
                    f.onFolded(true);                                            // ...until the group says so
                    if (toggle.textContent !== '▸' || toggle.getAttribute('aria-expanded') !== 'false') return false;
                    if (toggle.getAttribute('aria-label') !== 'Unfold Airport') return false;
                    // Pressed again: told it is folded, the fence asks to unfold.
                    toggle.dispatch('click', {});
                    if (told[1].folded !== false) return false;
                    f.onFolded(false);
                    if (toggle.textContent !== '▾') return false;
                    // A fence told it starts folded draws so; one built with no tell at all still draws, and its press is inert.
                    var g = createOutletFence(s, 'harbour', { branch: testBranch(), folded: true }), ghost = makeEl('div');
                    ghost.appendChild(g.fenceElement());
                    if (byClass(ghost, 'wb-fence-fold')[0].textContent !== '▸' || g.folded() !== true) return false;
                    byClass(ghost, 'wb-fence-fold')[0].dispatch('click', {});
                    return byClass(ghost, 'wb-fence-fold')[0].textContent === '▸' && told.length === 2;
                })()"""), "the toggle tells through the host's closure and paints itself only from what it is told");
    }
}
