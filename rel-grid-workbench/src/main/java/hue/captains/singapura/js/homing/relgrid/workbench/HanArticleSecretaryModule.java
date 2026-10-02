package hue.captains.singapura.js.homing.relgrid.workbench;

import hue.captains.singapura.js.homing.core.EsModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;

import java.util.List;

/**
 * The Han article party's secretary: {@code initial} and {@code behavior(state, envelope) →
 * { newState, actions }} - the one article, asked of the steward when first wanted, said to every
 * member after every change and to a member that asks, handed to the steward to keep. Pure.
 */
public record HanArticleSecretaryModule() implements EsModule<HanArticleSecretaryModule> {

    public record HanArticleSecretary() implements Exportable._Constant<HanArticleSecretaryModule> {}

    public static final HanArticleSecretaryModule INSTANCE = new HanArticleSecretaryModule();

    @Override
    public ImportsFor<HanArticleSecretaryModule> imports() {
        return ImportsFor.<HanArticleSecretaryModule>builder()
                .add(new ModuleImports<>(List.of(new HanStoreModule.HanStore()), HanStoreModule.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<HanArticleSecretaryModule> exports() { return new ExportsOf<>(INSTANCE, List.of(new HanArticleSecretary())); }
}
