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
// A PRESS-DRAG over the slots is captured here too, and reported as raw
// pointer facts — a press, each slot the pointer reaches while held, and the
// release. The layout decides nothing about what a drag MEANS; it does not
// even know there is a selection.
//
// The table sits in a WRAPPER the layout owns. Two things need somewhere to
// live that is not a table cell: an editor, which must be free to be larger
// than the cell it edits, and a MASK, which must cover the whole table. Both
// are positioned in the wrapper's own coordinates, so they scroll with the
// table for free and no scroll listener is needed anywhere.
//
// THE MASK (ext6) is the layout's second overlay. It dims the table so what is
// under it reads as unavailable, and it takes the focus so no key reaches the
// table beneath. Inside it the layout mints a PANEL on request — a golden
// rectangle at the golden section of what the person sees of the host's box —
// and PLACES the domain's element in it, as it places a cell's element in a
// slot: the box is the grid's, what is in it is the domain's, and the layout
// never reads what was drawn. The panel is a branch of its own under the
// mask's, so it can come down while the wash holds.
//
//   new RelGridLayout({ container, branch, label?, showHeader?, overflow?, onCellClick?,
//                       onCellDblClick?, onCellDown?, onCellDragTo?, onDragEnd?,
//                       onColResize?, resizeGuide? })   resizeGuide: an element, or a list of them
//   revealSlot(i, j)                          the least scroll that shows a slot
//   openOverlay(i, j) / closeOverlay()        the editor's anchor, over a slot
//   openMask() / openPanel(el) / closePanel() / closeMask()    the mask, and the domain's element placed in it
//   openGroup(i, j, n) / placeGroups()        a merged cell's host, over n slots
//
// A MERGED CELL is the layout's third overlay: a host laid over n slots of a
// row, in the wrapper, that the leading cell is placed into instead of its
// own slot. The n slots stay — the matrix is whole, and a covered slot is a
// slot — and the overlay mirrors their state: it wears the cursor when the
// cursor is on any of them and the selection when any of them is selected,
// so the whole group reads as one cell while the tracker keeps the exact
// square. It follows their size: measured after every arrangement and every
// resize, and again whenever the table changes size for a reason the grid is
// not told about, through a ResizeObserver. It takes no pointer, so a click
// lands on the exact slot beneath.
// =============================================================================

// ── the viewport follow ──────────────────────────────────────────────────
// The keyboard moves the cursor; the scrollport has to move with it, or the
// cursor walks out of the visible band and the grid looks dead. Native
// scrollIntoView({block:'nearest'}) has the right SEMANTICS — the least
// movement, and none when the slot already shows — but it takes no inset,
// and a sticky band (a group's header, one day) would park an upward move
// underneath it. So the same arithmetic is done here, in every ancestor that
// actually scrolls and then in the window. Ported from episode 1, where the
// inset carried the sticky header; here it is zero until something is sticky.
//
// The follow is NOT what focus() does. focus() takes the focus WITHOUT
// scrolling — a table taller than its pane would otherwise be pulled into
// view on every resume, moving the rows under the pointer — and the follow
// scrolls to the CURSOR, the one slot a person is looking at, by the least
// amount. The two are different questions with different answers.

/** The least delta that brings [er] inside the port. A slot TALLER than the
 *  port aligns to its top rather than its bottom: seeing where you are beats
 *  seeing where you end. */
function _hrgDelta(er, top, bottom, left, right) {
    var dy = 0, dx = 0;
    if (er.top < top)            dy = er.top - top;
    else if (er.bottom > bottom) dy = Math.min(er.bottom - bottom, er.top - top);
    if (er.left < left)          dx = er.left - left;
    else if (er.right > right)   dx = Math.min(er.right - right, er.left - left);
    return { dy: dy, dx: dx };
}
/** Does this element actually scroll? Overflowing content is not enough —
 *  overflow:visible spills without scrolling. Where no computed style is to
 *  be had, the overflow measurement stands on its own. */
