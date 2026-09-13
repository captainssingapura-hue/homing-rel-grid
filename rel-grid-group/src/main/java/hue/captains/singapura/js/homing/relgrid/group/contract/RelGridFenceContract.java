package hue.captains.singapura.js.homing.relgrid.group.contract;

/**
 * RFC 0050 · Episode 2 — a FENCE CELL: what the domain puts in the slot the
 * group mints between two members, or around the ends.
 *
 * <p>A fence is a noun, as a cell is. It owns its element — minted on a
 * DomOpsParty branch of the domain's, never the group's — and the group's
 * whole dealing with it is to ask {@link #fenceElement()} once and <i>place</i>
 * what comes back in the slot it minted. What is in it is the domain's — a
 * title, a published total, an illustration, a control — and the group never
 * reads what was drawn. It never calls {@link #dispose()} either: disposal is
 * the owner's, as it is for a cell.</p>
 *
 * <p>A fence has no handle and no way to the group: a control it draws that
 * must TELL the group something (a fold toggle, say) is wired by the
 * <b>host</b> at construction, with a closure onto the group's own
 * {@code tell} — the domain talks to the host, the host to the group, and the
 * fence knows neither. {@link #onFolded(boolean)} is optional — pure lifecycle,
 * the way a cell is told its selection mode — so a toggle can follow a fold
 * made by any road, from what it is told rather than from a memory of its own.</p>
 */
public interface RelGridFenceContract {
    Object fenceElement();               // the fence's own element, minted ONCE on its branch; the group places it
    void   onFolded(boolean folded);     // OPTIONAL: the member below folded or unfolded
    void   dispose();                    // the owner's, never the group's

    String[] METHOD_NAMES = { "fenceElement", "onFolded", "dispose" };
    String[] OPTIONAL_METHODS = { "onFolded" };
}
