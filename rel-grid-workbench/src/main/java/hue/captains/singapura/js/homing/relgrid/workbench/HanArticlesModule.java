package hue.captains.singapura.js.homing.relgrid.workbench;

import hue.captains.singapura.js.homing.component.keyboard.Key;
import hue.captains.singapura.js.homing.component.keyboard.KeyBinding;
import hue.captains.singapura.js.homing.component.keyboard.NeedKeyboard;
import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.relgrid.group.RelGridGroupModule;
import hue.captains.singapura.js.homing.workspace.widgets.SelfContainedWidget;

import java.util.List;

/**
 * The Han Article bench's articles: {@code new HanArticles(container, params)} - two poems down
 * one page with an illustration between, three members of a group; titles, the illustration
 * and the colophon are the domain's fences.
 */
public record HanArticlesModule() implements DomModule<HanArticlesModule> {

    public record HanArticles() implements SelfContainedWidget<HanArticlesModule>, NeedKeyboard {
        @Override public String summary() { return "Two poems and an illustration between them, three members of a group; the fences the domain's."; }
        @Override public List<KeyBinding> keys() {
            return List.of(KeyBinding.of(Key.ESCAPE, "the keys given back, when the group did not take it"));
        }
    }

    public static final HanArticlesModule INSTANCE = new HanArticlesModule();

    @Override
    public ImportsFor<HanArticlesModule> imports() {
        return ImportsFor.<HanArticlesModule>builder()
                .add(new ModuleImports<>(List.of(new BenchWidgetModule.BenchWidget()), BenchWidgetModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new RelGridGroupModule.RelGridGroup()), RelGridGroupModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new HanStoreModule.HanStore()), HanStoreModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new HanRelationModule.HanRelation()), HanRelationModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new HanFencesModule.HanTitleFence(), new HanFencesModule.HanOrnamentFence(),
                        new HanFencesModule.HanColophonFence()), HanFencesModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new WorkbenchStyles.wb_hint(), new WorkbenchStyles.wb_han_host(),
                        new WorkbenchStyles.wb_status()), WorkbenchStyles.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<HanArticlesModule> exports() { return new ExportsOf<>(INSTANCE, List.of(new HanArticles())); }
}
