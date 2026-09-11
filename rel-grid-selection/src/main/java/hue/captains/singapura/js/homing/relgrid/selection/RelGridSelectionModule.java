package hue.captains.singapura.js.homing.relgrid.selection;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;

import java.util.List;

/**
 * RFC 0050 · Episode 2, map 5 — {@code RelGridSelection}: an ordered list of
 * ranges, each a rectangle of positions owning both its corners, never merged
 * and never reordered. PURE logic.
 *
 * <p>{@link ImportsFor#noImports()} is the module's whole character, not an
 * accident of its current size. The selection holds no view map, no layout, no
 * cell, no relation and no cursor; the two operations that need something
 * outside the list are handed it — the cursor's position, because an empty
 * list means the cursor's implicit 1×1, and the extents, because select-all
 * builds a rectangle from a size this module never learns. It does not clamp,
 * it does not paint, and it tells no cell.</p>
 *
 * <p>It never initiates. It manages the list, and answers what is selected
 * when something else is triggered — map 5 laws 215–218.</p>
 */
public record RelGridSelectionModule() implements DomModule<RelGridSelectionModule> {

    public record RelGridSelection() implements Exportable._Class<RelGridSelectionModule> {}

    public static final RelGridSelectionModule INSTANCE = new RelGridSelectionModule();

    @Override public ImportsFor<RelGridSelectionModule> imports() { return ImportsFor.noImports(); }

    @Override public ExportsOf<RelGridSelectionModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new RelGridSelection()));
    }
}
