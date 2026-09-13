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
 * and never told anything after. A label that is a button — click to sort,
 * shift-click to add — a caret for the order held, and a funnel that opens a
 * popover to choose rows by the column: contains, a range, any of. The
 * popover is the cell's own, on a sub-branch, dissolved when it closes. Imports
 * its typed looks and nothing of the grid.
 */
public record GamesHeaderCells() implements DomModule<GamesHeaderCells> {

    public record GamesHeaderCell() implements Exportable._Class<GamesHeaderCells> {}

    public static final GamesHeaderCells INSTANCE = new GamesHeaderCells();

    @Override
    public ImportsFor<GamesHeaderCells> imports() {
        return ImportsFor.<GamesHeaderCells>builder()
                .add(new ModuleImports<>(List.of(new GamesStyles.wb_gh(), new GamesStyles.wb_gh_sort(), new GamesStyles.wb_gh_sort_hot(),
                        new GamesStyles.wb_gh_sort_on(), new GamesStyles.wb_gh_label(), new GamesStyles.wb_gh_caret(), new GamesStyles.wb_gh_order(),
                        new GamesStyles.wb_gh_filter(), new GamesStyles.wb_gh_filter_hot(), new GamesStyles.wb_gh_filter_on(), new GamesStyles.wb_gh_filter_open(),
                        new GamesStyles.wb_gpop(), new GamesStyles.wb_gpop_title(), new GamesStyles.wb_gpop_row(), new GamesStyles.wb_gpop_input(),
                        new GamesStyles.wb_gpop_list(), new GamesStyles.wb_gpop_check(), new GamesStyles.wb_gpop_actions(), new GamesStyles.wb_gpop_btn()),
                        GamesStyles.INSTANCE))
                .build();
    }

    @Override public ExportsOf<GamesHeaderCells> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new GamesHeaderCell()));
    }
}
