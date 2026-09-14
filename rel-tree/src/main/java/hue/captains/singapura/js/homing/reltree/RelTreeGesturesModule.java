package hue.captains.singapura.js.homing.reltree;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;

import java.util.List;

/**
 * RFC 0050 · Episode 3-ext1 — {@code RelTreeGestures}: what a key and a press
 * mean on a tree — the arrows the tree owns, Home, End, the page keys, Enter,
 * Space, the caret — refused while a question is pending.
 */
public record RelTreeGesturesModule() implements DomModule<RelTreeGesturesModule> {
    public record RelTreeGestures() implements Exportable._Constant<RelTreeGesturesModule> {}
    public static final RelTreeGesturesModule INSTANCE = new RelTreeGesturesModule();
    @Override public ImportsFor<RelTreeGesturesModule> imports() { return ImportsFor.noImports(); }
    @Override public ExportsOf<RelTreeGesturesModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new RelTreeGestures()));
    }
}
