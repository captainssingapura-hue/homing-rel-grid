package hue.captains.singapura.js.homing.jsonkit.contract;

/**
 * The JSON viewer's public surface, stated once, in Java: what a host may
 * call on a {@code JsonTreeView}. The conformance test pins the JS class's
 * prototype to exactly this list.
 *
 * <p>A pointer is a JSON pointer (RFC 6901): {@code ""} the root,
 * {@code "/a/0/b~1c"} a member. The view is the tree's host: it makes two
 * branches beneath the one it is handed — the tree's and the document's —
 * and mints nothing of its own. Fold state is the document's, kept across
 * {@link #set(Object)} wherever a container still stands; the cursor is the
 * tree's, kept by pointer.</p>
 */
public interface JsonTreeViewContract {
    Object  set(Object value);                  // a new value; the folds and the cursor kept where they can be
    Object  value();                            // the value, whole
    Object  valueAt(String pointer);            // the value at a pointer, or undefined
    Object  open(String pointer);               // a container unfolded; a leaf or a stranger is nothing
    Object  close(String pointer);              // folded
    Object  openAll(Object depth);              // every container down to a depth (the root is 0); no depth means all
    Object  closeAll();                         // the root row alone
    boolean selectPointer(String pointer);      // the ancestors opened and the cursor put on the node; false for no such node
    Object  cursor();                           // the pointer under the cursor, or null
    int     rows();                             // how many rows are presented
    void    focus();                            // the keyboard host
    Object  el();                               // the tree's element
    void    destroy();                          // the tree down, the document's cells disposed, both branches dissolved

    String   JS_CLASS_NAME = "JsonTreeView";
    String[] CALLBACK_OPTION_NAMES = { "onActivated", "onCursorMoved", "onArranged" };
}
