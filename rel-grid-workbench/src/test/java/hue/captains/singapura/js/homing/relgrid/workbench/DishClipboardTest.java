package hue.captains.singapura.js.homing.relgrid.workbench;

import hue.captains.singapura.js.homing.ssjs.test.JsModuleTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The bench's copier and copy panel, tested without a grid: given the blocks
 * the grid would have resolved, what does the domain answer, and how does a
 * person choose it?
 *
 * <p>RFC 0050 · Episode 2, map 6 law 48: the copier reads <b>cells</b>. That is
 * what makes the rating come out as a number in text and as stars in HTML —
 * the cell knows what it is worth, and the store does not. No grid is
 * constructed anywhere here; the panel is driven the way a person drives it,
 * on a host the test minted in the grid's stead.</p>
 */
class DishClipboardTest extends JsModuleTestBase {

    private static final String GRID_DIR = "/homing/js/hue/captains/singapura/js/homing/relgrid/";
    private static final String BENCH_DIR = GRID_DIR + "workbench/";
    private static final String PROTOCOL = GRID_DIR + "protocol/RelGridProtocolModule.js";

    /** The policy test's DOM, plus a focus-tracking activeElement the panel's arrows need. */
    private static final String DOM_STUB = DishPolicyTest.DOM_STUB + """
            Object.defineProperty(document, 'activeElement', { get: function () { return __focused; } });
            """;

