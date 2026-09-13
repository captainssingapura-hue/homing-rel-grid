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
public record GamesRelation() implements DomModule<GamesRelation> {

    public record createGamesRelation() implements Exportable._Constant<GamesRelation> {}

    public static final GamesRelation INSTANCE = new GamesRelation();

    @Override
    public ImportsFor<GamesRelation> imports() {
        return ImportsFor.<GamesRelation>builder()
                .add(new ModuleImports<>(List.of(new RelGridStockCellsModule.RelGridTextCell()), RelGridStockCellsModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new GamesConditions.gamesConditions(), new GamesConditions.gamesConditionsCopy(),
                        new GamesConditions.gamesToggleSort(), new GamesConditions.gamesSetSort(), new GamesConditions.gamesSetFilter(), new GamesConditions.gamesSortOf(),
                        new GamesConditions.gamesApply(), new GamesConditions.gamesDescribe()), GamesConditions.INSTANCE))
                .add(new ModuleImports<>(List.of(new GamesHeaderCells.GamesHeaderCell()), GamesHeaderCells.INSTANCE))
                .build();
    }

    @Override public ExportsOf<GamesRelation> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new createGamesRelation()));
    }
}
