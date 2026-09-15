// =============================================================================
// RelTreeRowsModule — RFC 0050 · Episode 3-ext1's ROWS: the positions of ONE
// tree, minted on the tree's own branch. Positional only — row i is row i
// whatever key is in it — and nothing here sees an identity. A row is the
// tree's caret and then whatever the facade places after it: the domain's
// cell, which this never touches.
//
// ROWS BELONG TO A COUNT. A tree's shape changes on every fold, so the rows
// are KEPT while the count holds and GROWN OR SHRUNK AT THE TAIL when it
// changes — never dissolved and re-minted whole (ext8's law 246 read for a
// list). Each row has a sub-branch of its own so that a trailing row can be
// released alone; a cell riding in a released row is the facade's to place
// again or to detach, and it is still alive either way.
//
// THE CARET IS THE TREE'S TYPED SVG — RelTreeSvgs.caret, a chevron in
// currentColor — parsed per row into the caret span the row owns. The span is
// the party's; the markup inside it is an asset, not authored DOM, on the
// same footing as a cell's text. Its state is the span's class: open turns
// the chevron a quarter, a leaf hides it and keeps the box.
//
// It is the CAPTURE SURFACE for rows: a press, a double-click and a press on
// the caret are reported by position. Nothing here decides what a gesture
// MEANS.
//
//   new RelTreeRows({ branch, host, caret?, onRowClick?, onRowDblClick?, onCaretClick? })
//   caret: false leaves the caret out — a domain that draws its own in its cell
//   render(count)        the rows for a count: grown or shrunk at the tail; how many were minted, minus how many released
//   paint(i, place)      the row's depth and fold: the indent, the caret's state, ARIA
//   rowAt(i) / caretAt(i) / rows()
//   destroy()
// =============================================================================

/** The asset, parsed: a fresh SVG element for one row. */
function _hrtCaretSvg() {
    return new DOMParser().parseFromString(caret, "image/svg+xml").documentElement;
}

class RelTreeRows {

    constructor(opts) {
        this._branch = opts.branch;                 // the tree's own
        this._host = opts.host;                     // the element the rows go in
        this._caret = opts.caret !== false;
        this._onRowClick = opts.onRowClick || null;         // (i, ev)
        this._onRowDblClick = opts.onRowDblClick || null;   // (i)
        this._onCaretClick = opts.onCaretClick || null;     // (i)
        this._rowsBranch = this._branch.createBranch("rows");
        this._rowsBranch.activate(this);
        this._rows = [];                            // [i] → { branch, row, caret }
    }

    rows() { return this._rows.length; }
    rowAt(i)   { var r = this._rows[i]; return r ? r.row : null; }
    caretAt(i) { var r = this._rows[i]; return r ? r.caret : null; }

    /** Grow or shrink at the tail; the rows in between are the same rows. */
    render(count) {
        var delta = 0;
        while (this._rows.length > count) {
            var last = this._rows.pop();
            last.branch.dissolve();
            delta--;
        }
        while (this._rows.length < count) {
            this._rows.push(this._mint(this._rows.length));
            delta++;
        }
        return delta;
    }

    _mint(i) {
        var self = this;
        var rb = this._rowsBranch.createBranch("r" + i);
        rb.activate(this);
        var row = rb.createElement("row", "div");
        css.addClass(row, hrt_row);
        row.setAttribute("role", "treeitem");
        var caret = null;
        if (this._caret) {
            caret = rb.createElement("caret", "span");
            css.addClass(caret, hrt_caret);
            caret.appendChild(_hrtCaretSvg());
            caret.addEventListener("click", function (ev) {
                if (ev && ev.stopPropagation) ev.stopPropagation();   // the row's press is this press: reported once, as the caret's
                if (self._onCaretClick) self._onCaretClick(i);
            });
            row.appendChild(caret);
        }
        row.addEventListener("click", function (ev) { if (self._onRowClick) self._onRowClick(i, ev); });
        row.addEventListener("dblclick", function () { if (self._onRowDblClick) self._onRowDblClick(i); });
        this._host.appendChild(row);
        return { branch: rb, row: row, caret: caret };
    }

    /** What the place says, painted: the indent by depth, the caret by fold, and ARIA for both. */
    paint(i, place) {
        var r = this._rows[i];
        if (!r) return false;
        r.row.style.setProperty("--hrt-depth", String(place.depth));
        r.row.setAttribute("aria-level", String(place.depth + 1));
        if (place.fold === "leaf") r.row.removeAttribute("aria-expanded");
        else r.row.setAttribute("aria-expanded", place.fold === "open" ? "true" : "false");
        if (r.caret) {
            css.toggleClass(r.caret, hrt_caret_open, place.fold === "open");
            css.toggleClass(r.caret, hrt_caret_leaf, place.fold === "leaf");
        }
        return true;
    }

    destroy() {
        this._rowsBranch.dissolve();
        this._rows = [];
    }
}
