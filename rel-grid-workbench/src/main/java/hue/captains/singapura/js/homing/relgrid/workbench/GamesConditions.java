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
public record GamesConditions() implements EsModule<GamesConditions> {

    public record gamesConditions()     implements Exportable._Constant<GamesConditions> {}
    public record gamesConditionsCopy() implements Exportable._Constant<GamesConditions> {}
    public record gamesToggleSort()     implements Exportable._Constant<GamesConditions> {}
    public record gamesSetSort()        implements Exportable._Constant<GamesConditions> {}
    public record gamesSetFilter()      implements Exportable._Constant<GamesConditions> {}
    public record gamesSortOf()         implements Exportable._Constant<GamesConditions> {}
    public record gamesApply()          implements Exportable._Constant<GamesConditions> {}
    public record gamesDescribe()       implements Exportable._Constant<GamesConditions> {}

    public static final GamesConditions INSTANCE = new GamesConditions();

    @Override public ImportsFor<GamesConditions> imports() { return ImportsFor.noImports(); }

    @Override public ExportsOf<GamesConditions> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new gamesConditions(), new gamesConditionsCopy(), new gamesToggleSort(),
                new gamesSetSort(), new gamesSetFilter(), new gamesSortOf(), new gamesApply(), new gamesDescribe()));
    }
}
