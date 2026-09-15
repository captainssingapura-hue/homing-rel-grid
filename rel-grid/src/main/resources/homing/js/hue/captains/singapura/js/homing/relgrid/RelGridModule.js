// =============================================================================
// RelGridModule — RFC 0050 · Episode 2's facade: composes the grid and is the
// ONLY place the two branches meet. Orchestration only — the seam is
// RelGridViewMaps, the chrome is RelGridLayout, the registry is RelGridCells;
// the facade reads the relation's View and columns, threads the pieces, and
// re-places cells into the slots on every arrangement pass — the same slots
// while the shape holds, fresh ones when it changes.
// What a gesture means is RelGridGestures; the cursor is RelGridCursor; the
// handover of control is RelGridControl; the channel and its questions are
// RelGridChannel; the widths are RelGridWidths; the window — the relation's
// seam asked to move — is RelGridWindow; the merged cells are RelGridSpans;
// the stock clipboard writer is RelGridClipboard. Each holds its own state and
// is handed only what it needs.
//
// TWO INDEPENDENT BRANCHES. The grid mints its own chrome on its own
// DomOpsParty branch and nothing else; the domain mints its cells on a
// branch of its own and the grid never sees it. A cell is a NOUN: the grid
// asks cellFor(pk, column) for it, asks it once for cellElement(), and
// PLACES that element in the slot its identity maps to — that is the whole
// of what crosses. A HEADER CELL is a noun the same way: headerFor(column),
// headerElement() once, placed in the <th>; the grid captures nothing on it
// but its own resize handle. Nothing is rendered into anything; nothing is
// read back.
//
//   new RelGrid({
//       container,        // where the layout mounts
//       branch,           // the grid's OWN branch (DomOpsParty), handed UNACTIVATED: the grid
//                         // activates it, as the owner does, and everything the grid mints —
//                         // chrome, slots, overlays, mask — is on it or a sub-branch of it.
//                         // Dissolving it is the host's. Never a cell's element.
//       relation,         // { view(intent?), columns(), cellFor(pk, column) } — and, optionally,
//                         // readOnlyColumns(), labels(), headerFor(column). See RootRelationContract.
//                         // ONE ROOT: view() answers the rows to PRESENT now, in order — the whole
//                         // of a static relation, a window of an endless one. The grid asks it at
//                         // construction and holds what it answered, and no other list; there is
//                         // no enumeration to ask for. view({ by: n }) is the same seam asked to
//                         // MOVE — keys back, or nothing — and is asked at the window's edges.
//       label?,           // aria-label
//       frame?,           // false: the grid draws no frame of its own — the HOST frames it, around
//                         // whatever scrolls the table, so the scrollbar sits inside the light. The
//                         // host wears hrg_frame and hrg_lit on that element; the cursor's colour
//                         // follows, since hrg_lit's property inherits. Default: the grid frames its wrap.
//       header?,          // { show?, sticky? } — grid chrome, the host's, read once (map 12).
//                         // sticky: the header stays at the top of whatever scrolls the table,
//                         // and the cursor is revealed clear of it. What a header SAYS is the
//                         // relation's: labels(), or a header cell from headerFor(column), placed
//                         // in the <th> as a cell is placed in a <td> (law 86). No labels here.
//       stickyInset?,     // () → px: a band the HOST keeps stuck above the table — a group's
//                         // header — that a revealed slot must clear. Geometry, asked when needed
//       overflow?,        // wrap | clip | ellipsis — what a slot does with content
//                         // too wide for it. Default ellipsis.
//       columnView?,      // a subset of relation.columns() — the column axis IS listed. A
//                         // relation may declare columns it shows only sometimes. (There is
//                         // no rowView: what is presented is what relation.view() answers,
//                         // and later remaps go through viewMaps().)
//       minColumnWidth?,  // the floor a width request is bounded to. Default 40, the
//                         // narrowest a column stays grabbable at; never below 8. A host
//                         // whose columns are half-squares says 24.
//       mergedCells?,     // true to honour a cell's colSpan(). Off by default: most
//                         // relations have no merged cells, and the feature is kept apart.
//       resizeGuide?,     // what a header drag's guide line spans: an element, or a LIST
//                         // of them for a segment each. Default the table; a host stacking
//                         // several tables names each table's box, so the line breaks
//                         // at whatever sits between them.
//       onArranged?,      // (kind) after every placement pass
//       onCursorMoved?,   // (pk, column)
//       onControlTaken?,  // (pk, column) — the cell took control of this one
//       onControlReleased?, // (pk, column) — and gave it back
//       onColumnResized?, // (column, px) — a REPORT of what is now held; the grid keeps nothing
//       onCopied?,        // (content) — a REPORT: this was written to the clipboard
//       onEdge?,          // (direction) — a REPORT: a bare arrow went nowhere, the cursor being
//                         // already at that edge — 'up' | 'down' | 'left' | 'right' — and, up
//                         // or down, the relation asked for the View a row on answered nothing.
//                         // The grid itself does nothing with it; a host stacking tables steps over.
//       ask?,             // (question, mask) — THE CHANNEL: see RelGridChannel.
//       clipboard?        // { write(content) → thenable } — the writer; the stock one by default
//   });
//
// THE ROOT PRINCIPLE, AS CODE: this file asks the relation for its View, its
// columns and its cells. It never asks for, holds, pushes or writes a value, and
// RelGridValueFreeTest holds every grid module to that.
//
// THE ARRANGEMENT CYCLE: every presented identity is ensured first — asked of
// the relation once per presentation, and kept while presented — BEFORE
// anything moves, so a relation that refuses one refuses the View whole; then
// the layout renders the slot matrix for the presented shape, widths ride
// identity onto the new positions, every cell is placed — into its slot, or
// into a merged cell's host when it reaches across several — the cursor
// resolves (identity first, position as the fallback; a cell it leaves is
// told so while the registry still knows it), whatever the view no longer
// shows leaves the tree alive AND is forgotten — the domain's cellFor is the
// keeper, and an identity that returns is asked for again — and every range
// goes (law 43: a selection is positions, and these are not the same
// positions). The registry is exactly the presented cells: a window of W
// rows costs W × columns entries, however far it has scrolled.
//
// MEMBERSHIP IS THE RELATION'S. The grid keeps no list of what exists — the
// row axis is the View — so it cannot tell a stranger from a row; the relation
// can, and refuses a stranger by throwing from cellFor. The ensure pass puts
// that refusal before the first slot is touched: the arrangement throws, the
// maps put the rows back, and the caller — a host's remap, or the channel's
// settle — hears it. Half a View is not a View.
//
// MERGED CELLS are RelGridSpans, behind mergedCells: a leading cell placed in
// a host over the slots it reaches across, a horizontal step out of the
// group, deep to the leading cell.
//
// THE WINDOW MOVES BY THE SAME SEAM. An arrow at the top or bottom edge, the
// wheel, PageUp and PageDown ask the relation for the View n rows on —
// view({ by: n }) — and present what comes back; nothing back means the rows
// stay, the arrow is reported as the edge it met, and the wheel is left to the
// browser. A static relation answers nothing and behaves as it always did; an
// endless one answers a window, arranged on the same slots. scrollRows(n) is
// the programmatic twin.
//
// LOCKED while a cell is deep OR a question is pending — the two are exclusive
// (law 228) — every intent is refused, not deferred (law 221): keys, clicks,
// drags, the handover, a resize, the window, and the programmatic twins of each.
// =============================================================================

