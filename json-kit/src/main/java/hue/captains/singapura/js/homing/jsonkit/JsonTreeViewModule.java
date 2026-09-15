package hue.captains.singapura.js.homing.jsonkit;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.relgrid.protocol.RelGridProtocolModule;
import hue.captains.singapura.js.homing.reltree.RelTreeModule;

import java.util.List;

/**
 * The JSON viewer, whole: a {@code RelTree} over a JSON document, composed
 * so a host writes one line. The view is the tree's host and keeps the
 * host's discipline — two branches beneath its own, the tree's and the
 * document's. Its surface is {@link hue.captains.singapura.js.homing.jsonkit.contract.JsonTreeViewContract}.
 */
public record JsonTreeViewModule() implements DomModule<JsonTreeViewModule> {
    public record JsonTreeView() implements Exportable._Constant<JsonTreeViewModule> {}
    public static final JsonTreeViewModule INSTANCE = new JsonTreeViewModule();
    @Override public ImportsFor<JsonTreeViewModule> imports() {
        return ImportsFor.<JsonTreeViewModule>builder()
                .add(new ModuleImports<>(List.of(new RelTreeModule.RelTree()), RelTreeModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new JsonDocumentModule.createJsonDocument()), JsonDocumentModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new RelGridProtocolModule.RelTreeViewChanged()), RelGridProtocolModule.INSTANCE))
                .build();
    }
    @Override public ExportsOf<JsonTreeViewModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new JsonTreeView()));
    }
}
