// =============================================================================
// RelTreeLayoutModule — RFC 0050 · Episode 3-ext1's CHROME: what the tree
// mints for itself on its own DomOpsParty branch — the wrap, the tree
// element that is the keyboard host, the rows in it, the mask and the panel
// over it — and the paint of the current row. Geometry and paint only:
// nothing here sees an identity or a value.
//
//   container > wrap.hrt_wrap > tree.hrt_tree[role=tree, tabindex=0] > row.hrt_row …
//                             > mask.hrt_mask > panel.hrt_panel      (while a question is pending)
//
// It is the channel's SURFACE — openMask, closeMask, setMasked, openPanel,
// closePanel, focus — the six the mask needs and the only way the core
// reaches the DOM. The mask is a wash over the rows that reads as
// unavailable and holds the focus; the panel is the tree's box for the
// domain's element, centred by the sheet, minted once per mask.
//
//   new RelTreeLayout({ branch, container, label?, caret?, folder?, onRowClick?, onRowDblClick?, onCaretClick? })
//   render(count) / paint(i, place) / rowAt(i) / rows()      the rows, through RelTreeRows
//   paintCursor(i)              the current row wears hrt_current; null for none
//   revealRow(i)                the least scroll that shows row i
//   hasKeyboard() / focus() / rowHeight() / el() / wrap()
//   openMask() / openPanel(el) / closePanel() / closeMask() / setMasked(on) / mask() / panel()
//   destroy()
// =============================================================================

class RelTreeLayout {

    constructor(opts) {
        this._branch = opts.branch;
        this._container = opts.container;
        var b = this._branch;
        this._wrap = b.createElement("wrap", "div");
        css.addClass(this._wrap, hrt_wrap);
        this._tree = b.createElement("tree", "div");
        css.addClass(this._tree, hrt_tree);
        this._tree.setAttribute("role", "tree");
        this._tree.setAttribute("tabindex", "0");              // the keyboard host
        if (opts.label) this._tree.setAttribute("aria-label", opts.label);
        this._wrap.appendChild(this._tree);
        this._container.appendChild(this._wrap);
        this._rows = new RelTreeRows({
            branch: b, host: this._tree, caret: opts.caret, folder: opts.folder,
            onRowClick: opts.onRowClick, onRowDblClick: opts.onRowDblClick, onCaretClick: opts.onCaretClick
        });
        this._current = -1;
        this._maskBranch = null; this._mask = null;
        this._panelBranch = null; this._panel = null;
    }

    el()   { return this._tree; }
    wrap() { return this._wrap; }

    // ── the rows ───────────────────────────────────────────────────────────

    render(count)   { return this._rows.render(count); }
    paint(i, place) { return this._rows.paint(i, place); }
    rowAt(i)        { return this._rows.rowAt(i); }
    rows()          { return this._rows.rows(); }

    /** The current row, and only it, wears the class; a released row needs no unpaint. */
    paintCursor(i) {
        var was = this._rows.rowAt(this._current);
        if (was) css.removeClass(was, hrt_current);
        this._current = (i === null || i === undefined) ? -1 : i;
        var now = this._rows.rowAt(this._current);
        if (now) css.addClass(now, hrt_current);
    }

    /** The least scroll, in whatever scrolls, that shows the row. */
    revealRow(i) {
        var row = this._rows.rowAt(i);
        if (row && typeof row.scrollIntoView === "function") row.scrollIntoView({ block: "nearest", inline: "nearest" });
    }

    /** Does the tree, or something in it — a cell, the mask — hold the focus? */
    hasKeyboard() {
        var a = (typeof document !== "undefined") ? document.activeElement : null;
        for (var n = a; n; n = n.parentNode) if (n === this._wrap) return true;
        return false;
    }

    /** Take the focus WITHOUT scrolling: a resume must not pull the tree into view. */
    focus() { if (this._tree.focus) this._tree.focus({ preventScroll: true }); return this; }

    /** A row's height, for a page of rows; the first row's, or a guess when there is none. */
    rowHeight() {
        var row = this._rows.rowAt(0);
        if (!row || !row.getBoundingClientRect) return 24;
        var h = row.getBoundingClientRect().height;
        return h > 0 ? h : 24;
    }

    // ── the mask, and the panel in it: the channel's surface ──────────────

    /** Mount the mask over the rows and give it the focus so the keys stop here. Idempotent. */
    openMask() {
        if (this._mask) return this._mask;
        var mb = this._maskBranch = this._branch.createBranch("mask");
        mb.activate(this);
        var el = mb.createElement("mask", "div");
        css.addClass(el, hrt_mask);
        el.setAttribute("tabindex", "-1");
        this._wrap.appendChild(el);
        this._mask = el;
        this._panel = null;
        if (el.focus) el.focus({ preventScroll: true });
        return el;
    }

    /** The panel inside the mask, minted once per mask, and the domain's element placed in it. */
    openPanel(element) {
        var mask = this.openMask();
        if (this._panel) {
            if (element && element.parentNode !== this._panel) this._panel.appendChild(element);
            return this._panel;
        }
        var pb = this._panelBranch = this._maskBranch.createBranch("panel");
        pb.activate(this);
        var el = pb.createElement("panel", "div");
        css.addClass(el, hrt_panel);
        el.setAttribute("tabindex", "-1");
        mask.appendChild(el);
        if (element) el.appendChild(element);
        this._panel = el;
        if (el.focus) el.focus({ preventScroll: true });
        return el;
    }

    /** The panel down — its branch dissolved — and the wash left up. The domain's element goes out alive. */
    closePanel() {
        if (this._panelBranch) { this._panelBranch.dissolve(); this._panelBranch = null; }
        this._panel = null;
        return this;
    }

    closeMask() {
        this.closePanel();
        if (this._maskBranch) { this._maskBranch.dissolve(); this._maskBranch = null; }
        this._mask = null;
        return this;
    }

    setMasked(on) { css.toggleClass(this._tree, hrt_masked, on); return this; }
    mask()  { return this._mask; }
    panel() { return this._panel; }

    destroy() {
        this.closeMask();
        this._rows.destroy();
        if (this._wrap.parentNode) this._wrap.parentNode.removeChild(this._wrap);
    }
}
