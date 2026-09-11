package hue.captains.singapura.js.homing.relgrid;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;

import java.util.List;

/**
 * RFC 0050 · Episode 2 — {@code RelGridViewMaps}: the identity/position seam, PURE
 * logic. The only place {@code (i, j)} meets {@code (pk, column)}. Carried over
 * from the live grid's maps unchanged in shape, minus {@code addPk / removePk}:
 * the identity space is the root's and fixed for the grid's lifetime, so there
 * is nothing to add to it and nothing to remove from it.
 */
public record RelGridViewMapsModule() implements DomModule<RelGridViewMapsModule> {

    public record RelGridViewMaps() implements Exportable._Constant<RelGridViewMapsModule> {}

    public static final RelGridViewMapsModule INSTANCE = new RelGridViewMapsModule();

    @Override public ImportsFor<RelGridViewMapsModule> imports() { return ImportsFor.noImports(); }

    @Override public ExportsOf<RelGridViewMapsModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new RelGridViewMaps()));
    }
}
