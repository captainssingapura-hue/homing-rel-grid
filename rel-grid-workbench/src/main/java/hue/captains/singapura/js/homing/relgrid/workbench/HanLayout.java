package hue.captains.singapura.js.homing.relgrid.workbench;

import hue.captains.singapura.js.homing.core.DomModule;
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
 * half-square. {@code hanIsPunct} and {@code hanIsOpener} are the one
 * definition of what a mark is, and the cell imports them rather than
 * deciding for itself.</p>
 *
 * <p>Imports nothing and holds nothing.</p>
 */
public record HanLayout() implements DomModule<HanLayout> {

    public record hanLayout()   implements Exportable._Constant<HanLayout> {}
    public record hanIsPunct()  implements Exportable._Constant<HanLayout> {}
    public record hanIsOpener() implements Exportable._Constant<HanLayout> {}

    public static final HanLayout INSTANCE = new HanLayout();

    @Override public ImportsFor<HanLayout> imports() { return ImportsFor.noImports(); }

    @Override public ExportsOf<HanLayout> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new hanLayout(), new hanIsPunct(), new hanIsOpener()));
    }
}
