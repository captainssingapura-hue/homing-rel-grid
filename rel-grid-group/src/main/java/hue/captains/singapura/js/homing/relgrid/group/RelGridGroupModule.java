package hue.captains.singapura.js.homing.relgrid.group;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.relgrid.RelGridModule;
import hue.captains.singapura.js.homing.relgrid.RelGridStyles;
import hue.captains.singapura.js.homing.relgrid.protocol.RelGridProtocolModule;

import java.util.List;

/**
 * RFC 0050 · Episode 2 — {@code RelGridGroup}: an ordered list of tables
 * stacked down one column, each an ordinary {@code RelGrid} that does not know
 * it is in one, with a <b>fence</b> — a slot the domain fills — between every
 * two and around the ends.
 *
 * <p>It imports the grid and the protocol's group kind, and the grid imports
 * nothing of it: that one-way dependency is the whole guarantee. Members are
 * identities; everything inside a member is the member's own, wired as it
 * would be alone. What the group shares is column geometry, kept level
 * through the members' public verbs and never by reaching into a layout;
 * what it holds of its own is which members are folded, told by the host's
 * verbs or by the domain through {@code tell}.</p>
 */
public record RelGridGroupModule() implements DomModule<RelGridGroupModule> {

    public record RelGridGroup() implements Exportable._Constant<RelGridGroupModule> {}

    public static final RelGridGroupModule INSTANCE = new RelGridGroupModule();

    @Override
    public ImportsFor<RelGridGroupModule> imports() {
        return ImportsFor.<RelGridGroupModule>builder()
                .add(new ModuleImports<>(List.of(new RelGridModule.RelGrid()), RelGridModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new RelGridProtocolModule.RelGridGroupFold()), RelGridProtocolModule.INSTANCE))
                // The looks, typed: the group's own, and the grid's hrg_lit so a fence lights under the focus.
                .add(new ModuleImports<>(List.of(
                        new RelGridGroupStyles.hrg_group(), new RelGridGroupStyles.hrg_member(), new RelGridGroupStyles.hrg_group_header(),
                        new RelGridGroupStyles.hrg_fence(), new RelGridGroupStyles.hrg_fence_empty(), new RelGridGroupStyles.hrg_folded(),
                        new RelGridGroupStyles.hrg_fence_folded(), new RelGridGroupStyles.hrg_active(), new RelGridGroupStyles.hrg_dormant(),
                        new RelGridGroupStyles.hrg_fence_cursor(), new RelGridGroupStyles.hrg_on_fence()), RelGridGroupStyles.INSTANCE))
                .add(new ModuleImports<>(List.of(new RelGridStyles.hrg_lit()), RelGridStyles.INSTANCE))
                .build();
    }

    @Override public ExportsOf<RelGridGroupModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new RelGridGroup()));
    }
}
