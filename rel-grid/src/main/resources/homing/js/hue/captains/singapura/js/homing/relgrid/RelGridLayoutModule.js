// =============================================================================
// RelGridLayoutModule — RFC 0050 · Episode 2's LAYOUT branch: the structural chrome.
// Owns the <table>, the <colgroup>, the header band and the wrapper they sit
// in; the slot matrix of one arrangement is RelGridSlots, and what is laid
// OVER the slots — the editor's anchor, the mask and its panel, a merged
// cell's host — is RelGridOverlays. Positional only — everything is addressed
// by (i, j) and nothing here sees an identity; headers arrive as display
// labels and widths arrive per visible position. It never touches slot
// content: a slot's children are the domain's, placed by the facade.
//
// It is the KEYBOARD HOST: the table is focusable, and the cursor and the
// selection are painted on the slots at the facade's direction — and
// mirrored onto a merged cell's host, so the whole group reads as one cell
// while the tracker keeps the exact square. The layout decides nothing — it
// reports gestures and paints answers.
//
// EVERY ELEMENT IS MINTED THROUGH THE DomOpsParty, on the branch the grid was
// handed — its own, and the host's to dissolve. What lives as long as the
// grid (the wrapper, the table, its colgroup and header band) is minted on
// that branch directly; what comes and goes is minted on a sub-branch made
// for it and dissolved with it — the slots of one arrangement, an overlay,
// the mask, the merged cells' hosts, a drag's guide — so a rebuild releases
// exactly what it replaces and nothing is ever removed by hand. The looks
// are TYPED — RelGridStyles — with theme tokens only, under the hrg- prefix
// so this grid and the live one can share a page.
//
//   new RelGridLayout({ container, branch, label?, showHeader?, overflow?, onCellClick?,
//                       onCellDblClick?, onCellDown?, onCellDragTo?, onDragEnd?,
//                       onColResize?, resizeGuide? })   resizeGuide: an element, or a list of them
//   render({ headers, rows }) / setColWidths(widths) / slotAt(i, j) / rows() / cols()
//   paintCursor(ij) / paintSelection(rects) / setDeep(on) / setMasked(on)
//   focus() / revealSlot(i, j) / hasKeyboard() / el() / rowHeight()
//   openOverlay(i, j) / closeOverlay() / overlay()
//   openMask() / openPanel(el) / closePanel() / closeMask() / mask() / panel()
//   openGroup(i, j, n) / groupAt(i, j) / placeGroups() / closeGroups() / groups()
// =============================================================================

/** wrap | clip | ellipsis — ellipsis unless the host says otherwise. */
function _hrgOverflowClass(choice) {
    if (choice === "wrap") return hrg_ov_wrap;
    if (choice === "clip") return hrg_ov_clip;
    return hrg_ov_ellipsis;
}

class RelGridLayout {

    constructor(opts) {
        opts = opts || {};
        if (!opts.container) throw new Error("[RelGridLayout] opts.container is required");
        if (!opts.branch) throw new Error("[RelGridLayout] opts.branch is required");
        var self = this;
        this._container = opts.container;
        this._branch = opts.branch;                           // the grid's own; the host dissolves it
        this._table = this._branch.createElement("table", "table");
        css.addClass(this._table, hrg_table, _hrgOverflowClass(opts.overflow));
        this._table.setAttribute("tabindex", "0");            // the keyboard host
        if (opts.label) this._table.setAttribute("aria-label", opts.label);
        // The resize gesture lives in RelGridHeaderDrag; wired per <th> at render.
        // It starts a drag from the width held, and draws its guide down the
        // extent the host names — a host stacking tables names their stack.
        this._colW = null;                                    // the last positional widths applied
        this._drag = opts.onColResize
                   ? new RelGridHeaderDrag({ branch: this._branch, table: this._table, onColResize: opts.onColResize,
                                             heldWidth: function (j) { return (self._colW && self._colW[j] != null) ? self._colW[j] : null; },
                                             extent: opts.resizeGuide || null })
                   : null;
        this._colgroup = this._branch.createElement("colgroup", "colgroup");
        this._table.appendChild(this._colgroup);
        this._headerRow = null;
        if (opts.showHeader !== false) {
            var thead = this._branch.createElement("thead", "thead");
            this._headerRow = this._branch.createElement("header-row", "tr");
            thead.appendChild(this._headerRow);
            this._table.appendChild(thead);
        }
        // container > wrapper > table, so an overlay can be a sibling of the
        // table in the wrapper's coordinates.
        this._wrap = this._branch.createElement("wrap", "div");
        css.addClass(this._wrap, hrg_wrap, hrg_frame, hrg_lit);   // positioned; framed; lit while it holds the focus
        this._wrap.appendChild(this._table);
        this._container.appendChild(this._wrap);

        this._slots = new RelGridSlots({
            branch: this._branch, table: this._table, colgroup: this._colgroup, headerRow: this._headerRow, drag: this._drag,
            onCellClick: opts.onCellClick, onCellDblClick: opts.onCellDblClick, onCellDown: opts.onCellDown,
            onCellDragTo: opts.onCellDragTo, onDragEnd: opts.onDragEnd
        });
        this._overlays = new RelGridOverlays({
            branch: this._branch, wrap: this._wrap, container: this._container,
            slotAt: function (i, j) { return self._slots.slotAt(i, j); }
        });
        this._cursorTd = null;   // the slot currently painted as the cursor
        this._selTds = [];       // the slots currently painted as selected
        // The one change the grid is never told about is a row growing because
        // some cell's content did; the observer catches it and re-measures.
        this._ro = null;
        if (typeof ResizeObserver === "function") {
            this._ro = new ResizeObserver(function () { self.placeGroups(); });
            this._ro.observe(this._table);
        }
        // A press ends wherever the pointer happens to be — off the table, off
        // the window — so the release is heard at the document, as the header
        // drag hears its own.
        this._mouseup = function () { self._slots.release(); };
        document.addEventListener("mouseup", this._mouseup);
    }

