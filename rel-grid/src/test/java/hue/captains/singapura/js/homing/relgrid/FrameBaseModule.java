package hue.captains.singapura.js.homing.relgrid;

import hue.captains.singapura.js.homing.component.taxonomy.ComponentModule;
import hue.captains.singapura.js.homing.design.styling.BaseModule;
import hue.captains.singapura.js.homing.design.styling.StyleBundle;
import hue.captains.singapura.js.homing.relgrid.frame.GridFrameComponentModule;
import hue.captains.singapura.js.homing.ui.styles.Base;
import hue.captains.singapura.js.homing.ui.styles.BaseScheme;

import java.util.List;

/**
 * The grid's frame registered with the house, for the grid's own tests: its component module, no look and no local style
 * - the house's local styles of it are another jar's, which depends on this one. Enough for the frame's constants, which
 * the module generates from the installed instance's taxonomy.
 */
record FrameBaseModule() implements BaseModule<BaseScheme, Base> {

    static final FrameBaseModule INSTANCE = new FrameBaseModule();

    @Override public Base style() { return Base.INSTANCE; }

    @Override public ComponentModule components() { return GridFrameComponentModule.INSTANCE; }

    @Override public List<StyleBundle<BaseScheme, Base>> bundles() { return List.of(); }
}
