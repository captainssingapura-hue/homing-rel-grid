package hue.captains.singapura.js.homing.relgrid.workbench;

import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.workspace.parties.PartyType;

import java.util.List;

/**
 * What a JSON text party carries: the one document the JSON Tree bench's widgets share, as its
 * text. A member does - {@link SetText}, {@link CurrentRequested}; the party says - {@link Text},
 * the text and its revision, to every member after every set and to a member that asks. Each
 * widget parses what it hears; none knows the others.
 */
public sealed interface JsonText {

    /** A member typed: this is the text now. */
    record SetText(String text) implements JsonText {}

    /** A member asks for the text - one that joins late - and is answered alone. */
    record CurrentRequested() implements JsonText {}

    /** The party says: this is the text, at this revision. */
    record Text(String text, int revision) implements JsonText {}

    /**
     * The type: {@code json-text}, served as {@code JSON_TEXT}; its root's secretary, unless a
     * workspace puts its own, {@code JsonTextSecretary}.
     */
    PartyType<JsonText> TYPE = new PartyType<>("json-text", JsonText.class)
            .servedFrom(new ModuleImports<>(List.of(new JsonTextModule.JSON_TEXT()), JsonTextModule.INSTANCE))
            .withSecretary(new ModuleImports<>(List.of(new JsonTextSecretaryModule.JsonTextSecretary()), JsonTextSecretaryModule.INSTANCE));
}
