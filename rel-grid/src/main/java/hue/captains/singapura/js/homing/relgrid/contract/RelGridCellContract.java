package hue.captains.singapura.js.homing.relgrid.contract;

/**
 * RFC 0050 · Episode 2 — a cell, as the <b>grid</b> sees it. The smallest
 * surface in the family: the grid provides a slot and never fills it (Map 24,
 * law 175), so everything a cell shows, holds, edits or commits is between the
 * cell and its domain, and none of it is on this contract.
 *
 * <p><b>The cell never takes control.</b> A single click is a shallow gesture
 * and belongs to the grid; a cell has no click handler of its own. It goes deep
 * only when the grid asks, and gives control back by calling the handle it was
 * given — once, whatever happened inside.</p>
 *
 * <ul>
 *   <li>{@code render(host)} — mount once into the element the grid minted.
 *       No value is passed: the cell already knows what it shows.</li>
 *   <li>{@code onSelect(mode)} — told {@code 'none' | 'shallow' | 'deep'}.
 *       Pure lifecycle; never focuses, never paints the cursor (the layout
 *       paints it on the slot).</li>
 *   <li>{@code beginEdit(release)} — <i>optional</i>. The grid asks the cell to
 *       go deep; the cell returns {@code false} to decline (read-only, or
 *       already deep) and the grid stays shallow. Otherwise the cell owns the
 *       keyboard until it calls {@code release()}. The grid never learns
 *       whether the edit was committed or cancelled — only that it ended.</li>
 *   <li>{@code dispose()} — <b>never called by the grid.</b> The cell
 *       manager's, when the domain decides the cell is done (Map 17, law 120).</li>
 * </ul>
 *
 * <p>Not here, on purpose: {@code update(value)}, {@code getValue()},
 * {@code commitEdit()}, {@code cancelEdit()}, {@code effectiveType()},
 * {@code preview()}. Edit, commit and update are domain operations; they never
 * change the arrangement of cells, so the grid drives none of them.</p>
 */
public interface RelGridCellContract {
    void    render(Object host);       // mount ONCE into the grid-minted element; no value crosses
    void    onSelect(String mode);     // 'none' | 'shallow' | 'deep' — pure lifecycle
    boolean beginEdit(Object release); // optional: go deep, or return false; call release() when done
    void    dispose();                 // the domain's to call, never the grid's

    String[] REQUIRED_METHODS = { "render" };
    String[] ALL_METHODS      = { "render", "onSelect", "beginEdit", "dispose" };
}
