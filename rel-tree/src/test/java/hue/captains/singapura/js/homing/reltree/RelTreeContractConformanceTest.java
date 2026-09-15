package hue.captains.singapura.js.homing.reltree;

import hue.captains.singapura.js.homing.reltree.contract.RelTreeCellContract;
import hue.captains.singapura.js.homing.reltree.contract.RelTreeContract;
import hue.captains.singapura.js.homing.reltree.contract.TreeRelationContract;
import hue.captains.singapura.js.homing.ssjs.test.JsModuleTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The contracts, bound to the code they describe: a surface and its description cannot drift. */
class RelTreeContractConformanceTest extends JsModuleTestBase {

    @BeforeEach
    void setup() {
        js = buildContext();
        js.eval("js", RelTreeTestDom.DOM_STUB);
        js.eval("js", RelTreeTestDom.STYLES);
        for (String m : RelTreeTestDom.PARTY) loadModule(m);
        loadModule(RelTreeTestDom.CHANNEL);
        loadModule(RelTreeTestDom.PROTOCOL);
        for (String m : RelTreeTestDom.MODULES) loadModule(RelTreeTestDom.DIR + m);
        js.eval("js", RelTreeTestDom.FIXTURE);
    }

    /** Every method a JS class actually declares on its prototype. */
    private TreeSet<String> prototypeMethods(String className) {
        String names = js.eval("js",
                "Object.getOwnPropertyNames(" + className + ".prototype)"
              + ".filter(function (n) { return n !== 'constructor' && n.charAt(0) !== '_'; })"
              + ".sort().join(',')").asString();
        var out = new TreeSet<String>();
        if (!names.isEmpty()) out.addAll(Arrays.asList(names.split(",")));
        return out;
    }

    private static TreeSet<String> declared(Class<?> contract) {
        var out = new TreeSet<String>();
        for (Method m : contract.getDeclaredMethods()) out.add(m.getName());
        return out;
    }

    @Test
    void theFacadeSurfaceIsExactlyWhatTheContractDeclares() {
        assertEquals(declared(RelTreeContract.class), prototypeMethods(RelTreeContract.JS_CLASS_NAME),
                "RelTree's public methods and RelTreeContract must be one list");
    }

    @Test
    void theStockCellAnswersTheWholeCellContract() {
        var contract = declared(RelTreeCellContract.class);
        contract.removeAll(Arrays.asList(RelTreeCellContract.OPTIONAL_METHODS));
        var actual = prototypeMethods("RelTreeTextCell");
        var missing = new ArrayList<String>();
        for (String m : contract) if (!actual.contains(m)) missing.add(m);
        assertEquals(List.of(), missing, "the stock cell must answer every method RelTreeCellContract declares, the optional ones aside");
        assertEquals(declared(RelTreeCellContract.class), new TreeSet<>(Arrays.asList(RelTreeCellContract.ALL_METHODS)),
                "the cell contract's own list must agree with its methods");
    }

    @Test
    void theRelationContractNamesWhatTheFacadeAsksFor() {
        assertEquals(declared(TreeRelationContract.class), new TreeSet<>(Arrays.asList(TreeRelationContract.METHOD_NAMES)),
                "the relation contract's METHOD_NAMES and its methods must be one list");
        // The facade refuses a relation missing either, by name.
        assertTrue(js.eval("js", """
                (() => {
                    var b = testBranch(), c = makeEl('div');
                    try { new RelTree({ container: c, branch: b, relation: { view: function () { return []; } } }); return false; }
                    catch (e) { return /view\\(\\) and cellFor\\(key\\)/.test(String(e)); }
                })()""").asBoolean(), "a relation without cellFor is refused by name");
    }

    @Test
    void theCallbackOptionsAreTheOnesTheFacadeReads() {
        String source = js.eval("js", "RelTree.toString()").asString();
        for (String cb : RelTreeContract.CALLBACK_OPTION_NAMES)
            assertTrue(source.contains("opts." + cb), "the facade reads opts." + cb);
        assertTrue(source.contains("opts." + RelTreeContract.CHANNEL_OPTION_NAME), "the facade reads the channel option");
    }
}
