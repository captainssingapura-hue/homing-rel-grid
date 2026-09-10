package hue.captains.singapura.js.homing.relgrid.workbench;

import hue.captains.singapura.js.homing.ssjs.test.JsModuleTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The Replicating Tables bench's DOMAIN, tested without a grid: who may edit
 * which column is each relation's rule, what may be written at all is the
 * store's, and a derived column follows its source into every relation's own
 * cells. No grid is constructed anywhere in this class, which is the point —
 * every one of these behaviours lives on the domain's side of the seam.
 *
 * <p>RFC 0050 · Episode 2, map 15 law 105 and map 16 law 113: a cell that
 * declines when asked is the situational "no", decided by the cell each time
 * and never declared to the grid. The relation's {@code readOnlyColumns()} is
 * the other one — structural, declared once, and answered without consulting a
 * cell at all. Both are exercised here, and neither substitutes for the
 * other.</p>
 */
class DishPolicyTest extends JsModuleTestBase {

    private static final String GRID_DIR = "/homing/js/hue/captains/singapura/js/homing/relgrid/";
    private static final String BENCH_DIR = GRID_DIR + "workbench/";

    /** Enough DOM for a stock cell to open and close an input, and a localStorage the store can persist to. */
    private static final String DOM_STUB = """
            var __focused = null;
            function makeEl(tag) {
                var el = { tagName: tag, id: '', className: '', textContent: '', value: '', children: [], parentNode: null, _l: {},
                    appendChild: function (c) { if (c.parentNode) c.parentNode.removeChild(c); c.parentNode = this; this.children.push(c); return c; },
                    removeChild: function (c) {
                        var i = this.children.indexOf(c);
                        if (i < 0) throw new Error('NotFoundError: not a child');
                        this.children.splice(i, 1);
                        if (__focused === c) { __focused = null; c.dispatch('blur', {}); }
                        c.parentNode = null; return c;
                    },
                    addEventListener: function (t, fn) { (this._l[t] = this._l[t] || []).push(fn); },
                    removeEventListener: function (t, fn) { var l = this._l[t] || [], i = l.indexOf(fn); if (i >= 0) l.splice(i, 1); },
                    dispatch: function (t, ev) {
                        ev = ev || {};
                        if (!ev.preventDefault) ev.preventDefault = function () {};
                        if (!ev.stopPropagation) ev.stopPropagation = function () {};
                        (this._l[t] || []).slice().forEach(function (fn) { fn(ev); });
                    },
                    focus: function () { var p = __focused; if (p === this) return; __focused = this; if (p) p.dispatch('blur', {}); },
                    select: function () {} };
                return el;
            }
            var document = {
                head: makeEl('head'), body: makeEl('body'),
                createElement: function (t) { return makeEl(t); },
                getElementById: function (id) {
                    var h = this.head.children;
                    for (var i = 0; i < h.length; i++) if (h[i].id === id) return h[i];
                    return null;
                }
            };
            var __mem = {};
            var localStorage = {
                getItem: function (k) { return Object.prototype.hasOwnProperty.call(__mem, k) ? __mem[k] : null; },
                setItem: function (k, v) { __mem[k] = String(v); },
                removeItem: function (k) { delete __mem[k]; }
            };
            var console = console || { error: function () {} };
            """;

    /** Owner-side helpers: mount a cell into a host, ask it to edit, type and commit. No grid. */
    private static final String HELPERS = """
            function mount(rel, pk, col) { var c = rel.cellFor(pk, col); if (!c._el) c.render(makeEl('div')); return c; }
            // The cell's own half of the two-stage handover. Stage one is the whole
            // question here: may this cell be written at all? Nothing is opened.
            function mayEdit(rel, pk, col) { return mount(rel, pk, col).mayTakeControl() === true; }
            function openEdit(rel, pk, col) {
                var c = mount(rel, pk, col);
                if (!c.mayTakeControl()) return null;
                c.takeControl();                           // a promise the OWNER settles
                return c;
            }
            function chooseAndChange(cell, n) {
                var sel = cell._el.children[0];
                sel.value = String(n);
                sel.dispatch('change', {});
            }
            function typeAndEnter(cell, text) {
                var input = cell._el.children[0];
                input.value = text;
                input.dispatch('keydown', { key: 'Enter' });
            }
            """;

    @BeforeEach
    void setup() {
        js = buildContext();
        js.eval("js", DOM_STUB);
        loadModule(GRID_DIR + "RelGridStockCellsModule.js");
        loadModule(BENCH_DIR + "DishStarsCellModule.js");
        loadModule(BENCH_DIR + "DishStore.js");
        loadModule(BENCH_DIR + "DishRelation.js");
        js.eval("js", HELPERS);
    }

    private boolean evalBool(String expr) { return js.eval("js", expr).asBoolean(); }

