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
 * The house's document table as a page has it: one stateless object, {@code HouseProse}, generated from the design - the
 * typed design a grid that reads as a document's table is made with. It wears and sheds a leaf of the grid's frame on
 * an element, and imports the design's sheet, so the sheet loads wherever the design is used.
 */
public record HouseProseModule() implements DomModule<HouseProseModule>, SelfContent {

    public static final HouseProseModule INSTANCE = new HouseProseModule();

    /** The design's object: {@code HouseProse.wear(GRID_FRAME.LATTICE, el)}. */
    public record HouseProse() implements Exportable._Constant<HouseProseModule> {}

    @Override
    public ImportsFor<HouseProseModule> imports() {
        return ImportsFor.<HouseProseModule>builder()
                .add(new ModuleImports<>(List.of(new HouseProseSheet.house_prose_sheet()), HouseProseSheet.INSTANCE))
                .build();
    }

    @Override public ExportsOf<HouseProseModule> exports() { return new ExportsOf<>(INSTANCE, List.of(new HouseProse())); }

    @Override
    public List<String> selfContent(ModuleNameResolver resolver) {
        return HouseDesigns.INSTANCE.object(hue.captains.singapura.js.homing.relgrid.house.HouseProse.INSTANCE, "house_prose_sheet");
    }
}
