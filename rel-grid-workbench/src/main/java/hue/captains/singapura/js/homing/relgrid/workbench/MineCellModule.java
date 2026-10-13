package hue.captains.singapura.js.homing.relgrid.workbench;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;

import java.util.List;

/**
 * A tile of the Minesweeper board, as a cell the grid places: {@code new MineCell({ branch })} - it shows what the game
 * tells it, in its own design, and has no behaviour towards the grid.
 */
public record MineCellModule() implements DomModule<MineCellModule> {

    public record MineCell() implements Exportable._Class<MineCellModule> {}

    public static final MineCellModule INSTANCE = new MineCellModule();

    @Override
    public ImportsFor<MineCellModule> imports() {
        return ImportsFor.<MineCellModule>builder()
                .add(new ModuleImports<>(List.of(new MinesweeperStyles.ms_tile(), new MinesweeperStyles.ms_hidden(), new MinesweeperStyles.ms_flagged(),
                        new MinesweeperStyles.ms_revealed(), new MinesweeperStyles.ms_mine(), new MinesweeperStyles.ms_boom(), new MinesweeperStyles.ms_wrong(),
                        new MinesweeperStyles.ms_n1(), new MinesweeperStyles.ms_n2(), new MinesweeperStyles.ms_n3(), new MinesweeperStyles.ms_n4(),
                        new MinesweeperStyles.ms_n5(), new MinesweeperStyles.ms_n6(), new MinesweeperStyles.ms_n7(), new MinesweeperStyles.ms_n8()),
                        MinesweeperStyles.INSTANCE))
                .build();
    }

    @Override public ExportsOf<MineCellModule> exports() { return new ExportsOf<>(INSTANCE, List.of(new MineCell())); }
}
