package hue.captains.singapura.js.homing.reltree;

import hue.captains.singapura.js.homing.ssjs.test.JsModuleTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * RFC 0050 · Episode 3-ext1, step 2 — fold and unfold as regular questions on
 * the channel: answered at once with the whole View, answered late under the
 * mask, answered with nothing and told later, refused for a leaf or an open
 * node; the tree locked while one is pending; a fold that frees; the tree's
 * branch bounded across a thousand folds.
 *
 * <p>GraalVM drains microtasks between evals, not within one, so anything
 * that waits on a promise acts in one eval and asserts in the next; timers
 * are fake and stepped.</p>
 */
class RelTreeFoldTest extends JsModuleTestBase {

    private static final String HELPERS = """
            function countTree(b) {
                var n = b.elementCount;
                b.listBranches().forEach(function (name) { n += countTree(b.getBranch(name)); });
                return n;
            }
            """;

    @BeforeEach
    void setup() {
        js = buildContext();
        js.eval("js", RelTreeTestDom.DOM_STUB);
        js.eval("js", RelTreeTestDom.STYLES);
        for (String m : RelTreeTestDom.PARTY) loadModule(m);
        loadModule(RelTreeTestDom.CHANNEL);
        loadModule(RelTreeTestDom.PROTOCOL);
        for (String m : RelTreeTestDom.MODULES) loadModule(RelTreeTestDom.DIR + m);
        js.eval("js", RelTreeTestDom.FIXTURE);
        js.eval("js", HELPERS);
    }

    private boolean evalBool(String expr) { return js.eval("js", expr).asBoolean(); }
    private void act(String code) { js.eval("js", code); }

    @Test
    void anUnfoldIsAskedAndAnsweredAtOnceWithTheWholeView() {
        act("""
                var F = fixture();
                var taken = F.tree.unfold('a');
                """);
        assertTrue(evalBool("""
                (() => {
                    // Asked: the question names the node; the tree is pending until the answer lands.
                    if (!taken || !(F.lastSent(RelTreeUnfold) instanceof RelTreeUnfold) || F.lastSent(RelTreeUnfold).key !== 'a') return false;
                    return F.sent.length === 1;
                })()"""), "unfold asks RelTreeUnfold(key) on the channel");
        assertTrue(evalBool("""
                (() => {
                    // Answered on a microtask: presented whole, no mask ever, not pending.
                    if (F.shown() !== 'a:0:open a1:1:leaf a2:1:closed b:0:leaf c:0:closed') return false;
                    if (F.drawn() !== '\\u25BEA@0 A1@1 \\u25B8A2@1 B@0 \\u25B8C@0') return false;
                    if (F.mask() !== null || F.tree.isPending() || pendingTimers() !== 0) return false;
                    if (F.arranged.join() !== 'base,unfold') return false;
                    // An unfold of an open node, of a leaf, of a stranger: nothing is asked.
                    if (F.tree.unfold('a') || F.tree.unfold('b') || F.tree.unfold('zz') || F.sent.length !== 1) return false;
                    // A fold of a closed node, of a leaf: nothing asked; of the open one: asked.
                    if (F.tree.fold('a2') || F.tree.fold('b') || !F.tree.fold('a')) return false;
                    return F.lastSent(RelTreeFold).key === 'a' && F.sent.length === 2;
                })()"""), "the answer is presented whole and at once; a fold or unfold that cannot apply is not asked");
        assertTrue(evalBool("""
                (() => {
                    if (F.shown() !== 'a:0:closed b:0:leaf c:0:closed') return false;
                    // A fold frees: the cells that left are out of the tree and forgotten; the rows shrank at the tail.
                    return F.tree.cells().size() === 3 && F.tree.cells().get('a1') === null && F.treeEl().children.length === 3;
                })()"""), "a fold withdraws the children and the tree forgets their cells");
    }

