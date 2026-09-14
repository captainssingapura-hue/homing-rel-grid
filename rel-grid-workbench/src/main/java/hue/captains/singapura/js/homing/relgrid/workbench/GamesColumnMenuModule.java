package hue.captains.singapura.js.homing.relgrid.workbench;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;

import java.util.List;

/**
 * The Games Catalogue's column menu — what opens from a header cell's ▾, the
 * way a spreadsheet's does. Sorting at the top, each choice applied at once
 * and the menu gone, with "then by" to keep the keys already held; below it
 * the filter: a search that narrows the column's values, the values with
 * their counts and a box each, "(select all)" over whatever the search shows,
 * a range for a number — staged until OK, so a table never empties under a
 * person still choosing. On its own branch, dissolved when it closes. Imports
 * its typed looks and nothing of the grid.
 */
public record GamesColumnMenuModule() implements DomModule<GamesColumnMenuModule> {

    public record GamesColumnMenu() implements Exportable._Class<GamesColumnMenuModule> {}

    public static final GamesColumnMenuModule INSTANCE = new GamesColumnMenuModule();

    @Override
    public ImportsFor<GamesColumnMenuModule> imports() {
        return ImportsFor.<GamesColumnMenuModule>builder()
                .add(new ModuleImports<>(List.of(new GamesStyles.wb_gmenu(), new GamesStyles.wb_gmenu_item(), new GamesStyles.wb_gmenu_item_hot(),
                        new GamesStyles.wb_gmenu_item_on(), new GamesStyles.wb_gmenu_check(), new GamesStyles.wb_gmenu_hidden(),
                        new GamesStyles.wb_gmenu_sep(), new GamesStyles.wb_gmenu_search(), new GamesStyles.wb_gmenu_range(),
                        new GamesStyles.wb_gmenu_list(), new GamesStyles.wb_gmenu_count(), new GamesStyles.wb_gmenu_actions(),
                        new GamesStyles.wb_gmenu_btn(), new GamesStyles.wb_gmenu_btn_primary()),
                        GamesStyles.INSTANCE))
                .build();
    }

    @Override public ExportsOf<GamesColumnMenuModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new GamesColumnMenu()));
    }
}
