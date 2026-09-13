// =============================================================================
// RelGridGroupModule — RFC 0050 · Episode 2's GROUP: an ordered list of tables
// stacked down one column, each an ordinary RelGrid THAT DOES NOT KNOW IT IS
// IN ONE. The federation (E2-ext2) re-read now that the table has the public
// verbs ext2 thought were missing: a group keeps its members agreeing through
// the surface every host already uses, and reaches into no member's layout.
//
//   new RelGridGroup({
//       container,          // where the group mounts
//       branch,             // the group's OWN branch (DomOpsParty): its boxes and fence slots
//                           // are minted on it, every member's grid gets a sub-branch of it,
//                           // and dissolving it is the host's
//       members: [{         // in order, top to bottom
//           id,             // the member's IDENTITY — unique in the group, never empty
//           grid,           // the ordinary RelGrid options this member is built from:
//                           // its relation, its ask, its callbacks. Verbatim, but for the
//                           // container (the group's box), the branch (the group's gift)
//                           // and what is shared.
//           fence?          // the cell for the slot ABOVE this member (see fences)
//       }, ...],
//       fence?,             // the cell for the trailing slot, below the last member
//       columnWidths?,      // the widths every member starts at — the group's, not any member's
//       header?,            // 'group' (default): ONE header, the group's, at the top above every
//                           // fence, and no member shows its own; 'each': every member keeps
//                           // its own header option, and the group adds none
//       folded?,            // the ids folded at first — [] by default
//       onColumnResized?,   // (column, px) — ONE report per change, however many members moved
//       onFolded?,          // (id, folded) — a REPORT: a member's box was folded or unfolded
//       label?              // aria-label on the group
//   });
//
// MEMBERS ARE IDENTITIES. A member is addressed by its id and nothing else;
// the group's own state is which members show and in what order, and one
// day which are folded — a view over members, as a table's is a view over
// rows. Nothing the group does is ever applied to a member's rows: the
// member's cursor, selection, copy, handover and the domain its ask reaches
// are its own, wired exactly as they would be for a table standing alone.
// The group observes; it does not route. (A member may have no rows at all —
// a relation with nothing to present — and still have its identity and its
// fence: that is how an illustration stands between two tables.)
//
// FENCES. Between every two members, and around the ends, the group mints a
// SLOT — N+1 of them for N members, each addressed by the member below it
// and the last by the group — and PLACES the domain's fence in it the way a
// table places a cell's element in a slot: fenceElement() once, and
// dispose() is the owner's. What is in it is the domain's — a title, a
// total, an illustration, a control — minted on the domain's own branch, and
// a slot nobody fills takes no height. The group knows no caption; it knows
// a slot.
//
// FOLD is the group's own state: which members show their table. A folded
// member's BOX is hidden — its fence stays — and the table inside is
// untouched: cursor, selection, cells, view, all as they were, and it never
// learns. fold(id, on), foldAll(on) and folded(id) are the host's verbs;
// onFolded(id, on) is the report; and every fence with an onFolded is told
// when the member below it folds or unfolds, by whatever road.
//
// ONE CURSOR. Every member keeps a cursor of its own — a table alone always
// has one — but a person's attention is in one place, so the group presents
// ONE: the ACTIVE member's. The active member is the one whose table last
// held the focus (observed, never asked of the table), or the one activate()
// named; its box wears hrg-active, and the group's sheet paints neither a
// cursor nor a selection wash in the others. Their state is untouched — the
// cursor and the ranges are still there — it is simply not shown until the
// member is active again. The first member is active at first.
//
// TAB walks the group: fence, table, fence, table, …, trailing fence, in
// order, and WRAPS within the group — Shift+Tab the other way. An unfilled
// fence, a folded member's table and a table with nothing to present are not
// stops. A fence stop is the FENCE ITSELF: it takes the focus and wears the
// cursor — the same outline a cell wears, so the eye follows one mark down
// the group — and the active member's cursor is not shown while it does.
// Enter on a fence presses its first control, a fold toggle say, so no
// control needs a focus ring of its own; a table stop makes that member
// active and gives its table the focus.
//
// ARROWS step over an edge. A table reports a bare arrow that went nowhere
// (onEdge — the one thing the table gained for groups, and a report a table
// alone may want too); the group then moves ONE stop, up or down, without
// wrapping: from the top row, up to the fence above; from the bottom row,
// down to the fence below. From a fence, down enters the table below on its
// FIRST row and up the table above on its LAST, in the column the cursor
// left — the table's own selectCell, by identity. Tab is the same walk a
// stop at a time from wherever the focus is, so it is the fast-forward.
//
// TELL is the channel's other direction. The grid asks and the domain
// answers; here the domain SAYS, unasked: a control the domain drew in a
// fence was pressed. tell(message) on the group carries a protocol value the
// same way an answer does; a fence has no handle and no way to the group of
// its own — the HOST wires a closure onto tell into the fence it builds, so
// the domain talks to the host and the host to the group. RelGridGroupFold
// is the first kind; an unknown one is recorded and refused, never dropped.
//
// WHAT IS SHARED, AND HOW. Column geometry is the one thing separate tables
// cannot agree on by themselves, so it is the group's: it applies its widths
// to every member at construction, hears any member's resize report, applies
// the change to the siblings through setColumnWidth, and reports ONCE. A
// sibling that refuses — locked, because a cell of its holds control or an
// answer is owed — is brought level the moment it is free again, on its own
// next report. Every member's own onColumnResized still fires — it is that
// member's truthful report of its own geometry — and the group's fires once
// for all of them.
//
// THE GROUP'S HEADER (header: 'group') is a table of its own at the very
// top, above the first fence: a grid over the members' columns with nothing
// to present — the illustration's trick again — whose header band is the
// group's, resize handles and all, and whose widths are levelled with the
// rest. Every member is then built with no header of its own. Its labels
// are the first member's; its columns must be every member's, checked once
// at construction. With header: 'each' the group adds no table and every
// member keeps whatever header its own options say.
//
// WHAT THE TABLE GAINED FOR THIS: one option, resizeGuide — what a header
// drag's guide line spans, which a host stacking tables wants to be each
// table's box, a segment apiece, so the line breaks at the fences. Every
// verb here existed already.
// =============================================================================

