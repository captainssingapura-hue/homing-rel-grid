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
 * The JSON Tree bench: the kit's viewer over a document typed in beside it.
 * Two widgets, one store: the Input writes it, the Display shows the last
 * value that parsed, and neither knows the other. The bench for the first
 * out-of-the-box offering — and a stress on the tree's remap: every keystroke
 * is a new value, the folds and the cursor kept by pointer.
 */
public final class JsonTreeSpec implements WorkspaceSpec {

    public static final JsonTreeSpec INSTANCE;

    static {
        INSTANCE = new JsonTreeSpec();
        WorkspaceSpecRegistry.INSTANCE.register(INSTANCE);
    }

    private JsonTreeSpec() {}

    @Override public String kind()  { return "jsonTree"; }
    @Override public String title() { return "JSON Tree"; }

    @Override
    public List<WidgetEntry> widgetEntries() {
        return List.of(
                WidgetEntry.of(JsonInputWidget.class, WidgetLabel.of("JSON input"))
                        .withIcon(new WidgetIcon.Emoji("✍"))
                        .withGroup(WidgetGroup.of("JSON"))
                        .withDescription(WidgetDescription.of(
                                "A plain textarea over the bench's one document. Every keystroke parses; the "
                              + "Display keeps the last value that parsed while the text is broken.")),
                WidgetEntry.of(JsonDisplayWidget.class, WidgetLabel.of("JSON tree"))
                        .withIcon(new WidgetIcon.Emoji("🌲"))
                        .withGroup(WidgetGroup.of("JSON"))
                        .withDescription(WidgetDescription.of(
                                "The kit's viewer, live: a JSON value as places by pointer and a cell per node, "
                              + "on the tree view. Dock two: each folds its own.")));
    }

    @Override public List<RibbonItem> ribbonItems() { return List.of(); }
    @Override public List<PartyDecl> parties() { return List.of(); }
    @Override public Map<String, ActionDispatch> actionDispatch() { return Map.of(); }
    @Override public List<WidgetCodecRef> widgetCodecs() { return List.of(); }

    /** Opens with the input on the left and the tree on the right. */
    @Override public Arrangement arrangement() {
        return PaneArrangements.COLUMNS.allocate()
                .place(Columns.LEFT,  JsonInputWidget.class)
                .place(Columns.RIGHT, JsonDisplayWidget.class)
                .build();
    }
}
