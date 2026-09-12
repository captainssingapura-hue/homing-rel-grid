package hue.captains.singapura.js.homing.relgrid.contract;

/**
 * RFC 0050 · Episode 2 — the grid facade's surface: arrangement, a cursor, a
 * selection, the shallow/deep discipline, column widths, and one channel to the
 * domain — and nothing that is somebody else's.
 *
 * <pre>{@code
 *   new RelGrid({
 *       container,          // where the layout mounts
 *       branch,             // the CELLS branch (DomOpsParty) — hands out cell host elements
 *       relation,           // RootRelationContract shape: pks(), columns(), cellFor(pk, column)
 *       label?,             // aria-label for the table
 *       header?,            // { show?: boolean, labels?: {column: text} } — display only
 *       ask?,               // (question) → thenable — THE CHANNEL (ext4, ext6)
 *       onArranged?,        // (kind) — after every placement pass
 *       onCursorMoved?,     // (pk, column)
 *       onControlTaken?,    // (pk, column) — the cell took control of this one
 *       onControlReleased?, // (pk, column) — and gave it back
 *       onColumnResized?    // (column, px) — a REPORT of what is now held
 *   })
 * }</pre>
 *
 * <h2>The channel</h2>
 *
 * <p>{@code ask} is the one seam to the domain: typed questions down, typed
 * answers up, with the payloads generated from Java records so a shape cannot
 * drift from its declaration (ext5). Its first customer is a <b>notification</b>
 * — a question expecting no answer — so the grid hands it over, does not wait
 * and does not mask, because nothing is pending on it (ext6, law 229). A
 * rejected notification is still recorded: fire-and-forget is not
 * fire-and-ignore. A host with no {@code ask} simply has no channel.</p>
 *
 * <h2>Shallow and deep are enforced here</h2>
 *
 * <p>A single click, a drag, or an arrow moves the cursor — an identity the
 * grid holds — and the cell is only told. Deep is entered only through the
 * grid, by Enter or a double-click on the cursor's cell, and the offer is made
 * in <b>two stages</b>: the column's declared constraint, which is answered
 * without touching the cell (map 16, law 112), and then the cell's own
 * {@code mayTakeControl()}, asked afresh every time (law 113). Only then
 * {@code takeControl()}, which must answer a thenable; the grid holds deep
 * until it settles, resolved or rejected alike, and never learns what happened
 * inside.</p>
 *
 * <p>{@code mayTakeControlAtCursor()} is public because a feature must be able
 * to ask whether a cell is writable <b>without opening it</b> — which is what
 * bulk edit and paste are written against (map 18, law 115).</p>
 *
 * <h2>The selection</h2>
 *
 * <p>An ordered list of position rectangles, never merged and never reordered
 * (map 5). Shift extends the last range's far corner without moving the cursor;
 * ctrl appends a 1×1 and moves there; {@code Ctrl+A} takes the presented space;
 * a bare move clears; every arrangement clears, because a range is positions
 * and those are not the same positions. {@code selectedRanges()} answers the
 * <b>resolved</b> list, so an empty one reads as the cursor's own 1×1 and no
 * caller needs a case for "nothing selected" (law 40).</p>
 *
 * <h2>Copy is a question the grid waits for</h2>
 *
 * <p>Map 6 and ext6: {@code copy()} — Ctrl+C's twin — resolves the selection
 * to identities, one block per range, and asks the channel what they are
 * worth. While the answer is outstanding {@code isPending()} is true and the
 * grid is <b>locked</b>: every intent above, and its programmatic twin here,
 * is refused rather than deferred. The answer is finished content, which the
 * grid writes through {@link #CLIPBOARD_OPTION_NAME} and reports through
 * {@code onCopied}; absence writes nothing. Clear and bulk are later rounds.</p>
 *
 * <h2>The rows' arrangement is a question too</h2>
 *
 * <p>A View is which of the root's identities are shown and in what order,
 * and it is the domain's to compute. {@code handoverView()} — the verb
 * behind a control in the host's own chrome, and the twin of Alt+Enter on
 * the table — asks {@code RelGridViewHandover} with the mask handle and
 * waits: the domain gathers its own conditions on the panel and answers a
 * {@code RelGridView}, which the grid presents exactly as given, or nothing.
 * The grid holds nothing about why the rows are in that order; that
 * explanation is the domain's. The grid attaches no control of its own to
 * the question, because a domain's arrangement is not a property of any
 * column. The other way rows come to be in an order — a specification the
 * grid gathers with its own caret and asks the relation to apply — is a
 * later round.</p>
 *
 * <h2>Widths are geometry, the grid's alone</h2>
 *
 * <p>Map 7, laws 53–60: held by column identity, applied by position, in place —
 * no arrangement runs. A request is bounded to {@code [40, 2000]} at
 * normalisation — the floor is the host's to lower, to no less than 8, for a
 * geometry of its own such as a half-square column — and the bounded request
 * is what is held. {@code columnWidths()}
 * returns what is held, never what was measured; {@code setColumnWidths(snapshot)}
 * drops unknown columns as drift and is idempotent. The grid persists nothing —
 * {@code onColumnResized} is a report, and keeping it is the host's (map 9).</p>
 *
 * <p>There is no {@code updateCell}, {@code flushNow}, {@code sortBy},
 * {@code filterRows}, {@code commitEdit} or {@code cancelEdit}. The first two
 * carried values; the next two are the relation's to answer with a View —
 * today through the handover, and a sort the grid gathers itself in a later
 * round; the last two are the cell's, never the grid's.</p>
 */
