package hue.captains.singapura.js.homing.relgrid;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.relgrid.protocol.RelGridProtocolModule;

import java.util.List;

/**
 * RFC 0050 · Episode 2 — {@code RelGridChannel}: the ASK CHANNEL and its three
 * customers. A notification (the selection) is handed over and never waited
 * on; a question (copy, the view handover) is waited on behind the mask, with
 * the delay and the hold, and its answer applied exactly as given. The payloads
 * are the protocol's generated value objects, which is all this imports.
 */
public record RelGridChannelModule() implements DomModule<RelGridChannelModule> {

    public record RelGridChannel() implements Exportable._Constant<RelGridChannelModule> {}

    public static final RelGridChannelModule INSTANCE = new RelGridChannelModule();

    @Override
    public ImportsFor<RelGridChannelModule> imports() {
        return ImportsFor.<RelGridChannelModule>builder()
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
