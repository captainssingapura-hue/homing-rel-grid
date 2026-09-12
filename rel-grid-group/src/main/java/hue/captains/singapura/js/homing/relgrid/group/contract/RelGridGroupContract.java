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
 * them, never here. With {@code sharedHeader} (the default) the first
 * member's header is the group's and the rest show none.</p>
 *
 * <p>Not here, by design: fold, the group's own ask channel, the unsolicited
 * {@code tell}, cursor crossing between members and a selection that spans
 * them. Each is a later round; none will be applied to a member's rows.</p>
 */
public interface RelGridGroupContract {
    Object  members();                              // the ids, in order
    Object  member(String id);                      // the member's ordinary RelGrid, or null
    Object  fence(String id);                       // the slot above the member (null: the trailing one), or null
    // ─── the one shared thing ────────────────────────────────────────────
    void    setColumnWidths(Object snapshot);       // the group's widths, applied to every member
    Object  columnWidths();                         // what the members hold — a plain object the host may keep
    // ─── lifecycle ───────────────────────────────────────────────────────
    Object  el();                                   // the group's root element
    void    destroy();                              // destroys every member's grid; disposes no fence cell

    String   JS_CLASS_NAME = "RelGridGroup";
    String[] CALLBACK_OPTION_NAMES = { "onColumnResized" };
}
