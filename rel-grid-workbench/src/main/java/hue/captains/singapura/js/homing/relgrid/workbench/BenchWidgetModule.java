package hue.captains.singapura.js.homing.relgrid.workbench;

import hue.captains.singapura.js.homing.component.keyboard.FocusPartyModule;
import hue.captains.singapura.js.homing.component.keyboard.KeysModule;
import hue.captains.singapura.js.homing.component.keyboard.focusParties;
import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.core.js.DomOpsPartyModule;
import hue.captains.singapura.js.homing.core.js.domOpsParties;
import hue.captains.singapura.js.homing.workspace.widgets.WidgetStyles;

import java.util.List;

/**
 * What every bench widget is to its host, said once: {@code BenchWidget} - its own DomOps and
 * focus parties offered as roots, the keys claimed on a press, the parties it shares with its
 * bench joined after it is made; a bench widget extends it and builds into its body.
 */
public record BenchWidgetModule() implements DomModule<BenchWidgetModule> {

    public static final BenchWidgetModule INSTANCE = new BenchWidgetModule();

    public record BenchWidget() implements Exportable._Class<BenchWidgetModule> {}

    @Override
    public ImportsFor<BenchWidgetModule> imports() {
        return ImportsFor.<BenchWidgetModule>builder()
                .add(new ModuleImports<>(List.of(new domOpsParties()), DomOpsPartyModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new focusParties()), FocusPartyModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new KeysModule.Keys()), KeysModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new WidgetStyles.wg_fill(), new WidgetStyles.wg_scroll()), WidgetStyles.INSTANCE))
                .add(new ModuleImports<>(List.of(new WorkbenchStyles.wb_root(), new WorkbenchStyles.wb_btn()), WorkbenchStyles.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<BenchWidgetModule> exports() { return new ExportsOf<>(INSTANCE, List.of(new BenchWidget())); }
}
