// =============================================================================
// DishStarsCell — a health rating, 1 to 5, edited with a CUSTOM CONTROL. DOMAIN
// CODE, written by the bench rather than shipped with the grid, and that is the
// point of it: until it existed the stock text cell was the only implementation
// of the cell contract, so the contract had only ever been proved against the
// one thing it was written for.
//
// This is a cell that is not text (map 17), and its editor is not a form
// element either. It shows stars; it edits with a PANEL far larger than the
// cell in both directions, hung off the anchor the grid places it in; and
// while it holds control it owns the keyboard outright:
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
// A NOUN, as the stock cell is: it owns its element and its editor, both
// minted on the branch it was handed — a DomOpsParty branch of its own, its
// owner's gift — and the grid mints nothing for it. The contract is answered
// exactly as the stock cell answers it:
//
//   cellElement()       the cell's element, minted once; the grid places it
//   set(v)              the OWNER changed it; repaint unless an edit is open
//   onSelect(mode)      'none' | 'shallow' | 'deep' — pure lifecycle
//   mayTakeControl()    stage one: nowhere to commit, not placed, or already
//                       open all mean no
//   editorElement()     the panel, minted once and kept; the grid places it
//                       in the anchor and gives it the focus
//   takeControl()       stage two: arm the panel and answer a PROMISE that
//                       settles when this cell is finished
//   dispose()           the owner's, never the grid's; dissolves the branch
//
// The same teardown discipline as the stock cell, for the same reason: detach
// the listeners FIRST, so nothing the grid does with a focused panel can
// re-enter through its own blur; then repaint; then report to the owner;
// then settle. The panel stays where the grid placed it; the grid takes the
// anchor down with it inside, and places the same panel again next time.
//
//   new DishStarsCell({ branch, value?, onCommit? })
// =============================================================================

var _WB_STARS_STYLE_ID = "bench-stars-style";
var _WB_STARS_CSS = [
    ".wb-stars{font:13px sans-serif;letter-spacing:2px;padding:0 6px;}",
    ".wb-stars-ro{opacity:0.55;}",
    // The PANEL. Hung off the anchor the grid placed it in — which is exactly
    // the cell — so it is free to be much larger in both directions. Out of
    // the table it costs the table nothing: no column widens, no row grows.
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
        if (!opts.branch) throw new Error("[DishStarsCell] opts.branch is required: the cell's own");
        this._branch = opts.branch;
        this._branch.activate(this);
        this._el = null;           // minted on first cellElement()
        this._value = this._clamp(opts.value);
        this._onCommit = (typeof opts.onCommit === "function") ? opts.onCommit : null;
        this._mode = "none";
        this._panel = null;        // the editor, minted on first editorElement() and kept
        this._stars = null;        // its five star elements
        this._draft = null;        // the rating being chosen, committed only on accept
        this._editing = false;     // true between takeControl() and its settle
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
        if (!this._el || this._editing) return;
        this._el.textContent = this._text();
    }

    /** The cell's element, minted once on its branch; the grid places it. */
    cellElement() {
        if (!this._el) {
            _wbStarsEnsureStyle();
            this._el = this._branch.createElement("cell", "span");
            _wbStarsAddClass(this._el, "wb-stars");
            if (!this._onCommit) _wbStarsAddClass(this._el, "wb-stars-ro");
            this._paint();
        }
        return this._el;
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
        return !!this._onCommit && !!this._el && !this._editing;
    }

    /**
     * THE EDITOR: the panel, minted once on the cell's branch and kept. Five
     * stars and a hint; the grid places it in the anchor over the slot and
     * gives it the focus.
     */
    editorElement() {
        if (this._panel) return this._panel;
        var self = this, b = this._branch;
        _wbStarsEnsureStyle();
        var panel = b.createElement("editor", "div");
        panel.className = "wb-stars-panel";
        panel.tabIndex = 0;                       // a property, so no attribute is needed
        var row = b.createElement("stars", "div");
        row.className = "wb-stars-row";
        this._stars = [];
        for (var k = _WB_STARS_MIN; k <= _WB_STARS_MAX; k++) {
            var star = b.createElement("star-" + k, "span");
            star.className = "wb-star";
            star.textContent = "★";
            // Keep the focus on the panel: a press inside must not blur it,
            // because a blur is a cancel.
            star.addEventListener("mousedown", function (e) {
                if (e.preventDefault) e.preventDefault();
            });
            star.addEventListener("click", (function (n) {
                return function () { if (self._editing) { self._draft = n; self._repaintStars(); self._end(true); } };
            })(k));
            row.appendChild(star);
            this._stars.push(star);
        }
        panel.appendChild(row);
        var hint = b.createElement("hint", "div");
        hint.className = "wb-stars-hint";
        hint.textContent = "← → to change   ·   Enter to commit   ·   Esc to cancel";
        panel.appendChild(hint);
        this._panel = panel;
        return panel;
    }

    /**
     * Stage two. Arms the panel — placed by the grid already — and answers a
     * promise that settles when this cell is finished — committed, cancelled,
     * or blurred away. The grid learns which of those it was: not at all.
     */
    takeControl() {
        var self = this, panel = this.editorElement();
        return new Promise(function (resolve) {
            self._done = resolve;
            self._editing = true;
            self._draft = (self._value == null) ? _WB_STARS_MIN : self._value;
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
     * Finish. Listeners off first, so nothing done with the focused panel can
     * re-enter here through its own blur; then repaint; then tell the owner;
     * then settle. The panel stays where the grid placed it.
     */
    _end(accept) {
        if (!this._editing) return;
        var panel = this._panel, chosen = this._draft;
        panel.removeEventListener("keydown", this._onKey);
        panel.removeEventListener("blur", this._onBlur);
        this._onKey = null; this._onBlur = null;
        this._editing = false; this._draft = null;
        this._paint();
        if (accept && this._onCommit) {
            try { this._onCommit(chosen); }
            catch (e) { console.error("[DishStarsCell] onCommit threw:", e); }
        }
        var done = this._done;
        this._done = null;
        if (done) done();
    }

    /** The OWNER's to call. Never the grid's. Dissolves the cell's branch. */
    dispose() {
        if (this._editing) this._end(false);
        this._el = null;
        this._panel = null; this._stars = null;
        this._branch.dissolve();
    }
}