var _HRGG_STYLE_ID = "homing-rel-grid-group-style";
var _HRGG_STYLE_CSS = [
    ".hrg-group{display:flex;flex-direction:column;align-items:stretch;}",
    ".hrg-member{flex:0 0 auto;}",
    ".hrg-group-header{flex:0 0 auto;}",
    ".hrg-fence{flex:0 0 auto;}",
    ".hrg-fence.hrg-fence-empty{display:none;}",
    // A folded member: its box hidden, its table inside untouched; the fence
    // above it wears the fact, for a domain that draws its control from it.
    ".hrg-member.hrg-folded{display:none;}",
    // One cursor: a member that is not active shows neither its cursor nor
    // its selection — ext3's predicates, which are published to be styled by.
    ".hrg-group .hrg-member:not(.hrg-active) .hrg-td.hrg-cursor,",
    ".hrg-group .hrg-member:not(.hrg-active) .hrg-merge.hrg-cursor{outline-color:transparent;}",
    ".hrg-group .hrg-member:not(.hrg-active) .hrg-td.hrg-sel,",
    ".hrg-group .hrg-member:not(.hrg-active) .hrg-merge.hrg-sel{background:transparent;}",
    // A fence that is the Tab stop itself (no control of its own to land on).
    // A fence that is the cursor wears the cell's mark: dimmed at rest, full
    // accent with the focus, as a slot's cursor is; and while a fence is the
    // cursor no member shows one.
    ".hrg-fence{outline:none;}",
    ".hrg-fence.hrg-fence-cursor{outline:2px solid color-mix(in srgb, var(--color-accent) 45%, var(--color-border));outline-offset:-2px;transition:outline-color .18s ease;}",
    ".hrg-fence.hrg-fence-cursor:focus{outline-color:var(--color-accent);}",
    ".hrg-group.hrg-on-fence .hrg-member .hrg-td.hrg-cursor,",
    ".hrg-group.hrg-on-fence .hrg-member .hrg-merge.hrg-cursor{outline-color:transparent;}",
    ".hrg-group.hrg-on-fence .hrg-member .hrg-td.hrg-sel,",
    ".hrg-group.hrg-on-fence .hrg-member .hrg-merge.hrg-sel{background:transparent;}"
].join("\n");
var _hrggStyled = false;

