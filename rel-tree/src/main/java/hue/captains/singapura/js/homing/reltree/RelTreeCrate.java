package hue.captains.singapura.js.homing.reltree;

import hue.captains.singapura.js.homing.core.Crate;
import hue.captains.singapura.js.homing.core.CrateEntry;
import hue.captains.singapura.js.homing.core.StandardJsModuleType;
import hue.captains.singapura.js.homing.relchannel.RelChannelCrate;
import hue.captains.singapura.js.homing.relgrid.protocol.RelGridProtocolCrate;

import java.util.List;

/**
 * RFC 0044 — the {@link Crate} for {@code rel-tree}. One crate per module;
 * every served JS module declared with its type, so conformance is ambient
 * from the first line. It requires the channel's core and the family's
 * protocol, and nothing of the grid's table.
 */
public final class RelTreeCrate implements Crate {

    public static final RelTreeCrate INSTANCE = new RelTreeCrate();

    private RelTreeCrate() {}

    @Override public String name() { return "rel-tree"; }

    @Override public List<Crate> requires() {
        return List.of(RelChannelCrate.INSTANCE, RelGridProtocolCrate.INSTANCE);
    }

    @Override
    public List<CrateEntry> entries() {
        return List.of(
                CrateEntry.of(RelTreePlacesModule.INSTANCE, StandardJsModuleType.PURE_LOGIC),
                CrateEntry.of(RelTreeStyles.INSTANCE,       StandardJsModuleType.GENERATED_CSS),
                CrateEntry.of(RelTreeStockStyles.INSTANCE,  StandardJsModuleType.GENERATED_CSS),
                // The typed SVG: generated from the assets beside it, as a style group is from its records.
                CrateEntry.of(RelTreeSvgs.INSTANCE),
                CrateEntry.of(RelTreeRowsModule.INSTANCE),
                CrateEntry.of(RelTreeLayoutModule.INSTANCE),
                CrateEntry.of(RelTreeCellsModule.INSTANCE),
                CrateEntry.of(RelTreeCursorModule.INSTANCE),
                CrateEntry.of(RelTreeChannelModule.INSTANCE),
                CrateEntry.of(RelTreeGesturesModule.INSTANCE),
                CrateEntry.of(RelTreeStockCellsModule.INSTANCE),
                CrateEntry.of(RelTreeModule.INSTANCE));
    }
}
