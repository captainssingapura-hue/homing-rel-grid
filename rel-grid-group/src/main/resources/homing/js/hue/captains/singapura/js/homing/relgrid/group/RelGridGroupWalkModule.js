// =============================================================================
// RelGridGroupWalkModule — RFC 0050 · Episode 2's ONE CURSOR for a group, and
// the walk between its stops.
//
// Every member keeps a cursor of its own — a table alone always has one — but
// a person's attention is in one place, so the group presents ONE: the ACTIVE
// member's. The active member is the one whose table last held the focus
// (observed at the root, never asked of the table), or the one activate()
// named; its box wears hrg-active, and every other member's box wears
// hrg-dormant, which paints neither a cursor nor a selection wash in it.
// Their state is untouched — the cursor and the ranges are still there — it
// is simply not shown until the member is active again.
//
// TAB walks the group: fence, table, fence, table, …, trailing fence, in
// order, and WRAPS within the group — Shift+Tab the other way. An unfilled
// fence, a folded member's table and a table with nothing to present are not
// stops. A fence stop is the FENCE ITSELF: it takes the focus and wears the
// cursor — the same outline a cell wears, so the eye follows one mark down
// the group — and every member is dormant while it does. Enter on a fence
// presses its first control, a fold toggle say, so no control needs a focus
// ring of its own; a table stop makes that member active and gives its table
// the focus.
//
// ARROWS step over an edge. A table reports a bare arrow that went nowhere
// (onEdge); the group then moves ONE stop, up or down, without wrapping:
// from the top row, up to the fence above; from the bottom row, down to the
// fence below. From a fence, down enters the table below on its FIRST row
// and up the table above on its LAST, in the column the cursor left — the
// table's own selectCell, by identity. Nothing here reaches into a table.
//
//   new RelGridGroupWalk({ root, members, fences, folded })    members/fences: the group's lists, live
//   listen() / unlisten()          the root's focusin and keydown
//   activate(id, focus) / active()
//   step(dir, wrap)                one stop on or back; false when there is nowhere to go
//   paintDormancy()                after the members exist
// =============================================================================

/** Is el inside ancestor (or it)? The stub's elements have no contains(). */
function _hrggWithin(el, ancestor) {
    for (var p = el; p; p = p.parentNode) if (p === ancestor) return true;
    return false;
}
/** Press a control as a click would — the DOM's click, or the stub's dispatch. */
function _hrggPress(el) {
    if (typeof el.click === "function") el.click();
    else if (typeof el.dispatch === "function") el.dispatch("click", {});
}
/** The first control in a fence — a button, a link, anything focusable — or null. Walks: no querySelector needed. */
function _hrggFirstControl(host) {
    var kids = host.children || [];
    for (var k = 0; k < kids.length; k++) {
        var el = kids[k], tag = String(el.tagName || "").toLowerCase();
        if (tag === "button" || tag === "input" || tag === "select" || tag === "textarea" || tag === "a"
            || (el.getAttribute && el.getAttribute("tabindex") !== null)) return el;
        var deeper = _hrggFirstControl(el);
        if (deeper) return deeper;
    }
    return null;
}

class RelGridGroupWalk {

    constructor(opts) {
        var self = this;
        this._root = opts.root;
        this._members = opts.members;             // [{ id, box, grid }] — the group's, in order
        this._fences = opts.fences;               // [{ id, host, cell }] — N+1, the last id null
        this._folded = opts.folded;               // (id) → boolean
        this._activeId = null;                    // the member whose cursor is the group's
        this._cursorFence = null;                 // the fence wearing the cursor, while one does
        this._lastColumn = null;                  // the column the cursor last left a table in
        this._onFocusIn = function (e) {
            var t = e && e.target;
            for (var k = 0; k < self._members.length; k++)
                if (t && _hrggWithin(t, self._members[k].box)) { self.activate(self._members[k].id, false); return; }
            for (var q = 0; q < self._fences.length; q++)
                if (t && _hrggWithin(t, self._fences[q].host)) { self._cursorOnFence(self._fences[q]); return; }
        };
        this._onKeyDown = function (e) {
            if (!e || e.altKey || e.ctrlKey || e.metaKey) return;
            if (e.key === "Tab") {
                if (self.step(e.shiftKey ? -1 : 1, true)) { if (e.preventDefault) e.preventDefault(); }
                return;
            }
            // Enter on a fence presses its first control — the fence is the stop,
            // the control never needs the focus.
            if (e.key === "Enter" && self._onFence(e.target)) {
                var f = self._fenceAt(e.target), control = f ? _hrggFirstControl(f.host) : null;
                if (control) { _hrggPress(control); if (e.preventDefault) e.preventDefault(); }
                return;
            }
            // An arrow pressed ON A FENCE steps one stop. Judged by where the key was
            // pressed, not where the focus is now: a table's own arrow may already have
            // moved the focus to a fence, through its edge report, on this same key.
            if ((e.key === "ArrowDown" || e.key === "ArrowUp") && !e.shiftKey && self._onFence(e.target)) {
                if (self.step(e.key === "ArrowDown" ? 1 : -1, false)) { if (e.preventDefault) e.preventDefault(); }
            }
        };
    }

