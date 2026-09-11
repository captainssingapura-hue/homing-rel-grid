package hue.captains.singapura.js.homing.relgrid.protocol;

import java.util.List;

/**
 * The grid telling the domain what is now selected — RFC 0050 · Episode 2,
 * ext4's first kind and ext6's first notification.
 *
 * <p><b>A question with no answer.</b> The grid does not wait for it, does not
 * mask for it, and learns nothing from it; ext6's law 229 says a notification
 * never masks, because nothing is pending on it. What the domain does with it
 * is entirely the domain's.</p>
 *
 * <p>The ranges are the <b>resolved</b> selection: the list as it was made, or
 * the cursor's own 1×1 when nothing was made, so a reader never needs a case
 * for "nothing selected" (map 5 law 40). Order is creation order and overlap is
 * intact — the grid imposes no shape rule (law 44), so what arrives here is
 * what the person made.</p>
 */
public record RelGridSelectionChanged(List<RelGridRange> ranges) {

    public RelGridSelectionChanged {
        ranges = List.copyOf(ranges);
    }
}
