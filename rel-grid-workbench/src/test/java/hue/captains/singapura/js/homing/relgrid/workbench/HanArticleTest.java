package hue.captains.singapura.js.homing.relgrid.workbench;

import hue.captains.singapura.js.homing.ssjs.test.JsModuleTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The Han Article bench's DOMAIN, tested without a grid: the layout engine on
 * the poem and on its edge rules, the store's splice, the relation's slots and
 * cells, and an edit through a cell re-flowing the article. No grid is
 * constructed anywhere here.
 */
class HanArticleTest extends JsModuleTestBase {

    private static final String BENCH_DIR = "/homing/js/hue/captains/singapura/js/homing/relgrid/workbench/";

    private static final String HELPERS = """
            var POEM = '月落乌啼霜满天，\\n江枫渔火对愁眠。\\n姑苏城外寒山寺，\\n夜半钟声到客船。';
            // A row as a string: glyphs, and '·' for an empty slot.
            function rowStr(row) { return row.map(function (s) { return s.glyph === null ? '·' : s.glyph; }).join(''); }
            function rowsStr(lay) { return lay.rows.map(rowStr).join('|'); }
            // A row with its slots bracketed, so a pair of marks reads as one slot: [月][落][」，]
            function slotsStr(lay) {
                return lay.rows.map(function (row) {
                    return row.map(function (s) { return s.glyph === null ? '·' : '[' + s.glyph + ']'; }).join('');
                }).join('|');
            }
            function mount(rel, pk, col) { var c = rel.cellFor(pk, col); if (!c._el) c.render(makeEl('div')); return c; }
            // What a square shows: its text, or its half-boxes' texts joined.
            function inkOf(c) {
                var ink = c._ink;
                return ink.children.length ? ink.children.map(function (h) { return h.textContent; }).join('') : ink.textContent;
            }
            function shown(rel, pk, col) { return inkOf(mount(rel, pk, col)); }
            // Edit a slot the way a person does: Enter opens the square, typing fills it, Enter commits.
            function edit(rel, pk, col, text) {
                var c = mount(rel, pk, col);
                if (c.mayTakeControl() !== true) return false;
                var host = makeEl('div');
                c.takeControl(host);
                var input = host.children[0].children[0];
                input.value = text;
                input.dispatch('keydown', { key: 'Enter' });
                return true;
            }
            """;

    @BeforeEach
    void setup() {
        js = buildContext();
        js.eval("js", DishPolicyTest.DOM_STUB);
        loadModule(BENCH_DIR + "HanLayout.js");
        loadModule(BENCH_DIR + "HanStore.js");
        loadModule(BENCH_DIR + "HanCellModule.js");
        loadModule(BENCH_DIR + "HanRelation.js");
        js.eval("js", HELPERS);
    }

    private boolean evalBool(String expr) { return js.eval("js", expr).asBoolean(); }

    @Test
    void thePoemLaysOutFourRowsOfNineWithOneEmptySquareEach() {
        assertTrue(evalBool("""
                (() => {
                    var lay = hanLayout(POEM, 9);
                    if (lay.rows.length !== 4 || lay.cols !== 9 || lay.glyphs !== 32) return false;
                    if (rowsStr(lay) !== '月落乌啼霜满天，·|江枫渔火对愁眠。·|姑苏城外寒山寺，·|夜半钟声到客船。·') return false;
                    // Every row is exactly nine slots, whatever the line held.
                    for (var r = 0; r < 4; r++) if (lay.rows[r].length !== 9) return false;
                    // A glyph slot commits by REPLACING itself; the poem's chars are at 0..7, 9..16, ...
                    var s = lay.rows[1][2];
                    if (s.glyph !== '渔' || s.at !== 11 || s.len !== 1) return false;
                    // The blank tail of a broken line INSERTS before the break; the last line's, at the end.
                    var tail1 = lay.rows[0][8], tail4 = lay.rows[3][8];
                    return tail1.glyph === null && tail1.at === 8 && tail1.len === 0
                        && tail4.at === lay.length && tail4.len === 0;
                })()"""), "the poem: four rows, one glyph a square, a blank square closing each line");
    }