function _hrgScrolls(el) {
    if (!(el.scrollHeight > el.clientHeight || el.scrollWidth > el.clientWidth)) return false;
    var cs = (typeof window !== "undefined" && window.getComputedStyle) ? window.getComputedStyle(el) : null;
    if (!cs) return true;
    return /auto|scroll|overlay/.test((cs.overflowY || "") + " " + (cs.overflowX || "") + " " + (cs.overflow || ""));
}
function _hrgRevealIn(scroller, el, topInset) {
    var sr = scroller.getBoundingClientRect();
    var top  = sr.top  + (scroller.clientTop  || 0);
    var left = sr.left + (scroller.clientLeft || 0);
    var d = _hrgDelta(el.getBoundingClientRect(), top + topInset, top + scroller.clientHeight, left, left + scroller.clientWidth);
    if (d.dy) scroller.scrollTop  += d.dy;
    if (d.dx) scroller.scrollLeft += d.dx;
}
function _hrgRevealInWindow(el, topInset) {
    if (typeof window === "undefined" || !window.scrollBy) return;
    var w = window.innerWidth || 0, h = window.innerHeight || 0;
    if (!w || !h) return;
    var d = _hrgDelta(el.getBoundingClientRect(), topInset, h, 0, w);
    if (d.dy || d.dx) window.scrollBy(d.dx, d.dy);
}

// THE LOOKS ARE TYPED — RelGridStyles, one class per thing the layout mints and
// one per state it paints, imported here as handles and applied through the
// css manager. Nothing here is a selector that reaches down: a state that
// must reach every slot beneath it (the focus lighting the cursor, a cell
// holding control dashing it, a group dimming a dormant member) is a custom
// property the state class sets on an ancestor and the slot's class reads.
// Geometry the layout measures rides custom properties on the element the
// same way — never an inline style.

/** Is el inside ancestor, or it? Walks parentNode: the stub's elements have no contains(). */
function _hrgWithinEl(el, ancestor) {
    for (var p = el; p; p = p.parentNode) if (p === ancestor) return true;
    return false;
}

/** The two modifiers that change what a click means. Meta stands in for ctrl. */
function _hrgMods(e) {
    e = e || {};
    return { shift: !!e.shiftKey, ctrl: !!(e.ctrlKey || e.metaKey) };
}

/** wrap | clip | ellipsis — ellipsis unless the host says otherwise. */
function _hrgOverflowClass(choice) {
    if (choice === "wrap") return hrg_ov_wrap;
    if (choice === "clip") return hrg_ov_clip;
    return hrg_ov_ellipsis;
}

/** Four measured numbers onto an element, as the custom properties its class reads. */
function _hrgPlace(el, r) {
    var st = el.style;
    if (!r || !st || !st.setProperty) return;
    st.setProperty("--hrg-left",   r.left + "px");
    st.setProperty("--hrg-top",    r.top + "px");
    st.setProperty("--hrg-width",  r.width + "px");
    st.setProperty("--hrg-height", r.height + "px");
}

var _HRG_PHI = (1 + Math.sqrt(5)) / 2;          // φ ≈ 1.618
var _HRG_PANEL_MIN_W = 320, _HRG_PANEL_MAX_W = 720;

/**
 * The panel's box inside a visible area of W×H: the largest GOLDEN RECTANGLE
 * that sits at the golden section of the area in BOTH dimensions.
 *
 * The panel is itself golden — its width is φ times its height — and it
 * takes at most the larger part of the golden cut of each dimension: no
 * wider than W/φ, no taller than H/φ, whichever binds. So over a wide, short
 * table it is the height that decides, and over a tall, narrow one the
 * width. It is centred. Two bounds keep it usable rather than merely
 * proportional: a floor, so a small table still mints a panel that can hold
 * something, and a ceiling, so a vast one does not mint a page. Under the
 * floor the panel may overflow a small area; that is the floor doing its job,
 * and it is the host's geometry that decides whether the overflow is seen.
 *
 * Whole pixels, so the edges are crisp.
 */
