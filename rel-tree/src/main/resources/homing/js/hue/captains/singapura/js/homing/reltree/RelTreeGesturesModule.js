// =============================================================================
// RelTreeGesturesModule — RFC 0050 · Episode 3-ext1's GESTURES: what a key and
// a press MEAN on a tree. The layout captures by position and the cursor
// moves by position; this is where a position, a place and a key become an
// intent — the cursor's, or a question for the relation. Every intent is
// refused while the tree is locked (law 221): a question is pending and the
// mask holds the keys, and a late one that reaches here is dropped.
//
// In the tree the tree owns the arrows:
//   ↑ ↓          the row before, the row after; at the edge, reported to the host and consumed
//   →            closed: asks unfold · open: to the first child · leaf: nothing
//   ←            open: asks fold · closed or leaf: to the parent · at the root level: nothing
//   Home End     the first row, the last
//   PgUp PgDn    a page of rows
//   Enter        the node is activated — a notification, and the host's callback
//   Space        closed: asks unfold · open: asks fold · leaf: nothing
// A press lands the cursor; a press on the caret lands it and asks the fold;
// a double-click lands it and activates.
//
//   new RelTreeGestures({ tree, places, cursor, locked, pageRows, onEdge? })
//   onKey(e) / onRowClick(i) / onRowDblClick(i) / onCaretClick(i)
// =============================================================================

class RelTreeGestures {

    constructor(opts) {
        this._tree = opts.tree;                   // unfold(key), fold(key), activate(key) — the facade's verbs
        this._places = opts.places;
        this._cursor = opts.cursor;
        this._locked = opts.locked;               // () → boolean
        this._pageRows = opts.pageRows;           // () → rows in a page
        this._onEdge = opts.onEdge || null;       // (direction)
    }

    _edge(direction) {
        if (!this._onEdge) return;
        try { this._onEdge(direction); }
        catch (e) { console.error("[RelTree] onEdge threw:", e); }
    }

    /** The fold a place would take next: the question to ask, or none for a leaf. */
    _toggle(i) {
        var fold = this._places.foldAt(i), key = this._places.keyAt(i);
        if (fold === "closed") return this._tree.unfold(key);
        if (fold === "open")   return this._tree.fold(key);
        return false;
    }

    onKey(e) {
        if (this._locked() || this._places.rows() === 0) return;
        var c = this._cursor, i = c.pos(), last = this._places.rows() - 1, handled = true;
        switch (e.key) {
            case "ArrowDown":
                if (i < 0) c.set(0);
                else if (i === last) this._edge("down");
                else c.move(1);
                break;
            case "ArrowUp":
                if (i < 0) c.set(0);
                else if (i === 0) this._edge("up");
                else c.move(-1);
                break;
            case "Home": c.set(0); c.follow(0); break;
            case "End":  c.set(last); c.follow(last); break;
            case "PageDown": if (i < 0) c.set(0); else c.move(Math.max(1, this._pageRows())); break;
            case "PageUp":   if (i < 0) c.set(0); else c.move(-Math.max(1, this._pageRows())); break;
            case "ArrowRight":
                if (i < 0) break;
                if (this._places.foldAt(i) === "closed") this._tree.unfold(this._places.keyAt(i));
                else if (this._places.foldAt(i) === "open") { var child = this._places.firstChildOf(i); if (child >= 0) { c.set(child); c.follow(child); } }
                break;
            case "ArrowLeft":
                if (i < 0) break;
                if (this._places.foldAt(i) === "open") this._tree.fold(this._places.keyAt(i));
                else { var parent = this._places.parentOf(i); if (parent >= 0) { c.set(parent); c.follow(parent); } }
                break;
            case "Enter":
                if (i >= 0) this._tree.activate(this._places.keyAt(i));
                break;
            case " ":
                if (i >= 0) this._toggle(i);
                break;
            default: handled = false;
        }
        if (handled && e.preventDefault) e.preventDefault();
    }

    /** A press lands the cursor. */
    onRowClick(i) {
        if (this._locked()) return;
        this._cursor.set(i);
    }

    /** A double-click lands the cursor and activates. */
    onRowDblClick(i) {
        if (this._locked()) return;
        this._cursor.set(i);
        this._tree.activate(this._places.keyAt(i));
    }

    /** A press on the caret lands the cursor and asks the fold the place would take next. */
    onCaretClick(i) {
        if (this._locked()) return;
        this._cursor.set(i);
        this._toggle(i);
    }
}
