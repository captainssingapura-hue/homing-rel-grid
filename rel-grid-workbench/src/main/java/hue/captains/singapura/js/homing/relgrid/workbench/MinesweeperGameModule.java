package hue.captains.singapura.js.homing.relgrid.workbench;

import hue.captains.singapura.js.homing.component.keyboard.Key;
import hue.captains.singapura.js.homing.component.keyboard.KeyBinding;
import hue.captains.singapura.js.homing.component.keyboard.NeedKeyboard;
import hue.captains.singapura.js.homing.conformance.rules.ConformanceDebt;
import hue.captains.singapura.js.homing.conformance.rules.WidgetDrawsOnlyComponentsRule;
import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.relgrid.RelGridModule;
import hue.captains.singapura.js.homing.workspace.widgets.SelfContainedWidget;

import java.util.List;

/**
 * Minesweeper on the grid, in Vista's design of its frame: {@code new MinesweeperGame(container, params)} - the grid
 * the substrate, the board the game, the tiles the game's cells; the moves the game's, taken on the board's host on
 * their way to the grid.
 */
@ConformanceDebt(rule = WidgetDrawsOnlyComponentsRule.class, findings = 9,
        reason = "It makes its hint, band and board host of its own and writes their text: it waits for the designed containers, the widget slot and the content components to draw only components.")
public record MinesweeperGameModule() implements DomModule<MinesweeperGameModule> {

    public record MinesweeperGame() implements SelfContainedWidget<MinesweeperGameModule>, NeedKeyboard {
        @Override public String summary() { return "Minesweeper on the grid, in Vista's design of its frame: the whole game by the keys, or the pointer."; }
        @Override public List<KeyBinding> keys() {
            return List.of(KeyBinding.of(Key.ENTER, "reveal the tiles selected"),
                           KeyBinding.of(Key.SPACE, "reveal the tiles selected"),
                           KeyBinding.of(Key.F, "flag the tiles selected, or take the flags off"),
                           KeyBinding.of(Key.C, "chord: reveal round a number whose flags are all down"),
                           KeyBinding.of(Key.R, "restart the board"),
                           KeyBinding.of(Key.ESCAPE, "the keys given back, when the grid did not take it"));
        }
    }

    public static final MinesweeperGameModule INSTANCE = new MinesweeperGameModule();

    @Override
    public ImportsFor<MinesweeperGameModule> imports() {
        return ImportsFor.<MinesweeperGameModule>builder()
                .add(new ModuleImports<>(List.of(new BenchWidgetModule.BenchWidget()), BenchWidgetModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new RelGridModule.RelGrid()), RelGridModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new MinesweeperVistaModule.MinesweeperVista()), MinesweeperVistaModule.INSTANCE))   // the design its grid is made with
                .add(new ModuleImports<>(List.of(new MinesweeperBoardModule.MinesweeperBoard()), MinesweeperBoardModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new MinesweeperRelationModule.MinesweeperRelation()), MinesweeperRelationModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new MinesweeperStyles.ms_band(), new MinesweeperStyles.ms_board()), MinesweeperStyles.INSTANCE))
                .add(new ModuleImports<>(List.of(new WorkbenchStyles.wb_hint()), WorkbenchStyles.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<MinesweeperGameModule> exports() { return new ExportsOf<>(INSTANCE, List.of(new MinesweeperGame())); }
}
