package hue.captains.singapura.js.homing.relgrid.protocol;

import java.util.List;

/**
 * A range RESOLVED: the identities a rectangle of positions covers, in view
 * order — RFC 0050 · Episode 2, map 6 law 47.
 *
 * <p>A {@link RelGridRange} is positions and stays positions until the moment
 * of use. Copy is such a moment: the domain that answers has no arrangement
 * and cannot turn a row number into a dish, so the grid resolves each range
 * to the pks down its rows and the columns across it, and hands over those.
 * Faithfully — one block per range, in the order the ranges were made, no
 * bounding box, no merging, no refusal of the grid's own.</p>
 *
 * <p>Identities only. What those cells are worth is exactly what the grid
 * does not know, which is why the domain is being asked.</p>
 */
public record RelGridBlock(List<Object> pks, List<String> columns) {

    public RelGridBlock {
        pks = List.copyOf(pks);
        columns = List.copyOf(columns);
        if (pks.isEmpty() || columns.isEmpty())
            throw new IllegalArgumentException("RelGridBlock covers at least one cell");
    }

    /** How many cells it covers. Derived, never stored. */
    public int cells() { return pks.size() * columns.size(); }
}
