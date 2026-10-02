package hue.captains.singapura.js.homing.relgrid.workbench;

import hue.captains.singapura.js.homing.component.keyboard.Key;
import hue.captains.singapura.js.homing.component.keyboard.KeyBinding;
import hue.captains.singapura.js.homing.component.keyboard.NeedKeyboard;
import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.relgrid.RelGridModule;
import hue.captains.singapura.js.homing.relgrid.RelGridStyles;
import hue.captains.singapura.js.homing.relgrid.protocol.RelGridProtocolModule;
import hue.captains.singapura.js.homing.workspace.widgets.SelfContainedWidget;

import java.util.List;

/**
 * The games catalogue bench: {@code new GamesCatalogue(container, params)} - seven hundred
 * releases, sorted and filtered from header cells that are the relation's own; the grid told,
 * unasked, when the relation's View changes.
 */
public record GamesCatalogueModule() implements DomModule<GamesCatalogueModule> {

    public record GamesCatalogue() implements SelfContainedWidget<GamesCatalogueModule>, NeedKeyboard {
        @Override public String summary() { return "Seven hundred releases, sorted and filtered from header cells that are the relation's own; the grid knows none of it."; }
        @Override public List<KeyBinding> keys() {
            return List.of(KeyBinding.of(Key.ESCAPE, "the keys given back, when the grid did not take it"));
        }
    }

    public static final GamesCatalogueModule INSTANCE = new GamesCatalogueModule();

    @Override
    public ImportsFor<GamesCatalogueModule> imports() {
        return ImportsFor.<GamesCatalogueModule>builder()
                .add(new ModuleImports<>(List.of(new BenchWidgetModule.BenchWidget()), BenchWidgetModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new RelGridModule.RelGrid()), RelGridModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new RelGridStyles.hrg_frame(), new RelGridStyles.hrg_lit()), RelGridStyles.INSTANCE))
                .add(new ModuleImports<>(List.of(new RelGridProtocolModule.RelGridViewChanged()), RelGridProtocolModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new GamesStoreModule.GamesStore()), GamesStoreModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new GamesRelationModule.GamesRelation()), GamesRelationModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new GamesDatasetModule.GAMES_CATALOGUE()), GamesDatasetModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new WorkbenchStyles.wb_hint(), new WorkbenchStyles.wb_bar(), new WorkbenchStyles.wb_frame(),
                        new WorkbenchStyles.wb_port(), new WorkbenchStyles.wb_status()), WorkbenchStyles.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<GamesCatalogueModule> exports() { return new ExportsOf<>(INSTANCE, List.of(new GamesCatalogue())); }
}
