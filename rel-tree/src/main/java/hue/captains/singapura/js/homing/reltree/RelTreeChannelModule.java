package hue.captains.singapura.js.homing.reltree;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;

import java.util.List;
import hue.captains.singapura.js.homing.relchannel.RelChannelModule;
import hue.captains.singapura.js.homing.relgrid.protocol.RelGridProtocolModule;

/**
 * RFC 0050 · Episode 3-ext1 — {@code RelTreeChannel}: the tree's customers of the
 * ask channel, over the core in {@code rel-channel}. Unfold and fold are
 * questions answered with a {@code RelTreeView} or nothing; the cursor and an
 * activation are notifications. The payloads are the protocol's generated
 * value objects, which with the core is all this imports.
 */
public record RelTreeChannelModule() implements DomModule<RelTreeChannelModule> {
    public record RelTreeChannel() implements Exportable._Constant<RelTreeChannelModule> {}
    public static final RelTreeChannelModule INSTANCE = new RelTreeChannelModule();
    @Override public ImportsFor<RelTreeChannelModule> imports() {
        return ImportsFor.<RelTreeChannelModule>builder()
                .add(new ModuleImports<>(List.of(new RelChannelModule.RelChannel()), RelChannelModule.INSTANCE))
                .add(new ModuleImports<>(
                        List.of(new RelGridProtocolModule.RelTreeView(),
                                new RelGridProtocolModule.RelTreeUnfold(),
                                new RelGridProtocolModule.RelTreeFold(),
                                new RelGridProtocolModule.RelTreeCursorChanged(),
                                new RelGridProtocolModule.RelTreeActivated()),
                        RelGridProtocolModule.INSTANCE))
                .build();
    }
    @Override public ExportsOf<RelTreeChannelModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new RelTreeChannel()));
    }
}
