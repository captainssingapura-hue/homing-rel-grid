package hue.captains.singapura.js.homing.relchannel;

import hue.captains.singapura.js.homing.core.Crate;
import hue.captains.singapura.js.homing.core.CrateEntry;

import java.util.List;

/**
 * RFC 0044 — the {@link Crate} for {@code rel-channel}. One module, a
 * consumer under the full DOM-owner discipline — it mounts a mask through a
 * surface — and {@link #requires()} empty on purpose: the crate every
 * presentation's channel stands on may reach none of them.
 */
public final class RelChannelCrate implements Crate {

    public static final RelChannelCrate INSTANCE = new RelChannelCrate();

    private RelChannelCrate() {}

    @Override public String name() { return "rel-channel"; }

    @Override public List<Crate> requires() { return List.of(); }

    @Override
    public List<CrateEntry> entries() {
        return List.of(CrateEntry.of(RelChannelModule.INSTANCE));
    }
}
