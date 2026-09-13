package hue.captains.singapura.js.homing.relgrid;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;

import java.util.List;

/**
 * RFC 0050 · Episode 2 — {@code RelGridWindow}: the relation's own seam,
 * {@code view(intent)}, asked to move. One root: the View is answered, never
 * listed, and {@code view({ by: n })} answers the View n rows on — or nothing,
 * the rows staying. The one place the grid asks the relation anything after
 * construction, and it asks for keys; the facade presents the answer on the
 * same slots. Imports nothing: it is the ask, and the maps it is handed.
 */
public record RelGridWindowModule() implements DomModule<RelGridWindowModule> {

    public record RelGridWindow() implements Exportable._Constant<RelGridWindowModule> {}

    public static final RelGridWindowModule INSTANCE = new RelGridWindowModule();

    @Override public ImportsFor<RelGridWindowModule> imports() { return ImportsFor.noImports(); }

    @Override public ExportsOf<RelGridWindowModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new RelGridWindow()));
    }
}
