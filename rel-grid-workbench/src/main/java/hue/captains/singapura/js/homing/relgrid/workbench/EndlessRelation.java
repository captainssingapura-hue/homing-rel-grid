package hue.captains.singapura.js.homing.relgrid.workbench;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.relgrid.RelGridStockCellsModule;

import java.util.List;

/**
 * A root relation over an identity space too large to list — a million rows
 * whose values are a function of the identity — answering a <b>window</b> of
 * them as its View: {@code view()} is W rows from where the window stands,
 * {@code view({ by })} moves it, clamped, and answers nothing at an end. One
 * root: nothing here enumerates, and the relation weighs nothing until a row
 * is asked for.
 *
 * <p>Its retention rule is the endless table's whole domain-side discipline:
 * a row is held while it is within two Views — the one answered last and the
 * one being answered — and freed the moment it is in neither, cells disposed
 * and branch dissolved. Whoever arranges a View does so before asking for the
 * next, so a row in neither is placed nowhere. That bounds what is held at 2W
 * rows however far the window travels, with no word from the arranger. An
 * edit is kept by identity and outlives its cell. Imports the stock text cell
 * and nothing of the grid.</p>
 */
public record EndlessRelation() implements DomModule<EndlessRelation> {

    public record createEndlessRelation() implements Exportable._Constant<EndlessRelation> {}

    public static final EndlessRelation INSTANCE = new EndlessRelation();

    @Override
    public ImportsFor<EndlessRelation> imports() {
        return ImportsFor.<EndlessRelation>builder()
                .add(new ModuleImports<>(List.of(new RelGridStockCellsModule.RelGridTextCell()), RelGridStockCellsModule.INSTANCE))
                .build();
    }

    @Override public ExportsOf<EndlessRelation> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new createEndlessRelation()));
    }
}