function _hrgGoldenBox(W, H) {
    // h ≤ H/φ with h = w/φ means w ≤ H; and w ≤ W/φ. The tighter one wins.
    var w = Math.min(W / _HRG_PHI, H);
    w = Math.max(_HRG_PANEL_MIN_W, Math.min(_HRG_PANEL_MAX_W, w));
    var h = w / _HRG_PHI;
    return {
        left:   Math.round((W - w) / 2),
        top:    Math.round((H - h) / 2),
        width:  Math.round(w),
        height: Math.round(h)
    };
}

/**
 * The area the panel is centred on, in the wrapper's own coordinates: the
 * CONTAINER the host gave the grid, clipped to the viewport.
 *
 * Not the table. The mask dims the table, but the panel is for the person,
 * and what the person sees is the host's box: a scrollport shows part of a
 * tall table and all of a short one with room to spare, and in both cases the
 * panel belongs in the middle of that box — not in the middle of a table
 * whose middle may be off-screen, or whose height is a third of the panel's.
 * The viewport clip is for a container taller than the window, so the panel
 * lands on the screen rather than on the page.
 */
function _hrgVisibleBox(wrap, container, viewport) {
    var a = container;
    if (viewport) {
        var l = Math.max(a.left, viewport.left), t = Math.max(a.top, viewport.top);
        var r = Math.min(a.right, viewport.right), b = Math.min(a.bottom, viewport.bottom);
        if (r > l && b > t) a = { left: l, top: t, width: r - l, height: b - t };
    }
    return { left: a.left - wrap.left, top: a.top - wrap.top, width: a.width, height: a.height };
}

class RelGridLayout {

    constructor(opts) {
        opts = opts || {};
        if (!opts.container) throw new Error("[RelGridLayout] opts.container is required");
        if (!opts.branch) throw new Error("[RelGridLayout] opts.branch is required");
        this._container = opts.container;
        this._branch = opts.branch;                           // the grid's own; the host dissolves it
        this._slotsBranch = null;                             // the current arrangement's slots
        this._overlayBranch = null;
        this._maskBranch = null;
        this._panelBranch = null;
        this._mergedBranch = null;
        this._onCellClick = opts.onCellClick || null;         // (i, j, mods)
        this._onCellDblClick = opts.onCellDblClick || null;   // (i, j)
        this._onCellDown = opts.onCellDown || null;           // (i, j, mods) — a press
        this._onCellDragTo = opts.onCellDragTo || null;       // (i, j) — reached while held
        this._onDragEnd = opts.onDragEnd || null;             // () — released
        this._press = null;                                   // the slot a button went down on
        this._table = this._branch.createElement("table", "table");
        css.addClass(this._table, hrg_table);
        this._table.setAttribute("tabindex", "0");            // the keyboard host
        if (opts.label) this._table.setAttribute("aria-label", opts.label);
        // The resize gesture lives in RelGridHeaderDrag; wired per <th> at render.
        // It starts a drag from the width held, and draws its guide down the
        // extent the host names — a host stacking tables names their stack.
        var lay = this;
        this._colW = null;                                    // the last positional widths applied
        this._drag = opts.onColResize
                   ? new RelGridHeaderDrag({ branch: this._branch, table: this._table, onColResize: opts.onColResize,
                                             heldWidth: function (j) { return (lay._colW && lay._colW[j] != null) ? lay._colW[j] : null; },
                                             extent: opts.resizeGuide || null })
                   : null;
        this._colgroup = this._branch.createElement("colgroup", "colgroup");
        this._table.appendChild(this._colgroup);
        this._showHead = opts.showHeader !== false;
        if (this._showHead) {
            this._thead = this._branch.createElement("thead", "thead");
            this._headerRow = this._branch.createElement("header-row", "tr");
            this._thead.appendChild(this._headerRow);
            this._table.appendChild(this._thead);
        } else {
            this._thead = null; this._headerRow = null;
        }
        this._tbody = null;                                   // minted with the slots, per arrangement
        // container > wrapper > table, so an overlay can be a sibling of the
        // table in the wrapper's coordinates.
        this._wrap = this._branch.createElement("wrap", "div");
        css.addClass(this._wrap, hrg_wrap, hrg_frame, hrg_lit);   // positioned; framed; lit while it holds the focus
        this._wrap.appendChild(this._table);
        this._container.appendChild(this._wrap);
        this._overlay = null;
        this._mask = null;       // the mask, while a question is pending
        this._panel = null;      // the domain's canvas inside it, once asked for
        this._groups = [];       // the merged cells' hosts: { i, j, n, el }
        // The one change the grid is never told about is a row growing because
        // some cell's content did; the observer catches it and re-measures.
        this._ro = null;
        if (typeof ResizeObserver === "function") {
            var lay = this;
            this._ro = new ResizeObserver(function () { lay.placeGroups(); });
            this._ro.observe(this._table);
        }
        css.addClass(this._table, _hrgOverflowClass(opts.overflow));
        this._slots = [];        // [i][j] → td
        this._cursorTd = null;   // the slot currently painted as the cursor
        this._selTds = [];       // the slots currently painted as selected

        // A press ends wherever the pointer happens to be — off the table, off
        // the window — so the release is heard at the document, as the header
        // drag hears its own.
        var self = this;
        this._mouseup = function () {
            if (!self._press) return;
            self._press = null;
            if (self._onDragEnd) self._onDragEnd();
        };
        document.addEventListener("mouseup", this._mouseup);
    }

