package hue.captains.singapura.js.homing.relgrid.workbench;

import hue.captains.singapura.js.homing.design.styling.StyledSheet;
import hue.captains.singapura.js.homing.design.styling.Styling;
import hue.captains.singapura.js.homing.core.DerivedCss;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;

import java.util.List;

/**
 * Minesweeper's Vista board as a sheet: a rule for every class its concrete leaves wear, in the style a page is worn in
 * - its edge tinted by the palette. A derived sheet that varies with the style (RFC 0067, appendix Two Classes per Leaf):
 * the same in every palette, its palettable colours the palette's variables, so a switch of palette repaints it and only
 * a switch of style fetches it again.
 */
public record MinesweeperVistaSheet() implements DerivedCss<MinesweeperVistaSheet>, StyledSheet {

    public static final MinesweeperVistaSheet INSTANCE = new MinesweeperVistaSheet();

    /** What the local style's object imports the sheet by, so that it loads: no element wears it. */
    public record minesweeper_vista_sheet() implements Exportable._Constant<MinesweeperVistaSheet> {}

    @Override public ExportsOf<MinesweeperVistaSheet> exports() { return new ExportsOf<>(INSTANCE, List.of(new minesweeper_vista_sheet())); }

    @Override public VariesWith variesWith() { return VariesWith.STYLE; }

    @Override public String fallback(Styling styling) { return styling.defaultStyle().token(); }

    @Override public String css(Styling styling, String style) { return styling.localStyleSheet(MinesweeperVista.INSTANCE, style); }
}
