package hue.captains.singapura.js.homing.relgrid.workbench;

import hue.captains.singapura.js.homing.ssjs.test.JsModuleTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The bench's view profiles and the panel a person picks one on, tested
 * without a grid: given the handover the grid would have asked, what does the
 * domain answer, and how does a person choose it?
 *
 * <p>RFC 0050 · Episode 2: a View is the domain's to compute, and the grid
 * presents what it is handed. What is proved here is the domain's half —
 * that each profile reads the store's values and answers the pks it should,
 * in the order it should, ties in base order; that a table remembers the
 * profile it is under and can explain it; and that the panel answers a
 * {@code RelGridView} for a choice and nothing for a cancel. No grid is
 * constructed anywhere here.</p>
 */
class DishViewsTest extends JsModuleTestBase {

    private static final String GRID_DIR = "/homing/js/hue/captains/singapura/js/homing/relgrid/";
    private static final String BENCH_DIR = GRID_DIR + "workbench/";
    private static final String PROTOCOL = GRID_DIR + "protocol/RelGridProtocolModule.js";

    /** The policy test's DOM, plus a focus-tracking activeElement and the attributes the list's ARIA needs. */
    private static final String DOM_STUB = DishPolicyTest.DOM_STUB + """
            Object.defineProperty(document, 'activeElement', { get: function () { return __focused; } });
            var __makeEl = makeEl;
            makeEl = function (tag) {
                var el = __makeEl(tag); el._a = {};
                el.setAttribute = function (k, v) { this._a[k] = String(v); };
                el.getAttribute = function (k) { return (this._a[k] == null) ? null : this._a[k]; };
                return el;
            };
            """;

    private static final String HELPERS = """
            // What the grid hands a domain with its question: a mask whose panel(element)
            // PLACES the domain's element in the grid's box — here, a host of the test's.
            function maskOf(host) { return { panel: function (el) { host.appendChild(el); return true; } }; }
            function byClass(root, c) {
                var out = [];
                (function walk(el) {
                    if ((el.className || '').split(' ').indexOf(c) >= 0) out.push(el);
                    (el.children || []).forEach(walk);
                })(root);
                return out;
            }
            """;

    @BeforeEach
    void setup() {
        js = buildContext();
        js.eval("js", DOM_STUB);
        for (String m : DishPolicyTest.PARTY) loadModule(m);
        js.eval("js", DishPolicyTest.STYLES);
        loadModule(PROTOCOL);
        loadModule(BENCH_DIR + "DishStore.js");
        loadModule(BENCH_DIR + "DishViews.js");
        js.eval("js", HELPERS);
    }

    private boolean evalBool(String expr) { return js.eval("js", expr).asBoolean(); }
    private void act(String src) { js.eval("js", src); }

    @Test
    void everyProfileReadsTheStoreAndAnswersPksInItsOrder() {
        assertTrue(evalBool("""
                (() => {
                    var store = createDishStore(), v = createDishViews(store);
                    // The seed: mapo 9.5/480/4★/71 sold, coq 18/610/2★/64, fish 12/560/5★/58,
                    // sauer 14/650/2★/49, burger 11/780/1★/88, carbo 13/720/3★/77.
                    if (v.viewFor('base').join(',') !== store.pks().join(',')) return false;
                    if (v.viewFor('cheapest').join(',') !== 'mapo,burger,fish,carbo,sauer,coq') return false;
                    // popularity is sold as a share of the best seller, so it orders as sold does.
                    if (v.viewFor('popular').join(',') !== 'burger,carbo,mapo,coq,fish,sauer') return false;
                    // stars desc, and the two 2★ dishes by price: sauer 14 before coq 18.
                    if (v.viewFor('rated').join(',') !== 'fish,mapo,carbo,sauer,coq,burger') return false;
                    // A filter: under 650 keeps three, and 650 itself is out.
                    if (v.viewFor('light').join(',') !== 'mapo,fish,coq') return false;
                    if (v.viewFor('european').join(',') !== 'fish,coq,sauer,carbo') return false;
                    // sold 60 or more keeps four (fish 58 and sauer 49 are out), dearest first.
                    if (v.viewFor('bestsellers').join(',') !== 'coq,carbo,burger,mapo') return false;
                    // Every profile is on the list, in the panel's order, base first.
                    var keys = v.profiles().map(function (p) { return p.key; }).join(',');
                    return keys === 'base,cheapest,popular,rated,light,european,bestsellers';
                })()"""), "each profile is a keep and an order over the store's values");
    }

    @Test
    void tiesFallToBaseOrderAndAViewIsAReadingOfTheStoreNow() {
        assertTrue(evalBool("""
                (() => {
                    var store = createDishStore(), v = createDishViews(store);
                    // Make coq as cheap as mapo: the tie falls to base order, mapo first.
                    store.commit('coq', 'price', 9.5);
                    if (v.viewFor('cheapest').slice(0, 2).join(',') !== 'mapo,coq') return false;
                    // A View is over the store NOW: fatten mapo past 650 and Light no longer has it.
                    store.commit('mapo', 'calories', 700);
                    if (v.viewFor('light').join(',') !== 'fish,coq') return false;
                    // Absence sorts last either way.
                    store.commit('fish', 'price', '');
                    var asc = v.viewFor('cheapest');
                    return asc[asc.length - 1] === 'fish';
                })()"""), "ties keep base order, absence sorts last, and every answer reads the store as it is");
    }

