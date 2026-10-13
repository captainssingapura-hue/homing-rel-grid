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
import hue.captains.singapura.js.homing.workspace.widgets.SelfContainedWidget;

import java.util.List;

/**
 * A widget of the Document Table bench: {@code new ProseFigures(container, params)} - a table wider than its page, scrolling across inside its box, a page of the house's
 * words and document tables shown whole. Given the keys by a call, they go on to the page's first table.
 */
@ConformanceDebt(rule = WidgetDrawsOnlyComponentsRule.class, findings = 1,
        reason = "It places its page in its body: it waits for the widget slot to show what it makes.")
public record ProseFiguresModule() implements DomModule<ProseFiguresModule> {

    public static final ProseFiguresModule INSTANCE = new ProseFiguresModule();

    public record ProseFigures() implements SelfContainedWidget<ProseFiguresModule>, NeedKeyboard {
        @Override public String summary() { return "A table wider than its page: thirteen columns scrolling across inside its box."; }
        @Override public List<KeyBinding> keys() {
            return List.of(KeyBinding.of(Key.ESCAPE, "the keys given back, when the table did not take it"));
        }
    }

    @Override public ImportsFor<ProseFiguresModule> imports() {
        return ImportsFor.<ProseFiguresModule>builder()
                .add(new ModuleImports<>(List.of(new BenchWidgetModule.BenchWidget()), BenchWidgetModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new ProsePagesModule.ProseFiguresPage()), ProsePagesModule.INSTANCE))
                .build();
    }

    @Override public ExportsOf<ProseFiguresModule> exports() { return new ExportsOf<>(INSTANCE, List.of(new ProseFigures())); }
}
