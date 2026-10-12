// =============================================================================
// RelGridMarksModule — RFC 0050 · Episode 2's MARKS: where the user is and what
// they have chosen, painted on the slots at the facade's direction, and nothing
// else. The cursor's slot wears the cursor's fact and the frame's leaf for where
// the keys are - elsewhere, with the grid, lent to its cell - and wears another
// as they move; a selected slot wears the selection's fact and aria-selected,
// which the design's selection fill shows by. The cursor's row's number is
// current, the numbers of rows selected whole selected; a merged cell's host
// mirrors both, so the group reads as one cell. The marks decide nothing: they
// paint the facade's answers, and never learn a class of the design's.
//
// WHERE THE KEYS ARE, as the grid knows it: with the grid while the focus is
// anywhere in its wrapper - the table, an editor's anchor, the mask.
//
//   new RelGridMarks({ design, wrap, slotAt, gutterAt, cols, groups })
//   paintCursor(ij) / paintSelection(rects) / setDeep(on) / forget() / destroy()
// =============================================================================

class RelGridMarks {

    constructor(opts) {
        var self = this;
        this._design = opts.design;
        this._wrap = opts.wrap;
        this._slotAt = opts.slotAt;          // (i, j) -> td | null, the layout's
        this._gutterAt = opts.gutterAt;      // i -> the row's number | null
        this._cols = opts.cols;              // () -> the presented columns
        this._groups = opts.groups;          // () -> the merged cells' hosts
        this._keys = false;
        this._deep = false;
        this.forget();
        this._onFocusIn = function () { self._setKeys(true); };
        this._onFocusOut = function (e) {
            var to = e ? e.relatedTarget : null;
            self._setKeys(!!(to && typeof self._wrap.contains === "function" && self._wrap.contains(to)));
        };
        this._wrap.addEventListener("focusin", this._onFocusIn);
        this._wrap.addEventListener("focusout", this._onFocusOut);
    }

    /** A fresh matrix: the old paint went with the old slots. */
    forget() {
        this._cursorTd = null;      // the slot painted as the cursor
        this._cursorLeaf = null;    // the leaf it wears for where the keys are
        this._cursorGutter = null;  // the cursor's row's number, current
        this._selTds = [];          // the slots painted as selected
        this._selGutters = [];      // the numbers of the rows selected whole
    }

    /**
     * Paint the cursor on one slot ({ i, j }) or on none (null). A diff, not a sweep. The slot wears the
     * cursor's fact and the cursor's leaf for where the keys are; its row's number is current.
     */
    paintCursor(ij) {
        var td = ij ? this._slotAt(ij.i, ij.j) : null;
        if (this._cursorTd !== td) {
            if (this._cursorTd) { css.removeClass(this._cursorTd, hrg_cursor); this._shedCursor(); }
            if (td) css.addClass(td, hrg_cursor);
            this._cursorTd = td;
            this._wearCursor();
        }
        var number = ij ? this._gutterAt(ij.i) : null;
        if (this._cursorGutter !== number) {
            if (this._cursorGutter) this._cursorGutter.removeAttribute("aria-current");
            if (number) number.setAttribute("aria-current", "true");
            this._cursorGutter = number;
        }
        // The merged cells mirror: a group wears the cursor when it is on any of its slots.
        var groups = this._groups();
        for (var g = 0; g < groups.length; g++) {
            var grp = groups[g], on = !!ij && ij.i === grp.i && ij.j >= grp.j && ij.j < grp.j + grp.n;
            css.toggleClass(grp.el, hrg_cursor, on);
            if (on) grp.el.setAttribute("aria-current", "true"); else grp.el.removeAttribute("aria-current");
        }
    }

    /** The cursor's leaf for where the keys are: lent to its cell, with the grid, or elsewhere. */
    _cursorLeafNow() {
        return this._deep ? GRID_FRAME.LENT_CURSOR_BORDER : this._keys ? GRID_FRAME.HELD_CURSOR_BORDER : GRID_FRAME.CURSOR_BORDER;
    }

    _wearCursor() {
        if (!this._cursorTd) return;
        this._cursorLeaf = this._cursorLeafNow();
        this._design.wear(this._cursorLeaf, this._cursorTd);
    }

    _shedCursor() {
        if (this._cursorTd && this._cursorLeaf) this._design.shed(this._cursorLeaf, this._cursorTd);
        this._cursorLeaf = null;
    }

    /** The keys came or went: the cursor wears the leaf that says where they are now. */
    _setKeys(on) {
        if (this._keys === on) return;
        this._keys = on;
        this._reWearCursor();
    }

    _reWearCursor() {
        if (!this._cursorTd || this._cursorLeafNow() === this._cursorLeaf) return;
        this._shedCursor();
        this._wearCursor();
    }

    /**
     * Paint the selection: a list of rectangles in the presented space,
     * { i0, j0, i1, j1 } inclusive of both corners. The layout is told WHICH
     * SLOTS, never why — overlap between rectangles is nothing it has to
     * resolve, since a slot either wears the predicate or does not.
     */
    paintSelection(rects) {
        for (var k = 0; k < this._selTds.length; k++) {
            css.removeClass(this._selTds[k], hrg_sel);
            this._selTds[k].removeAttribute("aria-selected");
        }
        for (var q0 = 0; q0 < this._selGutters.length; q0++) this._selGutters[q0].removeAttribute("aria-selected");
        this._selTds = [];
        this._selGutters = [];
        var list = rects || [], cols = this._cols();
        for (var r = 0; r < list.length; r++) {
            var box = list[r];
            for (var i = box.i0; i <= box.i1; i++) {
                for (var j = box.j0; j <= box.j1; j++) {
                    var td = this._slotAt(i, j);
                    if (!td || css.hasClass(td, hrg_sel)) continue;      // already painted by an overlap
                    css.addClass(td, hrg_sel);
                    td.setAttribute("aria-selected", "true");
                    this._selTds.push(td);
                }
                // a row selected whole: its number is selected too
                var number = (box.j0 === 0 && box.j1 >= cols - 1) ? this._gutterAt(i) : null;
                if (number && number.getAttribute && number.getAttribute("aria-selected") !== "true") {
                    number.setAttribute("aria-selected", "true");
                    this._selGutters.push(number);
                }
            }
        }
        // The merged cells mirror: a group is selected when any of its slots is.
        var groups = this._groups();
        for (var g = 0; g < groups.length; g++) {
            var grp = groups[g], on = false;
            for (var q = 0; q < list.length && !on; q++) {
                var b = list[q];
                on = b.i0 <= grp.i && grp.i <= b.i1 && b.j0 <= grp.j + grp.n - 1 && b.j1 >= grp.j;
            }
            css.toggleClass(grp.el, hrg_sel, on);
            if (on) grp.el.setAttribute("aria-selected", "true"); else grp.el.removeAttribute("aria-selected");
        }
    }

    /** A cell holds control, or gave it back: the cursor wears the lent leaf meanwhile. */
    setDeep(on) {
        this._deep = !!on;
        this._reWearCursor();
    }

    destroy() {
        this._wrap.removeEventListener("focusin", this._onFocusIn);
        this._wrap.removeEventListener("focusout", this._onFocusOut);
        this.forget();
    }
}
