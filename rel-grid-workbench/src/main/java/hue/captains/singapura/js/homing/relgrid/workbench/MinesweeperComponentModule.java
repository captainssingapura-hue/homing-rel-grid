package hue.captains.singapura.js.homing.relgrid.workbench;

import hue.captains.singapura.js.homing.component.taxonomy.Component;
import hue.captains.singapura.js.homing.component.taxonomy.ComponentModule;

import java.util.List;

/**
 * Minesweeper's components: none of its own. Its board is the grid, its frame the grid's, and its look of that frame a
 * local style ({@link MinesweeperVista}), which its base module registers - a module that adds a look, and nothing to
 * the taxonomy.
 */
public record MinesweeperComponentModule() implements ComponentModule {

    public static final MinesweeperComponentModule INSTANCE = new MinesweeperComponentModule();

    @Override public List<? extends Component<?>> components() { return List.of(); }
}
