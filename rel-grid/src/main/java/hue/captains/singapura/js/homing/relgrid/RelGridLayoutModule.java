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
 * their positional form. Every element it mints is on the grid's DomOpsParty
 * branch, and its looks are typed ({@link RelGridStyles}) under a prefix the
 * live grid does not use, so the two can share a page.
 */
public record RelGridLayoutModule() implements DomModule<RelGridLayoutModule> {

    public record RelGridLayout() implements Exportable._Constant<RelGridLayoutModule> {}

    public static final RelGridLayoutModule INSTANCE = new RelGridLayoutModule();

    @Override
    public ImportsFor<RelGridLayoutModule> imports() {
        return ImportsFor.<RelGridLayoutModule>builder()
                .add(new ModuleImports<>(List.of(new RelGridHeaderDragModule.RelGridHeaderDrag()), RelGridHeaderDragModule.INSTANCE))
                // The looks, typed: every class the layout mints or paints.
                .add(new ModuleImports<>(List.of(
                        new RelGridStyles.hrg_wrap(), new RelGridStyles.hrg_frame(), new RelGridStyles.hrg_lit(),
                        new RelGridStyles.hrg_table(), new RelGridStyles.hrg_th(), new RelGridStyles.hrg_col(),
                        new RelGridStyles.hrg_td(), new RelGridStyles.hrg_merge(), new RelGridStyles.hrg_edit(),
                        new RelGridStyles.hrg_mask(), new RelGridStyles.hrg_panel(),
                        new RelGridStyles.hrg_fixed(), new RelGridStyles.hrg_ov_ellipsis(), new RelGridStyles.hrg_ov_clip(),
                        new RelGridStyles.hrg_ov_wrap(), new RelGridStyles.hrg_lead(), new RelGridStyles.hrg_covered(),
                        new RelGridStyles.hrg_group_end(), new RelGridStyles.hrg_sel(), new RelGridStyles.hrg_cursor(),
                        new RelGridStyles.hrg_deep(), new RelGridStyles.hrg_masked()), RelGridStyles.INSTANCE))
                .build();
    }

    @Override public ExportsOf<RelGridLayoutModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new RelGridLayout()));
    }
}
