package hue.captains.singapura.js.homing.relgrid;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;

import java.util.List;

/**
 * RFC 0050 · Episode 2 — {@code RelGridLayout}: the LAYOUT branch. Owns the table
 * skeleton, the colgroup, the header band and the wrapper; the slot matrix of
 * one arrangement is {@link RelGridSlotsModule}, and what is laid over the
 * slots is {@link RelGridOverlaysModule}. Addresses everything by
 * {@code (i, j)} and never sees an identity; never touches slot content. It
 * is the keyboard host, paints the cursor and the selection where the facade
 * says, and applies column widths in their positional form. Every element it
 * mints is on the grid's DomOpsParty branch, and its looks are typed
 * ({@link RelGridStyles}) under a prefix the live grid does not use, so the
 * two can share a page.
 */
public record RelGridLayoutModule() implements DomModule<RelGridLayoutModule> {

    public record RelGridLayout() implements Exportable._Constant<RelGridLayoutModule> {}

    public static final RelGridLayoutModule INSTANCE = new RelGridLayoutModule();

    @Override
    public ImportsFor<RelGridLayoutModule> imports() {
        return ImportsFor.<RelGridLayoutModule>builder()
                .add(new ModuleImports<>(List.of(new RelGridHeaderDragModule.RelGridHeaderDrag()), RelGridHeaderDragModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new RelGridSlotsModule.RelGridSlots()), RelGridSlotsModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new RelGridOverlaysModule.RelGridOverlays()), RelGridOverlaysModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new RelGridRevealModule.relGridRevealSlot(), new RelGridRevealModule.relGridHasKeyboard()),
                        RelGridRevealModule.INSTANCE))
                // The looks, typed: every class the layout mints or paints.
                .add(new ModuleImports<>(List.of(
                        new RelGridStyles.hrg_wrap(), new RelGridStyles.hrg_frame(), new RelGridStyles.hrg_lit(),
                        new RelGridStyles.hrg_table(), new RelGridStyles.hrg_fixed(), new RelGridStyles.hrg_ov_ellipsis(),
                        new RelGridStyles.hrg_ov_clip(), new RelGridStyles.hrg_ov_wrap(), new RelGridStyles.hrg_sel(),
                        new RelGridStyles.hrg_cursor(), new RelGridStyles.hrg_deep(), new RelGridStyles.hrg_masked()),
                        RelGridStyles.INSTANCE))
                .build();
    }

    @Override public ExportsOf<RelGridLayoutModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new RelGridLayout()));
    }
}
