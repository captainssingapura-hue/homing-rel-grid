// =============================================================================
// RelGridLayoutModule — RFC 0050 · Episode 2's LAYOUT branch: the structural chrome.
// Owns the <table>, the <colgroup>, the header band and the <td> slots.
// Positional only — this class addresses everything by (i, j) and never sees
// an identity; headers arrive as display labels and widths arrive per visible
// position. It never touches slot content: a slot's children belong to the
// cells branch, and render() rebuilds the slot matrix while the facade
// re-places content afterwards (one appendChild per cell).
//
// It is also the CAPTURE SURFACE and the KEYBOARD HOST: the table is focusable,
// slots report click and double-click by position, headers carry the resize
// handle, and the cursor is painted on the slot at the facade's direction. The
// layout decides nothing — it reports gestures and paints answers.
//
// Raw DOM inside the primitive; an injected stylesheet with theme tokens only,
// under the hrg- prefix so this grid and the live one can share a page.
//
//   new RelGridLayout({ container, label?, showHeader?, onCellClick?, onCellDblClick?, onColResize? })
// =============================================================================

var _HRG_STYLE_ID = "homing-rel-grid-style";
var _HRG_STYLE_CSS = [
    ".hrg-table{border-collapse:collapse;width:100%;",
    "  background:var(--color-surface);color:var(--color-text-primary);",
    "  font:13px sans-serif;user-select:none;-webkit-user-select:none;}",
    ".hrg-table:focus{outline:none;}",
    ".hrg-table input{user-select:text;-webkit-user-select:text;}",
    ".hrg-th{position:relative;text-align:left;padding:6px 10px;",
    "  background:var(--color-surface-raised);color:var(--color-text-muted);",
    "  white-space:nowrap;",
    "  box-shadow:inset -1px 0 0 var(--color-border),",
    "             inset 0 -2px 0 var(--color-border);}",
    // The resize HANDLE: a real element on the header's right edge, so the
    // pointer shows col-resize on hover and the drag has a reliable target.
    ".hrg-resize-handle{position:absolute;top:0;right:0;width:8px;height:100%;cursor:col-resize;}",
    // Widths ride a custom property per <col>; hrg-fixed engages once any
    // explicit width exists, so unsized columns keep sharing the remainder.
    ".hrg-table.hrg-fixed{table-layout:fixed;}",
    ".hrg-table col{width:var(--hrg-col-w,auto);}",
    ".hrg-resize-guide{position:fixed;top:var(--hrg-guide-top);height:var(--hrg-guide-h);",
    "  left:var(--hrg-guide-x);width:2px;background:var(--color-accent);z-index:99;",
    "  pointer-events:none;}",
    ".hrg-td{padding:0;border-bottom:1px solid var(--color-border);",
    "  border-right:1px solid color-mix(in srgb, var(--color-border) 50%, transparent);",
    "  vertical-align:middle;overflow:hidden;}",
    // The selection: a wash on every slot the resolved list covers. A slot may
    // wear this and the cursor at once — with an empty list the selection IS
    // the cursor's 1x1, so the cursor's slot is always one of them.
    ".hrg-td.hrg-sel{background:color-mix(in srgb, var(--color-accent) 12%, transparent);}",
    // The cursor: painted on the slot, never on the cell. Solid while shallow;
    // dashed while the cell is deep, so the handover is visible.
    ".hrg-td.hrg-cursor{outline:2px solid var(--color-accent);outline-offset:-2px;}",
    ".hrg-table.hrg-deep .hrg-td.hrg-cursor{outline-style:dashed;}"
].join("\n");

/** The two modifiers that change what a click means. Meta stands in for ctrl. */
function _hrgMods(e) {
    e = e || {};
    return { shift: !!e.shiftKey, ctrl: !!(e.ctrlKey || e.metaKey) };
}

function _hrgEnsureStyles() {
    if (document.getElementById(_HRG_STYLE_ID)) return;
    var s = document.createElement("style");
    s.id = _HRG_STYLE_ID;
    s.textContent = _HRG_STYLE_CSS;
    document.head.appendChild(s);
}

function _hrgAddClass(el, c) {
    var parts = el.className ? el.className.split(" ") : [];
    if (parts.indexOf(c) < 0) el.className = parts.concat(c).join(" ");
}

function _hrgRemoveClass(el, c) {
    if (!el.className) return;
    var parts = el.className.split(" "), kept = [];
    for (var k = 0; k < parts.length; k++) if (parts[k] !== c) kept.push(parts[k]);
    el.className = kept.join(" ");
}

function _hrgHasClass(el, c) {
    if (!el.className) return false;
    var parts = el.className.split(" ");
    for (var k = 0; k < parts.length; k++) if (parts[k] === c) return true;
    return false;
}

class RelGridLayout {

    constructor(opts) {
        opts = opts || {};
        if (!opts.container) throw new Error("[RelGridLayout] opts.container is required");
        _hrgEnsureStyles();
        this._container = opts.container;
        this._onCellClick = opts.onCellClick || null;         // (i, j)
        this._onCellDblClick = opts.onCellDblClick || null;   // (i, j)
        this._table = document.createElement("table");
        this._table.className = "hrg-table";
        this._table.setAttribute("tabindex", "0");            // the keyboard host
        if (opts.label) this._table.setAttribute("aria-label", opts.label);
        // The resize gesture lives in RelGridHeaderDrag; wired per <th> at render.
        this._drag = opts.onColResize
                   ? new RelGridHeaderDrag({ table: this._table, onColResize: opts.onColResize })
                   : null;
        this._colgroup = document.createElement("colgroup");
        this._table.appendChild(this._colgroup);
        this._showHead = opts.showHeader !== false;
        if (this._showHead) {
            this._thead = document.createElement("thead");
            this._headerRow = document.createElement("tr");
            this._thead.appendChild(this._headerRow);
            this._table.appendChild(this._thead);
        } else {
            this._thead = null; this._headerRow = null;
        }
        this._tbody = document.createElement("tbody");
        this._table.appendChild(this._tbody);
        this._container.appendChild(this._table);
        this._slots = [];        // [i][j] → td
        this._cursorTd = null;   // the slot currently painted as the cursor
        this._selTds = [];       // the slots currently painted as selected
    }

