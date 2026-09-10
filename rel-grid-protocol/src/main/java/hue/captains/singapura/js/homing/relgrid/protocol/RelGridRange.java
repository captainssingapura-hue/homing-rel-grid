package hue.captains.singapura.js.homing.relgrid.protocol;

/**
 * A rectangle of POSITIONS in the presented space, owning both its corners —
 * RFC 0050 · Episode 2, map 5 law 38. The smallest is 1×1, where the two
 * corners are the same cell.
 *
 * <p>Positions, not identities: a range is defined positionally and resolved
 * to identities only at the moment of use, which is map 5's decision and not
 * this record's to revisit.</p>
 *
 * <p>Its JS class is generated from this declaration. Nothing about the shape
 * is written twice.</p>
 */
public record RelGridRange(int i0, int j0, int i1, int j1) {

    public RelGridRange {
        if (i0 < 0 || j0 < 0 || i1 < i0 || j1 < j0)
            throw new IllegalArgumentException(
                    "RelGridRange must be non-negative and normalised: " + i0 + "," + j0 + ".." + i1 + "," + j1);
    }

    /** How many cells it covers. Derived, never stored. */
    public int cells() { return (i1 - i0 + 1) * (j1 - j0 + 1); }
}