    listen()   { this._root.addEventListener("focusin", this._onFocusIn); this._root.addEventListener("keydown", this._onKeyDown); }
    unlisten() { this._root.removeEventListener("focusin", this._onFocusIn); this._root.removeEventListener("keydown", this._onKeyDown); }

    active() { return this._activeId; }

    _entry(id) {
        for (var k = 0; k < this._members.length; k++) if (this._members[k].id === id) return this._members[k];
        return null;
    }
    _fenceAt(el) {
        for (var k = 0; k < this._fences.length; k++) if (_hrggWithin(el, this._fences[k].host)) return this._fences[k];
        return null;
    }
    _onFence(el) { return !!el && this._fenceAt(el) !== null; }

    /** ONE CURSOR, painted: every member but the active one is dormant, and every member is while a fence is the stop. */
    paintDormancy() {
        for (var k = 0; k < this._members.length; k++) {
            var m = this._members[k];
            css.toggleClass(m.box, hrg_dormant, this._cursorFence !== null || m.id !== this._activeId);
        }
    }

    /** The cursor is on a fence: it wears the mark, and no member shows one. */
    _cursorOnFence(f) {
        if (this._cursorFence === f) return;
        if (this._cursorFence) css.removeClass(this._cursorFence.host, hrg_fence_cursor);
        this._cursorFence = f;
        css.addClass(f.host, hrg_fence_cursor);
        css.addClass(this._root, hrg_on_fence);
        this.paintDormancy();
    }
    _cursorOffFence() {
        if (!this._cursorFence) return;
        css.removeClass(this._cursorFence.host, hrg_fence_cursor);
        css.removeClass(this._root, hrg_on_fence);
        this._cursorFence = null;
        this.paintDormancy();
    }

    /** Mark the active member; with focus, give its table the focus and bring its box into view. */
    activate(id, focus) {
        var m = this._entry(id);
        if (!m) return false;
        this._cursorOffFence();                                 // a member's cursor is the group's again
        if (this._activeId !== id) {
            var was = this._entry(this._activeId);
            if (was) css.removeClass(was.box, hrg_active);
            css.addClass(m.box, hrg_active);
            this._activeId = id;
            this.paintDormancy();
        }
        if (focus) {
            if (m.box.scrollIntoView) { try { m.box.scrollIntoView({ block: "nearest" }); } catch (e) { /* headless */ } }
            m.grid.focus();
        }
        return true;
    }

    /** The stops Tab walks, in order: a filled fence, an unfolded member's table, … the trailing fence. */
    _stops() {
        var out = [];
        for (var k = 0; k < this._members.length; k++) {
            var f = this._fences[k], m = this._members[k];
            if (f.cell) out.push({ el: f.host, fence: f });
            if (!this._folded(m.id) && m.grid.viewMaps().rows() > 0) out.push({ el: m.box, member: m });
        }
        var last = this._fences[this._fences.length - 1];
        if (last.cell) out.push({ el: last.host, fence: last });
        return out;
    }

    /**
     * Move the focus one stop on (+1) or back (-1). Tab wraps within the
     * group; an arrow stops at the ends. Entering a table from above puts its
     * cursor on the first row, from below on the last, in the column the
     * cursor last left a table in — a table's own verb, by identity. False
     * when there is nowhere to go.
     */
    step(dir, wrap) {
        var stops = this._stops();
        if (!stops.length || typeof document === "undefined") return false;
        var at = -1, active = document.activeElement || null;
        for (var k = 0; k < stops.length; k++) if (active && _hrggWithin(active, stops[k].el)) { at = k; break; }
        // Leaving a table: remember the column, so the next table is entered in it.
        if (at >= 0 && stops[at].member) {
            var cur = stops[at].member.grid.cursor();
            if (cur) this._lastColumn = cur.column;
        }
        var next;
        if (at < 0) next = (dir > 0 ? 0 : stops.length - 1);
        else if (wrap) next = (at + dir + stops.length) % stops.length;
        else { next = at + dir; if (next < 0 || next >= stops.length) return false; }
        var stop = stops[next];
        if (stop.member) {
            if (!wrap) this._enter(stop.member, dir > 0 ? "first" : "last");
            return this.activate(stop.member.id, true);
        }
        // A fence stop is the fence itself: it takes the focus and the cursor.
        this._cursorOnFence(stop.fence);
        var host = stop.fence.host;
        if (host.focus) { try { host.focus({ preventScroll: false }); } catch (e) { host.focus(); } }
        return true;
    }

    /** Put a member's cursor on its first or last row, in the column last left when it has it. */
    _enter(m, row) {
        var maps = m.grid.viewMaps(), rows = maps.rows();
        if (rows === 0) return;
        var i = (row === "first") ? 0 : rows - 1;
        var j = (this._lastColumn !== null) ? maps.colOf(this._lastColumn) : -1;
        if (j < 0) { var cur = m.grid.cursor(); j = cur ? maps.colOf(cur.column) : 0; }
        if (j < 0) j = 0;
        var id = maps.resolve(i, j);
        if (id) m.grid.selectCell(id.pk, id.column);
    }
}
