// =============================================================================
// DishStarsCell — a health rating, 1 to 5, edited with a CUSTOM CONTROL. DOMAIN
// CODE, written by the bench rather than shipped with the grid, and that is the
// point of it: until it existed the stock text cell was the only implementation
// of the cell contract, so the contract had only ever been proved against the
// one thing it was written for.
//
// This is a cell that is not text (map 17), and its editor is not a form
// element either. It shows stars; it edits with a PANEL far larger than the
// cell in both directions, hung off the anchor the grid minted; and while it
// holds control it owns the keyboard outright:
//
//   →  one more star        ←  one fewer        (clamped at 5 and at 1)
//   ↑  ↓  nothing           deliberately dead, and swallowed rather than passed
//   Enter  commit           Escape / blur  cancel
//   a click on a star       choose it and commit
//
// Nothing of that reaches the grid. The editor lives OUTSIDE the table, so its
// keydowns bubble to the wrapper and never past the table the grid listens on
// — the isolation is structural rather than something this cell has to arrange,
// though it stops propagation anyway to say so out loud.
//
// The contract is answered exactly as the stock cell answers it:
//
//   render(host)        mount once into the element the grid minted
//   set(v)              the OWNER changed it; repaint unless an edit is open
//   onSelect(mode)      'none' | 'shallow' | 'deep' — pure lifecycle
//   mayTakeControl()    stage one: nowhere to commit, not mounted, or already
//                       open all mean no
//   takeControl(host)   stage two: build the panel in the host and answer a
//                       PROMISE that settles when this cell is finished
//   dispose()           the owner's, never the grid's
//
// The same teardown discipline as the stock cell, for the same reason: detach
// the listeners FIRST, so removing a focused panel cannot re-enter through its
// own blur; then remove; then repaint; then report to the owner; then settle.
//
//   new DishStarsCell({ value?, onCommit? })
// =============================================================================

