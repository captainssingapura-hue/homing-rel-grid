package hue.captains.singapura.js.homing.relgrid.workbench;

import hue.captains.singapura.js.homing.workspace.shell.ActionDispatch;
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
 * Han Article ({@code ws_kind=hanArticle}): a WYSIWYG Chinese article, edited
 * and displayed through the Relation Grid, every character in a strictly
 * square cell, nine to a row.
 *
 * <p>The article is edited as plain text and rendered as squares: one glyph a
 * square, two punctuation marks to a square, a hard-coded poem as the seed.
 * The editor pane has the text above the rendering; displays show the
 * rendering alone, and follow through the store. What this bench is going to
 * ask of the grid comes next: the two half-width columns, leading and
 * trailing, that a mark at the end of a line is squeezed into rather than
 * starting the next line with it.</p>
 */
public final class HanArticleSpec implements WorkspaceSpec {

    public static final HanArticleSpec INSTANCE;

    static {
        INSTANCE = new HanArticleSpec();
        WorkspaceSpecRegistry.INSTANCE.register(INSTANCE);
    }

    private HanArticleSpec() {}

    @Override public String kind()  { return "hanArticle"; }
    @Override public String title() { return "Han Article · 方格"; }

    @Override
    public List<WidgetEntry> widgetEntries() {
        return List.of(
                WidgetEntry.of(HanEditorWidget.class, WidgetLabel.of("Editor"))
                        .withIcon(new WidgetIcon.Emoji("✍️"))
                        .withGroup(WidgetGroup.of("Editors"))
                        .withDescription(WidgetDescription.of(
                                "The article as plain text, and rendered beneath it: nine squares to a "
                              + "row, one glyph each, two marks to a square. Type, and every display "
                              + "re-flows. One instance.")),
                WidgetEntry.of(HanDisplayWidget.class, WidgetLabel.of("Display"))
                        .withIcon(new WidgetIcon.Emoji("📜"))
                        .withGroup(WidgetGroup.of("Replicas"))
                        .withDescription(WidgetDescription.of(
                                "The same article, read-only, over the same store. Dock several; "
                              + "every one moves on every commit, and none of their grids is told.")),
                WidgetEntry.of(HanStressWidget.class, WidgetLabel.of("Stress"))
                        .withIcon(new WidgetIcon.Emoji("🧪"))
                        .withGroup(WidgetGroup.of("Stress"))
                        .withDescription(WidgetDescription.of(
                                "Hypothetical text over the same engine, header shown, every column "
                              + "resizable down to 12px. Measures every merged host against the slots "
                              + "beneath it after each change and reports the drift."))
        );
    }

    @Override public List<RibbonItem> ribbonItems() { return List.of(); }
    @Override public List<PartyDecl> parties() { return List.of(); }
    @Override public Map<String, ActionDispatch> actionDispatch() { return Map.of(); }
    @Override public List<WidgetCodecRef> widgetCodecs() { return List.of(); }

    /** Nothing pinned — displays must stay spawnable many times. */
    @Override public List<String> pinnedSpawns() { return List.of(); }
}
