package hue.captains.singapura.js.homing.relgrid.contract;

import java.util.List;

/**
 * RFC 0050 · Episode 2 — the root relation: what a domain hands a grid, and the
 * <b>whole</b> of what the grid may ask of it.
 *
 * <p>A relation is an immutable value — its identities, its columns and a
 * <b>cell manager</b>. It carries no value the grid could read, because the
 * grid reads none: a cell is the domain's, and the domain updates it directly
 * for its whole life. The old {@code get / update / subscribe / columnMeta}
 * are gone from this contract because each of them was a value crossing into
 * the grid (RFC 0050 · Episode 2 — Appendix: The Audit, findings 2, 4, 5,
 * 6).</p>
 *
 * <p><b>{@code cellFor} is a manager, not a factory.</b> The grid asks for a
 * cell <b>once per identity per grid lifetime</b> and keeps the instance; the
 * domain creates the cell on first ask and may hold it for as long as it
 * likes — a cell outlives the grid that placed it (Map 17, law 120). The grid
 * never disposes what it did not create.</p>
 *
 * <p>These three are <b>not</b> on the ask channel and never will be
 * (ext4, law 191). A question may go unrecognised, may be declined, may be
 * answered with nothing — and a thing that does not answer {@code cellFor} is
 * not a relation at all. They are the relation's shape rather than questions
 * put to it.</p>
 *
 * <p>Fixed for a grid's lifetime: a row that arrives is a new relation and a
 * new grid (Map 20, law 139). A row that goes is absorbed by a later View —
 * outside this round's scope, which is arrangement alone.</p>
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
    List<String> pks();                        // every identity, once, in base order — the identity space
    List<String> columns();                    // the column structure, in base order
    Object       cellFor(String pk, String column);   // the cell manager: the domain's cell for one identity

    /** OPTIONAL. Columns the grid must never ask for control; read once, at construction. */
    List<String> readOnlyColumns();

    /** The method names the JS relation object must expose. Nothing about values. */
    String[] METHOD_NAMES = { "pks", "columns", "cellFor" };
    /** Read once at construction if present; absence is no constraint. */
    String[] OPTIONAL_METHOD_NAMES = { "readOnlyColumns" };
}
