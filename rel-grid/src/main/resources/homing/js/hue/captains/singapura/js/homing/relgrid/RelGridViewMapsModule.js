// =============================================================================
// RelGridViewMapsModule — RFC 0050 · Episode 2's identity/position seam. PURE logic:
// no DOM, no knowledge of cells or slots, one optional callback.
//
// The ONLY place (i, j) meets (pk, column). Two maps:
//
//   i → pk        the row view: an ordered subset of the base identities
//   j → column    the column view: an ordered subset of the base columns
//
// The base axes are the ROOT RELATION's and fixed for the grid's lifetime —
// there is no addPk and no removePk here on purpose. A row that arrives is a
// new relation and a new grid; a row that goes is absorbed by a later View.
// Remaps (setRowView / setColumnView) are pure reassignments a later round
// will drive from the relation's answers; nothing in this class reads a value
// or decides an order.
//
//   new RelGridViewMaps({ pks, columns, rowView?, columnView?, onViewChanged? })
//
// An INITIAL view may be given, so a grid over a relation that declares more
// identities than it presents — an article with a capacity of rows — arranges
// only what is presented from its first pass, rather than everything and then
// a remap. The same checks as a remap; no callback, since nothing is arranged yet.
// =============================================================================

class RelGridViewMaps {

    constructor(opts) {
        opts = opts || {};
        this._basePks     = this._checkUnique(opts.pks || [], "pks");
        this._baseColumns = this._checkUnique(opts.columns || [], "columns");
        // onViewChanged(kind) — 'rows' | 'columns'. The facade re-arranges.
        this._onViewChanged = opts.onViewChanged || null;
        this._rowView = opts.rowView    ? this._checkSubset(opts.rowView,    this._basePks,     "rowView")
                                        : this._basePks.slice();
        this._colView = opts.columnView ? this._checkSubset(opts.columnView, this._baseColumns, "columnView")
                                        : this._baseColumns.slice();
        this._rowIndex = null;
        this._colIndex = null;
    }

    // ── the base axes (identity — what exists) ─────────────────────────────

    basePks()     { return this._basePks.slice(); }
    baseColumns() { return this._baseColumns.slice(); }

    // ── the view (position — what shows, where) ────────────────────────────

    rows() { return this._rowView.length; }
    cols() { return this._colView.length; }

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

    setRowView(pks) {
        this._rowView = this._checkSubset(pks, this._basePks, "row view");
        this._rowIndex = null;
        this._fire("rows");
        return this;
    }

    resetRowView() { return this.setRowView(this._basePks.slice()); }

    setColumnView(names) {
        this._colView = this._checkSubset(names, this._baseColumns, "column view");
        this._colIndex = null;
        this._fire("columns");
        return this;
    }

    resetColumnView() { return this.setColumnView(this._baseColumns.slice()); }

    // ── internals ──────────────────────────────────────────────────────────

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

    _fire(kind) {
        if (!this._onViewChanged) return;
        try { this._onViewChanged(kind); }
        catch (e) { console.error("[RelGridViewMaps] onViewChanged threw:", e); }
    }
}
