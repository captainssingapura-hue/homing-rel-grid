package hue.captains.singapura.js.homing.relgrid.house;

import hue.captains.singapura.js.homing.component.taxonomy.ComponentModule;
import hue.captains.singapura.js.homing.design.local.LocalStyle;
import hue.captains.singapura.js.homing.design.styling.BaseModule;
import hue.captains.singapura.js.homing.design.styling.StyleBundle;
import hue.captains.singapura.js.homing.relgrid.frame.GridFrameComponentModule;
import hue.captains.singapura.js.homing.ui.styles.Base;
import hue.captains.singapura.js.homing.ui.styles.BaseScheme;

import java.util.List;

/**
 * The grid's frame as an instance of the house registers it (RFC 0067, D51): a base module of the frame's component
 * module, which bundles no look - its leaves are virtual, drawn by a local style, never by a style - and the house's two
 * local styles of it, the grid's and a document table's (RFC 0067, appendix Local Styles). A site with the grid hands it
 * to the house's styling ({@code HouseStyles.INSTANCE.styling(List.of(GridFrameBaseModule.INSTANCE))}), so the frame is in
 * its taxonomy and its local styles are drawn in every style it serves.
 */
public record GridFrameBaseModule() implements BaseModule<BaseScheme, Base> {

    public static final GridFrameBaseModule INSTANCE = new GridFrameBaseModule();

    @Override public Base style() { return Base.INSTANCE; }

    @Override public ComponentModule components() { return GridFrameComponentModule.INSTANCE; }

    @Override public List<StyleBundle<BaseScheme, Base>> bundles() { return List.of(); }

    @Override public List<? extends LocalStyle<?, ?>> localStyles() { return List.of(HouseGrid.INSTANCE, HouseProse.INSTANCE); }
}
