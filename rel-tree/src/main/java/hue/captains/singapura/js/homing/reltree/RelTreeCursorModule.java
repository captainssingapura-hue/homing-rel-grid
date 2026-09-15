package hue.captains.singapura.js.homing.reltree;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;

import java.util.List;

/**
 * RFC 0050 · Episode 3-ext1 — {@code RelTreeCursor}: the current node, an
 * identity with its row as the fallback; tells its cell 'none' or 'shallow',
 * paints itself through the layout, lands on the node that folded when the
 * tree asked for the fold that took its row.
 */
public record RelTreeCursorModule() implements DomModule<RelTreeCursorModule> {
    public record RelTreeCursor() implements Exportable._Constant<RelTreeCursorModule> {}
    public static final RelTreeCursorModule INSTANCE = new RelTreeCursorModule();
    @Override public ImportsFor<RelTreeCursorModule> imports() { return ImportsFor.noImports(); }
    @Override public ExportsOf<RelTreeCursorModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new RelTreeCursor()));
    }
}
