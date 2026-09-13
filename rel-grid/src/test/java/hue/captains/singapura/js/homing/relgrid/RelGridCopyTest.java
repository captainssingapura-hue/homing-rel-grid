package hue.captains.singapura.js.homing.relgrid;

import hue.captains.singapura.js.homing.ssjs.test.JsModuleTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * RFC 0050 · Episode 2 — copy (map 6) and the pending answer (ext6), together,
 * because copy is the first question the grid waits for.
 *
 * <p>Three things are proved here. The grid resolves the selection to
 * <b>identities</b> and asks (laws 46, 47); while the answer is outstanding the
 * grid is <b>locked and masked</b>, and the domain may draw on the mask's panel
 * (laws 220–225); and the grid <b>writes</b> what comes back and reads none of
 * it (laws 48–50). Absence writes nothing (law 49).</p>
 *
 * <p>Two harness facts shape the tests. GraalVM drains microtasks between
 * evals, not within one, so anything that waits on a promise acts in one eval
 * and asserts in the next. Timers are fake: {@code runTimers()} fires what is
 * armed, so the mask's delay and hold are stepped rather than slept
 * through.</p>
 */
class RelGridCopyTest extends JsModuleTestBase {

    @BeforeEach
    void setup() {
        js = buildContext();
        js.eval("js", RelGridTestDom.DOM_STUB);
        js.eval("js", RelGridTestDom.STYLES);
        for (String m : RelGridTestDom.PARTY) loadModule(m);
        loadModule(RelGridTestDom.PROTOCOL);
        loadModule(RelGridTestDom.SELECTION);
        for (String m : RelGridTestDom.MODULES) loadModule(RelGridTestDom.DIR + m);
        js.eval("js", RelGridTestDom.FIXTURE);
    }

    private boolean evalBool(String expr) { return js.eval("js", expr).asBoolean(); }
    private void act(String src) { js.eval("js", src); }

    @Test
    void ctrlCAsksWithTheSelectionResolvedToIdentities() {
        assertTrue(evalBool("""
                (() => {
                    // The fixture's default answer is ABSENCE, and it is asynchronous, so
                    // within this eval the question is still outstanding.
                    var f = fixture();
                    f.click(0, 0); f.click(2, 1, { shift: true });
                    var ev = { key: 'c', ctrlKey: true, prevented: false };
                    ev.preventDefault = function () { ev.prevented = true; };
                    f.table().dispatch('keydown', ev);
                    var q = f.copyAsked();
                    if (!q || !(q instanceof RelGridCopyRequested)) return false;
                    if (!ev.prevented) return false;                          // consumed: the grid took it
                    // One block per range; pks down, columns across, in view order.
                    if (q.blocks.length !== 1) return false;
                    var b = q.blocks[0];
                    if (!(b instanceof RelGridBlock)) return false;
                    if (b.pks.join(',') !== 'mapo,coq,fish') return false;
                    if (b.columns.join(',') !== 'ingredient,calories') return false;
                    // And nothing but identities: no cell, no text, no number.
                    return Object.keys(b).join(',') === 'pks,columns' && f.grid.isPending();
                })()"""), "Ctrl+C asks with the selection as identities, one block per range");
    }

