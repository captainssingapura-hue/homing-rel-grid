package hue.captains.singapura.js.homing.relgrid.workbench;

import hue.captains.singapura.js.homing.core.EsModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;

import java.util.List;

/**
 * The Games Catalogue's store: seven hundred-odd releases, 1990 to 2020, read
 * from the catalogue's pipe-separated text. Read-only, and pure logic — no
 * DOM, no persistence: a fact the bench orders and chooses from. Numbers are
 * numbers here and a blank is null, so a sort compares as numbers and has
 * to say what it does with absence.
 */
public record GamesStore() implements EsModule<GamesStore> {

    public record createGamesStore() implements Exportable._Constant<GamesStore> {}

    public static final GamesStore INSTANCE = new GamesStore();

    @Override public ImportsFor<GamesStore> imports() { return ImportsFor.noImports(); }

    @Override public ExportsOf<GamesStore> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new createGamesStore()));
    }
}
