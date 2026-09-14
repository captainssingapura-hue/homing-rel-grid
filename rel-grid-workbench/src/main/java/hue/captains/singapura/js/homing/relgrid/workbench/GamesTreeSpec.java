package hue.captains.singapura.js.homing.relgrid.workbench;

import hue.captains.singapura.js.homing.workspace.WidgetDescription;
import hue.captains.singapura.js.homing.workspace.WidgetGroup;
import hue.captains.singapura.js.homing.workspace.WidgetIcon;
import hue.captains.singapura.js.homing.workspace.WidgetLabel;
import hue.captains.singapura.js.homing.workspace.shell.ActionDispatch;
import hue.captains.singapura.js.homing.workspace.shell.Arrangement;
import hue.captains.singapura.js.homing.workspace.shell.PaneArrangements.Columns;
import hue.captains.singapura.js.homing.workspace.shell.PaneArrangements;
import hue.captains.singapura.js.homing.workspace.shell.PartyDecl;
import hue.captains.singapura.js.homing.workspace.RibbonItem;
import hue.captains.singapura.js.homing.workspace.shell.WidgetCodecRef;
import hue.captains.singapura.js.homing.workspace.WidgetEntry;
import hue.captains.singapura.js.homing.workspace.shell.WorkspaceSpec;
import hue.captains.singapura.js.homing.workspace.shell.WorkspaceSpecRegistry;

import java.util.List;
import java.util.Map;

/**
 * Games Tree ({@code ws_kind=gamesTree}): the tree view's first bench — the
 * catalogue as type → series → title, lazily, over a relation that answers the
 * fold and unfold on the ask channel three different ways.
 */
public final class GamesTreeSpec implements WorkspaceSpec {

    public static final GamesTreeSpec INSTANCE;

    static {
        INSTANCE = new GamesTreeSpec();
        WorkspaceSpecRegistry.INSTANCE.register(INSTANCE);
    }

    private GamesTreeSpec() {}

    @Override public String kind()  { return "gamesTree"; }
    @Override public String title() { return "Games Tree"; }

    @Override
    public List<WidgetEntry> widgetEntries() {
        return List.of(
                WidgetEntry.of(GamesTreeWidget.class, WidgetLabel.of("Games tree"))
                        .withIcon(new WidgetIcon.Emoji("🌳"))
                        .withGroup(WidgetGroup.of("Tree"))
                        .withDescription(WidgetDescription.of(
                                "Seven hundred-odd releases as type, series and title, unfolded lazily. The "
                              + "tree owns the rows and the keys; the relation owns the cells and the fold "
                              + "state; an unfold is a question on the channel. Dock two: each folds its own.")));
    }

    @Override public List<RibbonItem> ribbonItems() { return List.of(); }
    @Override public List<PartyDecl> parties() { return List.of(); }
    @Override public Map<String, ActionDispatch> actionDispatch() { return Map.of(); }
    @Override public List<WidgetCodecRef> widgetCodecs() { return List.of(); }

    /** Opens with the tree on the left; the right is free for a second one, or a catalogue. */
    @Override public Arrangement arrangement() {
        return PaneArrangements.COLUMNS.allocate()
                .place(Columns.LEFT, GamesTreeWidget.class)
                .build();
    }
}
