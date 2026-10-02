package hue.captains.singapura.js.homing.relgrid.workbench;

import hue.captains.singapura.js.homing.core.EsModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;

import java.util.List;

/**
 * The dishes party's steward: {@code DishesSteward} - the one member that does I/O, keeping the
 * bench's dishes in the browser's storage across visits. A class a root hires.
 */
public record DishesStewardModule() implements EsModule<DishesStewardModule> {

    public record DishesSteward() implements Exportable._Class<DishesStewardModule> {}

    public static final DishesStewardModule INSTANCE = new DishesStewardModule();

    @Override public ImportsFor<DishesStewardModule> imports() { return ImportsFor.noImports(); }

    @Override
    public ExportsOf<DishesStewardModule> exports() { return new ExportsOf<>(INSTANCE, List.of(new DishesSteward())); }
}
