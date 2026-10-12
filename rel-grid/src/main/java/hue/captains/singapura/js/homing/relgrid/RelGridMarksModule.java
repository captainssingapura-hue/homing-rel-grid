package hue.captains.singapura.js.homing.relgrid;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.relgrid.frame.GridFrameModule;

import java.util.List;

/**
 * RFC 0050 · Episode 2 — the grid's marks: where the user is and what they have chosen, painted on the slots at the
 * facade's direction. The cursor's slot wears the frame's leaf for where the keys are and swaps it as they move; the
 * selection is a fact and an {@code aria-selected} the design's selection fill shows by.
 */
public record RelGridMarksModule() implements DomModule<RelGridMarksModule> {

    public record RelGridMarks() implements Exportable._Constant<RelGridMarksModule> {}

    public static final RelGridMarksModule INSTANCE = new RelGridMarksModule();

    @Override
    public ImportsFor<RelGridMarksModule> imports() {
        return ImportsFor.<RelGridMarksModule>builder()
                // the frame's leaves, by which the design it is made with is asked to wear one
                .add(new ModuleImports<>(List.of(new GridFrameModule.GRID_FRAME()), GridFrameModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new RelGridStyles.hrg_sel(), new RelGridStyles.hrg_cursor()), RelGridStyles.INSTANCE))
                .build();
    }

    @Override public ExportsOf<RelGridMarksModule> exports() { return new ExportsOf<>(INSTANCE, List.of(new RelGridMarks())); }
}
