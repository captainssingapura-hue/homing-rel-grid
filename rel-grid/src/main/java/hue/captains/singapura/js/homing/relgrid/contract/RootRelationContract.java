package hue.captains.singapura.js.homing.relgrid.contract;

import java.util.List;

/**
 * RFC 0050 · Episode 2 — the root relation: what a domain hands a grid, and the
 * <b>whole</b> of what the grid may ask of it.
 *
 * <p>A relation is its column structure, a <b>cell manager</b>, and a <b>View
 * answered on request</b>. It carries no value the grid could read, because
 * the grid reads none: a cell is the domain's, and the domain updates it
 * directly for its whole life. The old {@code get / update / subscribe /
 * columnMeta} are gone from this contract because each of them was a value
 * crossing into the grid (RFC 0050 · Episode 2 — Appendix: The Audit, findings
 * 2, 4, 5, 6) — and {@code pks()} is gone because it was an enumeration the
 * grid never needed.</p>
 *
 * <h2>One root: the View is answered, never listed</h2>
 *
 * <p>{@code view()} answers the rows to <b>present now</b>, in order: the
 * whole of a static relation, a window of an endless one. The grid asks it at
 * construction and holds exactly what it answered — the row axis is the View
 * and has no base — so a grid over a relation of any size holds as many keys
 * as it shows, and there is nothing to enumerate. The same seam asked to
 * <b>move</b>, {@code view({ by: n })} — n rows on, negative back — answers the
 * moved window's keys, or <b>nothing</b>: the rows stay as they are. A static
 * relation answers nothing to every movement, because its View is the whole
 * of it and has nowhere to go; the grid then reports the edge as it always
 * has. This is Episode 2's {@code view(intent)} — intent in, keys out — taken
 * for the second vocabulary it was named for.</p>
 *
 * <p>Nothing about a relation is fixed for a grid's lifetime but its columns
 * and its cell manager. A row that arrives is in the next View; a row that
 * goes is absent from it — the same sentence for a keystroke in an article and
 * a page from a server. (Map 20's law 139 — a row that arrives is a new
 * relation and a new grid — protected a base the grid no longer keeps.)</p>
 *
 * <h2>Membership is the relation's</h2>
 *
 * <p>The grid keeps no list of what exists, so it cannot tell a stranger from
 * a row — and it does not try. The relation can: <b>{@code cellFor} refuses an
 * identity the relation does not own, by throwing.</b> That is the one check
 * of a View, and the grid puts it before anything moves — every presented
 * identity is asked for before a slot is touched, so a refusal refuses the
 * View whole, the rows go back as they were, and the caller hears it. Half a
 * View is not a View; the authority on which half is the relation.</p>
 *
 * <p><b>{@code cellFor} is a manager, not a factory.</b> The grid asks for a
 * cell <b>once per presentation</b> — when an identity enters the View — and
 * keeps the instance while the identity is presented; when it leaves, the
 * grid forgets it, and asks again if it returns. The domain creates the cell
 * on first ask and may hold it for as long as it likes — a relation that
 * keeps its cells answers the same one, and a cell outlives the grid that
 * placed it (Map 17, law 120); a relation over an endless space may free a
 * row it no longer presents, and nothing in the grid holds it. The grid never
 * disposes what it did not create.</p>
 *
 * <p>These three are <b>not</b> on the ask channel and never will be
 * (ext4, law 191). A question may go unrecognised, may be declined, may be
 * answered with nothing — and a thing that does not answer {@code cellFor} is
 * not a relation at all. They are the relation's shape rather than questions
 * put to it.</p>
 *
 * <h2>The one optional declaration</h2>
 *
 * <p>{@code readOnlyColumns()} is a <b>constraint</b>, in map 16's sense: it
 * names the columns whose cells the grid must <b>never ask</b> for control
 * (law 112). Read <b>once, at construction</b>, so it is a declaration about
 * structure rather than a question about a moment — which is why it may sit
 * beside the three without weakening law 191.</p>
 *
 * <p>Absent, or empty, means <b>no constraint</b>: every column is askable and
 * each cell decides for itself. That is what a constraint is — a narrowing of
 * what would otherwise be offered — and it is why this names what is
 * <i>forbidden</i> rather than what is allowed. A relation that declares
 * nothing constrains nothing.</p>
 *
 * <p>The two refusals do not substitute for each other (map 16, law 113). This
 * one is structural, unconditional and free: no cell is consulted, no call is
 * made. A cell's own {@code mayTakeControl} is situational and answered afresh
 * every time. A constraint cannot express "not today", and a cell's judgement
 * cannot loosen a constraint, because a constrained cell is never asked.</p>
 */
public interface RootRelationContract {
    List<String> view(Object intent);          // the View: no intent → the rows to present now; { by: n } → moved, or nothing
    List<String> columns();                    // the column structure, in base order
    Object       cellFor(String pk, String column);   // the cell manager: the domain's cell for one identity; throws for a stranger

    /** OPTIONAL. Columns the grid must never ask for control; read once, at construction. */
    List<String> readOnlyColumns();

    /** The method names the JS relation object must expose. Nothing about values. */
    String[] METHOD_NAMES = { "view", "columns", "cellFor" };
    /** Read once at construction if present; absence is no constraint. */
    String[] OPTIONAL_METHOD_NAMES = { "readOnlyColumns" };
}
