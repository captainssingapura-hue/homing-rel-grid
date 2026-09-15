// =============================================================================
// RelTreeModule — RFC 0050 · Episode 3-ext1's facade: composes the tree view
// and is the ONLY place the two branches meet. Orchestration only — the
// structure is RelTreePlaces, the chrome is RelTreeLayout, the registry is
// RelTreeCells, the cursor is RelTreeCursor, what a gesture means is
// RelTreeGestures, the channel and its questions are RelTreeChannel over
// the core in rel-channel. Each holds its own state and is handed only
// what it needs.
//
// TWO INDEPENDENT BRANCHES. The tree mints its own chrome on its own
// DomOpsParty branch and nothing else; the domain mints its cells on a
// branch of its own and the tree never sees it. A cell is a NOUN: the tree
// asks cellFor(key) for it, asks it once for cellElement(), and PLACES that
// element in the row its key maps to, after the tree's own caret — that is
// the whole of what crosses. Nothing is rendered into anything; nothing is
// read back.
//
//   new RelTree({
//       container,        // where the layout mounts
//       branch,           // the tree's OWN branch, handed UNACTIVATED: the tree activates it and
//                         // mints everything — wrap, rows, mask — on it or a sub-branch of it.
//                         // Dissolving it is the host's. Never a cell's element.
//       relation,         // { view(), cellFor(key) } — see TreeRelationContract. THE TREE
//                         // STRUCTURE IS THE BINDING: view() answers the PLACES to present now,
//                         // { key, depth, fold } in order — a bare key is a leaf at depth 0 —
//                         // and the tree holds exactly that, no other list, and derives the
//                         // parent and the first child from it. Lazy by default: the roots,
//                         // closed, and the rest by asking.
//       label?,           // aria-label
//       caret?,           // false: no caret of the tree's — a domain that draws its own in its
//                         // cell, flips its own fold state and tells
//       folder?,          // true: a folder glyph after the caret says the fold state a second way —
//                         // 📁 closed, 📂 open, a blank box for a leaf; { closed?, open?, leaf? } to
//                         // choose the glyphs. Off by default.
//       ask?,             // (question, mask) — THE CHANNEL: RelTreeUnfold and RelTreeFold are
//                         // questions answered with a RelTreeView or nothing; RelTreeCursorChanged
//                         // and RelTreeActivated are notifications. See RelTreeChannel.
//       onArranged?,      // (kind) after every presentation pass
//       onCursorMoved?,   // (key)
//       onActivated?,     // (key) — Enter or a double-click reached it
//       onEdge?           // (direction) — a bare arrow went nowhere: 'up' | 'down'
//   });
//
// THE PRESENTATION CYCLE: the answer is coerced and checked at the door — a
// malformed outline is refused whole and nothing has moved; every presented
// key's cell is ensured BEFORE a row moves, so a relation that refuses a
// stranger refuses the View whole and the places go back; then the rows for
// the count — kept, grown or shrunk at the tail — each painted from its
// place, every cell placed after its caret, the cursor resolved (its key if
// presented; the node that folded, when the tree asked for the fold that
// took its row; else its position), whatever left is detached alive and
// forgotten.
//
// LOCKED while a question is pending: every intent is refused, not deferred.
// =============================================================================

class RelTree {

    constructor(opts) {
        opts = opts || {};
        if (!opts.container) throw new Error("[RelTree] opts.container is required");
        if (!opts.branch)    throw new Error("[RelTree] opts.branch is required");
        if (!opts.relation)  throw new Error("[RelTree] opts.relation is required");
        var r = opts.relation, self = this;
        if (typeof r.view !== "function" || typeof r.cellFor !== "function")
            throw new Error("[RelTree] relation must expose view() and cellFor(key)");
        opts.branch.activate(this);              // the tree's own: the party refuses one that is already somebody's
        this._relation = r;
        this._cellFor = function (key) { return r.cellFor(key); };
        this._cbArranged = opts.onArranged || null;
        this._cbActivated = opts.onActivated || null;
        this._destroyed = false;
        this._asked = null;                      // { key, kind } while the tree's own fold question is out
        this._places = new RelTreePlaces([]);
        this._layout = new RelTreeLayout({
            branch: opts.branch, container: opts.container, label: opts.label || null, caret: opts.caret, folder: opts.folder,
            onRowClick:    function (i) { self._gestures.onRowClick(i); },
            onRowDblClick: function (i) { self._gestures.onRowDblClick(i); },
            onCaretClick:  function (i) { self._gestures.onCaretClick(i); }
        });
        this._cells = new RelTreeCells();        // the domain's elements, by key; the tree mints none
        this._channel = new RelTreeChannel({
            ask: opts.ask, layout: this._layout,
            onView: function (places, kind) { self._present(places, kind); }
        });
        this._cursor = new RelTreeCursor({
            places: this._places, layout: this._layout, cells: this._cells,
            onMoved: function (key) {
                self._channel.cursorChanged(key);
                if (opts.onCursorMoved) {
                    try { opts.onCursorMoved(key); }
                    catch (e) { console.error("[RelTree] onCursorMoved threw:", e); }
                }
            }
        });
        this._gestures = new RelTreeGestures({
            tree: this, places: this._places, cursor: this._cursor,
            locked: function () { return self._locked(); },
            pageRows: function () { return self._pageRows(); },
            onEdge: opts.onEdge
        });
        this._keydown = function (e) { self._gestures.onKey(e); };
        this._layout.el().addEventListener("keydown", this._keydown);
        this._present(r.view(), "base");
    }

