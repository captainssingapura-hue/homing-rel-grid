package hue.captains.singapura.js.homing.relgrid.group;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.relgrid.RelGridModule;
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
                .build();
    }

    @Override public ExportsOf<RelGridGroupModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new RelGridGroup()));
    }
}
