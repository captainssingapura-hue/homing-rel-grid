package hue.captains.singapura.js.homing.relgrid.workbench;

import hue.captains.singapura.js.homing.core.EsModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;

import java.util.List;

/**
 * The order and the choice of rows, as data: PURE LOGIC, conditions in and
 * keys out. What a domain holds when it sorts and filters for itself — the
 * state a header cell's caret and funnel change, and the function that turns
 * it and the store's values into a View. Every function answers a new value.
 * The judgements are stated where they are made: numbers as numbers, text by
 * locale, absence last in either direction and never passing a filter.
 */
public record GamesConditionsModule() implements EsModule<GamesConditionsModule> {

    public record GamesConditions() implements Exportable._Class<GamesConditionsModule> {}

    public static final GamesConditionsModule INSTANCE = new GamesConditionsModule();

    @Override public ImportsFor<GamesConditionsModule> imports() { return ImportsFor.noImports(); }

    @Override public ExportsOf<GamesConditionsModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new GamesConditions()));
    }
}
