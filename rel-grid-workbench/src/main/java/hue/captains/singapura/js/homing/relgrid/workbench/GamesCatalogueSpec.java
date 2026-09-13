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
 * Games Catalogue ({@code ws_kind=gamesCatalogue}): the bench for sorting and
 * filtering — a relation that does both FOR ITSELF, from header cells of its
 * own, over seven hundred-odd releases from 1990 to 2020.
 *
 * <p>The grid places the relation's header cells in its header slots as it
 * places cells in its body slots, captures nothing on them, and is told
 * unasked when the relation's View has changed. What this bench proves is
 * that the grid needs nothing more than that; what it is built from — the
 * conditions as data, the header cell — is what a provided layer will offer
 * any relation next, as a decorator or as parts.</p>
 */
public final class GamesCatalogueSpec implements WorkspaceSpec {

    public static final GamesCatalogueSpec INSTANCE;

    static {
        INSTANCE = new GamesCatalogueSpec();
        WorkspaceSpecRegistry.INSTANCE.register(INSTANCE);
    }

    private GamesCatalogueSpec() {}

    @Override public String kind()  { return "gamesCatalogue"; }
    @Override public String title() { return "Games Catalogue"; }

    @Override
    public List<WidgetEntry> widgetEntries() {
        return List.of(
                WidgetEntry.of(GamesWidget.class, WidgetLabel.of("Catalogue"))
                        .withIcon(new WidgetIcon.Emoji("🎮"))
                        .withGroup(WidgetGroup.of("Catalogue"))
                        .withDescription(WidgetDescription.of(
                                "Seven hundred-odd releases, 1990 to 2020, sorted and filtered from header "
                              + "cells that are the relation's own. Click to sort, shift-click to add a key, "
                              + "open a column's funnel to choose rows. Dock two: each keeps its own order.")));
    }

    @Override public List<RibbonItem> ribbonItems() { return List.of(); }
    @Override public List<PartyDecl> parties() { return List.of(); }
    @Override public Map<String, ActionDispatch> actionDispatch() { return Map.of(); }
    @Override public List<WidgetCodecRef> widgetCodecs() { return List.of(); }

    /** Opens with the catalogue on the left; the right is free for a second one. */
    @Override public Arrangement arrangement() {
        return PaneArrangements.COLUMNS.allocate()
                .place(Columns.LEFT, GamesWidget.class)
                .build();
    }
}
