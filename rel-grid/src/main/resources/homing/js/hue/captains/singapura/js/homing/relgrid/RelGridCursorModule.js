// =============================================================================
// RelGridCursorModule — RFC 0050 · Episode 2's CURSOR: an IDENTITY the grid
// holds, with the position it was last seen at as the fallback when the
// identity leaves the presented space. Identity-first: an arrangement moves
// the rows and the cursor stays on its cell; only when the cell is no longer
// presented does the position stand in, clamped to what is.
//
// The cursor tells its cell — 'none' | 'shallow' | 'deep', pure lifecycle —
// paints itself on the slot through the layout, reports a move to the host,
// and follows itself into view when the grid has the keyboard. It knows
// nothing of the selection, which is next door, and nothing of what a key
// means, which is the gestures'.
//
//   new RelGridCursor({ maps, layout, cells, stepJ, onMoved? })
//   resolve(deep)        after an arrangement: the identity if presented, else the position, clamped
//   set(i, j)            a shallow move by position; false when nothing changed
//   move(di, dj)         one step, clamped, horizontal steps over a merged cell; then follow
//   edgeOf(key)          the edge a bare arrow would cross, or null
//   follow(pos, regardless)   the viewport follows — when the grid has the keyboard, or regardless
//   tell(id, mode)       a cell told its mode
//   id() / pos() / clampI(i) / clampJ(j)
// =============================================================================

class RelGridCursor {

    constructor(opts) {
        this._maps = opts.maps;
        this._layout = opts.layout;
        this._cells = opts.cells;
        this._stepJ = opts.stepJ;                 // (i, j, dj) → j': one horizontal step, over a merged cell entirely
        this._onMoved = opts.onMoved || null;     // (pk, column)
        this._cursor = null;                      // { pk, column } | null
        this._pos = null;                         // { i, j } | null
    }

    id()  { return this._cursor; }
    pos() { return this._pos; }
    clampI(i) { return Math.max(0, Math.min(this._maps.rows() - 1, i)); }
    clampJ(j) { return Math.max(0, Math.min(this._maps.cols() - 1, j)); }

    /** Tell a cell its selection mode — pure lifecycle; the cell may ignore it. */
    tell(id, mode) {
        if (!id) return;
        var entry = this._cells.get(id.pk, id.column);
        if (entry && typeof entry.cell.onSelect === "function") {
            try { entry.cell.onSelect(mode); }
            catch (e) { console.error("[RelGrid] cell.onSelect threw:", e); }
        }
    }

    /** After an arrangement: the identity survived, or the position stands in. */
    resolve(deep) {
        var maps = this._maps;
        if (maps.rows() === 0 || maps.cols() === 0) {          // an empty presented space: no cursor
            this.tell(this._cursor, "none");
            this._cursor = null; this._pos = null;
            this._layout.paintCursor(null);
            return;
        }
        var at = this._cursor ? maps.locate(this._cursor.pk, this._cursor.column) : null;
        if (at) {                                                // the identity survived
            this._pos = at;
        } else {                                                 // fall back to the position
            var p = this._pos || { i: 0, j: 0 };
            var i = Math.min(p.i, maps.rows() - 1), j = Math.min(p.j, maps.cols() - 1);
            this.tell(this._cursor, "none");
            this._cursor = maps.resolve(i, j);
            this._pos = { i: i, j: j };
            this.tell(this._cursor, deep ? "deep" : "shallow");
        }
        this._layout.paintCursor(this._pos);
    }

    set(i, j) {
        var id = this._maps.resolve(i, j);
        if (!id) return false;
        if (this._cursor && this._cursor.pk === id.pk && this._cursor.column === id.column) return false;
        this.tell(this._cursor, "none");
        this._cursor = id;
        this._pos = { i: i, j: j };
        this.tell(id, "shallow");
        this._layout.paintCursor(this._pos);
        if (this._onMoved) {
            try { this._onMoved(id.pk, id.column); }
            catch (e) { console.error("[RelGrid] onCursorMoved threw:", e); }
        }
        return true;
    }

    /** The edge a bare arrow would cross, or null when there is room to move. */
    edgeOf(key) {
        var p = this._pos;
        if (!p) return null;
        if (key === "ArrowUp"    && p.i === 0) return "up";
        if (key === "ArrowDown"  && p.i === this._maps.rows() - 1) return "down";
        if (key === "ArrowLeft"  && p.j === 0) return "left";
        if (key === "ArrowRight" && p.j === this._maps.cols() - 1) return "right";
        return null;
    }

    move(di, dj) {
        if (!this._pos) return;
        var p = this._pos;
        this.set(this.clampI(p.i + di), this.clampJ(dj ? this._stepJ(p.i, p.j, dj) : p.j));
        this.follow(this._pos);
    }

    /** The viewport follows a position the person went to — when the grid has the keyboard. */
    follow(pos, regardless) {
        if (!pos) return;
        if (regardless || this._layout.hasKeyboard()) this._layout.revealSlot(pos.i, pos.j);
    }
}
