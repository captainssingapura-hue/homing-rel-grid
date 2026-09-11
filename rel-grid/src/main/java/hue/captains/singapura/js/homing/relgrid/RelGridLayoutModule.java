package hue.captains.singapura.js.homing.relgrid;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;

import java.util.List;

/**
 * RFC 0050 · Episode 2 — {@code RelGridLayout}: the LAYOUT branch. Owns the table
 * skeleton, the colgroup, the header band and the {@code <td>} slots;
 * addresses everything by {@code (i, j)} and never sees an identity. Never
 * touches slot content — a slot's children are the cells branch's. It is also
 * the capture surface and the keyboard host, and applies column widths in
 * their positional form. Raw DOM inside the primitive, its own stylesheet
 * under a prefix the live grid does not use, so the two can share a page.
 */
public record RelGridLayoutModule() implements DomModule<RelGridLayoutModule> {

    public record RelGridLayout() implements Exportable._Constant<RelGridLayoutModule> {}

    public static final RelGridLayoutModule INSTANCE = new RelGridLayoutModule();

    @Override
    public ImportsFor<RelGridLayoutModule> imports() {
        return ImportsFor.<RelGridLayoutModule>builder()
                .add(new ModuleImports<>(List.of(new RelGridHeaderDragModule.RelGridHeaderDrag()), RelGridHeaderDragModule.INSTANCE))
                .build();
    }

    @Override public ExportsOf<RelGridLayoutModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new RelGridLayout()));
    }
}
