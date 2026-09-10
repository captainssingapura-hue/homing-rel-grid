package hue.captains.singapura.js.homing.relgrid.protocol;

import hue.captains.singapura.js.homing.core.Crate;
import hue.captains.singapura.js.homing.core.CrateEntry;
import hue.captains.singapura.js.homing.core.StandardJsModuleType;

import java.util.List;

/**
 * RFC 0044 — the {@link Crate} for {@code rel-grid-protocol}. One generated
 * module, pure logic, requiring nothing: a protocol that depended on something
 * would not be one both sides could hold.
 */
public final class RelGridProtocolCrate implements Crate {

    public static final RelGridProtocolCrate INSTANCE = new RelGridProtocolCrate();

    private RelGridProtocolCrate() {}

    @Override public String name() { return "rel-grid-protocol"; }

    @Override public List<Crate> requires() { return List.of(); }

    @Override
    public List<CrateEntry> entries() {
        return List.of(CrateEntry.of(RelGridProtocolModule.INSTANCE, StandardJsModuleType.PURE_LOGIC));
    }
}
