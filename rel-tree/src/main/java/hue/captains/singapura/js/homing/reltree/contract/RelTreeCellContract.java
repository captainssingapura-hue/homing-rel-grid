package hue.captains.singapura.js.homing.reltree.contract;

/**
 * RFC 0050 · Episode 3-ext1 — a tree cell: the grid's cell, smaller. The
 * domain's noun for one node; the tree asks it once for its element and places
 * that after its caret; the domain owns it for its whole life.
 *
 * <p>There is no {@code mayTakeControl} and no editor: a tree cell is never
 * entered. What Enter means on it is a notification on the channel, and what
 * a click inside it means is the cell's own listener.</p>
 */
public interface RelTreeCellContract {
    Object cellElement();              // the cell's own element, minted ONCE on the domain's branch; the tree places it
    void   onSelect(String mode);      // OPTIONAL: 'none' | 'shallow' — the grid's words; the tree never says 'deep'
    void   dispose();                  // the domain's to call, never the tree's

    String[] ALL_METHODS = { "cellElement", "onSelect", "dispose" };
    /** Absent means the default: a cell that is told nothing. */
    String[] OPTIONAL_METHODS = { "onSelect" };
}