    private static final String HELPERS = """
            function blocks(spec) {
                // [[pks], [columns]] per block, as the grid would have resolved them.
                return spec.map(function (b) { return new RelGridBlock(b[0], b[1]); });
            }
            function bench(role) {
                var store = createDishStore();
                return { store: store, rel: createDishRelation(store, { role: role || 'follower', branch: testBranch() }) };
            }
            // What the grid hands a domain with its question: a mask whose panel(element)
            // PLACES the domain's element in the grid's box — here, a host of the test's.
            function maskOf(host) { return { panel: function (el) { host.appendChild(el); return true; } }; }

            // Find the panel's parts by class, wherever they sit.
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
        loadModule(GRID_DIR + "RelGridStockCellsModule.js");
        loadModule(BENCH_DIR + "DishStarsCellModule.js");
        loadModule(BENCH_DIR + "DishStore.js");
        loadModule(BENCH_DIR + "DishRelation.js");
        loadModule(BENCH_DIR + "DishClipboard.js");
        js.eval("js", HELPERS);
    }

    private boolean evalBool(String expr) { return js.eval("js", expr).asBoolean(); }
    private void act(String src) { js.eval("js", src); }

    @Test
    void tsvIsTabsAndNewlinesWithAHeaderRowByDefault() {
        assertTrue(evalBool("""
                (() => {
                    var b = bench();
                    var c = dishClipboardContent(b.rel, blocks([[['mapo', 'coq'], ['ingredient', 'calories', 'stars']]]), 'tsv');
                    if (!(c instanceof RelGridClipboardContent)) return false;
                    if (c.html !== undefined) return false;                              // text only
                    var lines = c.text.split('\\n');
                    if (lines.length !== 3) return false;
                    if (lines[0] !== 'ingredient\\tcalories\\tstars') return false;
                    var mapo = lines[1].split('\\t');
                    // The rating is a NUMBER in text — what a spreadsheet can add up.
                    return mapo[0] === b.store.get('mapo', 'ingredient')
                        && mapo[1] === String(b.store.get('mapo', 'calories'))
                        && mapo[2] === '4';
                })()"""), "TSV: header, then one line per pk, tab between columns; the rating is its number");
    }

    @Test
    void headersAreOptionalAndCsvQuotesOnlyWhatItMust() {
        assertTrue(evalBool("""
                (() => {
                    var b = bench('chef');
                    // A value with a comma, a quote and a newline: the three things CSV must quote.
                    b.store.commit('mapo', 'ingredient', 'tofu, "silken"\\nsoft');
                    var c = dishClipboardContent(b.rel, blocks([[['mapo', 'coq'], ['ingredient', 'price']]]), 'csv', { headers: false });
                    var lines = c.text.split('\\n');
                    // Two pks, and the newline INSIDE the quoted field makes a third line.
                    if (lines.length !== 3) return false;
                    if (lines[0] !== '"tofu, ""silken""') return false;
                    if (lines[1].indexOf('soft",') !== 0) return false;
                    // The plain value is not quoted at all.
                    return lines[2] === 'chicken,' + b.store.get('coq', 'price');
                })()"""), "CSV quotes a field only when it holds the delimiter, a quote or a newline; headers can be left off");
    }

    @Test
    void htmlIsATableAndTheStarsStayStars() {
        assertTrue(evalBool("""
                (() => {
                    var b = bench();
                    var c = dishClipboardContent(b.rel, blocks([[['fish'], ['ingredient', 'stars', 'calories']]]), 'html');
                    if (typeof c.html !== 'string') return false;
                    // Text rides beside it — as TSV, since that is what text/plain is for.
                    if (c.text.split('\\n')[1] !== 'cod\\t5\\t' + b.store.get('fish', 'calories')) return false;
                    if (!/^<table/.test(c.html) || !/<\\/table>$/.test(c.html)) return false;
                    if (!/<th[^>]*>ingredient<\\/th><th[^>]*>stars<\\/th>/.test(c.html)) return false;
                    // THE POINT: the rating is copied as what the person saw, styled inline
                    // so it survives the trip out of the page — not as the number 5.
                    if (c.html.indexOf('\\u2605\\u2605\\u2605\\u2605\\u2605') < 0) return false;
                    if (!/<span style="color:#[0-9a-f]{6};letter-spacing:2px" title="5 of 5">/.test(c.html)) return false;
                    // A number is right-aligned; text is not.
                    if (!/<td style="padding:2px 8px;text-align:right">\\d+<\\/td>/.test(c.html)) return false;
                    return /<td style="padding:2px 8px">cod<\\/td>/.test(c.html);
                })()"""), "HTML: a styled table with the stars as stars — the cell knows what it is worth");
    }

    @Test
    void htmlEscapesWhatItMustAndReadsTheCellNotTheStore() {
        assertTrue(evalBool("""
                (() => {
                    var b = bench('chef');
                    b.store.commit('coq', 'ingredient', '<b>chicken</b> & "rice"');
                    var c = dishClipboardContent(b.rel, blocks([[['coq'], ['ingredient']]]), 'html', { headers: false });
                    if (c.html.indexOf('&lt;b&gt;chicken&lt;/b&gt; &amp; &quot;rice&quot;') < 0) return false;
                    // The copier read the CELL. Prove it: set the cell behind the store's back,
                    // and the copier follows the cell rather than the store.
                    var cell = b.rel.cellFor('coq', 'ingredient');
                    cell.set('what the cell says');
                    var d = dishClipboardContent(b.rel, blocks([[['coq'], ['ingredient']]]), 'tsv', { headers: false });
                    return d.text === 'what the cell says';
                })()"""), "HTML is escaped, and the copier reads the relation's cells rather than the store");
    }

    @Test
    void severalBlocksAreKeptApartNotMerged() {
        assertTrue(evalBool("""
                (() => {
                    var b = bench();
                    var two = blocks([[['mapo', 'coq'], ['ingredient']], [['fish'], ['calories', 'stars']]]);
                    var t = dishClipboardContent(b.rel, two, 'tsv');
                    // A blank line between blocks in text; the shape is the person's, not squared off.
                    var parts = t.text.split('\\n\\n');
                    if (parts.length !== 2) return false;
                    if (parts[0].split('\\n').length !== 3 || parts[1].split('\\n').length !== 2) return false;
                    var h = dishClipboardContent(b.rel, two, 'html');
                    return (h.html.match(/<table/g) || []).length === 2;
                })()"""), "each block is its own section — a blank line in text, a table each in HTML");
    }

    @Test
    void thePanelOffersThreeFormatsAndAChoiceAnswersContent() {
        act("""
                var P = bench('nutritionist');
                var host = makeEl('div');
                var chosen = null, answer = 'unsettled';
                var q = new RelGridCopyRequested(blocks([[['mapo', 'coq', 'fish'], ['ingredient', 'stars']]]));
                var branch = hostBranch();
                dishCopyPanel(P.rel, q, maskOf(host), { branch: branch, onChosen: function (f, n) { chosen = f + ':' + n; } })
                    .then(function (c) { answer = c; });
                var root = host.children[0];
                if (branch.branchCount !== 1) throw new Error('the panel mints on a branch of its own for the session');
                var opts = byClass(root, 'wb-copy-opt');
                """);
        assertTrue(evalBool("""
                (() => {
                    if (!root || !/wb-copy/.test(root.className)) return false;
                    if (byClass(root, 'wb-copy-title')[0].textContent !== 'Copy 6 cells') return false;
                    if (opts.length !== 3) return false;
                    if (opts.map(function (o) { return o.format; }).join(',') !== 'tsv,csv,html') return false;
                    // The first format has the focus, and the preview shows it.
                    if (document.activeElement !== opts[0]) return false;
                    var pre = byClass(root, 'wb-copy-preview')[0];
                    if (pre.textContent.split('\\n')[0] !== 'ingredient\\tstars') return false;
                    // Move to HTML with the arrow: the preview follows.
                    root.dispatch('keydown', { key: 'ArrowRight' }); root.dispatch('keydown', { key: 'ArrowRight' });
                    if (document.activeElement !== opts[2]) return false;
                    if (!/^<table/.test(pre.textContent)) return false;
                    // Nothing has been answered yet.
                    if (chosen !== null || answer !== 'unsettled') return false;
                    opts[2].dispatch('click', {});
                    return chosen === 'html:6';
                })()"""), "the panel draws three formats with a live preview, and a click chooses");
        assertTrue(evalBool("""
                (() => {
                    // Settled on the next eval: content, with html, for the grid to write.
                    if (!(answer instanceof RelGridClipboardContent)) return false;
                    if (typeof answer.html !== 'string' || answer.html.indexOf('\\u2605') < 0) return false;
                    // And a second click cannot settle it again.
                    opts[0].dispatch('click', {});
                    // Settled, the session's branch is dissolved: its elements are out of the
                    // box, and the box is the grid's to take down.
                    return chosen === 'html:6' && branch.branchCount === 0 && host.children.length === 0;
                })()"""), "the promise settles once, with the chosen format's content, and the panel's branch dissolves");
    }

    @Test
    void hotkeysChooseAndEscapeAnswersNothing() {
        act("""
                var K = bench();
                var kHost = makeEl('div'), kAnswer = 'unsettled', kChosen = null;
                var kq = new RelGridCopyRequested(blocks([[['mapo'], ['price']]]));
                dishCopyPanel(K.rel, kq, maskOf(kHost), { branch: hostBranch(), onChosen: function (f) { kChosen = f; } }).then(function (c) { kAnswer = c; });
                kHost.children[0].dispatch('keydown', { key: 'c' });
                """);
        assertTrue(evalBool("kChosen === 'csv' && kAnswer instanceof RelGridClipboardContent && kAnswer.text === 'price\\n' + K.store.get('mapo', 'price')"),
                "C chooses CSV");
        act("""
                var eHost = makeEl('div'), eAnswer = 'unsettled', eChosen = null;
                dishCopyPanel(K.rel, kq, maskOf(eHost), { branch: hostBranch(), onChosen: function (f) { eChosen = f; } }).then(function (c) { eAnswer = c; });
                eHost.children[0].dispatch('keydown', { key: 'Escape' });
                """);
        assertTrue(evalBool("eChosen === null && eAnswer === undefined"),
                "Escape answers absence: nothing chosen, and the grid will write nothing");
        act("""
                var xHost = makeEl('div'), xAnswer = 'unsettled';
                dishCopyPanel(K.rel, kq, maskOf(xHost), { branch: hostBranch() }).then(function (c) { xAnswer = c; });
                byClass(xHost, 'wb-copy-cancel')[0].dispatch('click', {});
                """);
        assertTrue(evalBool("xAnswer === undefined"), "and so does the Cancel button");
    }

    @Test
    void theHeaderToggleChangesWhatIsAnswered() {
        act("""
                var T = bench();
                var tHost = makeEl('div'), tAnswer = 'unsettled';
                var tq = new RelGridCopyRequested(blocks([[['mapo'], ['ingredient']]]));
                dishCopyPanel(T.rel, tq, maskOf(tHost), { branch: hostBranch() }).then(function (c) { tAnswer = c; });
                var box = byClass(tHost, 'wb-copy-foot')[0].children[0].children[0];
                box.checked = false;
                box.dispatch('change', {});
                var pre = byClass(tHost, 'wb-copy-preview')[0];
                var previewNow = pre.textContent;
                tHost.children[0].dispatch('keydown', { key: 't' });
                """);
        assertTrue(evalBool("previewNow === 'tofu' && tAnswer.text === 'tofu'"),
                "with headers off the preview and the answer are the values alone");
    }
}
