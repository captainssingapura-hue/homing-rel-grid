// =============================================================================
// RelGridViewMapsModule — RFC 0050 · Episode 2's identity/position seam. PURE logic:
// no DOM, no knowledge of cells or slots, one optional callback.
//
// The ONLY place (i, j) meets (pk, column). Two maps:
//
//   i → pk        the row view: the rows PRESENTED, in order — the View
//   j → column    the column view: an ordered subset of the base columns
//
// THE ROW AXIS HAS NO BASE. What the relation answers to view() is what is
// presented, and there is no other list: the grid holds W keys for a window
// of W rows over a relation of any size, and asks the relation for no
// enumeration of anything. Membership is not decided here — the relation is
// the authority on its own identity space, and refuses an identity it does
// not own where it is asked for the cell (cellFor). A remap this cannot
// check is a remap that never happened here: when the arrangement refuses a
// View — the facade rethrows a cellFor refusal before a slot moves — the
// rows go back as they were, and the caller hears the refusal.
//
// The column axis is STRUCTURE and stays listed: columns() is fixed for the
// grid's lifetime, widths are held by column identity, and a column view is
// a subset of it. Nothing in this class reads a value or decides an order.
//
//   new RelGridViewMaps({ rowView, columns, columnView?, onViewChanged? })
//
// Every check is O(W): uniqueness within the View, and — for columns — the
// subset of a list that is short by nature.
// =============================================================================

class RelGridViewMaps {

    constructor(opts) {
        opts = opts || {};
        this._baseColumns = this._checkUnique(opts.columns || [], "columns");
        // onViewChanged(kind) — 'rows' | 'columns'. The facade re-arranges.
        this._onViewChanged = opts.onViewChanged || null;
        this._rowView = this._checkUnique(opts.rowView || [], "rowView");
        this._colView = opts.columnView ? this._checkSubset(opts.columnView, this._baseColumns, "columnView")
                                        : this._baseColumns.slice();
        this._rowIndex = null;
        this._colIndex = null;
    }

    // ── the column structure (identity — what exists) ─────────────────────

    baseColumns() { return this._baseColumns.slice(); }

    // ── the view (position — what shows, where) ────────────────────────────

    rows() { return this._rowView.length; }
    cols() { return this._colView.length; }

    rowView()    { return this._rowView.slice(); }
    columnView() { return this._colView.slice(); }

    pkAt(i)     { return (i >= 0 && i < this._rowView.length) ? this._rowView[i] : null; }
    columnAt(j) { return (j >= 0 && j < this._colView.length) ? this._colView[j] : null; }

    rowOf(pk)   { return this._index(this._rowView, "_rowIndex", pk); }
    colOf(name) { return this._index(this._colView, "_colIndex", name); }

    /** (i, j) → { pk, column }, or null outside the view. */
    resolve(i, j) {
        var pk = this.pkAt(i), col = this.columnAt(j);
        return (pk != null && col != null) ? { pk: pk, column: col } : null;
    }

    /** (pk, column) → { i, j }, or null when either is not presented. */
    locate(pk, column) {
        var i = this.rowOf(pk), j = this.colOf(column);
        return (i >= 0 && j >= 0) ? { i: i, j: j } : null;
    }

    // ── remaps (pure reassignments) ────────────────────────────────────────

    /** Present these rows, in this order. Unique, or refused; refused by the arrangement, and undone. */
    setRowView(pks) {
        return this._swap("rows", "_rowView", "_rowIndex", this._checkUnique(pks, "row view"));
    }

    setColumnView(names) {
        return this._swap("columns", "_colView", "_colIndex", this._checkSubset(names, this._baseColumns, "column view"));
    }

    resetColumnView() { return this.setColumnView(this._baseColumns.slice()); }

    // ── internals ──────────────────────────────────────────────────────────

    /**
     * The one way a view changes: set, then tell the facade. An arrangement
     * that throws has refused the View — half a View is not a View — so the
     * view goes back to what it was and the refusal travels on to the caller.
     */
    _swap(kind, field, indexField, next) {
        var prev = this[field];
        this[field] = next;
        this[indexField] = null;
        if (!this._onViewChanged) return this;
        try { this._onViewChanged(kind); }
        catch (e) {
            this[field] = prev;
            this[indexField] = null;
            throw e;
        }
        return this;
    }

    _index(view, cacheField, key) {
        if (this[cacheField] == null) {
            var m = new Map();
            for (var k = 0; k < view.length; k++) m.set(view[k], k);
            this[cacheField] = m;
        }
        var i = this[cacheField].get(key);
        return (i === undefined) ? -1 : i;
    }

    _checkUnique(arr, what) {
        var seen = new Map();
        for (var k = 0; k < arr.length; k++) {
            if (seen.has(arr[k])) throw new Error("[RelGridViewMaps] duplicate in " + what + ": " + arr[k]);
            seen.set(arr[k], true);
        }
        return arr.slice();
    }

    _checkSubset(arr, base, what) {
        var out = this._checkUnique(arr, what);
        var baseSet = new Map();
        for (var k = 0; k < base.length; k++) baseSet.set(base[k], true);
        for (var m = 0; m < out.length; m++) {
            if (!baseSet.has(out[m])) throw new Error("[RelGridViewMaps] " + what + " references unknown key: " + out[m]);
        }
        return out;
    }
}