    /**
     * The structure for a shape: { headers: string[], rows: n }. The merged
     * cells' hosts go first — spans are read afresh on every pass, and the
     * slots they marked are unmarked, since those slots may live on — then
     * the matrix: kept when the shape is unchanged, minted fresh when not.
     * The cursor and selection paint is forgotten only with a fresh matrix;
     * on a kept one the painted slots are still the painted slots, and the
     * diff-painters take it from there. Slot CONTENT IS NOT TOUCHED.
     */
    render(shape) {
        this.closeGroups();
        if (this._slots.render(shape)) {                      // a fresh matrix: the old paint went with the old slots
            this._cursorTd = null;
            this._selTds = [];
        }
        return this;
    }

    /**
     * Apply widths in their POSITIONAL form — px | null per visible column —
     * in place: no slot is rebuilt and no cell moves. hrg-fixed engages once
     * any explicit width exists.
     *
     * A table whose EVERY column holds a width is at least their sum, and its
     * LAST presented column is elastic: it takes whatever the box has over,
     * and never less than it holds. Left at 100% of a wider box a fixed layout
     * would stretch every column past what it holds — widths nobody asked for
     * — and left at exactly the sum the table would stop short of its box; one
     * elastic column at the end is what a table in a wider box is expected to
     * do. With any column still free the table keeps the box's width and the
     * free columns share the remainder.
     */
    setColWidths(widths) {
        var any = false, all = true, sum = 0, n = this._slots.cols();
        this._colW = widths ? widths.slice() : null;
        for (var j = 0; j < n; j++) {
            var w = widths ? widths[j] : null, st = this._slots.colAt(j).style;
            if (w != null) { any = true; sum += w; if (st && st.setProperty) st.setProperty("--hrg-col-w", w + "px"); }
            else { all = false; if (st && st.removeProperty) st.removeProperty("--hrg-col-w"); }
        }
        css.toggleClass(this._table, hrg_fixed, any);
        var ts = this._table.style;
        if (ts && ts.setProperty) {
            if (any && all && n) {
                ts.setProperty("--hrg-table-w", "max(100%, " + sum + "px)");
                var lastSt = this._slots.colAt(n - 1).style;
                if (lastSt && lastSt.removeProperty) lastSt.removeProperty("--hrg-col-w");
            }
            else if (ts.removeProperty) ts.removeProperty("--hrg-table-w");
        }
        this.placeGroups();                                   // the slots moved; the hosts follow
        return this;
    }

    /** One row's height as laid out — the first slot's — or a nominal 24 where there is no geometry. */
    rowHeight() {
        var td = this.slotAt(0, 0);
        var h = (td && td.getBoundingClientRect) ? td.getBoundingClientRect().height : 0;
        return h > 0 ? h : 24;
    }

