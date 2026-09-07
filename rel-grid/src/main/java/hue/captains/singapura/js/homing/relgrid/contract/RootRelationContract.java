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
 * <p>Fixed for a grid's lifetime: a row that arrives is a new relation and a
 * new grid (Map 20, law 139). A row that goes is absorbed by a later View —
 * outside this round's scope, which is arrangement alone.</p>
 */
public interface RootRelationContract {
    List<String> pks();                        // every identity, once, in base order — the identity space
    List<String> columns();                    // the column structure, in base order
    Object       cellFor(String pk, String column);   // the cell manager: the domain's cell for one identity

    /** The method names the JS relation object must expose. Nothing about values. */
    String[] METHOD_NAMES = { "pks", "columns", "cellFor" };
}