    @Test
    void wrappingAndNewlinesFollowTheStatedRules() {
        assertTrue(evalBool("""
                (() => {
                    // Prose wraps at nine.
                    if (rowsStr(hanLayout('一二三四五六七八九十', 9)) !== '一二三四五六七八九|十········') return false;
                    // A newline right after a wrap IS the wrap: no blank row.
                    if (rowsStr(hanLayout('一二三四五六七八九\\n十', 9)) !== '一二三四五六七八九|十········') return false;
                    // But a second newline is a blank line.
                    if (rowsStr(hanLayout('一二三四五六七八九\\n\\n十', 9)) !== '一二三四五六七八九|·········|十········') return false;
                    // A full last row gets an empty row after it: always somewhere to type.
                    var full = hanLayout('一二三四五六七八九', 9);
                    if (rowsStr(full) !== '一二三四五六七八九|·········' || full.rows[1][0].at !== 9) return false;
                    // Empty text is one empty row, and CRLF is a newline.
                    if (rowsStr(hanLayout('', 9)) !== '·········') return false;
                    if (rowsStr(hanLayout('甲\\r\\n乙', 9)) !== '甲········|乙········') return false;
                    // Code points, not UTF-16 units: a supplementary-plane glyph is ONE square.
                    var sup = hanLayout('𠀋乙', 9);
                    return sup.glyphs === 2 && sup.rows[0][0].glyph === '𠀋' && sup.rows[0][1].at === 1;
                })()"""), "wrap at nine, newline ends a row, a wrap's newline adds nothing, always a slot to type in");
    }

    @Test
    void twoPunctuationMarksShareASquare() {
        assertTrue(evalBool("""
                (() => {
                    // What a mark is: CJK marks, fullwidth ASCII marks, dashes and the ellipsis,
                    // the middle dot — and not a character, a digit, or the ideographic space.
                    var marks = ['，', '。', '、', '；', '：', '？', '！', '「', '」', '『', '』', '（', '）', '《', '》', '…', '—', '·', '．'];
                    for (var i = 0; i < marks.length; i++) if (!hanIsPunct(marks[i])) return false;
                    var not = ['月', 'a', '1', '\\u3000', ' ', '\\n', ''];
                    for (var j = 0; j < not.length; j++) if (hanIsPunct(not[j])) return false;

                    // A mark that follows a lone mark joins it; a third starts a new slot.
                    var lay = hanLayout('「月落」，。乙', 9);
                    if (slotsStr(lay) !== '[「][月][落][」，][。][乙]···') return false;
                    var pair = lay.rows[0][3];
                    if (pair.kind !== 'punct' || pair.len !== 2 || pair.at !== 3 || pair.glyph !== '」，') return false;
                    if (lay.rows[0][0].kind !== 'punct' || lay.rows[0][0].len !== 1) return false;
                    if (lay.rows[0][1].kind !== 'han' || lay.rows[0][8].kind !== 'empty') return false;
                    if (lay.glyphs !== 7) return false;                          // marks count as glyphs

                    // A pair does not widen a row: eight characters and a pair of marks is
                    // nine slots, and a mark that joins a slot costs no slot.
                    var nine = hanLayout('一二三四五六七八，。', 9);
                    if (nine.rows[0].length !== 9 || nine.rows[0][8].glyph !== '，。' || nine.rows.length !== 2) return false;

                    // Only within a row: a mark that lands at the start of a row starts a slot
                    // there, like anything else — the end-of-line rule is the NEXT iteration.
                    var split = hanLayout('一二三四五六七八九，', 9);
                    if (slotsStr(split) !== '[一][二][三][四][五][六][七][八][九]|[，]········') return false;

                    // The poem is unchanged: every line's mark is alone, so it keeps its square.
                    var poem = hanLayout(POEM, 9);
                    return poem.rows.length === 4 && poem.rows[0][7].glyph === '，' && poem.rows[0][7].len === 1
                        && poem.rows[0][7].kind === 'punct';
                })()"""), "a mark joins a lone mark in the same row; a pair costs one slot; nothing else changes");
    }

