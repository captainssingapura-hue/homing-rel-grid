package hue.captains.singapura.js.homing.reltree;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;

import java.util.List;

/**
 * RFC 0050 · Episode 3-ext1 — {@code RelTreePlaces}: the tree structure as the
 * tree holds it — the places the relation answered, and nothing more. Coercion
 * at the door, the well-formedness check that refuses a malformed outline
 * whole, and the parent and first child derived from the list. PURE logic:
 * no DOM, no relation, no channel.
 */
public record RelTreePlacesModule() implements DomModule<RelTreePlacesModule> {
    public record RelTreePlaces() implements Exportable._Class<RelTreePlacesModule> {}
    public static final RelTreePlacesModule INSTANCE = new RelTreePlacesModule();
    @Override public ImportsFor<RelTreePlacesModule> imports() { return ImportsFor.noImports(); }
    @Override public ExportsOf<RelTreePlacesModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new RelTreePlaces()));
    }
}
