package hue.captains.singapura.js.homing.relgrid.workbench;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.core.ModuleNameResolver;
import hue.captains.singapura.js.homing.core.SelfContent;
import hue.captains.singapura.js.homing.design.styling.InstalledStyling;

import java.util.List;

/**
 * Minesweeper's Vista board as a page has it: one stateless object, {@code MinesweeperVista}, generated from the
 * local style - the typed local style the game's grid is made with. It wears a leaf of the grid's frame on an element, and imports
 * the local style's sheet so the sheet loads wherever the local style is used.
 */
public record MinesweeperVistaModule() implements DomModule<MinesweeperVistaModule>, SelfContent {

    public static final MinesweeperVistaModule INSTANCE = new MinesweeperVistaModule();

    /** The local style's object: {@code MinesweeperVista.wear(GRID_FRAME.GRID_EDGE, el)}. */
    public record MinesweeperVista() implements Exportable._Constant<MinesweeperVistaModule> {}

    @Override
    public ImportsFor<MinesweeperVistaModule> imports() {
        return ImportsFor.<MinesweeperVistaModule>builder()
                .add(new ModuleImports<>(List.of(new MinesweeperVistaSheet.minesweeper_vista_sheet()), MinesweeperVistaSheet.INSTANCE))
                .build();
    }

    @Override public ExportsOf<MinesweeperVistaModule> exports() { return new ExportsOf<>(INSTANCE, List.of(new MinesweeperVista())); }

    @Override
    public List<String> selfContent(ModuleNameResolver resolver) {
        return InstalledStyling.get().localStyleObject(hue.captains.singapura.js.homing.relgrid.workbench.MinesweeperVista.INSTANCE, MinesweeperVistaSheet.INSTANCE);
    }
}
