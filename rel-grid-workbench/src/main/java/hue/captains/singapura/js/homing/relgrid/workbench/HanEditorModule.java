package hue.captains.singapura.js.homing.relgrid.workbench;

import hue.captains.singapura.js.homing.component.keyboard.Key;
import hue.captains.singapura.js.homing.component.keyboard.KeyBinding;
import hue.captains.singapura.js.homing.component.keyboard.NeedKeyboard;
import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.workspace.widgets.SelfContainedWidget;

import java.util.List;

/**
 * The Han Article bench's editor: {@code new HanEditor(container, params)} - the article as plain
 * text above, and as square slots below it; every keystroke told to the {@code han-article} party.
 */
public record HanEditorModule() implements DomModule<HanEditorModule> {

    public record HanEditor() implements SelfContainedWidget<HanEditorModule>, NeedKeyboard {
        @Override public String summary() { return "The article as plain text, and as square slots below it; every keystroke told to the bench."; }
        @Override public List<KeyBinding> keys() { return List.of(KeyBinding.of(Key.ESCAPE, "the keys given back, when the text did not take it")); }
    }

    public static final HanEditorModule INSTANCE = new HanEditorModule();

    @Override
    public ImportsFor<HanEditorModule> imports() {
        return ImportsFor.<HanEditorModule>builder()
                .add(new ModuleImports<>(List.of(new HanArticleViewModule.HanArticleView()), HanArticleViewModule.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<HanEditorModule> exports() { return new ExportsOf<>(INSTANCE, List.of(new HanEditor())); }
}