class RelGrid {

    constructor(opts) {
        opts = opts || {};
        if (!opts.container) throw new Error("[RelGrid] opts.container is required");
        if (!opts.branch)    throw new Error("[RelGrid] opts.branch is required");
        if (!opts.relation)  throw new Error("[RelGrid] opts.relation is required");
        var r = opts.relation;
        if (typeof r.view !== "function" || typeof r.columns !== "function" || typeof r.cellFor !== "function")
            throw new Error("[RelGrid] relation must expose view(), columns() and cellFor(pk, column)");
        if (opts.rowView) throw new Error("[RelGrid] there is no rowView: what is presented is what relation.view() answers");
        var start = r.view();                    // the rows to present now — the relation's answer, held as the View
        if (!Array.isArray(start)) throw new Error("[RelGrid] relation.view() must answer the rows to present, as a list; got: " + start);
        var self = this;
        opts.branch.activate(this);              // the grid's own: the party refuses one that is already somebody's
        this._relation = r;
        this._cellFor = function (pk, col) { return r.cellFor(pk, col); };
        this._cbArranged = opts.onArranged || null;
        this._cbResized  = opts.onColumnResized || null;
        this._destroyed = false;
        var head = opts.header || {};
        if (head.labels) throw new Error("[RelGrid] header.labels is gone: what a column is called is the relation's — labels(), or headerFor(column) (map 12, law 86)");
        // What a column is CALLED: the relation's, read once (structure). A relation that
        // answers header cells says it in the cell; a plain one in labels(); else the name.
        var labels = (typeof r.labels === "function") ? (r.labels() || {}) : {};
        this._labelOf = function (c) { return Object.prototype.hasOwnProperty.call(labels, c) ? labels[c] : c; };
        this._headerFor = (typeof r.headerFor === "function") ? function (c) { return r.headerFor(c); } : null;
        // The column constraint: read ONCE, here, because it is structure and
        // not a question about a moment. Absent means no constraint.
        var readOnly = new Set();
        if (typeof r.readOnlyColumns === "function") {
            var declared = r.readOnlyColumns() || [];
            for (var d = 0; d < declared.length; d++) readOnly.add(declared[d]);
        }
        this._selection = new RelGridSelection();  // POSITIONS, and nothing else
        this._maps = new RelGridViewMaps({
            rowView: start, columns: r.columns(),
            columnView: opts.columnView || null,
            onViewChanged: function (kind) { self._arrange(kind); }
        });
        this._layout = new RelGridLayout({
            branch: opts.branch, container: opts.container, label: opts.label || null, frame: opts.frame,
            showHeader: head.show !== false, stickyHeader: head.sticky === true, stickyInset: opts.stickyInset || null,
            overflow: opts.overflow || null, resizeGuide: opts.resizeGuide || null,
            onCellClick:    function (i, j, mods) { self._gestures.onClick(i, j, mods); },
            onCellDblClick: function (i, j) { self._gestures.onDblClick(i, j); },
            onCellDown:     function (i, j, mods) { self._gestures.onDown(i, j, mods); },
            onCellDragTo:   function (i, j) { self._gestures.onDragTo(i, j); },
            onDragEnd:      function () { self._gestures.onDragEnd(); },
            // The staged drag MINTS (j, px) on release; position becomes identity
            // here, and the request is normalised where it is held.
            onColResize: function (j, px) {
                var c = self._maps.columnAt(j);
                if (c != null) self.setColumnWidth(c, px);
            }
        });
        this._cells = new RelGridCells();       // the domain's elements, by identity; the grid mints none
        this._headers = new RelGridHeaders();   // the domain's header cells, by column; the grid mints none
        this._spans = new RelGridSpans({ on: opts.mergedCells === true, layout: this._layout, cells: this._cells, maps: this._maps });
        this._window = new RelGridWindow({ relation: r, maps: this._maps });
        this._widths = new RelGridWidths({ maps: this._maps, minColumnWidth: opts.minColumnWidth });
        this._cursor = new RelGridCursor({
            maps: this._maps, layout: this._layout, cells: this._cells,
            stepJ: function (i, j, dj) { return self._spans.stepJ(i, j, dj); },
            onMoved: opts.onCursorMoved
        });
        this._control = new RelGridControl({
            layout: this._layout, cells: this._cells, readOnly: readOnly,
            tell: function (id, mode) { self._cursor.tell(id, mode); },
            onTaken: opts.onControlTaken, onReleased: opts.onControlReleased
        });
        this._channel = new RelGridChannel({
            ask: (typeof opts.ask === "function") ? opts.ask : null,
            layout: this._layout, maps: this._maps, selection: this._selection,
            cursorPos: function () { return self._cursor.pos(); },
            clipboard: opts.clipboard || createRelGridClipboard({ branch: opts.branch }),
            onCopied: opts.onCopied
        });
        this._gestures = new RelGridGestures({
            grid: this, maps: this._maps, selection: this._selection, cursor: this._cursor,
            locked: function () { return self._locked(); },
            afterSelection: function () { self._afterSelection(); },
            stepJ: function (i, j, dj) { return self._spans.stepJ(i, j, dj); },
            stepWidth: function (column, dir) { self.setColumnWidth(column, self._widths.stepped(column, dir)); },
            rowHeight: function () { return self._layout.rowHeight(); },
            onEdge: opts.onEdge
        });
        this._keydown = function (e) { self._gestures.onKey(e); };
        this._wheel = function (e) { self._gestures.onWheel(e); };
        this._layout.el().addEventListener("keydown", this._keydown);
        this._layout.el().addEventListener("wheel", this._wheel, { passive: false });
        this._arrange("base");
    }

