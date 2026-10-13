package hue.captains.singapura.js.homing.relgrid.house;

import hue.captains.singapura.js.homing.component.taxonomy.ConcreteComponent;
import hue.captains.singapura.js.homing.design.semantic.DesignExtension;

import java.util.List;

/** The house's design of the grid's frame, as this module adds it: a concrete leaf for every leaf of the branch. */
public record HouseGridExtension() implements DesignExtension {

    @Override public List<ConcreteComponent<?, ?, ?>> concretes() {
        return List.of(HouseGrid.Edge.INSTANCE, HouseGrid.Lattice.INSTANCE, HouseGrid.Cursor.INSTANCE, HouseGrid.HeldCursor.INSTANCE,
                       HouseGrid.LentCursor.INSTANCE, HouseGrid.Selection.INSTANCE, HouseGrid.Header.INSTANCE, HouseGrid.Gutter.INSTANCE,
                       HouseGrid.Anchor.INSTANCE, HouseGrid.Veil.INSTANCE, HouseGrid.Panel.INSTANCE, HouseGrid.Merged.INSTANCE,
                       HouseGrid.Guide.INSTANCE, HouseGrid.CopiedArea.INSTANCE, HouseGrid.CopiedBorder.INSTANCE);
    }
}
