package hue.captains.singapura.js.homing.relgrid;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;

import java.util.List;

/**
 * RFC 0050 · Episode 2 — {@code RelGridSlots}: the slot matrix of ONE
 * arrangement — cols, header cells, body and {@code <td>} slots — minted
 * fresh on a sub-branch of the grid's own and dissolved by the next, and the
 * pointer capture on the slots: a click, a double-click, a press-drag, each
 * reported by position with its modifiers and never interpreted. Never
 * touches slot content.
 */
public record RelGridSlotsModule() implements DomModule<RelGridSlotsModule> {

    public record RelGridSlots() implements Exportable._Constant<RelGridSlotsModule> {}

    public static final RelGridSlotsModule INSTANCE = new RelGridSlotsModule();

    @Override
    public ImportsFor<RelGridSlotsModule> imports() {
        return ImportsFor.<RelGridSlotsModule>builder()
                .add(new ModuleImports<>(List.of(new RelGridStyles.hrg_col(), new RelGridStyles.hrg_th(), new RelGridStyles.hrg_sticky(), new RelGridStyles.hrg_td()),
                        RelGridStyles.INSTANCE))
                .build();
    }

    @Override public ExportsOf<RelGridSlotsModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new RelGridSlots()));
    }
}
