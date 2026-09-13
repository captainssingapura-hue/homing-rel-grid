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

    /** The DomOpsParty — the real one, from homing-core-js: every cell, fence and panel mints on a branch of it. */
    static final String[] PARTY = {
            "/homing/js/hue/captains/singapura/js/homing/core/js/DomOpsPartyBaseModule.js",
            "/homing/js/hue/captains/singapura/js/homing/core/js/DomOpsPartyModule.js" };

    /** Enough DOM for a stock cell to open and close an input, and a localStorage the store can persist to. */
    static final String DOM_STUB = """
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
                    select: function () {},
                    remove: function () { if (this.parentNode) this.parentNode.removeChild(this); } };
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
            // A branch of the real party for one domain object to own — a relation, a
            // fence — as a widget hands it: UNACTIVATED, the owner activates. And a
            // branch the test itself divides, as a widget divides its 'domain': activated.
            var __branchSeq = 0, __branchOwner = { toString: function () { return 'test widget'; } };
            function testBranch() { return domOpsParty.createBranch('t' + (++__branchSeq)); }
            function hostBranch() { var b = testBranch(); b.activate(__branchOwner); return b; }
            """;

    /** Owner-side helpers: a relation over its own branch, a cell asked for its element, an edit driven. No grid. */
    private static final String HELPERS = """
            function relationOf(store, role) { return createDishRelation(store, { role: role, branch: testBranch() }); }
            // A cell is a NOUN: asked for its element, as the grid would ask, and nothing rendered into it.
            function mount(rel, pk, col) { var c = rel.cellFor(pk, col); c.cellElement(); return c; }
            // The cell's own half of the two-stage handover. Stage one is the whole
            // question here: may this cell be written at all? Nothing is opened.
            function mayEdit(rel, pk, col) { return mount(rel, pk, col).mayTakeControl() === true; }
            // Open as the grid opens: the editor asked for and placed in an anchor, then control taken.
            function openEdit(rel, pk, col, host) {
                var c = mount(rel, pk, col);
                if (!c.mayTakeControl()) return null;
                (host || makeEl('div')).appendChild(c.editorElement());
                c.takeControl();                           // a promise the OWNER settles
                return c;
            }
            // Drive the rating panel the way a person does: arrows to change, Enter to commit.
            function panelOf(cell) { return cell.editorElement(); }
            function press(cell, key) { panelOf(cell).dispatch('keydown', { key: key }); }
            function typeAndEnter(cell, text) {
                var input = cell.editorElement();
                input.value = text;
                input.dispatch('keydown', { key: 'Enter' });
            }
            """;

    @BeforeEach
    void setup() {
        js = buildContext();
        js.eval("js", DOM_STUB);
        for (String m : PARTY) loadModule(m);
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
                        var rel = relationOf(store, role);
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
                    try { relationOf(store, 'owner'); } catch (e) { threw = true; }
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
                    var rels = ['nutritionist', 'manager', 'follower'].map(function (r) { return relationOf(store, r); });
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
                    var nut = relationOf(store, 'nutritionist');
                    var mgr = relationOf(store, 'manager');
                    var fol = relationOf(store, 'follower');
                    var mCal = mount(mgr, 'mapo', 'calories'), fCal = mount(fol, 'mapo', 'calories');
                    var mPrice = mount(mgr, 'mapo', 'price');
                    var cell = openEdit(nut, 'mapo', 'calories');
                    if (!cell) return false;
                    typeAndEnter(cell, '500');
                    return store.get('mapo', 'calories') === 500                     // coerced by the relation's rule
                        && cell.value() === 500 && mCal.value() === 500 && fCal.value() === 500
                        && mPrice.value() === 9.5                                     // nothing else moved
                        && cell.cellElement().children.length === 0                  // the input was never in the cell
                        && cell.mayTakeControl() === true;                           // and the edit is over
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
    void theRatingIsACustomControlAndTheContractDoesNotNotice() {
        assertTrue(evalBool("""
                (() => {
                    var store = createDishStore();
                    var nut = relationOf(store, 'nutritionist');
                    var fol = relationOf(store, 'follower');
                    var cell = mount(nut, 'mapo', 'stars');

                    // A DIFFERENT KIND of cell, answering the same contract.
                    if (!(cell instanceof DishStarsCell)) return false;
                    if (nut.cellFor('mapo', 'calories') instanceof DishStarsCell) return false;
                    if (cell.value() !== 4 || cell.cellElement().textContent !== '\u2605\u2605\u2605\u2605\u2606') return false;

                    // Stage one, then the editor, then stage two. The editor is a PANEL, not
                    // a form element — five stars and a hint — and the contract does not care.
                    if (cell.mayTakeControl() !== true) return false;
                    var host = makeEl('div');
                    var panel = cell.editorElement();
                    if (panel !== cell.editorElement()) return false;           // a noun: the same one every time
                    host.appendChild(panel);                                    // placed, as the grid places it
                    var out = cell.takeControl();
                    if (!out || typeof out.then !== 'function') return false;   // a THENABLE, as the contract demands
                    if (cell.mayTakeControl() !== false) return false;          // already open: no
                    if (!/wb-stars-panel/.test(panel.className)) return false;
                    if (panel.children[0].children.length !== 5) return false;  // five stars
                    if (cell.draft() !== 4) return false;                       // opens on what it shows
                    if (cell.cellElement().children.length !== 0) return false; // NOT in the cell

                    // It OWNS the arrows: right adds a star, left takes one away, and
                    // both clamp. Nothing is committed until Enter.
                    press(cell, 'ArrowRight');
                    if (cell.draft() !== 5) return false;
                    press(cell, 'ArrowRight');
                    if (cell.draft() !== 5) return false;                       // clamped at five
                    press(cell, 'ArrowLeft'); press(cell, 'ArrowLeft'); press(cell, 'ArrowLeft');
                    if (cell.draft() !== 2) return false;
                    if (store.get('mapo', 'stars') !== 4) return false;         // still nothing committed

                    // Up and down are dead on purpose — a rating has one axis.
                    press(cell, 'ArrowUp'); press(cell, 'ArrowDown');
                    if (cell.draft() !== 2) return false;

                    press(cell, 'Enter');
                    if (store.get('mapo', 'stars') !== 2) return false;
                    if (cell.mayTakeControl() !== true) return false;           // finished: the anchor is the grid's to take down
                    if (cell.cellElement().textContent !== '\u2605\u2605\u2606\u2606\u2606') return false;
                    // And the follower's own stars cell followed, through the store.
                    return mount(fol, 'mapo', 'stars').value() === 2;
                })()"""), "a rating is a custom control that owns the arrows, on the same two-stage contract");
    }

    @Test
    void theEditorIsItsOwnElementAndNeverGoesIntoTheCell() {
        assertTrue(evalBool("""
                (() => {
                    var store = createDishStore();
                    var nut = relationOf(store, 'nutritionist');
                    var cell = mount(nut, 'mapo', 'stars');
                    var before = cell.cellElement().textContent;
                    var host = makeEl('div');
                    openEdit(nut, 'mapo', 'stars', host);
                    // The editor is an element of the cell's own, placed wherever the grid
                    // places it. The cell's own element is untouched — still showing exactly
                    // what it showed — which is why a panel far larger than the cell costs
                    // the table nothing at all.
                    return cell.cellElement().textContent === before
                        && cell.cellElement().children.length === 0
                        && host.children.length === 1
                        && host.children[0] === cell.editorElement()
                        && /wb-stars-panel/.test(host.children[0].className);
                })()"""), "the editor is the cell's own element, placed by the grid, and the cell is left exactly as it was");
    }

    @Test
    void escapingTheRatingPanelSettlesWithoutCommitting() {
        assertTrue(evalBool("""
                (() => {
                    var store = createDishStore();
                    var nut = relationOf(store, 'nutritionist');
                    var cell = openEdit(nut, 'mapo', 'stars');
                    press(cell, 'ArrowLeft'); press(cell, 'ArrowLeft'); press(cell, 'ArrowLeft');
                    if (cell.draft() !== 1) return false;                        // chosen, not committed
                    press(cell, 'Escape');
                    if (store.get('mapo', 'stars') !== 4) return false;          // nothing committed
                    if (cell.value() !== 4 || cell.mayTakeControl() !== true) return false;

                    // A blur cancels too, and neither may leave the cell open.
                    openEdit(nut, 'mapo', 'stars');
                    cell.editorElement().dispatch('blur', {});
                    return store.get('mapo', 'stars') === 4
                        && cell.mayTakeControl() === true;                       // ready to be asked again
                })()"""), "Escape and blur end the panel without committing, and settle the handover");
    }

    @Test
    void aCellThatCannotEditNamesThePropertyOnItsHost() {
        assertTrue(evalBool("""
                (() => {
                    var store = createDishStore();
                    var mgr = relationOf(store, 'manager');
                    var fol = relationOf(store, 'follower');
                    var ro = /\\bhrg-text-ro\\b/;
                    var cls = function (rel, col) { return mount(rel, 'mapo', col).cellElement().className; };
                    return !ro.test(cls(mgr, 'price'))                                  // the manager's price: editable
                        &&  ro.test(cls(mgr, 'calories'))                               // not the manager's
                        &&  ro.test(cls(mgr, 'popularity'))                             // nobody's
                        &&  ro.test(cls(fol, 'price'))                                  // a follower's anything
                        && document.getElementById('homing-rel-grid-stock-style') !== null;
                })()"""), "law 116: an uneditable cell has no affordance to be missing, so it names the property");
    }
}
