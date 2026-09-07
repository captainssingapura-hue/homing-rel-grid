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
// THE CELL NEVER TAKES CONTROL. It has no click handler: a single click is a
// shallow gesture and belongs to the grid. The cell goes deep only when the
// grid asks — beginEdit(release) — and holds control until it calls release()
// back, once, whatever happened inside. While deep it owns the keyboard: keys
// inside its input do not reach the grid.
//
//   new RelGridTextCell({ value?, onCommit? })
//     · onCommit(text) — when present the cell is editable. beginEdit opens an
//       input; Enter reports the text to the owner through onCommit; Escape,
//       or losing focus, cancels. THE COMMIT IS THE OWNER'S — the owner decides
//       (persist, replicate, refuse) and the cell shows whatever it then
//       set()s. A cell without onCommit declines beginEdit, and the grid
//       stays shallow.
//
// No focused node is ever removed: ending an edit detaches the input's
// listeners FIRST, then removes it, then repaints, then reports, then hands
// control back — in that order, so nothing can re-enter.
// =============================================================================

class RelGridTextCell {

    constructor(opts) {
        opts = opts || {};
        this._el = null;
        this._value = (opts.value === undefined) ? null : opts.value;
        this._onCommit = (typeof opts.onCommit === "function") ? opts.onCommit : null;
        this._mode = "none";
        this._input = null;
        this._release = null;
        this._onKey = null;
        this._onBlur = null;
    }

    _text() { return (this._value == null) ? "" : String(this._value); }

    _paint() {
        if (!this._el || this._input) return;
        this._el.textContent = this._text();
    }

    /** Mount once into the grid-minted host. Nothing is wired: the grid captures. */
    render(host) {
        this._el = host;
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
     * The grid asks the cell to go deep, handing it release() to call when
     * done. Returns false to decline — a read-only cell, or one already deep —
     * and the grid stays shallow.
     */
    beginEdit(release) {
        if (!this._onCommit || !this._el || this._input) return false;
        var self = this;
        this._release = (typeof release === "function") ? release : function () {};
        var input = document.createElement("input");
        input.value = this._text();
        this._input = input;
        this._el.textContent = "";
        this._el.appendChild(input);
        this._onKey = function (e) {
            if (e.stopPropagation) e.stopPropagation();       // the keyboard is the cell's while deep
            if (e.key === "Enter")       { if (e.preventDefault) e.preventDefault(); self._end(true); }
            else if (e.key === "Escape") { if (e.preventDefault) e.preventDefault(); self._end(false); }
        };
        this._onBlur = function () { self._end(false); };
        input.addEventListener("keydown", this._onKey);
        input.addEventListener("blur", this._onBlur);
        if (input.focus) input.focus();
        if (input.select) input.select();
        return true;
    }

    /**
     * End the edit. The ORDER is the whole safety argument: detach the
     * listeners first, so removing the input cannot re-enter here through its
     * own blur; then remove; then repaint; then report to the owner; then hand
     * control back to the grid — exactly once.
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
        var release = this._release;
        this._release = null;
        if (release) release();
    }

    /** The OWNER's to call, when it decides the cell is done. Never the grid's. */
    dispose() {
        if (this._input) this._end(false);
        this._el = null;
    }
}
