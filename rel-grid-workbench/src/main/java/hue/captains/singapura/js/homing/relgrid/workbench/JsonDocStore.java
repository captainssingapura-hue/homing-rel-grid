package hue.captains.singapura.js.homing.relgrid.workbench;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;

import java.util.List;

/**
 * The JSON Tree bench's one document: a text parsed on every set, and the
 * last value that parsed. The Input widget writes it, the Display shows it,
 * and neither knows the other — the same seam the Han and Outlets benches
 * share a store through. Domain code, no DOM.
 */
public record JsonDocStore() implements DomModule<JsonDocStore> {

    public record createJsonDocStore() implements Exportable._Constant<JsonDocStore> {}
    public record jsonDocStoreShared() implements Exportable._Constant<JsonDocStore> {}

    public static final JsonDocStore INSTANCE = new JsonDocStore();

    @Override public ImportsFor<JsonDocStore> imports() { return ImportsFor.noImports(); }

    @Override public ExportsOf<JsonDocStore> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new createJsonDocStore(), new jsonDocStoreShared()));
    }
}
