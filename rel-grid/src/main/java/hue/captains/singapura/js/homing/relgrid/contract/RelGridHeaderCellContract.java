package hue.captains.singapura.js.homing.relgrid.contract;

/**
 * RFC 0050 · Episode 2 — a <b>header cell</b>, as the grid sees it: a noun,
 * exactly as a cell is. The relation answers {@code headerFor(column)} the
 * way it answers {@code cellFor(pk, column)}; the grid asks it once for
 * {@code headerElement()} and <i>places</i> what comes back in the header
 * slot its column maps to — the {@code <th>} — and that is the whole of what
 * crosses. The slot stays the grid's: the resize handle on its edge, whether
 * it sticks, which slots exist at all (the column view). What the slot
 * <i>says</i> is the domain's: the label, a sort caret, a filter funnel, a
 * unit, a tooltip — none of which the grid learns exists.
 *
 * <p><b>Where a header cell differs from a cell.</b> The body is the cursor's
 * space, so a click on a slot there is a cursor intent the grid captures and
 * the cell is only <i>told</i>. The header is outside that space: a click on a
 * header cell's element means what the header cell says it means, and the
 * header cell wires it on its own element. The grid captures nothing on the
 * header but its own handle. A header cell is told nothing and offered
 * nothing; it has no modes.</p>
 *
 * <p><b>The registry rule is the cell's.</b> Asked once per presentation of
 * the column, placed, forgotten when the column leaves the column view, asked
 * again if it returns; never disposed by the grid. A relation with no
 * {@code headerFor} gets the grid's own text in the slot — the label the
 * relation declares through {@code labels()}, or the column's name — which
 * is the header a plain relation has always had.</p>
 *
 * <p>Later, the same shape carries map 26: a header cell that answers
 * {@code colSpan()} is placed over several header slots, as a merged cell is
 * placed over several body slots.</p>
 */
public interface RelGridHeaderCellContract {
    Object headerElement();            // the header cell's own element, minted ONCE on its branch; the grid places it
    void   dispose();                  // the domain's to call, never the grid's

    String[] REQUIRED_METHODS = { "headerElement" };
    String[] ALL_METHODS      = { "headerElement", "dispose" };
}
