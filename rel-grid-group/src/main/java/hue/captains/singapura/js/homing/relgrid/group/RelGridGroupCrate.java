package hue.captains.singapura.js.homing.relgrid.group;

import hue.captains.singapura.js.homing.core.Crate;
import hue.captains.singapura.js.homing.core.CrateEntry;
import hue.captains.singapura.js.homing.core.StandardJsModuleType;
import hue.captains.singapura.js.homing.relgrid.RelGridCrate;
import hue.captains.singapura.js.homing.relgrid.protocol.RelGridProtocolCrate;

import java.util.List;

/**
 * RFC 0044 — the {@link Crate} for {@code rel-grid-group}. One module, and
 * {@link #requires()} names the grid's crate and the protocol's — the group
 * speaks the protocol's group kinds — and nothing else: the group is built on
 * the table, and the table's crate requires nothing of this one.
 */
public final class RelGridGroupCrate implements Crate {

    public static final RelGridGroupCrate INSTANCE = new RelGridGroupCrate();

    private RelGridGroupCrate() {}

    @Override public String name() { return "rel-grid-group"; }

    @Override public List<Crate> requires() { return List.of(RelGridCrate.INSTANCE, RelGridProtocolCrate.INSTANCE); }

    @Override
    public List<CrateEntry> entries() {
        return List.of(CrateEntry.of(RelGridGroupModule.INSTANCE),     // undeclared = CONSUMER: a component, not a pane
                       CrateEntry.of(RelGridGroupStyles.INSTANCE, StandardJsModuleType.GENERATED_CSS));
    }
}
