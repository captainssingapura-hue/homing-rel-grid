package hue.captains.singapura.js.homing.relgrid.protocol;

/**
 * Told to a TREE, unasked: its relation's View changed underneath it — RFC
 * 0050 · Episode 3-ext1, the tree's {@link RelGridViewChanged}. The tree
 * answers by asking {@code view()} again and presenting what comes back. It
 * carries nothing, on purpose: the View is the answer to the question the
 * tree then asks, never something pushed into it. A domain that answered an
 * unfold with nothing, fetched, and now has the children sends this.
 */
public record RelTreeViewChanged() {
}
