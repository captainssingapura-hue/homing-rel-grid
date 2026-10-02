package hue.captains.singapura.js.homing.relgrid.workbench;

import hue.captains.singapura.js.homing.core.EsModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;

import java.util.List;

/**
 * An article: {@code HanStore} - one string, telling its subscribers when it changed, opening on
 * the poem ({@code HanStore.SEED}). Domain code: no DOM, and no persistence - the bench's one
 * article is the {@code han-article} party's, kept by its steward; each widget's store is its own
 * copy of what the party says.
 */
public record HanStoreModule() implements EsModule<HanStoreModule> {

    public record HanStore() implements Exportable._Class<HanStoreModule> {}

    public static final HanStoreModule INSTANCE = new HanStoreModule();

    @Override public ImportsFor<HanStoreModule> imports() { return ImportsFor.noImports(); }

    @Override public ExportsOf<HanStoreModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new HanStore()));
    }
}
