// =============================================================================
// RelGridOverlaysModule — RFC 0050 · Episode 2's OVERLAYS: the three things the
// layout lays over its slots, in the wrapper's own coordinates, so they
// scroll with the table for free and no scroll listener is needed anywhere.
//
//   THE EDITOR'S ANCHOR — an editor lives HERE, not in its slot: out of the
//   table it cannot widen a column, cannot stretch a row, and is not clipped
//   by the slot — so a cell may open something LARGER than itself. Laid over
//   the slot and sized to exactly the slot; how much more it takes is the
//   cell's.
//
//   THE MASK (ext6) dims the table so what is under it reads as unavailable,
//   and takes the focus so no key reaches the table beneath. Inside it the
//   PANEL is minted on request — a golden rectangle at the golden section of
//   what the person sees of the host's box — and the domain's element is
//   PLACED in it, as a cell's element is placed in a slot: the box is the
//   grid's, what is in it is the domain's, and nothing here reads what was
//   drawn. The panel is a branch of its own under the mask's, so it can come
//   down while the wash holds.
//
//   A MERGED CELL's host — laid over the n slots of a row it reaches across,
//   that the leading cell is placed into instead of its own slot. The n slots
//   stay — the matrix is whole, and a covered slot is a slot — and the host
//   follows their size: measured after every arrangement and every resize,
//   and again whenever the table changes size for a reason the grid is not
//   told about. It takes no pointer, so a click lands on the exact slot
//   beneath; the layout mirrors the slots' state onto it.
//
// Every element is minted through the DomOpsParty on a sub-branch of the
// grid's own, made for the occasion and dissolved with it. Geometry rides
// custom properties the element's class reads — never an inline style.
//
//   new RelGridOverlays({ branch, wrap, container, slotAt })
//   openOverlay(i, j) / closeOverlay() / overlay()
//   openMask() / openPanel(el) / closePanel() / closeMask() / mask() / panel()
//   openGroup(i, j, n) / groupAt(i, j) / placeGroups() / closeGroups() / groups()
// =============================================================================

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

/** Four measured numbers onto an element, as the custom properties its class reads. */
function _hrgPlace(el, r) {
    var st = el.style;
    if (!r || !st || !st.setProperty) return;
    st.setProperty("--hrg-left",   r.left + "px");
    st.setProperty("--hrg-top",    r.top + "px");
    st.setProperty("--hrg-width",  r.width + "px");
    st.setProperty("--hrg-height", r.height + "px");
}

class RelGridOverlays {

    constructor(opts) {
        this._branch = opts.branch;          // the grid's own; every overlay is a sub-branch of it
        this._wrap = opts.wrap;
        this._container = opts.container;
        this._slotAt = opts.slotAt;          // (i, j) → td | null, the layout's
        this._overlay = null;  this._overlayBranch = null;
        this._mask = null;     this._maskBranch = null;
        this._panel = null;    this._panelBranch = null;
        this._groups = [];     this._mergedBranch = null;   // { i, j, n, el }
    }

    /** The union of n slots from (i, j), in the wrapper's coordinates. */
    _unionRect(i, j, n) {
        var a = this._slotAt(i, j), b = this._slotAt(i, j + n - 1) || a;
        if (!a) return null;
        var ra = a.getBoundingClientRect(), rb = b.getBoundingClientRect(), w = this._wrap.getBoundingClientRect();
        return { left: ra.left - w.left, top: ra.top - w.top, width: rb.right - ra.left, height: ra.height };
    }

    // ── the editor's anchor ────────────────────────────────────────────────

    /**
     * Mint an anchor laid OVER a slot — or over the whole merged cell the slot
     * is in — and return it. The subtraction of two viewport rects cancels the
     * scroll offset, so what comes out is a content coordinate that scrolls
     * with the table. Sized to EXACTLY the slot: an anchor rather than a thing
     * with a size of its own.
     */
    openOverlay(i, j) {
        this.closeOverlay();
        var td = this._slotAt(i, j);
        if (!td) return null;
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
        _hrgPlace(el, r);
        this._wrap.appendChild(el);
        this._overlay = el;
        return el;
    }

