// =============================================================================
// RelGridStockCellsModule — RFC 0050 · Episode 2's stock cells. DOMAIN-side code
// that ships with the grid: a relation's cell manager may build from these,
// and the grid never imports them.
//
// A RelGridTextCell holds what it shows. It is told to change by whoever owns it —
// set(v) — and by nobody else; the grid calls render(host) exactly once and
// nothing after. There is no update(value) for the grid to call, because the
// grid has no value to pass.
//
// THE CELL NEVER TAKES CONTROL UNASKED. It has no click handler: a single
// click is a shallow gesture and belongs to the grid. The grid offers control
// in TWO STAGES, and the cell answers both:
//
//   mayTakeControl()   may I, right now? Situational and synchronous. Here:
//                      only a cell with somewhere to commit, already mounted
//                      and not already deep.
//   takeControl()      the handover. Returns a PROMISE that settles when the
//                      cell is finished — however it finished. The grid never
//                      learns whether it committed or cancelled.
//
// The promise is the cell's own; the grid hands out no callback it could
// store, fire twice, or fire after the grid is gone. While deep the cell owns
// the keyboard: keys inside its input do not reach the grid.
//
//   new RelGridTextCell({ value?, onCommit? })
//     · onCommit(text) — when present the cell is editable. takeControl opens
//       an input; Enter reports the text to the owner through onCommit;
//       Escape, or losing focus, cancels. THE COMMIT IS THE OWNER'S — the
//       owner decides (persist, replicate, refuse) and the cell shows whatever
//       it then set()s. A cell without onCommit answers no to mayTakeControl,
//       is never offered the handover, and the grid stays shallow.
//
// No focused node is ever removed: ending an edit detaches the input's
// listeners FIRST, then removes it, then repaints, then reports, then hands
// control back — in that order, so nothing can re-enter.
//
// A cell that cannot edit marks its host hrg-text-ro — the predicate map 16
// argues for (law 116): an uneditable cell has no resting affordance to be
// missing, so the property has to be named. Whether it is painted is the
// theme's business; the stock cell ships one muted rule as a default.
// =============================================================================

var _HRG_STOCK_STYLE_ID = "homing-rel-grid-stock-style";
var _HRG_STOCK_STYLE_CSS = [
    ".hrg-text-ro{color:var(--color-text-muted);}",
    // OVER the slot, not in it: an editor in flow would widen its column to the
    // input's intrinsic width and shuffle every other column while it is open.
    ".hrg-text-edit{position:absolute;top:0;right:0;bottom:0;left:0;",
    "  box-sizing:border-box;width:100%;height:100%;border:0;padding:0 6px;",
    "  font:13px sans-serif;background:var(--color-surface);color:var(--color-text-primary);}"
].join("\n");

function _hrgStockEnsureStyle() {
    if (typeof document === "undefined" || !document.head) return;
    if (document.getElementById(_HRG_STOCK_STYLE_ID)) return;
    var s = document.createElement("style");
    s.id = _HRG_STOCK_STYLE_ID;
    s.textContent = _HRG_STOCK_STYLE_CSS;
    document.head.appendChild(s);
}

function _hrgStockAddClass(el, name) {
    var cur = el.className || "", parts = cur.split(/s+/);
    for (var i = 0; i < parts.length; i++) if (parts[i] === name) return;
    el.className = cur ? cur + " " + name : name;
}

class RelGridTextCell {

    constructor(opts) {
        opts = opts || {};
        this._el = null;
        this._value = (opts.value === undefined) ? null : opts.value;
        this._onCommit = (typeof opts.onCommit === "function") ? opts.onCommit : null;
        this._mode = "none";
        this._input = null;
        this._done = null;         // resolves the promise takeControl handed the grid
        this._onKey = null;
        this._onBlur = null;
    }

    _text() { return (this._value == null) ? "" : String(this._value); }

    _paint() {
        if (!this._el || this._input) return;
        this._el.textContent = this._text();
    }

    /**
     * Mount once into the grid-minted host. Nothing is wired: the grid
     * captures. A cell with no commit target names itself read-only.
     */
    render(host) {
        this._el = host;
        if (!this._onCommit) { _hrgStockEnsureStyle(); _hrgStockAddClass(host, "hrg-text-ro"); }
        this._paint();
        return this;
    }

    /** The owner changed the cell. Repaints unless an edit is in flight. */
    set(v) {
        this._value = (v === undefined) ? null : v;
        this._paint();
        return this;
    }

    value() { return this._value; }

    /** Told by the grid: 'none' | 'shallow' | 'deep'. Pure lifecycle. */
    onSelect(mode) { this._mode = mode; }
    mode() { return this._mode; }

    /**
     * STAGE 1 — may this cell take control right now? Situational and cheap:
     * asked afresh on every gesture, answered without side effects. No
     * somewhere to commit means no; not mounted means no; already deep means
     * no.
     */
    mayTakeControl() {
        return !!this._onCommit && !!this._el && !this._input;
    }

    /**
     * STAGE 2 — the handover, offered only after a yes. Opens the editor and
     * answers a promise the grid holds until this cell is FINISHED. What
     * finished it — Enter, Escape, a lost focus, the owner disposing us — is
     * not the grid's to know, so the promise resolves with nothing in every
     * case.
     */
    takeControl() {
        var self = this;
        return new Promise(function (resolve) {
            self._done = resolve;
            var input = document.createElement("input");
            input.value = self._text();
            input.className = "hrg-text-edit";
            self._input = input;
            // The text STAYS, holding the slot open at its natural size; the
            // input covers it. Clearing it would collapse the row instead.
            self._el.appendChild(input);
            self._onKey = function (e) {
                if (e.stopPropagation) e.stopPropagation();   // the keyboard is the cell's while deep
                if (e.key === "Enter")       { if (e.preventDefault) e.preventDefault(); self._end(true); }
                else if (e.key === "Escape") { if (e.preventDefault) e.preventDefault(); self._end(false); }
            };
            self._onBlur = function () { self._end(false); };
            input.addEventListener("keydown", self._onKey);
            input.addEventListener("blur", self._onBlur);
            if (input.focus) input.focus();
            if (input.select) input.select();
        });
    }

    /**
     * End the edit. The ORDER is the whole safety argument: detach the
     * listeners first, so removing the input cannot re-enter here through its
     * own blur; then remove; then repaint; then report to the owner; then hand
     * control back to the grid. Settling twice is impossible now — a promise
     * settles once — so the order is all that is left to get right.
     */
    _end(accept) {
        var input = this._input;
        if (!input) return;
        var text = input.value;
        input.removeEventListener("keydown", this._onKey);
        input.removeEventListener("blur", this._onBlur);
        this._onKey = null; this._onBlur = null;
        this._input = null;
        if (input.parentNode) {
            try { input.parentNode.removeChild(input); }
            catch (e) { /* already detached — nothing left to do */ }
        }
        this._paint();
        if (accept && this._onCommit) {
            try { this._onCommit(text); }
            catch (e) { console.error("[RelGridTextCell] onCommit threw:", e); }
        }
        var done = this._done;
        this._done = null;
        if (done) done();
    }

    /** The OWNER's to call, when it decides the cell is done. Never the grid's. */
    dispose() {
        if (this._input) this._end(false);
        this._el = null;
    }
}
