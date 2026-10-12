package hue.captains.singapura.js.homing.relgrid.house;

import hue.captains.singapura.js.homing.core.DerivedCss;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.SelfRenderedCss;
import hue.captains.singapura.js.homing.ui.look.HouseDesigns;

import java.util.List;

/**
 * The house's grid as a sheet: a rule for every class its concrete leaves wear, in the theme a page is worn in. A derived
 * sheet (RFC 0064, D25): it renders itself per theme, and the theme's owner fetches it again on a switch.
 */
public record HouseGridSheet() implements DerivedCss<HouseGridSheet>, SelfRenderedCss {

    public static final HouseGridSheet INSTANCE = new HouseGridSheet();

    /** What the design's object imports the sheet by, so that it loads: no element wears it. */
    public record house_grid_sheet() implements Exportable._Constant<HouseGridSheet> {}

    @Override public ExportsOf<HouseGridSheet> exports() { return new ExportsOf<>(INSTANCE, List.of(new house_grid_sheet())); }

    @Override public String css(String themeSlug) { return HouseDesigns.INSTANCE.sheet(HouseGrid.INSTANCE, themeSlug); }
}
