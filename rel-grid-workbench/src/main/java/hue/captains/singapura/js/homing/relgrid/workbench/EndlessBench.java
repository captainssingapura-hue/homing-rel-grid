package hue.captains.singapura.js.homing.relgrid.workbench;

import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.workspace.groups.core.models.ArrangedWidget;
import hue.captains.singapura.js.homing.workspace.groups.core.models.Arrangement;
import hue.captains.singapura.js.homing.workspace.groups.core.models.SplitGrid;
import hue.captains.singapura.js.homing.workspace.groups.core.models.WorkspaceArrangements;
import hue.captains.singapura.js.homing.workspace.widgets.NoParams;
import hue.captains.singapura.js.homing.workspace.widgets.WidgetDeclaration;
import hue.captains.singapura.js.homing.workspace.widgets.WidgetQuery;
import hue.captains.singapura.js.homing.workspace.widgets.WorkspaceDeclaration;

import java.util.List;

/**
 * The Endless Table bench: the stress bench for the two branches - a window of twenty over a
 * million rows, measured through the party. One widget, the table, filling the bench.
 */
public final class EndlessBench {

    private EndlessBench() {}

    /** The endless table, declared: {@code endless-table}, no params, nothing shared. */
    public record Table() implements WidgetDeclaration<NoParams> {
        public static final Table INSTANCE = new Table();
        @Override public String kind() { return "endless-table"; }
        @Override public String title() { return "Endless table"; }
        @Override public Class<NoParams> paramsType() { return NoParams.class; }
        @Override public WidgetQuery<NoParams> query() { return new NoParams.Query(); }
        @Override public ModuleImports<?> constructs() {
            return new ModuleImports<>(List.of(new EndlessTableModule.EndlessTable()), EndlessTableModule.INSTANCE);
        }
    }

    /** The bench, a workspace: {@code endless-table}. */
    public record Workspace() implements WorkspaceDeclaration {
        public static final Workspace INSTANCE = new Workspace();
        @Override public String name() { return "endless-table"; }
        @Override public List<WidgetDeclaration<?>> kinds() { return List.of(Table.INSTANCE); }
    }

    /** The first time: the table, filling the bench. */
    public static final WorkspaceArrangements<Workspace> ARRANGED = WorkspaceArrangements.of(Workspace.INSTANCE,
            Arrangement.of(Workspace.INSTANCE, SplitGrid.of(SplitGrid.region("table", "table")),
                    ArrangedWidget.of("table", Table.INSTANCE.kind())));
}
