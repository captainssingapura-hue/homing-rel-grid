package hue.captains.singapura.js.homing.relgrid.workbench;

import hue.captains.singapura.js.homing.component.taxonomy.ComponentModule;
import hue.captains.singapura.js.homing.design.local.LocalStyle;
import hue.captains.singapura.js.homing.design.styling.BaseModule;
import hue.captains.singapura.js.homing.design.styling.StyleBundle;
import hue.captains.singapura.js.homing.ui.styles.Base;
import hue.captains.singapura.js.homing.ui.styles.BaseScheme;

import java.util.List;

/**
 * Minesweeper as the bench registers it (RFC 0067, appendix Local Styles): no component and no look in the base, and its
 * Vista local style of the grid's frame - the frame's module, registered beside it ({@code GridFrameBaseModule}), brings
 * the branch. A site that serves the board hands both to the house's styling, so the Vista is drawn in every style.
 */
public record MinesweeperBaseModule() implements BaseModule<BaseScheme, Base> {

    public static final MinesweeperBaseModule INSTANCE = new MinesweeperBaseModule();

    @Override public Base style() { return Base.INSTANCE; }

    @Override public ComponentModule components() { return MinesweeperComponentModule.INSTANCE; }

    @Override public List<StyleBundle<BaseScheme, Base>> bundles() { return List.of(); }

    @Override public List<? extends LocalStyle<?, ?>> localStyles() { return List.of(MinesweeperVista.INSTANCE); }
}