function _hrggEnsureStyles() {
    if (_hrggStyled || typeof document === "undefined" || !document.head) return;
    _hrggStyled = true;
    var s = document.createElement("style");
    s.id = _HRGG_STYLE_ID;
    s.textContent = _HRGG_STYLE_CSS;
    document.head.appendChild(s);
}

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

function _hrggAddClass(el, c) {
    var parts = el.className ? el.className.split(" ") : [];
    if (parts.indexOf(c) < 0) el.className = parts.concat(c).join(" ");
}
function _hrggRemoveClass(el, c) {
    var parts = el.className ? el.className.split(" ") : [], kept = [];
    for (var k = 0; k < parts.length; k++) if (parts[k] !== c) kept.push(parts[k]);
    el.className = kept.join(" ");
}

/** A copy of a plain options object — the member's spec is the host's; the group amends its own copy. */
function _hrggCopy(o) {
    var out = {};
    if (o) for (var k in o) if (Object.prototype.hasOwnProperty.call(o, k)) out[k] = o[k];
    return out;
}

class RelGridGroup {

    constructor(opts) {
        opts = opts || {};
        if (!opts.container) throw new Error("[RelGridGroup] opts.container is required");
        if (!opts.branch) throw new Error("[RelGridGroup] opts.branch is required");
        if (!Array.isArray(opts.members) || opts.members.length === 0)
            throw new Error("[RelGridGroup] opts.members must name at least one member");
        _hrggEnsureStyles();
        var self = this;
        this._cbResized = opts.onColumnResized || null;
        this._cbFolded = opts.onFolded || null;
        this._headerMode = (opts.header === "each") ? "each" : "group";
        if (opts.header !== undefined && opts.header !== "each" && opts.header !== "group")
            throw new Error("[RelGridGroup] header must be 'group' or 'each', not " + JSON.stringify(opts.header));
        this._folded = {};                                      // id → true while folded
        var f0 = opts.folded || [];
        for (var i = 0; i < f0.length; i++) this._folded[f0[i]] = true;
        this._widths = _hrggCopy(opts.columnWidths);          // the group's, identity-keyed
        this._broadcasting = false;                             // a report of the group's own making
        this._destroyed = false;
        this._branch = opts.branch;                             // the group's own; the host dissolves it
        this._grids = [];                                       // the sub-branches the members' grids were given

        this._root = this._branch.createElement("root", "div");
        this._root.className = "hrg-group";
        if (opts.label) this._root.setAttribute("aria-label", opts.label);
        this._members = [];                                     // { id, box, grid, spec }, in order
        this._fences = [];                                      // N+1 of { id, host, cell }; the last id is null
        this._boxes = [];                                       // the boxes a drag's guide runs down: the header's, then every member's
        this._header = null;                                    // { box, grid } — the group's own header, in 'group' mode
        this._activeId = null;                                  // the member whose cursor is the group's
        this._lastColumn = null;                                // the column the cursor last left a table in

        // The specs are checked whole before anything is minted: an id each, unique, and grid options.
        var seen = {};
        for (var v = 0; v < opts.members.length; v++) {
            var mv = opts.members[v] || {};
            if (mv.id === undefined || mv.id === null || mv.id === "")
                throw new Error("[RelGridGroup] member " + v + " has no id");
            if (seen[mv.id]) throw new Error("[RelGridGroup] duplicate member id: " + mv.id);
            seen[mv.id] = true;
            if (!mv.grid) throw new Error("[RelGridGroup] member '" + mv.id + "' has no grid options");
        }
        if (this._headerMode === "group") this._header = this._mintHeader(opts.members);
        for (var k = 0; k < opts.members.length; k++) {
            var m = opts.members[k];
            this._fences.push(this._mintFence(m.id, m.fence || null, k));
            this._members.push(this._mintMember(m, k));
            if (this._folded[m.id]) this._paintFold(m.id, true);   // folded at first: the box hidden before it is seen
        }
        this._fences.push(this._mintFence(null, opts.fence || null, opts.members.length));
        for (var fk in this._folded) if (!seen[fk]) delete this._folded[fk];   // drift: an id that is no member is dropped
        // One cursor: the member whose table holds the focus is the active one,
        // observed at the root; Tab walks the stops. Neither reaches into a table.
        this._cursorFence = null;                               // the fence wearing the cursor, while one does
        this._onFocusIn = function (e) {
            var t = e && e.target;
            for (var k = 0; k < self._members.length; k++)
                if (t && _hrggWithin(t, self._members[k].box)) { self._activate(self._members[k].id, false); return; }
            for (var q = 0; q < self._fences.length; q++)
                if (t && _hrggWithin(t, self._fences[q].host)) { self._cursorOnFence(self._fences[q]); return; }
        };
        this._onKeyDown = function (e) {
            if (!e || e.altKey || e.ctrlKey || e.metaKey) return;
            if (e.key === "Tab") {
                if (self._step(e.shiftKey ? -1 : 1, true)) { if (e.preventDefault) e.preventDefault(); }
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
                if (self._step(e.key === "ArrowDown" ? 1 : -1, false)) { if (e.preventDefault) e.preventDefault(); }
            }
        };
        this._root.addEventListener("focusin", this._onFocusIn);
        this._root.addEventListener("keydown", this._onKeyDown);
        this._activate(this._members[0].id, false);
        opts.container.appendChild(this._root);

        // Every member starts at the group's widths. Done after all are built,
        // so a member's report while it is being levelled finds its siblings.
        this._level(null);
    }

