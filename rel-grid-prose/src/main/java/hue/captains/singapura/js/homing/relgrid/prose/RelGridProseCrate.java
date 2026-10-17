package hue.captains.singapura.js.homing.relgrid.prose;

import hue.captains.singapura.js.homing.core.Crate;
import hue.captains.singapura.js.homing.core.CrateEntry;
import hue.captains.singapura.js.homing.core.StandardJsModuleType;
import hue.captains.singapura.js.homing.relgrid.RelGridCrate;
import hue.captains.singapura.js.homing.relgrid.house.RelGridHouseCrate;
import hue.captains.singapura.js.homing.relgrid.protocol.RelGridProtocolCrate;
import hue.captains.singapura.js.homing.ui.look.UiLookCrate;

import java.util.List;

/**
 * A document's table, out of the box: the relation it is made from, the table, its cells and its mechanics. It requires
 * the grid it is made of, the house's design it is drawn in, the protocol its copy answers in, and the house's look its
 * cells wear.
 */
public final class RelGridProseCrate implements Crate {

    public static final RelGridProseCrate INSTANCE = new RelGridProseCrate();

    private RelGridProseCrate() {}

    @Override public String name() { return "rel-grid-prose"; }

    @Override public List<Crate> requires() {
        return List.of(RelGridCrate.INSTANCE, RelGridHouseCrate.INSTANCE, RelGridProtocolCrate.INSTANCE, UiLookCrate.INSTANCE);
    }

    @Override public List<CrateEntry> entries() {
        return List.of(CrateEntry.of(DocTableRelationModule.INSTANCE, StandardJsModuleType.PURE_LOGIC),
                       CrateEntry.of(ProseStyles.INSTANCE), CrateEntry.of(ProseCellsModule.INSTANCE), CrateEntry.of(ProseTableModule.INSTANCE));
    }
}
