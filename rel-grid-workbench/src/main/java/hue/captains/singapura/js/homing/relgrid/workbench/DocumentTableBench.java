package hue.captains.singapura.js.homing.relgrid.workbench;

import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.workspace.groups.core.models.ArrangedWidget;
import hue.captains.singapura.js.homing.workspace.groups.core.models.Arrangement;
import hue.captains.singapura.js.homing.workspace.groups.core.models.SplitGrid;
import hue.captains.singapura.js.homing.workspace.groups.core.models.SplitGrid.Part;
import hue.captains.singapura.js.homing.workspace.groups.core.models.WorkspaceArrangements;
import hue.captains.singapura.js.homing.workspace.widgets.NoParams;
import hue.captains.singapura.js.homing.workspace.widgets.WidgetDeclaration;
import hue.captains.singapura.js.homing.workspace.widgets.WidgetQuery;
import hue.captains.singapura.js.homing.workspace.widgets.WorkspaceDeclaration;

import java.util.List;

/**
 * The Document Table bench: the out-of-the-box document table, {@code ProseTable}, as a document has it - drawn in the
 * house's document table, its rules and its head the theme's own for a document's table. Three pages: release notes
 * and a matrix with rows that head the rows under them; the table's own options, its code drawn inline, and a table
 * with no head; and a table wider than its page.
 */
public final class DocumentTableBench {

    private DocumentTableBench() {}

    /** Release notes and what each surface offers. */
    public record Notes() implements WidgetDeclaration<NoParams> {
        public static final Notes INSTANCE = new Notes();
        @Override public String kind() { return "prose-notes"; }
        @Override public String title() { return "Release notes"; }
        @Override public Class<NoParams> paramsType() { return NoParams.class; }
        @Override public WidgetQuery<NoParams> query() { return new NoParams.Query(); }
        @Override public ModuleImports<?> constructs() {
            return new ModuleImports<>(List.of(new ProseNotesModule.ProseNotes()), ProseNotesModule.INSTANCE);
        }
    }

    /** ProseTable's own options, and a table with no head. */
    public record Reference() implements WidgetDeclaration<NoParams> {
        public static final Reference INSTANCE = new Reference();
        @Override public String kind() { return "prose-reference"; }
        @Override public String title() { return "ProseTable"; }
        @Override public Class<NoParams> paramsType() { return NoParams.class; }
        @Override public WidgetQuery<NoParams> query() { return new NoParams.Query(); }
        @Override public ModuleImports<?> constructs() {
            return new ModuleImports<>(List.of(new ProseReferenceModule.ProseReference()), ProseReferenceModule.INSTANCE);
        }
    }

    /** A table wider than its page. */
    public record Figures() implements WidgetDeclaration<NoParams> {
        public static final Figures INSTANCE = new Figures();
        @Override public String kind() { return "prose-figures"; }
        @Override public String title() { return "A wide table"; }
        @Override public Class<NoParams> paramsType() { return NoParams.class; }
        @Override public WidgetQuery<NoParams> query() { return new NoParams.Query(); }
        @Override public ModuleImports<?> constructs() {
            return new ModuleImports<>(List.of(new ProseFiguresModule.ProseFigures()), ProseFiguresModule.INSTANCE);
        }
    }

    /** The bench, a workspace: {@code document-table}. */
    public record Workspace() implements WorkspaceDeclaration {
        public static final Workspace INSTANCE = new Workspace();
        @Override public String name() { return "prose-tables"; }
        @Override public List<WidgetDeclaration<?>> kinds() { return List.of(Notes.INSTANCE, Reference.INSTANCE, Figures.INSTANCE); }
    }

    /** The first time: the document wide on the left; the reference over the wide table on the right, narrow, so it scrolls. */
    public static final WorkspaceArrangements<Workspace> ARRANGED = WorkspaceArrangements.of(Workspace.INSTANCE,
            Arrangement.of(Workspace.INSTANCE,
                    SplitGrid.of(SplitGrid.row(Part.of(SplitGrid.region("notes", "notes"), 3),
                            Part.of(SplitGrid.column(SplitGrid.region("reference", "reference"), SplitGrid.region("figures", "figures")), 2))),
                    ArrangedWidget.of("notes", Notes.INSTANCE.kind()),
                    ArrangedWidget.of("reference", Reference.INSTANCE.kind()),
                    ArrangedWidget.of("figures", Figures.INSTANCE.kind())));
}
