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
//       overflow?,        // wrap | clip | ellipsis — what a slot does with content
//                         // too wide for it. Default ellipsis.
//       rowView?,         // the identities to PRESENT at first, in order — a subset of
//                         // relation.pks(). Default: all of them. For a relation that
//                         // declares more than it shows; later remaps go through viewMaps().
//       columnView?,      // the same for columns — a subset of relation.columns(). A
//                         // relation may declare columns it shows only sometimes.
//       minColumnWidth?,  // the floor a width request is bounded to. Default 40, the
//                         // narrowest a column stays grabbable at; never below 8. A host
//                         // whose columns are half-squares says 24.
//       mergedCells?,     // true to honour a cell's colSpan(). Off by default: most
//                         // relations have no merged cells, and the feature is kept apart.
//       onArranged?,      // (kind) after every placement pass
//       onCursorMoved?,   // (pk, column)
//       onControlTaken?,  // (pk, column) — the cell took control of this one
//       onControlReleased?, // (pk, column) — and gave it back
//       onColumnResized?, // (column, px) — a REPORT of what is now held; the grid keeps nothing
//       onCopied?,        // (content) — a REPORT: this was written to the clipboard
//       ask?,             // (question, mask) — THE CHANNEL. See below.
//       clipboard?        // { write(content) → thenable } — the writer; navigator.clipboard by default
//   });
//
// THE ASK CHANNEL (ext4, ext6): one function, typed questions down, typed
// answers up. Its first customer is a NOTIFICATION — a question with no answer
// — and notifications are where the channel is cheapest to prove: the grid
// hands one over, does not wait, does not mask, and learns nothing.
//
//   ask(new RelGridSelectionChanged([ RelGridRange, ... ]))
//
// The payload is a value object GENERATED from a Java record, so its shape
// cannot drift from the declaration. The grid mints it and never reads it
// back. What it must not do is lose a failure, so a returned thenable that
// rejects is recorded — the diligence rule, which is why fire-and-forget is
// not the same as fire-and-ignore.
//
// A host with no ask gets no channel and nothing changes; the callbacks that
// remain are reports the channel has not claimed yet.
//
// THE PENDING ANSWER (ext6): the second customer is a QUESTION — the grid
// waits for the answer, and the interval is dangerous, because an answer
// describes the state it was asked about and the person must not be allowed
// to change that state underneath it. So while a question is pending:
//
//   · the grid is LOCKED: every intent is refused, not deferred (law 221) —
//     keys, clicks, drags, the handover, a resize, and the programmatic
//     twins of each;
//   · the grid is MASKED: a wash over the table that reads as unavailable,
//     holding the focus so no key reaches the table (law 220). Delayed, so a
//     fast answer shows none, and held briefly once up, so it never strobes
//     (law 224);
//   · the domain may draw on it. The second argument to ask is the MASK
//     HANDLE, and mask.panel() mints the panel — a golden rectangle at the
//     golden section of what is seen of the host's box — and hands it over
//     as a slot is handed to a cell. Asking for it mounts the mask at once;
//     not asking leaves a bare wash, because the grid invents no progress
//     (law 225).
//
// The answer is applied when it comes; absence applies nothing; a rejection
// is recorded and the grid resumes. One question at a time, because the only
// party that can raise one while the grid is locked is the host, and a host
// that asks twice is told no.
//
// COPY (map 6) is that question. Ctrl+C, or copy(), resolves the selection to
// IDENTITIES — one block per range, pks down and columns across, in view
// order — and asks what they are worth. The domain answers with finished
// content, or with nothing, and the grid WRITES what it was given (law 50):
// it is the party inside the person's gesture, so it is the party that
// writes, and it reads nothing of what it writes. The clipboard is reached
// under that gesture's activation — the async Clipboard API, or the copy
// command where a page is denied it — which is why copy is no longer the
// channel's synchronous exception: nothing here needs the browser's copy
// event to arrive, and everything here needs a panel.
//
// THE ROOT PRINCIPLE, AS CODE: this file asks the relation for identities,
// columns and cells. It never asks for, holds, pushes or writes a value, and
// there is no method on it that could carry one.
//
// SHALLOW AND DEEP, ENFORCED HERE AND NOWHERE ELSE:
//   · A single click or an arrow key is a SHALLOW gesture — it moves the
//     cursor, an identity the grid holds. The cell is only told.
//   · Deep is entered ONLY through the grid — Enter or a double-click on the
//     cursor's cell — and the offer is made in TWO STAGES:
//        1. the column's constraint, declared once by the relation. A
//           constrained column's cell is NEVER ASKED (map 16, law 112).
//        2. cell.mayTakeControl(), asked afresh every time. Only an explicit
//           true is a yes.
//     Then, and only then, cell.takeControl(host) — which MUST answer a thenable.
//     The HOST is minted over the slot but OUTSIDE the table, so an editor
//     cannot widen a column, stretch a row, or be clipped by its own cell —
//     and a cell may open something larger than itself. Where it sits is the
//     grid's; what goes in it is the cell's, exactly as with render(host).
//     Structure refuses always; a cell refuses sometimes; neither substitutes
//     for the other (law 113). Two stages rather than one because bulk edit
//     and paste must ask whether a cell is writable WITHOUT opening it, and a
//     single take-it-or-leave-it call cannot serve them.
//   · The grid then goes deep and its capture is INERT: clicks and keys do
//     nothing, because the keyboard is the cell's now.
//   · The cell hands control back by SETTLING the thenable, resolved or
//     rejected — an edit that blew up still ended. The grid never learns what
//     happened inside; it resumes: shallow, focus on the table, the cursor
//     repainted. Edit, commit and update are the domain’s operations and never
//     change an arrangement.
//
// THE SELECTION IS ASKED, NEVER OBEYED (map 5): the facade holds a
// RelGridSelection — an ordered list of position rectangles that knows nothing
// but positions — and does three things with it. It ROUTES the gestures
// (shift extends the last range's far corner, ctrl adds a 1x1 and moves the
// cursor, Ctrl+A takes the whole presented space, a BARE move clears the
// list, and a PRESS-DRAG is whichever of those three the press meant followed
// by an extension per slot the pointer reaches); it CLEARS on every
// arrangement, because a selection is positions and these are not the same
// positions; and it asks what is selected in order to
// paint it. The cursor stays here rather than in the selection: it is
// identity-first, it tells its cell and it paints, and none of that is the
// selection's business. The two meet in exactly two lines — a bare move
// clears, and an empty list resolves to the cursor's own 1x1.
//
// Copy is the first thing that CONSUMES a selection, and it reads the list in
// one place — copy() — and resolves it there. Clear and bulk are later rounds.
//
// MERGED CELLS — a matrix that stays whole, and a cell laid over part of it.
// Every position keeps its slot and its own cell; nothing is skipped and
// nothing is asked differently. A cell that answers colSpan() > 1 is a
// LEADING cell: the layout mints a host over the n slots it reaches across
// and the cell is placed THERE instead of in its own slot, which stays,
// empty. The host is opaque, takes no pointer, and mirrors the state of the
// slots beneath — so the whole group reads as one cell while the tracker
// keeps the exact square:
//
//   · a vertical move passes through: the column is kept, covered or not;
//   · a horizontal move jumps OUT of the group — right lands on the slot
//     after it, left on the slot before — and a move that lands inside
//     another group lands on the exact slot reached;
//   · a selection is the rectangle it is, never widened; the group is painted
//     when any of its slots is in one;
//   · Enter anywhere in a group offers control to the LEADING cell, over the
//     group's whole box.
//
// Spans are read on every arrangement, so a span that moves is an
// arrangement the host asks for. Behind mergedCells, because most relations
// have no merged cell and the check is not free.
//
// WIDTHS ARE GEOMETRY, THE GRID'S ALONE (map 7): held by column IDENTITY,
// applied by POSITION, in place — no arrangement runs, because no identity
// and no order moved. A request is bounded at normalisation and the bounded
// request is what is held. A snapshot returns what is held, never what was
// measured. The grid persists nothing: onColumnResized is a report, and
// remembering it is the host's.
// =============================================================================

