package hue.captains.singapura.js.homing.relgrid.protocol;

/**
 * A notification: Enter, or a double-click, reached this node — RFC 0050 ·
 * Episode 3-ext1. What an activation means is the domain's; the tree only
 * says it happened. A tree cell is never entered, so this is the whole of
 * what Enter does on it.
 */
public record RelTreeActivated(Object key) {
    public RelTreeActivated {
        if (key == null) throw new IllegalArgumentException("RelTreeActivated names the node");
    }
}
