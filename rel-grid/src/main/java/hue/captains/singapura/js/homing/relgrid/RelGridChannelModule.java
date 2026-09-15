package hue.captains.singapura.js.homing.relgrid;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.relchannel.RelChannelModule;
import hue.captains.singapura.js.homing.relgrid.protocol.RelGridProtocolModule;

import java.util.List;

/**
 * RFC 0050 · Episode 2 — {@code RelGridChannel}: the grid's three CUSTOMERS of
 * the ask channel, over the core in {@code rel-channel}. A notification (the
 * selection) is handed over and never waited on; a question (copy, the view
 * handover) is waited on behind the mask, with the delay and the hold, and its
 * answer applied exactly as given — the waiting, the mask and the one-at-a-time
 * are the core's. The payloads are the protocol's generated value objects.
 */
public record RelGridChannelModule() implements DomModule<RelGridChannelModule> {

    public record RelGridChannel() implements Exportable._Constant<RelGridChannelModule> {}

    public static final RelGridChannelModule INSTANCE = new RelGridChannelModule();

    @Override
    public ImportsFor<RelGridChannelModule> imports() {
        return ImportsFor.<RelGridChannelModule>builder()
                .add(new ModuleImports<>(List.of(new RelChannelModule.RelChannel()), RelChannelModule.INSTANCE))
                .add(new ModuleImports<>(
                        List.of(new RelGridProtocolModule.RelGridRange(),
                                new RelGridProtocolModule.RelGridSelectionChanged(),
                                new RelGridProtocolModule.RelGridBlock(),
                                new RelGridProtocolModule.RelGridCopyRequested(),
                                new RelGridProtocolModule.RelGridClipboardContent(),
                                new RelGridProtocolModule.RelGridViewHandover(),
                                new RelGridProtocolModule.RelGridView()),
                        RelGridProtocolModule.INSTANCE))
                .build();
    }

    @Override public ExportsOf<RelGridChannelModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new RelGridChannel()));
    }
}
