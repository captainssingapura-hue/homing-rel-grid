package hue.captains.singapura.js.homing.relgrid.workbench;

import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.workspace.groups.core.models.ArrangedWidget;
import hue.captains.singapura.js.homing.workspace.groups.core.models.Arrangement;
import hue.captains.singapura.js.homing.workspace.groups.core.models.SplitGrid;
import hue.captains.singapura.js.homing.workspace.groups.core.models.WorkspaceArrangements;
import hue.captains.singapura.js.homing.workspace.widgets.NoParams;
import hue.captains.singapura.js.homing.workspace.widgets.WidgetDeclaration;
import hue.captains.singapura.js.homing.workspace.widgets.WidgetQuery;
import hue.captains.singapura.js.homing.workspace.widgets.WorkspaceDeclaration;

import java.util.List;

/**
 * The Minesweeper bench: the grid as a cell-widget substrate, not a table - a board of tiles that are the game's cells,
 * in a domain's own design of the frame (Vista's). One widget, the game, filling the bench.
 */
public final class MinesweeperBench {

    private MinesweeperBench() {}

    /** The game, declared: {@code minesweeper}, no params, nothing shared. */
    public record Game() implements WidgetDeclaration<NoParams> {
        public static final Game INSTANCE = new Game();
        @Override public String kind() { return "minesweeper"; }
        @Override public String title() { return "Minesweeper"; }
        @Override public Class<NoParams> paramsType() { return NoParams.class; }
        @Override public WidgetQuery<NoParams> query() { return new NoParams.Query(); }
        @Override public ModuleImports<?> constructs() {
            return new ModuleImports<>(List.of(new MinesweeperGameModule.MinesweeperGame()), MinesweeperGameModule.INSTANCE);
        }
    }

    /** The bench, a workspace: {@code minesweeper}. */
    public record Workspace() implements WorkspaceDeclaration {
        public static final Workspace INSTANCE = new Workspace();
        @Override public String name() { return "minesweeper"; }
        @Override public List<WidgetDeclaration<?>> kinds() { return List.of(Game.INSTANCE); }
    }

    /** The first time: the game, filling the bench. */
    public static final WorkspaceArrangements<Workspace> ARRANGED = WorkspaceArrangements.of(Workspace.INSTANCE,
            Arrangement.of(Workspace.INSTANCE, SplitGrid.of(SplitGrid.region("game", "game")),
                    ArrangedWidget.of("game", Game.INSTANCE.kind())));
}