    /** Locked while a cell is deep OR a question is pending — the two are exclusive (law 228). */
    _locked() { return this._control.isDeep() || this._channel.isPending(); }

    // ── the arrangement cycle: structure, then re-place ────────────────────

    _arrange(kind) {
        var maps = this._maps, headers = [], ids = [], i, j, id;
        for (var j0 = 0; j0 < maps.cols(); j0++) headers.push(this._labelOf(maps.columnAt(j0)));
        // One ask per identity per presentation — and every ask BEFORE a slot moves:
        // a refusal here leaves the pass with nothing changed, and the maps undo the View.
        // The header cells first, one per presented column, when the relation answers them.
        if (this._headerFor) for (j = 0; j < maps.cols(); j++) this._headers.ensure(maps.columnAt(j), this._headerFor);
        for (i = 0; i < maps.rows(); i++) {
            for (j = 0; j < maps.cols(); j++) {
                id = maps.resolve(i, j);
                this._cells.ensure(id.pk, id.column, this._cellFor);
                ids.push(id);
            }
        }
        this._layout.render({ headers: headers, rows: maps.rows(), labelled: !this._headerFor });
        this._layout.setColWidths(this._widths.positional());   // widths ride identity onto the new positions
        // The header cells into the header slots; whatever column left the view is forgotten.
        if (this._headerFor) {
            for (j = 0; j < maps.cols(); j++) this._headers.place(maps.columnAt(j), this._layout.headerSlotAt(j));
            this._headers.detachAbsent(function (col) { return maps.colOf(col) >= 0; });
        }
        // One appendChild per cell, per pass.
        for (i = 0; i < maps.rows(); i++) {
            for (j = 0; j < maps.cols(); j++) {
                id = ids[i * maps.cols() + j];
                this._cells.place(id.pk, id.column, this._layout.slotAt(i, j));
                this._spans.mark(i, j, id);
            }
        }
        this._spans.settle();                                 // every cell is in; measure the hosts
        this._cursor.resolve(this._control.isDeep());         // first: a cell the cursor leaves is told while it is still known
        this._cells.detachInvisible(function (pk, col) { return maps.locate(pk, col) !== null; });   // alive, out of the tree, forgotten
        this._selection.clear();                              // law 43: every range goes with the presented space
        this._afterSelection();
        if (this._cbArranged) {
            try { this._cbArranged(kind); }
            catch (e) { console.error("[RelGrid] onArranged threw:", e); }
        }
    }