    /**
     * (Re)build the structure for a shape: { headers: string[], rows: n }.
     * Slots are minted fresh; slot CONTENT IS NOT TOUCHED — whatever sits in an
     * old slot rides the discarded subtree until the facade re-places it.
     */
    render(shape) {
        var headers = (shape && shape.headers) || [];
        var rows = (shape && shape.rows) || 0;

        while (this._colgroup.firstChild) this._colgroup.removeChild(this._colgroup.firstChild);
        if (this._headerRow)
            while (this._headerRow.firstChild) this._headerRow.removeChild(this._headerRow.firstChild);
        for (var h = 0; h < headers.length; h++) {
            this._colgroup.appendChild(document.createElement("col"));   // widths need cols regardless
            if (!this._headerRow) continue;
            var th = document.createElement("th");
            th.className = "hrg-th";
            th.textContent = headers[h];
            if (this._drag) this._drag.wire(th, h);
            this._headerRow.appendChild(th);
        }

        var oldBody = this._tbody;
        this._tbody = document.createElement("tbody");
        this._slots = [];
        this._cursorTd = null;                                // the old tds go with the old body
        this._selTds = [];
        var self = this;
        var wire = function (td, i, j) {
            // The MODIFIERS travel with the position: shift and ctrl are what
            // separate a cursor move from an extension or an addition, and the
            // layout decides neither — it reports both and lets the facade read
            // the gesture. Meta stands in for ctrl, so the Mac chord works.
            td.addEventListener("click", function (e) {
                if (self._onCellClick) self._onCellClick(i, j, _hrgMods(e));
            });
            td.addEventListener("dblclick", function (e) {
                if (self._onCellDblClick) self._onCellDblClick(i, j, _hrgMods(e));
            });
        };
        for (var i = 0; i < rows; i++) {
            var tr = document.createElement("tr");
            var rowSlots = [];
            for (var j = 0; j < headers.length; j++) {
                var td = document.createElement("td");
                td.className = "hrg-td";
                wire(td, i, j);
                tr.appendChild(td);
                rowSlots.push(td);
            }
            this._tbody.appendChild(tr);
            this._slots.push(rowSlots);
        }
        this._table.replaceChild(this._tbody, oldBody);
        return this;
    }

    /**
     * Apply widths in their POSITIONAL form — px | null per visible column —
     * in place: no slot is rebuilt and no cell moves. hrg-fixed engages once
     * any explicit width exists.
     */
    setColWidths(widths) {
        var any = false, cols = this._colgroup.children;
        for (var j = 0; j < cols.length; j++) {
            var w = widths ? widths[j] : null, st = cols[j].style;
            if (w != null) { any = true; if (st && st.setProperty) st.setProperty("--hrg-col-w", w + "px"); }
            else if (st && st.removeProperty) st.removeProperty("--hrg-col-w");
        }
        if (any) _hrgAddClass(this._table, "hrg-fixed");
        else _hrgRemoveClass(this._table, "hrg-fixed");
        return this;
    }

    /** Paint the cursor on one slot ({ i, j }) or on none (null). A diff, not a sweep. */
    paintCursor(ij) {
        var td = ij ? this.slotAt(ij.i, ij.j) : null;
        if (this._cursorTd === td) return this;
        if (this._cursorTd) _hrgRemoveClass(this._cursorTd, "hrg-cursor");
        if (td) _hrgAddClass(td, "hrg-cursor");
        this._cursorTd = td;
        return this;
    }

    /**
     * Paint the selection: a list of rectangles in the presented space,
     * {@code { i0, j0, i1, j1 }} inclusive of both corners. The layout is told
     * WHICH SLOTS, never why — overlap between rectangles is nothing it has to
     * resolve, since a slot either wears the predicate or does not.
     */
    paintSelection(rects) {
        for (var k = 0; k < this._selTds.length; k++) _hrgRemoveClass(this._selTds[k], "hrg-sel");
        this._selTds = [];
        var list = rects || [];
        for (var r = 0; r < list.length; r++) {
            var box = list[r];
            for (var i = box.i0; i <= box.i1; i++) {
                for (var j = box.j0; j <= box.j1; j++) {
                    var td = this.slotAt(i, j);
                    if (!td || _hrgHasClass(td, "hrg-sel")) continue;    // already painted by an overlap
                    _hrgAddClass(td, "hrg-sel");
                    this._selTds.push(td);
                }
            }
        }
        return this;
    }

    /** The table wears the deep state, so CSS and tests can see the handover. */
    setDeep(on) {
        if (on) _hrgAddClass(this._table, "hrg-deep"); else _hrgRemoveClass(this._table, "hrg-deep");
        return this;
    }

    focus() { if (this._table.focus) this._table.focus(); return this; }

    el() { return this._table; }

    /** The slot at a position, or null. */
    slotAt(i, j) {
        var row = this._slots[i];
        return (row && row[j]) ? row[j] : null;
    }

    rows() { return this._slots.length; }
    cols() { return this._slots.length ? this._slots[0].length : 0; }

    destroy() {
        if (this._table.parentNode) this._table.parentNode.removeChild(this._table);
        this._slots = [];
        this._cursorTd = null;
    }
}