    @Test
    void aSquareDrawsAPairAsTwoHalvesAndACharacterAsItself() {
        assertTrue(evalBool("""
                (() => {
                    var store = createHanStore('甲，。乙。');
                    var rel = createHanRelation(store, { cols: 9, editable: true });
                    // A character: the square's own text, no halves.
                    var han = mount(rel, 'r0', 'c0');
                    if (han._ink.textContent !== '甲' || han._ink.children.length !== 0) return false;
                    if (/han-punct/.test(han._ink.className)) return false;
                    // A pair: two half-boxes, one mark each, and the square marked as punctuation.
                    var pair = mount(rel, 'r0', 'c1');
                    if (!/han-punct/.test(pair._ink.className) || pair._ink.children.length !== 2) return false;
                    if (pair._ink.children[0].textContent !== '，' || pair._ink.children[1].textContent !== '。') return false;
                    if (!/han-half/.test(pair._ink.children[0].className)) return false;
                    // A lone mark: one half-box, the other half left empty.
                    var lone = mount(rel, 'r0', 'c3');
                    if (!/han-punct/.test(lone._ink.className) || lone._ink.children.length !== 1) return false;
                    // Editing the pair replaces BOTH marks (len 2): the square held two, and
                    // what it held is replaced by what was typed.
                    if (!edit(rel, 'r0', 'c1', '！')) return false;
                    if (store.text() !== '甲！乙。') return false;
                    if (inkOf(pair) !== '！' || pair._ink.children.length !== 1) return false;
                    // And a square changes KIND as the article moves under it: replace the
                    // character with a mark, and its ink is rebuilt as halves.
                    if (!edit(rel, 'r0', 'c0', '、')) return false;
                    if (store.text() !== '、！乙。') return false;
                    // 、 and ！ are now adjacent marks, so they PAIR: one square, two halves.
                    if (inkOf(han) !== '、！' || han._ink.children.length !== 2) return false;
                    return shown(rel, 'r0', 'c1') === '乙';
                })()"""), "the square draws a pair as halves, a character as text, and rebuilds when its kind changes");
    }

    @Test
    void theStoreSplicesInCodePointsAndTellsItsSubscribers() {
        assertTrue(evalBool("""
                (() => {
                    var store = createHanStore('甲乙丙');
                    var told = [];
                    store.subscribe(function (t) { told.push(t); });
                    store.splice(1, 1, '丁');                       // replace
                    if (store.text() !== '甲丁丙') return false;
                    store.splice(1, 0, '戊己');                     // insert
                    if (store.text() !== '甲戊己丁丙') return false;
                    store.splice(0, 1, '');                         // delete
                    if (store.text() !== '戊己丁丙') return false;
                    // Out of range is clamped, not thrown; a no-op change tells nobody.
                    store.splice(99, 5, '庚');
                    if (store.text() !== '戊己丁丙庚') return false;
                    var before = told.length;
                    store.splice(0, 0, '');
                    if (told.length !== before) return false;
                    store.reset();
                    return store.text() === '甲乙丙' && store.revision() === 5 && told.length === 5;
                })()"""), "splice replaces, inserts and deletes by code point; subscribers hear each real change");
    }

    @Test
    void theRelationOwnsOneSquareCellPerSlotAndShowsTheGlyph() {
        assertTrue(evalBool("""
                (() => {
                    var store = createHanStore(POEM);
                    var rel = createHanRelation(store, { cols: 9, editable: false, capacity: 12 });
                    // Identities are the CAPACITY; what is presented is the prefix in use.
                    if (rel.pks().length !== 12 || rel.pks()[11] !== 'r11') return false;
                    if (rel.presented().join(',') !== 'r0,r1,r2,r3') return false;
                    if (rel.columns().join(',') !== 'c0,c1,c2,c3,c4,c5,c6,c7,c8') return false;
                    if (shown(rel, 'r0', 'c0') !== '月' || shown(rel, 'r3', 'c7') !== '。') return false;
                    if (shown(rel, 'r0', 'c8') !== '') return false;                 // the blank square
                    // The same cell every time, and a square by class.
                    var c = rel.cellFor('r1', 'c3');
                    if (c !== rel.cellFor('r1', 'c3')) return false;
                    if (!/han-glyph/.test(mount(rel, 'r1', 'c3')._el.className)) return false;
                    // Read-only: the cell has nowhere to commit and says so.
                    return c.mayTakeControl() === false && rel.cellCount() === 4;
                })()"""), "positional identity: a square per slot, the glyph as what it shows, read-only when told so");
    }

