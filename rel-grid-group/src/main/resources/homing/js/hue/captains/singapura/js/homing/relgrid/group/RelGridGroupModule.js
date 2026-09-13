// =============================================================================
// RelGridGroupModule — RFC 0050 · Episode 2's GROUP: an ordered list of tables
// stacked down one column, each an ordinary RelGrid THAT DOES NOT KNOW IT IS
// IN ONE. The federation (E2-ext2) re-read now that the table has the public
// verbs ext2 thought were missing: a group keeps its members agreeing through
// the surface every host already uses, and reaches into no member's layout.
// The minting is RelGridGroupMint; the one cursor and the walk between the
// stops is RelGridGroupWalk; what is here is the group's own state — which
// members, in what order, which folded, the widths they share — and the verbs
// over it.
//
//   new RelGridGroup({
//       container,          // where the group mounts
//       branch,             // the group's OWN branch (DomOpsParty), handed unactivated: the
//                           // group activates it; its boxes and fence slots are minted on it,
//                           // every member's grid gets a sub-branch of it to activate as its
//                           // own, and dissolving it is the host's
//       members: [{         // in order, top to bottom
//           id,             // the member's IDENTITY — unique in the group, never empty
//           grid,           // the ordinary RelGrid options this member is built from:
//                           // its relation, its ask, its callbacks. Verbatim, but for the
//                           // container (the group's box), the branch (the group's gift)
//                           // and what is shared.
//           fence?          // the cell for the slot ABOVE this member (see RelGridGroupMint)
//       }, ...],
//       fence?,             // the cell for the trailing slot, below the last member
//       columnWidths?,      // the widths every member starts at — the group's, not any member's
//       header?,            // 'group' (default): ONE header, the group's, at the top above every
//                           // fence, and no member shows its own; 'each': every member keeps
//                           // its own header option, and the group adds none
//       stickyHeader?,      // true: the group's header stays at the top of whatever scrolls the
//                           // group, and every member reveals its cursor clear of it. 'group'
//                           // mode only — in 'each' mode a member's own header option says
//       folded?,            // the ids folded at first — [] by default
//       onColumnResized?,   // (column, px) — ONE report per change, however many members moved
//       onFolded?,          // (id, folded) — a REPORT: a member's box was folded or unfolded
//       label?              // aria-label on the group
//   });
//
// MEMBERS ARE IDENTITIES. A member is addressed by its id and nothing else;
// the group's own state is which members show, in what order, and which are
// folded — a view over members, as a table's is a view over rows. Nothing the
// group does is ever applied to a member's rows: the member's cursor,
// selection, copy, handover and the domain its ask reaches are its own,
// wired exactly as they would be for a table standing alone. The group
// observes; it does not route. (A member may have no rows at all — a relation
// with nothing to present — and still have its identity and its fence: that
// is how an illustration stands between two tables.)
//
// FOLD is the group's own state: which members show their table. A folded
// member's BOX is hidden — its fence stays — and the table inside is
// untouched: cursor, selection, cells, view, all as they were, and it never
// learns. fold(id, on), foldAll(on) and folded(id) are the host's verbs;
// onFolded(id, on) is the report; and every fence with an onFolded is told
// when the member below it folds or unfolds, by whatever road.
//
// TELL is the channel's other direction. The grid asks and the domain
// answers; here the domain SAYS, unasked: a control the domain drew in a
// fence was pressed. tell(message) carries a protocol value the same way an
// answer does; a fence has no handle and no way to the group of its own —
// the HOST wires a closure onto tell into the fence it builds. A
// RelGridGroupFold is the first kind; an unknown one is recorded and
// refused, never dropped.
//
// WHAT IS SHARED, AND HOW. Column geometry is the one thing separate tables
// cannot agree on by themselves, so it is the group's: it applies its widths
// to every member at construction, hears any member's resize report, applies
// the change to the siblings through setColumnWidth, and reports ONCE. A
// sibling that refuses — locked, because a cell of its holds control or an
// answer is owed — is brought level the moment it is free again, on its own
// next report. Every member's own onColumnResized still fires — it is that
// member's truthful report of its own geometry — and the group's fires once
// for all of them. What the table gained for this: one option, resizeGuide —
// what a header drag's guide line spans, a segment per box, so the line
// breaks at the fences. Every other verb existed already.
// =============================================================================

