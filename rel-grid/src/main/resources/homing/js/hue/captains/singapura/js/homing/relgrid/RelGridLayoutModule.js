// =============================================================================
// RelGridLayoutModule — RFC 0050 · Episode 2's LAYOUT branch: the structural chrome.
// Owns the <table>, the <colgroup>, the header band and the wrapper they sit
// in; the slot matrix of one arrangement is RelGridSlots, and what is laid
// OVER the slots — the editor's anchor, the mask and its panel, a merged
// cell's host — is RelGridOverlays. Positional only — everything is addressed
// by (i, j); the columns' identities arrive only to say the shape, and widths
// arrive per visible position. It never touches slot content: a slot's
// children are the domain's, a header slot's too, placed by the facade.
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
// classes are its MECHANICS and its FACTS (RelGridStyles); every look is the
// design's it is made with, worn on what it makes - its box, its slots, its
// overlays - and the cursor and the selection are RelGridMarks'.
//
//   new RelGridLayout({ container, branch, design, box?, label?, rowNumbers?, showHeader?, stickyHeader?, stickyInset?,
//                       onCellClick?, onCellDblClick?, onCellDown?, onCellDragTo?, onDragEnd?,
//                       onColResize?, resizeGuide? })   resizeGuide: an element, or a list of them
//   stickyHeader: the header cells stay at the top of whatever scrolls the table, and a
//                 revealed slot clears them; stickyInset: () → px of a band the HOST keeps stuck
//                 above the table — a group's header — that a revealed slot must clear too
//   render({ columns, rows }) / setColWidths(widths) / slotAt(i, j) / headerSlotAt(j) / rows() / cols()
//   paintCursor(ij) / paintSelection(rects) / setDeep(on) / setMasked(on)
//   focus() / revealSlot(i, j) / hasKeyboard() / el() / rowHeight()
//   openOverlay(i, j) / closeOverlay() / overlay()
//   openMask() / openPanel(el) / closePanel() / closeMask() / mask() / panel()
//   openGroup(i, j, n) / groupAt(i, j) / placeGroups() / closeGroups() / groups()
//   paintCopied(rects) / copied()   the copied state shown: an overlay over each block (RelGridCopiedOverlay)
// =============================================================================

class RelGridLayout {