    @Test
    void aLateAnswerLocksAndMasksTheTreeUntilItLands() {
        act("""
                var answer = null;
                var G = fixture({ ask: function (q) {
                    if (q instanceof RelTreeUnfold) { G.relation.open(q.key); return new Promise(function (r) { answer = r; }); }
                    return Promise.resolve();
                }});
                G.click(0);
                G.tree.unfold('a');
                """);
        assertTrue(evalBool("""
                (() => {
                    if (!G.tree.isPending() || G.mask() !== null || pendingTimers() !== 1) return false;   // not yet: the delay
                    // LOCKED: every intent refused while the answer is owed.
                    if (G.key('ArrowDown') || G.current() !== 0 || G.tree.selectNode('b') || G.tree.fold('a') || G.tree.unfold('c')) return false;
                    if (G.sent.length !== 2) return false;                     // the cursor's notification, and the unfold
                    runTimers();                                                // the threshold passes
                    var m = G.mask();
                    if (!m || G.panel() !== null) return false;                  // a bare wash: nothing invented
                    if (!/hrt-masked/.test(G.treeEl().className)) return false;
                    return document.activeElement === m;
                })()"""), "a slow answer masks after the threshold, and the tree refuses every intent meanwhile");
        act("answer(new RelTreeView(G.relation.places()));");
        assertTrue(evalBool("""
                (() => {
                    // Landed: presented at once, no longer pending — but the mask is held so it never strobes.
                    if (G.shown() !== 'a:0:open a1:1:leaf a2:1:closed b:0:leaf c:0:closed') return false;
                    if (G.tree.isPending() || G.mask() === null || pendingTimers() !== 1) return false;
                    runTimers();                                                // the hold ends
                    if (G.mask() !== null || /hrt-masked/.test(G.treeEl().className)) return false;
                    if (document.activeElement !== G.treeEl()) return false;    // the keys come home
                    // The cursor kept its node across the answer; a key works again.
                    return G.tree.cursor() === 'a' && G.key('ArrowDown') && G.tree.cursor() === 'a1';
                })()"""), "the answer applies at once; the mask comes down after its hold; the focus returns");
    }

    @Test
    void thePanelIsTheDomainsToFillAndAnsweredWithNothingTheRowsStay() {
        act("""
                var H_answer = null;
                var H = fixture({ ask: function (q, mask) {
                    if (!(q instanceof RelTreeUnfold)) return Promise.resolve();
                    var note = makeEl('div'); note.textContent = 'fetching ' + q.key;
                    mask.panel(note);                                           // the domain's element, in the tree's box, at once
                    return new Promise(function (r) { H_answer = r; });         // and, in the end, nothing — the rows stay
                }});
                H.tree.unfold('c');
                """);
        assertTrue(evalBool("""
                (() => {
                    // Mounted at once by the panel: the mask and the box, holding the domain's note.
                    if (H.mask() === null || H.panel() === null || H.panel().children[0].textContent !== 'fetching c') return false;
                    return H.tree.isPending() && pendingTimers() === 0;         // mounted by the panel: the delay clock is gone
                })()"""), "the panel handle mounts the mask and places the domain's element");
        act("H_answer();");
        assertTrue(evalBool("""
                (() => {
                    // Nothing came back: the rows stay; the panel is down at once, the wash held.
                    if (H.shown() !== 'a:0:closed b:0:leaf c:0:closed' || H.tree.isPending()) return false;
                    if (H.panel() !== null || H.mask() === null) return false;
                    runTimers();
                    return H.mask() === null;
                })()"""), "absence leaves the rows as they were");
    }

