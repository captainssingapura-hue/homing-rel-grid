package hue.captains.singapura.js.homing.relgrid.workbench;

import hue.captains.singapura.js.homing.core.EsModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;

import java.util.List;

/**
 * A JSON document: {@code JsonDocStore} - a text, parsed on every set, and the last value that
 * parsed - with the sample the bench opens with. Domain code: no DOM. Each JSON bench widget
 * keeps its own; what they share is the text, through the {@code json-text} party.
 */
public record JsonDocStoreModule() implements EsModule<JsonDocStoreModule> {

    public record JsonDocStore() implements Exportable._Class<JsonDocStoreModule> {}

    public static final JsonDocStoreModule INSTANCE = new JsonDocStoreModule();

    @Override public ImportsFor<JsonDocStoreModule> imports() { return ImportsFor.noImports(); }

    @Override public ExportsOf<JsonDocStoreModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new JsonDocStore()));
    }
}
