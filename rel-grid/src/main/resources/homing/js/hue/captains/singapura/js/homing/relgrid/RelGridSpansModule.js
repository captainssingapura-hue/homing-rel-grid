// =============================================================================
// RelGridSpansModule — RFC 0050 · Episode 2's MERGED CELLS (map 25), the
// feature kept apart: a matrix that stays whole, and a cell laid over part
// of it. Every position keeps its slot and its own cell. A cell that answers
// colSpan() > 1 is a LEADING cell: the layout mints a host over the n slots
// it reaches across and the cell is placed THERE; the host mirrors the state
// of the slots beneath. A vertical move passes through; a horizontal move
// jumps OUT of the group; Enter anywhere in a group offers control to the
// LEADING cell. Spans are read on every arrangement. Off, every question
// here has the plain answer: no group anywhere, one slot per step, the
// cursor's own cell.
//
//   new RelGridSpans({ on, layout, cells, maps })
//   mark(i, j, id)        after a cell is placed: a leading cell's reach, read afresh, and the host it moves into
//   groupAt(i, j)         the merged cell a position is in, or null: { i, j, n }
//   stepJ(i, j, dj)       one horizontal step: out of a merged cell entirely, one slot otherwise
//   deepAt(id, at)        where deep goes from a cursor: its own cell, or the group's leading cell
//   settle()              every cell is in: measure the hosts
// =============================================================================

class RelGridSpans {

    constructor(opts) {
        this._on = opts.on === true;              // honour colSpan() at all
        this._layout = opts.layout;
        this._cells = opts.cells;
        this._maps = opts.maps;
    }

    on() { return this._on; }

    /**
     * A leading cell's reach, read afresh on every pass: only a finite span
     * above one counts, clamped to the row's end, and a cell that throws or
     * answers nonsense is a plain cell. The layout marks the slots; the cell
     * draws itself.
     */
    mark(i, j, id) {
        if (!this._on) return;
        var entry = this._cells.get(id.pk, id.column), cell = entry && entry.cell;
        if (!cell || typeof cell.colSpan !== "function") return;
        if (this._layout.groupAt(i, j)) return;               // inside an earlier group: a plain cell
        var n;
        try { n = Number(cell.colSpan()); } catch (e) { console.error("[RelGrid] cell.colSpan threw:", e); return; }
        if (!isFinite(n) || n <= 1) return;
        n = Math.min(Math.floor(n), this._maps.cols() - j);
        if (n <= 1) return;
        var host = this._layout.openGroup(i, j, n);
        if (host) this._cells.place(id.pk, id.column, host);  // the leading cell lives in the host
    }

    /** Every cell is in: size and place the hosts over their slots as they are now. */
    settle() { if (this._on) this._layout.placeGroups(); }

    /** The merged cell a position is in, or null: { i, j, n }. */
    groupAt(i, j) { return this._on ? this._layout.groupAt(i, j) : null; }

    /** One horizontal step from a position: out of a merged cell entirely, and one slot otherwise. */
    stepJ(i, j, dj) {
        var grp = this.groupAt(i, j);
        if (!grp) return j + dj;
        return dj > 0 ? grp.j + grp.n : grp.j - 1;
    }

    /**
     * Where deep goes from a cursor: its own identity, or — inside a merged
     * cell — the LEADING cell's, since that is the cell with anything in it,
     * and the editor opens over the whole group. Null when there is no cursor.
     */
    deepAt(id, at) {
        if (!id || !at) return null;
        var grp = this.groupAt(at.i, at.j);
        if (!grp) return { id: id, i: at.i, j: at.j };
        var lead = this._maps.resolve(grp.i, grp.j);
        return lead ? { id: lead, i: grp.i, j: grp.j } : null;
    }
}