    @Test
    void aTableRemembersItsProfileAndExplainsIt() {
        assertTrue(evalBool("""
                (() => {
                    var store = createDishStore(), changed = [];
                    var v = createDishViews(store, { onChanged: function (k) { changed.push(k); } });
                    if (v.held() !== 'base') return false;
                    if (!/^As entered — .* · 6 of 6$/.test(v.describe())) return false;
                    var pks = v.choose('light');
                    if (pks.join(',') !== 'mapo,fish,coq' || v.held() !== 'light' || changed.join(',') !== 'light') return false;
                    if (v.describe() !== 'Light dishes — calories under 650 · calories ↑ · 3 of 6') return false;
                    // The explanation follows the store, not the last answer: an edit may leave
                    // the table showing 3 while the profile now reads 2.
                    store.commit('mapo', 'calories', 700);
                    if (v.describe() !== 'Light dishes — calories under 650 · calories ↑ · 2 of 6') return false;
                    // A profile that does not exist holds nothing.
                    try { v.choose('nope'); return false; } catch (e) { if (!/no profile/.test(String(e))) return false; }
                    return v.held() === 'light';
                })()"""), "one memory per table: the profile it is under, and one line that says why");
    }

    @Test
    void thePanelListsTheProfilesAndAChoiceAnswersAView() {
        act("""
                var store = createDishStore(), v = createDishViews(store);
                var host = makeEl('div'), chosen = null, answer = 'unsettled';
                var q = new RelGridViewHandover();
                var branch = hostBranch();
                dishViewPanel(v, q, maskOf(host), { branch: branch, onChosen: function (k, n) { chosen = k + ':' + n; } })
                    .then(function (a) { answer = a; });
                var root = host.children[0];
                if (branch.branchCount !== 1) throw new Error('the panel mints on a branch of its own for the session');
                var items = byClass(root, 'wb-view-item');
                """);
        assertTrue(evalBool("""
                (() => {
                    if (!root || !/wb-view/.test(root.className)) return false;
                    if (byClass(root, 'wb-view-title')[0].textContent !== 'Arrange the dishes') return false;
                    if (byClass(root, 'wb-view-sub')[0].textContent.indexOf('is waiting') < 0) return false;
                    if (items.length !== 7) return false;
                    if (items.map(function (li) { return li.profile; }).join(',') !== 'base,cheapest,popular,rated,light,european,bestsellers') return false;
                    // The held profile is marked and has the focus; every row says how many dishes it shows now.
                    if (!/wb-view-held/.test(items[0].className) || document.activeElement !== items[0]) return false;
                    if (byClass(items[4], 'wb-view-count')[0].textContent !== '3 dishes') return false;
                    // Down four: the focus walks the list.
                    for (var k = 0; k < 4; k++) root.dispatch('keydown', { key: 'ArrowDown' });
                    if (document.activeElement !== items[4]) return false;
                    if (chosen !== null || answer !== 'unsettled') return false;
                    root.dispatch('keydown', { key: 'Enter' });
                    return chosen === 'light:3' && v.held() === 'light';
                })()"""), "the panel lists every profile with its count; arrows walk it and Enter chooses");
        assertTrue(evalBool("""
                (() => {
                    // Settled on the next eval: a View, for the grid to present as given.
                    if (!(answer instanceof RelGridView)) return false;
                    if (answer.pks.join(',') !== 'mapo,fish,coq') return false;
                    // And a second choice cannot settle it again.
                    items[1].dispatch('click', {});
                    // Settled, the session's branch is dissolved: the list is out of the box.
                    return chosen === 'light:3' && branch.branchCount === 0 && host.children.length === 0;
                })()"""), "the promise settles once, with the chosen profile's View, and the panel's branch dissolves");
    }

    @Test
    void numbersClickAndCancelDoWhatTheySay() {
        act("""
                var S = createDishStore(), V = createDishViews(S);
                var nHost = makeEl('div'), nAnswer = 'unsettled';
                dishViewPanel(V, new RelGridViewHandover(), maskOf(nHost), { branch: hostBranch() }).then(function (a) { nAnswer = a; });
                nHost.children[0].dispatch('keydown', { key: '4' });
                """);
        assertTrue(evalBool("nAnswer instanceof RelGridView && nAnswer.pks.join(',') === 'fish,mapo,carbo,sauer,coq,burger' && V.held() === 'rated'"),
                "4 chooses the fourth profile");
        act("""
                var cHost = makeEl('div'), cAnswer = 'unsettled';
                dishViewPanel(V, new RelGridViewHandover(), maskOf(cHost), { branch: hostBranch() }).then(function (a) { cAnswer = a; });
                byClass(cHost, 'wb-view-item')[6].dispatch('click', {});
                """);
        assertTrue(evalBool("cAnswer instanceof RelGridView && cAnswer.pks.join(',') === 'coq,carbo,burger,mapo' && V.held() === 'bestsellers'"),
                "a click chooses, and the table now remembers that one");
        act("""
                var eHost = makeEl('div'), eAnswer = 'unsettled';
                dishViewPanel(V, new RelGridViewHandover(), maskOf(eHost), { branch: hostBranch() }).then(function (a) { eAnswer = a; });
                if (!/wb-view-held/.test(byClass(eHost, 'wb-view-item')[6].className)) throw new Error('the held profile is not marked');
                eHost.children[0].dispatch('keydown', { key: 'Escape' });
                var xHost = makeEl('div'), xAnswer = 'unsettled';
                dishViewPanel(V, new RelGridViewHandover(), maskOf(xHost), { branch: hostBranch() }).then(function (a) { xAnswer = a; });
                byClass(xHost, 'wb-view-cancel')[0].dispatch('click', {});
                """);
        assertTrue(evalBool("eAnswer === undefined && xAnswer === undefined && V.held() === 'bestsellers'"),
                "Escape and Cancel answer absence, and the table stays under what it was");
    }
}
