package hue.captains.singapura.js.homing.relgrid.workbench;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;

import java.util.List;

/**
 * The Games Catalogue's header cells: nouns the relation answers to
 * {@code headerFor(column)}, placed by whoever arranges into the header slot
 * and never told anything after. A label, an
 * indication — a caret for the direction it sorts in, its number among the
 * keys, a mark while a filter is held — and one control, the ▾ that opens the
 * column's menu, where the sorting and the filtering are done. The label and
 * the caret are not buttons. Imports its typed looks, the menu, and nothing of
 * the grid.
 */
public record GamesHeaderCells() implements DomModule<GamesHeaderCells> {

    public record GamesHeaderCell() implements Exportable._Class<GamesHeaderCells> {}

    public static final GamesHeaderCells INSTANCE = new GamesHeaderCells();

    @Override
    public ImportsFor<GamesHeaderCells> imports() {
        return ImportsFor.<GamesHeaderCells>builder()
                .add(new ModuleImports<>(List.of(new GamesStyles.wb_gh(), new GamesStyles.wb_gh_label(), new GamesStyles.wb_gh_caret(),
                        new GamesStyles.wb_gh_order(), new GamesStyles.wb_gh_mark(), new GamesStyles.wb_gh_menu(), new GamesStyles.wb_gh_menu_hot(),
                        new GamesStyles.wb_gh_menu_on(), new GamesStyles.wb_gh_menu_open()),
                        GamesStyles.INSTANCE))
                .add(new ModuleImports<>(List.of(new GamesColumnMenuModule.GamesColumnMenu()), GamesColumnMenuModule.INSTANCE))
                .build();
    }

    @Override public ExportsOf<GamesHeaderCells> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new GamesHeaderCell()));
    }
}
