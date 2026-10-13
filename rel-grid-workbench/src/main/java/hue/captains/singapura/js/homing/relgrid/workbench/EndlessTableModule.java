package hue.captains.singapura.js.homing.relgrid.workbench;

import hue.captains.singapura.js.homing.component.keyboard.Key;
import hue.captains.singapura.js.homing.component.keyboard.KeyBinding;
import hue.captains.singapura.js.homing.component.keyboard.NeedKeyboard;
import hue.captains.singapura.js.homing.conformance.rules.ConformanceDebt;
import hue.captains.singapura.js.homing.conformance.rules.WidgetDrawsOnlyComponentsRule;
import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.relgrid.RelGridModule;
import hue.captains.singapura.js.homing.relgrid.protocol.RelGridProtocolModule;
import hue.captains.singapura.js.homing.relgrid.house.HouseGridModule;
import hue.captains.singapura.js.homing.workspace.widgets.SelfContainedWidget;

import java.util.List;

/**
 * The endless table, the stress bench for the two branches: {@code new EndlessTable(container,
 * params)} - a window of twenty over a million rows, the grid never told there are more, its
 * invariants read off the party's numbers after every step.
 */
@ConformanceDebt(rule = WidgetDrawsOnlyComponentsRule.class, findings = 8,
        reason = "It makes its hint, bar and readouts of its own and writes their text: it waits for the designed containers, the widget slot and the content components to draw only components.")
public record EndlessTableModule() implements DomModule<EndlessTableModule> {

    public record EndlessTable() implements SelfContainedWidget<EndlessTableModule>, NeedKeyboard {
        @Override public String summary() { return "A window of twenty over a million rows, the grid never told there are more; the party's numbers checked after every step."; }
        @Override public List<KeyBinding> keys() {
            return List.of(KeyBinding.of(Key.ESCAPE, "the keys given back, when the grid did not take it"));
        }
    }

    public static final EndlessTableModule INSTANCE = new EndlessTableModule();

    @Override
    public ImportsFor<EndlessTableModule> imports() {
        return ImportsFor.<EndlessTableModule>builder()
                .add(new ModuleImports<>(List.of(new BenchWidgetModule.BenchWidget()), BenchWidgetModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new RelGridModule.RelGrid()), RelGridModule.INSTANCE))
                // the copy question it answers, and the content it answers with
                .add(new ModuleImports<>(List.of(new RelGridProtocolModule.RelGridCopyRequested(), new RelGridProtocolModule.RelGridClipboardContent()),
                        RelGridProtocolModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new HouseGridModule.HouseGrid()), HouseGridModule.INSTANCE))   // the design its grid is made with
                .add(new ModuleImports<>(List.of(new EndlessRelationModule.EndlessRelation()), EndlessRelationModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new WorkbenchStyles.wb_hint(), new WorkbenchStyles.wb_bar(),
                        new WorkbenchStyles.wb_window_host(), new WorkbenchStyles.wb_status()), WorkbenchStyles.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<EndlessTableModule> exports() { return new ExportsOf<>(INSTANCE, List.of(new EndlessTable())); }
}
