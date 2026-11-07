package hue.captains.singapura.js.homing.relgrid.house;

import hue.captains.singapura.js.homing.design.styling.StyledSheet;
import hue.captains.singapura.js.homing.design.styling.Styling;
import hue.captains.singapura.js.homing.core.DerivedCss;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;

import java.util.List;

/**
 * The house's document table as a sheet: a rule for every class its concrete leaves wear, in the style a page is worn in -
 * its rules the style's own for a document's table. A derived sheet that varies with the style (RFC 0067, appendix Two Classes per Leaf): the same in every
 * palette, its palettable colours the palette's variables, so a switch of palette repaints it and only a switch of style
 * fetches it again.
 */
public record HouseProseSheet() implements DerivedCss<HouseProseSheet>, StyledSheet {

    public static final HouseProseSheet INSTANCE = new HouseProseSheet();

    /** What the local style's object imports the sheet by, so that it loads: no element wears it. */
    public record house_prose_sheet() implements Exportable._Constant<HouseProseSheet> {}

    @Override public ExportsOf<HouseProseSheet> exports() { return new ExportsOf<>(INSTANCE, List.of(new house_prose_sheet())); }

    @Override public VariesWith variesWith() { return VariesWith.STYLE; }

    @Override public String fallback(Styling styling) { return styling.defaultStyle().token(); }

    @Override public String css(Styling styling, String style) { return styling.localStyleSheet(HouseProse.INSTANCE, style); }
}
