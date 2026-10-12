package hue.captains.singapura.js.homing.relgrid.house;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.core.ModuleNameResolver;
import hue.captains.singapura.js.homing.core.SelfContent;
import hue.captains.singapura.js.homing.ui.look.HouseDesigns;

import java.util.List;

/**
 * The house's grid as a page has it: one stateless object, {@code HouseGrid}, generated from the design - the typed
 * design a grid is made with. It wears and sheds a leaf of the grid's frame on an element, asked by the leaf's constant,
 * and refuses a leaf of another branch; it imports the design's sheet, so the sheet loads wherever the design is used.
 * A DOM module, honestly: what it does is put its classes on an element, as the look and the icon do theirs.
 */
public record HouseGridModule() implements DomModule<HouseGridModule>, SelfContent {

    public static final HouseGridModule INSTANCE = new HouseGridModule();

    /** The design's object: {@code HouseGrid.wear(GRID_FRAME.LATTICE, el)}. */
    public record HouseGrid() implements Exportable._Constant<HouseGridModule> {}

    @Override
    public ImportsFor<HouseGridModule> imports() {
        return ImportsFor.<HouseGridModule>builder()
                .add(new ModuleImports<>(List.of(new HouseGridSheet.house_grid_sheet()), HouseGridSheet.INSTANCE))
                .build();
    }

    @Override public ExportsOf<HouseGridModule> exports() { return new ExportsOf<>(INSTANCE, List.of(new HouseGrid())); }

    @Override
    public List<String> selfContent(ModuleNameResolver resolver) {
        return HouseDesigns.INSTANCE.object(hue.captains.singapura.js.homing.relgrid.house.HouseGrid.INSTANCE, "house_grid_sheet");
    }
}