    @Test
    void aMultiRangeSelectionArrivesFaithfullyAsOneBlockPerRange() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture();
                    f.click(0, 0); f.click(1, 0, { shift: true });          // rows 0-1 of column 0
                    f.click(2, 1, { ctrl: true });                          // and one cell, added
                    if (!f.grid.copy()) return false;
                    var q = f.copyAsked();
                    if (q.blocks.length !== 2) return false;
                    return q.blocks[0].pks.join(',') === 'mapo,coq' && q.blocks[0].columns.join(',') === 'ingredient'
                        && q.blocks[1].pks.join(',') === 'fish'     && q.blocks[1].columns.join(',') === 'calories';
                })()"""), "ranges are resolved in creation order, with no bounding box and no merge");
    }

    @Test
    void withNothingSelectedTheCursorsOwnCellIsWhatIsCopied() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture();
                    f.click(1, 1);
                    if (!f.grid.copy()) return false;
                    var q = f.copyAsked();
                    return q.blocks.length === 1 && q.blocks[0].pks.join(',') === 'coq'
                        && q.blocks[0].columns.join(',') === 'calories';
                })()"""), "an empty selection resolves to the cursor's 1x1, so copy always has something to ask about");
    }

    @Test
    void whilePendingEveryIntentIsRefusedNotDeferred() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture({ editable: true });
                    f.click(0, 0);
                    if (!f.grid.copy()) return false;
                    if (!f.grid.isPending()) return false;
                    var before = f.painted(), cursor = f.grid.cursor();
                    // Every gesture, and every programmatic twin, and a second question.
                    f.key('ArrowDown'); f.key('ArrowRight', { shift: true }); f.key('a', { ctrl: true });
                    f.key('Enter');
                    f.click(2, 1); f.click(1, 1, { shift: true }); f.click(2, 0, { ctrl: true });
                    f.drag([0, 0], [[1, 0], [2, 0]]);
                    if (f.grid.selectCell('fish', 'calories') !== false) return false;
                    if (f.grid.extendSelection('fish', 'calories') !== false) return false;
                    if (f.grid.addToSelection('fish', 'calories') !== false) return false;
                    if (f.grid.selectAll() !== false) return false;
                    if (f.grid.takeControlAtCursor() !== false) return false;
                    if (f.grid.setColumnWidth('ingredient', 300) !== false) return false;
                    if (f.grid.copy() !== false) return false;                // one question at a time
                    return f.painted() === before && f.grid.cursor().pk === cursor.pk
                        && !f.grid.isDeep() && f.grid.isPending()
                        && f.sent.filter(function (q) { return q instanceof RelGridCopyRequested; }).length === 1;
                })()"""), "while an answer is owed the grid is locked: refused, not queued");
    }

    @Test
    void theDomainMayAskForThePanelAndTheMaskGoesUpAtOnce() {
        assertTrue(evalBool("""
                (() => {
                    var drawn = makeEl('div'), placed = [];
                    drawn.textContent = 'choose a format';                    // the domain's own element, drawn already
                    var f = fixture({ ask: function (q, mask) {
                        if (!(q instanceof RelGridCopyRequested)) return Promise.resolve();
                        placed.push(mask.panel(drawn));
                        placed.push(mask.panel(drawn));                        // again: the same box, nothing doubled
                        return new Promise(function () {});                    // and thinks
                    }});
                    f.click(0, 0);
                    if (!f.grid.copy()) return false;
                    var m = f.mask(), p = f.panel();
                    if (!m || !p || placed.join() !== 'true,true') return false;
                    // A sibling of the table in the wrapper, over it — never in it.
                    if (m.parentNode !== f.wrap() || f.wrap().children[0] !== f.table()) return false;
                    if (!/hrg-panel/.test(p.className) || p.parentNode !== m) return false;
                    // Placed and sized by the grid; the content is the domain's, in the box, once.
                    var st = p.style;                                     // the geometry the grid measured, as custom properties its class reads
                    if (st.getPropertyValue('--hrg-left') === '' || st.getPropertyValue('--hrg-top') === '') return false;
                    if (st.getPropertyValue('--hrg-width') === '' || st.getPropertyValue('--hrg-height') === '') return false;
                    if (p.children.length !== 1 || p.children[0] !== drawn) return false;
                    // The box is never handed out: the domain must hand an element.
                    var refused = false;
                    try { f.handles[f.handles.length - 1].panel(); } catch (e) { refused = /hands its own element/.test(String(e)); }
                    if (!refused) return false;
                    // The table wears the state, and no delay timer is left armed: the
                    // domain asked, so the clock has nothing to do.
                    return /hrg-masked/.test(f.table().className) && pendingTimers() === 0
                        && document.activeElement === p;                       // the keys stop in the panel
                })()"""), "asking for the panel mounts the mask at once, with the panel as the domain's canvas");
    }

    @Test
    void theGoldenBoxIsAGoldenRectangleAtTheGoldenSection() {
        assertTrue(evalBool("""
                (() => {
                    var phi = (1 + Math.sqrt(5)) / 2;
                    // A tall area: the WIDTH binds. width = W/φ; height = width/φ; centred.
                    var b = _hrgGoldenBox(1000, 2000);
                    if (b.width !== Math.round(1000 / phi)) return false;                  // 618
                    if (b.height !== Math.round(1000 / phi / phi)) return false;           // 382
                    if (Math.abs(b.width / b.height - phi) > 0.01) return false;           // itself golden
                    if (b.left !== Math.round((1000 - 1000 / phi) / 2)) return false;      // 191
                    if (b.top !== Math.round((2000 - 1000 / phi / phi) / 2)) return false; // 809
                    // A wide, short area: the HEIGHT binds. The panel is no taller than H/φ,
                    // and still golden — so it is H wide.
                    var s = _hrgGoldenBox(1000, 400);
                    if (s.width !== 400 || s.height !== Math.round(400 / phi)) return false;   // 400 × 247
                    if (s.left !== 300 || s.top !== Math.round((400 - 400 / phi) / 2)) return false;
                    // The floor: a small area still gets a panel that can hold something,
                    // even if it overflows — that is what the floor is for.
                    var f = _hrgGoldenBox(300, 100);
                    if (f.width !== 320 || f.height !== Math.round(320 / phi) || f.top >= 0) return false;
                    // The ceiling: a vast area does not mint a page.
                    var g = _hrgGoldenBox(3000, 2000);
                    if (!(g.width === 720 && g.height === Math.round(720 / phi) && g.left === (3000 - 720) / 2)) return false;
                    // And the panel is centred on the HOST'S BOX, not on the table: a
                    // scrollport 300 tall over a 900-tall table, scrolled to its middle.
                    var seen = _hrgVisibleBox({ left: 0, top: -300, right: 500, bottom: 600, width: 500, height: 900 },
                                              { left: 0, top: 0,    right: 500, bottom: 300, width: 500, height: 300 }, null);
                    if (seen.left !== 0 || seen.top !== 300 || seen.width !== 500 || seen.height !== 300) return false;
                    // A short table in a tall container: the container's box, so the panel
                    // may use the room below the table rather than overflow above it.
                    var room = _hrgVisibleBox({ left: 10, top: 10, right: 210, bottom: 110, width: 200, height: 100 },
                                              { left: 10, top: 10, right: 210, bottom: 250, width: 200, height: 240 }, null);
                    if (room.left !== 0 || room.top !== 0 || room.width !== 200 || room.height !== 240) return false;
                    // A container taller than the window: clipped to the viewport, so the
                    // panel lands on the screen and not on the page.
                    var page = _hrgVisibleBox({ left: 0, top: 0, right: 800, bottom: 3000, width: 800, height: 3000 },
                                              { left: 0, top: 0, right: 800, bottom: 3000, width: 800, height: 3000 },
                                              { left: 0, top: 0, right: 800, bottom: 600 });
                    return page.top === 0 && page.height === 600 && page.width === 800;
                })()"""), "the panel is golden, at the golden section of the visible area in both dimensions, floored and ceilinged");
    }

    @Test
    void aFastAnswerShowsNoMaskAndTheContentIsWritten() {
        act("""
                var F = fixture({ ask: function (q) {
                    if (!(q instanceof RelGridCopyRequested)) return Promise.resolve();
                    return Promise.resolve(new RelGridClipboardContent('tofu\\t480', '<table><tr><td>tofu</td></tr></table>'));
                }});
                F.click(0, 0);
                F.grid.copy();
                """);
        assertTrue(evalBool("""
                (() => {
                    // Answered on a microtask, well inside the delay: the mask never mounted.
                    if (F.mask() !== null || F.wrap().children.length !== 1) return false;
                    if (F.grid.isPending()) return false;
                    if (pendingTimers() !== 0) return false;                   // the delay was cancelled
                    // The grid WROTE it — both forms — and reported it; it read nothing.
                    if (F.written.length !== 1) return false;
                    var c = F.written[0];
                    if (!(c instanceof RelGridClipboardContent)) return false;
                    if (c.text !== 'tofu\\t480' || !/<table>/.test(c.html)) return false;
                    return F.copied.length === 1 && F.copied[0] === c;
                })()"""), "a quick answer masks nothing, and what came back is written and reported as-is");
    }

    @Test
    void aSlowAnswerWithNoPanelDimsAfterTheThresholdAndIsHeld() {
        act("""
                var G = fixture({ ask: function (q) {
                    if (!(q instanceof RelGridCopyRequested)) return Promise.resolve();
                    return new Promise(function (r) { G_answer = r; });
                }});
                var G_answer = null;
                G.click(0, 0);
                G.grid.copy();
                """);
        assertTrue(evalBool("""
                (() => {
                    if (G.mask() !== null) return false;                       // not yet: the delay
                    if (pendingTimers() !== 1) return false;
                    runTimers();                                               // the threshold passes
                    var m = G.mask();
                    if (!m || G.panel() !== null) return false;                // a bare wash: the grid invents no progress
                    if (!/hrg-masked/.test(G.table().className)) return false;
                    return document.activeElement === m && G.grid.isPending();
                })()"""), "a slow answer masks after the threshold, with nothing drawn on it");
        act("G_answer(new RelGridClipboardContent('x', undefined));");
        assertTrue(evalBool("""
                (() => {
                    // Answered: written at once, no longer pending — but the mask is HELD,
                    // because it went up just now and must not strobe.
                    if (G.written.length !== 1 || G.written[0].text !== 'x') return false;
                    if (G.grid.isPending()) return false;
                    if (G.mask() === null || pendingTimers() !== 1) return false;
                    runTimers();                                               // the hold ends
                    return G.mask() === null && !/hrg-masked/.test(G.table().className)
                        && document.activeElement === G.table();               // the keys come home
                })()"""), "the answer applies at once; the mask comes down after its hold, and the focus returns");
    }

    @Test
    void absenceWritesNothingAndTheGridResumes() {
        act("""
                var H = fixture({ ask: function (q, mask) {
                    if (!(q instanceof RelGridCopyRequested)) return Promise.resolve();
                    mask.panel(makeEl('div'));
                    return new Promise(function (r) { H_answer = r; });
                }});
                var H_answer = null;
                H.click(0, 0);
                H.grid.copy();
                H_answer(undefined);                                           // cancel is the domain answering absence
                """);
        assertTrue(evalBool("""
                (() => {
                    if (H.written.length !== 0 || H.copied.length !== 0) return false;
                    if (H.grid.isPending()) return false;
                    runTimers();                                               // the hold
                    if (H.mask() !== null) return false;
                    // And the grid is itself again: intents work.
                    H.key('ArrowDown');
                    return H.grid.cursor().pk === 'coq' && H.grid.copy() === true;
                })()"""), "absence writes nothing, the clipboard keeps what it had, and the grid is unlocked");
    }

    @Test
    void aRejectionAndAWrongAnswerAreRecordedAndTheGridResumes() {
        act("""
                var errors = [];
                console.error = function () { errors.push(Array.prototype.slice.call(arguments).join(' ')); };
                var R = fixture({ ask: function (q) {
                    if (!(q instanceof RelGridCopyRequested)) return Promise.resolve();
                    return Promise.reject(new Error('the domain fell over'));
                }});
                R.click(0, 0);
                R.grid.copy();
                """);
        assertTrue(evalBool("""
                (() => {
                    if (R.grid.isPending() || R.written.length !== 0) return false;
                    if (!errors.some(function (e) { return /rejected/.test(e); })) return false;
                    return true;
                })()"""), "a rejected question is recorded, nothing is written, and the grid is unlocked");
        act("""
                var W = fixture({ ask: function (q) {
                    if (!(q instanceof RelGridCopyRequested)) return Promise.resolve();
                    return Promise.resolve({ text: 'not the protocol' });     // a plain object is not an answer
                }});
                W.click(0, 0);
                W.grid.copy();
                """);
        assertTrue(evalBool("""
                (() => {
                    return !W.grid.isPending() && W.written.length === 0
                        && errors.some(function (e) { return /not clipboard content/.test(e); });
                })()"""), "an answer that is not a protocol value is recorded and not written");
    }

    @Test
    void noChannelNoCopyAndTheBrowserKeepsItsOwn() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture({ noAsk: true });
                    f.click(0, 0);
                    if (f.grid.copy() !== false) return false;
                    var ev = { key: 'c', ctrlKey: true, prevented: false };
                    ev.preventDefault = function () { ev.prevented = true; };
                    f.table().dispatch('keydown', ev);
                    // Not consumed: the browser's own copy is left alone.
                    return !ev.prevented && !f.grid.isPending() && f.mask() === null;
                })()"""), "with no channel there is nothing to ask, and the keystroke is not consumed");
    }

    @Test
    void deepAndPendingAreExclusive() {
        act("""
                var D = fixture({ editable: true });
                D.click(0, 0);
                D.key('Enter');                                                // deep
                """);
        assertTrue(evalBool("""
                (() => {
                    if (!D.grid.isDeep()) return false;
                    // No question while a cell holds control (law 228)...
                    if (D.grid.copy() !== false || D.copyAsked() !== null) return false;
                    D.overlay().children[0].dispatch('keydown', { key: 'Escape' });
                    return true;
                })()"""), "no copy is asked while a cell is deep");
        act("""
                var D2 = fixture({ editable: true, ask: function (q) {
                    return (q instanceof RelGridCopyRequested) ? new Promise(function () {}) : Promise.resolve();
                }});
                D2.click(0, 0);
                D2.grid.copy();
                """);
        assertTrue(evalBool("""
                (() => {
                    // ...and no cell is entered while the grid is masked.
                    if (!D2.grid.isPending()) return false;
                    D2.key('Enter');
                    return D2.grid.takeControlAtCursor() === false && !D2.grid.isDeep() && D2.overlay() === null;
                })()"""), "no cell is entered while an answer is owed");
    }

    @Test
    void aSettleAfterDestroyDoesNothing() {
        act("""
                var X = fixture({ ask: function (q) {
                    if (!(q instanceof RelGridCopyRequested)) return Promise.resolve();
                    return new Promise(function (r) { X_answer = r; });
                }});
                var X_answer = null;
                X.click(0, 0);
                X.grid.copy();
                runTimers();                                                   // masked
                X.grid.destroy();
                X_answer(new RelGridClipboardContent('late', undefined));
                """);
        assertTrue(evalBool("""
                (() => {
                    return X.written.length === 0 && X.copied.length === 0
                        && X.container.children.length === 0 && pendingTimers() === 0;
                })()"""), "a late answer to a dead grid writes nothing and arms nothing");
    }

    @Test
    void theStockWriterUsesTheAsyncClipboardWithBothFormsWhenItCan() {
        assertTrue(evalBool("""
                (() => {
                    // A navigator with the modern API: one item, two forms.
                    var got = null;
                    var FakeBlob = function (parts, o) { this.parts = parts; this.type = o.type; };
                    var FakeItem = function (m) { this.m = m; };
                    var modern = { clipboard: {
                        write:     function (items) { got = { items: items }; return Promise.resolve(); },
                        writeText: function (t) { got = { text: t }; return Promise.resolve(); }
                    }};
                    var w = createRelGridClipboard({ navigator: modern, ClipboardItem: FakeItem, Blob: FakeBlob });
                    var rich = new RelGridClipboardContent('a\\tb', '<b>a</b>');
                    var plain = new RelGridClipboardContent('a\\tb', undefined);
                    w.write(rich);
                    if (!got || !got.items || got.items.length !== 1) return false;
                    var item = got.items[0];
                    if (!(item instanceof FakeItem)) return false;
                    if (item.m['text/plain'].parts[0] !== 'a\\tb' || item.m['text/html'].parts[0] !== '<b>a</b>') return false;
                    if (item.m['text/html'].type !== 'text/html') return false;
                    w.write(plain);
                    if (got.text !== 'a\\tb') return false;
                    // Without ClipboardItem the rich content still goes, as text.
                    got = null;
                    createRelGridClipboard({ navigator: modern, ClipboardItem: null, Blob: FakeBlob }).write(rich);
                    if (got.text !== 'a\\tb') return false;
                    // And with no clipboard at all — an insecure origin — a rejection, not a silence.
                    var out = createRelGridClipboard({ navigator: {}, ClipboardItem: null, Blob: null }).write(plain);
                    return !!out && typeof out.then === 'function';
                })()"""), "the stock writer sends both forms as one item when it can, and the plain one otherwise");
    }

    @Test
    void whenTheAsyncClipboardIsRefusedTheWriterTakesTheCommandRoad() {
        act("""
                // A page where the async API is DENIED — an embedded page under a
                // permissions policy — but the copy command still runs. The command
                // fires a copy event, and the writer answers it with both forms.
                var byCommand = null, cmdCalls = 0, listeners = [];
                var denied = { clipboard: { write: function () { return Promise.reject(new Error('Write permission denied')); },
                                            writeText: function () { return Promise.reject(new Error('Write permission denied')); } } };
                var table = makeEl('table');
                table.focus();                                                 // where the focus was
                var doc = {
                    body: makeEl('body'),
                    get activeElement() { return __focused; },
                    createElement: function (t) { return makeEl(t); },
                    addEventListener: function (t, fn) { listeners.push(fn); },
                    removeEventListener: function (t, fn) { listeners = listeners.filter(function (f) { return f !== fn; }); },
                    execCommand: function (cmd) {
                        cmdCalls++;
                        if (cmd !== 'copy') return false;
                        var data = {};
                        var ev = { clipboardData: { setData: function (k, v) { data[k] = v; } }, preventDefault: function () {} };
                        listeners.slice().forEach(function (fn) { fn(ev); });
                        byCommand = data;
                        return true;
                    }
                };
                var FakeBlob = function (parts, o) { this.parts = parts; this.type = o.type; };
                var FakeItem = function (m) { this.m = m; };
                var branch = hostBranch();                                     // the grid's, which the grid activated
                var W = createRelGridClipboard({ navigator: denied, ClipboardItem: FakeItem, Blob: FakeBlob, document: doc, branch: branch });
                var outcome = 'pending';
                W.write(new RelGridClipboardContent('a\\tb', '<b>a</b>')).then(function () { outcome = 'written'; },
                                                                                function (e) { outcome = 'failed: ' + e.message; });
                """);
        assertTrue(evalBool("""
                (() => {
                    if (outcome !== 'written') return false;
                    if (cmdCalls !== 1) return false;
                    if (!byCommand || byCommand['text/plain'] !== 'a\\tb' || byCommand['text/html'] !== '<b>a</b>') return false;
                    // The selection it needed is gone again, nothing is left listening,
                    // and the focus it took to select is back where it was.
                    return doc.body.children.length === 0 && listeners.length === 0
                        && branch.branchCount === 0                                // the scratch branch dissolved with its textarea
                        && __focused === table;
                })()"""), "denied the async API, the writer copies through the command, both forms, and cleans up");
        act("""
                // Both roads closed: a rejection that says so, never a silence.
                var noDoc = createRelGridClipboard({ navigator: denied, ClipboardItem: null, Blob: null, document: null, branch: hostBranch() });
                var outcome2 = 'pending';
                noDoc.write(new RelGridClipboardContent('x', undefined)).then(function () { outcome2 = 'written'; },
                                                                              function (e) { outcome2 = e.message; });
                """);
        assertTrue(evalBool("/refused both roads/.test(outcome2) && /denied/.test(outcome2)"),
                "with both roads closed the write rejects, naming the reason");
    }
}
