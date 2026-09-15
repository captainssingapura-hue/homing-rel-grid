package hue.captains.singapura.js.homing.jsonkit;

import hue.captains.singapura.js.homing.jsonkit.contract.JsonTreeViewContract;
import hue.captains.singapura.js.homing.reltree.contract.RelTreeCellContract;
import hue.captains.singapura.js.homing.reltree.contract.TreeRelationContract;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The contracts, bound to the code: the view's surface is exactly its contract; the cell and the document answer the tree's. */
class JsonKitContractConformanceTest extends JsonKitTestBase {

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
    void theViewsSurfaceIsExactlyWhatTheContractDeclares() {
        assertEquals(declared(JsonTreeViewContract.class), prototypeMethods(JsonTreeViewContract.JS_CLASS_NAME),
                "JsonTreeView's public methods and JsonTreeViewContract must be one list");
    }

    @Test
    void theCellAnswersTheWholeTreeCellContract() {
        var methods = prototypeMethods("JsonNodeCell");
        for (String m : RelTreeCellContract.ALL_METHODS)
            assertTrue(methods.contains(m), "JsonNodeCell must answer " + m);
    }

    @Test
    void theDocumentAnswersTheTreeRelationContractAndTheChannel() {
        act("var D = createJsonDocument(SAMPLE, { branch: testBranch() });");
        for (String m : TreeRelationContract.METHOD_NAMES)
            assertTrue(evalBool("typeof D." + m + " === 'function'"), "a JSON document must answer " + m);
        assertTrue(evalBool("typeof D.answer === 'function' && typeof D.set === 'function'"));
    }
}
