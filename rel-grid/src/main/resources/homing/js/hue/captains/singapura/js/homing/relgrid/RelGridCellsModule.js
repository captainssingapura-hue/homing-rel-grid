// =============================================================================
// RelGridCellsModule — RFC 0050 · Episode 2's cells branch, grid side. A registry
// keyed by identity: the host element the grid minted on the branch it was
// handed — a sub-branch of the grid's own, dissolved with the grid — and the
// cell the domain's manager answered with. Addressed purely by
// (pk, column); it never sees (i, j) and never touches the layout — the facade
// hands it layout-owned slots to place into.
//
// Three rules, each one a law of the episode:
//   · ensure() asks cellFor ONCE per identity and keeps the instance. The
//     domain created it; the grid only holds a reference so it can place it.
//   · Nothing is passed into a cell but its host, and nothing is read out.
//   · Nothing is ever disposed. Detach leaves element, instance and state
//     alive; destroy() detaches everything and forgets — the cells are still
//     the domain's, and the next grid over the same relation may find them.
//
//   new RelGridCells({ branch })
// =============================================================================

class RelGridCells {

    constructor(opts) {
        opts = opts || {};
        if (!opts.branch) throw new Error("[RelGridCells] opts.branch is required");
        this._branch = opts.branch;
        this._entries = new Map();   // "pk col" → { pk, col, cell, el }
        this._seq = 0;
    }

    _key(pk, col) { return pk + " " + col; }

    /**
     * The entry for (pk, col): minted on first ask — a host element through
     * the branch, the cell from the manager, one render(host) — and returned
     * untouched on every ask after. Idempotent by identity.
     */
    ensure(pk, col, cellFor) {
        var k = this._key(pk, col);
        var entry = this._entries.get(k);
        if (entry) return entry;
        var el = this._branch.createElement("cell" + (++this._seq), "div");
        var cell = cellFor(pk, col);
        if (!cell || typeof cell.render !== "function")
            throw new Error("[RelGridCells] cellFor(" + pk + ", " + col + ") must answer a cell with render(host)");
        entry = { pk: pk, col: col, cell: cell, el: el };
        this._entries.set(k, entry);
        try { cell.render(el); }
        catch (e) { console.error("[RelGridCells] cell.render threw:", e); }
        return entry;
    }

    /** The live entry, or null — no side effects. */
    get(pk, col) { return this._entries.get(this._key(pk, col)) || null; }

    /** Place the cell's element into a layout-owned slot — the merge point. */
    place(pk, col, slotEl) {
        var entry = this.get(pk, col);
        if (!entry || !slotEl) return false;
        if (entry.el.parentNode !== slotEl) slotEl.appendChild(entry.el);
        return true;
    }

    /** DETACH — leave the visible tree; element, instance and state stay alive. */
    detach(pk, col) {
        var entry = this.get(pk, col);
        if (entry && entry.el.parentNode) entry.el.parentNode.removeChild(entry.el);
        return this;
    }

    /** After a placement pass: whatever isVisible rejects comes out of the tree, alive. */
    detachInvisible(isVisible) {
        var self = this;
        this._entries.forEach(function (entry) {
            if (!isVisible(entry.pk, entry.col)) self.detach(entry.pk, entry.col);
        });
        return this;
    }

    size() { return this._entries.size; }

    /** Detach everything and forget the registry. Disposes nothing. */
    destroy() {
        this._entries.forEach(function (entry) {
            if (entry.el.parentNode) entry.el.parentNode.removeChild(entry.el);
        });
        this._entries.clear();
    }
}