    // ── minting ────────────────────────────────────────────────────────────

    /**
     * The slot above a member (or the trailing one): the group's box, with the
     * domain's fence PLACED in it once it has one — fenceElement(), the noun,
     * asked once. The fence is handed nothing and reads nothing.
     */
    _mintFence(id, cell, k) {
        var host = this._branch.createElement("fence-" + k, "div");   // by position: an id is the domain's, and any string
        host.className = "hrg-fence" + (cell ? "" : " hrg-fence-empty");
        host.setAttribute("tabindex", "-1");                    // a Tab stop by the group's hand, not the browser's
        if (id !== null) host.setAttribute("data-member", String(id));
        this._root.appendChild(host);
        if (cell) {
            if (typeof cell.fenceElement !== "function")
                throw new Error("[RelGridGroup] a fence cell must answer fenceElement()" + (id !== null ? " — member '" + id + "'" : ""));
            var el = cell.fenceElement();
            if (!el || typeof el !== "object" || typeof el.appendChild !== "function")
                throw new Error("[RelGridGroup] fenceElement() must answer an element" + (id !== null ? " — member '" + id + "'" : ""));
            host.appendChild(el);
        }
        return { id: id, host: host, cell: cell };
    }

    /**
     * A member: its box, and the grid built from its own options — verbatim,
     * but for the container, the header when it is the group's, and the
     * reports the group listens to, which still reach the member's own host.
     */
    /**
     * The group's own header: a grid over the members' columns that presents
     * nothing, built first so it sits above the first fence. Its header band
     * carries the labels and the resize handles; a drag on it is a member's
     * drag as far as the group is concerned — held, levelled, reported once.
     * The columns are checked here: one header means one column list.
     */
    _mintHeader(specs) {
        var self = this, first = specs[0] || {}, spec = first.grid || {};
        var relation = spec.relation;
        if (!relation || typeof relation.columns !== "function")
            throw new Error("[RelGridGroup] member '" + first.id + "' has no relation to take the header's columns from");
        var columns = relation.columns().slice();
        for (var k = 1; k < specs.length; k++) {
            var r = specs[k] && specs[k].grid && specs[k].grid.relation, cols = (r && typeof r.columns === "function") ? r.columns() : null;
            if (!cols || cols.join("\u0000") !== columns.join("\u0000"))
                throw new Error("[RelGridGroup] one header means one column list: member '" + (specs[k] && specs[k].id)
                              + "' declares " + JSON.stringify(cols) + ", member '" + first.id + "' " + JSON.stringify(columns));
        }
        var box = this._branch.createElement("header", "div");
        box.className = "hrg-group-header";
        this._root.appendChild(box);
        this._boxes.push(box);
        var grid = new RelGrid({
            container: box,
            branch: this._gridBranch("header"),                 // never asked for a cell: nothing is presented
            relation: {
                pks:     function () { return []; },
                columns: function () { return columns.slice(); },
                cellFor: function (pk, col) { throw new Error("[RelGridGroup] the header presents no cell (" + pk + ", " + col + ")"); }
            },
            header: { show: true, labels: (spec.header && spec.header.labels) || {} },
            columnView: spec.columnView || null,
            minColumnWidth: spec.minColumnWidth,
            resizeGuide: this._boxes,
            label: "header",
            onColumnResized: function (column, px) { self._onMemberResized(null, column, px); },
            onArranged: function () { self._level(null); }
        });
        return { box: box, grid: grid };
    }

