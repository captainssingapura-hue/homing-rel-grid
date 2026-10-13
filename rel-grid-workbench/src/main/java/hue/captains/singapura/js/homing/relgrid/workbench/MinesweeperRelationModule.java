package hue.captains.singapura.js.homing.relgrid.workbench;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;

import java.util.List;

/**
 * The Minesweeper board as a relation: {@code new MinesweeperRelation({ branch, rows, cols })} - a row per board row,
 * a column per board column, a {@code MineCell} per position, one View that never moves. Imports the tile and nothing
 * of the grid.
 */
public record MinesweeperRelationModule() implements DomModule<MinesweeperRelationModule> {

    public record MinesweeperRelation() implements Exportable._Class<MinesweeperRelationModule> {}

    public static final MinesweeperRelationModule INSTANCE = new MinesweeperRelationModule();

    @Override
    public ImportsFor<MinesweeperRelationModule> imports() {
        return ImportsFor.<MinesweeperRelationModule>builder()
                .add(new ModuleImports<>(List.of(new MineCellModule.MineCell()), MineCellModule.INSTANCE))
                .build();
    }

    @Override public ExportsOf<MinesweeperRelationModule> exports() { return new ExportsOf<>(INSTANCE, List.of(new MinesweeperRelation())); }
}
