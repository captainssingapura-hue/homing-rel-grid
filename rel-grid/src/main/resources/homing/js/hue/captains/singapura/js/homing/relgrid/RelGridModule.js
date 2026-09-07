// =============================================================================
// RelGridModule — RFC 0050 · Episode 2's facade: composes the grid and
// is the ONLY place the two branches meet. Orchestration only — the seam is
// RelGridViewMaps, the chrome is RelGridLayout, the registry is RelGridCells; the facade
// reads the relation's identities and columns, threads the pieces, and
// re-places cells into freshly minted slots on every arrangement pass.
//
//   new RelGrid({
//       container,        // where the layout mounts
//       branch,           // the CELLS branch (DomOpsParty) — hands out cell hosts
//       relation,         // { pks(), columns(), cellFor(pk, column) } — and nothing else
//       label?,           // aria-label
//       header?,          // { show?, labels? } — display only
//       onArranged?,      // (kind) after every placement pass
//       onCursorMoved?,   // (pk, column)
//       onEditStarted?,   // (pk, column) — the grid went deep on this cell
//       onEditEnded?,     // (pk, column) — the cell handed control back
//       onColumnResized?  // (column, px) — a REPORT of what is now held; the grid keeps nothing
//   });
//
// THE ROOT PRINCIPLE, AS CODE: this file asks the relation for identities,
// columns and cells. It never asks for, holds, pushes or writes a value, and
// there is no method on it that could carry one.
//
// SHALLOW AND DEEP, ENFORCED HERE AND NOWHERE ELSE:
//   · A single click or an arrow key is a SHALLOW gesture — it moves the
//     cursor, an identity the grid holds. The cell is only told.
//   · Deep is entered ONLY through the grid — Enter or a double-click on the
//     cursor's cell. The grid calls cell.beginEdit(release), marks itself
//     deep, and its capture goes INERT: clicks and keys do nothing, because
//     the keyboard is the cell's now.
//   · The cell hands control back by calling release(). The grid never learns
//     what happened inside — it resumes: shallow, focus on the table, the
//     cursor repainted. Edit, commit and update are the domain's operations
//     and never change an arrangement.
//
// WIDTHS ARE GEOMETRY, THE GRID'S ALONE (map 7): held by column IDENTITY,
// applied by POSITION, in place — no arrangement runs, because no identity
// and no order moved. A request is bounded at normalisation and the bounded
// request is what is held. A snapshot returns what is held, never what was
// measured. The grid persists nothing: onColumnResized is a report, and
// remembering it is the host's.
// =============================================================================

var _HRG_MIN_W = 40, _HRG_MAX_W = 2000;   // the legal range — normalisation's bound
var _HRG_DEFAULT_W = 120;                 // what a keyboard resize starts from when nothing is held
var _HRG_KEY_STEP = 10;                   // one Alt+arrow

class RelGrid {

    constructor(opts) {
        opts = opts || {};
        if (!opts.container) throw new Error("[RelGrid] opts.container is required");
        if (!opts.branch)    throw new Error("[RelGrid] opts.branch is required");
        if (!opts.relation)  throw new Error("[RelGrid] opts.relation is required");
        var r = opts.relation;
        if (typeof r.pks !== "function" || typeof r.columns !== "function" || typeof r.cellFor !== "function")
            throw new Error("[RelGrid] relation must expose pks(), columns() and cellFor(pk, column)");
        var self = this;
        this._relation = r;
        this._cellFor = function (pk, col) { return r.cellFor(pk, col); };
        this._cbArranged    = opts.onArranged || null;
        this._cbCursor      = opts.onCursorMoved || null;
        this._cbEditStarted = opts.onEditStarted || null;
        this._cbEditEnded   = opts.onEditEnded || null;
        this._cbResized     = opts.onColumnResized || null;

        var head = opts.header || {};
        this._showHead = head.show !== false;
        var labels = head.labels || {};
        this._labelOf = function (c) {
            return Object.prototype.hasOwnProperty.call(labels, c) ? labels[c] : c;
        };

        // The cursor: an IDENTITY the grid holds, with the position it was last
        // seen at as the fallback when the identity leaves the presented space.
        this._cursor = null;       // { pk, column } | null
        this._cursorPos = null;    // { i, j } | null
        this._deep = false;        // the one fact the grid holds about editing
        this._widths = new Map();  // column → px, identity-keyed; positional only at the layout

        this._maps = new RelGridViewMaps({
            pks: r.pks(),
            columns: r.columns(),
            onViewChanged: function (kind) { self._arrange(kind); }
        });
        this._layout = new RelGridLayout({
            container: opts.container,
            label: opts.label || null,
            showHeader: this._showHead,
            onCellClick:    function (i, j) { self._onClick(i, j); },
            onCellDblClick: function (i, j) { self._onDblClick(i, j); },
            // The staged drag MINTS (j, px) on release; position becomes identity
            // here, and the request is normalised where it is held.
            onColResize: function (j, px) {
                var c = self._maps.columnAt(j);
                if (c != null) self.setColumnWidth(c, px);
            }
        });
        this._cells = new RelGridCells({ branch: opts.branch });

        this._keydown = function (e) { self._onKey(e); };
        this._layout.el().addEventListener("keydown", this._keydown);

        this._arrange("base");
    }