    _locked() { return this._channel.isPending(); }

    /** How many rows a page is: what the wrap shows, or ten when nothing has a height yet. */
    _pageRows() {
        var h = this._layout.wrap().clientHeight || 0, rh = this._layout.rowHeight();
        var n = Math.floor(h / rh);
        return n > 0 ? n : 10;
    }

    // ── the presentation cycle ─────────────────────────────────────────────

    _present(raw, kind) {
        var next = RelTreePlaces.coerce(raw);
        var places = this._places, previous = places.swap(next);           // checked: a malformed outline throws before anything moved
        var i;
        try {
            for (i = 0; i < places.rows(); i++) this._cells.ensure(places.keyAt(i), this._cellFor);   // every ask BEFORE a row moves
        } catch (e) {
            places.swap(previous);                                         // a stranger refuses the View whole: the places go back
            throw e;
        }
        this._layout.render(places.rows());
        for (i = 0; i < places.rows(); i++) {
            this._layout.paint(i, places.at(i));
            this._cells.place(places.keyAt(i), this._layout.rowAt(i));
        }
        var asked = this._asked, held = this._cursor.key();
        this._asked = null;
        if (asked && held !== null && places.indexOf(held) < 0 && places.indexOf(asked.key) >= 0)
            this._cursor.landOn(asked.key);                                // the row folded away under the cursor: the folder
        else
            this._cursor.resolve();
        this._cells.detachInvisible(function (key) { return places.indexOf(key) >= 0; });   // alive, out of the tree, forgotten
        if (this._cbArranged) {
            try { this._cbArranged(kind); }
            catch (e) { console.error("[RelTree] onArranged threw:", e); }
        }
    }

    // ── the questions: fold and unfold, on the channel ────────────────────

    /** Ask for a closed node's children. False when locked, channel-less, not presented, or not closed. */
    unfold(key) {
        if (this._locked() || !this._channel.has()) return false;
        var i = this._places.indexOf(key);
        if (i < 0 || this._places.foldAt(i) !== "closed") return false;
        this._asked = { key: key, kind: "unfold" };
        return this._channel.unfold(key);
    }

    /** Ask for an open node's children to be withdrawn. False when locked, channel-less, not presented, or not open. */
    fold(key) {
        if (this._locked() || !this._channel.has()) return false;
        var i = this._places.indexOf(key);
        if (i < 0 || this._places.foldAt(i) !== "open") return false;
        this._asked = { key: key, kind: "fold" };
        return this._channel.fold(key);
    }

    /** Enter, or a double-click, reached a node: told, and reported. The programmatic twin of both. */
    activate(key) {
        if (this._places.indexOf(key) < 0) return false;
        this._channel.activated(key);
        if (this._cbActivated) {
            try { this._cbActivated(key); }
            catch (e) { console.error("[RelTree] onActivated threw:", e); }
        }
        return true;
    }

    // ── told, unasked ──────────────────────────────────────────────────────

    /** RelTreeViewChanged: the relation's View changed underneath — ask view() again and present it. */
    tell(message) {
        if (this._destroyed) return false;
        if (message instanceof RelTreeViewChanged) {
            this._present(this._relation.view(), "told");
            return true;
        }
        console.error("[RelTree] told something it does not understand:", message);
        return false;
    }

    // ── the cursor ─────────────────────────────────────────────────────────

    cursor() { return this._cursor.key(); }

    /** The programmatic cursor: a presented key, revealed. False when locked or not presented. */
    selectNode(key) {
        if (this._locked()) return false;
        var i = this._places.indexOf(key);
        if (i < 0) return false;
        this._cursor.set(i);
        this._cursor.follow(i, true);
        return true;
    }

    // ── seams + lifecycle ──────────────────────────────────────────────────

    isPending() { return this._channel.isPending(); }
    focus()     { this._layout.focus(); return this; }
    el()        { return this._layout.el(); }
    cells()     { return this._cells; }
    places()    { return this._places; }

    /** Detaches every cell and removes the chrome; DISPOSES NOTHING. */
    destroy() {
        this._destroyed = true;
        this._channel.destroy();
        this._layout.el().removeEventListener("keydown", this._keydown);
        this._cells.destroy();
        this._layout.destroy();
    }
}