public interface RelGridContract {
    void    reapply();                              // arrange again; carries nothing
    boolean selectCell(String pk, String column);   // programmatic shallow cursor; a BARE move, so it clears
    Object  cursor();                               // { pk, column } or null — never a position
    boolean isDeep();                               // the one fact the grid holds about editing
    boolean isPending();                            // the second: a question is outstanding, and the grid is locked
    void    focus();                                // the keyboard host
    // ─── the handover, in two stages (maps 15, 16) ───────────────────────
    boolean mayTakeControlAtCursor();               // ask WITHOUT opening: constraint, then the cell
    boolean takeControlAtCursor();                  // offer control; false if either stage refuses
    // ─── the selection (map 5) ───────────────────────────────────────────
    boolean extendSelection(String pk, String column);  // shift: move the last range's far corner
    boolean addToSelection(String pk, String column);   // ctrl: append a 1x1 and go there
    boolean selectAll();                                // one range over the presented space
    void    clearSelection();
    Object  selectedRanges();                       // the RESOLVED list of { i0, j0, i1, j1 }
    int     selectionCount();                       // how many ranges were MADE; zero is the cursor's own
    // ─── copy (map 6, ext6) ──────────────────────────────────────────────
    boolean copy();                                 // ask what the selection is worth; write the answer. False when locked or channel-less
    // ─── the view handover ───────────────────────────────────────────────
    boolean handoverView();                         // hand the rows' arrangement to the domain; present its View. False when locked or channel-less
    // ─── widths (map 7) ──────────────────────────────────────────────────
    boolean setColumnWidth(String column, double px);   // bounded, held by identity, applied in place; false for drift
    Object  columnWidth(String column);                 // what is held, or null
    Object  columnWidths();                             // the snapshot — a plain object the host may keep
    void    setColumnWidths(Object snapshot);           // restore; unknown columns dropped; idempotent
    // ─── seams + lifecycle ───────────────────────────────────────────────
    Object  viewMaps();                             // the identity/position seam (RelGridViewMaps)
    Object  cells();                                // the cell registry, for a host that must reach one
    Object  el();                                   // the table element
    void    destroy();                              // detaches every cell and removes the table; DISPOSES NOTHING

    String   JS_CLASS_NAME = "RelGrid";
    String[] CALLBACK_OPTION_NAMES = {
            "onArranged", "onCursorMoved", "onControlTaken", "onControlReleased", "onColumnResized", "onCopied" };
    /** The channel is not a callback: it is asked, and it answers. */
    String   CHANNEL_OPTION_NAME = "ask";
    /** The clipboard writer: {@code { write(content) → thenable }}. The async Clipboard API unless the host says otherwise. */
    String   CLIPBOARD_OPTION_NAME = "clipboard";
}