var _WB_STARS_STYLE_ID = "bench-stars-style";
var _WB_STARS_CSS = [
    ".wb-stars{font:13px sans-serif;letter-spacing:2px;padding:0 6px;}",
    ".wb-stars-ro{opacity:0.55;}",
    // The PANEL. Hung off the anchor the grid minted — which is exactly the
    // cell — so it is free to be much larger in both directions. Out of the
    // table it costs the table nothing: no column widens, no row grows.
    ".wb-stars-panel{position:absolute;top:100%;left:0;min-width:250px;",
    "  padding:12px 14px;box-sizing:border-box;",
    "  background:var(--color-surface-raised);color:var(--color-text-primary);",
    "  border:1px solid var(--color-border);border-radius:6px;",
    "  box-shadow:0 8px 24px rgba(0,0,0,0.28);outline:none;}",
    ".wb-stars-row{display:flex;gap:8px;font-size:30px;line-height:1;cursor:pointer;}",
    ".wb-star{user-select:none;-webkit-user-select:none;color:var(--color-text-muted);opacity:0.45;}",
    ".wb-star-on{color:var(--color-accent);opacity:1;}",
    ".wb-stars-hint{margin-top:10px;font:11px sans-serif;color:var(--color-text-muted);",
    "  white-space:nowrap;}"
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
        this._panel = null;        // the editor, while one is open
        this._stars = null;        // its five star elements
        this._draft = null;        // the rating being chosen, committed only on accept
        this._host = null;         // the anchor the grid minted for this session
        this._done = null;         // resolves the promise takeControl handed the grid
        this._onKey = null;
        this._onBlur = null;
    }

    /** A rating is an integer in [1, 5], or nothing at all. */
    _clamp(v) {
        var n = Math.round(Number(v));
        if (!isFinite(n)) return null;
        return Math.max(_WB_STARS_MIN, Math.min(_WB_STARS_MAX, n));
    }

    _starsFor(n) {
        var out = "";
        for (var k = _WB_STARS_MIN; k <= _WB_STARS_MAX; k++) out += (k <= n) ? "★" : "☆";
        return out;
    }

    _text() { return (this._value == null) ? "—" : this._starsFor(this._value); }

    _paint() {
        if (!this._el || this._panel) return;
        this._el.textContent = this._text();
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
    draft() { return this._draft; }

    /**
     * What this cell is worth on a clipboard that takes HTML: the stars a
     * person SAW, not the number underneath. A literal colour rather than a
     * theme token, because the clipboard leaves the page and a token means
     * nothing in a spreadsheet. Not on the cell contract — the grid never
     * asks it; the domain's copier does (map 6 law 48).
     */
    clipboardHtml() {
        if (this._value == null) return "—";
        return '<span style="color:#e0a300;letter-spacing:2px" title="' + this._value + ' of 5">'
             + this._starsFor(this._value) + "</span>";
    }

    onSelect(mode) { this._mode = mode; }
    mode() { return this._mode; }

    /** Stage one. Situational, synchronous, and free of side effects. */
    mayTakeControl() {
        return !!this._onCommit && !!this._el && !this._panel;
    }

    /**
     * Stage two. Builds the panel in the host the grid minted and answers a
     * promise that settles when this cell is finished — committed, cancelled,
     * or blurred away. The grid learns which of those it was: not at all.
     */
    takeControl(host) {
        var self = this;
        return new Promise(function (resolve) {
            self._done = resolve;
            self._host = host || self._el;
            self._draft = (self._value == null) ? _WB_STARS_MIN : self._value;

            var panel = document.createElement("div");
            panel.className = "wb-stars-panel";
            panel.tabIndex = 0;                       // a property, so no attribute is needed

            var row = document.createElement("div");
            row.className = "wb-stars-row";
            self._stars = [];
            for (var k = _WB_STARS_MIN; k <= _WB_STARS_MAX; k++) {
                var star = document.createElement("span");
                star.className = "wb-star";
                star.textContent = "★";
                // Keep the focus on the panel: a press inside must not blur it,
                // because a blur is a cancel.
                star.addEventListener("mousedown", function (e) {
                    if (e.preventDefault) e.preventDefault();
                });
                star.addEventListener("click", (function (n) {
                    return function () { self._draft = n; self._repaintStars(); self._end(true); };
                })(k));
                row.appendChild(star);
                self._stars.push(star);
            }
            panel.appendChild(row);

            var hint = document.createElement("div");
            hint.className = "wb-stars-hint";
            hint.textContent = "← → to change   ·   Enter to commit   ·   Esc to cancel";
            panel.appendChild(hint);

            self._panel = panel;
            self._host.appendChild(panel);
            self._repaintStars();

            self._onKey = function (e) {
                // The keyboard is this cell's while it holds control. Nothing
                // here reaches the grid — the panel is outside the table — and
                // stopping propagation says so rather than relying on it.
                if (e.stopPropagation) e.stopPropagation();
                var key = e.key;
                if (key === "ArrowRight" || key === "ArrowLeft") {
                    if (e.preventDefault) e.preventDefault();
                    var step = (key === "ArrowRight") ? 1 : -1;
                    self._draft = Math.max(_WB_STARS_MIN, Math.min(_WB_STARS_MAX, self._draft + step));
                    self._repaintStars();
                } else if (key === "ArrowUp" || key === "ArrowDown") {
                    // Deliberately dead, and SWALLOWED: a rating has one axis,
                    // and letting these through would move the grid's cursor out
                    // from under an open editor.
                    if (e.preventDefault) e.preventDefault();
                } else if (key === "Enter") {
                    if (e.preventDefault) e.preventDefault();
                    self._end(true);
                } else if (key === "Escape") {
                    if (e.preventDefault) e.preventDefault();
                    self._end(false);
                }
            };
            self._onBlur = function () { self._end(false); };
            panel.addEventListener("keydown", self._onKey);
            panel.addEventListener("blur", self._onBlur);
            if (panel.focus) panel.focus();
        });
    }

    _repaintStars() {
        if (!this._stars) return;
        for (var k = 0; k < this._stars.length; k++) {
            var on = (k + 1) <= this._draft;
            this._stars[k].className = on ? "wb-star wb-star-on" : "wb-star";
            this._stars[k].textContent = on ? "★" : "☆";
        }
    }

    /**
     * Finish. Listeners off first, so removing a focused panel cannot re-enter
     * here through its own blur; then remove; then repaint; then tell the
     * owner; then settle.
     */
    _end(accept) {
        var panel = this._panel;
        if (!panel) return;
        var chosen = this._draft;
        panel.removeEventListener("keydown", this._onKey);
        panel.removeEventListener("blur", this._onBlur);
        this._onKey = null; this._onBlur = null;
        this._panel = null; this._stars = null; this._draft = null; this._host = null;
        if (panel.parentNode) {
            try { panel.parentNode.removeChild(panel); }
            catch (e) { /* already gone — the grid took its host down */ }
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
        if (this._panel) this._end(false);
        this._el = null;
    }
}