    constructor(opts) {
        opts = opts || {};
        if (!opts.container) throw new Error("[RelGridLayout] opts.container is required");
        if (!opts.branch) throw new Error("[RelGridLayout] opts.branch is required");
        var self = this;
        this._container = opts.container;
        this._branch = opts.branch;                           // the grid's own; the host dissolves it
        this._design = opts.design;                           // what wears every leaf of the frame; the layout never learns a class
        this._table = this._branch.createElement("table", "table");
        css.addClass(this._table, hrg_table);
        this._table.setAttribute("tabindex", "0");            // the keyboard host
        if (opts.label) this._table.setAttribute("aria-label", opts.label);
        // The resize gesture lives in RelGridHeaderDrag; wired per <th> at render.
        // It starts a drag from the width held, and draws its guide down the
        // extent the host names — a host stacking tables names their stack.
        this._colW = null;                                    // the last positional widths applied
        this._drag = opts.onColResize
                   ? new RelGridHeaderDrag({ branch: this._branch, table: this._table, design: this._design, onColResize: opts.onColResize,
                                             heldWidth: function (j) { return (self._colW && self._colW[j] != null) ? self._colW[j] : null; },
                                             extent: opts.resizeGuide || null })
                   : null;
        this._colgroup = this._branch.createElement("colgroup", "colgroup");
        this._table.appendChild(this._colgroup);
        this._headerRow = null;
        this._sticky = opts.showHeader !== false && opts.stickyHeader === true;
        this._stickyInset = (typeof opts.stickyInset === "function") ? opts.stickyInset : null;
        if (opts.showHeader !== false) {
            var thead = this._branch.createElement("thead", "thead");
            this._headerRow = this._branch.createElement("header-row", "tr");
            thead.appendChild(this._headerRow);
            this._table.appendChild(thead);
        }
        // container > wrapper > table, so an overlay can be a sibling of the
        // table in the wrapper's coordinates.
        this._wrap = this._branch.createElement("wrap", "div");
        css.addClass(this._wrap, hrg_wrap);                        // positioned
        // THE BOX: the grid's edge, ground and corner, as the design draws them - on the wrapper, or on
        // the scrollport a host that scrolls the table names, so the edge stands round what is seen.
        this._box = opts.box || this._wrap;
        this._design.wear(GRID_FRAME.GRID_EDGE, this._box);
        this._wrap.appendChild(this._table);
        this._container.appendChild(this._wrap);

        this._slots = new RelGridSlots({
            branch: this._branch, table: this._table, colgroup: this._colgroup, headerRow: this._headerRow, drag: this._drag, design: this._design,
            sticky: this._sticky, gutter: opts.rowNumbers === true,
            onCellClick: opts.onCellClick, onCellDblClick: opts.onCellDblClick, onCellDown: opts.onCellDown,
            onCellDragTo: opts.onCellDragTo, onDragEnd: opts.onDragEnd, onGutterClick: opts.onGutterClick
        });
        this._overlays = new RelGridOverlays({
            branch: this._branch, wrap: this._wrap, container: this._container, design: this._design,
            slotAt: function (i, j) { return self._slots.slotAt(i, j); }
        });
        // THE COPIED RANGE: an overlay over each block the grid last copied, shown at the facade's direction.
        this._copied = new RelGridCopiedOverlay({
            branch: this._branch, wrap: this._wrap, design: this._design,
            slotAt: function (i, j) { return self._slots.slotAt(i, j); }
        });
        // THE MARKS: where the user is and what they have chosen, painted at the facade's direction.
        this._marks = new RelGridMarks({
            design: this._design, wrap: this._wrap,
            slotAt:   function (i, j) { return self._slots.slotAt(i, j); },
            gutterAt: function (i) { return self._slots.gutterAt(i); },
            cols:     function () { return self._slots.cols(); },
            groups:   function () { return self._overlays.groups(); }
        });
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
     * The structure for a shape: { columns: string[], rows: n }. The merged
     * cells' hosts go first — spans are read afresh on every pass, and the
     * slots they marked are unmarked, since those slots may live on — then
     * the matrix: kept when the shape is unchanged, minted fresh when not.
     * The cursor and selection paint is forgotten only with a fresh matrix;
     * on a kept one the painted slots are still the painted slots, and the
     * diff-painters take it from there. Slot CONTENT IS NOT TOUCHED.
     */
    render(shape) {
        this.closeGroups();
        if (this._slots.render(shape)) this._marks.forget();   // a fresh matrix: the old paint went with the old slots
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

    /** Paint the cursor on one slot ({ i, j }) or on none (null): the marks' to do. */
    paintCursor(ij) { this._marks.paintCursor(ij); return this; }

    /** Paint the selection - rectangles in the presented space, { i0, j0, i1, j1 }: the marks' to do. */
    paintSelection(rects) { this._marks.paintSelection(rects); return this; }

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
    placeGroups()       { this._overlays.placeGroups(); this._copied.place(); return this; }   // the copied overlays follow the slots too
    closeGroups()       { this._overlays.closeGroups(); return this; }
    groups()            { return this._overlays.groups(); }
    paintCopied(rects)  { this._copied.paint(rects); return this; }
    copied()            { return this._copied.boxes(); }

    // ── the states the table wears, and the keyboard ───────────────────────

    /** The table and the wrapper wear the deep fact, so tests can see the handover; the cursor wears the lent leaf. */
    setDeep(on) {
        css.toggleClass(this._table, hrg_deep, on);
        css.toggleClass(this._wrap, hrg_deep, on);
        this._marks.setDeep(on);
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
    revealSlot(i, j) { RelGridReveal.revealSlot(this.slotAt(i, j), this._inset()); return this; }

    /** What is stuck at the top of the port: the grid's own header when it sticks, and the host's band when it names one. */
    _inset() {
        var px = 0;
        if (this._sticky && this._headerRow && this._headerRow.getBoundingClientRect) px += this._headerRow.getBoundingClientRect().height || 0;
        if (this._stickyInset) { var band = Number(this._stickyInset()); if (band > 0) px += band; }
        return px;
    }

    /** Does the grid have the keyboard — the table, or something in it, holding the focus? */
    hasKeyboard() { return RelGridReveal.hasKeyboard(this._table); }

    el() { return this._table; }
    slotAt(i, j) { return this._slots.slotAt(i, j); }
    /** The header slot — the <th> — at a column position, or null: where a header cell is placed. */
    headerSlotAt(j) { return this._slots.thAt(j); }
    gutterAt(i)     { return this._slots.gutterAt(i); }       // the row's number cell, when there is a gutter
    rows() { return this._slots.rows(); }
    cols() { return this._slots.cols(); }

    destroy() {
        document.removeEventListener("mouseup", this._mouseup);
        this._marks.destroy();
        if (this._box !== this._wrap) this._design.shed(GRID_FRAME.GRID_EDGE, this._box);   // the host's element, given back bare
        if (this._ro) { this._ro.disconnect(); this._ro = null; }
        this._overlays.destroy();
        this._copied.close();
        this._slots.destroy();
        // The wrapper, table and header band stay the branch's: the host dissolves it.
        if (this._wrap.parentNode) this._wrap.parentNode.removeChild(this._wrap);
    }
}
