package hue.captains.singapura.js.homing.relgrid;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;

import java.util.List;

/**
 * RFC 0050 · Episode 2 — {@code RelGridCells}: the grid's side of the CELLS branch.
 * A registry from identity to the cell the domain handed over and the element
 * that cell owns — nothing more. It asks the relation's cell manager
 * <b>once per identity</b>, asks the cell for {@code cellElement()} once, and
 * from then on only places and detaches that element in the grid's slots. It
 * mints nothing for a cell, never passes a value in, never reads one out, and
 * never disposes: a cell outlives the grid that placed it.
 */
public record RelGridCellsModule() implements DomModule<RelGridCellsModule> {

    public record RelGridCells() implements Exportable._Constant<RelGridCellsModule> {}

    public static final RelGridCellsModule INSTANCE = new RelGridCellsModule();

    @Override public ImportsFor<RelGridCellsModule> imports() { return ImportsFor.noImports(); }

    @Override public ExportsOf<RelGridCellsModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new RelGridCells()));
    }
}
