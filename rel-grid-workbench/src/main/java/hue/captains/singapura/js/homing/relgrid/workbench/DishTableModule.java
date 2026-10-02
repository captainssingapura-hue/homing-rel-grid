package hue.captains.singapura.js.homing.relgrid.workbench;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.relgrid.RelGridModule;
import hue.captains.singapura.js.homing.relgrid.RelGridStyles;
import hue.captains.singapura.js.homing.relgrid.protocol.RelGridProtocolModule;

import java.util.List;

/**
 * What a table of the Replicating Tables bench is, for a role: {@code DishTable} - a replica of
 * the bench's one store of dishes, which the {@code dishes} party says and its editors write
 * through; a relation for the role; the views and the clipboard it answers the grid's questions
 * with. The role widgets - chef, nutritionist, manager, follower - each make one.
 */
public record DishTableModule() implements DomModule<DishTableModule> {

    public record DishTable() implements Exportable._Class<DishTableModule> {}

    public static final DishTableModule INSTANCE = new DishTableModule();

    @Override
    public ImportsFor<DishTableModule> imports() {
        return ImportsFor.<DishTableModule>builder()
                .add(new ModuleImports<>(List.of(new BenchWidgetModule.BenchWidget()), BenchWidgetModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new RelGridModule.RelGrid()), RelGridModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new RelGridStyles.hrg_frame(), new RelGridStyles.hrg_lit()), RelGridStyles.INSTANCE))
                // The protocol, so the bench can recognise what the grid asks. The widget imports
                // this and NOT the grid's internals - a domain answers through the protocol.
                .add(new ModuleImports<>(List.of(new RelGridProtocolModule.RelGridSelectionChanged(),
                        new RelGridProtocolModule.RelGridCopyRequested(), new RelGridProtocolModule.RelGridViewHandover()),
                        RelGridProtocolModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new DishStoreModule.DishStore()), DishStoreModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new DishRelationModule.DishRelation()), DishRelationModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new DishViewsModule.DishViews()), DishViewsModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new DishClipboardModule.DishClipboard()), DishClipboardModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new DishesModule.DISHES()), DishesModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new WorkbenchStyles.wb_hint(), new WorkbenchStyles.wb_bar(), new WorkbenchStyles.wb_frame(),
                        new WorkbenchStyles.wb_port(), new WorkbenchStyles.wb_status()), WorkbenchStyles.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<DishTableModule> exports() { return new ExportsOf<>(INSTANCE, List.of(new DishTable())); }
}
