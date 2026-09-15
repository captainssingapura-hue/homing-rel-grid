// =============================================================================
// RelChannelModule — the ASK CHANNEL's core (RFC 0050 · Episode 2, ext4 and
// ext6; Episode 3-ext1): one function the host gave, typed questions down,
// typed answers up. The family's, not the grid's — what is asked and what an
// answer means are the customers' business, and a customer is written over
// this for each presentation: the grid's channel keeps its selection, copy
// and view handover here, the tree's its fold and unfold.
//
// A NOTIFICATION expects no answer: handed over, not waited on, nothing
// learned — but a thenable that rejects is recorded, because a lost failure
// is worse than a slow one (law 229).
//
// A QUESTION is waited on, and the person is stopped until it is answered
// (ext6). The interval is dangerous — an answer describes the state it was
// asked about — so while a question is pending the presentation is LOCKED
// (every intent refused, law 221; that is the customer's, read off
// isPending()) and MASKED: a wash that reads as unavailable, holding the
// focus (law 220). Delayed, so a fast answer shows none, and held briefly
// once up, so it never strobes (law 224). The second argument to ask is the
// MASK HANDLE: mask.panel(element) PLACES the domain's element in the panel
// — a box the presentation mints — as a cell's element is placed in a slot;
// handing one over mounts the mask at once, and nothing here invents
// progress (law 225). The panel comes down the moment the answer comes; the
// wash holds its time. One question at a time.
//
// The SURFACE is what the mask needs of a presentation — six functions a
// layout has — and the only way this module reaches the DOM:
//   openMask()  closeMask()  setMasked(on)  openPanel(element)  closePanel()  focus()
//
//   new RelChannel({ ask, surface, tag? })
//   has()                   a channel was given
//   notify(question)        the first kind: handed over, never waited on
//   ask(question, apply)    the second: waited on behind the mask; apply(answer) runs when it
//                           comes — before the mask comes down, because what it does may not
//                           wait on a hold. True when asked; false with no channel, or one pending
//   isPending() / destroy()
// =============================================================================

var _HRC_MASK_DELAY = 200;                // a question answered sooner shows no mask
var _HRC_MASK_HOLD = 250;                 // and one that showed stays at least this long

function _hrcLater(fn, ms)  { return setTimeout(fn, ms); }
function _hrcCancel(handle) { clearTimeout(handle); }
function _hrcNow()          { return Date.now(); }

class RelChannel {

    constructor(opts) {
        opts = opts || {};
        this._ask = (typeof opts.ask === "function") ? opts.ask : null;
        this._surface = opts.surface;
        this._tag = opts.tag || "[RelChannel]";   // who is speaking, on the console
        this._pending = null;                     // the question outstanding, while one is
        this._destroyed = false;
    }

    has()       { return !!this._ask; }
    isPending() { return !!this._pending; }

    /** Cancel a late clock; a settle after this does nothing. */
    destroy() {
        this._destroyed = true;
        if (this._pending && this._pending.timer) _hrcCancel(this._pending.timer);
        this._pending = null;
    }

    // ── the two kinds ──────────────────────────────────────────────────────

    notify(question) {
        if (!this._ask) return;
        var out, tag = this._tag;
        try { out = this._ask(question); }
        catch (e) { console.error(tag + " ask threw:", e); return; }
        if (out && typeof out.then === "function")
            out.then(null, function (e) { console.error(tag + " ask rejected:", e); });
    }

    ask(question, apply) {
        if (!this._ask || this._pending) return false;
        var self = this, tag = this._tag;
        var session = { question: question, mounted: false, shownAt: 0, timer: null };
        this._pending = session;
        var handle = {
            panel: function (element) {
                if (!element || typeof element !== "object" || typeof element.appendChild !== "function")
                    throw new Error(tag + " mask.panel(element): the domain hands its own element; the box is the presentation's");
                if (self._pending !== session) return false;
                self._mount(session);
                self._surface.openPanel(element);
                return true;
            }
        };
        var out;
        try { out = this._ask(question, handle); }
        catch (e) {
            console.error(tag + " ask threw:", e);
            this._settle(session, undefined, apply);
            return true;                       // the gesture was taken; the domain failed it
        }
        var p = (out && typeof out.then === "function") ? out : Promise.resolve(out);
        if (!session.mounted)
            session.timer = _hrcLater(function () {
                session.timer = null;
                if (self._pending === session) self._mount(session);
            }, _HRC_MASK_DELAY);
        p.then(function (answer) { self._settle(session, answer, apply); },
               function (e) {
                   console.error(tag + " ask rejected:", e);
                   self._settle(session, undefined, apply);
               });
        return true;
    }

    /** The mask goes up: once per session, whether the domain asked or the clock did. */
    _mount(session) {
        if (session.mounted) return;
        session.mounted = true;
        session.shownAt = _hrcNow();
        if (session.timer) { _hrcCancel(session.timer); session.timer = null; }
        this._surface.openMask();
        this._surface.setMasked(true);
    }

    /**
     * The answer came — or did not. Apply first; then the panel at once, and
     * the wash when it has been up long enough. A settle after destroy, or
     * for a session that is not the current one, does nothing.
     */
    _settle(session, answer, apply) {
        if (this._destroyed || this._pending !== session) return;
        this._pending = null;
        if (session.timer) { _hrcCancel(session.timer); session.timer = null; }
        try { apply(answer); }
        catch (e) { console.error(this._tag + " applying an answer threw:", e); }
        if (!session.mounted) return;          // nothing to take down
        this._surface.closePanel();            // the answer came: the domain's box goes at once; the wash holds
        var self = this, up = _hrcNow() - session.shownAt;
        if (up >= _HRC_MASK_HOLD) this._unmask();
        else _hrcLater(function () { self._unmask(); }, _HRC_MASK_HOLD - up);
    }

    /** Down, unless a new question has taken the mask over in the meantime. */
    _unmask() {
        if (this._destroyed || this._pending) return;
        this._surface.closeMask();
        this._surface.setMasked(false);
        this._surface.focus();                 // the keyboard host takes the keys again
    }
}