    /** A sub-branch of the group's for one grid to own, remembered so destroy() dissolves it. */
    _gridBranch(name) {
        var b = this._branch.createBranch("grid-" + name);
        b.activate(this);
        this._grids.push(b);
        return b;
    }

    _mintMember(m, k) {
        var self = this, id = m.id, spec = m.grid;
        var box = this._branch.createElement("member-" + k, "div");
        box.className = "hrg-member";
        box.setAttribute("data-member", String(id));
        this._root.appendChild(box);
        this._boxes.push(box);
        var g = _hrggCopy(spec);
        g.container = box;
        g.branch = this._gridBranch(String(k));                 // the member's grid owns a sub-branch of the group's
        if (this._headerMode === "group") g.header = { show: false };   // the header is the group's, above
        g.resizeGuide = this._boxes;                            // a drag's guide: a segment down the header and each member, none across a fence
        g.onColumnResized = function (column, px) {
            self._onMemberResized(id, column, px);
            if (spec.onColumnResized) spec.onColumnResized(column, px);
        };
        // A member that refused a width while locked is levelled when it is
        // free again — the reports that mark that are its own, passed on.
        g.onControlReleased = function (pk, column) {
            if (spec.onControlReleased) spec.onControlReleased(pk, column);
            self._level(id);
        };
        g.onArranged = function (kind) {
            if (spec.onArranged) spec.onArranged(kind);
            self._level(id);
        };
        // The cursor met this table's edge: step over it, one stop, no wrap.
        g.onEdge = function (direction) {
            if (spec.onEdge) spec.onEdge(direction);
            if (direction === "up") self._step(-1, false);
            else if (direction === "down") self._step(1, false);
        };
        var entry = { id: id, box: box, grid: null, spec: spec };
        entry.grid = new RelGrid(g);
        return entry;
    }

    // ── the one shared thing: column geometry ──────────────────────────────

    /** A member — or the group's header, id null — moved a column. Hold it, level the rest, report once. */
    _onMemberResized(id, column, px) {
        if (this._broadcasting || this._destroyed) return;
        this._widths[column] = px;
        this._broadcasting = true;
        try {
            if (this._header && id !== null) this._header.grid.setColumnWidth(column, px);
            for (var k = 0; k < this._members.length; k++)
                if (this._members[k].id !== id) this._members[k].grid.setColumnWidth(column, px);
        } finally { this._broadcasting = false; }
        if (this._cbResized) {
            try { this._cbResized(column, px); }
            catch (e) { console.error("[RelGridGroup] onColumnResized threw:", e); }
        }
    }

