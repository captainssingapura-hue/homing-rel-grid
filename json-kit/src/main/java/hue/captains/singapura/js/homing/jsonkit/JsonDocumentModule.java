package hue.captains.singapura.js.homing.jsonkit;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.relgrid.protocol.RelGridProtocolModule;
import hue.captains.singapura.js.homing.reltree.RelTreePlacesModule;

import java.util.List;

/**
 * A JSON value as what a tree asks its domain for — places by JSON pointer,
 * a cell per node, the fold and unfold answered at once from the value in
 * hand. Answers the tree's {@code TreeRelationContract}; a host hands it to
 * a {@code RelTree} as its {@code relation}, or takes the {@link
 * JsonTreeViewModule view}, which does.
 */
public record JsonDocumentModule() implements DomModule<JsonDocumentModule> {
    public record createJsonDocument() implements Exportable._Constant<JsonDocumentModule> {}
    public static final JsonDocumentModule INSTANCE = new JsonDocumentModule();
    @Override public ImportsFor<JsonDocumentModule> imports() {
        return ImportsFor.<JsonDocumentModule>builder()
                .add(new ModuleImports<>(List.of(new RelTreePlacesModule.RelTreePlaces()), RelTreePlacesModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new JsonNodeCellModule.JsonNodeCell()), JsonNodeCellModule.INSTANCE))
                .add(new ModuleImports<>(
                        List.of(new RelGridProtocolModule.RelTreeUnfold(), new RelGridProtocolModule.RelTreeFold(),
                                new RelGridProtocolModule.RelTreeView()),
                        RelGridProtocolModule.INSTANCE))
                .build();
    }
    @Override public ExportsOf<JsonDocumentModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new createJsonDocument()));
    }
}
