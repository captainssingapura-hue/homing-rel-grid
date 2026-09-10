// =============================================================================
// DishStarsCell — a health rating, 1 to 5, edited with a DROPDOWN. DOMAIN CODE,
// written by the bench rather than shipped with the grid, and that is the point
// of it: until now the stock text cell was the only implementation of the cell
// contract, so the contract was only ever proved against the one thing it was
// written for.
//
// This is a cell that is not text (map 17). It shows stars, not a string; it
// edits with a <select>, not an <input>; it commits on CHANGE rather than on
// Enter; and the grid cannot tell the difference, because everything the grid
// knows about a cell is on the contract:
//
//   render(host)        mount once into the element the grid minted
//   set(v)              the OWNER changed it; repaint unless an edit is open
//   onSelect(mode)      'none' | 'shallow' | 'deep' — pure lifecycle
//   mayTakeControl()    stage one: nowhere to commit, not mounted, or already
//                       open all mean no
//   takeControl()       stage two: open the dropdown and answer a PROMISE that
//                       settles when this cell is finished
//   dispose()           the owner's, never the grid's
//
// The same teardown discipline as the stock cell, for the same reason: detach
// the listeners FIRST, so removing a focused <select> cannot re-enter through
// its own blur; then remove; then repaint; then report to the owner; then
// settle. A promise settles once, so the order is all that is left to get
// right.
//
//   new DishStarsCell({ value?, onCommit? })
// =============================================================================

var _WB_STARS_STYLE_ID = "bench-stars-style";
var _WB_STARS_CSS = [
    ".wb-stars{font:13px sans-serif;letter-spacing:2px;padding:0 6px;}",
    ".wb-stars-ro{opacity:0.55;}",
    // OVER the slot, not in it. A <select> is intrinsically about as wide as
    // its longest option plus an arrow, and in an auto-layout table anything
    // in flow sets the column width — so a dropdown in flow would push every
    // other column aside for as long as it is open.
    ".wb-stars select{position:absolute;top:0;right:0;bottom:0;left:0;",
    "  box-sizing:border-box;width:100%;height:100%;font:13px sans-serif;",
    "  background:var(--color-surface);color:var(--color-text-primary);}"
].join("\n");

function _wbStarsEnsureStyle() {
    if (typeof document === "undefined" || !document.head) return;
    if (document.getElementById(_WB_STARS_STYLE_ID)) return;
    var s = document.createElement("style");
    s.id = _WB_STARS_STYLE_ID;
    s.textContent = _WB_STARS_CSS;
    document.head.appendChild(s);
}

function _wbStarsAddClass(el, name) {
    var cur = el.className || "", parts = cur.split(/\s+/);
    for (var i = 0; i < parts.length; i++) if (parts[i] === name) return;
    el.className = cur ? cur + " " + name : name;
}

var _WB_STARS_MIN = 1, _WB_STARS_MAX = 5;

class DishStarsCell {

    constructor(opts) {
        opts = opts || {};
        this._el = null;
        this._value = this._clamp(opts.value);
        this._onCommit = (typeof opts.onCommit === "function") ? opts.onCommit : null;
        this._mode = "none";
        this._select = null;
        this._done = null;
        this._onKey = null;
        this._onBlur = null;
        this._onChange = null;
    }

    /** A rating is an integer in [1, 5], or nothing at all. */
    _clamp(v) {
        var n = Math.round(Number(v));
        if (!isFinite(n)) return null;
        return Math.max(_WB_STARS_MIN, Math.min(_WB_STARS_MAX, n));
    }

    _stars() {
        if (this._value == null) return "—";
        var out = "";
        for (var k = _WB_STARS_MIN; k <= _WB_STARS_MAX; k++) out += (k <= this._value) ? "★" : "☆";
        return out;
    }

    _paint() {
        if (!this._el || this._select) return;
        this._el.textContent = this._stars();
    }

    render(host) {
        this._el = host;
        _wbStarsEnsureStyle();
        _wbStarsAddClass(host, "wb-stars");
        if (!this._onCommit) _wbStarsAddClass(host, "wb-stars-ro");
        this._paint();
        return this;
    }

    set(v) {
        this._value = this._clamp(v);
        this._paint();
        return this;
    }

    value() { return this._value; }

    onSelect(mode) { this._mode = mode; }
    mode() { return this._mode; }

    /** Stage one. Situational, synchronous, and free of side effects. */
    mayTakeControl() {
        return !!this._onCommit && !!this._el && !this._select;
    }

    /**
     * Stage two. Opens a dropdown of the five ratings and answers a promise
     * that settles when this cell is finished — chosen, cancelled, or blurred
     * away. The grid learns which of those it was: not at all.
     */
    takeControl() {
        var self = this;
        return new Promise(function (resolve) {
            self._done = resolve;
            var sel = document.createElement("select");
            for (var k = _WB_STARS_MIN; k <= _WB_STARS_MAX; k++) {
                var opt = document.createElement("option");
                opt.value = String(k);
                opt.textContent = k + "  " + self._starsFor(k);
                sel.appendChild(opt);
            }
            sel.value = String(self._value == null ? _WB_STARS_MIN : self._value);
            self._select = sel;
            // The stars STAY, holding the slot open at its natural size; the
            // dropdown covers them.
            self._el.appendChild(sel);
            // A dropdown commits when it CHANGES — there is no typing to finish,
            // and no Enter to wait for.
            self._onChange = function () { self._end(true); };
            self._onKey = function (e) {
                if (e.stopPropagation) e.stopPropagation();   // the keyboard is the cell's while deep
                if (e.key === "Escape")     { if (e.preventDefault) e.preventDefault(); self._end(false); }
                else if (e.key === "Enter") { if (e.preventDefault) e.preventDefault(); self._end(true); }
            };
            self._onBlur = function () { self._end(false); };
            sel.addEventListener("change", self._onChange);
            sel.addEventListener("keydown", self._onKey);
            sel.addEventListener("blur", self._onBlur);
            if (sel.focus) sel.focus();
        });
    }

    _starsFor(n) {
        var out = "";
        for (var k = _WB_STARS_MIN; k <= _WB_STARS_MAX; k++) out += (k <= n) ? "★" : "☆";
        return out;
    }

    /**
     * Finish. Listeners off first, so removing a focused <select> cannot
     * re-enter here through its own blur; then remove; then repaint; then tell
     * the owner; then settle.
     */
    _end(accept) {
        var sel = this._select;
        if (!sel) return;
        var chosen = sel.value;
        sel.removeEventListener("change", this._onChange);
        sel.removeEventListener("keydown", this._onKey);
        sel.removeEventListener("blur", this._onBlur);
        this._onChange = null; this._onKey = null; this._onBlur = null;
        this._select = null;
        if (sel.parentNode) {
            try { sel.parentNode.removeChild(sel); }
            catch (e) { /* already detached — nothing left to do */ }
        }
        this._paint();
        if (accept && this._onCommit) {
            try { this._onCommit(chosen); }
            catch (e) { console.error("[DishStarsCell] onCommit threw:", e); }
        }
        var done = this._done;
        this._done = null;
        if (done) done();
    }

    /** The OWNER's to call. Never the grid's. */
    dispose() {
        if (this._select) this._end(false);
        this._el = null;
    }
}
