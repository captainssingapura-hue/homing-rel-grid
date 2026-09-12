package hue.captains.singapura.js.homing.relgrid.group.contract;

/**
 * RFC 0050 · Episode 2 — the GROUP's public surface, stated once, in Java:
 * an ordered list of tables, each an ordinary {@code RelGrid} that does not
 * know it is in one, with a fence — a slot the domain fills — between every
 * two and around the ends. The conformance test pins {@code RelGridGroup}'s
 * prototype to exactly this list.
 *
 * <h2>Members are identities</h2>
 *
 * <p>A member is addressed by its id and nothing else. The group's own state
 * is which members show, in what order — a view over members, as a table's is
 * a view over rows — and nothing the group does is ever applied to a member's
 * rows. A member's cursor, selection, copy, handover and the domain its ask
 * reaches are its own, wired exactly as they would be for a table standing
 * alone; {@link #member(String)} hands the ordinary grid to a host that must
 * reach it. A member may have no rows and still have its identity and its
 * fence — an illustration between two tables is exactly that.</p>
 *
 * <h2>Fences</h2>
 *
 * <p>N members, N+1 slots: one above each member, addressed by that member,
 * and one trailing, addressed by nothing ({@link #fence(String)} with null).
 * Each is handed to the domain's {@link RelGridFenceContract fence cell} as a
 * table hands a slot to a cell; a slot nobody fills takes no height. The
 * group knows no caption.</p>
 *
 * <h2>What is shared: column geometry</h2>
 *
 * <p>The one thing separate tables cannot agree on by themselves. The group
 * applies its widths to every member at construction, hears any member's
 * resize report, applies the change to the siblings through their own
 * {@code setColumnWidth}, and reports once through {@code onColumnResized}.
 * A sibling that refuses because it is locked is levelled the moment it is
 * free. {@link #columnWidths()} is what the members accepted — bounded by
 * them, never here.</p>
 *
 * <h2>One header, or one each</h2>
 *
 * <p>{@code header: 'group'} (the default): the group mints a table of its
 * own at the very top, above the first fence — a grid over the members'
 * columns that presents nothing, the illustration's trick — whose header
 * band is the group's, resize handles and all, levelled with the rest; every
 * member is built with none. Its labels are the first member's; its columns
 * must be every member's, checked once at construction. {@code header:
 * 'each'}: the group adds no table and every member keeps whatever its own
 * options say.</p>
 *
 * <h2>One cursor, and Tab between the stops</h2>
 *
 * <p>Every member keeps a cursor of its own, and the group presents one: the
 * <b>active</b> member's — the one whose table last held the focus, observed
 * and never asked of the table, or the one {@link #activate(String)} named.
 * Its box wears {@code hrg-active}; the others show neither cursor nor
 * selection, their state untouched. Tab and Shift+Tab walk the group's
 * stops — fence, table, fence, table, …, trailing fence — wrapping within
 * the group; an unfilled fence, a folded member's table and a table with
 * nothing to present are skipped, and a fence stop lands on its first control
 * when it has one. Arrows step over an edge one stop at a time, without
 * wrapping: the table reports a bare arrow that went nowhere ({@code onEdge}),
 * and the group moves up to the fence above or down to the fence below; from
 * a fence, down enters the table below on its first row and up the table
 * above on its last, in the column the cursor left.</p>
 *
 * <h2>Fold, and the channel's other direction</h2>
 *
 * <p>Fold is the group's own state: which members show their table. A folded
 * member's <b>box</b> is hidden and its fence stays; the table inside is
 * untouched and never learns. {@link #fold(String, boolean)},
 * {@link #foldAll(boolean)} and {@link #folded(String)} are the host's;
 * {@code onFolded(id, folded)} is the report; a fence that offers
 * {@code onFolded(folded)} is told when the member below it changes, by
 * whatever road. {@link #tell(Object)} is the domain saying, unasked — a
 * protocol value such as {@code RelGridGroupFold}, the same kind a fence's
 * handle carries — applied as the verb would be; an unknown kind is recorded
 * and refused.</p>
 *
 * <p>Not here, by design: the group's own ask channel, cursor crossing
 * between members and a selection that spans them. Each is a later round;
 * none will be applied to a member's rows.</p>
 */
public interface RelGridGroupContract {
    Object  members();                              // the ids, in order
    Object  member(String id);                      // the member's ordinary RelGrid, or null
    Object  fence(String id);                       // the slot above the member (null: the trailing one), or null
    // ─── one cursor ──────────────────────────────────────────────────────
    Object  active();                               // the id of the member whose cursor is the group's
    boolean activate(String id);                    // make it active and give its table the focus; false for no such member
    // ─── fold, and tell ──────────────────────────────────────────────────
    boolean fold(String id, boolean folded);        // hide or show the member's box; true when anything changed. Unknown id throws
    void    foldAll(boolean folded);                // every member; one report per member that changed
    boolean folded(String id);                      // is the member's box hidden
    boolean tell(Object message);                   // the domain saying, unasked: a protocol value, applied as a verb would be
    // ─── the one shared thing ────────────────────────────────────────────
    void    setColumnWidths(Object snapshot);       // the group's widths, applied to every member
    Object  columnWidths();                         // what the members hold — a plain object the host may keep
    // ─── lifecycle ───────────────────────────────────────────────────────
    Object  el();                                   // the group's root element
    void    destroy();                              // destroys every member's grid; disposes no fence cell

    String   JS_CLASS_NAME = "RelGridGroup";
    String[] CALLBACK_OPTION_NAMES = { "onColumnResized", "onFolded" };
}
