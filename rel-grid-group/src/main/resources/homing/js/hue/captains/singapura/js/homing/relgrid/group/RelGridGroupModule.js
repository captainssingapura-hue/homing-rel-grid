// =============================================================================
// RelGridGroupModule — RFC 0050 · Episode 2's GROUP: an ordered list of tables
// stacked down one column, each an ordinary RelGrid THAT DOES NOT KNOW IT IS
// IN ONE. The federation (E2-ext2) re-read now that the table has the public
// verbs ext2 thought were missing: a group keeps its members agreeing through
// the surface every host already uses, and reaches into no member's layout.
//
//   new RelGridGroup({
//       container,          // where the group mounts
//       members: [{         // in order, top to bottom
//           id,             // the member's IDENTITY — unique in the group, never empty
//           grid,           // the ordinary RelGrid options this member is built from:
//                           // its relation, its branch, its ask, its callbacks. Verbatim,
//                           // but for the container (the group's box) and what is shared.
//           fence?          // the cell for the slot ABOVE this member (see fences)
//       }, ...],
//       fence?,             // the cell for the trailing slot, below the last member
//       columnWidths?,      // the widths every member starts at — the group's, not any member's
//       sharedHeader?,      // true (default): the first member's header is the group's and the
//                           // rest show none; false: every member keeps its own header option
//       onColumnResized?,   // (column, px) — ONE report per change, however many members moved
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
// and the last by the group — and hands it to the domain the way a table
// hands a slot to a cell: render(host), and dispose() is the owner's. What
// goes in it is the domain's — a title, a total, an illustration, a control
// — and a slot nobody fills takes no height. The group knows no caption; it
// knows a slot.
//
// WHAT IS SHARED, AND HOW. Column geometry is the one thing separate tables
// cannot agree on by themselves, so it is the group's: it applies its widths
// to every member at construction, hears any member's resize report, applies
// the change to the siblings through setColumnWidth, and reports ONCE. A
// sibling that refuses — locked, because a cell of its holds control or an
// answer is owed — is brought level the moment it is free again, on its own
// next report. The header is one: the first member's, with the resize
// handles; the rest are built with none (sharedHeader). Every member's own
// onColumnResized still fires — it is that member's truthful report of its
// own geometry — and the group's fires once for all of them.
//
// WHAT THE TABLE GAINED FOR THIS: nothing. Every verb here existed already.
// =============================================================================

var _HRGG_STYLE_ID = "homing-rel-grid-group-style";
var _HRGG_STYLE_CSS = [
    ".hrg-group{display:flex;flex-direction:column;align-items:stretch;}",
    ".hrg-member{flex:0 0 auto;}",
    ".hrg-fence{flex:0 0 auto;}",
    ".hrg-fence.hrg-fence-empty{display:none;}"
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
        if (!Array.isArray(opts.members) || opts.members.length === 0)
            throw new Error("[RelGridGroup] opts.members must name at least one member");
        _hrggEnsureStyles();
        var self = this;
        this._cbResized = opts.onColumnResized || null;
        this._sharedHeader = opts.sharedHeader !== false;
        this._widths = _hrggCopy(opts.columnWidths);          // the group's, identity-keyed
        this._broadcasting = false;                             // a report of the group's own making
        this._destroyed = false;

        this._root = document.createElement("div");
        this._root.className = "hrg-group";
        if (opts.label) this._root.setAttribute("aria-label", opts.label);
        this._members = [];                                     // { id, box, grid, spec }, in order
        this._fences = [];                                      // N+1 of { id, host, cell }; the last id is null

        var seen = {};
        for (var k = 0; k < opts.members.length; k++) {
            var m = opts.members[k] || {};
            if (m.id === undefined || m.id === null || m.id === "")
                throw new Error("[RelGridGroup] member " + k + " has no id");
            if (seen[m.id]) throw new Error("[RelGridGroup] duplicate member id: " + m.id);
            seen[m.id] = true;
            if (!m.grid) throw new Error("[RelGridGroup] member '" + m.id + "' has no grid options");
            this._fences.push(this._mintFence(m.id, m.fence || null));
            this._members.push(this._mintMember(m, k));
        }
        this._fences.push(this._mintFence(null, opts.fence || null));
        opts.container.appendChild(this._root);

        // Every member starts at the group's widths. Done after all are built,
        // so a member's report while it is being levelled finds its siblings.
        this._level(null);
    }

    // ── minting ────────────────────────────────────────────────────────────

    /** The slot above a member (or the trailing one): the domain's, once it has a cell. */
    _mintFence(id, cell) {
        var host = document.createElement("div");
        host.className = "hrg-fence" + (cell ? "" : " hrg-fence-empty");
        if (id !== null) host.setAttribute("data-member", String(id));
        this._root.appendChild(host);
        if (cell) {
            if (typeof cell.render !== "function")
                throw new Error("[RelGridGroup] a fence cell must render(host)" + (id !== null ? " — member '" + id + "'" : ""));
            try { cell.render(host); }
            catch (e) { console.error("[RelGridGroup] fence.render threw:", e); }
        }
        return { id: id, host: host, cell: cell };
    }

    /**
     * A member: its box, and the grid built from its own options — verbatim,
     * but for the container, the header when it is the group's, and the
     * reports the group listens to, which still reach the member's own host.
     */
    _mintMember(m, k) {
        var self = this, id = m.id, spec = m.grid;
        var box = document.createElement("div");
        box.className = "hrg-member";
        box.setAttribute("data-member", String(id));
        this._root.appendChild(box);
        var g = _hrggCopy(spec);
        g.container = box;
        if (this._sharedHeader && k > 0) g.header = { show: false };
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
        var entry = { id: id, box: box, grid: null, spec: spec };
        entry.grid = new RelGrid(g);
        return entry;
    }

    // ── the one shared thing: column geometry ──────────────────────────────

    /** A member moved a column. Hold it, level the siblings, report once. */
    _onMemberResized(id, column, px) {
        if (this._broadcasting || this._destroyed) return;
        this._widths[column] = px;
        this._broadcasting = true;
        try {
            for (var k = 0; k < this._members.length; k++)
                if (this._members[k].id !== id) this._members[k].grid.setColumnWidth(column, px);
        } finally { this._broadcasting = false; }
        if (this._cbResized) {
            try { this._cbResized(column, px); }
            catch (e) { console.error("[RelGridGroup] onColumnResized threw:", e); }
        }
    }

    /** Bring one member (or all) to the group's widths. Idempotent; silent while locked. */
    _level(id) {
        if (this._broadcasting || this._destroyed) return;
        this._broadcasting = true;
        try {
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
        for (var k = 0; k < this._members.length; k++) if (this._members[k].id === id) return this._members[k].grid;
        return null;
    }

    /** A fence's host: the slot above the member, or the trailing one for null. */
    fence(id) {
        for (var k = 0; k < this._fences.length; k++)
            if (this._fences[k].id === (id === undefined ? null : id)) return this._fences[k].host;
        return null;
    }

    el() { return this._root; }

    /** Destroys every member's grid and removes the group. Disposes no fence cell: they are the domain's. */
    destroy() {
        this._destroyed = true;
        for (var k = 0; k < this._members.length; k++) this._members[k].grid.destroy();
        if (this._root.parentNode) this._root.parentNode.removeChild(this._root);
    }
}