    /** Bring one member (or all, the header too) to the group's widths. Idempotent; silent while locked. */
    _level(id) {
        if (this._broadcasting || this._destroyed) return;
        this._broadcasting = true;
        try {
            if (this._header && id === null) this._header.grid.setColumnWidths(this._widths);
            for (var k = 0; k < this._members.length; k++)
                if (id === null || this._members[k].id === id) this._members[k].grid.setColumnWidths(this._widths);
        } finally { this._broadcasting = false; }
    }

    // ── one cursor: the active member, and Tab between the stops ───────────

    /** The cursor is on a fence: it wears the mark, and no member shows one. */
    _cursorOnFence(f) {
        if (this._cursorFence === f) return;
        if (this._cursorFence) _hrggRemoveClass(this._cursorFence.host, "hrg-fence-cursor");
        this._cursorFence = f;
        _hrggAddClass(f.host, "hrg-fence-cursor");
        _hrggAddClass(this._root, "hrg-on-fence");
    }
    _cursorOffFence() {
        if (!this._cursorFence) return;
        _hrggRemoveClass(this._cursorFence.host, "hrg-fence-cursor");
        _hrggRemoveClass(this._root, "hrg-on-fence");
        this._cursorFence = null;
    }
    _fenceAt(el) {
        for (var k = 0; k < this._fences.length; k++) if (_hrggWithin(el, this._fences[k].host)) return this._fences[k];
        return null;
    }

    /** Mark the active member; with focus, give its table the focus and bring its box into view. */
    _activate(id, focus) {
        var m = this._entry(id);
        if (!m) return false;
        this._cursorOffFence();                                 // a member's cursor is the group's again
        if (this._activeId !== id) {
            var was = this._entry(this._activeId);
            if (was) _hrggRemoveClass(was.box, "hrg-active");
            _hrggAddClass(m.box, "hrg-active");
            this._activeId = id;
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
            if (!this._folded[m.id] && m.grid.viewMaps().rows() > 0) out.push({ el: m.box, member: m });
        }
        var last = this._fences[this._fences.length - 1];
        if (last.cell) out.push({ el: last.host, fence: last });
        return out;
    }

    /** Is this element in one of the fences? */
    _onFence(el) {
        if (!el) return false;
        for (var k = 0; k < this._fences.length; k++) if (_hrggWithin(el, this._fences[k].host)) return true;
        return false;
    }

