package hue.captains.singapura.js.homing.relgrid.house;

import hue.captains.singapura.js.homing.component.taxonomy.ConcreteComponent;
import hue.captains.singapura.js.homing.design.semantic.DesignExtension;

import java.util.List;

/** The house's design of a document's table, as this module adds it: a concrete leaf for every leaf of the frame. */
public record HouseProseExtension() implements DesignExtension {
    @Override public List<ConcreteComponent<?, ?, ?>> concretes() {
        return List.of(HouseProse.Edge.INSTANCE, HouseProse.Lattice.INSTANCE, HouseProse.Cursor.INSTANCE, HouseProse.HeldCursor.INSTANCE,
                       HouseProse.LentCursor.INSTANCE, HouseProse.Selection.INSTANCE, HouseProse.Header.INSTANCE, HouseProse.Gutter.INSTANCE,
                       HouseProse.Anchor.INSTANCE, HouseProse.Veil.INSTANCE, HouseProse.Panel.INSTANCE, HouseProse.Merged.INSTANCE,
                       HouseProse.Guide.INSTANCE);
    }
}
