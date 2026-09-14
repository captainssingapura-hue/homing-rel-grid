// =============================================================================
// RelGridHeadersModule — RFC 0050 · Episode 2's HEADER CELLS, grid side: a
// registry keyed by column of the header cell the relation answered with, and
// the ELEMENT that cell owns — asked of it once, through headerElement(), and
// placed by the grid into the header slot its column maps to. The twin of
// RelGridCells one band up: the grid mints nothing for a header cell, and the
// domain's element is handed to the grid's <th> exactly as a cell's element is
// handed to a <td>. Addressed by column; it never sees j.
//
// The same three rules:
//   · ensure() asks headerFor ONCE per PRESENTATION of the column and keeps
//     the instance, and asks headerElement() of it once.
//   · Nothing is passed into a header cell, and nothing is read out but its
//     element. A header cell is told nothing: it has no modes.
//   · Nothing is ever disposed — and nothing is kept past its presentation:
//     detach takes the element out of the slot AND FORGETS it; a column that
//     leaves the column view and returns is asked for again.
//
//   new RelGridHeaders()
// =============================================================================

class RelGridHeaders {

    constructor() {
        this._entries = new Map();   // column → { col, cell, el }
    }

    /** The entry for a column: on first ask, the header cell from the relation and its element from the cell; kept. */
    ensure(col, headerFor) {
        var entry = this._entries.get(col);
        if (entry) return entry;
        var cell = headerFor(col);
        if (!cell || typeof cell.headerElement !== "function")
            throw new Error("[RelGridHeaders] headerFor(" + col + ") must answer a header cell with headerElement()");
        var el = cell.headerElement();
        if (!el || typeof el !== "object" || typeof el.appendChild !== "function")
            throw new Error("[RelGridHeaders] headerElement() of " + col + " must answer an element");
        entry = { col: col, cell: cell, el: el };
        this._entries.set(col, entry);
        return entry;
    }

    /** The live entry, or null — no side effects. */
    get(col) { return this._entries.get(col) || null; }

    /** Place the header cell's element into a layout-owned header slot. */
    place(col, slotEl) {
        var entry = this.get(col);
        if (!entry || !slotEl) return false;
        if (entry.el.parentNode !== slotEl) slotEl.appendChild(entry.el);
        return true;
    }

    /** DETACH — out of the slot, and out of the registry; the cell stays the domain's. */
    detach(col) {
        var entry = this.get(col);
        if (!entry) return this;
        if (entry.el.parentNode) entry.el.parentNode.removeChild(entry.el);
        this._entries.delete(col);
        return this;
    }

    /** After a placement pass: whatever isPresented rejects comes out, alive, and is forgotten. */
    detachAbsent(isPresented) {
        var self = this, gone = [];
        this._entries.forEach(function (entry) { if (!isPresented(entry.col)) gone.push(entry.col); });
        for (var g = 0; g < gone.length; g++) this.detach(gone[g]);
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
