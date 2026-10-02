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
 * The Colour Extents bench - the substrate's, kept here because the grid's cells are its first
 * customers: six scaled words at nine extents, and a card on a slider. Two widgets, nothing
 * shared: the numbers are each widget's, the colours the design's.
 */
public final class ColourExtentsBench {

    private ColourExtentsBench() {}

    /** The table, declared: {@code extent-table}, no params. */
    public record Table() implements WidgetDeclaration<NoParams> {
        public static final Table INSTANCE = new Table();
        @Override public String kind() { return "extent-table"; }
        @Override public String title() { return "Colour extents"; }
        @Override public Class<NoParams> paramsType() { return NoParams.class; }
        @Override public WidgetQuery<NoParams> query() { return new NoParams.Query(); }
        @Override public ModuleImports<?> constructs() {
            return new ModuleImports<>(List.of(new ExtentTableModule.ExtentTable()), ExtentTableModule.INSTANCE);
        }
    }

    /** The card, declared: {@code extent-card}, no params. */
    public record Card() implements WidgetDeclaration<NoParams> {
        public static final Card INSTANCE = new Card();
        @Override public String kind() { return "extent-card"; }
        @Override public String title() { return "A card at an extent"; }
        @Override public Class<NoParams> paramsType() { return NoParams.class; }
        @Override public WidgetQuery<NoParams> query() { return new NoParams.Query(); }
        @Override public ModuleImports<?> constructs() {
            return new ModuleImports<>(List.of(new ExtentCardModule.ExtentCard()), ExtentCardModule.INSTANCE);
        }
    }

    /** The bench, a workspace: {@code colour-extents}. */
    public record Workspace() implements WorkspaceDeclaration {
        public static final Workspace INSTANCE = new Workspace();
        @Override public String name() { return "colour-extents"; }
        @Override public List<WidgetDeclaration<?>> kinds() { return List.of(Table.INSTANCE, Card.INSTANCE); }
    }

    /** The first time: the table, twice as wide as the card beside it. */
    public static final WorkspaceArrangements<Workspace> ARRANGED = WorkspaceArrangements.of(Workspace.INSTANCE,
            Arrangement.of(Workspace.INSTANCE,
                    SplitGrid.of(SplitGrid.row(Part.of(SplitGrid.region("table", "table"), 2), Part.of(SplitGrid.region("card", "card"), 1))),
                    ArrangedWidget.of("table", Table.INSTANCE.kind()),
                    ArrangedWidget.of("card", Card.INSTANCE.kind())));
}
