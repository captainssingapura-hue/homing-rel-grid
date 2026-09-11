package hue.captains.singapura.js.homing.relgrid.workbench;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;

import java.util.List;

/**
 * The Replicating Tables bench's <b>persisted store</b> — the domain's single
 * source of truth for the six dishes, shared by every table on the page and
 * saved to {@code localStorage} on every change.
 *
 * <p>Pure domain code. It knows nothing of any grid: it holds the data,
 * persists it, and tells its subscribers what changed. The relations built
 * over it are its subscribers; the grids over those relations are told
 * nothing, because there is nothing about an arrangement that a value change
 * affects.</p>
 *
 * <p>Two of its columns nobody edits: {@code sold} is a source that only sales
 * move, and {@code popularity} is <b>derived</b> from it — each dish's sales
 * as a share of the best seller's — recomputed on every sale and never
 * persisted. The store refuses a commit to either, whichever relation asks;
 * <i>who</i> may write the writable columns is each relation's rule.</p>
 */
public record DishStore() implements DomModule<DishStore> {

    public record createDishStore() implements Exportable._Constant<DishStore> {}
    public record dishStoreShared() implements Exportable._Constant<DishStore> {}

    public static final DishStore INSTANCE = new DishStore();

    @Override public ImportsFor<DishStore> imports() { return ImportsFor.noImports(); }

    @Override public ExportsOf<DishStore> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new createDishStore(), new dishStoreShared()));
    }
}
