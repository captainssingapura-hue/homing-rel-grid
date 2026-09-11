package hue.captains.singapura.js.homing.relgrid.workbench;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;

import java.util.List;

/**
 * The Han Article bench's persisted store: one string, the article, saved on
 * every change and telling its subscribers when it changed. Edits arrive as
 * the whole text, from the editor's textarea, on every keystroke.
 */
public record HanStore() implements DomModule<HanStore> {

    public record createHanStore() implements Exportable._Constant<HanStore> {}
    public record hanStoreShared() implements Exportable._Constant<HanStore> {}

    public static final HanStore INSTANCE = new HanStore();

    @Override public ImportsFor<HanStore> imports() { return ImportsFor.noImports(); }

    @Override public ExportsOf<HanStore> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new createHanStore(), new hanStoreShared()));
    }
}
