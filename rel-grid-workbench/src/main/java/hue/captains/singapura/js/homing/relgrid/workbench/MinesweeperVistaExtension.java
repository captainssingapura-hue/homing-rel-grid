package hue.captains.singapura.js.homing.relgrid.workbench;

import hue.captains.singapura.js.homing.component.taxonomy.ConcreteComponent;
import hue.captains.singapura.js.homing.design.semantic.DesignExtension;

import java.util.List;

/** Minesweeper's Vista design of the grid's frame, as the workbench adds it: a concrete leaf for every leaf of the branch. */
public record MinesweeperVistaExtension() implements DesignExtension {

    @Override public List<ConcreteComponent<?, ?, ?>> concretes() {
        return List.of(MinesweeperVista.Edge.INSTANCE, MinesweeperVista.Lattice.INSTANCE, MinesweeperVista.Cursor.INSTANCE,
                       MinesweeperVista.HeldCursor.INSTANCE, MinesweeperVista.LentCursor.INSTANCE, MinesweeperVista.Selection.INSTANCE,
                       MinesweeperVista.Header.INSTANCE, MinesweeperVista.Gutter.INSTANCE, MinesweeperVista.Anchor.INSTANCE,
                       MinesweeperVista.Veil.INSTANCE, MinesweeperVista.Panel.INSTANCE, MinesweeperVista.Merged.INSTANCE,
                       MinesweeperVista.Guide.INSTANCE, MinesweeperVista.CopiedArea.INSTANCE, MinesweeperVista.CopiedBorder.INSTANCE);
    }
}