    // ── the arrangement cycle: structure, then re-place ────────────────────

    _arrange(kind) {
        var maps = this._maps, headers = [];
        for (var j0 = 0; j0 < maps.cols(); j0++) headers.push(this._labelOf(maps.columnAt(j0)));
        this._layout.render({ headers: headers, rows: maps.rows() });
        this._layout.setColWidths(this._widthsPositional());   // widths ride identity onto the new positions

        // One ask per identity, ever; one appendChild per cell, per pass.
        for (var i = 0; i < maps.rows(); i++) {
            for (var j = 0; j < maps.cols(); j++) {
                var id = maps.resolve(i, j);
                this._cells.ensure(id.pk, id.column, this._cellFor);
                this._cells.place(id.pk, id.column, this._layout.slotAt(i, j));
            }
        }
        // Whatever the view no longer shows leaves the tree alive.
        this._cells.detachInvisible(function (pk, col) { return maps.locate(pk, col) !== null; });

        this._resolveCursor();

        if (this._cbArranged) {
            try { this._cbArranged(kind); }
            catch (e) { console.error("[RelGrid] onArranged threw:", e); }
        }
    }

    /** Arrange again. The domain calls this after changing something the grid
     *  cannot see; it carries nothing and the grid asks nothing. */
    reapply() { this._arrange("reapply"); return this; }

    // ── widths: identity-keyed, applied positionally, in place ─────────────

    _widthsPositional() {
        var out = [], m = this._maps;
        for (var j = 0; j < m.cols(); j++) {
            var c = m.columnAt(j);
            out.push(this._widths.has(c) ? this._widths.get(c) : null);
        }
        return out;
    }

    /**
     * Hold a width for a column. The request is bounded to the legal range and
     * the BOUNDED request is what is held; a column the relation does not have
     * is refused (drift). Applied in place — no arrangement runs.
     */
    setColumnWidth(column, px) {
        if (this._maps.baseColumns().indexOf(column) < 0) return false;
        var n = Number(px);
        if (!isFinite(n)) return false;
        var bounded = Math.max(_HRG_MIN_W, Math.min(_HRG_MAX_W, n));
        if (this._widths.get(column) === bounded) return true;    // already held: nothing to do
        this._widths.set(column, bounded);
        this._layout.setColWidths(this._widthsPositional());
        if (this._cbResized) {
            try { this._cbResized(column, bounded); }
            catch (e) { console.error("[RelGrid] onColumnResized threw:", e); }
        }
        return true;
    }

    /** What is HELD for a column, or null. Never what was measured. */
    columnWidth(column) { return this._widths.has(column) ? this._widths.get(column) : null; }

    /** The snapshot: every held width, by column. A plain object — the host may keep it. */
    columnWidths() {
        var out = {};
        this._widths.forEach(function (px, c) { out[c] = px; });
        return out;
    }

    /** Apply a snapshot. Unknown columns are dropped as drift; the rest apply;
     *  applying a snapshot twice is applying it once. */
    setColumnWidths(widths) {
        if (!widths) return this;
        for (var c in widths) {
            if (Object.prototype.hasOwnProperty.call(widths, c)) this.setColumnWidth(c, widths[c]);
        }
        return this;
    }

    // ── the cursor: identity first, position as the fallback ───────────────

    _resolveCursor() {
        var maps = this._maps;
        if (maps.rows() === 0 || maps.cols() === 0) {          // an empty presented space: no cursor
            this._tell(this._cursor, "none");
            this._cursor = null; this._cursorPos = null;
            this._layout.paintCursor(null);
            return;
        }
        var at = this._cursor ? maps.locate(this._cursor.pk, this._cursor.column) : null;
        if (at) {                                                // the identity survived
            this._cursorPos = at;
        } else {                                                 // fall back to the position
            var p = this._cursorPos || { i: 0, j: 0 };
            var i = Math.min(p.i, maps.rows() - 1), j = Math.min(p.j, maps.cols() - 1);
            this._tell(this._cursor, "none");
            this._cursor = maps.resolve(i, j);
            this._cursorPos = { i: i, j: j };
            this._tell(this._cursor, this._deep ? "deep" : "shallow");
        }
        this._layout.paintCursor(this._cursorPos);
    }

    _setCursor(i, j) {
        var id = this._maps.resolve(i, j);
        if (!id) return false;
        if (this._cursor && this._cursor.pk === id.pk && this._cursor.column === id.column) return false;
        this._tell(this._cursor, "none");
        this._cursor = id;
        this._cursorPos = { i: i, j: j };
        this._tell(id, "shallow");
        this._layout.paintCursor(this._cursorPos);
        if (this._cbCursor) {
            try { this._cbCursor(id.pk, id.column); }
            catch (e) { console.error("[RelGrid] onCursorMoved threw:", e); }
        }
        return true;
    }

