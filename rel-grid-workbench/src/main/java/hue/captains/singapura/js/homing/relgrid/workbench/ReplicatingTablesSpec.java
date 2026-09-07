package hue.captains.singapura.js.homing.relgrid.workbench;

import hue.captains.singapura.js.homing.workspace.RibbonItem;
import hue.captains.singapura.js.homing.workspace.WidgetDescription;
import hue.captains.singapura.js.homing.workspace.WidgetEntry;
import hue.captains.singapura.js.homing.workspace.WidgetGroup;
import hue.captains.singapura.js.homing.workspace.WidgetIcon;
import hue.captains.singapura.js.homing.workspace.WidgetLabel;
import hue.captains.singapura.js.homing.workspace.shell.ActionDispatch;
import hue.captains.singapura.js.homing.workspace.shell.PartyDecl;
import hue.captains.singapura.js.homing.workspace.shell.WidgetCodecRef;
import hue.captains.singapura.js.homing.workspace.shell.WorkspaceSpec;
import hue.captains.singapura.js.homing.workspace.shell.WorkspaceSpecRegistry;

import java.util.List;
import java.util.Map;

/**
 * Replicating Tables ({@code ws_kind=replicatingTables}): the first bench on
 * the Relation Grid, and the proof of its root principle.
 *
 * <p>One editor and any number of read-only followers, all over one persisted
 * store. An edit commits to the store; the store tells every relation; each
 * relation updates its own cells. Every follower on the page moves, and
 * <b>no grid was told</b> — the grid was handed a relation at construction
 * and never spoken to again. That is what "data agnostic" looks like from the
 * outside, and this bench exists so it can be watched rather than asserted.
 * How to use it is on the studio's home page.</p>
 */
public final class ReplicatingTablesSpec implements WorkspaceSpec {

    public static final ReplicatingTablesSpec INSTANCE;

    static {
        INSTANCE = new ReplicatingTablesSpec();
        WorkspaceSpecRegistry.INSTANCE.register(INSTANCE);
    }

    private ReplicatingTablesSpec() {}

    @Override public String kind()  { return "replicatingTables"; }
    @Override public String title() { return "Replicating Tables"; }

    @Override
    public List<WidgetEntry> widgetEntries() {
        return List.of(
                WidgetEntry.of(DishEditorWidget.class, WidgetLabel.of("Dish editor"))
                        .withIcon(new WidgetIcon.Emoji("✏️"))
                        .withGroup(WidgetGroup.of("Replicas"))
                        .withDescription(WidgetDescription.of(
                                "The one table that edits. Click or arrow to a cell, then Enter or "
                              + "double-click: the cell commits to the shared store. One instance.")),
                WidgetEntry.of(DishFollowerWidget.class, WidgetLabel.of("Dish follower"))
                        .withIcon(new WidgetIcon.Emoji("👥"))
                        .withGroup(WidgetGroup.of("Replicas"))
                        .withDescription(WidgetDescription.of(
                                "A read-only replica over the same store. Dock several; every "
                              + "one moves on every commit, and none of their grids is told."))
        );
    }

    @Override public List<RibbonItem> ribbonItems() { return List.of(); }
    @Override public List<PartyDecl> parties() { return List.of(); }
    @Override public Map<String, ActionDispatch> actionDispatch() { return Map.of(); }
    @Override public List<WidgetCodecRef> widgetCodecs() { return List.of(); }

    /** Nothing pinned — followers must stay spawnable many times. */
    @Override public List<String> pinnedSpawns() { return List.of(); }
}
