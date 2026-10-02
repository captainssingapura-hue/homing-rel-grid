package hue.captains.singapura.js.homing.relgrid.workbench;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.relgrid.RelGridStockCellsModule;

import java.util.List;

/**
 * The Games Catalogue's root relation, and the bench's whole point: a
 * relation that sorts and filters FOR ITSELF, with header cells of its own,
 * over a store of its own. It answers what rows to present now, the cell for
 * an identity and the header cell for a column — and the header cells it
 * answers with are the controls by which a person changes what {@code view()}
 * will answer next. It cannot reach whoever presents it and does not try: it
 * tells its owner, {@code onViewChanged}, and the owner tells the presenter.
 * Imports the stock text cell, the conditions and its header cells; nothing
 * of the grid.
 */
public record GamesRelationModule() implements DomModule<GamesRelationModule> {

    public record GamesRelation() implements Exportable._Class<GamesRelationModule> {}

    public static final GamesRelationModule INSTANCE = new GamesRelationModule();

    @Override
    public ImportsFor<GamesRelationModule> imports() {
        return ImportsFor.<GamesRelationModule>builder()
                .add(new ModuleImports<>(List.of(new RelGridStockCellsModule.RelGridTextCell()), RelGridStockCellsModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new GamesConditionsModule.GamesConditions()), GamesConditionsModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new GamesHeaderCells.GamesHeaderCell()), GamesHeaderCells.INSTANCE))
                .build();
    }

    @Override public ExportsOf<GamesRelationModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new GamesRelation()));
    }
}
