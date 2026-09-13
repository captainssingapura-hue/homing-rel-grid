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
// exactly what it replaces and nothing is ever removed by hand. An injected
// stylesheet with theme tokens only, under the hrg- prefix so this grid and
// the live one can share a page.
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

var _HRG_STYLE_ID = "homing-rel-grid-style";
var _HRG_STYLE_CSS = [
    // The positioned parent for anything that is not a cell.
    ".hrg-wrap{position:relative;}",
    // LIT, NOT LIFTED. The grid that holds the focus is visibly the grid that
    // holds the focus, and it says so with light: a frame that catches it, and
    // a cursor at full strength only then. Nothing moves — no elevation, no
    // offset shadow, no transform — because a table that rises when clicked
    // reads as a card. The frame is a layer OVER the table, drawn INSET,
    // because a host that mounts the grid in a scrollport clips anything
    // outside the box; it takes no pointer and sits under the editor (50) and
    // the mask (60). Focus is :focus-within — the browser's own fact — so the
    // editor's overlay and the mask's panel count as the grid holding it, and
    // the grid keeps no fact of its own.
    //
    // A hint of morphism, over the semantic tokens only, so it follows any
    // palette: an accent-tinted hairline, a catch of light on the inner
    // top-left edge (white mixed into the raised surface, so a dark theme gets
    // a dim catch), and a soft inner glow.
    //
    // LATER — themed lighting. A theme with an idiom of its own (a hard
    // brutalist ring, a Material outline, a neumorphic relief) should be able
    // to say so, and the way to do that in the typed CSS substrate is a grid
    // vocabulary of tokens (--hrg-frame-rest, --hrg-frame-focus, --hrg-cursor-
    // rest, --hrg-cursor-focus, --hrg-focus-transition) that every registered
    // theme provides, with these values as the fallbacks. Not done here: the
    // substrate's vocabulary is the studio's alone today (StudioVars, with
    // every ThemeVariables in studio-base providing exactly it), so a component
    // cannot yet contribute tokens without either a per-deployment registry
    // wrapper or a fork of every theme. It waits on the theme design system
    // growing a way for a component to declare a vocabulary of its own.
    ".hrg-wrap::after{content:\"\";position:absolute;left:0;top:0;right:0;bottom:0;",
    "  pointer-events:none;z-index:40;",
    "  box-shadow:inset 0 0 0 1px var(--color-border);",
    "  transition:box-shadow .18s ease;}",
    ".hrg-wrap:focus-within::after{",
    "  box-shadow:inset 0 0 0 1px color-mix(in srgb, var(--color-accent) 60%, var(--color-border)),",
    "    inset 1px 1px 0 1px color-mix(in srgb, white 35%, var(--color-surface-raised)),",
    "    inset 0 0 14px color-mix(in srgb, var(--color-accent) 18%, transparent);}",
    // An editor lives HERE, not in its slot: out of the table it cannot widen a
    // column, cannot stretch a row, and is not clipped by the slot — so a cell
    // may open something LARGER than itself. The grid places it over the slot
    // and sizes it to at least the slot; how much more it takes is the cell's.
    ".hrg-edit{position:absolute;z-index:50;box-sizing:border-box;",
    "  background:var(--color-surface);color:var(--color-text-primary);",
    "  outline:2px solid var(--color-accent);outline-offset:-2px;}",
    // THE MASK. Over the whole table, in the wrapper: a wash heavy enough that
    // the rows beneath read as unavailable rather than current, and focusable
    // so the keys stop here. It dims rather than replaces — the context stays,
    // nothing jumps — which ext6 argued for and this is.
    ".hrg-mask{position:absolute;left:0;top:0;right:0;bottom:0;z-index:60;outline:none;",
    "  background:color-mix(in srgb, var(--color-surface) 64%, transparent);}",
    // THE PANEL: the domain's canvas, sized by the grid. Raised, bordered,
    // and scrolling inside itself if the domain draws more than fits.
    ".hrg-panel{position:absolute;box-sizing:border-box;overflow:auto;outline:none;",
    "  background:var(--color-surface-raised);color:var(--color-text-primary);",
    "  border:1px solid var(--color-border);border-radius:8px;",
    "  box-shadow:0 12px 36px rgba(0,0,0,0.32);}",
    // What a slot does with content too wide for it. The grid decides, because
    // it is the slot's box.
    //
    // Ellipsis by default, because the alternative — wrapping, which is what
    // plain CSS does — makes one row six lines tall while its neighbours stay
    // at one, and a grid whose rows disagree about their height reads as
    // broken.
    //
    // What this does NOT do on its own is truncate. In an auto-layout table
    // nowrap moves the give from the row to the COLUMN: the column widens to
    // the unbroken string and the table scrolls sideways instead. The ellipsis
    // engages only once a column has a definite width — after a resize, or
    // under hrg-fixed. That is the honest bound of this option, and the reason
    // a declared per-column width is the other half of the story.
    ".hrg-ov-ellipsis .hrg-td > *{overflow:hidden;white-space:nowrap;text-overflow:ellipsis;}",
    ".hrg-ov-clip .hrg-td > *{overflow:hidden;white-space:nowrap;}",
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
    // position:relative makes every slot a containing block, so a cell may
    // lay an editor OVER it instead of IN it. That matters because the table
    // is auto-layout: anything in flow contributes its intrinsic width to the
    // column, and an <input> or a <select> is far wider than the text it
    // replaces — so an editor in flow moves every column while it is open.
    // Where the editor sits is the CELL's business; giving it something to sit
    // against is geometry, and geometry is the grid's.
    ".hrg-td{padding:0;position:relative;border-bottom:1px solid var(--color-border);",
    "  border-right:1px solid color-mix(in srgb, var(--color-border) 50%, transparent);",
    "  vertical-align:middle;overflow:hidden;}",
    // A MERGED CELL: the leading cell's host, laid over the n slots it reaches
    // across — opaque, so the slots beneath and the lines between them are
    // covered; no pointer, so a click lands on the exact slot beneath; and
    // wearing the group's state, mirrored from the slots: the cursor when the
    // cursor is on any of them, the wash when any is selected, dashed while
    // deep, dimmed while the grid does not hold the focus — the slot's own
    // rules, one level up. The slot marks stay, for a host that wants them.
    ".hrg-merge{position:absolute;z-index:30;box-sizing:border-box;overflow:hidden;pointer-events:none;",
    "  background:var(--color-surface);color:var(--color-text-primary);",
    "  transition:outline-color .18s ease;}",
    ".hrg-merge.hrg-sel{background:color-mix(in srgb, var(--color-accent) 12%, var(--color-surface));}",
    ".hrg-merge.hrg-cursor{outline:2px solid color-mix(in srgb, var(--color-accent) 45%, var(--color-border));outline-offset:-2px;}",
    ".hrg-wrap:focus-within .hrg-merge.hrg-cursor{outline-color:var(--color-accent);}",
    ".hrg-wrap.hrg-deep .hrg-merge.hrg-cursor{outline-style:dashed;}",
    ".hrg-td.hrg-lead{border-right-color:transparent;}",
    ".hrg-td.hrg-covered{border-right-color:transparent;}",
    ".hrg-td.hrg-covered.hrg-group-end{border-right-color:color-mix(in srgb, var(--color-border) 50%, transparent);}",
    // The selection: a wash on every slot the resolved list covers. A slot may
    // wear this and the cursor at once — with an empty list the selection IS
    // the cursor's 1x1, so the cursor's slot is always one of them.
    ".hrg-td.hrg-sel{background:color-mix(in srgb, var(--color-accent) 12%, transparent);}",
    // The cursor: painted on the slot, never on the cell. Solid while shallow;
    // dashed while the cell is deep, so the handover is visible. Full accent
    // only while the grid holds the focus; dimmed towards the border otherwise,
    // so a cursor in a table that is NOT listening does not look like one that is.
    ".hrg-td.hrg-cursor{outline:2px solid color-mix(in srgb, var(--color-accent) 45%, var(--color-border));",
    "  outline-offset:-2px;transition:outline-color .18s ease;}",
    ".hrg-wrap:focus-within .hrg-td.hrg-cursor{outline-color:var(--color-accent);}",
    ".hrg-table.hrg-deep .hrg-td.hrg-cursor{outline-style:dashed;}"
].join("\n");

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