    @Test
    void eachRoleEditsItsOwnColumnsAndNoOthers() {
        assertTrue(evalBool("""
                (() => {
                    var store = createDishStore(), roles = dishRoles(), cols = store.columns();
                    var ok = true;
                    Object.keys(roles).forEach(function (role) {
                        var rel = createDishRelation(store, { role: role });
                        cols.forEach(function (col) {
                            var may = roles[role].indexOf(col) >= 0;
                            if (mayEdit(rel, 'mapo', col) !== may) ok = false;    // asked each time, answered each time
                        });
                        if (rel.role() !== role || rel.editableColumns().join() !== roles[role].join()) ok = false;
                    });
                    return ok
                        && roles.chef.join() === 'ingredient,style'
                        && roles.nutritionist.join() === 'calories,stars'
                        && roles.manager.join() === 'price'
                        && roles.follower.length === 0
                        && Object.keys(roles).every(function (r) {
                               return roles[r].indexOf('sold') < 0 && roles[r].indexOf('popularity') < 0; });
                })()"""), "a role's cells say yes on exactly its columns; sold and popularity are nobody's");
    }

    @Test
    void whatNobodyMayWriteIsTheStoresRuleWhoeverAsks() {
        assertTrue(evalBool("""
                (() => {
                    var store = createDishStore();
                    var pop = store.get('mapo', 'popularity'), sold = store.get('mapo', 'sold'), rev = store.revision();
                    var a = store.commit('mapo', 'popularity', 5);
                    var b = store.commit('mapo', 'sold', 5);
                    var c = store.commit('mapo', 'nonsense', 5);
                    var threw = false;
                    try { createDishRelation(store, { role: 'owner' }); } catch (e) { threw = true; }
                    return a === false && b === false && c === false
                        && store.get('mapo', 'popularity') === pop && store.get('mapo', 'sold') === sold
                        && store.revision() === rev
                        && store.writableColumns().join() === 'ingredient,style,calories,stars,price'
                        && threw;
                })()"""), "the store refuses a commit to sold or popularity, and an unknown role at construction");
    }

    @Test
    void aSaleRederivesPopularityIntoEveryRelationsOwnCellsWithoutACommit() {
        assertTrue(evalBool("""
                (() => {
                    var store = createDishStore(), pks = store.pks();
                    var rels = ['nutritionist', 'manager', 'follower'].map(function (r) { return createDishRelation(store, { role: r }); });
                    rels.forEach(function (rel) { pks.forEach(function (pk) { mount(rel, pk, 'popularity'); mount(rel, pk, 'sold'); }); });
                    // Seed: burger is the best seller (88); sauer sold 49 of that.
                    if (store.get('burger', 'popularity') !== 100 || store.get('sauer', 'popularity') !== 56) return false;
                    var heard = [];
                    store.subscribe(function (pk, col) { heard.push(pk + ' ' + col); });
                    var rev = store.revision();
                    store.sell('sauer', 200);                                       // 249 — sauer is now the best seller
                    if (store.revision() !== rev + 1) return false;                 // one sale, one revision
                    if (store.get('sauer', 'popularity') !== 100) return false;
                    if (store.get('burger', 'popularity') !== Math.round(100 * 88 / 249)) return false;
                    // Every relation's own cells show the derived value, and nobody committed anything.
                    var shown = rels.every(function (rel) { return pks.every(function (pk) {
                        return rel.cellFor(pk, 'popularity').value() === store.get(pk, 'popularity')
                            && rel.cellFor(pk, 'sold').value() === store.get(pk, 'sold'); }); });
                    // Subscribers heard the source once, then each derived cell that changed — all six did.
                    return shown && heard[0] === 'sauer sold'
                        && heard.filter(function (h) { return /popularity$/.test(h); }).length === 6;
                })()"""), "a sale moves sold, re-derives popularity for every dish, and reaches every relation's cells");
    }

    @Test
    void anEditorsCommitReachesEveryOtherRelationThroughTheStoreAlone() {
        assertTrue(evalBool("""
                (() => {
                    var store = createDishStore();
                    var nut = createDishRelation(store, { role: 'nutritionist' });
                    var mgr = createDishRelation(store, { role: 'manager' });
                    var fol = createDishRelation(store, { role: 'follower' });
                    var mCal = mount(mgr, 'mapo', 'calories'), fCal = mount(fol, 'mapo', 'calories');
                    var mPrice = mount(mgr, 'mapo', 'price');
                    var cell = openEdit(nut, 'mapo', 'calories');
                    if (!cell) return false;
                    typeAndEnter(cell, '500');
                    return store.get('mapo', 'calories') === 500                     // coerced by the relation's rule
                        && cell.value() === 500 && mCal.value() === 500 && fCal.value() === 500
                        && mPrice.value() === 9.5                                     // nothing else moved
                        && cell._el.children.length === 0;                           // the input is gone
                })()"""), "Enter in the nutritionist's cell commits to the store, and every relation's cell follows");
    }

