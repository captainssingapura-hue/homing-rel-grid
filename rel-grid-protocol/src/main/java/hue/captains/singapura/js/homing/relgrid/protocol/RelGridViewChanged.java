package hue.captains.singapura.js.homing.relgrid.protocol;

/**
 * The domain telling the grid, unasked, that its <b>View has changed</b> —
 * RFC 0050 · Episode 2, the first message that travels the channel's other
 * way to a <i>grid</i>, as {@link RelGridGroupFold} was the first to a group.
 *
 * <p>The grid asks {@code view()} at construction and holds what it was
 * answered; it is never spoken to after — except through {@code tell}. A
 * relation whose View changes underneath it — an article that grew a row, a
 * provided layer whose header cell was clicked into a new order, a store
 * whose filter moved — has no way to reach the grid and should not want one:
 * it tells the <b>host</b>, which is the common parent, and the host tells
 * the grid this. The grid answers by asking {@code view()} again and
 * presenting what comes back, exactly as it would any remap: the cursor keeps
 * its identity, the ranges go, cells that left are forgotten.</p>
 *
 * <p>It carries nothing. The View is not <i>in</i> the message — that would
 * be the domain pushing keys into the grid — it is the answer to the question
 * the grid then asks, which keeps the one seam one. Rows only: a column view
 * is a host option and is remapped as one.</p>
 */
public record RelGridViewChanged() {
}
