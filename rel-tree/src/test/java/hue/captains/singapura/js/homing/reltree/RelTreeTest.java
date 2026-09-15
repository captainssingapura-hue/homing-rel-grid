package hue.captains.singapura.js.homing.reltree;

import hue.captains.singapura.js.homing.ssjs.test.JsModuleTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * RFC 0050 · Episode 3-ext1, step 1 — the structure as places, rendered: a
 * lazy tree of closed roots, a bare key a leaf, a malformed answer or a
 * stranger refused whole, the rows positions and the cells the domain's on
 * the domain's branch — read off the party, never the DOM.
 *
 * <p>The same two harness facts as the grid's tests: GraalVM drains microtasks
 * between evals, not within one; timers are fake and stepped.</p>
 */
class RelTreeTest extends JsModuleTestBase {

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
        js.eval("js", RelTreeTestDom.DOM_PARSER);
        js.eval("js", RelTreeTestDom.STYLES);
        js.eval("js", RelTreeTestDom.SVGS);
        for (String m : RelTreeTestDom.PARTY) loadModule(m);
        loadModule(RelTreeTestDom.CHANNEL);
        loadModule(RelTreeTestDom.PROTOCOL);
        for (String m : RelTreeTestDom.MODULES) loadModule(RelTreeTestDom.DIR + m);
        js.eval("js", RelTreeTestDom.FIXTURE);
        js.eval("js", HELPERS);
    }

    private boolean evalBool(String expr) { return js.eval("js", expr).asBoolean(); }

    @Test
    void theStructureIsAnsweredAsPlacesAndTheRootsRenderClosed() {
        assertTrue(evalBool("""
                (() => {
                    var F = fixture();
                    // Lazy by default: the roots, closed; a leaf among them; nothing under anything asked for.
                    if (F.shown() !== 'a:0:closed b:0:leaf c:0:closed') return false;
                    if (F.drawn() !== '\\u25B8A@0 B@0 \\u25B8C@0') return false;
                    if (F.asked() !== 3 || F.mints() !== 3 || F.tree.cells().size() !== 3) return false;
                    if (F.arranged.join() !== 'base' || F.tree.cursor() !== null) return false;
                    // The parent and the first child are derived from the answer, not asked.
                    var p = F.tree.places();
                    if (p.parentOf(0) !== -1 || p.firstChildOf(0) !== -1 || p.indexOf('c') !== 2 || p.indexOf('zz') !== -1) return false;
                    // A bare key is a place at depth 0, a leaf.
                    var G = fixture({ view: function () { return ['a', 'b']; } });
                    return G.shown() === 'a:0:leaf b:0:leaf' && G.drawn() === 'A@0 B@0';
                })()"""), "the roots render closed from the places answered; a bare key is a leaf");
    }

    @Test
    void theTwoBranchesAreIndependentAndReadOffTheParty() {
        assertTrue(evalBool("""
                (() => {
                    var F = fixture(), b = F.branch;
                    // The tree's branch: wrap and tree on it; 'rows' under it, a branch per row, each a row and a caret.
                    if (b.elementCount !== 2 || b.listBranches().join() !== 'rows') return false;
                    var rows = b.getBranch('rows');
                    if (rows.elementCount !== 0 || rows.branchCount !== 3 || countTree(rows) !== 6) return false;
                    // The domain's: a branch per cell, one element each; the tree minted none of them.
                    if (F.cellsBranch.branchCount !== 3 || countTree(F.cellsBranch) !== 3) return false;
                    // The cell's element sits in the tree's row, after the caret — the one thing that crosses.
                    if (F.cellEl(0) !== F.relation.elementOf('a') || F.row(0).children.length !== 2) return false;
                    // The caret is the tree's typed SVG inside the span the row owns: one child, an svg, not the party's.
                    if (F.caret(0).children.length !== 1 || F.caret(0).children[0].tagName !== 'svg' || !/<path /.test(F.caret(0).children[0]._markup)) return false;
                    return F.treeEl().children.length === 3 && countTree(b) === 8;
                })()"""), "two branches, one crossing: the counts are the party's, not the DOM's");
    }

    @Test
    void aMalformedAnswerAndAStrangerAreRefusedWholeAndTheRowsStay() {
        assertTrue(evalBool("""
                (() => {
                    var next = null;
                    var F = fixture({ view: function () { return next || ['a', 'b']; } });
                    var before = F.drawn(), row0 = F.row(0);
                    // A level skipped, and a child under a leaf: the check refuses before anything moved.
                    var bad = [[{ key: 'a', depth: 1 }], [{ key: 'a', depth: 0, fold: 'leaf' }, { key: 'a1', depth: 1 }],
                               [{ key: 'a', depth: 0, fold: 'open' }, { key: 'a1', depth: 2 }], [{ key: 'a' }, { key: 'a' }],
                               [{ key: 'a', depth: 0, fold: 'maybe' }]];
                    for (var k = 0; k < bad.length; k++) {
                        next = bad[k];
                        var threw = false;
                        try { F.tree.tell(new RelTreeViewChanged()); } catch (e) { threw = /\\[RelTree\\]/.test(String(e)); }
                        if (!threw || F.drawn() !== before || F.row(0) !== row0) return false;
                    }
                    // A stranger: the relation refuses it from cellFor, and the places go back.
                    next = ['a', 'zz', 'b'];
                    var refused = false;
                    try { F.tree.tell(new RelTreeViewChanged()); } catch (e) { refused = /no such node: zz/.test(String(e)); }
                    if (!refused || F.shown() !== 'a:0:leaf b:0:leaf' || F.drawn() !== before) return false;
                    // Not a list at all.
                    next = 'a,b';
                    try { F.tree.tell(new RelTreeViewChanged()); return false; } catch (e) { if (!/list of places/.test(String(e))) return false; }
                    // A stranger message is refused, not thrown.
                    return F.tree.tell({ kind: 'x' }) === false && F.shown() === 'a:0:leaf b:0:leaf';
                })()"""), "half a View is not a View: a malformed outline or a stranger leaves the rows as they were");
    }

    @Test
    void toldUnaskedTheTreeAsksAgainAndRowsArePositionsGrownAtTheTail() {
        assertTrue(evalBool("""
                (() => {
                    var F = fixture(), row0 = F.row(0), row2 = F.row(2), cellA = F.cellEl(0);
                    // The domain opens a node itself and tells: the tree asks view() again.
                    F.relation.open('a');
                    if (!F.tree.tell(new RelTreeViewChanged())) return false;
                    if (F.shown() !== 'a:0:open a1:1:leaf a2:1:closed b:0:leaf c:0:closed') return false;
                    if (F.drawn() !== '\\u25BEA@0 A1@1 \\u25B8A2@1 B@0 \\u25B8C@0') return false;
                    // The rows that were are the rows that are: grown at the tail, nothing re-minted.
                    if (F.row(0) !== row0 || F.row(2) !== row2 || F.treeEl().children.length !== 5) return false;
                    if (F.branch.getBranch('rows').branchCount !== 5 || countTree(F.branch.getBranch('rows')) !== 10) return false;
                    // A's cell is the same element; B's cell moved into row 3 with its key; the new cells were asked once.
                    if (F.cellEl(0) !== cellA || F.cellEl(3) !== F.relation.elementOf('b')) return false;
                    if (F.asked() !== 5 || F.mints() !== 5 || F.tree.cells().size() !== 5) return false;
                    // Closed again: shrunk at the tail; the cells that left are out of the tree, alive, and forgotten.
                    F.relation.close('a');
                    F.tree.tell(new RelTreeViewChanged());
                    if (F.drawn() !== '\\u25B8A@0 B@0 \\u25B8C@0' || F.row(0) !== row0 || F.row(2) !== row2) return false;
                    if (F.branch.getBranch('rows').branchCount !== 3 || countTree(F.branch) !== 8) return false;
                    var a1 = F.relation.cell('a1');
                    if (!a1 || F.relation.elementOf('a1').parentNode !== null || F.tree.cells().size() !== 3 || F.tree.cells().get('a1') !== null) return false;
                    // Opened once more: a1 is asked for again — once per PRESENTATION — and the relation
                    // answers the same cell, whose element is the one it always had.
                    F.relation.open('a');
                    F.tree.tell(new RelTreeViewChanged());
                    if (F.tree.cells().get('a1').cell !== a1 || F.asked() !== 7 || F.mints() !== 7) return false;
                    if (F.cellEl(1) !== F.relation.elementOf('a1')) return false;
                    return F.arranged.join() === 'base,told,told,told';
                })()"""), "told unasked, the tree asks view() again; the rows are positions kept while the count holds");
    }

    @Test
    void theFolderGlyphIsAnOptionPaintedFromThePlace() {
        assertTrue(evalBool("""
                (() => {
                    // Off by default: the row is the caret and the cell.
                    var F = fixture({ open: ['a'] });
                    if (F.row(0).children.length !== 2 || F.folder(0) !== null) return false;
                    // On: a folder after the caret — the tree's two SVGs in the span, one shown by the place:
                    // the open one for an open node, the closed one for a closed node, neither for a leaf.
                    var G = fixture({ folder: true, open: ['a'] });                 // a(open) a1 a2(closed) b c(closed)
                    if (G.row(0).children.length !== 3 || G.cellEl(0).textContent !== 'A') return false;
                    function shown(i) {
                        var f = G.folder(i), out = [];
                        for (var k = 0; k < f.children.length; k++) if (!/hrt-folder-off/.test(f.children[k].className)) out.push(/folderOpen|M3 17.5v-10/.test(f.children[k]._markup) ? 'open' : 'closed');
                        return out.join('+') || '-';
                    }
                    if (G.folder(0).children.length !== 2 || G.folder(0).children[0].tagName !== 'svg') return false;
                    var states = []; for (var i = 0; i < 5; i++) states.push(shown(i));
                    if (states.join('|') !== 'open|-|closed|-|closed') return false;
                    if (!/hrt-folder-open/.test(G.folder(0).className) || /hrt-folder-open/.test(G.folder(2).className)) return false;
                    if (!/hrt-folder-leaf/.test(G.folder(1).className) || /hrt-folder-leaf/.test(G.folder(0).className)) return false;
                    // Three OWNED elements a row on the tree's branch — the SVGs are content, not the party's; the domain's cells as before.
                    if (countTree(G.branch.getBranch('rows')) !== 15 || G.cellsBranch.branchCount !== 5) return false;
                    // Folded: the same span and the same SVGs, repainted — nothing parsed again.
                    var f0 = G.folder(0), svgs = [f0.children[0], f0.children[1]];
                    G.relation.close('a'); G.tree.tell(new RelTreeViewChanged());
                    if (G.folder(0) !== f0 || f0.children[0] !== svgs[0] || f0.children[1] !== svgs[1] || shown(0) !== 'closed' || G.treeEl().children.length !== 3) return false;
                    // The host's own text glyphs instead — any of the three, a missing one blank.
                    var H = fixture({ folder: { closed: '+', open: '-', leaf: '\u00b7' }, open: ['a'] });
                    var own = []; for (var k = 0; k < 5; k++) own.push(H.folder(k).textContent);
                    if (own.join('') !== '-\u00b7+\u00b7+' || H.folder(0).children.length !== 0) return false;
                    var J = fixture({ folder: { open: 'v' } });
                    return J.folder(0).textContent === '' && J.folder(1).textContent === '';
                })()"""), "the folder glyph is off by default, on by option, painted from the place, and the host's to choose");
    }

    @Test
    void destroyDetachesEveryCellAndDisposesNothing() {
        assertTrue(evalBool("""
                (() => {
                    var F = fixture({ open: ['a'] });
                    var a1 = F.relation.cell('a1');
                    F.tree.destroy();
                    if (F.container.children.length !== 0 || F.tree.cells().size() !== 0) return false;
                    // Alive: the domain's cell still has its element and its branch.
                    if (a1.cellElement().textContent !== 'A1' || F.cellsBranch.branchCount !== 5) return false;
                    // The tree's rows are released; the host dissolves the branch itself.
                    if (F.branch.getBranch('rows') !== null) return false;
                    return F.tree.tell(new RelTreeViewChanged()) === false;
                })()"""), "destroy detaches and forgets; the domain's cells are untouched");
    }
}
