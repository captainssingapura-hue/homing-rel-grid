package hue.captains.singapura.js.homing.relgrid;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;

import java.util.List;

/**
 * RFC 0050 · Episode 2 — {@code RelGridSpans}: the merged cells (map 25), the
 * feature kept apart. A leading cell's reach read afresh on every pass and
 * the host it moves into; the group a position is in; one horizontal step
 * out of a group; where deep goes from a cursor inside one. Off, every answer
 * is the plain one. Split from the facade when the header cells arrived and
 * the conformance ceiling said so. Imports nothing: it asks the layout, the
 * cells and the maps it is handed.
 */
public record RelGridSpansModule() implements DomModule<RelGridSpansModule> {

    public record RelGridSpans() implements Exportable._Constant<RelGridSpansModule> {}

    public static final RelGridSpansModule INSTANCE = new RelGridSpansModule();

    @Override public ImportsFor<RelGridSpansModule> imports() { return ImportsFor.noImports(); }

    @Override public ExportsOf<RelGridSpansModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new RelGridSpans()));
    }
}
