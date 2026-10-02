package hue.captains.singapura.js.homing.relgrid.workbench;

import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.workspace.groups.core.models.ArrangedWidget;
import hue.captains.singapura.js.homing.workspace.groups.core.models.Arrangement;
import hue.captains.singapura.js.homing.workspace.groups.core.models.SplitGrid;
import hue.captains.singapura.js.homing.workspace.groups.core.models.WorkspaceArrangements;
import hue.captains.singapura.js.homing.workspace.parties.PartyType;
import hue.captains.singapura.js.homing.workspace.widgets.NoParams;
import hue.captains.singapura.js.homing.workspace.widgets.WidgetDeclaration;
import hue.captains.singapura.js.homing.workspace.widgets.WidgetQuery;
import hue.captains.singapura.js.homing.workspace.widgets.WorkspaceDeclaration;

import java.util.List;

/**
 * The Han Article bench: a WYSIWYG Chinese article in strictly square cells, nine to a row - the
 * bench that asks the grid for half-width punctuation columns. The editor and its displays meet
 * in the {@code han-article} party, which keeps the article across visits; the stress and the
 * articles are specimens of their own.
 */
public final class HanArticleBench {

    private HanArticleBench() {}

    /** The editor, declared: {@code han-editor}, one per bench, joining the Han article party. */
    public record Editor() implements WidgetDeclaration<NoParams> {
        public static final Editor INSTANCE = new Editor();
        @Override public String kind() { return "han-editor"; }
        @Override public String title() { return "Editor"; }
        @Override public Class<NoParams> paramsType() { return NoParams.class; }
        @Override public WidgetQuery<NoParams> query() { return new NoParams.Query(); }
        @Override public List<PartyType<?>> parties() { return List.of(HanArticle.TYPE); }
        /** One editor: the article is edited in one place, and every display follows it. */
        @Override public boolean single() { return true; }
        @Override public ModuleImports<?> constructs() {
            return new ModuleImports<>(List.of(new HanEditorModule.HanEditor()), HanEditorModule.INSTANCE);
        }
    }

    /** A display, declared: {@code han-display}, joining the Han article party. Dock several. */
    public record Display() implements WidgetDeclaration<NoParams> {
        public static final Display INSTANCE = new Display();
        @Override public String kind() { return "han-display"; }
        @Override public String title() { return "Display"; }
        @Override public Class<NoParams> paramsType() { return NoParams.class; }
        @Override public WidgetQuery<NoParams> query() { return new NoParams.Query(); }
        @Override public List<PartyType<?>> parties() { return List.of(HanArticle.TYPE); }
        @Override public ModuleImports<?> constructs() {
            return new ModuleImports<>(List.of(new HanDisplayModule.HanDisplay()), HanDisplayModule.INSTANCE);
        }
    }

    /** The stress, declared: {@code han-stress}, its own text, nothing shared. */
    public record Stress() implements WidgetDeclaration<NoParams> {
        public static final Stress INSTANCE = new Stress();
        @Override public String kind() { return "han-stress"; }
        @Override public String title() { return "Stress"; }
        @Override public Class<NoParams> paramsType() { return NoParams.class; }
        @Override public WidgetQuery<NoParams> query() { return new NoParams.Query(); }
        @Override public ModuleImports<?> constructs() {
            return new ModuleImports<>(List.of(new HanStressModule.HanStress()), HanStressModule.INSTANCE);
        }
    }

    /** The articles, declared: {@code han-articles}, two poems of their own, nothing shared. */
    public record Articles() implements WidgetDeclaration<NoParams> {
        public static final Articles INSTANCE = new Articles();
        @Override public String kind() { return "han-articles"; }
        @Override public String title() { return "Articles"; }
        @Override public Class<NoParams> paramsType() { return NoParams.class; }
        @Override public WidgetQuery<NoParams> query() { return new NoParams.Query(); }
        @Override public ModuleImports<?> constructs() {
            return new ModuleImports<>(List.of(new HanArticlesModule.HanArticles()), HanArticlesModule.INSTANCE);
        }
    }

    /** The bench, a workspace: {@code han-article}. */
    public record Workspace() implements WorkspaceDeclaration {
        public static final Workspace INSTANCE = new Workspace();
        @Override public String name() { return "han-article"; }
        @Override public List<WidgetDeclaration<?>> kinds() { return List.of(Editor.INSTANCE, Display.INSTANCE, Stress.INSTANCE, Articles.INSTANCE); }
    }

    /** The first time: the editor beside a display; the stress and the articles a tab away in the display's region. */
    public static final WorkspaceArrangements<Workspace> ARRANGED = WorkspaceArrangements.of(Workspace.INSTANCE,
            Arrangement.of(Workspace.INSTANCE,
                    SplitGrid.of(SplitGrid.row(SplitGrid.region("editor", "editor"), SplitGrid.region("specimens", "display", "stress", "articles"))),
                    ArrangedWidget.of("editor", Editor.INSTANCE.kind()),
                    ArrangedWidget.of("display", Display.INSTANCE.kind()),
                    ArrangedWidget.of("stress", Stress.INSTANCE.kind()),
                    ArrangedWidget.of("articles", Articles.INSTANCE.kind())));
}
