package hue.captains.singapura.js.homing.jsonkit;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;

import java.util.List;

/**
 * The cell for one JSON node: a member's name, a colon, and its value printed
 * by kind — the kit's translation of a value into a row. Answers the tree's
 * cell contract; the tree places it and never reads it.
 */
public record JsonNodeCellModule() implements DomModule<JsonNodeCellModule> {
    public record JsonNodeCell() implements Exportable._Constant<JsonNodeCellModule> {}
    public static final JsonNodeCellModule INSTANCE = new JsonNodeCellModule();
    @Override public ImportsFor<JsonNodeCellModule> imports() {
        return ImportsFor.<JsonNodeCellModule>builder()
                .add(new ModuleImports<>(
                        List.of(new JsonTreeStyles.jk_cell(), new JsonTreeStyles.jk_cell_current(), new JsonTreeStyles.jk_key(),
                                new JsonTreeStyles.jk_index(), new JsonTreeStyles.jk_punct(), new JsonTreeStyles.jk_string(),
                                new JsonTreeStyles.jk_number(), new JsonTreeStyles.jk_literal(), new JsonTreeStyles.jk_container()),
                        JsonTreeStyles.INSTANCE))
                .build();
    }
    @Override public ExportsOf<JsonNodeCellModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new JsonNodeCell()));
    }
}
