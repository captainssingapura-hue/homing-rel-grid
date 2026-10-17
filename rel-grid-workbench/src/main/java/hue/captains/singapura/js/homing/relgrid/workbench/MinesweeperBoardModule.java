package hue.captains.singapura.js.homing.relgrid.workbench;

import hue.captains.singapura.js.homing.core.EsModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;

import java.util.List;

/**
 * The Minesweeper board, the game alone (RFC 0050, appendix Keyboard Minesweeper): {@code new MinesweeperBoard({ rows,
 * cols, mines, seed })} - its mines laid at the first reveal, never under it nor beside it; reveal, flag, chord and
 * restart, each answering the tiles it changed. Pure: it touches nothing on a page.
 */
public record MinesweeperBoardModule() implements EsModule<MinesweeperBoardModule> {

    public record MinesweeperBoard() implements Exportable._Class<MinesweeperBoardModule> {}

    public static final MinesweeperBoardModule INSTANCE = new MinesweeperBoardModule();

    @Override public ImportsFor<MinesweeperBoardModule> imports() { return ImportsFor.noImports(); }

    @Override public ExportsOf<MinesweeperBoardModule> exports() { return new ExportsOf<>(INSTANCE, List.of(new MinesweeperBoard())); }
}
