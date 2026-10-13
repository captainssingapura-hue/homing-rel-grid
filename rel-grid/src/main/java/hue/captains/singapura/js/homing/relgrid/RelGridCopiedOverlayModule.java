package hue.captains.singapura.js.homing.relgrid;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.relgrid.frame.GridFrameModule;

import java.util.List;

/**
 * RFC 0050 · Episode 2, appendix The Copied Range — {@code RelGridCopiedOverlay}: the copied state shown, an overlay
 * over each block the grid last copied. Two parts, each one element wearing its leaf of the frame: the area, a box
 * over the block (CopiedArea), and the border, a vector rectangle round it (CopiedBorder). Placed, stacked and
 * re-placed as the grid's other overlays are; what each looks like is the design's.
 */
public record RelGridCopiedOverlayModule() implements DomModule<RelGridCopiedOverlayModule> {

    public record RelGridCopiedOverlay() implements Exportable._Constant<RelGridCopiedOverlayModule> {}

    public static final RelGridCopiedOverlayModule INSTANCE = new RelGridCopiedOverlayModule();

    @Override
    public ImportsFor<RelGridCopiedOverlayModule> imports() {
        return ImportsFor.<RelGridCopiedOverlayModule>builder()
                // the frame's leaves, by which the design it is made with is asked to wear one
                .add(new ModuleImports<>(List.of(new GridFrameModule.GRID_FRAME()), GridFrameModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new RelGridStyles.hrg_copied(), new RelGridStyles.hrg_copied_line(),
                        new RelGridStyles.hrg_copied_rect()), RelGridStyles.INSTANCE))
                .build();
    }

    @Override public ExportsOf<RelGridCopiedOverlayModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new RelGridCopiedOverlay()));
    }
}
