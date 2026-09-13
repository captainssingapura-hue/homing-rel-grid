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
            // A row as a string: its squares' glyphs, '·' for an empty square.
            function rowStr(row) { return row.cells.map(function (s) { return s.glyph === null ? '·' : s.glyph; }).join(''); }
            function rowsStr(lay) { return lay.rows.map(rowStr).join('|'); }
            // A row with its squares bracketed, so a pair of marks reads as one square —
            // [月][落][」，] — and its half-squares, when it has them, in braces on either
            // side: {「}[月]…[天]{。}
            function slotsStr(lay) {
                return lay.rows.map(function (row) {
                    return (row.lead ? '{' + row.lead.glyph + '}' : '')
                         + row.cells.map(function (s) { return s.glyph === null ? '·' : '[' + s.glyph + ']'; }).join('')
                         + (row.trail ? '{' + row.trail.glyph + '}' : '');
                }).join('|');
            }
            // A cell is a NOUN: asked for its square, as the grid would ask; nothing rendered into it.
            function mount(rel, pk, col) { var c = rel.cellFor(pk, col); c.cellElement(); return c; }
            // What a square shows: the spans attached to its ink — a mark, or half-boxes — joined; nothing when none.
            function inkOf(c) { return c._ink.children.map(function (h) { return h.textContent; }).join(''); }
            function shown(rel, pk, col) { return inkOf(mount(rel, pk, col)); }
            // The policy stub's elements have no style; a run's cell sets a custom property on its.
            var _mk = makeEl;
            makeEl = function (t) {
                var el = _mk(t), props = {};
                el.style = { setProperty: function (k, v) { props[k] = v; }, removeProperty: function (k) { delete props[k]; },
                             getPropertyValue: function (k) { return props[k] || ''; } };
                return el;
            };
            """;

    @BeforeEach
    void setup() {
        js = buildContext();
        js.eval("js", DishPolicyTest.DOM_STUB);
        for (String m : DishPolicyTest.PARTY) loadModule(m);
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
                    // Every row is exactly nine squares, whatever the line held, and no
                    // half-square is used: the poem squeezes nothing.
                    for (var r = 0; r < 4; r++) if (lay.rows[r].cells.length !== 9 || lay.rows[r].lead || lay.rows[r].trail) return false;
                    if (lay.usesLead || lay.usesTrail) return false;
                    var s = lay.rows[1].cells[2];
                    if (s.glyph !== '渔' || s.kind !== 'han') return false;
                    // Each line's mark is alone, so it keeps its square — a lone mark.
                    return lay.rows[0].cells[7].glyph === '，' && lay.rows[0].cells[7].kind === 'punct'
                        && lay.rows[0].cells[8].glyph === null && lay.rows[0].cells[8].kind === 'empty';
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
                    return sup.glyphs === 2 && sup.rows[0].cells[0].glyph === '𠀋' && sup.rows[0].cells[1].glyph === '乙';
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
                    var pair = lay.rows[0].cells[3];
                    if (pair.kind !== 'punct' || pair.glyph !== '」，') return false;
                    if (lay.rows[0].cells[0].kind !== 'punct' || lay.rows[0].cells[1].kind !== 'han' || lay.rows[0].cells[8].kind !== 'empty') return false;
                    if (lay.glyphs !== 7) return false;                          // marks count as glyphs

                    // A pair does not widen a row, even across a closed one: eight characters
                    // and a pair of marks is nine slots and one row.
                    var nine = hanLayout('一二三四五六七八，。', 9);
                    if (slotsStr(nine) !== '[一][二][三][四][五][六][七][八][，。]') return false;

                    // Only within a line: nothing joins across a newline.
                    if (slotsStr(hanLayout('甲，\\n。乙', 9)) !== '[甲][，]·······|[。][乙]·······') return false;

                    return true;
                })()"""), "a mark joins a lone mark; a pair costs one slot; nothing joins across a line");
    }

    @Test
    void aClosingMarkAtTheHeadOfALineIsSqueezedIntoTheTrailingHalfSquare() {
        assertTrue(evalBool("""
                (() => {
                    // The case from the bench: nine characters fill the row, and the mark that
                    // follows may not start a line. It goes into the row's trailing half-square.
                    var lay = hanLayout('一二三四五六七八九。乙', 9);
                    if (slotsStr(lay) !== '[一][二][三][四][五][六][七][八][九]{。}|[乙]········') return false;
                    if (!lay.usesTrail || lay.usesLead) return false;
                    if (lay.rows[0].trail.kind !== 'punct' || lay.glyphs !== 11) return false;
                    // One mark fits; a second starts the next line after all.
                    if (slotsStr(hanLayout('一二三四五六七八九。」乙', 9)) !== '[一][二][三][四][五][六][七][八][九]{。}|[」][乙]·······') return false;
                    // Pairing comes first: a lone mark in the ninth square takes the next mark
                    // as its partner, and only a THIRD mark is squeezed.
                    if (slotsStr(hanLayout('一二三四五六七八，。！乙', 9)) !== '[一][二][三][四][五][六][七][八][，。]{！}|[乙]········') return false;
                    // Only after a WRAP. After a newline the mark starts the line the author
                    // gave it, and after a newline nothing is squeezed backwards.
                    if (slotsStr(hanLayout('一二三四五六七八九\\n。乙', 9)) !== '[一][二][三][四][五][六][七][八][九]|[。][乙]·······') return false;
                    if (slotsStr(hanLayout('甲\\n。乙', 9)) !== '[甲]········|[。][乙]·······') return false;
                    // An OPENER at the head of a line is not squeezed back — it opens what follows.
                    var open = hanLayout('一二三四五六七八九「乙', 9);
                    return slotsStr(open) === '[一][二][三][四][五][六][七][八][九]|[「][乙]·······' && !open.usesTrail;
                })()"""), "a closer that would start a line hangs off the previous line's trailing half-square");
    }

    @Test
    void anOpeningBracketThatWouldEndALineLeadsTheNextFromItsHalfSquare() {
        assertTrue(evalBool("""
                (() => {
                    // The opener would take the ninth square and end the line. The line closes
                    // with that square empty, and the opener leads the next line.
                    var lay = hanLayout('一二三四五六七八「九十', 9);
                    if (slotsStr(lay) !== '[一][二][三][四][五][六][七][八]·|{「}[九][十]·······') return false;
                    if (!lay.usesLead || lay.usesTrail || lay.rows[1].lead.kind !== 'punct') return false;
                    // The line it leads still has all nine squares, and may squeeze at its end too.
                    var both = hanLayout('一二三四五六七八「九十一二三四五六七。八', 9);
                    if (slotsStr(both) !== '[一][二][三][四][五][六][七][八]·|{「}[九][十][一][二][三][四][五][六][七]{。}|[八]········') return false;
                    if (!both.usesLead || !both.usesTrail) return false;
                    // An opener anywhere but the last square is just a square.
                    if (slotsStr(hanLayout('一「二', 9)) !== '[一][「][二]······') return false;
                    // A leading half-square alone is a row: the opener was the last thing typed.
                    return slotsStr(hanLayout('一二三四五六七八「', 9)) === '[一][二][三][四][五][六][七][八]·|{「}·········';
                })()"""), "an opener that would end a line leads the next line from its leading half-square");
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
                    var rel = createHanRelation(store, { cols: 9, capacity: 12, branch: ownBranch() });
                    // Identities are the CAPACITY; what is presented is the prefix in use.
                    if (rel.pks().length !== 12 || rel.pks()[11] !== 'r11') return false;
                    if (rel.presented().join(',') !== 'r0,r1,r2,r3') return false;
                    // The two half-square columns are declared always and presented only in use.
                    if (rel.columns().join(',') !== 'lead,c0,c1,c2,c3,c4,c5,c6,c7,c8,trail') return false;
                    if (rel.presentedColumns().join(',') !== 'c0,c1,c2,c3,c4,c5,c6,c7,c8') return false;
                    if (rel.narrowColumns().join(',') !== 'lead,trail') return false;
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
                    var rel = createHanRelation(store, { cols: 9, branch: ownBranch() });
                    // A character: the mark span alone, no halves.
                    var han = mount(rel, 'r0', 'c0');
                    if (inkOf(han) !== '甲' || han._ink.children.length !== 1) return false;
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
                    // square now holds a character, and its ink is the mark again — the halves
                    // detached, nothing re-minted.
                    return inkOf(pair) === '甲' && pair._ink.children.length === 1
                        && !/han-punct/.test(pair._ink.className);
                })()"""), "the square draws a pair as halves, a character as its mark, and swaps its spans when its kind changes");
    }

    @Test
    void theHalfSquareColumnsArePresentedWhenARowUsesThemAndTheirCellsAreNarrow() {
        assertTrue(evalBool("""
                (() => {
                    var store = createHanStore('一二三四五六七八九。乙');
                    var rel = createHanRelation(store, { cols: 9, branch: ownBranch() });
                    if (rel.presentedColumns().join(',') !== 'c0,c1,c2,c3,c4,c5,c6,c7,c8,trail') return false;
                    // The trailing cell: narrow, and showing the squeezed mark as one half-box.
                    var t = mount(rel, 'r0', 'trail');
                    if (!t.narrow() || !/han-narrow/.test(t._el.className)) return false;
                    if (inkOf(t) !== '。' || t._ink.children.length !== 1) return false;
                    // The squares are not narrow; a lead cell is, and is empty here.
                    if (mount(rel, 'r0', 'c0').narrow() || !mount(rel, 'r0', 'lead').narrow()) return false;
                    if (shown(rel, 'r0', 'lead') !== '') return false;
                    // Edit the text so the mark is no longer squeezed: the trailing column
                    // leaves the presentation and its cell shows nothing — alive, unshown.
                    store.set('一二三四五六七八九乙。');
                    if (rel.presentedColumns().indexOf('trail') >= 0 || inkOf(t) !== '') return false;
                    // And the other way: an opener that would end a line presents the leading column.
                    store.set('一二三四五六七八「九');
                    if (rel.presentedColumns().join(',') !== 'lead,c0,c1,c2,c3,c4,c5,c6,c7,c8') return false;
                    var l = mount(rel, 'r1', 'lead');
                    return inkOf(l) === '「' && /han-open/.test(l._ink.children[0].className);
                })()"""), "the half-square columns come and go with use; their cells are narrow and show the squeezed mark");
    }

    @Test
    void aRunOfNarrowCharactersIsOneSlotTwoToASquare() {
        assertTrue(evalBool("""
                (() => {
                    // What is narrow: Latin, digits, spaces, ASCII marks. What is not: a
                    // character, kana, hangul, a fullwidth letter, the ideographic space, a mark.
                    var narrow = ['w', 'A', '7', ' ', '.', '-', '?'];
                    for (var i = 0; i < narrow.length; i++) if (!hanIsNarrow(narrow[i])) return false;
                    var wide = ['月', 'あ', '한', 'Ａ', '\\u3000', '，', '「', '\\n', '𠀋'];
                    for (var j = 0; j < wide.length; j++) if (hanIsNarrow(wide[j])) return false;

                    // "what" is four narrow characters: one slot, two squares; the second is covered.
                    var lay = hanLayout('善哉what也', 9);
                    if (slotsStr(lay) !== '[善][哉][what]·[也]····') return false;
                    var run = lay.rows[0].cells[2], cov = lay.rows[0].cells[3];
                    if (run.kind !== 'latin' || run.span !== 2 || cov.kind !== 'covered' || cov.glyph !== null) return false;
                    if (lay.glyphs !== 7) return false;                        // letters count
                    // An odd run rounds up; a run with a space is one run; a single letter is one square.
                    if (hanLayout('abc', 9).rows[0].cells[0].span !== 2) return false;
                    if (slotsStr(hanLayout('a b', 9)) !== '[a b]········') return false;     // two squares: one covered, seven empty
                    if (hanLayout('x', 9).rows[0].cells[0].span !== 1) return false;
                    // The span key names the reach of every square, row by row.
                    return lay.spanKey === '112111111';
                })()"""), "a run of narrow characters is one slot reaching over ⌈n/2⌉ squares");
    }

    @Test
    void aRunFitsWholeOrMovesWholeOrBreaksAtTheRowsEnd() {
        assertTrue(evalBool("""
                (() => {
                    // Room for it: it stays. Eight characters, then "what" needs two squares
                    // and one is left — it moves whole to the next row.
                    if (slotsStr(hanLayout('一二三四五六七what', 9)) !== '[一][二][三][四][五][六][七][what]·') return false;
                    if (slotsStr(hanLayout('一二三四五六七八what', 9)) !== '[一][二][三][四][五][六][七][八]·|[what]········') return false;
                    // Longer than a row: broken at the row's end, two characters a square.
                    var long = hanLayout('internationalization', 9);      // 20 letters: 18 fill a row, 2 remain
                    if (slotsStr(long) !== '[internationalizati]········|[on]········') return false;
                    if (long.rows[0].cells[0].span !== 9 || long.rows[1].cells[0].span !== 1) return false;
                    // A run that fills the row wraps like anything else, and a closer that
                    // follows is squeezed into the trailing half-square as usual — the
                    // covered square reads as '·' here, since it holds nothing of its own.
                    return slotsStr(hanLayout('一二三四五六七what。', 9)) === '[一][二][三][四][五][六][七][what]·{。}';
                })()"""), "whole if it fits, whole on the next row if it fits one, else broken at the row's end");
    }

    @Test
    void aRunCellReachesOverItsSquaresAndSaysSo() {
        assertTrue(evalBool("""
                (() => {
                    var store = createHanStore('善哉what也');
                    var rel = createHanRelation(store, { cols: 9, branch: ownBranch() });
                    var run = mount(rel, 'r0', 'c2');
                    // The cell answers the reach the grid asks about, and draws itself that wide.
                    if (run.colSpan() !== 2 || !/han-run/.test(run._el.className)) return false;
                    if (inkOf(run) !== 'what' || run._ink.children.length !== 1) return false;
                    // The covered square has a cell of its own, showing nothing, reaching over nothing.
                    var cov = mount(rel, 'r0', 'c3');
                    if (cov.colSpan() !== 1 || shown(rel, 'r0', 'c3') !== '' || /han-run/.test(cov._el.className)) return false;
                    if (mount(rel, 'r0', 'c0').colSpan() !== 1) return false;
                    // The run moves: the same cells, their reach re-said, and the span key changed.
                    var before = rel.spanKey();
                    store.set('善what哉也');
                    if (rel.spanKey() === before) return false;
                    // c2 is now the square the run reaches over; 哉 moved to c3.
                    if (run.colSpan() !== 1 || shown(rel, 'r0', 'c2') !== '' || /han-run/.test(run._el.className)) return false;
                    if (shown(rel, 'r0', 'c3') !== '哉') return false;
                    var c1 = mount(rel, 'r0', 'c1');
                    return c1.colSpan() === 2 && shown(rel, 'r0', 'c1') === 'what' && /han-run/.test(c1._el.className);
                })()"""), "a run's cell answers colSpan and draws itself wide; the squares it covers show nothing");
    }

    @Test
    void anEditToTheTextReflowsEveryRelationOverTheStore() {
        assertTrue(evalBool("""
                (() => {
                    var store = createHanStore(POEM);
                    var a = createHanRelation(store, { cols: 9, branch: ownBranch() });
                    var b = createHanRelation(store, { cols: 9, branch: ownBranch() });
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
                    if (shown(b, 'r4', 'c0') !== '夜') return false;

                    // DELETE: take 山 out — the article shrinks back to four rows, and the cell
                    // the fifth row had is left alive with nothing in it.
                    store.set(store.text().replace('寒山', '寒'));
                    if (a.rows() !== 4 || a.presented().length !== 4 || shown(b, 'r1', 'c0') !== '江') return false;
                    if (shown(b, 'r4', 'c0') !== '') return false;
                    return shown(a, 'r0', 'c8') === '寒';
                })()"""), "replace, insert and delete in the text; every relation re-flows through the store");
    }
}
