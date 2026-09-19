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
 * The Colour Extents bench: not a grid bench but the substrate's, kept here
 * because the grid's cells are its first customers. The table crosses six
 * scaled words with nine extents under whatever design the page wears; the
 * card puts three of them on one element and a slider on the number. By
 * claiming the six pairs in {@code extents()} the bench obliges every design
 * the studio registers to anchor them at −1, 0 and 1 — the table is the
 * specification, and the completeness test its proof.
 */
public final class ExtentTableSpec implements WorkspaceSpec {

    public static final ExtentTableSpec INSTANCE;

    static {
        INSTANCE = new ExtentTableSpec();
        WorkspaceSpecRegistry.INSTANCE.register(INSTANCE);
    }

    private ExtentTableSpec() {}

    @Override public String kind()  { return "colourExtents"; }
    @Override public String title() { return "Colour Extents"; }

    @Override
    public List<WidgetEntry> widgetEntries() {
        return List.of(
                WidgetEntry.of(ExtentTableWidget.class, WidgetLabel.of("Extent table"))
                        .withIcon(new WidgetIcon.Emoji("🎚"))
                        .withGroup(WidgetGroup.of("Extents"))
                        .withDescription(WidgetDescription.of(
                                "Six words that scale, at nine extents each: a row is a design pair, a cell an element "
                              + "wearing it with css.extent(el, x). Switch the theme and every colour changes; the numbers stay.")),
                WidgetEntry.of(ExtentCardWidget.class, WidgetLabel.of("Extent card"))
                        .withIcon(new WidgetIcon.Emoji("🃏"))
                        .withGroup(WidgetGroup.of("Extents"))
                        .withDescription(WidgetDescription.of(
                                "One card wearing its surface, edge and ink at an extent, and a slider on the number; "
                              + "the property is registered, so the transition tweens the colours.")));
    }

    @Override public List<RibbonItem> ribbonItems() { return List.of(); }
    @Override public List<PartyDecl> parties() { return List.of(); }
    @Override public Map<String, ActionDispatch> actionDispatch() { return Map.of(); }
    @Override public List<WidgetCodecRef> widgetCodecs() { return List.of(); }

    /** Opens with the table on the left and the card on the right. */
    @Override public Arrangement arrangement() {
        return PaneArrangements.COLUMNS.allocate()
                .place(Columns.LEFT,  ExtentTableWidget.class)
                .place(Columns.RIGHT, ExtentCardWidget.class)
                .build();
    }
}
