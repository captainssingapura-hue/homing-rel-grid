package hue.captains.singapura.js.homing.relchannel;

import hue.captains.singapura.js.homing.ssjs.test.JsModuleTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The ask channel's core, on a bare context: no DOM, a surface that records
 * what the mask asked of it, fake clocks driven by the test. What the grid's
 * copy and handover tests prove through the grid, this proves of the core
 * alone — so the tree may stand on it without a grid in the room.
 */
class RelChannelTest extends JsModuleTestBase {

    private static final String MODULE =
            "/homing/js/hue/captains/singapura/js/homing/relchannel/RelChannelModule.js";

    /** Fake clocks, a recording surface, and a channel whose ask a test scripts. */
    private static final String STUB = """
            var __timers = [], __tid = 0, __now = 1000;
            function setTimeout(fn, ms) { __timers.push({ id: ++__tid, fn: fn, ms: ms || 0 }); return __tid; }
            function clearTimeout(id) { for (var k = 0; k < __timers.length; k++) if (__timers[k].id === id) { __timers.splice(k, 1); return; } }
            function runTimers(advance) {
                __now += (advance || 0);
                var due = __timers.slice().sort(function (a, b) { return a.ms - b.ms || a.id - b.id; });
                __timers = [];
                for (var k = 0; k < due.length; k++) due[k].fn();
                return due.length;
            }
            var Date = { now: function () { return __now; } };
            var errors = [];
            var console = { error: function () { errors.push(Array.prototype.join.call(arguments, ' ')); } };
            function harness(opts) {
                opts = opts || {};
                var log = [], sent = [], handles = [], applied = [];
                var surface = {
                    openMask:   function () { log.push('openMask'); },
                    closeMask:  function () { log.push('closeMask'); },
                    setMasked:  function (on) { log.push('masked:' + on); },
                    openPanel:  function (el) { log.push('openPanel:' + el.name); },
                    closePanel: function () { log.push('closePanel'); },
                    focus:      function () { log.push('focus'); }
                };
                var channel = new RelChannel({
                    surface: surface, tag: '[T]',
                    ask: opts.noAsk ? undefined : function (q, mask) { sent.push(q); handles.push(mask); return opts.ask ? opts.ask(q, mask) : Promise.resolve(); }
                });
                return { channel: channel, log: log, sent: sent, handles: handles, applied: applied,
                         apply: function (a) { applied.push(a); } };
            }
            // Promises settle in microtasks; the test drains them by awaiting a resolved one.
            async function tick() { await Promise.resolve(); await Promise.resolve(); await Promise.resolve(); }
            """;

    @BeforeEach
    void setup() {
        js = buildContext();
        js.eval("js", STUB);
        loadModule(MODULE);
    }

    private boolean evalBool(String expr) { return js.eval("js", expr).asBoolean(); }

    @Test
    void aNotificationIsHandedOverAndNeverWaitedOn() {
        assertTrue(evalBool("""
                (() => {
                    var h = harness();
                    h.channel.notify({ kind: 'n' });
                    if (h.sent.length !== 1 || h.handles[0] !== undefined || h.channel.isPending()) return false;
                    if (h.log.length !== 0) return false;                       // no mask, ever
                    var none = harness({ noAsk: true });
                    none.channel.notify({ kind: 'n' });
                    return !none.channel.has() && none.sent.length === 0 && none.channel.ask({}, function () {}) === false;
                })()"""), "a notification goes down without a wait, a mask, or a channel to go down");
    }

    @Test
    void aQuestionAnsweredAtOnceShowsNoMaskAndAppliesItsAnswer() throws Exception {
        js.eval("js", """
                var out = (async () => {
                    var h = harness({ ask: function () { return Promise.resolve({ ok: 1 }); } });
                    var taken = h.channel.ask({ kind: 'q' }, h.apply);
                    if (!taken || !h.channel.isPending() || h.sent.length !== 1) return 'not taken';
                    if (h.channel.ask({ kind: 'q2' }, h.apply) !== false) return 'a second question while one is pending';
                    await tick();
                    if (h.channel.isPending() || h.applied.length !== 1 || h.applied[0].ok !== 1) return 'not applied: ' + JSON.stringify(h.applied);
                    if (h.log.length !== 0) return 'mask shown: ' + h.log.join(',');
                    runTimers(300);                                             // the delay clock was cancelled with the settle
                    return h.log.length === 0 ? 'ok' : 'late mask: ' + h.log.join(',');
                })();
                """);
        drain();
        assertTrue(evalBool("__result === 'ok'"), js.eval("js", "String(__result)").asString());
    }

