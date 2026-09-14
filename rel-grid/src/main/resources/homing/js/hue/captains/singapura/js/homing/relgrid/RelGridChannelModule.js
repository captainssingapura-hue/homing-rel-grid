// =============================================================================
// RelGridChannelModule — RFC 0050 · Episode 2's ASK CHANNEL (ext4, ext6), the
// grid's side: the three CUSTOMERS the grid has for it, over the core in
// rel-channel. The core is one function the host gave, a notification handed
// over and never waited on, a question waited on with the person stopped —
// the mask session with its delay and its hold, the panel handle, one at a
// time (laws 220–225, 229). What is asked and what an answer means is here.
//
// The layout IS the core's surface: openMask, closeMask, setMasked,
// openPanel, closePanel, focus — the six the mask needs — and nothing else
// of it is reached from the core.
//
// The payloads are value objects GENERATED from Java records (ext5), so a
// shape cannot drift from its declaration. The grid mints them and never
// reads them back; an answer is applied exactly as given, or not at all.
//
//   new RelGridChannel({ ask, layout, maps, selection, cursorPos, clipboard, onCopied? })
//   selectionChanged(rects)   the notification
//   copy()                    the question: what the selection is worth on a clipboard; the answer is written
//   handoverView()            the question: the rows' arrangement; the answer is presented
//   isPending() / destroy()
// =============================================================================

class RelGridChannel {

    constructor(opts) {
        this._core = new RelChannel({ ask: opts.ask || null, surface: opts.layout, tag: "[RelGrid]" });
        this._maps = opts.maps;
        this._selection = opts.selection;
        this._cursorPos = opts.cursorPos;         // () → { i, j } | null
        this._clipboard = opts.clipboard;
        this._onCopied = opts.onCopied || null;
    }

    has()       { return this._core.has(); }
    isPending() { return this._core.isPending(); }

    /** Cancel a late clock; a settle after this does nothing. */
    destroy() { this._core.destroy(); }

    // ── the customers ──────────────────────────────────────────────────────

    /** The selection changed: told, never asked. rects are the resolved rectangles. */
    selectionChanged(rects) {
        if (!this._core.has()) return;
        var ranges = [];
        for (var k = 0; k < rects.length; k++) {
            var r = rects[k];
            ranges.push(new RelGridRange(r.i0, r.j0, r.i1, r.j1));
        }
        this._core.notify(new RelGridSelectionChanged(ranges));
    }

    /**
     * What is selected, as IDENTITIES: one block per range, in the order the
     * ranges were made, each the pks down its rows and the columns across it
     * in view order. Faithful — no bounding box, no merge, no refusal here.
     */
    _blocks() {
        var rects = this._selection.resolve(this._cursorPos()), maps = this._maps, out = [];
        for (var k = 0; k < rects.length; k++) {
            var r = rects[k], pks = [], cols = [];
            for (var i = r.i0; i <= r.i1; i++) pks.push(maps.pkAt(i));
            for (var j = r.j0; j <= r.j1; j++) cols.push(maps.columnAt(j));
            out.push(new RelGridBlock(pks, cols));
        }
        return out;
    }

    /** Ask what the selection is worth on a clipboard, and write the answer. True when asked. */
    copy() {
        var self = this;
        return this._core.ask(new RelGridCopyRequested(this._blocks()), function (answer) { self._applyCopy(answer); });
    }

    /** Content is written and reported; absence writes nothing (law 49). */
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
            if (self._onCopied) {
                try { self._onCopied(answer); }
                catch (e) { console.error("[RelGrid] onCopied threw:", e); }
            }
        }, function (e) { console.error("[RelGrid] the clipboard write failed:", e); });
    }

    /** Hand the arrangement of the rows to the domain. True when asked. */
    handoverView() {
        var self = this;
        return this._core.ask(new RelGridViewHandover(), function (answer) { self._applyView(answer); });
    }

    /**
     * A View is presented, exactly as given; absence leaves the rows as they
     * are. A View naming a pk the relation never declared is refused whole —
     * the maps throw, the settle records it, and nothing moves — because half
     * a View is not a View.
     */
    _applyView(answer) {
        if (answer == null) return;
        if (!(answer instanceof RelGridView)) {
            console.error("[RelGrid] a view handover was answered with something that is not a View:", answer);
            return;
        }
        this._maps.setRowView(answer.pks);                  // → arrange("rows"): the cursor keeps its identity, ranges clear
    }
}
