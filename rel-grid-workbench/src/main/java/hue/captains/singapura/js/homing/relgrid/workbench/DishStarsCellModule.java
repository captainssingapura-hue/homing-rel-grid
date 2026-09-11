package hue.captains.singapura.js.homing.relgrid.workbench;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;

import java.util.List;

/**
 * A health rating, 1 to 5, edited with a <b>dropdown</b> — the bench's own cell,
 * written against {@code RelGridCellContract} without shipping with the grid.
 *
 * <p>It exists to test the contract rather than the grid. Until now the stock
 * text cell was the only implementation, so the contract had only ever been
 * proved against the one thing it was written for. This is a cell that is not
 * text (map 17): it shows stars rather than a string, edits with a
 * {@code <select>} rather than an {@code <input>}, and commits on <b>change</b>
 * rather than on Enter — and the grid cannot tell the difference, because
 * everything the grid knows about a cell is on the contract.</p>
 *
 * <p>The two-stage handover is answered exactly as the stock cell answers it:
 * {@code mayTakeControl()} synchronously and without side effects, then
 * {@code takeControl()} with a promise that settles when the dropdown is done —
 * chosen, cancelled with Escape, or blurred away. The grid never learns
 * which.</p>
 */
public record DishStarsCellModule() implements DomModule<DishStarsCellModule> {

    public record DishStarsCell() implements Exportable._Class<DishStarsCellModule> {}

    public static final DishStarsCellModule INSTANCE = new DishStarsCellModule();

    @Override public ImportsFor<DishStarsCellModule> imports() { return ImportsFor.noImports(); }

    @Override public ExportsOf<DishStarsCellModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new DishStarsCell()));
    }
}
