package hue.captains.singapura.js.homing.relgrid.workbench;

import hue.captains.singapura.js.homing.relgrid.RelGridTestDom;
import hue.captains.singapura.js.homing.ssjs.test.JsModuleTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The secretaries of the parties the benches' widgets share - the JSON text and the Han article;
 * the dishes' is DishPolicyTest's - each stepped by hand, pure, as the party steps it: what each
 * says, to whom, and what goes to the steward.
 */
class BenchSecretariesTest extends JsModuleTestBase {

    private static final String BENCH_DIR = RelGridTestDom.DIR + "workbench/";

    private static final String STEPPER = """
            function stepper(secretary) {
                var s = secretary.initial;
                return {
                    step: function (from, m) { var r = secretary.behavior(s, { from: from, message: m }); s = r.newState; return r.actions; },
                    state: function () { return s; }
                };
            }
            """;

    @BeforeEach
    void setup() {
        js = buildContext();
        loadModule(BENCH_DIR + "JsonDocStoreModule.js");
        loadModule(BENCH_DIR + "JsonTextSecretaryModule.js");
        loadModule(BENCH_DIR + "HanStoreModule.js");
        loadModule(BENCH_DIR + "HanArticleSecretaryModule.js");
        js.eval("js", STEPPER);
    }

    private boolean evalBool(String expr) { return js.eval("js", expr).asBoolean(); }

    @Test
    void theJsonTextIsTheSampleThenWhatIsTyped_saidToEveryMember() {
        assertTrue(evalBool("""
                (() => {
                    var p = stepper(JsonTextSecretary);
                    var a = p.step('display', { kind: 'CurrentRequested' });          // answered alone, with the sample
                    if (a.length !== 1 || a[0].kind !== 'SendToMember' || a[0].to !== 'display') return false;
                    if (a[0].message.text !== JsonDocStore.SAMPLE || a[0].message.revision !== 0) return false;
                    a = p.step('input', { kind: 'SetText', text: '{"a":1}' });           // to every member, the one who typed too
                    if (a.length !== 1 || a[0].kind !== 'BroadcastToMembers' || a[0].message.text !== '{"a":1}' || a[0].message.revision !== 1) return false;
                    if (p.step('input', { kind: 'Text', text: 'x', revision: 9 }).length !== 0) return false;   // the party's word, never a member's
                    return p.state().recentUnknown.length === 1 && p.state().lastSetBy === 'input' && p.state().text === '{"a":1}';
                })()"""), "the JSON text opens on the sample, and every set is said to every member");
    }

    @Test
    void theHanArticleIsAskedOfTheStewardOnce_thenKeptAfterEveryChange() {
        assertTrue(evalBool("""
                (() => {
                    var p = stepper(HanArticleSecretary);
                    var a = p.step('display', { kind: 'CurrentRequested' });          // not loaded: the steward is asked
                    if (a.length !== 1 || a[0].kind !== 'SendToSteward' || a[0].message.kind !== 'Load') return false;
                    if (p.step('editor', { kind: 'CurrentRequested' }).length !== 0) return false;   // asked once
                    a = p.step('steward', { kind: 'Loaded', text: '床前明月光', found: true });
                    if (a.length !== 1 || a[0].kind !== 'BroadcastToMembers' || a[0].message.text !== '床前明月光') return false;
                    a = p.step('editor', { kind: 'SetText', text: '床前明月光，' });
                    if (a.length !== 2 || a[1].kind !== 'SendToSteward' || a[1].message.kind !== 'Save' || a[1].message.text !== '床前明月光，') return false;
                    if (p.step('editor', { kind: 'SetText', text: '床前明月光，' }).length !== 0) return false;   // the same again: nothing
                    a = p.step('editor', { kind: 'Reset' });
                    if (a[0].message.text !== HanStore.SEED || a[0].message.revision !== 2) return false;
                    return p.step('display', { kind: 'CurrentRequested' })[0].to === 'display';
                })()"""), "the article is loaded once, said to every member, and handed to the steward after every change");
    }

    @Test
    void withNoStewardTheArticleIsThePoem() {
        assertTrue(evalBool("""
                (() => {
                    var p = stepper(HanArticleSecretary);
                    p.step('display', { kind: 'CurrentRequested' });
                    var a = p.step('unrouted', { kind: 'Load' });                      // no steward took it: the poem
                    if (a.length !== 1 || a[0].message.text !== HanStore.SEED) return false;
                    var b = p.step('unrouted', { kind: 'Save', text: 'x' });           // nothing to keep it: dropped
                    var c = p.step('steward', { kind: 'Loaded', text: 'late', found: true });   // too late: kept as it is
                    return b.length === 0 && c.length === 0 && p.state().text === HanStore.SEED && !p.state().loading;
                })()"""), "a page with no steward reads the poem, and a late load does not overwrite the article");
    }
}
