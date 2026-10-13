package hue.captains.singapura.js.homing.relgrid.prose;

import hue.captains.singapura.js.homing.conformance.rules.CrateDependencyRule;
import hue.captains.singapura.js.homing.conformance.rules.OrphanCheck;
import hue.captains.singapura.js.homing.relgrid.RelGridTestDom;
import hue.captains.singapura.js.homing.ssjs.test.JsModuleTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A document's table: a {@code DocTableRelation} - what the table says, judged when it is made and frozen - handed to a
 * {@code ProseTable}, which takes nothing else and makes a grid that reads as a document's table: its cells the house's
 * data and header cells, its caption the house's, its rules its design's; read only, its copy what its cells say. The
 * house's look is the test DOM's stand-in, which wears a leaf as {@code look-<leaf>}; the design is the test design,
 * which wears a leaf of the frame as {@code frame-<token>}.
 */
class ProseTableTest extends JsModuleTestBase {

    private static final String DIR = RelGridTestDom.DIR + "prose/";

    @BeforeEach
    void setup() {
        js = buildContext();
        js.eval("js", RelGridTestDom.DOM_STUB);
        js.eval("js", RelGridTestDom.STYLES + RelGridTestDom.handles(ProseStyles.INSTANCE));
        for (String m : RelGridTestDom.PARTY) loadModule(m);
        loadModule(RelGridTestDom.SELECTION);
        loadModule(RelGridTestDom.CHANNEL);
        loadModule(RelGridTestDom.PROTOCOL);
        for (String m : RelGridTestDom.MODULES) loadModule(RelGridTestDom.DIR + m);
        js.eval("js", "var HouseProse = TEST_DESIGN;");          // the house's document table, as the test design stands in for it
        loadModule(DIR + "DocTableRelationModule.js");
        loadModule(DIR + "ProseCellsModule.js");
        loadModule(DIR + "ProseTableModule.js");
        js.eval("js", """
                function has(el, c) { return (el.className || '').split(' ').indexOf(c) >= 0; }
                function find(el, c) {
                    if (has(el, c)) return el;
                    for (var k = 0; k < el.children.length; k++) { var f = find(el.children[k], c); if (f) return f; }
                    return null;
                }
                function all(el, c, out) {
                    out = out || [];
                    if (has(el, c)) out.push(el);
                    for (var k = 0; k < el.children.length; k++) all(el.children[k], c, out);
                    return out;
                }
                var RELEASES = { caption: 'Releases', columns: ['Version', { title: 'Downloads', align: 'right' }], rows: [['1.0', '1,204'], ['1.1', 980]] };
                function releases(opts) { return new ProseTable(testBranch(), new DocTableRelation(RELEASES), opts); }
                function refusal(spec) { try { new DocTableRelation(spec); return null; } catch (e) { return String(e.message); } }
                """);
    }

    private boolean evalBool(String expr) { return js.eval("js", expr).asBoolean(); }

    // ── the relation: what a doc's table says, judged when made ─────────────

    @Test
    void theRelation_saysWhatTheTableSays_byIdentity_andIsFrozen() {
        assertTrue(evalBool("""
                (() => {
                    var r = new DocTableRelation({ caption: 'Matrix', columns: ['Area', { title: 'State', align: 'center' }, 'Note'],
                                                   rows: [[{ text: 'Grid', emphasis: 'strong', colSpan: 2 }, 'x'], ['Tree']] });
                    if (r.columns().join() !== 'c0,c1,c2' || r.view().join() !== 'r0,r1') return false;
                    if (r.column('c1').title !== 'State' || r.column('c1').align !== 'center' || r.column('c9') !== null) return false;
                    var lead = r.cell('r0', 'c0'), over = r.cell('r0', 'c1'), short = r.cell('r1', 'c1');
                    if (lead.text !== 'Grid' || lead.colSpan !== 2 || lead.emphasis !== 'strong') return false;
                    if (over.text !== '' || over.colSpan !== 1 || over.align !== 'center') return false;   // what a span reaches over: empty, its column's alignment
                    if (short.text !== '' || r.cell('r1', 'c2').text !== '') return false;                 // a row that says fewer: the rest empty
                    if (r.cell('r9', 'c0') !== null || r.cell('r0', 'c9') !== null) return false;          // a stranger: nothing
                    if (!r.titled() || !r.spans() || r.caption() !== 'Matrix') return false;
                    if (!Object.isFrozen(r) || !Object.isFrozen(lead) || !Object.isFrozen(r.column('c0'))) return false;
                    var none = new DocTableRelation({ columns: ['', ''], rows: [[1, 2]] });
                    return !none.titled() && !none.spans() && none.caption() === null && none.cell('r0', 'c0').text === '1';
                })()"""), "columns and rows by identity, a value for every cell, frozen");
    }

