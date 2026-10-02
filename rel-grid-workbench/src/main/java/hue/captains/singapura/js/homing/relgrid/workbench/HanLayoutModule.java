package hue.captains.singapura.js.homing.relgrid.workbench;

import hue.captains.singapura.js.homing.core.EsModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;

import java.util.List;

/**
 * The Han Article bench's rendering engine: a pure function from text to rows
 * of square slots, a fixed number per row, each row with a leading and a
 * trailing <b>half-square</b>. One character, one square; a newline ends the
 * row; two punctuation marks share a square; a closing mark that would start
 * a line is squeezed into the previous row's trailing half-square; an opening
 * bracket that would end a line leads the next row from its leading
 * half-square. {@code HanLayout.isPunct} and {@code HanLayout.isOpener} are the one
 * definition of what a mark is, and the cell imports them rather than
 * deciding for itself.</p>
 *
 * <p>Imports nothing and holds nothing.</p>
 */
public record HanLayoutModule() implements EsModule<HanLayoutModule> {

    public record HanLayout() implements Exportable._Class<HanLayoutModule> {}

    public static final HanLayoutModule INSTANCE = new HanLayoutModule();

    @Override public ImportsFor<HanLayoutModule> imports() { return ImportsFor.noImports(); }

    @Override public ExportsOf<HanLayoutModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new HanLayout()));
    }
}
