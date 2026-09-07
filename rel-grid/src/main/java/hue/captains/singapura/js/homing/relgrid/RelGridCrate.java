package hue.captains.singapura.js.homing.relgrid;

import hue.captains.singapura.js.homing.core.Crate;
import hue.captains.singapura.js.homing.core.CrateEntry;
import hue.captains.singapura.js.homing.core.StandardJsModuleType;

import java.util.List;

/**
 * RFC 0044 — the {@link Crate} for {@code homing-rel-grid}. One crate
 * per module; every served JS module declared with its type, so conformance is
 * ambient from the first line.
 */
public final class RelGridCrate implements Crate {

    public static final RelGridCrate INSTANCE = new RelGridCrate();

    private RelGridCrate() {}

    @Override public String name() { return "homing-rel-grid"; }

    @Override public List<Crate> requires() { return List.of(); }

    @Override
    public List<CrateEntry> entries() {
        return List.of(
                CrateEntry.of(RelGridViewMapsModule.INSTANCE,     StandardJsModuleType.PURE_LOGIC),
                CrateEntry.of(RelGridHeaderDragModule.INSTANCE,   StandardJsModuleType.PRIMITIVE),
                CrateEntry.of(RelGridLayoutModule.INSTANCE,       StandardJsModuleType.PRIMITIVE),
                CrateEntry.of(RelGridCellsModule.INSTANCE,        StandardJsModuleType.PRIMITIVE),
                CrateEntry.of(RelGridStockCellsModule.INSTANCE,   StandardJsModuleType.PRIMITIVE),
                CrateEntry.of(RelGridModule.INSTANCE, StandardJsModuleType.PRIMITIVE));
    }
}