    @Test
    void whatADocsTableCannotSay_isRefusedByTheRelation_byName() {
        assertTrue(evalBool("""
                (() => {
                    var rowSpan = refusal({ columns: ['a', 'b'], rows: [[{ text: 'x', rowSpan: 2 }, 'y']] });
                    var wide = refusal({ columns: ['a'], rows: [['x', 'y']] });
                    var badge = refusal({ columns: ['a'], rows: [[{ text: 'x', badge: 'info' }]] });
                    var align = refusal({ columns: [{ title: 'a', align: 'justify' }], rows: [] });
                    var none = refusal({ columns: [], rows: [] });
                    return /\\[DocTableRelation\\] cell 0,0 spans 2 rows: the grid spans columns only/.test(rowSpan)
                        && /more cells than the 1 columns/.test(wide)
                        && /badge is one of success, warning, danger, not "info"/.test(badge) && /align is one of left, center, right/.test(align)
                        && /at least one column/.test(none);
                })()"""), "row spans, rows wider than the table, words it has not: refused when the relation is made");
    }

    // ── the table: it takes a relation, and nothing else ───────────────────

    @Test
    void theTable_takesADocTableRelation_andNothingElse() {
        assertTrue(evalBool("""
                (() => {
                    function refused(relation) { try { new ProseTable(testBranch(), relation); return null; } catch (e) { return String(e.message); } }
                    var plain = refused(RELEASES), lookalike = refused({ columns: function () { return ['c0']; }, view: function () { return []; } });
                    return /a DocTableRelation is required/.test(plain) && /not Object/.test(plain)
                        && /a DocTableRelation is required/.test(lookalike) && /a DocTableRelation is required/.test(refused(null))
                        && releases().grid !== null;
                })()"""), "the shape of a relation is not a relation: only a DocTableRelation, judged when made");
    }

    @Test
    void aRelation_readsAsADocumentsTable() {
        assertTrue(evalBool("""
                (() => {
                    var t = releases();
                    var root = t.root, caption = root.children[0], box = root.children[1];
                    if (!has(root, 'prose-table') || !has(root, 'look-table')) return false;
                    if (!has(caption, 'prose-caption') || !has(caption, 'look-caption') || caption.textContent !== 'Releases') return false;
                    if (!has(box, 'prose-box') || !find(box, 'hrg-wrap')) return false;
                    var heads = all(root, 'prose-head'), cells = all(root, 'prose-cell');
                    if (heads.map(function (h) { return h.textContent; }).join() !== 'Version,Downloads') return false;
                    if (!heads.every(function (h) { return has(h, 'look-header-cell'); })) return false;
                    if (cells.map(function (c) { return c.textContent; }).join() !== '1.0,1,204,1.1,980') return false;
                    if (!cells.every(function (c) { return has(c, 'look-data-cell'); })) return false;
                    if (!has(heads[1], 'prose-right') || !has(cells[1], 'prose-right') || has(cells[0], 'prose-right')) return false;
                    // the rules are the design's, on the slots
                    if (!all(root, 'frame-lattice').length || !all(root, 'frame-header-band').length) return false;
                    return t.grid.viewMaps().rows() === 2 && t.grid.viewMaps().cols() === 2;
                })()"""), "a caption, a head, its rows: the house's caption, header and data cells, the design's rules");
    }

    @Test
    void itIsReadOnly_noCellIsEverOfferedControl() {
        assertTrue(evalBool("""
                (() => {
                    var t = releases();
                    find(t.root, 'hrg-table').dispatch('keydown', { key: 'Enter', preventDefault: function () {} });
                    return !t.grid.isDeep() && t.grid.mayTakeControlAtCursor() === false;
                })()"""), "a document's table: Enter opens nothing");
    }

    @Test
    void aCellsEmphasisAndBadge_areTheHousesWords() {
        assertTrue(evalBool("""
                (() => {
                    var t = new ProseTable(testBranch(), new DocTableRelation({ columns: ['Area', 'State', 'Note'], rows: [[
                        { text: 'Grid', emphasis: 'strong' }, { text: 'done', badge: 'success' }, { text: 'later', emphasis: 'muted', align: 'center' }]] }));
                    var cells = all(t.root, 'prose-cell');
                    if (!has(cells[0], 'look-data-cell') || !has(cells[0], 'look-header-cell')) return false;   // strong: a header cell's weight
                    var badge = find(cells[1], 'prose-badge');
                    if (!badge || !has(badge, 'look-success-badge') || badge.textContent !== 'done') return false;
                    return has(cells[2], 'look-caption') && has(cells[2], 'prose-center');                     // muted: a caption's ink
                })()"""), "strong is a header cell's weight, muted a caption's ink, a badge the house's of its tone");
    }

