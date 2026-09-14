package hue.captains.singapura.js.homing.relgrid.protocol;

/**
 * The tree asking for a node's children to be presented — RFC 0050 · Episode
 * 3-ext1, the first question a tree waits for. A regular ask: answered with a
 * {@link RelTreeView}, or with nothing — the rows stay. Lazy by default: this
 * is the only way children are ever asked for, so a domain that never hears
 * it never loads them.
 */
public record RelTreeUnfold(Object key) {
    public RelTreeUnfold {
        if (key == null) throw new IllegalArgumentException("RelTreeUnfold names the node");
    }
}
