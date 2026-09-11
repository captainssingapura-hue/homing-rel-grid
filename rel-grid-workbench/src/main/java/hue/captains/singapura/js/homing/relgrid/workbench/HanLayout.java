package hue.captains.singapura.js.homing.relgrid.workbench;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;

import java.util.List;

/**
 * The Han Article bench's rendering engine, first iteration: a pure function
 * from text to rows of square slots, a fixed number per row. One character,
 * one slot; a newline ends the row; there is always somewhere to type. Every
 * slot knows where in the text a commit to it goes, so the relation can splice
 * the store without knowing anything about layout itself.
 *
 * <p>Second iteration: <b>two punctuation marks share a slot</b> — a mark that
 * follows a lone mark joins it, in the same row, and that is the whole rule;
 * where a mark at the end of a line should go instead is the next iteration.
 * {@code hanIsPunct} is the one definition of what a mark is, and the cell
 * imports it rather than deciding for itself.</p>
 *
 * <p>Imports nothing and holds nothing.</p>
 */
public record HanLayout() implements DomModule<HanLayout> {

    public record hanLayout()  implements Exportable._Constant<HanLayout> {}
    public record hanIsPunct() implements Exportable._Constant<HanLayout> {}

    public static final HanLayout INSTANCE = new HanLayout();

    @Override public ImportsFor<HanLayout> imports() { return ImportsFor.noImports(); }

    @Override public ExportsOf<HanLayout> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new hanLayout(), new hanIsPunct()));
    }
}
