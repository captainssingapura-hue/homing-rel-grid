package hue.captains.singapura.js.homing.relgrid.house;

import hue.captains.singapura.js.homing.core.DerivedCss;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.SelfRenderedCss;
import hue.captains.singapura.js.homing.ui.look.HouseDesigns;

import java.util.List;

/**
 * The house's document table as a sheet: a rule for every class its concrete leaves wear, in the theme a page is worn
 * in - its rules the theme's own for a document's table. A derived sheet (RFC 0064, D25): it renders itself per theme,
 * and the theme's owner fetches it again on a switch.
 */
public record HouseProseSheet() implements DerivedCss<HouseProseSheet>, SelfRenderedCss {

    public static final HouseProseSheet INSTANCE = new HouseProseSheet();

    /** What the design's object imports the sheet by, so that it loads: no element wears it. */
    public record house_prose_sheet() implements Exportable._Constant<HouseProseSheet> {}

    @Override public ExportsOf<HouseProseSheet> exports() { return new ExportsOf<>(INSTANCE, List.of(new house_prose_sheet())); }

    @Override public String css(String themeSlug) { return HouseDesigns.INSTANCE.sheet(HouseProse.INSTANCE, themeSlug); }
}
