package hue.captains.singapura.js.homing.relgrid;

import hue.captains.singapura.js.homing.relgrid.contract.RelGridCellContract;
import hue.captains.singapura.js.homing.relgrid.contract.RelGridContract;
import hue.captains.singapura.js.homing.relgrid.contract.RootRelationContract;
import hue.captains.singapura.js.homing.ssjs.test.JsModuleTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The contracts, bound to the code they describe.
 *
 * <p>Before this existed the three interfaces declared {@code METHOD_NAMES},
 * {@code ALL_METHODS} and {@code CALLBACK_OPTION_NAMES} as though something
 * scanned them — and nothing did. They were prose wearing the costume of a
 * check, and they drifted: two rounds of work added seven public methods and a
 * channel option to the facade without a line of the contract moving.</p>
 *
 * <p>This is the third instance of the same move, after the value-free scan and
 * the protocol's shape test, and it earns its place the same way: it fails on
 * the day a surface and its description disagree, rather than whenever somebody
 * next reads both.</p>
 */
class RelGridContractConformanceTest extends JsModuleTestBase {

    @BeforeEach
    void setup() {
        js = buildContext();
        js.eval("js", RelGridTestDom.DOM_STUB);
        js.eval("js", RelGridTestDom.STYLES);
        for (String m : RelGridTestDom.PARTY) loadModule(m);
        loadModule(RelGridTestDom.SELECTION);
        loadModule(RelGridTestDom.PROTOCOL);
        for (String m : RelGridTestDom.MODULES) loadModule(RelGridTestDom.DIR + m);
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
        assertEquals(declared(RelGridContract.class), prototypeMethods(RelGridContract.JS_CLASS_NAME),
                "RelGrid's public methods and RelGridContract must be one list");
    }

    @Test
    void theStockCellAnswersTheWholeCellContract() {
        var contract = declared(RelGridCellContract.class);
        contract.removeAll(Arrays.asList(RelGridCellContract.OPTIONAL_METHODS));   // absent means the default
        var actual = prototypeMethods("RelGridTextCell");
        var missing = new ArrayList<String>();
        for (String m : contract) if (!actual.contains(m)) missing.add(m);
        assertEquals(List.of(), missing,
                "the stock cell must answer every method RelGridCellContract declares, the optional ones aside");
    }

    @Test
    void theCellContractsOwnListsAgreeWithItself() {
        assertEquals(declared(RelGridCellContract.class),
                new TreeSet<>(Arrays.asList(RelGridCellContract.ALL_METHODS)),
                "ALL_METHODS must name exactly the contract's methods");
        // All of the handover, or none: the arrays say so and the grid enforces it.
        assertEquals(new TreeSet<>(List.of("mayTakeControl", "editorElement", "takeControl")),
                new TreeSet<>(Arrays.asList(RelGridCellContract.CONTROL_METHODS)),
                "CONTROL_METHODS is the two-stage handover and the editor between, and nothing else");
        assertEquals(new TreeSet<>(List.of("cellElement")),
                new TreeSet<>(Arrays.asList(RelGridCellContract.REQUIRED_METHODS)),
                "the one thing every cell must answer is its element");
    }

    @Test
    void theRelationsMandatoryThreeAreWhatTheGridDemands() {
        var mandatory = new TreeSet<>(Arrays.asList(RootRelationContract.METHOD_NAMES));
        assertEquals(new TreeSet<>(List.of("cellFor", "columns", "view")), mandatory,
                "law 191: the root relation is these three, and they are never on the channel");
        // The grid's own guard must demand exactly them, and nothing more.
        String guard = js.eval("js",
                "String(RelGrid.prototype.constructor).match(/relation must expose[^\"]*/)[0]").asString();
        var named = new ArrayList<String>();
        for (String m : mandatory) if (guard.contains(m)) named.add(m);
        assertEquals(new ArrayList<>(mandatory), named,
                "the constructor's guard names every mandatory method");
        assertEquals(new TreeSet<>(List.of("readOnlyColumns")),
                new TreeSet<>(Arrays.asList(RootRelationContract.OPTIONAL_METHOD_NAMES)),
                "the one optional declaration is the column constraint");
    }

    @Test
    void everyDeclaredOptionIsReadByTheConstructor() {
        String src = js.eval("js", "String(RelGrid.prototype.constructor)").asString();
        var unread = new ArrayList<String>();
        for (String opt : RelGridContract.CALLBACK_OPTION_NAMES)
            if (!src.contains("opts." + opt)) unread.add(opt);
        if (!src.contains("opts." + RelGridContract.CHANNEL_OPTION_NAME))
            unread.add(RelGridContract.CHANNEL_OPTION_NAME);
        if (!src.contains("opts." + RelGridContract.CLIPBOARD_OPTION_NAME))
            unread.add(RelGridContract.CLIPBOARD_OPTION_NAME);
        assertEquals(List.of(), unread,
                "a declared option nobody reads is a promise the grid does not keep");
    }
}