    /** Where deep goes from the cursor: its own cell, or a merged cell's leading cell. */
    _deepAt() { return this._spans.deepAt(this._cursor.id(), this._cursor.pos()); }

    /** Ask what is selected, paint it, and tell the domain. The only reader of the list there is. */
    _afterSelection() {
        var rects = this._selection.resolve(this._cursor.pos());
        this._layout.paintSelection(rects);
        this._channel.selectionChanged(rects);
    }

    /** Arrange again. The domain calls this after changing something the grid
     *  cannot see; it carries nothing and the grid asks nothing. */
    reapply() { this._arrange("reapply"); return this; }

    // ── widths: identity-keyed, applied positionally, in place ─────────────

    /**
     * Hold a width for a column: bounded, held, applied in place, reported
     * once. Not while a cell holds control: the editor is laid over a slot at
     * a fixed position, and moving the column under it would leave the two
     * disagreeing.
     */
    setColumnWidth(column, px) {
        if (this._locked()) return false;
        var held = this._widths.hold(column, px);
        if (!held) return false;
        if (!held.changed) return true;                       // already held: nothing to do
        this._layout.setColWidths(this._widths.positional());
        if (this._cbResized) {
            try { this._cbResized(column, held.px); }
            catch (e) { console.error("[RelGrid] onColumnResized threw:", e); }
            // A host may answer the report by changing the geometry the grid sits in
            // — sizing its container to the widths now held is the common case — and
            // the merged cells' hosts were measured before it did. Measure again.
            this._spans.settle();
        }
        return true;
    }

    /** What is HELD for a column, or null. Never what was measured. */
    columnWidth(column) { return this._widths.get(column); }

    /** The snapshot: every held width, by column. A plain object — the host may keep it. */
    columnWidths() { return this._widths.snapshot(); }

    /** Apply a snapshot. Unknown columns are dropped as drift; applying it twice is applying it once. */
    setColumnWidths(widths) {
        if (!widths) return this;
        for (var c in widths) {
            if (Object.prototype.hasOwnProperty.call(widths, c)) this.setColumnWidth(c, widths[c]);
        }
        return this;
    }

    // ── the channel's questions ────────────────────────────────────────────

    /** Ask what the selection is worth on a clipboard, and write the answer. False when locked, channel-less, or empty. */
    copy() {
        if (this._locked() || !this._channel.has() || !this._cursor.pos()) return false;
        return this._channel.copy();
    }

