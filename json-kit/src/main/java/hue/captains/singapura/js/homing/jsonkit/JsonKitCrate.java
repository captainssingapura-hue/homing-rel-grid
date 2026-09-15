package hue.captains.singapura.js.homing.jsonkit;

import hue.captains.singapura.js.homing.core.Crate;
import hue.captains.singapura.js.homing.core.CrateEntry;
import hue.captains.singapura.js.homing.relgrid.protocol.RelGridProtocolCrate;
import hue.captains.singapura.js.homing.reltree.RelTreeCrate;

import java.util.List;

/**
 * RFC 0044 — the {@link Crate} for {@code json-kit}: the JSON viewer on the
 * tree view, an out-of-the-box offering that is <b>domain-side code</b>. It
 * requires the tree it composes and the protocol it answers through — a
 * JSON relation answers {@code RelTreeUnfold} with a {@code RelTreeView} —
 * and nothing of the grid's table.
 *
 * <p>Not value-free, on purpose: a JSON value is exactly what a relation holds
 * and the tree never sees. What the kit ships is one implementation each of
 * the tree relation and the tree cell contracts, and a viewer over them; the
 * sweep grades it under the same rule set as everything else, against an
 * empty ledger.</p>
 */
public final class JsonKitCrate implements Crate {

    public static final JsonKitCrate INSTANCE = new JsonKitCrate();

    private JsonKitCrate() {}

    @Override public String name() { return "json-kit"; }

    @Override public List<Crate> requires() {
        return List.of(RelTreeCrate.INSTANCE, RelGridProtocolCrate.INSTANCE);
    }

    @Override
    public List<CrateEntry> entries() {
        return List.of();
    }
}
