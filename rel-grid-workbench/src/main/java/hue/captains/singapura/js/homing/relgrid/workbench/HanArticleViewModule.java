package hue.captains.singapura.js.homing.relgrid.workbench;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.relgrid.RelGridModule;

import java.util.List;

/**
 * What the Han Article bench's editor and its displays are: {@code HanArticleView} - the article
 * as rows of square slots over a relation over the widget's own copy of the bench's one article,
 * which the {@code han-article} party says; with a textarea above, for the editor.
 */
public record HanArticleViewModule() implements DomModule<HanArticleViewModule> {

    public record HanArticleView() implements Exportable._Class<HanArticleViewModule> {}

    public static final HanArticleViewModule INSTANCE = new HanArticleViewModule();

    @Override
    public ImportsFor<HanArticleViewModule> imports() {
        return ImportsFor.<HanArticleViewModule>builder()
                .add(new ModuleImports<>(List.of(new BenchWidgetModule.BenchWidget()), BenchWidgetModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new RelGridModule.RelGrid()), RelGridModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new HanStoreModule.HanStore()), HanStoreModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new HanRelationModule.HanRelation()), HanRelationModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new HanArticleModule.HAN_ARTICLE()), HanArticleModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new WorkbenchStyles.wb_hint(), new WorkbenchStyles.wb_bar(), new WorkbenchStyles.wb_han_text(),
                        new WorkbenchStyles.wb_han_host(), new WorkbenchStyles.wb_status()), WorkbenchStyles.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<HanArticleViewModule> exports() { return new ExportsOf<>(INSTANCE, List.of(new HanArticleView())); }
}
