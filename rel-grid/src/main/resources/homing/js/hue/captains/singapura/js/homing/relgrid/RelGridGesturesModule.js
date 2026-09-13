// =============================================================================
// RelGridGesturesModule — RFC 0050 · Episode 2's GESTURES: what a click, a
// press-drag and a key MEAN, and the SELECTION they route. The layout reports
// raw facts by position; here they become intents on the cursor and the
// selection, and the verbs a key stands for are the grid's own.
//
// THE SELECTION IS ASKED, NEVER OBEYED (map 5): a RelGridSelection — an
// ordered list of position rectangles that knows nothing but positions — and
// three things are done with it. The gestures are ROUTED: shift extends the
// last range's far corner, ctrl adds a 1x1 and moves the cursor, Ctrl+A takes
// the whole presented space, a BARE move clears the list (law 40), and a
// PRESS-DRAG is whichever of those the press meant followed by an extension
// per slot the pointer reaches. The cursor stays with the cursor: it is
// identity-first, it tells its cell and it paints, and none of that is the
// selection's business. The two meet in exactly two lines — a bare move
// clears, and an empty list resolves to the cursor's own 1x1.
//
// A drag is only ever a click that kept going: the press action — what a
// click WOULD have done — is performed early, when the pointer first travels,
// and the click the browser fires after a drag is swallowed so it cannot undo
// what the drag just made. Everything is INERT while the grid is locked: the
// keyboard is the cell's, or an answer is owed.
//
// Keys: arrows move (a bare arrow at an edge goes nowhere and is REPORTED, so
// a host stacking tables may step over); Shift+arrow extends; Ctrl+A selects
// all; Ctrl+C asks the copy; Alt+Left/Right resizes the cursor's column;
// Alt+Enter hands the rows' arrangement over; Enter offers the cell control.
// A key is consumed only when it did something.
//
// THE WINDOW. A bare arrow at the top or bottom edge first asks the grid to
// move the window one row — the relation answers a View, or nothing. A View
// moves the rows under the cursor, whose cell is still its cell, one row in
// from the edge; the arrow then steps onto the new row. Nothing is the edge,
// reported as before. PageUp/PageDown ask for the window's height of rows;
// the wheel asks for the rows its delta is worth, the remainder carried to
// the next tick. Whatever the relation answers nothing to is not the grid's:
// the key bubbles, the wheel scrolls what the browser will — a static table
// in a scrollport scrolls as it always did.
//
//   new RelGridGestures({ grid, maps, selection, cursor, locked, afterSelection, stepJ, stepWidth, rowHeight, onEdge? })
//   onDown / onDragTo / onDragEnd / onClick / onDblClick / onKey / onWheel    the layout's reports
//   bareMove(fn) / extendTo(i, j) / press(i, j, mods)              the routes, for the surface's twins
// =============================================================================

class RelGridGestures {

    constructor(opts) {
        this._grid = opts.grid;                   // the verbs a key stands for
        this._maps = opts.maps;
        this._selection = opts.selection;
        this._cursor = opts.cursor;
        this._locked = opts.locked;               // () → boolean
        this._afterSelection = opts.afterSelection;   // () — paint, and tell the domain
        this._stepJ = opts.stepJ;                 // (i, j, dj): one horizontal step, over a merged cell entirely
        this._stepWidth = opts.stepWidth;         // (column, dir): one Alt+arrow of the cursor's column
        this._rowHeight = opts.rowHeight;         // () → px: one row, for a wheel that speaks pixels
        this._onEdge = opts.onEdge || null;
        this._wheelRest = 0;                      // the part of a row the wheel has not yet moved
        this._scrolls = false;                    // has the relation ever moved the window? then the page holds still under the wheel
        this._pressAt = null;                     // the slot a button went down on, with its modifiers
        this._dragged = false;
        this._swallowClick = false;
    }

    // ── the routes ─────────────────────────────────────────────────────────

