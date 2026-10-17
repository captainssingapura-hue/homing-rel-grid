package hue.captains.singapura.js.homing.relgrid.frame;

import hue.captains.singapura.js.homing.core.EsModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleNameResolver;
import hue.captains.singapura.js.homing.core.SelfContent;
import hue.captains.singapura.js.homing.design.semantic.LocalStyleLeaves;
import hue.captains.singapura.js.homing.design.styling.InstalledStyling;

import java.util.List;

/**
 * The grid's frame as a page has it: {@code GRID_FRAME}, one frozen constant per leaf of the {@link GridFrame} branch,
 * generated from the installed instance's taxonomy - by which the grid asks the local style it is made with to wear a
 * leaf on an element, never by a string. Each carries its branch, so a local style of another branch refuses it, and the
 * grid refuses a local style that is not of this one.
 */
public record GridFrameModule() implements EsModule<GridFrameModule>, SelfContent {

    public static final GridFrameModule INSTANCE = new GridFrameModule();

    /** Every leaf of the frame: {@code GRID_FRAME.LATTICE}. */
    public record GRID_FRAME() implements Exportable._Constant<GridFrameModule> {}

    @Override public ImportsFor<GridFrameModule> imports() { return ImportsFor.noImports(); }

    @Override public ExportsOf<GridFrameModule> exports() { return new ExportsOf<>(INSTANCE, List.of(new GRID_FRAME())); }

    @Override public List<String> selfContent(ModuleNameResolver resolver) {
        return LocalStyleLeaves.INSTANCE.lines(InstalledStyling.get().taxonomy(), GridFrame.INSTANCE);
    }
}
