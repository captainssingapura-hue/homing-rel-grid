// =============================================================================
// RelGridCellsModule — RFC 0050 · Episode 2's cells branch, grid side. A registry
// keyed by identity: the cell the domain's manager answered with, and the
// ELEMENT that cell owns — asked of it once, through cellElement(), and
// placed by the grid into a slot the layout minted. The grid mints nothing
// for a cell: the two branches are independent, and this registry is where
// the domain's elements are handed to the grid's slots. Addressed purely by
// (pk, column); it never sees (i, j) and never touches the layout — the
// facade hands it layout-owned slots to place into.
//
// Three rules, each one a law of the episode:
//   · ensure() asks cellFor ONCE per PRESENTATION and keeps the instance, and
//     asks cellElement() of it once. The domain created both; the grid only
//     holds a reference so it can place the element.
//   · Nothing is passed into a cell, and nothing is read out but its element.
//   · Nothing is ever disposed — and nothing is kept past its presentation.
//     Detach takes the element out of the tree AND FORGETS the entry: the
//     cell is still the domain's, alive with its state, and the domain's
//     cellFor is its keeper — an identity that leaves the View and returns
//     is asked for again, and a relation that kept the cell answers the same
//     one. So this registry is exactly the presented cells, never more: a
//     grid over a window of W rows holds W × columns entries whatever it has
//     scrolled past, and a domain that frees a row it no longer presents
//     frees it — nothing here can hand a dead element back into a slot.
//     destroy() detaches everything and forgets.
//
//   new RelGridCells()
// =============================================================================

class RelGridCells {

    constructor() {
        this._entries = new Map();   // "pk col" → { pk, col, cell, el }
    }

    _key(pk, col) { return pk + " " + col; }

    /**
     * The entry for (pk, col): on first ask, the cell from the manager and
     * its element from the cell — one cellElement(), kept — and returned
     * untouched on every ask after. Idempotent by identity.
     */
    ensure(pk, col, cellFor) {
        var k = this._key(pk, col);
        var entry = this._entries.get(k);
        if (entry) return entry;
        var cell = cellFor(pk, col);
        if (!cell || typeof cell.cellElement !== "function")
            throw new Error("[RelGridCells] cellFor(" + pk + ", " + col + ") must answer a cell with cellElement()");
        var el = cell.cellElement();
        if (!el || typeof el !== "object" || typeof el.appendChild !== "function")
            throw new Error("[RelGridCells] cellElement() of (" + pk + ", " + col + ") must answer an element");
        entry = { pk: pk, col: col, cell: cell, el: el };
        this._entries.set(k, entry);
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

    /** DETACH — out of the visible tree, and out of the registry; element, instance and state stay the domain's, alive. */
    detach(pk, col) {
        var entry = this.get(pk, col);
        if (!entry) return this;
        if (entry.el.parentNode) entry.el.parentNode.removeChild(entry.el);
        this._entries.delete(this._key(pk, col));
        return this;
    }

    /** After a placement pass: whatever isVisible rejects comes out of the tree and out of the registry, alive. */
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