    /** A bare move: the gesture means START OVER, so the list goes (law 40). */
    bareMove(fn) {
        this._selection.clear();
        fn();
        this._afterSelection();
    }

    /**
     * Extension to a position. The cursor DOES NOT MOVE (law 39) — the anchor
     * is where the cursor already is, and what travels is the last range's far
     * corner.
     */
    extendTo(i, j) {
        var pos = this._cursor.pos();
        if (!pos) return false;
        this._selection.extend({ i: this._cursor.clampI(i), j: this._cursor.clampJ(j) }, pos);
        this._afterSelection();
        return true;
    }

    /** Shift+arrow: step the corner extension moves, from the cursor on the first one. */
    _extendBy(key) {
        if (!this._cursor.pos()) return;
        var from = this._selection.far() || this._cursor.pos();
        var di = (key === "ArrowUp") ? -1 : (key === "ArrowDown") ? 1 : 0;
        var dj = (key === "ArrowLeft") ? -1 : (key === "ArrowRight") ? 1 : 0;
        this.extendTo(from.i + di, dj ? this._stepJ(from.i, from.j, dj) : from.j);
        this._cursor.follow(this._selection.far());          // the range's far corner is where the person went
    }

    /**
     * The press action — what a click WOULD have done, performed early because
     * the pointer has started to travel. The three meanings are the click's
     * three, so a drag is only ever a click that kept going.
     */
    press(i, j, mods) {
        var self = this;
        if (mods.shift) return;                // the range extends from where it already is
        if (mods.ctrl) {
            this._selection.add({ i: i, j: j });
            this._cursor.set(i, j);
            this._afterSelection();
            return;
        }
        this.bareMove(function () { self._cursor.set(i, j); });
    }

    // ── the pointer ────────────────────────────────────────────────────────

    /** A button went down on a slot. Nothing happens yet — a press that never
     *  travels is a click, and the click handler owns it. */
    onDown(i, j, mods) {
        if (this._locked()) return;
        this._swallowClick = false;            // a fresh gesture; whatever the last one left, drop it
        this._pressAt = { i: i, j: j, mods: mods || {} };
        this._dragged = false;
    }

    /** The pointer reached another slot while held. The FIRST such report turns the press into a drag. */
    onDragTo(i, j) {
        if (this._locked() || !this._pressAt) return;
        if (!this._dragged) {
            this._dragged = true;
            this.press(this._pressAt.i, this._pressAt.j, this._pressAt.mods);
        }
        this.extendTo(i, j);
    }

    /** Released. The click that follows a drag must not undo it: swallowed, once. */
    onDragEnd() {
        if (this._dragged) this._swallowClick = true;
        this._pressAt = null;
        this._dragged = false;
    }

    onClick(i, j, mods) {
        if (this._swallowClick) { this._swallowClick = false; return; }
        if (this._locked()) return;            // the cell has the pointer and the keyboard, or an answer is owed
        mods = mods || {};
        // A shift-click is an extension TO where it landed; the other two mean
        // at the slot itself, which is exactly the press action.
        if (mods.shift) this.extendTo(i, j);
        else            this.press(i, j, mods);
    }

    onDblClick(i, j) {
        if (this._locked()) return;
        var self = this;
        // A bare move like any other. In a browser the click that precedes has
        // already cleared, but the double-click must not depend on that.
        this.bareMove(function () { self._cursor.set(i, j); });
        this._grid.takeControlAtCursor();
    }

    // ── the keys ───────────────────────────────────────────────────────────

