package hue.captains.singapura.js.homing.jsonkit;

import hue.captains.singapura.js.homing.reltree.RelTreeTestDom;
import hue.captains.singapura.js.homing.ssjs.test.JsModuleTestBase;
import org.junit.jupiter.api.BeforeEach;

/** A kit test: the tree's world loaded, then the kit's modules, then the fixture. */
abstract class JsonKitTestBase extends JsModuleTestBase {

    @BeforeEach
    void setup() {
        js = buildContext();
        js.eval("js", RelTreeTestDom.DOM_STUB);
        js.eval("js", RelTreeTestDom.DOM_PARSER);
        js.eval("js", RelTreeTestDom.STYLES);
        js.eval("js", RelTreeTestDom.SVGS);
        js.eval("js", JsonKitTestDom.STYLES);
        for (String m : RelTreeTestDom.PARTY) loadModule(m);
        loadModule(RelTreeTestDom.CHANNEL);
        loadModule(RelTreeTestDom.PROTOCOL);
        for (String m : RelTreeTestDom.MODULES) loadModule(RelTreeTestDom.DIR + m);
        for (String m : JsonKitTestDom.MODULES) loadModule(JsonKitTestDom.DIR + m);
        js.eval("js", JsonKitTestDom.FIXTURE);
    }

    boolean evalBool(String expr) { return js.eval("js", expr).asBoolean(); }
    String evalString(String expr) { return js.eval("js", expr).asString(); }
    void act(String code) { js.eval("js", code); }
}
