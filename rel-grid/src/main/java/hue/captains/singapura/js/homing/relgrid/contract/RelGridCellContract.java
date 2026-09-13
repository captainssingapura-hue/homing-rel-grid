package hue.captains.singapura.js.homing.relgrid.contract;

/**
 * RFC 0050 · Episode 2 — a cell, as the <b>grid</b> sees it. The smallest
 * surface in the family: the grid provides a slot and never fills it (Map 24,
 * law 175), so everything a cell shows, holds, edits or commits is between the
 * cell and its domain, and none of it is on this contract.
 *
 * <p><b>A cell is a noun.</b> It owns its element — minted on a DomOpsParty
 * branch of the domain's, never the grid's — and the grid's whole dealing with
 * it is to ask {@code cellElement()} once and <i>place</i> what comes back in
 * the slot its identity maps to. Nothing is rendered into anything: the two
 * branches are independent, and a cell has no behaviour towards the grid at
 * all. The editor is a noun the same way: {@code editorElement()} is placed by
 * the grid in an anchor over the slot, and only then is control offered.</p>
 *
 * <p><b>The cell never takes control unasked.</b> A single click is a shallow
 * gesture and belongs to the grid; a cell has no click handler of its own. It
 * goes deep only when the grid offers, and the offer is made in <b>two
 * stages</b> — because a feature that needs to know whether a cell may be
 * written must be able to ask <i>without opening it</i>. Bulk edit and paste
 * are written against exactly that (Map 18, law 115), and a single
 * take-it-or-leave-it call cannot serve them: asking would <i>be</i> taking.</p>
 *
 * <ol>
 *   <li>{@code mayTakeControl()} — may this cell take control <b>right now</b>?
 *       Situational, synchronous, and asked afresh every time: whether this row
 *       is locked today, whether this person has the right, whether the value
 *       is derived. Only an explicit {@code true} is a yes.</li>
 *   <li>{@code editorElement()} — the editor, as an element the cell owns:
 *       minted on its branch, kept, and answered the same every time. The
 *       grid places it in an anchor laid over the slot but outside the table,
 *       so an editor may be larger than its cell and costs the table nothing.</li>
 *   <li>{@code takeControl()} — the handover itself, made only after a yes and
 *       once the editor is placed. <b>It must return a thenable</b>, and the
 *       grid holds control for the cell until that thenable settles — and
 *       gives the editor the focus. Settling means <i>finished</i>, not
 *       <i>committed</i>: the grid never learns which (law 108).</li>
 * </ol>
 *
 * <p>The stages are back to back in one task, so nothing can change between
 * them — that is the reason {@code mayTakeControl} is synchronous, beyond its
 * being cheap enough to ask on every gesture. A cell that genuinely needs time
 * to decide should say yes and decide <i>inside</i>: it owns the keyboard and
 * its editor by then, and can show its own progress there rather than making
 * the whole grid wait.</p>
 *
 * <p><b>Why a returned thenable and not a handed-in callback.</b> A callback
 * would be a capability the grid hands out — storable, callable twice, callable
 * after the grid is gone. A thenable reverses the direction: the cell hands the
 * grid a <i>notification</i>, the grid exposes nothing, and settling once is
 * the language's guarantee rather than a flag somebody has to remember. It also
 * makes an ordering bug impossible, because a settle can never run before the
 * grid has finished the bookkeeping around the call.</p>
 *
 * <p>The other members:</p>
 *
 * <ul>
 *   <li>{@code cellElement()} — the cell's element, minted once on the cell's
 *       own branch and answered the same every time; the grid places it. No
 *       value is passed: the cell already knows what it shows.</li>
 *   <li>{@code onSelect(mode)} — told {@code 'none' | 'shallow' | 'deep'}.
 *       Pure lifecycle; never focuses, never paints the cursor (the layout
 *       paints it on the slot). There is deliberately <b>no {@code 'sel'}</b>:
 *       a selected cell is not told, because selection is a predicate the theme
 *       paints and telling a two-hundred-row range on every keystroke would be
 *       a great many calls for nothing (Map 5, law 217).</li>
 *   <li>{@code dispose()} — <b>never called by the grid.</b> The cell
 *       manager's, when the domain decides the cell is done (Map 17, law 120).</li>
 * </ul>
 *
 * <p><b>{@code colSpan()}</b> is optional, and only read when the grid was
 * built with {@code mergedCells}: how many columns this cell reaches over,
 * counting its own. A cell answering more than one is a <i>leading</i> cell;
 * the grid unclips its slot and drops the grid lines it reaches across, and
 * the cell draws itself that wide. Read on every arrangement, so a span that
 * moves is an arrangement the host asks for. Nothing about the slots it
 * reaches over changes: they keep their identities and their own cells.</p>
 *
 * <p>Not here, on purpose: {@code update(value)}, {@code getValue()},
 * {@code commitEdit()}, {@code cancelEdit()}, {@code effectiveType()},
 * {@code preview()}. Edit, commit and update are domain operations; they never
 * change the arrangement of cells, so the grid drives none of them.</p>
 *
 * <p><b>Failing safe.</b> A cell missing any of the three control methods is
 * never offered control. A {@code mayTakeControl} that answers anything but
 * {@code true} — including nothing at all, which is what a forgotten
 * {@code return} produces — is a no. An {@code editorElement} that answers
 * anything but an element, or a {@code takeControl} that answers without a
 * thenable, breaks this contract, and the grid records it and stays shallow
 * rather than waiting forever for a settle that cannot come.</p>
 */
public interface RelGridCellContract {
    Object  cellElement();             // the cell's own element, minted ONCE on its branch; the grid places it
    void    onSelect(String mode);     // 'none' | 'shallow' | 'deep' — pure lifecycle
    boolean mayTakeControl();          // stage 1: situational, synchronous; only true is yes
    Object  editorElement();           // the editor, the cell's own element; the grid places it in an anchor
    Object  takeControl();             // stage 2: MUST answer a thenable; settling means finished
    void    dispose();                 // the domain's to call, never the grid's
    int     colSpan();                 // OPTIONAL: columns this cell reaches over; only with mergedCells

    String[] REQUIRED_METHODS = { "cellElement" };
    /** A cell may leave these out entirely; absent means the default. */
    String[] OPTIONAL_METHODS = { "colSpan" };
    /** All three or none: a cell offering part of the handover is offered nothing. */
    String[] CONTROL_METHODS  = { "mayTakeControl", "editorElement", "takeControl" };
    String[] ALL_METHODS      = { "cellElement", "onSelect", "mayTakeControl", "editorElement", "takeControl", "dispose", "colSpan" };
}
