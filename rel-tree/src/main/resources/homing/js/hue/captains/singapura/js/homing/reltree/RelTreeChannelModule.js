// =============================================================================
// RelTreeChannelModule — RFC 0050 · Episode 3-ext1's ASK CHANNEL, the tree's
// side: the tree's CUSTOMERS of the core in rel-channel. Two questions —
// unfold and fold — each answered with a View to present whole, or nothing;
// two notifications — the cursor moved, a node was activated — handed over
// and never waited on. The waiting, the mask, the one-at-a-time are the
// core's, and the layout is its surface.
//
// FOLD AND UNFOLD ARE BOTH QUESTIONS, answered with the whole View. Not the
// children of one node: the tree holds no structure to insert them into,
// and the fold state is the relation's — a domain that folds a node may
// free what was under it, and a domain that refuses answers nothing. LAZY
// BY DEFAULT: an unfold is the only way children are ever asked for.
//
// The payloads are the protocol's generated value objects. The tree mints
// them and never reads them back; an answer is presented exactly as given,
// or not at all.
//
//   new RelTreeChannel({ ask, layout, onView })
//   unfold(key) / fold(key)         the questions; true when asked
//   cursorChanged(key) / activated(key)   the notifications
//   has() / isPending() / destroy()
// =============================================================================

class RelTreeChannel {

    constructor(opts) {
        this._core = new RelChannel({ ask: opts.ask || null, surface: opts.layout, tag: "[RelTree]" });
        this._onView = opts.onView;               // (places) — the facade presents; it may throw, and the core records it
    }

    has()       { return this._core.has(); }
    isPending() { return this._core.isPending(); }
    destroy()   { this._core.destroy(); }

    // ── the questions ──────────────────────────────────────────────────────

    unfold(key) {
        var self = this;
        return this._core.ask(new RelTreeUnfold(key), function (answer) { self._applyView(answer, "unfold"); });
    }

    fold(key) {
        var self = this;
        return this._core.ask(new RelTreeFold(key), function (answer) { self._applyView(answer, "fold"); });
    }

    /** A View is presented, exactly as given; absence leaves the rows as they are; a malformed one is refused whole. */
    _applyView(answer, kind) {
        if (answer == null) return;
        if (!(answer instanceof RelTreeView)) {
            console.error("[RelTree] a " + kind + " was answered with something that is not a tree View:", answer);
            return;
        }
        this._onView(answer.places, kind);
    }

    // ── the notifications ──────────────────────────────────────────────────

    cursorChanged(key) { if (this._core.has()) this._core.notify(new RelTreeCursorChanged(key)); }
    activated(key)     { if (this._core.has()) this._core.notify(new RelTreeActivated(key)); }
}