    @Test
    void aLateAnswerIsMaskedAfterTheDelayAndHeldForTheHold() throws Exception {
        js.eval("js", """
                var out = (async () => {
                    var resolve = null;
                    var h = harness({ ask: function () { return new Promise(function (r) { resolve = r; }); } });
                    h.channel.ask({ kind: 'q' }, h.apply);
                    if (h.log.length !== 0) return 'masked too soon';
                    runTimers(200);                                             // the delay elapses: the wash goes up
                    if (h.log.join(',') !== 'openMask,masked:true') return 'after the delay: ' + h.log.join(',');
                    runTimers(100);                                             // 100 ms up
                    resolve({ ok: 2 });
                    await tick();
                    // Applied, the panel down at once, the wash still up: 100 of the 250 ms hold.
                    if (h.applied.length !== 1 || h.log.join(',') !== 'openMask,masked:true,closePanel') return 'on the answer: ' + h.log.join(',');
                    if (h.channel.isPending()) return 'still pending';
                    runTimers(150);                                             // the rest of the hold
                    return h.log.join(',') === 'openMask,masked:true,closePanel,closeMask,masked:false,focus' ? 'ok' : 'after the hold: ' + h.log.join(',');
                })();
                """);
        drain();
        assertTrue(evalBool("__result === 'ok'"), js.eval("js", "String(__result)").asString());
    }

    @Test
    void thePanelHandleMountsTheMaskAtOnceWithTheDomainsElement() throws Exception {
        js.eval("js", """
                var out = (async () => {
                    var resolve = null, thrown = false;
                    var h = harness({ ask: function (q, mask) {
                        try { mask.panel(null); } catch (e) { thrown = true; }            // not an element: refused
                        mask.panel({ name: 'p', appendChild: function () {} });
                        return new Promise(function (r) { resolve = r; });
                    } });
                    h.channel.ask({ kind: 'q' }, h.apply);
                    if (!thrown) return 'a non-element was taken';
                    if (h.log.join(',') !== 'openMask,masked:true,openPanel:p') return 'on panel: ' + h.log.join(',');
                    runTimers(200);                                             // the delay clock: cancelled by the mount, nothing fires
                    if (h.log.length !== 3) return 'mounted twice: ' + h.log.join(',');
                    resolve();
                    await tick();
                    if (h.handles[0].panel({ name: 'late', appendChild: function () {} }) !== false) return 'a late panel was placed';
                    runTimers(250);
                    return h.log.join(',') === 'openMask,masked:true,openPanel:p,closePanel,closeMask,masked:false,focus' ? 'ok' : h.log.join(',');
                })();
                """);
        drain();
        assertTrue(evalBool("__result === 'ok'"), js.eval("js", "String(__result)").asString());
    }

    @Test
    void aFailedAskSettlesWithNothingAndIsRecordedAndDestroyCancelsALateClock() throws Exception {
        js.eval("js", """
                var out = (async () => {
                    var h = harness({ ask: function () { throw new Error('boom'); } });
                    if (h.channel.ask({ kind: 'q' }, h.apply) !== true) return 'the gesture was not taken';
                    if (h.channel.isPending() || h.applied.length !== 1 || h.applied[0] !== undefined) return 'a throw did not settle with nothing';
                    if (!errors.some(function (e) { return /\\[T\\] ask threw/.test(e); })) return 'not recorded';
                    var r = harness({ ask: function () { return Promise.reject(new Error('no')); } });
                    r.channel.ask({ kind: 'q' }, r.apply);
                    await tick();
                    if (r.channel.isPending() || r.applied.length !== 1 || !errors.some(function (e) { return /ask rejected/.test(e); })) return 'a rejection did not settle';
                    var a = harness({ ask: function () { return Promise.resolve(1); } });
                    a.channel.ask({ kind: 'q' }, function () { throw new Error('apply'); });
                    await tick();
                    if (a.channel.isPending() || !errors.some(function (e) { return /applying an answer threw/.test(e); })) return 'an apply throw was not recorded';
                    var d = harness({ ask: function () { return new Promise(function () {}); } });
                    d.channel.ask({ kind: 'q' }, d.apply);
                    d.channel.destroy();
                    if (d.channel.isPending()) return 'pending after destroy';
                    return runTimers(300) === 0 && d.log.length === 0 ? 'ok' : 'a late clock fired after destroy';
                })();
                """);
        drain();
        assertTrue(evalBool("__result === 'ok'"), js.eval("js", "String(__result)").asString());
    }

    /** Let the async body run to its end: the harness's microtasks drain when the promise is awaited from Java. */
    private void drain() throws Exception {
        js.eval("js", "var __result = null; out.then(function (v) { __result = v; }, function (e) { __result = 'threw: ' + e; });");
        for (int k = 0; k < 50 && js.eval("js", "__result === null").asBoolean(); k++)
            js.eval("js", "Promise.resolve()");       // each eval turn drains the microtask queue
    }
}
