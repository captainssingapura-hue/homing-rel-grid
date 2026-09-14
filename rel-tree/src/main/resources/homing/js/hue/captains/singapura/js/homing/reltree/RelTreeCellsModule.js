// =============================================================================
// RelTreeCellsModule — RFC 0050 · Episode 3-ext1's cells branch, tree side: a
// registry keyed by node — the cell the domain's manager answered with, and
// the ELEMENT that cell owns, asked of it once through cellElement() and
// placed by the tree into a row the rows module minted. The tree mints
// nothing for a cell: the two branches are independent, and this registry
// is where the domain's elements are handed to the tree's rows. Addressed
// by key; it never sees a position and never touches the layout.
//
// The grid's registry with one axis (ext8's laws 244–245, 249, read for a
// tree), and the same three rules:
//   · ensure() asks cellFor ONCE per PRESENTATION and keeps the instance, and
//     asks cellElement() of it once.
//   · Nothing is passed into a cell, and nothing is read out but its element.
//   · Nothing is ever disposed — and nothing is kept past its presentation.
//     Detach takes the element out of the tree AND FORGETS the entry; a key
//     that leaves the View and returns is asked for again, and a relation
//     that kept the cell answers the same one. The registry is exactly the
//     presented cells, so a domain that frees a node it no longer presents
//     frees it, and nothing here can hand a dead element back into a row.
//
//   new RelTreeCells()
//   ensure(key, cellFor) / get(key) / place(key, rowEl) / detach(key) / detachInvisible(isVisible)
//   size() / destroy()
// =============================================================================

class RelTreeCells {

    constructor() {
        this._entries = new Map();   // key → { key, cell, el }
    }

    /** On first ask, the cell from the manager and its element from the cell; untouched on every ask after. */
    ensure(key, cellFor) {
        var entry = this._entries.get(key);
        if (entry) return entry;
        var cell = cellFor(key);
        if (!cell || typeof cell.cellElement !== "function")
            throw new Error("[RelTreeCells] cellFor(" + key + ") must answer a cell with cellElement()");
        var el = cell.cellElement();
        if (!el || typeof el !== "object" || typeof el.appendChild !== "function")
            throw new Error("[RelTreeCells] cellElement() of " + key + " must answer an element");
        entry = { key: key, cell: cell, el: el };
        this._entries.set(key, entry);
        return entry;
    }

    /** The live entry, or null — no side effects. */
    get(key) { return this._entries.get(key) || null; }

    /** Place the cell's element into a row the rows module owns, after the caret — the merge point. */
    place(key, rowEl) {
        var entry = this.get(key);
        if (!entry || !rowEl) return false;
        if (entry.el.parentNode !== rowEl) rowEl.appendChild(entry.el);
        return true;
    }

    /** DETACH — out of the visible tree, and out of the registry; element, instance and state stay the domain's, alive. */
    detach(key) {
        var entry = this.get(key);
        if (!entry) return this;
        if (entry.el.parentNode) entry.el.parentNode.removeChild(entry.el);
        this._entries.delete(key);
        return this;
    }

    /** After a placement pass: whatever isVisible rejects comes out of the tree and out of the registry, alive. */
    detachInvisible(isVisible) {
        var self = this;
        this._entries.forEach(function (entry) {
            if (!isVisible(entry.key)) self.detach(entry.key);
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