    /** Paint the cursor on one slot ({ i, j }) or on none (null). A diff, not a sweep. */
    paintCursor(ij) {
        var td = ij ? this.slotAt(ij.i, ij.j) : null;
        if (this._cursorTd !== td) {
            if (this._cursorTd) css.removeClass(this._cursorTd, hrg_cursor);
            if (td) css.addClass(td, hrg_cursor);
            this._cursorTd = td;
        }
        // The merged cells mirror: a group wears the cursor when it is on any of its slots.
        var groups = this._overlays.groups();
        for (var g = 0; g < groups.length; g++) {
            var grp = groups[g];
            css.toggleClass(grp.el, hrg_cursor, !!ij && ij.i === grp.i && ij.j >= grp.j && ij.j < grp.j + grp.n);
        }
        return this;
    }

    /**
     * Paint the selection: a list of rectangles in the presented space,
     * { i0, j0, i1, j1 } inclusive of both corners. The layout is told WHICH
     * SLOTS, never why — overlap between rectangles is nothing it has to
     * resolve, since a slot either wears the predicate or does not.
     */
    paintSelection(rects) {
        for (var k = 0; k < this._selTds.length; k++) css.removeClass(this._selTds[k], hrg_sel);
        this._selTds = [];
        var list = rects || [];
        for (var r = 0; r < list.length; r++) {
            var box = list[r];
            for (var i = box.i0; i <= box.i1; i++) {
                for (var j = box.j0; j <= box.j1; j++) {
                    var td = this.slotAt(i, j);
                    if (!td || css.hasClass(td, hrg_sel)) continue;      // already painted by an overlap
                    css.addClass(td, hrg_sel);
                    this._selTds.push(td);
                }
            }
        }
        // The merged cells mirror: a group is selected when any of its slots is.
        var groups = this._overlays.groups();
        for (var g = 0; g < groups.length; g++) {
            var grp = groups[g], on = false;
            for (var q = 0; q < list.length && !on; q++) {
                var b = list[q];
                on = b.i0 <= grp.i && grp.i <= b.i1 && b.j0 <= grp.j + grp.n - 1 && b.j1 >= grp.j;
            }
            css.toggleClass(grp.el, hrg_sel, on);
        }
        return this;
    }

    // ── the overlays: the anchor, the mask and its panel, the merged hosts ─

    openOverlay(i, j)   { return this._overlays.openOverlay(i, j); }
    closeOverlay()      { this._overlays.closeOverlay(); return this; }
    overlay()           { return this._overlays.overlay(); }
    openMask()          { return this._overlays.openMask(); }
    openPanel(element)  { return this._overlays.openPanel(element); }
    closePanel()        { this._overlays.closePanel(); return this; }
    closeMask()         { this._overlays.closeMask(); return this; }
    mask()              { return this._overlays.mask(); }
    panel()             { return this._overlays.panel(); }
    openGroup(i, j, n)  { return this._overlays.openGroup(i, j, n); }
    groupAt(i, j)       { return this._overlays.groupAt(i, j); }
    placeGroups()       { this._overlays.placeGroups(); return this; }
    closeGroups()       { this._overlays.closeGroups(); return this; }
    groups()            { return this._overlays.groups(); }

    // ── the states the table wears, and the keyboard ───────────────────────

    /** The table and the wrapper wear the deep state, so CSS and tests can see the handover. */
    setDeep(on) {
        css.toggleClass(this._table, hrg_deep, on);
        css.toggleClass(this._wrap, hrg_deep, on);
        return this;
    }

    /** The table wears the masked state while a question is pending. */
    setMasked(on) {
        css.toggleClass(this._table, hrg_masked, on);
        return this;
    }

    /** The keyboard host takes the keys. Without scrolling: a table taller than its pane
     *  would otherwise be pulled into view on every resume, moving the rows under the pointer.
     *  Showing the CURSOR is revealSlot's, by the least movement. */
    focus() { if (this._table.focus) this._table.focus({ preventScroll: true }); return this; }

    /** The least scroll that shows a slot; nothing moves when it already shows. */
    revealSlot(i, j) { relGridRevealSlot(this.slotAt(i, j)); return this; }

    /** Does the grid have the keyboard — the table, or something in it, holding the focus? */
    hasKeyboard() { return relGridHasKeyboard(this._table); }

    el() { return this._table; }
    slotAt(i, j) { return this._slots.slotAt(i, j); }
    rows() { return this._slots.rows(); }
    cols() { return this._slots.cols(); }

    destroy() {
        document.removeEventListener("mouseup", this._mouseup);
        if (this._ro) { this._ro.disconnect(); this._ro = null; }
        this._overlays.destroy();
        this._slots.destroy();
        // The wrapper, table and header band stay the branch's: the host dissolves it.
        if (this._wrap.parentNode) this._wrap.parentNode.removeChild(this._wrap);
        this._cursorTd = null;
        this._selTds = [];
    }
}
