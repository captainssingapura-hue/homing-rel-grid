package hue.captains.singapura.js.homing.relgrid.protocol;

import java.util.List;

/**
 * A <b>View</b>: which of the root's identities are shown, in what order —
 * RFC 0050 · Episode 2's answer to a view question, and the domain's answer
 * to {@link RelGridViewHandover}.
 *
 * <p>An ordered list of the relation's own pks, and nothing else: not values,
 * not a sorted copy of anything, not a promise about how the order was
 * reached. The grid places exactly these rows in exactly this order (map 1,
 * law 1) and reorders nothing; a pk the relation never declared is an error
 * the grid records and does not apply. The list may be shorter than the root
 * — a View may filter — and it may be empty, in which case the presented
 * space is empty and the cursor is nowhere (map 14, law 100).</p>
 *
 * <p>The alternative answer is <b>absence</b>: the promise resolves with
 * nothing, and the rows stay as they were.</p>
 */
public record RelGridView(List<Object> pks) {
    public RelGridView {
        pks = List.copyOf(pks);
    }
    /** How many rows it shows. Derived, never stored. */
    public int size() { return pks.size(); }
}
