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
 * <p>Imports nothing and holds nothing. The next iterations — punctuation
 * sharing a slot, the squeezed leading and trailing columns — are this
 * function growing, and nothing else changing.</p>
 */
public record HanLayout() implements DomModule<HanLayout> {

    public record hanLayout() implements Exportable._Constant<HanLayout> {}

    public static final HanLayout INSTANCE = new HanLayout();

    @Override public ImportsFor<HanLayout> imports() { return ImportsFor.noImports(); }

    @Override public ExportsOf<HanLayout> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new hanLayout()));
    }
}
