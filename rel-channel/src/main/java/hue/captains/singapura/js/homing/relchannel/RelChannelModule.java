package hue.captains.singapura.js.homing.relchannel;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;

import java.util.List;

/**
 * RFC 0050 · Episode 2 (ext4, ext6) and Episode 3-ext1 — {@code RelChannel}:
 * the ask channel's CORE, the family's. One function the host gave; a
 * notification handed over and never waited on; a question waited on behind
 * the mask with the delay and the hold, one at a time, its answer applied
 * exactly as given. It reaches a presentation only through the SURFACE it is
 * handed — six functions a layout has — and reads no payload: what is asked
 * and what an answer means are a customer's, written over this per
 * presentation.
 *
 * <p>{@link ImportsFor#noImports()} is the point: the grid's channel and the
 * tree's both reach this, and neither reaches the other.</p>
 */
public record RelChannelModule() implements DomModule<RelChannelModule> {

    public record RelChannel() implements Exportable._Class<RelChannelModule> {}

    public static final RelChannelModule INSTANCE = new RelChannelModule();

    @Override public ImportsFor<RelChannelModule> imports() { return ImportsFor.noImports(); }

    @Override public ExportsOf<RelChannelModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new RelChannel()));
    }
}
