package hue.captains.singapura.js.homing.relgrid.contract;

/**
 * RFC 0050 · Episode 2 — the grid facade's surface for round 1: arrangement,
 * a cursor, the shallow/deep discipline, and column widths — and nothing that
 * is somebody else's.
 *
 * <pre>{@code
 *   new RelGrid({
 *       container,        // where the layout mounts
 *       branch,           // the CELLS branch (DomOpsParty) — hands out cell host elements
 *       relation,         // RootRelationContract shape: pks(), columns(), cellFor(pk, column)
 *       label?,           // aria-label for the table
 *       header?,          // { show?: boolean, labels?: {column: text} } — display only
 *       onArranged?,      // (kind) — after every placement pass
 *       onCursorMoved?,   // (pk, column)
 *       onEditStarted?,   // (pk, column) — the grid went deep on this cell
 *       onEditEnded?,     // (pk, column) — the cell handed control back
 *       onColumnResized?  // (column, px) — a REPORT of what is now held
 *   })
 * }</pre>
 *
 * <p><b>Shallow and deep are enforced here.</b> A single click or an arrow key
 * moves the cursor — an identity the grid holds — and the cell is only told.
 * Deep is entered only through the grid, by Enter or a double-click on the
 * cursor's cell: the grid calls {@code cell.beginEdit(release)}, and while the
 * cell holds control the grid's capture is inert. The cell calls
 * {@code release()} when done; the grid resumes and never learns what happened
 * inside.</p>
 *
 * <p><b>Widths are geometry, the grid's alone</b> (Map 7, laws 53–60): held by
 * column identity, applied by position, in place — no arrangement runs. A
 * request is bounded to {@code [40, 2000]} at normalisation and the bounded
 * request is what is held. {@code columnWidths()} returns what is held, never
 * what was measured; {@code setColumnWidths(snapshot)} drops unknown columns as
 * drift and is idempotent. The grid persists nothing — {@code onColumnResized}
 * is a report, and keeping it is the host's (Map 9).</p>
 *
 * <p>There is no {@code updateCell}, {@code flushNow}, {@code sortBy},
 * {@code filterRows}, {@code commitEdit} or {@code cancelEdit}. The first two
 * carried values; the next two are the relation's to answer with a View, in a
 * later round; the last two are the cell's, never the grid's.</p>
 */
public interface RelGridContract {
    void    reapply();                              // arrange again; carries nothing
    boolean selectCell(String pk, String column);   // programmatic shallow cursor; ignored while deep
    Object  cursor();                               // { pk, column } or null — never a position
    boolean beginEditAtCursor();                    // ask the cursor's cell to go deep; false if it declines
    boolean isDeep();                               // the one fact the grid holds about editing
    void    focus();                                // the keyboard host
    // ─── widths (Map 7) ──────────────────────────────────────────────────
    boolean setColumnWidth(String column, double px);   // bounded, held by identity, applied in place; false for drift
    Object  columnWidth(String column);                 // what is held, or null
    Object  columnWidths();                             // the snapshot — a plain object the host may keep
    void    setColumnWidths(Object snapshot);           // restore; unknown columns dropped; idempotent
    // ─── seams + lifecycle ───────────────────────────────────────────────
    Object  viewMaps();                             // the identity/position seam (RelGridViewMaps)
    Object  el();                                   // the table element
    void    destroy();                              // detaches every cell and removes the table; DISPOSES NOTHING

    String   JS_CLASS_NAME = "RelGrid";
    String[] CALLBACK_OPTION_NAMES = { "onArranged", "onCursorMoved", "onEditStarted", "onEditEnded", "onColumnResized" };
}