var _HRG_MIN_W = 40, _HRG_MAX_W = 2000;   // the legal range — normalisation's bound
var _HRG_MIN_W_FLOOR = 8;                 // and the least a host may lower the floor to
var _HRG_DEFAULT_W = 120;                 // what a keyboard resize starts from when nothing is held
var _HRG_KEY_STEP = 10;                   // one Alt+arrow
var _HRG_MASK_DELAY = 200;                // a question answered sooner shows no mask
var _HRG_MASK_HOLD = 250;                 // and one that showed stays at least this long

function _hrgLater(fn, ms)  { return setTimeout(fn, ms); }
function _hrgCancel(handle) { clearTimeout(handle); }
function _hrgNow()          { return Date.now(); }

/**
 * The stock clipboard writer. Two roads to the same clipboard, both open only
 * while the person's activation is fresh — and it is, because the answer was
 * produced by their click on the domain's panel:
 *
 *   1. the async Clipboard API. Rich content goes as one item carrying both
 *      forms, so a spreadsheet takes the table and an editor takes the text;
 *      without an html, or without ClipboardItem, the plain form alone.
 *   2. the copy COMMAND, when the first is absent or refused. An insecure
 *      origin has no navigator.clipboard at all, and an embedded page can be
 *      denied it by policy while the command — gated on activation, not on
 *      policy — still works. Both forms ride the copy event's clipboardData.
 *
 * Neither road is a silence: a write that fails on both is a rejection, which
 * the grid records.
 */
