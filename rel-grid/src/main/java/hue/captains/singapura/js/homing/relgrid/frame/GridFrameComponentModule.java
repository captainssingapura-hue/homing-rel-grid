package hue.captains.singapura.js.homing.relgrid.frame;

import hue.captains.singapura.js.homing.component.taxonomy.Component;
import hue.captains.singapura.js.homing.component.taxonomy.ComponentModule;
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

import java.util.List;

/**
 * The grid's frame as a component module (RFC 0067, D51): the {@link GridFrame} branch's virtual leaves, which an instance
 * registers beside the house's modules wherever it has the grid - nothing found on the classpath. Its leaves are drawn by
 * designs, never by a style: a base module of it bundles no look. Meta only: the frame is declared in the house's branches,
 * and lists nothing of the house's components.
 */
public record GridFrameComponentModule() implements ComponentModule {

    public static final GridFrameComponentModule INSTANCE = new GridFrameComponentModule();

    /** Every leaf of the frame, in the order a design draws them. */
    @Override public List<Component<?>> components() {
        return List.of(GridEdge.INSTANCE, Lattice.INSTANCE, CursorBorder.INSTANCE, HeldCursorBorder.INSTANCE, LentCursorBorder.INSTANCE,
                       SelectionFill.INSTANCE, HeaderBand.INSTANCE, GutterBand.INSTANCE, EditorAnchor.INSTANCE, Mask.INSTANCE,
                       QuestionPanel.INSTANCE, MergeHost.INSTANCE, ResizeGuide.INSTANCE, CopiedArea.INSTANCE, CopiedBorder.INSTANCE);
    }
}
