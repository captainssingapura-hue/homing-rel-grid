package hue.captains.singapura.js.homing.relgrid.protocol;

import java.util.List;

/**
 * The grid asking what the selection is worth on a clipboard — RFC 0050 ·
 * Episode 2, map 6 law 46, and the first question the grid <b>waits</b> for.
 *
 * <p>Unlike {@link RelGridSelectionChanged} this expects an answer: a
 * {@link RelGridClipboardContent}, or nothing. While it is outstanding the
 * grid is masked and the person is stopped (ext6 law 220); the domain may
 * ask the grid for the mask's panel and draw its choices there, and the
 * answer is whatever those choices produce. The grid writes what comes back
 * and learns nothing of how it was made.</p>
 *
 * <p>The selection arrives resolved, one {@link RelGridBlock} per range, in
 * the order the ranges were made. A domain that will not represent an
 * irregular selection answers nothing — the refusal a spreadsheet makes,
 * living in the party qualified to make it (map 6).</p>
 */
public record RelGridCopyRequested(List<RelGridBlock> blocks) {

    public RelGridCopyRequested {
        blocks = List.copyOf(blocks);
        if (blocks.isEmpty()) throw new IllegalArgumentException("RelGridCopyRequested carries at least one block");
    }
}
