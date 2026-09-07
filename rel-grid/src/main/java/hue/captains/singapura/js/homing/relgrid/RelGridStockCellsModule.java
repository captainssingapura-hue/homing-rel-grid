package hue.captains.singapura.js.homing.relgrid;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;

import java.util.List;

/**
 * RFC 0050 · Episode 2 — stock cells a domain may build its cell manager from.
 * These are <b>domain-side</b> code that happens to ship with the grid: a
 * {@code RelGridTextCell} holds what it shows and is told to change by whoever
 * owns it ({@code set}), never by the grid. The grid's facade does not import
 * this module; a relation does.
 */
public record RelGridStockCellsModule() implements DomModule<RelGridStockCellsModule> {

    public record RelGridTextCell() implements Exportable._Constant<RelGridStockCellsModule> {}

    public static final RelGridStockCellsModule INSTANCE = new RelGridStockCellsModule();

    @Override public ImportsFor<RelGridStockCellsModule> imports() { return ImportsFor.noImports(); }

    @Override public ExportsOf<RelGridStockCellsModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new RelGridTextCell()));
    }
}
