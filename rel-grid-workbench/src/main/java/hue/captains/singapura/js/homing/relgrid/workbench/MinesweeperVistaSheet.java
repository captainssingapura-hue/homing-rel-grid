package hue.captains.singapura.js.homing.relgrid.workbench;

import hue.captains.singapura.js.homing.core.DerivedCss;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.SelfRenderedCss;
import hue.captains.singapura.js.homing.ui.look.HouseDesigns;

import java.util.List;

/**
 * Minesweeper's Vista board as a sheet: a rule for every class its concrete leaves wear, in the theme a page is worn
 * in - its edge tinted by the palette. A derived sheet: it renders itself per theme, and is fetched again on a switch.
 */
public record MinesweeperVistaSheet() implements DerivedCss<MinesweeperVistaSheet>, SelfRenderedCss {

    public static final MinesweeperVistaSheet INSTANCE = new MinesweeperVistaSheet();

    /** What the design's object imports the sheet by, so that it loads: no element wears it. */
    public record minesweeper_vista_sheet() implements Exportable._Constant<MinesweeperVistaSheet> {}

    @Override public ExportsOf<MinesweeperVistaSheet> exports() { return new ExportsOf<>(INSTANCE, List.of(new minesweeper_vista_sheet())); }

    @Override public String css(String themeSlug) { return HouseDesigns.INSTANCE.sheet(MinesweeperVista.INSTANCE, themeSlug); }
}