    @Test
    void nothingNowAndToldLaterIsTheSecondWayToBeLazy() {
        act("""
                var K = fixture({ ask: function (q) {
                    if (q instanceof RelTreeUnfold) {
                        // The domain fetches: nothing now — the tree is free — and a tell when the children come.
                        setTimeout(function () { K.relation.open(q.key); K.tree.tell(new RelTreeViewChanged()); }, 500);
                        return Promise.resolve();
                    }
                    return Promise.resolve();
                }});
                K.click(0);
                K.tree.unfold('a');
                """);
        assertTrue(evalBool("""
                (() => {
                    // Not pending: the answer was nothing. The tree works meanwhile — a key moves the cursor.
                    if (K.tree.isPending() || K.mask() !== null) return false;
                    if (K.shown() !== 'a:0:closed b:0:leaf c:0:closed' || !K.key('ArrowDown') || K.tree.cursor() !== 'b') return false;
                    runTimers();                                                // the fetch lands, and tells
                    if (K.shown() !== 'a:0:open a1:1:leaf a2:1:closed b:0:leaf c:0:closed') return false;
                    // The cursor kept its node through the re-ask: b, now in row 3.
                    return K.tree.cursor() === 'b' && K.current() === 3 && K.arranged.join() === 'base,told';
                })()"""), "a domain that answers nothing and tells later never locks the tree");
    }

    @Test
    void aMalformedOrForeignAnswerIsRecordedAndTheRowsStay() {
        act("""
                var errors = [];
                var __err = console.error; console.error = function () { errors.push(Array.prototype.join.call(arguments, ' ')); __err.apply(console, arguments); };
                var M = fixture({ ask: function (q) {
                    if (q instanceof RelTreeUnfold) return Promise.resolve(q.key === 'a' ? { places: [] } : new RelTreeView([{ key: 'zz', depth: 0, fold: 'leaf' }]));
                    return Promise.resolve();
                }});
                M.tree.unfold('a');
                """);
        assertTrue(evalBool("""
                (() => {
                    // Not a tree View: recorded, not applied.
                    if (M.shown() !== 'a:0:closed b:0:leaf c:0:closed' || M.tree.isPending()) return false;
                    if (!errors.some(function (e) { return /not a tree View/.test(e); })) return false;
                    M.tree.unfold('c');
                    return true;
                })()"""), "an answer that is not a View is recorded and ignored");
        assertTrue(evalBool("""
                (() => {
                    // A stranger in the answer: refused whole in the apply, recorded by the settle, the rows stay.
                    if (M.shown() !== 'a:0:closed b:0:leaf c:0:closed' || M.tree.isPending()) return false;
                    return errors.some(function (e) { return /applying an answer threw/.test(e) && /no such node: zz/.test(e); });
                })()"""), "a View naming a stranger is refused whole and recorded");
    }

    @Test
    void theTreesBranchIsBoundedAcrossAThousandFolds() {
        act("""
                var T = fixture(), T_max = 0, T_rows = T.branch.getBranch('rows');
                var T_base = countTree(T.branch);
                """);
        // Each fold round-trips a microtask; drive them one eval at a time.
        for (int k = 0; k < 1000; k++) {
            act("T.tree." + (k % 2 == 0 ? "unfold" : "fold") + "('a'); T_max = Math.max(T_max, countTree(T.branch));");
        }
        assertTrue(evalBool("""
                (() => {
                    // Closed at the end; the tree's branch is what it was at the start, and never exceeded
                    // the open shape — rows and carets for five rows, plus the wrap and the tree.
                    if (T.shown() !== 'a:0:closed b:0:leaf c:0:closed') return false;
                    if (countTree(T.branch) !== T_base || T_max !== 2 + 5 * 2) return false;
                    // The rows branch is the same branch throughout: grown and shrunk, never re-minted.
                    if (T.branch.getBranch('rows') !== T_rows) return false;
                    // The domain kept its cells: the tree forgot a1 and a2 five hundred times and asked again each time.
                    return T.tree.cells().size() === 3 && T.asked() === 3 + 2 * 500 && T.relation.cell('a1') !== null;
                })()"""), "a thousand folds leave the tree's branch exactly as it was");
    }
}