    @Test
    void theDerivedColumnIsRecomputedFromWhatWasPersistedNotStored() {
        assertTrue(evalBool("""
                (() => {
                    var a = createDishStore();
                    a.sell('fish', 100);                                              // 158 — fish is the best seller
                    var b = createDishStore();                                        // loads what a persisted
                    var raw = JSON.parse(localStorage.getItem('bench.replicatingTables.dishes.v3'));
                    return b.get('fish', 'sold') === 158 && b.get('fish', 'popularity') === 100
                        && b.get('burger', 'popularity') === Math.round(100 * 88 / 158)
                        && raw.fish.sold === 158 && raw.fish.popularity === undefined;   // derived is never persisted
                })()"""), "sold is persisted and popularity is derived again on load");
    }

    @Test
    void theRatingIsADropdownAndTheContractDoesNotNotice() {
        assertTrue(evalBool("""
                (() => {
                    var store = createDishStore();
                    var nut = createDishRelation(store, { role: 'nutritionist' });
                    var fol = createDishRelation(store, { role: 'follower' });
                    var cell = mount(nut, 'mapo', 'stars');

                    // A DIFFERENT KIND of cell, answering the same contract.
                    if (!(cell instanceof DishStarsCell)) return false;
                    if (nut.cellFor('mapo', 'calories') instanceof DishStarsCell) return false;
                    if (cell.value() !== 4 || cell._el.textContent !== '\u2605\u2605\u2605\u2605\u2606') return false;

                    // Stage one, then stage two: a dropdown of the five ratings.
                    if (cell.mayTakeControl() !== true) return false;
                    var out = cell.takeControl();
                    if (!out || typeof out.then !== 'function') return false;   // a THENABLE, as the contract demands
                    if (cell.mayTakeControl() !== false) return false;          // already open: no
                    var sel = cell._el.children[0];
                    if (sel.tagName !== 'select' || sel.children.length !== 5) return false;
                    if (sel.value !== '4') return false;                        // opens on what it shows

                    // It commits on CHANGE, not on Enter — and the owner coerces to a number.
                    chooseAndChange(cell, 2);
                    if (store.get('mapo', 'stars') !== 2) return false;
                    if (cell._el.children.length !== 0) return false;           // the select is gone
                    if (cell._el.textContent !== '\u2605\u2605\u2606\u2606\u2606') return false;
                    // And the follower's own stars cell followed, through the store.
                    return mount(fol, 'mapo', 'stars').value() === 2;
                })()"""), "a rating is a dropdown; it answers the same two-stage contract as a text cell");
    }

    @Test
    void theDropdownIsLaidOverTheCellRatherThanInIt() {
        assertTrue(evalBool("""
                (() => {
                    var store = createDishStore();
                    var nut = createDishRelation(store, { role: 'nutritionist' });
                    var cell = mount(nut, 'mapo', 'stars');
                    var before = cell._el.textContent;
                    cell.takeControl();
                    // The stars stay underneath, holding the cell open at its own size;
                    // the dropdown covers them. A <select> left IN flow would set the
                    // column to its longest option plus an arrow and push every other
                    // column aside while it was open.
                    return cell._el.textContent === before
                        && cell._el.children.length === 1
                        && cell._el.children[0].tagName === 'select';
                })()"""), "the dropdown overlays the cell and the cell keeps its own size");
    }

    @Test
    void escapingTheDropdownSettlesWithoutCommitting() {
        assertTrue(evalBool("""
                (() => {
                    var store = createDishStore();
                    var nut = createDishRelation(store, { role: 'nutritionist' });
                    var cell = mount(nut, 'mapo', 'stars');
                    var settled = 0;
                    cell.takeControl().then(function () { settled++; });
                    var sel = cell._el.children[0];
                    sel.value = '1';
                    sel.dispatch('keydown', { key: 'Escape' });
                    if (store.get('mapo', 'stars') !== 4) return false;         // nothing committed
                    if (cell.value() !== 4 || cell._el.children.length !== 0) return false;

                    // A blur cancels too, and neither may leave the cell open.
                    cell.takeControl();
                    cell._el.children[0].dispatch('blur', {});
                    return store.get('mapo', 'stars') === 4 && cell._el.children.length === 0
                        && cell.mayTakeControl() === true;                       // ready to be asked again
                })()"""), "Escape and blur end the dropdown without committing, and settle the handover");
    }

    @Test
    void aCellThatCannotEditNamesThePropertyOnItsHost() {
        assertTrue(evalBool("""
                (() => {
                    var store = createDishStore();
                    var mgr = createDishRelation(store, { role: 'manager' });
                    var fol = createDishRelation(store, { role: 'follower' });
                    var ro = /\\bhrg-text-ro\\b/;
                    return !ro.test(mount(mgr, 'mapo', 'price')._el.className)        // the manager's price: editable
                        &&  ro.test(mount(mgr, 'mapo', 'calories')._el.className)     // not the manager's
                        &&  ro.test(mount(mgr, 'mapo', 'popularity')._el.className)   // nobody's
                        &&  ro.test(mount(fol, 'mapo', 'price')._el.className)        // a follower's anything
                        && document.getElementById('homing-rel-grid-stock-style') !== null;
                })()"""), "law 116: an uneditable cell has no affordance to be missing, so it names the property");
    }
}
