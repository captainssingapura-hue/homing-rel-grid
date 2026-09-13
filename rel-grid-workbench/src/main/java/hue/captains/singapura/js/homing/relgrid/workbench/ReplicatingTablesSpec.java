package hue.captains.singapura.js.homing.relgrid.workbench;

import hue.captains.singapura.js.homing.workspace.RibbonItem;
import hue.captains.singapura.js.homing.workspace.WidgetDescription;
import hue.captains.singapura.js.homing.workspace.WidgetEntry;
import hue.captains.singapura.js.homing.workspace.WidgetGroup;
import hue.captains.singapura.js.homing.workspace.WidgetIcon;
import hue.captains.singapura.js.homing.workspace.WidgetLabel;
import hue.captains.singapura.js.homing.workspace.shell.ActionDispatch;
import hue.captains.singapura.js.homing.workspace.shell.Arrangement;
import hue.captains.singapura.js.homing.workspace.shell.PaneArrangements;
import hue.captains.singapura.js.homing.workspace.shell.PaneArrangements.Columns;
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
 * <p>Three editors with different rights, and any number of read-only
 * followers, all over one persisted store. The chef edits ingredient and
 * style, the nutritionist calories, the shop manager price; nobody edits
 * {@code sold}, which only sales move, or {@code popularity}, which is
 * derived from it. An edit commits to the store; the store tells every
 * relation; each relation updates its own cells. Every table on the page
 * moves, and <b>no grid was told</b> — each grid was handed a relation at
 * construction and never spoken to again, and the grid cannot tell the
 * editors apart because nothing in its construction carries the role. That
 * is what "data agnostic" looks like from the outside, and this bench exists
 * so it can be watched rather than asserted. How to use it is on the studio's
 * home page.</p>
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
                WidgetEntry.of(DishChefWidget.class, WidgetLabel.of("Chef"))
                        .withIcon(new WidgetIcon.Emoji("🍳"))
                        .withGroup(WidgetGroup.of("Editors"))
                        .withDescription(WidgetDescription.of(
                                "Edits ingredient and style. Every other cell declines Enter: the grid "
                              + "asked, the cell said no. One instance.")),
                WidgetEntry.of(DishNutritionistWidget.class, WidgetLabel.of("Nutritionist"))
                        .withIcon(new WidgetIcon.Emoji("🥗"))
                        .withGroup(WidgetGroup.of("Editors"))
                        .withDescription(WidgetDescription.of(
                                "Edits calories only. Price is not theirs, and popularity is nobody's "
                              + "— try Enter on either. One instance.")),
                WidgetEntry.of(DishManagerWidget.class, WidgetLabel.of("Shop manager"))
                        .withIcon(new WidgetIcon.Emoji("💰"))
                        .withGroup(WidgetGroup.of("Editors"))
                        .withDescription(WidgetDescription.of(
                                "Edits price only, and runs a day of trade: sales are the one thing "
                              + "that moves popularity, which is derived and nobody's to edit. One instance.")),
                WidgetEntry.of(DishFollowerWidget.class, WidgetLabel.of("Follower"))
                        .withIcon(new WidgetIcon.Emoji("👥"))
                        .withGroup(WidgetGroup.of("Replicas"))
                        .withDescription(WidgetDescription.of(
                                "A read-only replica over the same store. Dock several; every "
                              + "one moves on every commit and every sale, and none of their grids is told.")),
                WidgetEntry.of(OutletsWidget.class, WidgetLabel.of("Outlets"))
                        .withIcon(new WidgetIcon.Emoji("🏪"))
                        .withGroup(WidgetGroup.of("Groups"))
                        .withDescription(WidgetDescription.of(
                                "Sales at three outlets, one table each, stacked in a group that shares "
                              + "only column widths. Trade at one outlet and only its book moves."))
        );
    }

    @Override public List<RibbonItem> ribbonItems() { return List.of(); }
    @Override public List<PartyDecl> parties() { return List.of(); }
    @Override public Map<String, ActionDispatch> actionDispatch() { return Map.of(); }
    @Override public List<WidgetCodecRef> widgetCodecs() { return List.of(); }

    /**
     * Opens as the intro says to dock it: the chef on the left, a follower on
     * the right — the smallest pair that shows one store, two relations. A seed
     * for a workspace with no saved state, not a template — and it hides nothing
     * from the picker, so the other editors and more followers are a click.
     */
    @Override public Arrangement arrangement() {
        return PaneArrangements.COLUMNS.allocate()
                .place(Columns.LEFT,  DishChefWidget.class)
                .place(Columns.RIGHT, DishFollowerWidget.class)
                .build();
    }
}
