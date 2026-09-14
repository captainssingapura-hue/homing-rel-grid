package hue.captains.singapura.js.homing.reltree;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;

import java.util.List;

/**
 * RFC 0050 · Episode 3-ext1 — {@code RelTreeCells}: the registry of the domain's
 * cells by key — asked once per presentation, placed after the caret, detached
 * and forgotten when the key leaves the View, never disposed. The grid's
 * registry with one axis.
 */
public record RelTreeCellsModule() implements DomModule<RelTreeCellsModule> {
    public record RelTreeCells() implements Exportable._Constant<RelTreeCellsModule> {}
    public static final RelTreeCellsModule INSTANCE = new RelTreeCellsModule();
    @Override public ImportsFor<RelTreeCellsModule> imports() { return ImportsFor.noImports(); }
    @Override public ExportsOf<RelTreeCellsModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new RelTreeCells()));
    }
}
