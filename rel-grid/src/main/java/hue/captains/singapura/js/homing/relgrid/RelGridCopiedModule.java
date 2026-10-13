package hue.captains.singapura.js.homing.relgrid;

import hue.captains.singapura.js.homing.core.EsModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;

import java.util.List;

/**
 * RFC 0050 · Episode 2, appendix The Copied Range — {@code RelGridCopied}: the copied state, kept with the cursor and
 * the selection - nothing, or the blocks of the last copy the grid wrote, by identity - and where each block stands
 * under the View as it is now, or that one no longer does. Pure: it asks the maps it is handed, and imports nothing.
 */
public record RelGridCopiedModule() implements EsModule<RelGridCopiedModule> {

    public record RelGridCopied() implements Exportable._Constant<RelGridCopiedModule> {}

    public static final RelGridCopiedModule INSTANCE = new RelGridCopiedModule();

    @Override public ImportsFor<RelGridCopiedModule> imports() { return ImportsFor.noImports(); }

    @Override public ExportsOf<RelGridCopiedModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new RelGridCopied()));
    }
}
