package hue.captains.singapura.js.homing.relgrid.workbench;

import hue.captains.singapura.js.homing.relgrid.RelGridTestDom;
import hue.captains.singapura.js.homing.reltree.RelTreeTestDom;
import hue.captains.singapura.js.homing.ssjs.test.JsModuleTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The Games Tree bench: the catalogue as a lazy three-level tree over the
 * tree view, the fold and unfold answered on the channel three ways, and the
 * navigator's move — the domain opens the ancestors, tells, asks for the
 * cursor.
 */
class GamesTreeTest extends JsModuleTestBase {

    private static final String BENCH_DIR = RelGridTestDom.DIR + "workbench/";

    private static final String HELPERS = """
            var PSV = %s;
            function countTree(b) {
                var n = b.elementCount;
                b.listBranches().forEach(function (name) { n += countTree(b.getBranch(name)); });
                return n;
            }
            // The bench, as the widget wires it: two branches under a host's, the relation on the
            // domain's, the tree on its own, the channel the relation's answer, the tell through the host.
            function bench(opts) {
                opts = opts || {};
                var hostB = hostBranch();
                var treeB = hostB.createBranch('tree'), domainB = hostB.createBranch('domain');
                domainB.activate({ toString: function () { return 'bench'; } });
                var tree = null, told = 0;
                var store = createGamesStore(PSV);
                var relation = createGamesTreeRelation(store, {
                    branch: domainB.createBranch('cells'), delay: 300,
                    onViewChanged: function () { told++; if (tree) tree.tell(new RelTreeViewChanged()); }
                });
                var container = makeEl('div'), moves = [];
                tree = new RelTree({ container: container, branch: treeB, relation: relation,
                                     ask: function (q, mask) { return relation.answer(q, mask); },
                                     onCursorMoved: function (k) { moves.push(k); } });
                function shown() { var out = [], p = tree.places(); for (var i = 0; i < p.rows(); i++) out.push(p.keyAt(i)); return out; }
                function textAt(i) { return container.children[0].children[0].children[i].children[1].textContent; }
                function mask() { var w = container.children[0]; for (var k = 0; k < w.children.length; k++) if ((w.children[k].className || '').split(' ').indexOf('hrt-mask') >= 0) return w.children[k]; return null; }
                return { tree: tree, relation: relation, store: store, treeB: treeB, cellsB: domainB.getBranch('cells'),
                         shown: shown, textAt: textAt, mask: mask, moves: moves, told: function () { return told; } };
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
        loadModule(BENCH_DIR + "GamesStore.js");
        loadModule(BENCH_DIR + "GamesTreeRelation.js");
        js.eval("js", HELPERS.formatted(HanStressWidget.jsString(GamesDataset.psv())));
    }

    private boolean evalBool(String expr) { return js.eval("js", expr).asBoolean(); }
    private void act(String code) { js.eval("js", code); }

    @Test
    void theCatalogueIsATreeOfTypesClosedAtFirstAndNothingUnderThemIsBuilt() {
        assertTrue(evalBool("""
                (() => {
                    var B = bench(), types = B.relation.types();
                    // Every type a root, closed, most releases first; the cells made are exactly the roots.
                    if (types.length < 15 || types[0] !== 'Sports' || B.shown().length !== types.length) return false;
                    if (B.tree.places().foldAt(0) !== 'closed' || B.relation.cellCount() !== types.length) return false;
                    if (!/^Sports \\u2014 \\d+ releases in \\d+ series$/.test(B.textAt(0))) return false;
                    // The tree's branch holds the rows for the roots and nothing more; the domain's a cell per root.
                    if (B.treeB.getBranch('rows').branchCount !== types.length || B.cellsB.branchCount !== types.length) return false;
                    // A stranger is refused by the relation.
                    try { B.relation.cellFor('t:Nope'); return false; } catch (e) { if (!/no such node/.test(String(e))) return false; }
                    return B.relation.describe().indexOf(types.length + ' nodes presented, 0 open') === 0;
                })()"""), "the tree begins with the types, closed, and builds nothing under them");
    }

    @Test
    void anUnfoldAnsweredAtOnceOpensTheSeriesAndThenTheTitles() {
        act("var B = bench(); B.tree.unfold('t:Racing');");
        assertTrue(evalBool("""
                (() => {
                    // The series under Racing, in order of first release; still no title anywhere.
                    var shown = B.shown(), i = shown.indexOf('t:Racing');
                    if (i < 0 || shown[i + 1] !== 's:Racing|Need for Speed' && shown[i + 1].indexOf('s:Racing|') !== 0) return false;
                    if (B.tree.places().foldAt(i) !== 'open' || B.tree.places().depthAt(i + 1) !== 1) return false;
                    if (shown.some(function (k) { return /^g\\d+$/.test(k); })) return false;
                    if (B.mask() !== null || B.tree.isPending()) return false;
                    // Now a series: the titles come, a leaf each, with year and platform in the cell.
                    B.tree.unfold('s:Racing|Need for Speed');
                    return true;
                })()"""), "unfolding a type presents its series at once");
        assertTrue(evalBool("""
                (() => {
                    var p = B.tree.places(), i = p.indexOf('s:Racing|Need for Speed');
                    if (i < 0 || p.foldAt(i) !== 'open' || p.depthAt(i + 1) !== 2 || p.foldAt(i + 1) !== 'leaf') return false;
                    if (!/Need for Speed.* \\u00b7 \\d{4} \\u00b7 /.test(B.textAt(i + 1))) return false;   // The Need for Speed, 1994, comes first
                    var n = p.rows();
                    // Fold the type: the series and the titles go; the cells are kept by the domain.
                    var made = B.relation.cellCount();
                    B.tree.fold('t:Racing');
                    return made > B.relation.types().length + 1 && n > B.relation.types().length + 10;
                })()"""), "unfolding a series presents its titles");
        assertTrue(evalBool("""
                (() => {
                    if (B.shown().length !== B.relation.types().length || B.relation.isOpen('t:Racing')) return false;
                    // The series stays open in the domain's state, so unfolding the type again shows the titles at once.
                    if (!B.relation.isOpen('s:Racing|Need for Speed')) return false;
                    return B.treeB.getBranch('rows').branchCount === B.relation.types().length;
                })()"""), "a fold withdraws the subtree; the rows shrink; the domain keeps what it knows");
    }

    @Test
    void theSlowTypeMasksWithANoteAndTheToldTypeNeverLocks() {
        act("var B = bench(); B.tree.unfold('t:Strategy');");
        assertTrue(evalBool("""
                (() => {
                    // Late: the panel was handed the domain's note at once, so the mask is up already.
                    var m = B.mask();
                    if (!m || !B.tree.isPending() || m.children.length !== 1) return false;
                    if (!/Fetching the Strategy series/.test(m.children[0].children[0].textContent)) return false;
                    if (B.tree.unfold('t:Sports') || B.tree.selectNode('t:Sports')) return false;   // locked
                    runTimers();                                               // the fetch lands
                    return true;
                })()"""), "the slow type locks the tree and puts the domain's note on the panel");
        assertTrue(evalBool("""
                (() => {
                    if (B.tree.isPending() || !B.relation.isOpen('t:Strategy')) return false;
                    if (B.shown().indexOf('s:Strategy|Age of Empires') < 0 && !B.shown().some(function (k) { return k.indexOf('s:Strategy|') === 0; })) return false;
                    runTimers();                                               // the hold ends; the mask comes down
                    if (B.mask() !== null) return false;
                    // The told type: nothing now, the tree free; then the tell.
                    B.tree.unfold('t:Puzzle');
                    return true;
                })()"""), "the answer lands and the mask comes down");
        assertTrue(evalBool("""
                (() => {
                    if (B.tree.isPending() || B.mask() !== null || B.relation.isOpen('t:Puzzle')) return false;
                    if (!B.tree.selectNode('t:Sports')) return false;          // free meanwhile
                    runTimers();                                               // fetched, and told
                    if (B.told() !== 1 || !B.relation.isOpen('t:Puzzle')) return false;
                    return B.shown().some(function (k) { return k.indexOf('s:Puzzle|') === 0; }) && B.tree.cursor() === 't:Sports';
                })()"""), "the told type answers nothing and tells later; the tree never locked");
    }

    @Test
    void theNavigatorsMoveIsTheDomainsToOpenAndTheTreesToReach() {
        assertTrue(evalBool("""
                (() => {
                    var B = bench(), r = B.relation, s = B.store;
                    var pk = s.pks().filter(function (k) { return s.get(k, 'title') === 'Need for Speed: Most Wanted' && s.get(k, 'year') === 2005; })[0];
                    if (!pk) return false;
                    // Not presented: the tree reaches nothing and opens nothing.
                    if (B.tree.selectNode(pk) || B.tree.cursor() !== null) return false;
                    // The domain opens the ancestors — its own fold state — tells, and asks for the cursor.
                    r.open(r.typeKey('Racing')); r.open(r.seriesKey('Racing', 'Need for Speed'));
                    B.tree.tell(new RelTreeViewChanged());
                    if (!B.tree.selectNode(pk) || B.tree.cursor() !== pk || B.moves.join() !== pk) return false;
                    var p = B.tree.places(), i = p.indexOf(pk);
                    if (p.depthAt(i) !== 2 || p.keyAt(p.parentOf(i)) !== 's:Racing|Need for Speed') return false;
                    // Close all: the cursor's row went with a re-ask the tree did not ask for — its position stands in.
                    r.closeAll(); B.tree.tell(new RelTreeViewChanged());
                    return B.shown().length === r.types().length && B.tree.cursor() !== null && p.depthAt(p.indexOf(B.tree.cursor())) === 0;
                })()"""), "the domain opens the path and tells; selectNode reaches the node");
    }
}
