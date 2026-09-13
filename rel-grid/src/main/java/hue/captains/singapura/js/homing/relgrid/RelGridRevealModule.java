package hue.captains.singapura.js.homing.relgrid;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;

import java.util.List;

/**
 * RFC 0050 · Episode 2 — the VIEWPORT FOLLOW, and two facts about focus and
 * containment: the least scroll that shows an element, in every port that
 * scrolls and then the window; whether a root holds the keyboard; whether an
 * element is inside another. Geometry only — nothing minted, painted or kept.
 */
public record RelGridRevealModule() implements DomModule<RelGridRevealModule> {

    public record relGridRevealSlot()  implements Exportable._Constant<RelGridRevealModule> {}
    public record relGridHasKeyboard() implements Exportable._Constant<RelGridRevealModule> {}
    public record relGridWithin()      implements Exportable._Constant<RelGridRevealModule> {}

    public static final RelGridRevealModule INSTANCE = new RelGridRevealModule();

    @Override public ImportsFor<RelGridRevealModule> imports() { return ImportsFor.noImports(); }

    @Override public ExportsOf<RelGridRevealModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new relGridRevealSlot(), new relGridHasKeyboard(), new relGridWithin()));
    }
}
