// =============================================================================
// RelGridSelectionModule — RFC 0050 · Episode 2, map 5. THE SELECTION, and
// nothing else.
//
//   A selection is an ORDERED LIST OF RANGES. A range is a rectangle of
//   POSITIONS owning both its corners; the smallest is 1x1. The list is in
//   creation order, and it is never merged, deduplicated or reordered.
//
// This module imports NOTHING, and that is the point of it being a module.
// It holds no view map, no layout, no cell, no relation and no cursor. The two
// operations that need something outside the list are handed it:
//
//   · resolve/covers take the CURSOR'S POSITION, because an empty list means
//     the cursor's implicit 1x1 (law 40) — the meaning of the selection, not
//     the cursor itself.
//   · all() takes the EXTENTS, because select-all is a rectangle built from a
//     size the caller knows and this module never learns.
//
// It does not clamp. Whoever knows the presented space clamps before asking.
// It does not paint, and it does not tell a cell: the cursor's cell is told a
// mode, selection is a predicate the theme paints (law 217).
//
// It never initiates. It manages the list and answers what is selected when
// something else is triggered (law 216).
//
//   new RelGridSelection()
//     extend(to, anchor)   the shift gesture — law 41: an empty list anchors a
//                          new range AT THE ANCHOR; otherwise the LAST range's
//                          far corner moves. There is no focus: the far corner
//                          IS the thing extension moves, and a range owns it.
//     add(at)              the ctrl gesture — append a 1x1. (The caller moves
//                          the cursor; that is not this module's business.)
//     all(rows, cols)      replace the list with one range over the extents.
//     clear()              law 40's bare move, and law 43's view change.
//
//     ranges()             the RAW list, as rectangles, in creation order,
//                          overlap and duplicates intact.
//     resolve(cursorAt)    the list's MEANING: ranges(), or the cursor's 1x1
//                          when the list is empty, so no reader needs a case
//                          for "nothing selected".
//     covers(i, j, cursorAt)   is this position in the resolved selection?
//     isEmpty()  count()   about the raw list.
//
// A range is held as { anchor, far } and reported as { i0, j0, i1, j1 } with
// i0 <= i1 and j0 <= j1. The held form keeps the anchor distinct, because
// extension moves the far corner and not the near one; the reported form is
// normalised, because a reader wants a rectangle. Nothing is stored
// normalised, so a range dragged up and left still extends downward from where
// it started.
// =============================================================================

function _hrgSelInt(n) {
    return typeof n === "number" && isFinite(n) && Math.floor(n) === n;
}

/** A position, copied and checked. Malformed input fails HERE, at the call site. */
function _hrgSelPos(p, what) {
    if (!p || !_hrgSelInt(p.i) || !_hrgSelInt(p.j) || p.i < 0 || p.j < 0)
        throw new Error("[RelGridSelection] " + what + " must be { i, j } of non-negative integers");
    return { i: p.i, j: p.j };
}

/** The reported form: a normalised rectangle owning both corners. */
function _hrgSelRect(r) {
    return {
        i0: Math.min(r.anchor.i, r.far.i), j0: Math.min(r.anchor.j, r.far.j),
        i1: Math.max(r.anchor.i, r.far.i), j1: Math.max(r.anchor.j, r.far.j)
    };
}

class RelGridSelection {

    constructor() {
        this._ranges = [];        // [{ anchor: {i,j}, far: {i,j} }], creation order
    }

    // ── the gestures, as operations on the list ────────────────────────────

    /**
     * Shift. An empty list anchors a new range at the anchor — the cursor's
     * position, which the caller supplies because this module does not hold a
     * cursor. Otherwise the LAST range's far corner moves, and only that.
     */
    extend(to, anchor) {
        var t = _hrgSelPos(to, "to");
        if (this._ranges.length === 0) {
            // Not "the cursor's position": this module does not know what the anchor is,
            // and the scan that says so is the reason the message says so too.
            if (!anchor) throw new Error(
                "[RelGridSelection] extend on an empty selection needs an anchor position");
            this._ranges.push({ anchor: _hrgSelPos(anchor, "anchor"), far: t });
        } else {
            this._ranges[this._ranges.length - 1].far = t;
        }
        return this;
    }

    /** Ctrl. Append a 1x1 range. Overlap with what is already there is allowed and kept. */
    add(at) {
        var p = _hrgSelPos(at, "at");
        this._ranges.push({ anchor: p, far: { i: p.i, j: p.j } });
        return this;
    }

    /** Select-all. One range over the extents the caller knows; an empty space selects nothing. */
    all(rows, cols) {
        if (!_hrgSelInt(rows) || !_hrgSelInt(cols) || rows < 0 || cols < 0)
            throw new Error("[RelGridSelection] all(rows, cols) needs non-negative integer extents");
        this._ranges = (rows === 0 || cols === 0)
            ? []
            : [{ anchor: { i: 0, j: 0 }, far: { i: rows - 1, j: cols - 1 } }];
        return this;
    }

    /** A bare cursor move (law 40), and a change of the presented space (law 43). */
    clear() {
        this._ranges = [];
        return this;
    }

    // ── what it is asked ───────────────────────────────────────────────────

    isEmpty() { return this._ranges.length === 0; }

    count() { return this._ranges.length; }

    /**
     * The corner extension moves — the last range's far corner, copied, or
     * null when the list is empty. Reading it is how a caller steps it by one
     * position; it is not a second distinguished locus, because it is the
     * range's own corner and a range owns its corners (law 41).
     */
    far() {
        if (this._ranges.length === 0) return null;
        var f = this._ranges[this._ranges.length - 1].far;
        return { i: f.i, j: f.j };
    }

    /**
     * The raw list as rectangles, in creation order. Never merged, never
     * deduplicated, never reordered, and no bounding box is ever taken — the
     * grid imposes no shape rule (law 44), so an irregular selection reaches
     * whoever asked exactly as it was made.
     */
    ranges() {
        var out = [];
        for (var k = 0; k < this._ranges.length; k++) out.push(_hrgSelRect(this._ranges[k]));
        return out;
    }

    /**
     * The list's MEANING (law 40): the ranges, or — when the list is empty —
     * the cursor's implicit 1x1, so every reader sees a non-empty selection
     * whenever there is a cursor and none needs a special case. With an empty
     * list and no cursor there is nothing selected, and the answer is empty.
     */
    resolve(cursorAt) {
        if (this._ranges.length > 0) return this.ranges();
        if (cursorAt == null) return [];
        var c = _hrgSelPos(cursorAt, "cursorAt");
        return [{ i0: c.i, j0: c.j, i1: c.i, j1: c.j }];
    }

    /** Is this position within the resolved selection? For the paint, and for a reader's test. */
    covers(i, j, cursorAt) {
        if (!_hrgSelInt(i) || !_hrgSelInt(j)) return false;
        var list = this.resolve(cursorAt);
        for (var k = 0; k < list.length; k++) {
            var r = list[k];
            if (i >= r.i0 && i <= r.i1 && j >= r.j0 && j <= r.j1) return true;
        }
        return false;
    }
}
