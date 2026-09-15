package hue.captains.singapura.js.homing.relgrid.group;

import hue.captains.singapura.js.homing.relgrid.RelGridTestDom;
import hue.captains.singapura.js.homing.relgrid.group.contract.RelGridFenceContract;
import hue.captains.singapura.js.homing.relgrid.group.contract.RelGridGroupContract;
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
 * The group's contract and its JS surface are one list, and every declared
 * option is read — the same pin the grid's contract has, for the same reason:
 * a method the contract does not name is a promise nobody documented, and an
 * option nobody reads is a promise the group does not keep.
 */
class RelGridGroupContractConformanceTest extends JsModuleTestBase {

    private static final String GROUP_DIR = "/homing/js/hue/captains/singapura/js/homing/relgrid/group/";
    private static final String[] GROUP = { "RelGridGroupMintModule.js", "RelGridGroupWalkModule.js", "RelGridGroupModule.js" };

    @BeforeEach
    void setup() {
        js = buildContext();
        js.eval("js", RelGridTestDom.DOM_STUB);
        js.eval("js", RelGridTestDom.STYLES);
        js.eval("js", RelGridTestDom.handles(RelGridGroupStyles.INSTANCE));
        for (String m : RelGridTestDom.PARTY) loadModule(m);
        loadModule(RelGridTestDom.CHANNEL);
        loadModule(RelGridTestDom.PROTOCOL);
        loadModule(RelGridTestDom.SELECTION);
        for (String m : RelGridTestDom.MODULES) loadModule(RelGridTestDom.DIR + m);
        for (String m : GROUP) loadModule(GROUP_DIR + m);
    }

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
    void theGroupSurfaceIsExactlyWhatTheContractDeclares() {
        assertEquals(declared(RelGridGroupContract.class), prototypeMethods(RelGridGroupContract.JS_CLASS_NAME),
                "RelGridGroup's public methods and RelGridGroupContract must be one list");
    }

    @Test
    void theFenceContractNamesItsMethods() {
        assertEquals(declared(RelGridFenceContract.class),
                new TreeSet<>(Arrays.asList(RelGridFenceContract.METHOD_NAMES)),
                "METHOD_NAMES must name exactly the fence contract's methods");
        var optional = new TreeSet<>(Arrays.asList(RelGridFenceContract.OPTIONAL_METHODS));
        assertEquals(new TreeSet<>(List.of("onFolded")), optional, "the one optional method is the fold lifecycle");
        assertEquals(true, declared(RelGridFenceContract.class).containsAll(optional), "an optional method is still a declared one");
    }

    @Test
    void everyDeclaredOptionIsReadByTheConstructor() {
        String src = js.eval("js", "String(RelGridGroup.prototype.constructor)").asString();
        var unread = new ArrayList<String>();
        for (String opt : RelGridGroupContract.CALLBACK_OPTION_NAMES)
            if (!src.contains("opts." + opt)) unread.add(opt);
        assertEquals(List.of(), unread, "a declared option nobody reads is a promise the group does not keep");
    }
}
