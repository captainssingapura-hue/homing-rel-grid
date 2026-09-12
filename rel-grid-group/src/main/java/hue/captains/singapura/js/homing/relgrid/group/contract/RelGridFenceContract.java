package hue.captains.singapura.js.homing.relgrid.group.contract;

/**
 * RFC 0050 · Episode 2 — a FENCE CELL: what the domain puts in the slot the
 * group mints between two members, or around the ends.
 *
 * <p>The group hands the slot over exactly as a table hands a slot to a cell:
 * {@link #render(Object, Object)} once, into an element the group minted,
 * with a <b>handle</b> — {@code { tell(message), folded() }}: the channel's
 * other direction, through which a control the fence drew tells the group
 * something (a {@code RelGridGroupFold}, say), and the one thing it may read
 * of the member below it. What goes in the slot is the domain's — a title, a
 * published total, an illustration, a control. The group never reads what was
 * drawn and never calls {@link #dispose()}: disposal is the owner's, as it is
 * for a cell. {@link #onFolded(boolean)} is optional — pure lifecycle, the way
 * a cell is told its selection mode — so a toggle can follow a fold made by
 * any road.</p>
 */
public interface RelGridFenceContract {
    void render(Object host, Object handle);   // mount once into the element the group minted; keep the handle
    void onFolded(boolean folded);             // OPTIONAL: the member below folded or unfolded
    void dispose();                            // the owner's, never the group's

    String[] METHOD_NAMES = { "render", "onFolded", "dispose" };
    String[] OPTIONAL_METHODS = { "onFolded" };
}