    /**
     * (Re)build the structure for a shape: { headers: string[], rows: n }.
     * Slots are minted fresh, on a branch of their own, and the last
     * arrangement's branch is dissolved first — that is what releases its
     * cols, headers, body and slots. Slot CONTENT IS NOT TOUCHED: whatever sits
     * in an old slot rides the released subtree until the facade re-places it.
     */
    render(shape) {
        var headers = (shape && shape.headers) || [];
        var rows = (shape && shape.rows) || 0;
        this.closeGroups();                                   // the slots they sat over are going

        if (this._slotsBranch) this._slotsBranch.dissolve();
        var slots = this._slotsBranch = this._branch.createBranch("slots");
        slots.activate(this);
        for (var h = 0; h < headers.length; h++) {
            var col = slots.createElement("col-" + h, "col");   // widths need cols regardless
            css.addClass(col, hrg_col);
            this._colgroup.appendChild(col);
            if (!this._headerRow) continue;
            var th = slots.createElement("th-" + h, "th");
            css.addClass(th, hrg_th);
            th.textContent = headers[h];
            if (this._drag) this._drag.wire(th, h, slots);
            this._headerRow.appendChild(th);
        }

        this._tbody = slots.createElement("tbody", "tbody");
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
            // The press, and every slot the pointer reaches while it is held.
            // Moving within the slot it went down on reports nothing — a press
            // that never leaves its cell is a click, and is handled as one.
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
                // Once it HAS left, coming back to the press's slot is a report
                // too — that is how a drag shrinks to 1x1 again.
                if (self._onCellDragTo) self._onCellDragTo(i, j);
            });
        };
        for (var i = 0; i < rows; i++) {
            var tr = slots.createElement("tr-" + i, "tr");
            var rowSlots = [];
            for (var j = 0; j < headers.length; j++) {
                var td = slots.createElement("td-" + i + "-" + j, "td");
                css.addClass(td, hrg_td);
                wire(td, i, j);
                tr.appendChild(td);
                rowSlots.push(td);
            }
            this._tbody.appendChild(tr);
            this._slots.push(rowSlots);
        }
        this._table.appendChild(this._tbody);
        return this;
    }

    /**
     * Apply widths in their POSITIONAL form — px | null per visible column —
     * in place: no slot is rebuilt and no cell moves. hrg-fixed engages once
     * any explicit width exists.
     */
    setColWidths(widths) {
        var any = false, all = true, sum = 0, cols = this._colgroup.children;
        this._colW = widths ? widths.slice() : null;
        for (var j = 0; j < cols.length; j++) {
            var w = widths ? widths[j] : null, st = cols[j].style;
            if (w != null) { any = true; sum += w; if (st && st.setProperty) st.setProperty("--hrg-col-w", w + "px"); }
            else { all = false; if (st && st.removeProperty) st.removeProperty("--hrg-col-w"); }
        }
        css.toggleClass(this._table, hrg_fixed, any);
        // A table whose EVERY column holds a width is at least their sum, and
        // its LAST presented column is elastic: it takes whatever the box has
        // over, and never less than it holds. Left at 100% of a wider box a
        // fixed layout would stretch every column past what it holds — widths
        // nobody asked for — and left at exactly the sum the table would stop
        // short of its box; one elastic column at the end is what a table in a
        // wider box is expected to do. With any column still free the table
        // keeps the box's width and the free columns share the remainder.
        var ts = this._table.style;
        if (ts && ts.setProperty) {
            if (any && all && cols.length) {
                ts.setProperty("--hrg-table-w", "max(100%, " + sum + "px)");
                var lastSt = cols[cols.length - 1].style;
                if (lastSt && lastSt.removeProperty) lastSt.removeProperty("--hrg-col-w");
            }
            else if (ts.removeProperty) ts.removeProperty("--hrg-table-w");
        }
        this.placeGroups();                                   // the slots moved; the hosts follow
        return this;
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
        for (var g = 0; g < this._groups.length; g++) {
            var grp = this._groups[g];
            var on = !!ij && ij.i === grp.i && ij.j >= grp.j && ij.j < grp.j + grp.n;
            css.toggleClass(grp.el, hrg_cursor, on);
        }
        return this;
    }

    /**
     * Paint the selection: a list of rectangles in the presented space,
     * {@code { i0, j0, i1, j1 }} inclusive of both corners. The layout is told
     * WHICH SLOTS, never why — overlap between rectangles is nothing it has to
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
        for (var g = 0; g < this._groups.length; g++) {
            var grp = this._groups[g], on = false;
            for (var q = 0; q < list.length && !on; q++) {
                var b = list[q];
                on = b.i0 <= grp.i && grp.i <= b.i1 && b.j0 <= grp.j + grp.n - 1 && b.j1 >= grp.j;
            }
            css.toggleClass(grp.el, hrg_sel, on);
        }
        return this;
    }

    /**
     * Mint a merged cell's host over the n slots from (i, j), mark those slots,
     * and return the host. The leading cell is placed into it by the facade.
     * Measured later, by placeGroups(), once every cell of the pass is in.
     */
    openGroup(i, j, n) {
        var lead = this.slotAt(i, j);
        if (!lead) return null;
        css.addClass(lead, hrg_lead);
        if (lead.style && lead.style.setProperty) lead.style.setProperty("--hrg-span", String(n));
        for (var k = 1; k < n; k++) {
            var td = this.slotAt(i, j + k);
            if (!td) break;
            css.addClass(td, hrg_covered);
            if (k === n - 1) css.addClass(td, hrg_group_end);
        }
        if (!this._mergedBranch) {
            this._mergedBranch = this._branch.createBranch("merged");
            this._mergedBranch.activate(this);
        }
        var el = this._mergedBranch.createElement("merged-" + this._groups.length, "div");
        css.addClass(el, hrg_merge);
        this._wrap.appendChild(el);
        this._groups.push({ i: i, j: j, n: n, el: el });
        return el;
    }

    /** The group a position is in, or null. */
    groupAt(i, j) {
        for (var g = 0; g < this._groups.length; g++) {
            var grp = this._groups[g];
            if (grp.i === i && j >= grp.j && j < grp.j + grp.n) return grp;
        }
        return null;
    }

    /** The union of n slots from (i, j), in the wrapper's coordinates. */
    _unionRect(i, j, n) {
        var a = this.slotAt(i, j), b = this.slotAt(i, j + n - 1) || a;
        if (!a) return null;
        var ra = a.getBoundingClientRect(), rb = b.getBoundingClientRect(), w = this._wrap.getBoundingClientRect();
        return { left: ra.left - w.left, top: ra.top - w.top, width: rb.right - ra.left, height: ra.height };
    }

    /** Size and place every merged cell's host over its slots, as they are now. */
    placeGroups() {
        for (var g = 0; g < this._groups.length; g++) {
            var grp = this._groups[g];
            _hrgPlace(grp.el, this._unionRect(grp.i, grp.j, grp.n));
        }
        return this;
    }

    /** Take every merged cell's host down — their branch dissolved. Whatever cell was in one is detached with it. */
    closeGroups() {
        if (this._mergedBranch) { this._mergedBranch.dissolve(); this._mergedBranch = null; }
        this._groups = [];
        return this;
    }

    groups() { return this._groups.slice(); }

    /**
     * Mint a host laid OVER a slot, in the wrapper's coordinates, and return
     * it. The subtraction of two viewport rects cancels the scroll offset, so
     * what comes out is a content coordinate that scrolls with the table.
     *
     * <p>Sized to EXACTLY the slot, and it is an anchor: a cell that wants to
     * look in-cell fills it, and one that wants a picker or a panel hangs that
     * off it as its own positioned child. Either way the table below is
     * untouched, which is the whole reason the editor left it.</p>
     */
    openOverlay(i, j) {
        this.closeOverlay();
        var td = this.slotAt(i, j);
        if (!td) return null;
        // A position inside a merged cell edits the merged cell: the anchor is
        // the whole group's box.
        var grp = this.groupAt(i, j);
        var r = grp ? this._unionRect(grp.i, grp.j, grp.n) : null;
        if (!r) {
            var a = td.getBoundingClientRect(), w = this._wrap.getBoundingClientRect();
            r = { left: a.left - w.left, top: a.top - w.top, width: a.width, height: a.height };
        }
        var ov = this._overlayBranch = this._branch.createBranch("overlay");
        ov.activate(this);
        var el = ov.createElement("overlay", "div");
        css.addClass(el, hrg_edit);
        // EXACTLY the slot, so the overlay is an anchor rather than a thing
        // with a size of its own. A cell that wants more room hangs it off
        // this box as its own positioned child — which keeps the geometry
        // the grid states exact, and leaves the cell free.
        _hrgPlace(el, r);
        this._wrap.appendChild(el);
        this._overlay = el;
        return el;
    }

    /** Take it down — its branch dissolved. Whatever the cell put in it goes with it. */
    closeOverlay() {
        if (this._overlayBranch) { this._overlayBranch.dissolve(); this._overlayBranch = null; }
        this._overlay = null;
        return this;
    }

    overlay() { return this._overlay; }

    /**
     * Mount the mask over the table, and give it the focus so the keys stop
     * here. Idempotent: a second call while one is up is the same mask. What
     * it says is nothing until the domain asks for the panel — the grid
     * invents no progress (ext6 law 225).
     */
    openMask() {
        if (this._mask) return this._mask;
        var mb = this._maskBranch = this._branch.createBranch("mask");
        mb.activate(this);
        var el = mb.createElement("mask", "div");
        css.addClass(el, hrg_mask);
        el.setAttribute("tabindex", "-1");         // focusable, and not in the tab order
        this._wrap.appendChild(el);
        this._mask = el;
        this._panel = null;
        if (el.focus) el.focus({ preventScroll: true });   // it is already in view; a scroll would move the table
        return el;
    }

    /**
     * Mint the panel inside the mask, mounting the mask first if it is not
     * up, and PLACE the domain's element in it. Sized by the golden rule over
     * the wrapper's box as it is NOW, and centred on it; minted once per
     * mask, so a second call places into the same box. The focus moves into
     * it, and the domain may move it further in.
     */
    openPanel(element) {
        var mask = this.openMask();
        if (this._panel) {
            if (element && element.parentNode !== this._panel) this._panel.appendChild(element);
            return this._panel;
        }
        var pb = this._panelBranch = this._maskBranch.createBranch("panel");
        pb.activate(this);
        // Centred on what the person can SEE of the host's box, sized by the
        // golden rule over that, and placed in the wrapper's coordinates so it
        // scrolls with the table like everything else the wrapper holds.
        var vp = (typeof window !== "undefined" && window.innerWidth)
               ? { left: 0, top: 0, right: window.innerWidth, bottom: window.innerHeight } : null;
        var seen = _hrgVisibleBox(this._wrap.getBoundingClientRect(), this._container.getBoundingClientRect(), vp);
        var box = _hrgGoldenBox(seen.width, seen.height);
        var el = pb.createElement("panel", "div");
        css.addClass(el, hrg_panel);
        el.setAttribute("tabindex", "-1");
        _hrgPlace(el, { left: seen.left + box.left, top: seen.top + box.top, width: box.width, height: box.height });
        mask.appendChild(el);
        if (element) el.appendChild(element);
        this._panel = el;
        if (el.focus) el.focus({ preventScroll: true });   // it is already in view; a scroll would move the table
        return el;
    }

    /** Take the panel down — its branch dissolved — and leave the wash up. The domain's element goes out with it, alive. */
    closePanel() {
        if (this._panelBranch) { this._panelBranch.dissolve(); this._panelBranch = null; }
        this._panel = null;
        return this;
    }

    /** Take the mask down, panel and all — their branch dissolved. */
    closeMask() {
        this.closePanel();
        if (this._maskBranch) { this._maskBranch.dissolve(); this._maskBranch = null; }
        this._mask = null;
        return this;
    }

    mask()  { return this._mask; }
    panel() { return this._panel; }

    /** The table wears the deep state, so CSS and tests can see the handover. */
    setDeep(on) {
        css.toggleClass(this._table, hrg_deep, on);
        css.toggleClass(this._wrap, hrg_deep, on);
        return this;
    }

    /** And the masked state, for the same reason. */
    setMasked(on) {
        css.toggleClass(this._table, hrg_masked, on);
        return this;
    }

    /** The keyboard host takes the keys. Without scrolling: a table taller than its pane
     *  would otherwise be pulled into view on every resume, moving the rows under the pointer.
     *  Showing the CURSOR is revealSlot's, by the least movement. */
    focus() { if (this._table.focus) this._table.focus({ preventScroll: true }); return this; }

    /**
     * The least scroll that shows a slot: in every ancestor that actually
     * scrolls, innermost first, and then the window. Nothing moves when the
     * slot already shows.
     */
    revealSlot(i, j) {
        var el = this.slotAt(i, j);
        if (!el || !el.getBoundingClientRect || typeof document === "undefined") return this;
        var inset = 0;                                        // a sticky band's height, when there is one
        var node = el.parentNode;
        while (node && node !== document.body && node !== document.documentElement) {
            if (node.getBoundingClientRect && _hrgScrolls(node)) {
                _hrgRevealIn(node, el, inset);
                inset = 0;                                    // claimed by the innermost port
            }
            node = node.parentNode;
        }
        _hrgRevealInWindow(el, inset);
        return this;
    }

    /** Does the grid have the keyboard — the table, or something in it, holding the focus? */
    hasKeyboard() {
        if (typeof document === "undefined") return false;
        var a = document.activeElement;
        if (!a || a === document.body) return false;
        return a === this._table || _hrgWithinEl(a, this._table);
    }

    el() { return this._table; }

    /** The slot at a position, or null. */
    slotAt(i, j) {
        var row = this._slots[i];
        return (row && row[j]) ? row[j] : null;
    }

    rows() { return this._slots.length; }
    cols() { return this._slots.length ? this._slots[0].length : 0; }

    destroy() {
        document.removeEventListener("mouseup", this._mouseup);
        if (this._ro) { this._ro.disconnect(); this._ro = null; }
        this.closeOverlay();
        this.closeMask();
        this.closeGroups();
        if (this._slotsBranch) { this._slotsBranch.dissolve(); this._slotsBranch = null; }
        // The wrapper, table and header band stay the branch's: the host dissolves it.
        if (this._wrap.parentNode) this._wrap.parentNode.removeChild(this._wrap);
        this._slots = [];
        this._cursorTd = null;
        this._selTds = [];
        this._press = null;
    }
}
