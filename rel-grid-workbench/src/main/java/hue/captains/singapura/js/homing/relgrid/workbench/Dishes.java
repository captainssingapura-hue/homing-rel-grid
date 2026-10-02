package hue.captains.singapura.js.homing.relgrid.workbench;

import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.workspace.parties.PartyType;

import java.util.List;

/**
 * What a dishes party carries: the Replicating Tables bench's one store of dishes, which every
 * table there replicates and its steward keeps across visits. A member does - {@link Commit},
 * {@link Sell}, {@link Reset}, {@link CurrentRequested}; the party says - {@link State}, the
 * dishes and their revision, to every member after every change and to a member that asks.
 * Between the secretary and the steward: {@link Load} and {@link Loaded} when the dishes are
 * first asked for, {@link Save} after every change.
 *
 * <p>The dishes travel as JSON text: the store's own document, its values typed as the store
 * holds them - a number where a number was committed, text where text was - which a fixed
 * record shape could not carry.</p>
 */
public sealed interface Dishes {

    /** A member committed a value to a dish's column - the value as JSON. */
    record Commit(String pk, String column, String value) implements Dishes {}

    /** A member sold n more of a dish. */
    record Sell(String pk, int n) implements Dishes {}

    /** A member asks for the seed back. */
    record Reset() implements Dishes {}

    /** A member asks for the dishes - one that joins late - and is answered alone, once there are some. */
    record CurrentRequested() implements Dishes {}

    /** The party says: these are the dishes, at this revision - as JSON. */
    record State(String dishes, int revision) implements Dishes {}

    /** The secretary asks the steward for the dishes kept. */
    record Load() implements Dishes {}

    /** The steward answers: the dishes kept, as JSON, when there were any. */
    record Loaded(String dishes, boolean found) implements Dishes {}

    /** The secretary asks the steward to keep the dishes - as JSON. */
    record Save(String dishes) implements Dishes {}

    /**
     * The type: {@code dishes}, served as {@code DISHES}; its root's secretary, unless a workspace
     * puts its own, {@code DishesSecretary}; its steward, which keeps the dishes in the browser's
     * storage, {@code DishesSteward}.
     */
    PartyType<Dishes> TYPE = new PartyType<>("dishes", Dishes.class)
            .servedFrom(new ModuleImports<>(List.of(new DishesModule.DISHES()), DishesModule.INSTANCE))
            .withSecretary(new ModuleImports<>(List.of(new DishesSecretaryModule.DishesSecretary()), DishesSecretaryModule.INSTANCE))
            .withSteward(new ModuleImports<>(List.of(new DishesStewardModule.DishesSteward()), DishesStewardModule.INSTANCE));
}
