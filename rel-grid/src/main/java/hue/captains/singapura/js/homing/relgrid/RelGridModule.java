package hue.captains.singapura.js.homing.relgrid;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;

import hue.captains.singapura.js.homing.relgrid.protocol.RelGridProtocolModule;
import hue.captains.singapura.js.homing.relgrid.selection.RelGridSelectionModule;

import java.util.List;

/**
 * RFC 0050 · Episode 2 — {@code RelGrid}: the facade, and the only place
 * the layout and cells branches meet. Orchestration only: it reads the
 * relation's identities and columns, builds the seam, and on every arrangement
 * pass mints slots, asks the cell manager once per identity, and places.
 *
 * <p>It does not import the stock cells. Cells are the domain's; a relation
 * imports what it builds its manager from.</p>
 *
 * <p>It does import the {@code RelGridSelection}, and it is the only thing
 * that does. The selection imports nothing itself (map 5 law 215), so the
 * dependency runs one way only: the facade knows about the list, and the list
 * knows about nothing.</p>
 */
public record RelGridModule() implements DomModule<RelGridModule> {

    public record RelGrid() implements Exportable._Constant<RelGridModule> {}

    public static final RelGridModule INSTANCE = new RelGridModule();

    @Override
    public ImportsFor<RelGridModule> imports() {
        return ImportsFor.<RelGridModule>builder()
                .add(new ModuleImports<>(List.of(new RelGridViewMapsModule.RelGridViewMaps()), RelGridViewMapsModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new RelGridLayoutModule.RelGridLayout()),     RelGridLayoutModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new RelGridCellsModule.RelGridCells()),       RelGridCellsModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new RelGridSelectionModule.RelGridSelection()), RelGridSelectionModule.INSTANCE))
                .add(new ModuleImports<>(
                        List.of(new RelGridProtocolModule.RelGridRange(),
                                new RelGridProtocolModule.RelGridSelectionChanged(),
                                new RelGridProtocolModule.RelGridBlock(),
                                new RelGridProtocolModule.RelGridCopyRequested(),
                                new RelGridProtocolModule.RelGridClipboardContent()),
                        RelGridProtocolModule.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<RelGridModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new RelGrid()));
    }
}
