// =============================================================================
// RelGridSlotsModule — RFC 0050 · Episode 2's SLOT MATRIX: the cols, the header
// cells, the body and the <td> slots of ONE arrangement, and the pointer
// capture on them. Positional only — everything is addressed by (i, j) and
// nothing here sees an identity. It never touches slot content: a slot's
// children are the domain's, placed by the facade, and a rebuild leaves
// whatever sits in an old slot riding the released subtree until the facade
// re-places it.
//
// It is the CAPTURE SURFACE for slots: a click and a double-click are
// reported by position with their modifiers, and a PRESS-DRAG as raw pointer
// facts — a press, each slot the pointer reaches while held, and the release.
// Nothing here decides what a gesture MEANS; it does not even know there is
// a selection.
//
// Every element is minted through the DomOpsParty on a 'slots' sub-branch of
// the grid's own, made for a SHAPE and dissolved by the next shape: that is
// what releases the cols, the headers, the body and every slot, and nothing
// is ever removed by hand. An arrangement whose shape is UNCHANGED — the same
// headers, the same number of rows — keeps the matrix it has: the slots are
// positions, and the same positions are the same slots. A window of W rows
// over an endless relation is arranged on the same W × columns slots however
// far it scrolls; only the cells in them change. Nothing is minted, nothing
// is released, and the grid's branch holds a constant count.
//
//   new RelGridSlots({ branch, table, colgroup, headerRow, drag, sticky?, onCellClick?,
//                      onCellDblClick?, onCellDown?, onCellDragTo?, onDragEnd? })
//   sticky: the header cells stay at the top of whatever scrolls the table
//   render({ headers, rows })      the matrix for a shape: true when minted fresh, false when kept
//   slotAt(i, j) / rows() / cols() / colAt(j)
//   pressed() / release()          the press-drag's state, for the document-level release
//   destroy()
// =============================================================================

/** The two modifiers that change what a click means. Meta stands in for ctrl. */
function _hrgMods(e) {
    e = e || {};
    return { shift: !!e.shiftKey, ctrl: !!(e.ctrlKey || e.metaKey) };
}

class RelGridSlots {

    constructor(opts) {
        this._branch = opts.branch;             // the grid's own; the slots' branch is a sub-branch of it
        this._table = opts.table;
        this._colgroup = opts.colgroup;
        this._headerRow = opts.headerRow || null;
        this._drag = opts.drag || null;         // the header drag, wired per <th>
        this._sticky = opts.sticky === true;    // the header cells wear hrg_sticky
        this._onCellClick = opts.onCellClick || null;         // (i, j, mods)
        this._onCellDblClick = opts.onCellDblClick || null;   // (i, j)
        this._onCellDown = opts.onCellDown || null;           // (i, j, mods) — a press
        this._onCellDragTo = opts.onCellDragTo || null;       // (i, j) — reached while held
        this._onDragEnd = opts.onDragEnd || null;             // () — released
        this._slotsBranch = null;               // the current shape's
        this._shape = null;                     // { headers, rows } the matrix was minted for
        this._tbody = null;
        this._slots = [];                       // [i][j] → td
        this._cols = [];                        // [j] → col
        this._press = null;                     // the slot a button went down on
    }

    /**
     * The MODIFIERS travel with the position: shift and ctrl are what separate
     * a cursor move from an extension or an addition, and the slots decide
     * neither — they report both and let the facade read the gesture. A press
     * that never leaves its cell is a click, and is handled as one; once it
     * HAS left, coming back to the press's slot is a report too — that is how
     * a drag shrinks to 1x1 again.
     */
    _wire(td, i, j) {
        var self = this;
        td.addEventListener("click", function (e) {
            if (self._onCellClick) self._onCellClick(i, j, _hrgMods(e));
        });
        td.addEventListener("dblclick", function (e) {
            if (self._onCellDblClick) self._onCellDblClick(i, j, _hrgMods(e));
        });
        td.addEventListener("mousedown", function (e) {
            self._press = { i: i, j: j };
            if (self._onCellDown) self._onCellDown(i, j, _hrgMods(e));
        });
        td.addEventListener("mousemove", function () {
            if (!self._press) return;
            if (!self._press.moved) {
                if (self._press.i === i && self._press.j === j) return;   // never left its own slot
                self._press.moved = true;
            }
            if (self._onCellDragTo) self._onCellDragTo(i, j);
        });
    }

    /** The same headers, in the same order, and the same number of rows. */
    _sameShape(headers, rows) {
        var s = this._shape;
        if (!s || s.rows !== rows || s.headers.length !== headers.length) return false;
        for (var h = 0; h < headers.length; h++) if (s.headers[h] !== headers[h]) return false;
        return true;
    }

    /**
     * The matrix for a shape: { headers: string[], rows: n }. An unchanged
     * shape keeps the matrix it has and answers false — nothing minted,
     * nothing released. A new shape dissolves the last shape's branch first
     * and mints everything fresh on a new one; true.
     */
    render(shape) {
        var headers = (shape && shape.headers) || [];
        var rows = (shape && shape.rows) || 0;
        if (this._slotsBranch && this._sameShape(headers, rows)) return false;
        if (this._slotsBranch) this._slotsBranch.dissolve();
        var slots = this._slotsBranch = this._branch.createBranch("slots");
        slots.activate(this);
        this._shape = { headers: headers.slice(), rows: rows };
        this._cols = [];
        for (var h = 0; h < headers.length; h++) {
            var col = slots.createElement("col-" + h, "col");   // widths need cols regardless
            css.addClass(col, hrg_col);
            this._colgroup.appendChild(col);
            this._cols.push(col);
            if (!this._headerRow) continue;
            var th = slots.createElement("th-" + h, "th");
            css.addClass(th, hrg_th);
            if (this._sticky) css.addClass(th, hrg_sticky);
            th.textContent = headers[h];
            if (this._drag) this._drag.wire(th, h, slots);
            this._headerRow.appendChild(th);
        }
        this._tbody = slots.createElement("tbody", "tbody");
        this._slots = [];
        for (var i = 0; i < rows; i++) {
            var tr = slots.createElement("tr-" + i, "tr");
            var rowSlots = [];
            for (var j = 0; j < headers.length; j++) {
                var td = slots.createElement("td-" + i + "-" + j, "td");
                css.addClass(td, hrg_td);
                this._wire(td, i, j);
                tr.appendChild(td);
                rowSlots.push(td);
            }
            this._tbody.appendChild(tr);
            this._slots.push(rowSlots);
        }
        this._table.appendChild(this._tbody);
        return true;
    }

    /** The slot at a position, or null. */
    slotAt(i, j) {
        var row = this._slots[i];
        return (row && row[j]) ? row[j] : null;
    }

    colAt(j) { return this._cols[j] || null; }
    rows() { return this._slots.length; }
    cols() { return this._cols.length; }              // the presented columns — with rows or without, as a header alone has them

    /** A press is held — the document-level release ends it and reports. */
    pressed() { return !!this._press; }
    release() {
        if (!this._press) return;
        this._press = null;
        if (this._onDragEnd) this._onDragEnd();
    }

    destroy() {
        if (this._slotsBranch) { this._slotsBranch.dissolve(); this._slotsBranch = null; }
        this._shape = null;
        this._slots = [];
        this._cols = [];
        this._press = null;
    }
}