    /** Take it down — its branch dissolved. Whatever the cell put in it goes with it, alive. */
    closeOverlay() {
        if (this._overlayBranch) { this._overlayBranch.dissolve(); this._overlayBranch = null; }
        this._overlay = null;
        return this;
    }

    overlay() { return this._overlay; }

    // ── the mask, and the panel in it ──────────────────────────────────────

    /**
     * Mount the mask over the table, and give it the focus so the keys stop
     * here. Idempotent: a second call while one is up is the same mask. What
     * it says is nothing until the domain hands a panel — the grid invents no
     * progress (ext6 law 225).
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
        if (el.focus) el.focus({ preventScroll: true });
        return el;
    }

    /** Take the panel down — its branch dissolved — and leave the wash up. The domain's element goes out with it, alive. */
    closePanel() {
        if (this._panelBranch) { this._panelBranch.dissolve(); this._panelBranch = null; }
        this._panel = null;
        return this;
    }

    /** Take the mask down, panel and all. */
    closeMask() {
        this.closePanel();
        if (this._maskBranch) { this._maskBranch.dissolve(); this._maskBranch = null; }
        this._mask = null;
        return this;
    }

    mask()  { return this._mask; }
    panel() { return this._panel; }

    // ── merged cells' hosts ────────────────────────────────────────────────

    /**
     * Mint a merged cell's host over the n slots from (i, j), mark those slots,
     * and return the host. The leading cell is placed into it by the facade.
     * Measured later, by placeGroups(), once every cell of the pass is in.
     */
    openGroup(i, j, n) {
        var lead = this._slotAt(i, j);
        if (!lead) return null;
        var marked = [lead];                                  // remembered, to be unmarked: the slots may outlive the group
        css.addClass(lead, hrg_lead);
        if (lead.style && lead.style.setProperty) lead.style.setProperty("--hrg-span", String(n));
        for (var k = 1; k < n; k++) {
            var td = this._slotAt(i, j + k);
            if (!td) break;
            css.addClass(td, hrg_covered);
            if (k === n - 1) css.addClass(td, hrg_group_end);
            marked.push(td);
        }
        if (!this._mergedBranch) {
            this._mergedBranch = this._branch.createBranch("merged");
            this._mergedBranch.activate(this);
        }
        var el = this._mergedBranch.createElement("merged-" + this._groups.length, "div");
        css.addClass(el, hrg_merge);
        this._wrap.appendChild(el);
        this._groups.push({ i: i, j: j, n: n, el: el, marked: marked });
        return el;
    }

    /** The marks off a group's slots: they are plain slots again. */
    _unmark(grp) {
        var lead = grp.marked[0];
        css.removeClass(lead, hrg_lead);
        if (lead.style && lead.style.removeProperty) lead.style.removeProperty("--hrg-span");
        for (var k = 1; k < grp.marked.length; k++) css.removeClass(grp.marked[k], hrg_covered, hrg_group_end);
    }

    /** The group a position is in, or null. */
    groupAt(i, j) {
        for (var g = 0; g < this._groups.length; g++) {
            var grp = this._groups[g];
            if (grp.i === i && j >= grp.j && j < grp.j + grp.n) return grp;
        }
        return null;
    }

    /** Size and place every merged cell's host over its slots, as they are now. */
    placeGroups() {
        for (var g = 0; g < this._groups.length; g++) {
            var grp = this._groups[g];
            _hrgPlace(grp.el, this._unionRect(grp.i, grp.j, grp.n));
        }
        return this;
    }

    /** Take every merged cell's host down — their branch dissolved, their slots unmarked. Whatever cell was in one is detached with it. */
    closeGroups() {
        for (var g = 0; g < this._groups.length; g++) this._unmark(this._groups[g]);
        if (this._mergedBranch) { this._mergedBranch.dissolve(); this._mergedBranch = null; }
        this._groups = [];
        return this;
    }

    groups() { return this._groups.slice(); }

    /** Everything down. The wrapper is the layout's. */
    destroy() {
        this.closeOverlay();
        this.closeMask();
        this.closeGroups();
    }
}