function _hrgStockClipboard(env) {
    env = env || {
        navigator:     (typeof navigator !== "undefined") ? navigator : null,
        ClipboardItem: (typeof ClipboardItem !== "undefined") ? ClipboardItem : null,
        Blob:          (typeof Blob !== "undefined") ? Blob : null,
        document:      (typeof document !== "undefined") ? document : null
    };
    function modern(content) {
        var cb = env.navigator ? env.navigator.clipboard : null;
        if (!cb) return Promise.reject(new Error("navigator.clipboard is absent (not a secure context?)"));
        if (content.html != null && typeof env.ClipboardItem === "function" && typeof env.Blob === "function"
                && typeof cb.write === "function") {
            var item = new env.ClipboardItem({
                "text/plain": new env.Blob([content.text], { type: "text/plain" }),
                "text/html":  new env.Blob([content.html], { type: "text/html" })
            });
            return cb.write([item]);
        }
        if (typeof cb.writeText !== "function") return Promise.reject(new Error("navigator.clipboard cannot write"));
        return cb.writeText(content.text);
    }
    function command(content) {
        var doc = env.document;
        if (!doc || typeof doc.execCommand !== "function" || !doc.body) return false;
        var took = false;
        // The command fires a copy event; answering it is how both forms are
        // set. Captured, so nothing on the page sees a copy it did not make.
        var onCopy = function (e) {
            var data = e.clipboardData;
            if (!data || typeof data.setData !== "function") return;
            data.setData("text/plain", content.text);
            if (content.html != null) data.setData("text/html", content.html);
            if (e.preventDefault) e.preventDefault();
            took = true;
        };
        // The command needs a selection to act on; a textarea off-screen is one.
        var ta = doc.createElement("textarea");
        ta.textContent = content.text;
        ta.setAttribute("aria-hidden", "true");
        if (ta.style && ta.style.setProperty) {
            ta.style.setProperty("position", "fixed");
            ta.style.setProperty("left", "-9999px");
            ta.style.setProperty("top", "0");
        }
        // Selecting the textarea takes the focus, and this runs AFTER the grid
        // has already handed the focus back to the table — so what was focused
        // is put back, or the person is left typing into nothing.
        var prev = doc.activeElement || null;
        doc.addEventListener("copy", onCopy, true);
        doc.body.appendChild(ta);
        var ran = false;
        try { ta.select(); ran = doc.execCommand("copy"); }
        catch (e) { ran = false; }
        doc.body.removeChild(ta);
        doc.removeEventListener("copy", onCopy, true);
        if (prev && prev.focus) {
            try { prev.focus({ preventScroll: true }); } catch (e) { /* gone; nothing to restore */ }
        }
        return ran && took;
    }
    return {
        write: function (content) {
            return modern(content).then(null, function (why) {
                if (command(content)) return;
                throw new Error("the clipboard refused both roads — " + (why && why.message ? why.message : why));
            });
        }
    };
}

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
        this._cbTaken       = opts.onControlTaken || null;
        this._cbReleased    = opts.onControlReleased || null;
        this._cbResized     = opts.onColumnResized || null;
        this._cbCopied      = opts.onCopied || null;
        this._ask = (typeof opts.ask === "function") ? opts.ask : null;
        this._clipboard = opts.clipboard || _hrgStockClipboard();

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
        this._pending = null;      // and the second, about waiting: the question outstanding
        this._destroyed = false;
        // The column constraint: read ONCE, here, because it is structure and
        // not a question about a moment. Absent means no constraint.
        this._readOnly = new Set();
        if (typeof r.readOnlyColumns === "function") {
            var declared = r.readOnlyColumns() || [];
            for (var d = 0; d < declared.length; d++) this._readOnly.add(declared[d]);
        }
        this._widths = new Map();  // column → px, identity-keyed; positional only at the layout
        // The floor of the legal range: the host's, within reason. A column
        // narrower than the default is a host's deliberate geometry — a
        // half-square for a squeezed mark — and not something a drag should
        // be able to reach by accident, which is what the default protects.
        var minW = Number(opts.minColumnWidth);
        this._minW = isFinite(minW) ? Math.max(_HRG_MIN_W_FLOOR, Math.min(_HRG_MAX_W, minW)) : _HRG_MIN_W;
        this._merge = opts.mergedCells === true;   // honour colSpan() at all
        // The selection: POSITIONS, and it holds nothing else. The facade is
        // the only thing that knows both it and the cursor.
        this._selection = new RelGridSelection();

        this._maps = new RelGridViewMaps({
            pks: r.pks(),
            columns: r.columns(),
            rowView: opts.rowView || null,
            columnView: opts.columnView || null,
            onViewChanged: function (kind) { self._arrange(kind); }
        });
        this._layout = new RelGridLayout({
            container: opts.container,
            label: opts.label || null,
            showHeader: this._showHead,
            overflow: opts.overflow || null,
            onCellClick:    function (i, j, mods) { self._onClick(i, j, mods); },
            onCellDblClick: function (i, j) { self._onDblClick(i, j); },
            onCellDown:     function (i, j, mods) { self._onDown(i, j, mods); },
            onCellDragTo:   function (i, j) { self._onDragTo(i, j); },
            onDragEnd:      function () { self._onDragEnd(); },
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
                if (this._merge) this._markSpan(i, j, id);
            }
        }
        if (this._merge) this._layout.placeGroups();          // every cell is in; measure the hosts
        // Whatever the view no longer shows leaves the tree alive.
        this._cells.detachInvisible(function (pk, col) { return maps.locate(pk, col) !== null; });

        this._resolveCursor();

        // Law 43: every range goes when the presented space is rebuilt. A range
        // is positions, and after an arrangement these are not the same
        // positions — the cursor survives because it is an identity, and the
        // selection does not because it is not.
        this._selection.clear();
        this._afterSelection();

        if (this._cbArranged) {
            try { this._cbArranged(kind); }
            catch (e) { console.error("[RelGrid] onArranged threw:", e); }
        }
    }

    /**
     * A leading cell's reach, read afresh on every pass: only a finite span
     * above one counts, clamped to the row's end, and a cell that throws or
     * answers nonsense is a plain cell. The layout marks the slots; the cell
     * draws itself.
     */
    _markSpan(i, j, id) {
        var entry = this._cells.get(id.pk, id.column), cell = entry && entry.cell;
        if (!cell || typeof cell.colSpan !== "function") return;
        if (this._layout.groupAt(i, j)) return;               // inside an earlier group: a plain cell
        var n;
        try { n = Number(cell.colSpan()); } catch (e) { console.error("[RelGrid] cell.colSpan threw:", e); return; }
        if (!isFinite(n) || n <= 1) return;
        n = Math.min(Math.floor(n), this._maps.cols() - j);
        if (n <= 1) return;
        var host = this._layout.openGroup(i, j, n);
        if (host) this._cells.place(id.pk, id.column, host);  // the leading cell lives in the host
    }

    /** The merged cell a position is in, or null: { i, j, n }. */
    _groupAt(i, j) { return this._merge ? this._layout.groupAt(i, j) : null; }

    /**
     * One horizontal step from a position: out of a merged cell entirely, if
     * the position is in one, and one slot otherwise. Landing inside another
     * group lands on the exact slot reached.
     */
    _stepJ(i, j, dj) {
        var grp = this._groupAt(i, j);
        if (!grp) return j + dj;
        return dj > 0 ? grp.j + grp.n : grp.j - 1;
    }

    /**
     * Where deep goes from the cursor: the cursor's own identity, or — inside
     * a merged cell — the LEADING cell's, since that is the cell with anything
     * in it, and the editor opens over the whole group.
     */
    _deepAt() {
        if (!this._cursor || !this._cursorPos) return null;
        var at = this._cursorPos, grp = this._groupAt(at.i, at.j);
        if (!grp) return { id: this._cursor, i: at.i, j: at.j };
        var id = this._maps.resolve(grp.i, grp.j);
        return id ? { id: id, i: grp.i, j: grp.j } : null;
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
        // Geometry is the grid's, but not while a cell holds control: the
        // editor is laid over a slot at a fixed position, and moving the column
        // under it would leave the two disagreeing. The keyboard path was
        // already inert while deep; a header drag was not.
        if (this._locked()) return false;
        if (this._maps.baseColumns().indexOf(column) < 0) return false;
        var n = Number(px);
        if (!isFinite(n)) return false;
        var bounded = Math.max(this._minW, Math.min(_HRG_MAX_W, n));
        if (this._widths.get(column) === bounded) return true;    // already held: nothing to do
        this._widths.set(column, bounded);
        this._layout.setColWidths(this._widthsPositional());
        if (this._cbResized) {
            try { this._cbResized(column, bounded); }
            catch (e) { console.error("[RelGrid] onColumnResized threw:", e); }
            // A host may answer the report by changing the geometry the grid sits in
            // — sizing its container to the widths now held is the common case — and
            // the merged cells' hosts were measured before it did. Measure again.
            if (this._merge) this._layout.placeGroups();
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
        var p = this._cursorPos;
        this._setCursor(this._clampI(p.i + di), this._clampJ(dj ? this._stepJ(p.i, p.j, dj) : p.j));
    }

    _clampI(i) { return Math.max(0, Math.min(this._maps.rows() - 1, i)); }
    _clampJ(j) { return Math.max(0, Math.min(this._maps.cols() - 1, j)); }

    // ── the selection: routed here, held next door, painted by the layout ──

    /**
     * A NOTIFICATION: a question the domain is not expected to answer. The grid
     * does not wait for it and does not mask for it, because nothing is pending
     * on it (ext6 law 229). It may still FAIL, though, and a lost failure is
     * worse than a slow one — so a thenable that rejects is recorded.
     */
    _notify(question) {
        if (!this._ask) return;
        var out;
        try { out = this._ask(question); }
        catch (e) { console.error("[RelGrid] ask threw:", e); return; }
        if (out && typeof out.then === "function")
            out.then(null, function (e) { console.error("[RelGrid] ask rejected:", e); });
    }

    /**
     * A QUESTION: the grid waits, and the person is stopped until it is
     * answered (ext6). The mask handle is the domain's way to draw while it
     * thinks; the delay timer is the grid's way to mask anyway if it does
     * not. apply(answer) runs when the answer comes — before the mask comes
     * down, because what it does may not wait on a hold.
     *
     * One at a time. The person cannot raise a second while locked, and a
     * host that does is refused, so no generation stamp is needed yet.
     */
    _askPending(question, apply) {
        if (!this._ask || this._pending) return false;
        var self = this;
        var session = { question: question, mounted: false, shownAt: 0, timer: null };
        this._pending = session;
        var handle = {
            /** The domain's canvas, minted on first call; null once the session is over. */
            panel: function () {
                if (self._pending !== session) return null;
                self._mount(session);
                return self._layout.openPanel();
            }
        };
        var out;
        try { out = this._ask(question, handle); }
        catch (e) {
            console.error("[RelGrid] ask threw:", e);
            this._settle(session, undefined, apply);
            return true;                       // the gesture was taken; the domain failed it
        }
        var p = (out && typeof out.then === "function") ? out : Promise.resolve(out);
        if (!session.mounted)
            session.timer = _hrgLater(function () {
                session.timer = null;
                if (self._pending === session) self._mount(session);
            }, _HRG_MASK_DELAY);
        p.then(function (answer) { self._settle(session, answer, apply); },
               function (e) {
                   console.error("[RelGrid] ask rejected:", e);
                   self._settle(session, undefined, apply);
               });
        return true;
    }

    /** The mask goes up: once per session, whether the domain asked or the clock did. */
    _mount(session) {
        if (session.mounted) return;
        session.mounted = true;
        session.shownAt = _hrgNow();
        if (session.timer) { _hrgCancel(session.timer); session.timer = null; }
        this._layout.openMask();
        this._layout.setMasked(true);
    }

    /**
     * The answer came — or did not. Apply first; then the mask, at once if it
     * has been up long enough and after the rest of the hold otherwise. A
     * settle after destroy, or for a session that is not the current one,
     * does nothing.
     */
    _settle(session, answer, apply) {
        if (this._destroyed || this._pending !== session) return;
        this._pending = null;
        if (session.timer) { _hrgCancel(session.timer); session.timer = null; }
        if (apply) {
            try { apply.call(this, answer); }
            catch (e) { console.error("[RelGrid] applying an answer threw:", e); }
        }
        if (!session.mounted) return;          // nothing to take down
        var self = this, up = _hrgNow() - session.shownAt;
        if (up >= _HRG_MASK_HOLD) this._unmask();
        else _hrgLater(function () { self._unmask(); }, _HRG_MASK_HOLD - up);
    }

    /** Down, unless a new question has taken the mask over in the meantime. */
    _unmask() {
        if (this._destroyed || this._pending) return;
        this._layout.closeMask();
        this._layout.setMasked(false);
        this._layout.focus();                  // the keyboard host takes the keys again
    }

    /** Locked while a cell is deep OR a question is pending — the two are exclusive (law 228). */
    _locked() { return this._deep || !!this._pending; }

    // ── copy: the selection, resolved, asked about, and the answer written ──

    /**
     * What is selected, as IDENTITIES: one block per range, in the order the
     * ranges were made, each the pks down its rows and the columns across it
     * in view order. Faithful — no bounding box, no merge, no refusal here.
     */
    _blocks() {
        var rects = this._selection.resolve(this._cursorPos), maps = this._maps, out = [];
        for (var k = 0; k < rects.length; k++) {
            var r = rects[k], pks = [], cols = [];
            for (var i = r.i0; i <= r.i1; i++) pks.push(maps.pkAt(i));
            for (var j = r.j0; j <= r.j1; j++) cols.push(maps.columnAt(j));
            out.push(new RelGridBlock(pks, cols));
        }
        return out;
    }

    /**
     * Ask what the selection is worth on a clipboard, and write the answer.
     * False when there is no channel, nothing presented, or the grid is
     * locked; true when the question was asked, whatever comes of it.
     */
    copy() {
        if (this._locked() || !this._ask || !this._cursorPos) return false;
        return this._askPending(new RelGridCopyRequested(this._blocks()), this._applyCopy);
    }

    /** The answer to a copy: content is written and reported; absence writes nothing (law 49). */
    _applyCopy(answer) {
        if (answer == null) return;
        if (!(answer instanceof RelGridClipboardContent)) {
            console.error("[RelGrid] a copy was answered with something that is not clipboard content:", answer);
            return;
        }
        var self = this, out;
        try { out = this._clipboard.write(answer); }
        catch (e) { console.error("[RelGrid] the clipboard write threw:", e); return; }
        var p = (out && typeof out.then === "function") ? out : Promise.resolve();
        p.then(function () {
            if (self._cbCopied) {
                try { self._cbCopied(answer); }
                catch (e) { console.error("[RelGrid] onCopied threw:", e); }
            }
        }, function (e) { console.error("[RelGrid] the clipboard write failed:", e); });
    }

    /** Ask what is selected, paint it, and tell the domain. The only reader of the list there is. */
    _afterSelection() {
        var rects = this._selection.resolve(this._cursorPos);
        this._layout.paintSelection(rects);
        if (!this._ask) return;                // nothing to tell, and nothing to build
        var ranges = [];
        for (var k = 0; k < rects.length; k++) {
            var r = rects[k];
            ranges.push(new RelGridRange(r.i0, r.j0, r.i1, r.j1));
        }
        this._notify(new RelGridSelectionChanged(ranges));
    }

    /** A bare move: the gesture means START OVER, so the list goes (law 40). */
    _bareMove(fn) {
        this._selection.clear();
        fn.call(this);
        this._afterSelection();
    }

    /**
     * Extension to a position. The cursor DOES NOT MOVE (law 39) — the anchor
     * is where the cursor already is, and what travels is the last range's far
     * corner.
     */
    _extendTo(i, j) {
        if (!this._cursorPos) return false;
        this._selection.extend({ i: this._clampI(i), j: this._clampJ(j) }, this._cursorPos);
        this._afterSelection();
        return true;
    }

    /** Shift+arrow: step the corner extension moves, from the cursor on the first one. */
    _extendBy(key) {
        if (!this._cursorPos) return;
        var from = this._selection.far() || this._cursorPos;
        var di = (key === "ArrowUp") ? -1 : (key === "ArrowDown") ? 1 : 0;
        var dj = (key === "ArrowLeft") ? -1 : (key === "ArrowRight") ? 1 : 0;
        this._extendTo(from.i + di, dj ? this._stepJ(from.i, from.j, dj) : from.j);
    }

    /**
     * The press action — what a click WOULD have done, performed early because
     * the pointer has started to travel. The three meanings are the click's
     * three, so a drag is only ever a click that kept going.
     */
    _press(i, j, mods) {
        if (mods.shift) return;                // the range extends from where it already is
        if (mods.ctrl) {
            this._selection.add({ i: i, j: j });
            this._setCursor(i, j);
            this._afterSelection();
            return;
        }
        this._bareMove(function () { this._setCursor(i, j); });
    }

    // ── capture: inert while deep ──────────────────────────────────────────

    /** A button went down on a slot. Nothing happens yet — a press that never
     *  travels is a click, and the click handler owns it. */
    _onDown(i, j, mods) {
        if (this._locked()) return;
        this._swallowClick = false;            // a fresh gesture; whatever the last one left, drop it
        this._pressAt = { i: i, j: j, mods: mods || {} };
        this._dragged = false;
    }

    /**
     * The pointer reached another slot while held. The FIRST such report turns
     * the press into a drag and performs the press action; every one after
     * moves the far corner. Blocked while deep, like every other selection
     * gesture.
     */
    _onDragTo(i, j) {
        if (this._locked() || !this._pressAt) return;
        if (!this._dragged) {
            this._dragged = true;
            this._press(this._pressAt.i, this._pressAt.j, this._pressAt.mods);
        }
        this._extendTo(i, j);
    }

    /**
     * Released. A drag is followed by a click — on the slot it started and
     * ended in, or on none at all — and that click must not undo what the drag
     * just made, so it is swallowed. The flag cannot outlive its gesture: the
     * next press clears it whether a click came or not.
     */
    _onDragEnd() {
        if (this._dragged) this._swallowClick = true;
        this._pressAt = null;
        this._dragged = false;
    }

    _onClick(i, j, mods) {
        if (this._swallowClick) { this._swallowClick = false; return; }
        if (this._locked()) return;            // the cell has the pointer and the keyboard, or an answer is owed
        mods = mods || {};
        // A shift-click is an extension TO where it landed; the other two mean
        // at the slot itself, which is exactly the press action.
        if (mods.shift) this._extendTo(i, j);
        else            this._press(i, j, mods);
    }

    _onDblClick(i, j) {
        if (this._locked()) return;
        // A bare move like any other. In a browser the click that precedes has
        // already cleared, but the double-click must not depend on that.
        this._bareMove(function () { this._setCursor(i, j); });
        this.takeControlAtCursor();
    }

    _onKey(e) {
        if (this._locked()) return;            // the keyboard is the cell's, or an answer is owed
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
        var arrow = (key === "ArrowUp" || key === "ArrowDown" || key === "ArrowLeft" || key === "ArrowRight");
        // Ctrl+A: the whole presented space, in one range. The cursor stays.
        if ((e.ctrlKey || e.metaKey) && (key === "a" || key === "A")) {
            this._selection.all(this._maps.rows(), this._maps.cols());
            this._afterSelection();
        }
        // Ctrl+C: the question. Consumed only when it was actually asked, so
        // a grid with no channel leaves the browser's own copy alone.
        else if ((e.ctrlKey || e.metaKey) && (key === "c" || key === "C")) {
            if (!this.copy()) return;
        }
        // Shift+arrow: extension. The cursor stays here too (law 39).
        else if (e.shiftKey && arrow) this._extendBy(key);
        else if (arrow) {
            var self = this;
            this._bareMove(function () {
                if      (key === "ArrowUp")    self._move(-1, 0);
                else if (key === "ArrowDown")  self._move(1, 0);
                else if (key === "ArrowLeft")  self._move(0, -1);
                else                           self._move(0, 1);
            });
        }
        else if (key === "Enter")      this.takeControlAtCursor();
        else return;                           // not ours; let it bubble
        if (e.preventDefault) e.preventDefault();
    }

    // ── deep: the grid hands control to the cell, and takes it back ────────

    /**
     * May the cursor's cell take control right now? The two refusals, in
     * order: the column's declared constraint, answered without touching the
     * cell at all, and then the cell's own judgement. Public, because a
     * feature must be able to ask WITHOUT opening anything.
     */
    mayTakeControlAtCursor() {
        var at = this._deepAt();
        return at ? this._mayTakeControl(at.id) : false;
    }

    _mayTakeControl(id) {
        if (this._readOnly.has(id.column)) return false;   // structural: never asked
        var entry = this._cells.get(id.pk, id.column);
        var cell = entry && entry.cell;
        if (!cell) return false;
        // Both halves or neither: a cell offering one is offered nothing.
        if (typeof cell.mayTakeControl !== "function" || typeof cell.takeControl !== "function") return false;
        var may;
        try { may = cell.mayTakeControl(); }
        catch (e) { console.error("[RelGrid] cell.mayTakeControl threw:", e); return false; }
        return may === true;                               // only an explicit yes
    }

    /**
     * Offer the cursor's cell control. Both stages must pass, and the cell
     * must answer the second with a thenable; the grid is deep until that
     * thenable SETTLES, resolved or rejected alike, because an edit that blew
     * up still ended.
     *
     * Nothing is marked deep until the cell has answered, so there is no
     * optimistic state to roll back — and a settle cannot arrive before the
     * bookkeeping below is done, because a thenable's callbacks never run in
     * the task that created it.
     */
    takeControlAtCursor() {
        if (this._locked() || !this._cursor) return false;
        var at = this._deepAt();
        if (!at) return false;
        var id = at.id;
        if (!this._mayTakeControl(id)) return false;
        var cell = this._cells.get(id.pk, id.column).cell;
        var host = this._layout.openOverlay(at.i, at.j);
        if (!host) return false;
        var out;
        try { out = cell.takeControl(host); }
        catch (e) {
            console.error("[RelGrid] cell.takeControl threw:", e);
            this._layout.closeOverlay();
            return false;
        }
        if (!out || typeof out.then !== "function") {
            // A contract violation, not a decline: it already said it may.
            // Recorded and refused, because waiting for a settle that cannot
            // come would leave the grid inert with nothing to show for it.
            console.error("[RelGrid] cell.takeControl must answer a thenable; got:", out);
            this._layout.closeOverlay();
            return false;
        }
        this._deep = true;
        this._layout.setDeep(true);
        this._tell(id, "deep");
        if (this._cbTaken) {
            try { this._cbTaken(id.pk, id.column); }
            catch (e) { console.error("[RelGrid] onControlTaken threw:", e); }
        }
        var self = this;
        out.then(function () { self._resume(id); },
                 function (e) {
                     console.error("[RelGrid] the cell's control ended in a rejection:", e);
                     self._resume(id);
                 });
        return true;
    }

    /** The cell handed control back. What happened inside is not the grid's. */
    _resume(id) {
        // A settle can arrive after the grid is gone, or after something else
        // has already resumed it. Neither may resurrect a dead grid or fire a
        // second report.
        if (this._destroyed || !this._deep) return;
        this._deep = false;
        this._layout.closeOverlay();           // the editor goes with the session
        this._layout.setDeep(false);
        this._tell(id, "shallow");
        this._layout.focus();                  // the keyboard host takes the keys again
        if (this._cbReleased) {
            try { this._cbReleased(id.pk, id.column); }
            catch (e) { console.error("[RelGrid] onControlReleased threw:", e); }
        }
    }

    // ── the surface ────────────────────────────────────────────────────────

    /** Programmatic shallow cursor; a position not presented is ignored. A
     *  BARE move, so it clears the list exactly as a click without a modifier does. */
    selectCell(pk, column) {
        if (this._locked()) return false;
        var at = this._maps.locate(pk, column);
        if (!at) return false;
        var moved = false, self = this;
        this._bareMove(function () { moved = self._setCursor(at.i, at.j); });
        return moved;
    }

    // ── the selection's surface: the gestures' API twins, and one read ─────

    /** Extend to an identity — the shift gesture. The cursor does not move. */
    extendSelection(pk, column) {
        if (this._locked()) return false;
        var at = this._maps.locate(pk, column);
        return at ? this._extendTo(at.i, at.j) : false;
    }

    /** Add a 1x1 at an identity and go there — the ctrl gesture. */
    addToSelection(pk, column) {
        if (this._locked()) return false;
        var at = this._maps.locate(pk, column);
        if (!at) return false;
        this._selection.add({ i: at.i, j: at.j });
        this._setCursor(at.i, at.j);
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

    /**
     * What is selected: the RESOLVED list of rectangles in the presented
     * space, so an empty list reads as the cursor's own 1x1 and no caller
     * needs a case for "nothing selected". Positions, because that is what a
     * range is; resolving them to identities is the business of whoever uses
     * them, and in this round nobody does.
     */
    selectedRanges()  { return this._selection.resolve(this._cursorPos); }
    /** How many ranges were actually MADE — zero when the selection is just the cursor. */
    selectionCount()  { return this._selection.count(); }

    cursor()   { return this._cursor ? { pk: this._cursor.pk, column: this._cursor.column } : null; }
    isDeep()   { return this._deep; }
    isPending() { return !!this._pending; }
    focus()    { this._layout.focus(); return this; }
    viewMaps() { return this._maps; }
    el()       { return this._layout.el(); }
    cells()    { return this._cells; }

    /** Detaches every cell and removes the table. Disposes nothing: the cells
     *  are the domain's, and the next grid over this relation may find them. */
    destroy() {
        this._destroyed = true;                // a late settle must not resume a dead grid
        if (this._pending && this._pending.timer) _hrgCancel(this._pending.timer);
        this._pending = null;
        this._layout.el().removeEventListener("keydown", this._keydown);
        this._cells.destroy();
        this._layout.destroy();
    }
}
