package hue.captains.singapura.js.homing.relgrid.group;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.relgrid.RelGridModule;
import hue.captains.singapura.js.homing.relgrid.RelGridStyles;

import java.util.List;

/**
 * RFC 0050 · Episode 2 — {@code RelGridGroupWalk}: ONE CURSOR for the group,
 * and the walk between its stops. The active member is the one whose table
 * holds the focus, observed at the root; every other member is painted
 * dormant. Tab walks fence, table, fence, table … and wraps; a fence stop is
 * the fence itself, wearing the cursor; Enter on it presses its first control;
 * an arrow at a table's edge, reported by the table, steps one stop over. It
 * reaches into no table: a member is entered through its own selectCell.
 */
public record RelGridGroupWalkModule() implements DomModule<RelGridGroupWalkModule> {

    public record RelGridGroupWalk() implements Exportable._Constant<RelGridGroupWalkModule> {}

    public static final RelGridGroupWalkModule INSTANCE = new RelGridGroupWalkModule();

    @Override
    public ImportsFor<RelGridGroupWalkModule> imports() {
        return ImportsFor.<RelGridGroupWalkModule>builder()
                .add(new ModuleImports<>(List.of(new RelGridGroupStyles.hrg_active(), new RelGridGroupStyles.hrg_dormant(),
                        new RelGridGroupStyles.hrg_fence_cursor(), new RelGridGroupStyles.hrg_on_fence()), RelGridGroupStyles.INSTANCE))
                .build();
    }

    @Override public ExportsOf<RelGridGroupWalkModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new RelGridGroupWalk()));
    }
}
