package hue.captains.singapura.js.homing.reltree.contract;

/**
 * RFC 0050 · Episode 3-ext1 — the tree view's facade, {@code RelTree}: its
 * public surface, bound to the JS class by a conformance test so the two
 * cannot drift.
 *
 * <p>Options: {@code container}, {@code branch} (the tree's own, unactivated),
 * {@code relation} ({@link TreeRelationContract}), {@code label},
 * {@code caret} (false for a domain that draws its own), {@code folder} (true
 * for a closed/open folder glyph after the caret, or the three glyphs to use),
 * {@code ask} — the channel — and the callbacks named below. No {@code view({ by })} in this
 * phase: the window is reserved.</p>
 *
 * <p>Unfold and fold are questions on the channel, answered with the whole
 * View or nothing; while one is pending the tree is locked and masked and every
 * intent is refused. {@code tell(new RelTreeViewChanged())} asks {@code view()}
 * again. {@code selectNode} reaches a presented key and reveals it; the tree
 * opens nothing on a domain's behalf — the ancestors are the domain's to open,
 * and to tell.</p>
 */
public interface RelTreeContract {
    Object  cursor();                       // the current key, or null — never a position
    boolean selectNode(Object key);         // programmatic cursor on a presented key, revealed; false otherwise, or when locked
    boolean unfold(Object key);             // ask for a closed node's children; false when locked, channel-less, not presented, not closed
    boolean fold(Object key);               // ask for an open node's children to go; false likewise
    boolean activate(Object key);           // the programmatic twin of Enter and a double-click: told, and reported
    boolean tell(Object message);           // RelTreeViewChanged asks view() again. False for a stranger
    boolean isPending();                    // a question is outstanding, and the tree is locked
    void    focus();                        // the keyboard host, without scrolling
    Object  el();                           // the tree element
    Object  cells();                        // the cell registry, for a host that must reach one
    Object  places();                       // the presented places — the structure seam
    void    destroy();                      // detaches every cell and removes the chrome; DISPOSES NOTHING

    String   JS_CLASS_NAME = "RelTree";
    String[] CALLBACK_OPTION_NAMES = { "onArranged", "onCursorMoved", "onActivated", "onEdge" };
    /** The channel is not a callback: it is asked, and it answers. */
    String   CHANNEL_OPTION_NAME = "ask";
}