    @Test
    void aCellSpansColumns_aShortRowStandsEmpty_andAHeadlessTableHasNoHead() {
        assertTrue(evalBool("""
                (() => {
                    var t = new ProseTable(testBranch(), new DocTableRelation({ columns: ['', '', ''], rows: [[{ text: 'all three', colSpan: 3 }], ['a', 'b']] }));
                    var table = find(t.root, 'hrg-table');
                    for (var k = 0; k < table.children.length; k++) if (table.children[k].tagName === 'thead') return false;
                    if (t.grid.viewMaps().cols() !== 3) return false;
                    var hosts = all(t.root, 'hrg-merge');
                    if (hosts.length !== 1 || hosts[0].children[0].textContent !== 'all three') return false;
                    var cells = all(t.root, 'prose-cell').map(function (c) { return c.textContent; });
                    return cells.indexOf('a') >= 0 && cells.indexOf('b') >= 0 && cells.filter(function (s) { return s === ''; }).length === 3;
                })()"""), "a span is a merged cell; a short row's rest is empty; no titles, no head");
    }

    @Test
    void itsTextIsDrawnAsTheDocumentSays() {
        assertTrue(evalBool("""
                (() => {
                    var drawn = [];
                    var t = releases({ inline: function (text, el) { drawn.push(text); el.textContent = '[' + text + ']'; } });
                    return t.root.children[0].textContent === '[Releases]' && all(t.root, 'prose-head')[0].textContent === '[Version]'
                        && all(t.root, 'prose-cell')[0].textContent === '[1.0]' && drawn.length === 7;
                })()"""), "the caption, every title and every cell drawn by the inline drawing handed in");
    }

    @Test
    void aCopy_carriesTheHeadOverTheRows_asTextAndAsATable() {
        assertTrue(evalBool("""
                (() => {
                    var t = releases();
                    var c = t._answer(new RelGridCopyRequested([new RelGridBlock(['r0', 'r1'], ['c0', 'c1'])]));
                    return c instanceof RelGridClipboardContent && c.text === 'Version\\tDownloads\\n1.0\\t1,204\\n1.1\\t980'
                        && c.html === '<table><thead><tr><th>Version</th><th>Downloads</th></tr></thead>'
                                    + '<tbody><tr><td>1.0</td><td>1,204</td></tr><tr><td>1.1</td><td>980</td></tr></tbody></table>'
                        && t._answer({}) === undefined;
                })()"""), "two cells or more: the selected columns' titles over the rows, tab-separated and as a table with its head");
    }

    @Test
    void aCopyOfOneCell_isItsValueAlone() {
        assertTrue(evalBool("""
                (() => {
                    var c = releases()._answer(new RelGridCopyRequested([new RelGridBlock(['r1'], ['c1'])]));
                    return c.text === '980' && c.html === undefined;
                })()"""), "a single cell: its value, no head, no table");
    }

    @Test
    void aCopy_takesEachRangeWithItsHead_aSpanAColspan_andNoHeadWhereThereIsNone() {
        assertTrue(evalBool("""
                (() => {
                    var two = releases()._answer(new RelGridCopyRequested([new RelGridBlock(['r0'], ['c0']), new RelGridBlock(['r1'], ['c1'])]));
                    if (two.text !== 'Version\\n1.0\\n\\nDownloads\\n980') return false;
                    var spans = new ProseTable(testBranch(), new DocTableRelation({ columns: ['a', 'b', 'c'],
                                                                                  rows: [[{ text: 'all', colSpan: 3 }], ['x', 'y', 'z']] }));
                    var s = spans._answer(new RelGridCopyRequested([new RelGridBlock(['r0', 'r1'], ['c0', 'c1', 'c2'])]));
                    if (s.text !== 'a\\tb\\tc\\nall\\t\\t\\nx\\ty\\tz') return false;
                    if (s.html.indexOf('<tr><td colspan="3">all</td></tr>') < 0) return false;
                    var bare = new ProseTable(testBranch(), new DocTableRelation({ columns: ['', ''], rows: [['k', 'v'], ['k2', 'v2']] }));
                    var b = bare._answer(new RelGridCopyRequested([new RelGridBlock(['r0', 'r1'], ['c0', 'c1'])]));
                    return b.text === 'k\\tv\\nk2\\tv2' && b.html.indexOf('<thead>') < 0;
                })()"""), "a block a range, each with its head; a span a colspan, empty under it in the text; no titles, no head");
    }

    @Test
    void disposing_takesDownTheGridAndItsCells() {
        assertTrue(evalBool("""
                (() => {
                    var t = releases();
                    t.dispose();
                    return t.grid === null;
                })()"""), "the table's own, dissolved");
    }

    @Test
    void theCrateHoldsEveryServedModule_andImportsOnlyWhatItRequires() {
        assertEquals(List.of(), OrphanCheck.check(RelGridProseCrate.INSTANCE));
        assertEquals(List.of(), CrateDependencyRule.check(RelGridProseCrate.INSTANCE));
    }
}
