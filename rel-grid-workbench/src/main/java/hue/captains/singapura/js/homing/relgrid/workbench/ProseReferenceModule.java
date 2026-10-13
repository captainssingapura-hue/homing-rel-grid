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
 * A widget of the Document Table bench: {@code new ProseReference(container, params)} - ProseTable's options, its code drawn inline, and a table with no head, a page of the house's
 * words and document tables shown whole. Given the keys by a call, they go on to the page's first table.
 */
@ConformanceDebt(rule = WidgetDrawsOnlyComponentsRule.class, findings = 1,
        reason = "It places its page in its body: it waits for the widget slot to show what it makes.")
public record ProseReferenceModule() implements DomModule<ProseReferenceModule> {

    public static final ProseReferenceModule INSTANCE = new ProseReferenceModule();

    public record ProseReference() implements SelfContainedWidget<ProseReferenceModule>, NeedKeyboard {
        @Override public String summary() { return "ProseTable's own options, its code drawn by an inline drawing, and a table with no head."; }
        @Override public List<KeyBinding> keys() {
            return List.of(KeyBinding.of(Key.ESCAPE, "the keys given back, when the table did not take it"));
        }
    }

    @Override public ImportsFor<ProseReferenceModule> imports() {
        return ImportsFor.<ProseReferenceModule>builder()
                .add(new ModuleImports<>(List.of(new BenchWidgetModule.BenchWidget()), BenchWidgetModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new ProsePagesModule.ProseReferencePage()), ProsePagesModule.INSTANCE))
                .build();
    }

    @Override public ExportsOf<ProseReferenceModule> exports() { return new ExportsOf<>(INSTANCE, List.of(new ProseReference())); }
}
