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
 * A display of the Han Article bench's article: {@code new HanDisplay(container, params)} - square
 * slots, moving as the {@code han-article} party says the editor typed; its grid never told.
 */
public record HanDisplayModule() implements DomModule<HanDisplayModule> {

    public record HanDisplay() implements SelfContainedWidget<HanDisplayModule>, NeedKeyboard {
        @Override public String summary() { return "The bench's article as square slots, moving as the editor types; its grid never told."; }
        @Override public List<KeyBinding> keys() { return List.of(KeyBinding.of(Key.ESCAPE, "the keys given back, when the grid did not take it")); }
    }

    public static final HanDisplayModule INSTANCE = new HanDisplayModule();

    @Override
    public ImportsFor<HanDisplayModule> imports() {
        return ImportsFor.<HanDisplayModule>builder()
                .add(new ModuleImports<>(List.of(new HanArticleViewModule.HanArticleView()), HanArticleViewModule.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<HanDisplayModule> exports() { return new ExportsOf<>(INSTANCE, List.of(new HanDisplay())); }
}