    onKey(e) {
        if (this._locked()) return;            // the keyboard is the cell's, or an answer is owed
        var key = e.key, self = this, grid = this._grid;
        // Alt+Left/Right: the pointer-free resize of the CURSOR's column. The cursor does not move.
        if (e.altKey && (key === "ArrowLeft" || key === "ArrowRight")) {
            var id = this._cursor.id();
            if (id) this._stepWidth(id.column, key === "ArrowRight" ? 1 : -1);
            if (e.preventDefault) e.preventDefault();
            return;
        }
        // Alt+Enter: hand the rows' arrangement to the domain — the table's own
        // road to the verb. Consumed only when it was actually asked, as Ctrl+C is.
        if (e.altKey && key === "Enter") {
            if (!grid.handoverView()) return;
            if (e.preventDefault) e.preventDefault();
            return;
        }
        var arrow = (key === "ArrowUp" || key === "ArrowDown" || key === "ArrowLeft" || key === "ArrowRight");
        // Ctrl+A: the whole presented space, in one range. The cursor stays.
        if ((e.ctrlKey || e.metaKey) && (key === "a" || key === "A")) {
            this._selection.all(this._maps.rows(), this._maps.cols());
            this._afterSelection();
        }
        // Ctrl+C: the question. Consumed only when it was actually asked, so a
        // grid with no channel leaves the browser's own copy alone.
        else if ((e.ctrlKey || e.metaKey) && (key === "c" || key === "C")) {
            if (!grid.copy()) return;
        }
        // Shift+arrow: extension. The cursor stays here too (law 39).
        else if (e.shiftKey && arrow) this._extendBy(key);
        else if (arrow) {
            // A bare arrow with the cursor already at that edge: up or down, the
            // relation is asked to move the window one row, and a View back moves
            // the rows under the cursor — one row in from the edge now — so the
            // arrow steps onto the new row. Nothing back, or a side edge, goes
            // nowhere in this table — and is REPORTED, so a host that stacks
            // tables may step over the edge. The key is still the grid's: consumed.
            var edge = this._cursor.edgeOf(key);
            var di = (key === "ArrowDown") ? 1 : (key === "ArrowUp") ? -1 : 0;
            if (edge && di && grid.scrollRows(di)) {
                this.bareMove(function () { self._cursor.move(di, 0); });
            } else if (edge) {
                this.bareMove(function () {});      // a bare move still clears the ranges (law 39)
                if (this._onEdge) {
                    try { this._onEdge(edge); }
                    catch (err) { console.error("[RelGrid] onEdge threw:", err); }
                }
            } else {
                this.bareMove(function () {
                    if      (key === "ArrowUp")    self._cursor.move(-1, 0);
                    else if (key === "ArrowDown")  self._cursor.move(1, 0);
                    else if (key === "ArrowLeft")  self._cursor.move(0, -1);
                    else                           self._cursor.move(0, 1);
                });
            }
        }
        else if (key === "Enter")      grid.takeControlAtCursor();
        // PageUp/PageDown: the window's height of rows. Consumed only when the
        // relation moved the window; a static table leaves the key to the browser.
        else if (key === "PageDown" || key === "PageUp") {
            if (!grid.scrollRows(key === "PageDown" ? this._maps.rows() : -this._maps.rows())) return;
        }
        else return;                           // not ours; let it bubble
        if (e.preventDefault) e.preventDefault();
    }

    /**
     * The wheel: its delta as whole rows — lines as they are, pixels against a
     * row's height, pages as the window — and the grid asked to move by them.
     * Consumed when the window moved; left to the browser when the relation
     * answered nothing. A part-row remainder is carried to the next tick, and
     * once the window has moved under the wheel the page holds still for it.
     */
    onWheel(e) {
        if (this._locked()) return;            // inert; the browser scrolls what it will
        var dy = Number(e.deltaY) || 0, rows;
        if (e.deltaMode === 1)      rows = dy;
        else if (e.deltaMode === 2) rows = dy * this._maps.rows();
        else                        rows = dy / this._rowHeight();
        if (rows * this._wheelRest < 0) this._wheelRest = 0;   // a change of direction owes nothing to the last one
        this._wheelRest += rows;
        var whole = Math.trunc(this._wheelRest);
        this._wheelRest -= whole;
        if (!whole) {
            if (this._scrolls && e.preventDefault) e.preventDefault();
            return;
        }
        if (!this._grid.scrollRows(whole)) return;
        this._scrolls = true;
        if (e.preventDefault) e.preventDefault();
    }
}
