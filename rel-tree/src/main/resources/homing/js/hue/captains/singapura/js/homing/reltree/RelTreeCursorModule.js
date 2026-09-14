// =============================================================================
// RelTreeCursorModule — RFC 0050 · Episode 3-ext1's CURSOR: the current node,
// an IDENTITY the tree holds, with the position it was last seen at as the
// fallback when the identity leaves the presented places. Identity-first:
// a View that moves the rows keeps the cursor on its node; only when the
// node is no longer presented does the position stand in, clamped to what
// is — or, across a fold the tree itself asked for, the node that folded
// (the facade says which, through landOn).
//
// The cursor tells its cell — 'none' | 'shallow', the grid's words, and
// never 'deep': a tree cell is not entered — paints itself on the row
// through the layout, reports a move to the host, and follows itself into
// view when the tree has the keyboard. It knows nothing of what a key
// means, which is the gestures'.
//
//   new RelTreeCursor({ places, layout, cells, onMoved? })
//   resolve()            after a presentation: the identity if presented, else the position, clamped
//   set(i)               a move by position; false when nothing changed
//   move(di)             a step, clamped; then follow
//   landOn(key)          the row of a key, when a re-ask took the cursor's own away — the folder
//   follow(i, regardless)   the viewport follows — when the tree has the keyboard, or regardless
//   tell(key, mode)      a cell told its mode
//   key() / pos() / clamp(i)
// =============================================================================

class RelTreeCursor {

    constructor(opts) {
        this._places = opts.places;
        this._layout = opts.layout;
        this._cells = opts.cells;
        this._onMoved = opts.onMoved || null;     // (key)
        this._key = null;                         // the current node, or null
        this._pos = -1;                           // its row, or -1
    }

    key() { return this._key; }
    pos() { return this._pos; }
    clamp(i) { return Math.max(0, Math.min(this._places.rows() - 1, i)); }

    /** Tell a cell its selection mode — pure lifecycle; the cell may ignore it. */
    tell(key, mode) {
        if (key === null || key === undefined) return;
        var entry = this._cells.get(key);
        if (entry && typeof entry.cell.onSelect === "function") {
            try { entry.cell.onSelect(mode); }
            catch (e) { console.error("[RelTree] cell.onSelect threw:", e); }
        }
    }

    /** After a presentation: the identity survived, or the position stands in. */
    resolve() {
        var places = this._places;
        if (places.rows() === 0) {                               // nothing presented: no cursor
            this.tell(this._key, "none");
            this._key = null; this._pos = -1;
            this._layout.paintCursor(null);
            return;
        }
        if (this._key === null) { this._layout.paintCursor(null); return; }      // none yet: none still
        var at = places.indexOf(this._key);
        if (at >= 0) {                                           // the identity survived
            this._pos = at;
        } else {                                                 // the position stands in
            var i = this.clamp(this._pos < 0 ? 0 : this._pos);
            this.tell(this._key, "none");
            this._key = places.keyAt(i);
            this._pos = i;
            this.tell(this._key, "shallow");
        }
        this._layout.paintCursor(this._pos);
    }

    set(i) {
        var key = this._places.keyAt(i);
        if (key === null) return false;
        if (this._key === key) { this._pos = i; return false; }
        this.tell(this._key, "none");
        this._key = key;
        this._pos = i;
        this.tell(key, "shallow");
        this._layout.paintCursor(i);
        if (this._onMoved) {
            try { this._onMoved(key); }
            catch (e) { console.error("[RelTree] onCursorMoved threw:", e); }
        }
        return true;
    }

    /** A step, clamped; then the viewport follows. */
    move(di) {
        if (this._pos < 0) return false;
        var moved = this.set(this.clamp(this._pos + di));
        this.follow(this._pos);
        return moved;
    }

    /** The cursor's own row went with a fold the tree asked for: land on the node that folded. */
    landOn(key) {
        var i = this._places.indexOf(key);
        if (i < 0) return false;
        this.set(i);
        this.follow(i);
        return true;
    }

    /** The viewport follows a row the person went to — when the tree has the keyboard. */
    follow(i, regardless) {
        if (i < 0) return;
        if (regardless || this._layout.hasKeyboard()) this._layout.revealRow(i);
    }
}
