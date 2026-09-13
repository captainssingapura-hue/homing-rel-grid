package hue.captains.singapura.js.homing.relgrid.workbench;

import hue.captains.singapura.js.homing.workspace.shell.ActionDispatch;
import hue.captains.singapura.js.homing.workspace.shell.Arrangement;
import hue.captains.singapura.js.homing.workspace.shell.PaneArrangements;
import hue.captains.singapura.js.homing.workspace.shell.PaneArrangements.Columns;
import hue.captains.singapura.js.homing.workspace.shell.PartyDecl;
import hue.captains.singapura.js.homing.workspace.RibbonItem;
import hue.captains.singapura.js.homing.workspace.shell.WidgetCodecRef;
import hue.captains.singapura.js.homing.workspace.WidgetDescription;
import hue.captains.singapura.js.homing.workspace.WidgetEntry;
import hue.captains.singapura.js.homing.workspace.WidgetGroup;
import hue.captains.singapura.js.homing.workspace.WidgetIcon;
import hue.captains.singapura.js.homing.workspace.WidgetLabel;
import hue.captains.singapura.js.homing.workspace.shell.WorkspaceSpec;
import hue.captains.singapura.js.homing.workspace.shell.WorkspaceSpecRegistry;

import java.util.List;
import java.util.Map;

/**
 * Endless Table ({@code ws_kind=endlessTable}): the stress bench for the two
 * branches — a grid over a relation of a million rows, showing a window of
 * twenty and never told there are more.
 *
 * <p>The relation answers its View as a window and moves it when asked —
 * {@code view({ by })}, the one root's one seam — and frees a row the moment
 * it is outside two Views. The grid arranges every window on the same slots,
 * forgets what leaves, and asks again for what returns. The bench reads the
 * party's numbers after every step: the grid's branch a constant, the
 * domain's bounded, the registry exactly the window. A burst of a few hundred
 * steps says what a step costs.</p>
 */
public final class EndlessTableSpec implements WorkspaceSpec {

    public static final EndlessTableSpec INSTANCE;

    static {
        INSTANCE = new EndlessTableSpec();
        WorkspaceSpecRegistry.INSTANCE.register(INSTANCE);
    }

    private EndlessTableSpec() {}

    @Override public String kind()  { return "endlessTable"; }
    @Override public String title() { return "Endless Table"; }

    @Override
    public List<WidgetEntry> widgetEntries() {
        return List.of(
                WidgetEntry.of(EndlessWidget.class, WidgetLabel.of("Endless"))
                        .withIcon(new WidgetIcon.Emoji("♾️"))
                        .withGroup(WidgetGroup.of("Stress"))
                        .withDescription(WidgetDescription.of(
                                "A million rows, twenty shown. Wheel, arrows at the edge, page keys and "
                              + "buttons move the window; the party's counts after every step say the "
                              + "grid's side is constant and the domain's bounded. Dock two to compare.")));
    }

    @Override public List<RibbonItem> ribbonItems() { return List.of(); }
    @Override public List<PartyDecl> parties() { return List.of(); }
    @Override public Map<String, ActionDispatch> actionDispatch() { return Map.of(); }
    @Override public List<WidgetCodecRef> widgetCodecs() { return List.of(); }

    /** Opens with the endless table on the left; the right is free for a second one, or a control. */
    @Override public Arrangement arrangement() {
        return PaneArrangements.COLUMNS.allocate()
                .place(Columns.LEFT, EndlessWidget.class)
                .build();
    }
}
