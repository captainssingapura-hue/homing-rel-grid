package hue.captains.singapura.js.homing.relgrid.frame;

import hue.captains.singapura.js.homing.component.taxonomy.Component;
import hue.captains.singapura.js.homing.component.taxonomy.TaxonomyExtension;
import hue.captains.singapura.js.homing.component.taxonomy.TaxonomySource;
import hue.captains.singapura.js.homing.relgrid.frame.GridFrame.CopiedArea;
import hue.captains.singapura.js.homing.relgrid.frame.GridFrame.CopiedBorder;
import hue.captains.singapura.js.homing.relgrid.frame.GridFrame.CursorBorder;
import hue.captains.singapura.js.homing.relgrid.frame.GridFrame.EditorAnchor;
import hue.captains.singapura.js.homing.relgrid.frame.GridFrame.GridEdge;
import hue.captains.singapura.js.homing.relgrid.frame.GridFrame.GutterBand;
import hue.captains.singapura.js.homing.relgrid.frame.GridFrame.HeaderBand;
import hue.captains.singapura.js.homing.relgrid.frame.GridFrame.HeldCursorBorder;
import hue.captains.singapura.js.homing.relgrid.frame.GridFrame.Lattice;
import hue.captains.singapura.js.homing.relgrid.frame.GridFrame.LentCursorBorder;
import hue.captains.singapura.js.homing.relgrid.frame.GridFrame.Mask;
import hue.captains.singapura.js.homing.relgrid.frame.GridFrame.MergeHost;
import hue.captains.singapura.js.homing.relgrid.frame.GridFrame.QuestionPanel;
import hue.captains.singapura.js.homing.relgrid.frame.GridFrame.ResizeGuide;
import hue.captains.singapura.js.homing.relgrid.frame.GridFrame.SelectionFill;
import hue.captains.singapura.js.homing.ui.taxonomy.HouseTaxonomy;

import java.util.List;

/**
 * The grid's frame, as the grid adds it to the house's taxonomy: the {@link GridFrame} branch and its virtual leaves,
 * in the tree if and only if the grid is on the classpath. Meta only: the grid takes the house's taxonomy to declare in,
 * and nothing of the house's components.
 */
public record GridFrameExtension() implements TaxonomyExtension {

    public static final GridFrameExtension INSTANCE = new GridFrameExtension();

    @Override public Class<? extends TaxonomySource> extending() { return HouseTaxonomy.class; }

    /** Every leaf of the frame, in the order a design draws them. */
    @Override public List<Component<?>> components() {
        return List.of(GridEdge.INSTANCE, Lattice.INSTANCE, CursorBorder.INSTANCE, HeldCursorBorder.INSTANCE, LentCursorBorder.INSTANCE,
                       SelectionFill.INSTANCE, HeaderBand.INSTANCE, GutterBand.INSTANCE, EditorAnchor.INSTANCE, Mask.INSTANCE,
                       QuestionPanel.INSTANCE, MergeHost.INSTANCE, ResizeGuide.INSTANCE, CopiedArea.INSTANCE, CopiedBorder.INSTANCE);
    }
}