    /** Hand the arrangement of the rows to the domain. The twin of Alt+Enter; the grid offers no control of its own. */
    handoverView() {
        if (this._locked() || !this._channel.has()) return false;
        return this._channel.handoverView();
    }

    // ── told, unasked: the channel's other direction ───────────────────────

    /**
     * The domain saying, unasked, through the host. A protocol value by kind:
     * RelGridViewChanged — the View changed underneath the grid — is answered
     * by asking view() again and presenting the answer, as any remap; an
     * unknown kind is recorded and refused. Whether anything was done.
     */
    tell(message) {
        if (this._destroyed) return false;
        if (message instanceof RelGridViewChanged) {
            var keys = this._relation.view();
            if (!Array.isArray(keys)) throw new Error("[RelGrid] relation.view() must answer the rows to present, as a list; got: " + keys);
            this._maps.setRowView(keys);                      // → arrange("rows")
            return true;
        }
        console.error("[RelGrid] told something it does not understand:", message);
        return false;
    }

    // ── the window: the same seam, asked to move ───────────────────────────

    /**
     * Ask the relation for the View n rows on (negative: back) and present it.
     * The twin of the wheel and of an arrow at the window's edge. False when
     * locked, when n is 0, and when the relation answers nothing or the same
     * rows — the rows stay; that is how a static relation answers every time.
     */
    scrollRows(n) {
        if (this._locked()) return false;
        return this._window.move(n);
    }

    // ── deep: the grid hands control to the cell, and takes it back ────────

    /** May the cursor's cell take control right now? Public, because a feature must be able to ask WITHOUT opening anything. */
    mayTakeControlAtCursor() {
        var at = this._deepAt();
        return at ? this._control.may(at.id) : false;
    }

    /** Offer the cursor's cell control. True when it was taken. */
    takeControlAtCursor() {
        if (this._locked()) return false;
        var at = this._deepAt();
        return at ? this._control.take(at) : false;
    }

    // ── the surface ────────────────────────────────────────────────────────

    /** Programmatic shallow cursor; a position not presented is ignored. A
     *  BARE move, so it clears the list exactly as a click without a modifier does. */
    selectCell(pk, column) {
        if (this._locked()) return false;
        var at = this._maps.locate(pk, column);
        if (!at) return false;
        var moved = false, self = this;
        this._gestures.bareMove(function () { moved = self._cursor.set(at.i, at.j); });
        this._cursor.follow(at, true);                        // a host asked for this cell: show it, keyboard or not
        return moved;
    }

    /** Extend to an identity — the shift gesture. The cursor does not move. */
    extendSelection(pk, column) {
        if (this._locked()) return false;
        var at = this._maps.locate(pk, column);
        return at ? this._gestures.extendTo(at.i, at.j) : false;
    }

    /** Add a 1x1 at an identity and go there — the ctrl gesture. */
    addToSelection(pk, column) {
        if (this._locked()) return false;
        var at = this._maps.locate(pk, column);
        if (!at) return false;
        this._selection.add({ i: at.i, j: at.j });
        this._cursor.set(at.i, at.j);
        this._afterSelection();
        return true;
    }

    /** The whole presented space, in one range. */
    selectAll() {
        if (this._locked()) return false;
        this._selection.all(this._maps.rows(), this._maps.cols());
        this._afterSelection();
        return true;
    }

    clearSelection() {
        this._selection.clear();
        this._afterSelection();
        return this;
    }

    /** What is selected: the RESOLVED list of rectangles in the presented space — an empty list reads as the cursor's own 1x1. */
    selectedRanges()  { return this._selection.resolve(this._cursor.pos()); }
    /** How many ranges were actually MADE — zero when the selection is just the cursor. */
    selectionCount()  { return this._selection.count(); }

    cursor()    { var id = this._cursor.id(); return id ? { pk: id.pk, column: id.column } : null; }
    isDeep()    { return this._control.isDeep(); }
    isPending() { return this._channel.isPending(); }
    focus()     { this._layout.focus(); return this; }
    viewMaps()  { return this._maps; }
    el()        { return this._layout.el(); }
    cells()     { return this._cells; }

    /** Detaches every cell and removes the table. Disposes nothing: the cells
     *  are the domain's, and the next grid over this relation may find them. */
    destroy() {
        this._destroyed = true;
        this._control.destroy();               // a late settle must not resume a dead grid
        this._channel.destroy();
        this._layout.el().removeEventListener("keydown", this._keydown);
        this._layout.el().removeEventListener("wheel", this._wheel);
        this._cells.destroy();
        this._headers.destroy();
        this._layout.destroy();
    }
}
