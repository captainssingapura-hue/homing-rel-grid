package hue.captains.singapura.js.homing.relgrid.workbench;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.relgrid.protocol.RelGridProtocolModule;
import hue.captains.singapura.js.homing.reltree.RelTreeStockCellsModule;

import java.util.List;

/**
 * The Games Tree bench's tree relation: the catalogue as type → series →
 * title, answering places lazily and the fold and unfold on the channel by
 * flipping a fold state of its own. Three ways to answer an unfold — at once,
 * late under the mask with a note on the panel, nothing-then-told — so the
 * bench shows all three. Imports the tree's stock text cell and the protocol's
 * tree messages; nothing of the tree itself.
 */
public record GamesTreeRelationModule() implements DomModule<GamesTreeRelationModule> {

    public record GamesTreeRelation() implements Exportable._Class<GamesTreeRelationModule> {}

    public static final GamesTreeRelationModule INSTANCE = new GamesTreeRelationModule();

    @Override public ImportsFor<GamesTreeRelationModule> imports() {
        return ImportsFor.<GamesTreeRelationModule>builder()
                .add(new ModuleImports<>(List.of(new RelTreeStockCellsModule.RelTreeTextCell()), RelTreeStockCellsModule.INSTANCE))
                .add(new ModuleImports<>(
                        List.of(new RelGridProtocolModule.RelTreeUnfold(), new RelGridProtocolModule.RelTreeFold(),
                                new RelGridProtocolModule.RelTreeView()),
                        RelGridProtocolModule.INSTANCE))
                .build();
    }

    @Override public ExportsOf<GamesTreeRelationModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new GamesTreeRelation()));
    }
}
