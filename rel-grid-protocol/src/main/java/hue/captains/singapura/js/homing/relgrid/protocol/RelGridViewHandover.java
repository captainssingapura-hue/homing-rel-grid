package hue.captains.singapura.js.homing.relgrid.protocol;

/**
 * The grid handing the <b>arrangement of its rows</b> to the domain — RFC
 * 0050 · Episode 2, the second question the grid waits for, and the first of
 * the two view questions.
 *
 * <p>There are two ways rows come to be in an order. In one the grid holds a
 * <i>specification</i> — a sort it gathered with its own caret — and asks the
 * relation to apply it; the grid can then explain the arrangement, because it
 * holds what was asked. This is the other: the grid holds nothing and hands
 * control over. The domain gathers its own conditions, by whatever controls it
 * likes, on the mask's panel the grid lends it; it answers with a
 * {@link RelGridView}, or with nothing to leave the rows as they are; and the
 * explanation of <i>why</i> the rows are in that order is the domain's alone,
 * kept and shown in its own chrome. The grid places what it is handed and
 * asserts nothing about how one View relates to the next (map 1, law 8).</p>
 *
 * <p>{@code column} is the column the gesture came from — the header's control,
 * or the cursor's column for the keyboard — offered as context. A domain whose
 * panel is about the table as a whole may ignore it.</p>
 *
 * <p>While it is outstanding the grid is locked and masked (ext6, law 220),
 * which is what makes the domain's panel modal: conditions are gathered, then
 * applied once, on the answer.</p>
 */
public record RelGridViewHandover(String column) {
    public RelGridViewHandover {
        if (column == null || column.isEmpty())
            throw new IllegalArgumentException("RelGridViewHandover names the column the gesture came from");
    }
}
