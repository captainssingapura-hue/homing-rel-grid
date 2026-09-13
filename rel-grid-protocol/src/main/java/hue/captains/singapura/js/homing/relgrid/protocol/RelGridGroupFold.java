package hue.captains.singapura.js.homing.relgrid.protocol;

/**
 * A member of a group folded, or unfolded — RFC 0050 · Episode 2, the group,
 * and the first message that travels the channel's <b>other</b> way.
 *
 * <p>The grid asks and the domain answers; this is the domain <i>telling</i>,
 * unasked: a control the domain drew in a fence — the slot between two
 * tables — was pressed, and the group is to fold the member below it. It is
 * an answer nobody asked for, and it rides the same catalogue as the answers
 * do, through {@code tell(message)} on the group or on the handle a fence is
 * given.</p>
 *
 * <p>A fold is the group's own state — which members show their table — and
 * is applied by the group to the member's <b>box</b>. The table inside is
 * untouched: its cursor, its selection, its cells and its view are as they
 * were, and it never learns it was folded.</p>
 */
public record RelGridGroupFold(String member, boolean folded) {
    public RelGridGroupFold {
        if (member == null || member.isEmpty())
            throw new IllegalArgumentException("RelGridGroupFold names the member");
    }
}
