package hue.captains.singapura.js.homing.relgrid.workbench;

import hue.captains.singapura.js.homing.component.keyboard.Key;
import hue.captains.singapura.js.homing.component.keyboard.KeyBinding;
import hue.captains.singapura.js.homing.component.keyboard.NeedKeyboard;
import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.relgrid.RelGridStyles;
import hue.captains.singapura.js.homing.relgrid.protocol.RelGridProtocolModule;
import hue.captains.singapura.js.homing.reltree.RelTreeModule;
import hue.captains.singapura.js.homing.workspace.widgets.SelfContainedWidget;

import java.util.List;

/**
 * The games tree bench: {@code new GamesTree(container, params)} - the catalogue as a lazy
 * three-level tree on the tree view, the fold and the unfold asked on the channel and answered
 * three ways by the relation.
 */
public record GamesTreeModule() implements DomModule<GamesTreeModule> {

    public record GamesTree() implements SelfContainedWidget<GamesTreeModule>, NeedKeyboard {
        @Override public String summary() { return "The catalogue as a lazy tree - type, series, title - the fold and the unfold questions on the channel, answered three ways."; }
        @Override public List<KeyBinding> keys() {
            return List.of(KeyBinding.of(Key.ESCAPE, "the keys given back, when the tree did not take it"));
        }
    }

    public static final GamesTreeModule INSTANCE = new GamesTreeModule();

    @Override
    public ImportsFor<GamesTreeModule> imports() {
        return ImportsFor.<GamesTreeModule>builder()
                .add(new ModuleImports<>(List.of(new BenchWidgetModule.BenchWidget()), BenchWidgetModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new RelTreeModule.RelTree()), RelTreeModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new RelGridStyles.hrg_frame(), new RelGridStyles.hrg_lit()), RelGridStyles.INSTANCE))
                .add(new ModuleImports<>(List.of(new RelGridProtocolModule.RelTreeViewChanged()), RelGridProtocolModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new GamesStoreModule.GamesStore()), GamesStoreModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new GamesTreeRelationModule.GamesTreeRelation()), GamesTreeRelationModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new GamesDatasetModule.GAMES_CATALOGUE()), GamesDatasetModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new WorkbenchStyles.wb_hint(), new WorkbenchStyles.wb_bar(), new WorkbenchStyles.wb_frame(),
                        new WorkbenchStyles.wb_port(), new WorkbenchStyles.wb_status()), WorkbenchStyles.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<GamesTreeModule> exports() { return new ExportsOf<>(INSTANCE, List.of(new GamesTree())); }
}
