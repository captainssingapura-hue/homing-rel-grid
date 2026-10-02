package hue.captains.singapura.js.homing.relgrid.workbench;

import hue.captains.singapura.js.homing.component.keyboard.Key;
import hue.captains.singapura.js.homing.component.keyboard.KeyBinding;
import hue.captains.singapura.js.homing.component.keyboard.NeedKeyboard;
import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.relgrid.group.RelGridGroupModule;
import hue.captains.singapura.js.homing.workspace.widgets.SelfContainedWidget;

import java.util.List;

/**
 * The Replicating Tables bench's outlets: {@code new Outlets(container, params)} - the same six
 * dishes sold at three outlets, a table each, stacked in a group whose fences are the domain's;
 * its ledger its own.
 */
public record OutletsModule() implements DomModule<OutletsModule> {

    public record Outlets() implements SelfContainedWidget<OutletsModule>, NeedKeyboard {
        @Override public String summary() { return "Six dishes sold at three outlets, a table each, stacked in a group; the fences the domain's."; }
        @Override public List<KeyBinding> keys() { return List.of(KeyBinding.of(Key.ESCAPE, "the keys given back, when the group did not take it")); }
    }

    public static final OutletsModule INSTANCE = new OutletsModule();

    @Override
    public ImportsFor<OutletsModule> imports() {
        return ImportsFor.<OutletsModule>builder()
                .add(new ModuleImports<>(List.of(new BenchWidgetModule.BenchWidget()), BenchWidgetModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new RelGridGroupModule.RelGridGroup()), RelGridGroupModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new SalesStoreModule.SalesStore()), SalesStoreModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new OutletRelationModule.OutletRelation(), new OutletRelationModule.OutletFence(),
                        new OutletRelationModule.LedgerFence()), OutletRelationModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new WorkbenchStyles.wb_hint(), new WorkbenchStyles.wb_bar(), new WorkbenchStyles.wb_host(),
                        new WorkbenchStyles.wb_status()), WorkbenchStyles.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<OutletsModule> exports() { return new ExportsOf<>(INSTANCE, List.of(new Outlets())); }
}
