package hue.captains.singapura.js.homing.relgrid.workbench;

import hue.captains.singapura.js.homing.core.EsModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;

import java.util.List;

/**
 * The JSON text party's secretary: {@code initial} and {@code behavior(state, envelope) →
 * { newState, actions }} - the one text, opening on the sample, said to every member after every
 * set and to a member that asks. Pure.
 */
public record JsonTextSecretaryModule() implements EsModule<JsonTextSecretaryModule> {

    public record JsonTextSecretary() implements Exportable._Constant<JsonTextSecretaryModule> {}

    public static final JsonTextSecretaryModule INSTANCE = new JsonTextSecretaryModule();

    @Override
    public ImportsFor<JsonTextSecretaryModule> imports() {
        return ImportsFor.<JsonTextSecretaryModule>builder()
                .add(new ModuleImports<>(List.of(new JsonDocStoreModule.JsonDocStore()), JsonDocStoreModule.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<JsonTextSecretaryModule> exports() { return new ExportsOf<>(INSTANCE, List.of(new JsonTextSecretary())); }
}