/** wrap | clip | ellipsis — ellipsis unless the host says otherwise. */
function _hrgOverflowClass(choice) {
    if (choice === "wrap") return "hrg-ov-wrap";
    if (choice === "clip") return "hrg-ov-clip";
    return "hrg-ov-ellipsis";
}

function _hrgHasClass(el, c) {
    if (!el.className) return false;
    var parts = el.className.split(" ");
    for (var k = 0; k < parts.length; k++) if (parts[k] === c) return true;
    return false;
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
        _hrgEnsureStyles();
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
        this._table.className = "hrg-table";
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
        this._wrap.className = "hrg-wrap";
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
        _hrgAddClass(this._table, _hrgOverflowClass(opts.overflow));
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
            this._colgroup.appendChild(slots.createElement("col-" + h, "col"));   // widths need cols regardless
            if (!this._headerRow) continue;
            var th = slots.createElement("th-" + h, "th");
            th.className = "hrg-th";
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
                td.className = "hrg-td";
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
        if (any) _hrgAddClass(this._table, "hrg-fixed");
        else _hrgRemoveClass(this._table, "hrg-fixed");
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
                ts.setProperty("width", "max(100%, " + sum + "px)");
                var lastSt = cols[cols.length - 1].style;
                if (lastSt && lastSt.removeProperty) lastSt.removeProperty("--hrg-col-w");
            }
            else if (ts.removeProperty) ts.removeProperty("width");
        }
        this.placeGroups();                                   // the slots moved; the hosts follow
        return this;
    }

    /** Paint the cursor on one slot ({ i, j }) or on none (null). A diff, not a sweep. */
    paintCursor(ij) {
        var td = ij ? this.slotAt(ij.i, ij.j) : null;
        if (this._cursorTd !== td) {
            if (this._cursorTd) _hrgRemoveClass(this._cursorTd, "hrg-cursor");
            if (td) _hrgAddClass(td, "hrg-cursor");
            this._cursorTd = td;
        }
        // The merged cells mirror: a group wears the cursor when it is on any of its slots.
        for (var g = 0; g < this._groups.length; g++) {
            var grp = this._groups[g];
            var on = !!ij && ij.i === grp.i && ij.j >= grp.j && ij.j < grp.j + grp.n;
            if (on) _hrgAddClass(grp.el, "hrg-cursor"); else _hrgRemoveClass(grp.el, "hrg-cursor");
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
        // The merged cells mirror: a group is selected when any of its slots is.
        for (var g = 0; g < this._groups.length; g++) {
            var grp = this._groups[g], on = false;
            for (var q = 0; q < list.length && !on; q++) {
                var b = list[q];
                on = b.i0 <= grp.i && grp.i <= b.i1 && b.j0 <= grp.j + grp.n - 1 && b.j1 >= grp.j;
            }
            if (on) _hrgAddClass(grp.el, "hrg-sel"); else _hrgRemoveClass(grp.el, "hrg-sel");
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
        _hrgAddClass(lead, "hrg-lead");
        if (lead.style && lead.style.setProperty) lead.style.setProperty("--hrg-span", String(n));
        for (var k = 1; k < n; k++) {
            var td = this.slotAt(i, j + k);
            if (!td) break;
            _hrgAddClass(td, "hrg-covered");
            if (k === n - 1) _hrgAddClass(td, "hrg-group-end");
        }
        if (!this._mergedBranch) {
            this._mergedBranch = this._branch.createBranch("merged");
            this._mergedBranch.activate(this);
        }
        var el = this._mergedBranch.createElement("merged-" + this._groups.length, "div");
        el.className = "hrg-merge";
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
            var grp = this._groups[g], r = this._unionRect(grp.i, grp.j, grp.n), st = grp.el.style;
            if (!r || !st || !st.setProperty) continue;
            st.setProperty("left",   r.left + "px");
            st.setProperty("top",    r.top + "px");
            st.setProperty("width",  r.width + "px");
            st.setProperty("height", r.height + "px");
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
        el.className = "hrg-edit";
        var st = el.style;
        if (st && st.setProperty) {
            st.setProperty("left", r.left + "px");
            st.setProperty("top", r.top + "px");
            // EXACTLY the slot, so the overlay is an anchor rather than a thing
            // with a size of its own. A cell that wants more room hangs it off
            // this box as its own positioned child — which keeps the geometry
            // the grid states exact, and leaves the cell free.
            st.setProperty("width", r.width + "px");
            st.setProperty("height", r.height + "px");
        }
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
        el.className = "hrg-mask";
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
        el.className = "hrg-panel";
        el.setAttribute("tabindex", "-1");
        var st = el.style;
        if (st && st.setProperty) {
            st.setProperty("left",   (seen.left + box.left) + "px");
            st.setProperty("top",    (seen.top + box.top) + "px");
            st.setProperty("width",  box.width + "px");
            st.setProperty("height", box.height + "px");
        }
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
        if (on) { _hrgAddClass(this._table, "hrg-deep"); _hrgAddClass(this._wrap, "hrg-deep"); }
        else    { _hrgRemoveClass(this._table, "hrg-deep"); _hrgRemoveClass(this._wrap, "hrg-deep"); }
        return this;
    }

    /** And the masked state, for the same reason. */
    setMasked(on) {
        if (on) _hrgAddClass(this._table, "hrg-masked"); else _hrgRemoveClass(this._table, "hrg-masked");
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