    /** Tell a cell its selection mode — pure lifecycle; the cell may ignore it. */
    _tell(id, mode) {
        if (!id) return;
        var entry = this._cells.get(id.pk, id.column);
        if (entry && typeof entry.cell.onSelect === "function") {
            try { entry.cell.onSelect(mode); }
            catch (e) { console.error("[RelGrid] cell.onSelect threw:", e); }
        }
    }

    _move(di, dj) {
        if (!this._cursorPos) return;
        var maps = this._maps;
        var i = Math.max(0, Math.min(maps.rows() - 1, this._cursorPos.i + di));
        var j = Math.max(0, Math.min(maps.cols() - 1, this._cursorPos.j + dj));
        this._setCursor(i, j);
    }

    // ── capture: inert while deep ──────────────────────────────────────────

    _onClick(i, j) {
        if (this._deep) return;                // the cell has the pointer and the keyboard
        this._setCursor(i, j);
    }

    _onDblClick(i, j) {
        if (this._deep) return;
        this._setCursor(i, j);
        this.beginEditAtCursor();
    }

    _onKey(e) {
        if (this._deep) return;                // the keyboard is the cell's
        var key = e.key;
        // Alt+Left/Right: the pointer-free resize of the CURSOR's column. The
        // cursor does not move.
        if (e.altKey && (key === "ArrowLeft" || key === "ArrowRight")) {
            if (this._cursor) {
                var held = this.columnWidth(this._cursor.column);
                var from = (held == null) ? _HRG_DEFAULT_W : held;
                this.setColumnWidth(this._cursor.column, from + (key === "ArrowRight" ? _HRG_KEY_STEP : -_HRG_KEY_STEP));
            }
            if (e.preventDefault) e.preventDefault();
            return;
        }
        if      (key === "ArrowUp")    this._move(-1, 0);
        else if (key === "ArrowDown")  this._move(1, 0);
        else if (key === "ArrowLeft")  this._move(0, -1);
        else if (key === "ArrowRight") this._move(0, 1);
        else if (key === "Enter")      this.beginEditAtCursor();
        else return;                           // not ours; let it bubble
        if (e.preventDefault) e.preventDefault();
    }

    // ── deep: the grid hands control to the cell, and takes it back ────────

    /**
     * Ask the cursor's cell to go deep. The cell may decline (a read-only cell
     * answers false, or has no beginEdit at all) and the grid stays shallow.
     * Otherwise the grid is deep until the cell calls release() — once.
     */
    beginEditAtCursor() {
        if (this._deep || !this._cursor) return false;
        var id = this._cursor;
        var entry = this._cells.get(id.pk, id.column);
        var cell = entry && entry.cell;
        if (!cell || typeof cell.beginEdit !== "function") return false;
        var self = this, released = false;
        var release = function () {
            if (released) return;
            released = true;
            self._resume(id);
        };
        this._deep = true;
        this._layout.setDeep(true);
        var ok;
        try { ok = cell.beginEdit(release); }
        catch (e) { console.error("[RelGrid] cell.beginEdit threw:", e); ok = false; }
        if (ok === false) {                    // declined: nothing changed hands
            this._deep = false;
            this._layout.setDeep(false);
            return false;
        }
        this._tell(id, "deep");
        if (this._cbEditStarted) {
            try { this._cbEditStarted(id.pk, id.column); }
            catch (e) { console.error("[RelGrid] onEditStarted threw:", e); }
        }
        return true;
    }

    /** The cell handed control back. What happened inside is not the grid's. */
    _resume(id) {
        this._deep = false;
        this._layout.setDeep(false);
        this._tell(id, "shallow");
        this._layout.focus();                  // the keyboard host takes the keys again
        if (this._cbEditEnded) {
            try { this._cbEditEnded(id.pk, id.column); }
            catch (e) { console.error("[RelGrid] onEditEnded threw:", e); }
        }
    }

    // ── the surface ────────────────────────────────────────────────────────

    /** Programmatic shallow cursor; a position not presented is ignored. */
    selectCell(pk, column) {
        if (this._deep) return false;
        var at = this._maps.locate(pk, column);
        return at ? this._setCursor(at.i, at.j) : false;
    }

    cursor()   { return this._cursor ? { pk: this._cursor.pk, column: this._cursor.column } : null; }
    isDeep()   { return this._deep; }
    focus()    { this._layout.focus(); return this; }
    viewMaps() { return this._maps; }
    el()       { return this._layout.el(); }
    cells()    { return this._cells; }

    /** Detaches every cell and removes the table. Disposes nothing: the cells
     *  are the domain's, and the next grid over this relation may find them. */
    destroy() {
        this._layout.el().removeEventListener("keydown", this._keydown);
        this._cells.destroy();
        this._layout.destroy();
    }
}
