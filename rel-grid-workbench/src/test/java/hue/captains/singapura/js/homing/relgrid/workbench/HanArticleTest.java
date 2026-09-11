package hue.captains.singapura.js.homing.relgrid.workbench;

import hue.captains.singapura.js.homing.ssjs.test.JsModuleTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The Han Article bench's DOMAIN, tested without a grid: the layout engine on
 * the poem and on its edge rules, the store, the relation's slots and cells,
 * and an edit to the text re-flowing every relation over it. No grid is
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
                    var s = lay.rows[1][2];
                    if (s.glyph !== '渔' || s.kind !== 'han') return false;
                    // Each line's mark is alone, so it keeps its square — a lone mark.
                    return lay.rows[0][7].glyph === '，' && lay.rows[0][7].kind === 'punct'
                        && lay.rows[0][8].glyph === null && lay.rows[0][8].kind === 'empty';
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
                    // A full last row is just that: the article is edited as text, so
                    // the layout owes nobody an empty row to type in.
                    if (rowsStr(hanLayout('一二三四五六七八九', 9)) !== '一二三四五六七八九') return false;
                    // Empty text is one empty row, and CRLF is a newline.
                    if (rowsStr(hanLayout('', 9)) !== '·········') return false;
                    if (rowsStr(hanLayout('甲\\r\\n乙', 9)) !== '甲········|乙········') return false;
                    // Code points, not UTF-16 units: a supplementary-plane glyph is ONE square.
                    var sup = hanLayout('𠀋乙', 9);
                    return sup.glyphs === 2 && sup.rows[0][0].glyph === '𠀋' && sup.rows[0][1].glyph === '乙';
                })()"""), "wrap at nine, newline ends a row, a wrap's newline adds nothing");
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
                    if (pair.kind !== 'punct' || pair.glyph !== '」，') return false;
                    if (lay.rows[0][0].kind !== 'punct' || lay.rows[0][1].kind !== 'han' || lay.rows[0][8].kind !== 'empty') return false;
                    if (lay.glyphs !== 7) return false;                          // marks count as glyphs

                    // A pair does not widen a row, even across a closed one: eight characters
                    // and a pair of marks is nine slots and one row.
                    var nine = hanLayout('一二三四五六七八，。', 9);
                    if (slotsStr(nine) !== '[一][二][三][四][五][六][七][八][，。]') return false;

                    // Only within a line: nothing joins across a newline.
                    if (slotsStr(hanLayout('甲，\\n。乙', 9)) !== '[甲][，]·······|[。][乙]·······') return false;

                    // And a mark that lands at the start of a row starts a slot there, like
                    // anything else — the end-of-line rule is the NEXT iteration.
                    var split = hanLayout('一二三四五六七八九，', 9);
                    return slotsStr(split) === '[一][二][三][四][五][六][七][八][九]|[，]········';
                })()"""), "a mark joins a lone mark; a pair costs one slot; nothing joins across a line");
    }

    @Test
    void theStoreHoldsTheTextAndTellsItsSubscribers() {
        assertTrue(evalBool("""
                (() => {
                    var store = createHanStore('甲乙丙');
                    var told = [];
                    store.subscribe(function (t) { told.push(t); });
                    if (!store.set('甲丁丙')) return false;
                    if (store.text() !== '甲丁丙' || told.join('|') !== '甲丁丙') return false;
                    // Setting what is already there tells nobody.
                    if (store.set('甲丁丙')) return false;
                    if (told.length !== 1) return false;
                    store.reset();
                    return store.text() === '甲乙丙' && store.revision() === 2 && told.length === 2;
                })()"""), "the store takes the whole text, and subscribers hear each real change");
    }

    @Test
    void theRelationOwnsOneSquareCellPerSlotAndShowsTheGlyph() {
        assertTrue(evalBool("""
                (() => {
                    var store = createHanStore(POEM);
                    var rel = createHanRelation(store, { cols: 9, capacity: 12 });
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
                    // A DISPLAY cell: neither half of the handover, so a grid never asks.
                    return typeof c.mayTakeControl === 'undefined' && typeof c.takeControl === 'undefined'
                        && rel.cellCount() === 4;
                })()"""), "positional identity: a square per slot, the glyph as what it shows, nothing to take");
    }

    @Test
    void aSquareDrawsAPairAsTwoHalvesAndACharacterAsItself() {
        assertTrue(evalBool("""
                (() => {
                    var store = createHanStore('甲，。乙。');
                    var rel = createHanRelation(store, { cols: 9 });
                    // A character: the square's own text, no halves.
                    var han = mount(rel, 'r0', 'c0');
                    if (han._ink.textContent !== '甲' || han._ink.children.length !== 0) return false;
                    if (/han-punct/.test(han._ink.className)) return false;
                    // A pair: two half-boxes, one mark each, and the square marked as punctuation.
                    var pair = mount(rel, 'r0', 'c1');
                    if (!/han-punct/.test(pair._ink.className) || pair._ink.children.length !== 2) return false;
                    if (pair._ink.children[0].textContent !== '，' || pair._ink.children[1].textContent !== '。') return false;
                    if (!/han-half/.test(pair._ink.children[0].className)) return false;
                    // A lone mark: one half-box, the other half left empty; an opener aligned right.
                    var lone = mount(rel, 'r0', 'c3');
                    if (!/han-punct/.test(lone._ink.className) || lone._ink.children.length !== 1) return false;
                    store.set('「甲');
                    if (!/han-open/.test(han._ink.children[0].className)) return false;
                    // And a square changes KIND as the article moves under it: the pair's
                    // square now holds a character, and its ink is text again.
                    return pair._ink.textContent === '甲' && pair._ink.children.length === 0
                        && !/han-punct/.test(pair._ink.className);
                })()"""), "the square draws a pair as halves, a character as text, and rebuilds when its kind changes");
    }

    @Test
    void anEditToTheTextReflowsEveryRelationOverTheStore() {
        assertTrue(evalBool("""
                (() => {
                    var store = createHanStore(POEM);
                    var a = createHanRelation(store, { cols: 9 });
                    var b = createHanRelation(store, { cols: 9 });
                    // Mount a few cells on both, as two grids would have.
                    ['r0','r1','r2','r3'].forEach(function (r) { for (var k = 0; k < 9; k++) { mount(a, r, 'c' + k); mount(b, r, 'c' + k); } });

                    // REPLACE one character: 乌 → 鸟. Both relations' cells follow, through the store.
                    store.set(POEM.replace('乌', '鸟'));
                    if (shown(a, 'r0', 'c2') !== '鸟' || shown(b, 'r0', 'c2') !== '鸟') return false;

                    // INSERT two characters into line 1: it grows to ten and WRAPS — the tenth
                    // lands on a new row, and every later row moves down one. Same cells, new ink.
                    store.set(store.text().replace('，', '，寒山'));
                    if (a.rows() !== 5 || a.presented().join(',') !== 'r0,r1,r2,r3,r4') return false;
                    if (shown(a, 'r0', 'c8') !== '寒' || shown(a, 'r1', 'c0') !== '山') return false;
                    if (shown(b, 'r2', 'c0') !== '江') return false;             // line 2 is row 2 now
                    if (mount(b, 'r4', 'c0')._ink.textContent !== '夜') return false;

                    // DELETE: take 山 out — the article shrinks back to four rows, and the cell
                    // the fifth row had is left alive with nothing in it.
                    store.set(store.text().replace('寒山', '寒'));
                    if (a.rows() !== 4 || a.presented().length !== 4 || shown(b, 'r1', 'c0') !== '江') return false;
                    if (shown(b, 'r4', 'c0') !== '') return false;
                    return shown(a, 'r0', 'c8') === '寒';
                })()"""), "replace, insert and delete in the text; every relation re-flows through the store");
    }
}