/** A copy of a plain object: what is held is a snapshot the host may keep. */
function _hrggSnapshot(o) {
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
        var self = this;
        this._cbResized = opts.onColumnResized || null;
        this._cbFolded = opts.onFolded || null;
        var headerMode = (opts.header === "each") ? "each" : "group";
        if (opts.header !== undefined && opts.header !== "each" && opts.header !== "group")
            throw new Error("[RelGridGroup] header must be 'group' or 'each', not " + JSON.stringify(opts.header));
        var sticky = headerMode === "group" && opts.stickyHeader === true;
        this._folded = {};                                      // id → true while folded
        var f0 = opts.folded || [];
        for (var i = 0; i < f0.length; i++) this._folded[f0[i]] = true;
        this._widths = _hrggSnapshot(opts.columnWidths);      // the group's, identity-keyed
        this._broadcasting = false;                             // a report of the group's own making
        this._destroyed = false;
        this._branch = opts.branch;                             // the group's own; the host dissolves it
        this._branch.activate(this);
        this._root = this._branch.createElement("root", "div");
        css.addClass(this._root, hrg_group);
        if (opts.label) this._root.setAttribute("aria-label", opts.label);
        this._members = [];                                     // { id, box, grid, spec }, in order
        this._fences = [];                                      // N+1 of { id, host, cell }; the last id is null
        this._header = null;                                    // { box, grid } — the group's own header, in 'group' mode

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
        this._mint = new RelGridGroupMint({
            branch: this._branch, root: this._root, headerMode: headerMode, sticky: sticky,
            hooks: {
                onResized:  function (id, column, px) { self._onMemberResized(id, column, px); },
                onReleased: function (id) { self._level(id); },
                onArranged: function (id) { self._level(id); },
                // The cursor met a table's edge: step over it, one stop, no wrap.
                onEdge:     function (direction) {
                    if (direction === "up") self._walk.step(-1, false);
                    else if (direction === "down") self._walk.step(1, false);
                }
            }
        });
        if (headerMode === "group") this._header = this._mint.header(opts.members);
        for (var k = 0; k < opts.members.length; k++) {
            var m = opts.members[k];
            this._fences.push(this._mint.fence(m.id, m.fence || null, k));
            this._members.push(this._mint.member(m, k));
            if (this._folded[m.id]) this._paintFold(m.id, true);   // folded at first: the box hidden before it is seen
        }
        this._fences.push(this._mint.fence(null, opts.fence || null, opts.members.length));
        for (var fk in this._folded) if (!seen[fk]) delete this._folded[fk];   // drift: an id that is no member is dropped

        // One cursor: the member whose table holds the focus is the active one,
        // observed at the root; Tab walks the stops. Neither reaches into a table.
        this._walk = new RelGridGroupWalk({
            root: this._root, members: this._members, fences: this._fences,
            folded: function (id) { return self._folded[id] === true; }
        });
        this._walk.listen();
        this._walk.activate(this._members[0].id, false);
        this._walk.paintDormancy();                             // every member is built now: the others dormant
        opts.container.appendChild(this._root);

        // Every member starts at the group's widths. Done after all are built,
        // so a member's report while it is being levelled finds its siblings.
        this._level(null);
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

    columnWidths() { return _hrggSnapshot(this._widths); }

    // ── one cursor ─────────────────────────────────────────────────────────

    /** The member whose cursor is the group's. */
    active() { return this._walk.active(); }

    /** Make a member the active one and give its table the focus. False for an id that is no member. */
    activate(id) { return this._walk.activate(id, true); }

    // ── fold: the group's own state, applied to a member's box ─────────────

    /** The box hidden or shown, and the fence above it marked; the table inside is not touched. */
    _paintFold(id, on) {
        var m = this._entry(id), f = this._fenceOf(id);
        if (!m) return;
        css.toggleClass(m.box, hrg_folded, on);
        if (f) css.toggleClass(f.host, hrg_fence_folded, on);
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
        this._walk.unlisten();
        if (this._header) this._header.grid.destroy();
        for (var k = 0; k < this._members.length; k++) this._members[k].grid.destroy();
        this._mint.dissolve();
        if (this._root.parentNode) this._root.parentNode.removeChild(this._root);
    }
}
