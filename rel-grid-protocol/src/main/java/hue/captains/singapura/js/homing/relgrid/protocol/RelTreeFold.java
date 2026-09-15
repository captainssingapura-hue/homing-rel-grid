package hue.captains.singapura.js.homing.relgrid.protocol;

/**
 * The tree asking for a node's children to be withdrawn — RFC 0050 · Episode
 * 3-ext1. A question like the unfold, not a local edit, because the fold state
 * is the relation's and a domain that folds a node may free what was under
 * it. Answered with a {@link RelTreeView}, or with nothing.
 */
public record RelTreeFold(Object key) {
    public RelTreeFold {
        if (key == null) throw new IllegalArgumentException("RelTreeFold names the node");
    }
}
