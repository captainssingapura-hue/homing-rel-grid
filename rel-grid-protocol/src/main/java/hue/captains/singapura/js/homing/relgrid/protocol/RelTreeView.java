package hue.captains.singapura.js.homing.relgrid.protocol;

import java.util.List;

/**
 * A tree's <b>View</b>: the places to present now, whole and in order — RFC
 * 0050 · Episode 3-ext1's answer to {@link RelTreeUnfold} and
 * {@link RelTreeFold}, the shape {@link RelGridView} has for the grid.
 *
 * <p>Whole, never a delta: the tree holds no structure to insert children
 * into, and the fold state is the relation's, so what an unfold answers is
 * everything the tree shows next. It may be shorter than the last — a fold
 * withdraws, a domain bounds a large tree by what it answers — and it may
 * be empty. The tree checks it is well-formed at the door and refuses a
 * malformed one whole.</p>
 *
 * <p>The alternative answer is <b>absence</b>: the promise resolves with
 * nothing, and the rows stay as they were — a leaf, a stranger, a node
 * already in that state, or a domain that will fetch and tell later.</p>
 */
public record RelTreeView(List<RelTreePlace> places) {
    public RelTreeView {
        places = List.copyOf(places);
    }
    /** How many rows it shows. Derived, never stored. */
    public int size() { return places.size(); }
}
