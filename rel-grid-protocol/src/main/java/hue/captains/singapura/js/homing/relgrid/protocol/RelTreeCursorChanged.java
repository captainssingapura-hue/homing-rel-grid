package hue.captains.singapura.js.homing.relgrid.protocol;

/**
 * A notification: the tree's current node is now this one — RFC 0050 ·
 * Episode 3-ext1. Told, never waited on; sent for a press and for a key
 * alike, so a domain that follows the cursor — a navigator showing what is
 * current — hears both. Nothing is sent when the cursor leaves every row.
 */
public record RelTreeCursorChanged(Object key) {
    public RelTreeCursorChanged {
        if (key == null) throw new IllegalArgumentException("RelTreeCursorChanged names the node");
    }
}