    @Test
    void anEditThroughACellReplacesInsertsOrDeletesAndTheArticleReflows() {
        assertTrue(evalBool("""
                (() => {
                    var store = createHanStore(POEM);
                    var ed = createHanRelation(store, { cols: 9, editable: true });
                    var show = createHanRelation(store, { cols: 9, editable: false });
                    // Mount a few cells on both, as two grids would have.
                    ['r0','r1','r2','r3'].forEach(function (r) { for (var k = 0; k < 9; k++) { mount(ed, r, 'c' + k); mount(show, r, 'c' + k); } });

                    // REPLACE one glyph: 乌 → 鸟. Both relations' cells follow, through the store.
                    if (!edit(ed, 'r0', 'c2', '鸟')) return false;
                    if (store.text().indexOf('月落鸟啼') !== 0) return false;
                    if (shown(ed, 'r0', 'c2') !== '鸟' || shown(show, 'r0', 'c2') !== '鸟') return false;

                    // INSERT two glyphs at the blank tail of line 1: the line grows to ten
                    // and WRAPS — the tenth glyph lands on a new row, and every later row
                    // moves down one. Same cells, new ink.
                    if (!edit(ed, 'r0', 'c8', '寒山')) return false;
                    if (store.text().indexOf('月落鸟啼霜满天，寒山\\n') !== 0) return false;
                    if (ed.rows() !== 5 || ed.presented().join(',') !== 'r0,r1,r2,r3,r4') return false;
                    if (shown(ed, 'r0', 'c8') !== '寒' || shown(ed, 'r1', 'c0') !== '山') return false;
                    if (shown(show, 'r2', 'c0') !== '江') return false;             // line 2 is row 2 now
                    if (mount(show, 'r4', 'c0')._ink.textContent !== '夜') return false;

                    // DELETE: commit nothing on 山 — the article shrinks back to four rows,
                    // and the cell the fifth row had is left alive with nothing in it.
                    if (!edit(ed, 'r1', 'c0', '')) return false;
                    if (ed.rows() !== 4 || ed.presented().length !== 4 || shown(show, 'r1', 'c0') !== '江') return false;
                    if (shown(show, 'r4', 'c0') !== '') return false;
                    return shown(ed, 'r0', 'c8') === '寒';
                })()"""), "replace, insert and delete through a square; both relations re-flow through the store");
    }

    @Test
    void aSlotCommitsWhereItMeansNowNotWhereItWasBuilt() {
        assertTrue(evalBool("""
                (() => {
                    // The cell for r0/c1 was built when c1 held 乙 at index 1. After an insert
                    // ahead of it, that square holds 甲's neighbour 丁 at index 1 — and a
                    // commit must replace THAT, not the glyph that has moved on.
                    var store = createHanStore('甲乙丙');
                    var ed = createHanRelation(store, { cols: 9, editable: true });
                    mount(ed, 'r0', 'c1');
                    store.splice(1, 0, '丁');                                       // 甲丁乙丙
                    if (shown(ed, 'r0', 'c1') !== '丁') return false;
                    if (!edit(ed, 'r0', 'c1', '戊')) return false;
                    return store.text() === '甲戊乙丙';
                })()"""), "a commit goes where the slot is now: the slot is looked up at commit time");
    }

    @Test
    void theSquareEditorLeavesEnterToAnOpenComposition() {
        assertTrue(evalBool("""
                (() => {
                    var store = createHanStore('甲');
                    var ed = createHanRelation(store, { cols: 9, editable: true });
                    var c = mount(ed, 'r0', 'c0');
                    var host = makeEl('div');
                    var settled = false;
                    c.takeControl(host).then(function () { settled = true; });
                    var input = host.children[0].children[0];
                    if (input.tagName !== 'input' || input.value !== '甲') return false;
                    // An IME is composing: its Enter takes the candidate and is NOT a commit.
                    input.dispatch('compositionstart', {});
                    input.value = 'yue';
                    input.dispatch('keydown', { key: 'Enter', isComposing: true });
                    if (store.text() !== '甲' || c.mayTakeControl() !== false) return false;   // still open
                    input.dispatch('compositionend', {});
                    input.value = '月';
                    input.dispatch('keydown', { key: 'Enter' });
                    return store.text() === '月' && host.children.length === 0 && c.mayTakeControl() === true;
                })()"""), "Enter during a composition is the input method's; Enter after it commits");
    }
}
