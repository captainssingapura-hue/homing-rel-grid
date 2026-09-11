package hue.captains.singapura.js.homing.relgrid.selection;

import hue.captains.singapura.js.homing.core.Crate;
import hue.captains.singapura.js.homing.core.CrateEntry;
import hue.captains.singapura.js.homing.core.StandardJsModuleType;

import java.util.List;

/**
 * RFC 0044 — the {@link Crate} for {@code rel-grid-selection}. One module,
 * pure logic, and {@link #requires()} is empty on purpose: a crate that
 * requires nothing cannot acquire an import by accident, which is the
 * structural half of map 5's law 215.
 */
public final class RelGridSelectionCrate implements Crate {

    public static final RelGridSelectionCrate INSTANCE = new RelGridSelectionCrate();

    private RelGridSelectionCrate() {}

    @Override public String name() { return "rel-grid-selection"; }

    @Override public List<Crate> requires() { return List.of(); }

    @Override
    public List<CrateEntry> entries() {
        return List.of(
                CrateEntry.of(RelGridSelectionModule.INSTANCE, StandardJsModuleType.PURE_LOGIC));
    }
}
