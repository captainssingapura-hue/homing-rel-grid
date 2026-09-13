// =============================================================================
// RelGridStockCellsModule — RFC 0050 · Episode 2's stock cells. DOMAIN-side code
// that ships with the grid: a relation's cell manager may build from these,
// and the grid never imports them.
//
// A CELL IS A NOUN. It holds what it shows, and it OWNS what it is made of:
// its element is minted on the branch it was handed — a DomOpsParty branch
// of its own, its owner's gift — and the grid never mints anything for it.
// The grid asks cellElement() once, PLACES the element in a slot, and asks
// nothing after; a cell has no behaviour towards the grid at all. It is told
// to change by whoever owns it — set(v) — and by nobody else. There is no
// update(value) for the grid to call, because the grid has no value to pass.
//
// THE CELL NEVER TAKES CONTROL UNASKED. It has no click handler: a single
// click is a shallow gesture and belongs to the grid. The grid offers control
// in TWO STAGES, and the cell answers both — with one more noun between them:
//
//   mayTakeControl()   may I, right now? Situational and synchronous. Here:
//                      only a cell with somewhere to commit, already placed
//                      and not already deep.
//   editorElement()    the editor, as an element the cell owns — minted once
//                      on its branch and kept. The grid places it in an
//                      anchor laid over the slot but OUTSIDE the table, and
//                      gives it the focus. Never the cell's own element,
//                      which is left exactly as it was showing.
//   takeControl()      the handover, once the editor is placed. Returns a
//                      PROMISE that settles when the cell is finished —
//                      however it finished. The grid never learns whether it
//                      committed or cancelled.
//
// The promise is the cell's own; the grid hands out no callback it could
// store, fire twice, or fire after the grid is gone. While deep the cell owns
// the keyboard: keys inside its input do not reach the grid.
//
//   new RelGridTextCell({ branch, value?, onCommit? })
//     · branch — the cell's OWN DomOpsParty branch (unactivated; the cell
//       activates it, and dispose() dissolves it). The owner mints one per
//       cell from a branch of its own.
//     · onCommit(text) — when present the cell is editable. takeControl arms
//       the input; Enter reports the text to the owner through onCommit;
//       Escape, or losing focus, cancels. THE COMMIT IS THE OWNER'S — the
//       owner decides (persist, replicate, refuse) and the cell shows whatever
//       it then set()s. A cell without onCommit answers no to mayTakeControl,
//       is never offered the handover, and the grid stays shallow.
//
// No focused node is ever removed by the cell: ending an edit detaches the
// input's listeners FIRST, then repaints, then reports, then hands control
// back — in that order, so nothing can re-enter. The input stays where the
// grid placed it; the grid takes its anchor down with the input inside, and
// the same input is placed again the next time control is offered.
//
// A cell that cannot edit marks its element hrg-text-ro — the predicate map
// 16 argues for (law 116): an uneditable cell has no resting affordance to be
// missing, so the property has to be named. Whether it is painted is the
// theme's business; the stock cell ships one muted rule as a default. And it
// honours what the slot asks of its text: nowrap is inherited, the ellipsis
// is read from --hrg-text-overflow — the grid's published property — because
// the text is this element's, and the slot only clips.
// =============================================================================

// THE LOOKS ARE TYPED — RelGridStockStyles, the cell's own group, applied
// through the css manager: hrg_text on the element, hrg_text_ro when it
// cannot edit, hrg_text_edit on the editor.

class RelGridTextCell {

    constructor(opts) {
        opts = opts || {};
        if (!opts.branch) throw new Error("[RelGridTextCell] opts.branch is required: the cell's own");
        this._branch = opts.branch;
        this._branch.activate(this);
        this._el = null;           // minted on first cellElement()
        this._value = (opts.value === undefined) ? null : opts.value;
        this._onCommit = (typeof opts.onCommit === "function") ? opts.onCommit : null;
        this._mode = "none";
        this._input = null;        // minted on first editorElement(), kept
        this._editing = false;     // true between takeControl() and its settle
        this._done = null;         // resolves the promise takeControl handed the grid
        this._onKey = null;
        this._onBlur = null;
    }

    _text() { return (this._value == null) ? "" : String(this._value); }

    _paint() {
        if (!this._el || this._editing) return;
        this._el.textContent = this._text();
    }

    /**
     * The cell's element, minted once on its branch and answered the same
     * every time. Nothing is wired: the grid captures. A cell with no commit
     * target names itself read-only.
     */
    cellElement() {
        if (!this._el) {
            this._el = this._branch.createElement("cell", "div");
            css.addClass(this._el, hrg_text);
            if (!this._onCommit) css.addClass(this._el, hrg_text_ro);
            this._paint();
        }
        return this._el;
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
     * somewhere to commit means no; not placed means no; already deep means
     * no.
     */
    mayTakeControl() {
        return !!this._onCommit && !!this._el && !this._editing;
    }

    /**
     * THE EDITOR — an input the cell owns, minted once and kept across
     * sessions. The grid places it in the anchor over the slot and gives it
     * the focus; the cell never touches the anchor.
     */
    editorElement() {
        if (!this._input) {
            this._input = this._branch.createElement("editor", "input");
            css.addClass(this._input, hrg_text_edit);
        }
        return this._input;
    }

    /**
     * STAGE 2 — the handover, offered only after a yes and once the editor is
     * placed. Arms the editor and answers a promise the grid holds until this
     * cell is FINISHED. What finished it — Enter, Escape, a lost focus, the
     * owner disposing us — is not the grid's to know, so the promise resolves
     * with nothing in every case.
     */
    takeControl() {
        var self = this, input = this.editorElement();
        return new Promise(function (resolve) {
            self._done = resolve;
            self._editing = true;
            input.value = self._text();
            self._onKey = function (e) {
                if (e.stopPropagation) e.stopPropagation();   // the keyboard is the cell's while deep
                if (e.key === "Enter")       { if (e.preventDefault) e.preventDefault(); self._end(true); }
                else if (e.key === "Escape") { if (e.preventDefault) e.preventDefault(); self._end(false); }
            };
            self._onBlur = function () { self._end(false); };
            input.addEventListener("keydown", self._onKey);
            input.addEventListener("blur", self._onBlur);
            if (input.select) input.select();                 // the whole text, ready to be replaced
        });
    }

    /**
     * End the edit. The ORDER is the whole safety argument: detach the
     * listeners first, so nothing the grid does with the anchor can re-enter
     * here through the input's blur; then repaint; then report to the owner;
     * then hand control back to the grid. Settling twice is impossible — a
     * promise settles once — so the order is all that is left to get right.
     */
    _end(accept) {
        if (!this._editing) return;
        var input = this._input, text = input.value;
        input.removeEventListener("keydown", this._onKey);
        input.removeEventListener("blur", this._onBlur);
        this._onKey = null; this._onBlur = null;
        this._editing = false;
        this._paint();
        if (accept && this._onCommit) {
            try { this._onCommit(text); }
            catch (e) { console.error("[RelGridTextCell] onCommit threw:", e); }
        }
        var done = this._done;
        this._done = null;
        if (done) done();
    }

    /** The OWNER's to call, when it decides the cell is done. Never the grid's. Dissolves the cell's branch. */
    dispose() {
        if (this._editing) this._end(false);
        this._el = null;
        this._input = null;
        this._branch.dissolve();
    }
}
