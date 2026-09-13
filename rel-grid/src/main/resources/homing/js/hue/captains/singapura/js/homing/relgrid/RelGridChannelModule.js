// =============================================================================
// RelGridChannelModule — RFC 0050 · Episode 2's ASK CHANNEL (ext4, ext6): one
// function the host gave, typed questions down, typed answers up — and the
// three customers the grid has for it.
//
// A NOTIFICATION expects no answer: the grid hands it over, does not wait,
// does not mask, and learns nothing — but a thenable that rejects is
// recorded, because a lost failure is worse than a slow one (law 229). The
// selection is one.
//
// A QUESTION is waited on, and the person is stopped until it is answered
// (ext6). The interval is dangerous — an answer describes the state it was
// asked about — so while a question is pending the grid is LOCKED (every
// intent refused, law 221) and MASKED: a wash that reads as unavailable,
// holding the focus (law 220). Delayed, so a fast answer shows none, and held
// briefly once up, so it never strobes (law 224). The second argument to ask
// is the MASK HANDLE: mask.panel(element) PLACES the domain's element in the
// panel — a golden box the grid mints — as a cell's element is placed in a
// slot; handing one over mounts the mask at once, and the grid invents no
// progress (law 225). The panel comes down the moment the answer comes; the
// wash holds its time. One at a time. Copy and the view handover are the
// two.
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

var _HRG_MASK_DELAY = 200;                // a question answered sooner shows no mask
var _HRG_MASK_HOLD = 250;                 // and one that showed stays at least this long

function _hrgLater(fn, ms)  { return setTimeout(fn, ms); }
function _hrgCancel(handle) { clearTimeout(handle); }
function _hrgNow()          { return Date.now(); }

class RelGridChannel {

    constructor(opts) {
        this._ask = opts.ask || null;
        this._layout = opts.layout;
        this._maps = opts.maps;
        this._selection = opts.selection;
        this._cursorPos = opts.cursorPos;         // () → { i, j } | null
        this._clipboard = opts.clipboard;
        this._onCopied = opts.onCopied || null;
        this._pending = null;                     // the question outstanding, while one is
        this._destroyed = false;
    }

    has()       { return !!this._ask; }
    isPending() { return !!this._pending; }

    /** Cancel a late clock; a settle after this does nothing. */
    destroy() {
        this._destroyed = true;
        if (this._pending && this._pending.timer) _hrgCancel(this._pending.timer);
        this._pending = null;
    }

    // ── the two kinds ──────────────────────────────────────────────────────

    _notify(question) {
        if (!this._ask) return;
        var out;
        try { out = this._ask(question); }
        catch (e) { console.error("[RelGrid] ask threw:", e); return; }
        if (out && typeof out.then === "function")
            out.then(null, function (e) { console.error("[RelGrid] ask rejected:", e); });
    }

    /** apply(answer) runs when the answer comes — before the mask comes down, because what it does may not wait on a hold. */
    _pend(question, apply) {
        if (!this._ask || this._pending) return false;
        var self = this;
        var session = { question: question, mounted: false, shownAt: 0, timer: null };
        this._pending = session;
        var handle = {
            panel: function (element) {
                if (!element || typeof element !== "object" || typeof element.appendChild !== "function")
                    throw new Error("[RelGrid] mask.panel(element): the domain hands its own element; the box is the grid's");
                if (self._pending !== session) return false;
                self._mount(session);
                self._layout.openPanel(element);
                return true;
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
     * The answer came — or did not. Apply first; then the panel at once, and
     * the wash when it has been up long enough. A settle after destroy, or
     * for a session that is not the current one, does nothing.
     */
    _settle(session, answer, apply) {
        if (this._destroyed || this._pending !== session) return;
        this._pending = null;
        if (session.timer) { _hrgCancel(session.timer); session.timer = null; }
        try { apply(answer); }
        catch (e) { console.error("[RelGrid] applying an answer threw:", e); }
        if (!session.mounted) return;          // nothing to take down
        this._layout.closePanel();             // the answer came: the domain's box goes at once; the wash holds
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

    // ── the customers ──────────────────────────────────────────────────────

    /** The selection changed: told, never asked. rects are the resolved rectangles. */
    selectionChanged(rects) {
        if (!this._ask) return;
        var ranges = [];
        for (var k = 0; k < rects.length; k++) {
            var r = rects[k];
            ranges.push(new RelGridRange(r.i0, r.j0, r.i1, r.j1));
        }
        this._notify(new RelGridSelectionChanged(ranges));
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
        return this._pend(new RelGridCopyRequested(this._blocks()), function (answer) { self._applyCopy(answer); });
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
        return this._pend(new RelGridViewHandover(), function (answer) { self._applyView(answer); });
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