    /**
     * Move the focus one stop on (+1) or back (-1). Tab wraps within the
     * group; an arrow stops at the ends. Entering a table from above puts its
     * cursor on the first row, from below on the last, in the column the
     * cursor last left a table in — a table's own verb, by identity. False
     * when there is nowhere to go.
     */
    _step(dir, wrap) {
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
            return this._activate(stop.member.id, true);
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

    /** The member whose cursor is the group's. */
    active() { return this._activeId; }

    /** Make a member the active one and give its table the focus. False for an id that is no member. */
    activate(id) { return this._activate(id, true); }

    // ── fold: the group's own state, applied to a member's box ─────────────

    /** The box hidden or shown, and the fence above it marked; the table inside is not touched. */
    _paintFold(id, on) {
        var m = this._entry(id), f = this._fenceOf(id);
        if (!m) return;
        if (on) { _hrggAddClass(m.box, "hrg-folded"); if (f) _hrggAddClass(f.host, "hrg-fence-folded"); }
        else    { _hrggRemoveClass(m.box, "hrg-folded"); if (f) _hrggRemoveClass(f.host, "hrg-fence-folded"); }
    }

    /**
     * Fold or unfold a member. Idempotent — the same state again is nothing,
     * no report — and every road lands here: the host's verb, foldAll, and a
     * fence's tell. The fence above the member is told, if it listens; the
     * host is told once. An unknown member is a mistake and throws.
     */
    fold(id, folded) {
        if (!this._entry(id)) throw new Error("[RelGridGroup] fold: no member '" + id + "'");
        var on = folded !== false;
        if ((this._folded[id] === true) === on) return false;
        if (on) this._folded[id] = true; else delete this._folded[id];
        this._paintFold(id, on);
        var f = this._fenceOf(id);
        if (f && f.cell && typeof f.cell.onFolded === "function") {
            try { f.cell.onFolded(on); }
            catch (e) { console.error("[RelGridGroup] fence.onFolded threw:", e); }
        }
        if (this._cbFolded) {
            try { this._cbFolded(id, on); }
            catch (e) { console.error("[RelGridGroup] onFolded threw:", e); }
        }
        return true;
    }

    /** Every member at once — one report per member that actually changed. */
    foldAll(folded) {
        for (var k = 0; k < this._members.length; k++) this.fold(this._members[k].id, folded);
        return this;
    }

    folded(id) { return this._folded[id] === true; }

    // ── tell: the channel's other direction ────────────────────────────────

    /**
     * The domain saying, unasked. A protocol value, dispatched by kind — a
     * RelGridGroupFold folds — and applied as the host's verb would be; an
     * unknown kind is recorded and refused, never dropped. Answers whether
     * anything changed.
     */
    tell(message) {
        if (this._destroyed) return false;
        if (message instanceof RelGridGroupFold) {
            if (!this._entry(message.member)) {
                console.error("[RelGridGroup] told to fold a member that is not here:", message.member);
                return false;
            }
            return this.fold(message.member, message.folded);
        }
        console.error("[RelGridGroup] told something it does not understand:", message);
        return false;
    }

    /** The group's widths — applied to every member; the snapshot a host may keep. */
    setColumnWidths(widths) {
        if (!widths) return this;
        for (var c in widths) if (Object.prototype.hasOwnProperty.call(widths, c)) this._widths[c] = widths[c];
        this._level(null);
        // What is held is what the members accepted — bounded by them, not here —
        // read back from any member that was free to accept it.
        for (var k = 0; k < this._members.length; k++) {
            var g = this._members[k].grid;
            if (!g.isDeep() && !g.isPending()) { this._widths = g.columnWidths(); break; }
        }
        return this;
    }

    columnWidths() { return _hrggCopy(this._widths); }

    // ── members ────────────────────────────────────────────────────────────

    /** The member ids, in order. */
    members() {
        var out = [];
        for (var k = 0; k < this._members.length; k++) out.push(this._members[k].id);
        return out;
    }

    /** A member's grid — the ordinary RelGrid, for a host that must reach it. */
    member(id) {
        var m = this._entry(id);
        return m ? m.grid : null;
    }

    _entry(id) {
        for (var k = 0; k < this._members.length; k++) if (this._members[k].id === id) return this._members[k];
        return null;
    }
    _fenceOf(id) {
        for (var k = 0; k < this._fences.length; k++) if (this._fences[k].id === id) return this._fences[k];
        return null;
    }

    /** A fence's host: the slot above the member, or the trailing one for null. */
    fence(id) {
        for (var k = 0; k < this._fences.length; k++)
            if (this._fences[k].id === (id === undefined ? null : id)) return this._fences[k].host;
        return null;
    }

    el() { return this._root; }

    /**
     * Destroys every member's grid and the header's, dissolves the branches
     * they were given, and removes the group. Disposes no fence cell: they are
     * the domain's. The root, boxes and fence slots stay the branch's: the
     * host dissolves it.
     */
    destroy() {
        this._destroyed = true;
        this._root.removeEventListener("focusin", this._onFocusIn);
        this._root.removeEventListener("keydown", this._onKeyDown);
        if (this._header) this._header.grid.destroy();
        for (var k = 0; k < this._members.length; k++) this._members[k].grid.destroy();
        for (var b = 0; b < this._grids.length; b++) this._grids[b].dissolve();
        this._grids = [];
        if (this._root.parentNode) this._root.parentNode.removeChild(this._root);
    }
}
