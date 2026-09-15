package hue.captains.singapura.js.homing.reltree;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;

import java.util.List;

/**
 * RFC 0050 · Episode 3-ext1 — {@code RelTreeRows}: the positions of one tree,
 * kept while the count holds and grown or shrunk at the tail; a row is the
 * tree's caret — the typed SVG chevron, parsed into the span the row owns —
 * and whatever the facade places after it. The capture surface for a press, a
 * double-click and a press on the caret, reported by position.
 */
public record RelTreeRowsModule() implements DomModule<RelTreeRowsModule> {
    public record RelTreeRows() implements Exportable._Constant<RelTreeRowsModule> {}
    public static final RelTreeRowsModule INSTANCE = new RelTreeRowsModule();
    @Override public ImportsFor<RelTreeRowsModule> imports() {
        return ImportsFor.<RelTreeRowsModule>builder()
                .add(new ModuleImports<>(List.of(new RelTreeStyles.hrt_row(), new RelTreeStyles.hrt_caret(),
                        new RelTreeStyles.hrt_caret_open(), new RelTreeStyles.hrt_caret_leaf()), RelTreeStyles.INSTANCE))
                .add(new ModuleImports<>(List.of(new RelTreeSvgs.caret()), RelTreeSvgs.INSTANCE))
                .build();
    }
    @Override public ExportsOf<RelTreeRowsModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new RelTreeRows()));
    }
}
