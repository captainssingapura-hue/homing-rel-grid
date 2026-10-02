package hue.captains.singapura.js.homing.relgrid.workbench;

import hue.captains.singapura.js.homing.core.EsModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;

import java.util.List;

/**
 * The dishes party's secretary: {@code initial} and {@code behavior(state, envelope) →
 * { newState, actions }} - the one store of dishes, asked of the steward when first wanted, changed
 * by commits, sales and resets as the store's rules say, said to every member after every change
 * and to a member that asks, handed to the steward to keep. Pure.
 */
public record DishesSecretaryModule() implements EsModule<DishesSecretaryModule> {

    public record DishesSecretary() implements Exportable._Constant<DishesSecretaryModule> {}

    public static final DishesSecretaryModule INSTANCE = new DishesSecretaryModule();

    @Override
    public ImportsFor<DishesSecretaryModule> imports() {
        return ImportsFor.<DishesSecretaryModule>builder()
                .add(new ModuleImports<>(List.of(new DishStoreModule.DishStore()), DishStoreModule.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<DishesSecretaryModule> exports() { return new ExportsOf<>(INSTANCE, List.of(new DishesSecretary())); }
}
