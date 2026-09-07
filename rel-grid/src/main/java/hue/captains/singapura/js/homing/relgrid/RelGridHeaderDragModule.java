package hue.captains.singapura.js.homing.relgrid;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;

import java.util.List;

/**
 * RFC 0050 · Episode 2 — {@code RelGridHeaderDrag}: the header's resize gesture.
 * A staged drag on an 8px handle — a guide line follows the pointer, one
 * request {@code (j, px)} is reported on release, Escape abandons. Positional
 * only; the facade turns {@code j} into a column, bounds the request and holds
 * it. The gesture mints; it never applies.
 */
public record RelGridHeaderDragModule() implements DomModule<RelGridHeaderDragModule> {

    public record RelGridHeaderDrag() implements Exportable._Constant<RelGridHeaderDragModule> {}

    public static final RelGridHeaderDragModule INSTANCE = new RelGridHeaderDragModule();

    @Override public ImportsFor<RelGridHeaderDragModule> imports() { return ImportsFor.noImports(); }

    @Override public ExportsOf<RelGridHeaderDragModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new RelGridHeaderDrag()));
    }
}
