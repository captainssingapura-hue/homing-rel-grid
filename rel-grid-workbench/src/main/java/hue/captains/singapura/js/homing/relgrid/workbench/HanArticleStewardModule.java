package hue.captains.singapura.js.homing.relgrid.workbench;

import hue.captains.singapura.js.homing.core.EsModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;

import java.util.List;

/**
 * The Han article party's steward: {@code HanArticleSteward} - the one member that does I/O,
 * keeping the bench's article in the browser's storage across visits. A class a root hires.
 */
public record HanArticleStewardModule() implements EsModule<HanArticleStewardModule> {

    public record HanArticleSteward() implements Exportable._Class<HanArticleStewardModule> {}

    public static final HanArticleStewardModule INSTANCE = new HanArticleStewardModule();

    @Override public ImportsFor<HanArticleStewardModule> imports() { return ImportsFor.noImports(); }

    @Override
    public ExportsOf<HanArticleStewardModule> exports() { return new ExportsOf<>(INSTANCE, List.of(new HanArticleSteward())); }
}
