// =============================================================================
// HanCell — one square, one glyph. DOMAIN CODE, the Han Article bench's cell,
// written against the cell contract and shipped with nothing.
//
// STRICTLY SQUARE, by construction rather than by a number: the cell fills
// the width the grid gives its column and is exactly as tall as it is wide
// (aspect-ratio 1), so the column's width is the only geometry anyone sets
// and the square follows it — through a resize, through a theme, through
// anything. The glyph is sized from the square too (container units), so it
// scales with it.
//
// The contract, answered as the stock cell answers it:
//
//   render(host)        mount once into the element the grid minted
//   set(glyph)          the OWNER changed it; repaint unless an edit is open
//   onSelect(mode)      'none' | 'shallow' | 'deep'
//   mayTakeControl()    nowhere to commit, not mounted, or already open: no
//   takeControl(host)   a square input in the anchor the grid minted, and a
//                       promise that settles when this cell is finished
//   dispose()           the owner's
//
// The editor is an <input> because an <input> is what an IME composes into.
// Enter commits whatever is in it — one character REPLACES the glyph, several
// INSERT, none DELETES it — which is the whole editing model of this
// iteration and enough to write a poem with. Enter DURING a composition is
// the IME's, not ours: a Chinese input method uses Enter to take the
// candidate, and a cell that committed on that keystroke would commit the
// pinyin. Escape and blur cancel.
//
//   new HanCell({ glyph?, onCommit? })
// =============================================================================

var _HAN_STYLE_ID = "bench-han-style";
var _HAN_CSS = [
    // The square. Width from the column; height from the width; the glyph
    // measured against it. `container-type` is what makes cqw units below
    // refer to THIS box.
    ".han-glyph{width:100%;aspect-ratio:1/1;box-sizing:border-box;container-type:inline-size;",
    "  display:grid;place-items:center;overflow:hidden;}",
    ".han-ink{font:68cqw/1 'Noto Serif CJK SC','Source Han Serif SC','Songti SC','SimSun','PMingLiU',serif;",
    "  color:var(--color-text-primary);user-select:none;-webkit-user-select:none;}",
    // Editing: the same square, now an input, in the anchor the grid minted.
    ".han-editing{background:var(--color-surface-raised);}",
    ".han-input{width:100%;height:100%;border:0;padding:0;margin:0;box-sizing:border-box;",
    "  text-align:center;background:transparent;outline:none;color:var(--color-text-primary);",
    "  font:68cqw/1 'Noto Serif CJK SC','Source Han Serif SC','Songti SC','SimSun','PMingLiU',serif;}"
].join("\n");

function _hanEnsureStyle() {
    if (typeof document === "undefined" || !document.head) return;
    if (document.getElementById(_HAN_STYLE_ID)) return;
    var s = document.createElement("style");
    s.id = _HAN_STYLE_ID;
    s.textContent = _HAN_CSS;
    document.head.appendChild(s);
}

function _hanAddClass(el, name) {
    var cur = el.className || "", parts = cur.split(/\s+/);
    for (var i = 0; i < parts.length; i++) if (parts[i] === name) return;
    el.className = cur ? cur + " " + name : name;
}

class HanCell {

    constructor(opts) {
        opts = opts || {};
        this._el = null;
        this._ink = null;
        this._glyph = (typeof opts.glyph === "string" && opts.glyph.length) ? opts.glyph : null;
        this._onCommit = (typeof opts.onCommit === "function") ? opts.onCommit : null;
        this._mode = "none";
        this._input = null;
        this._box = null;
        this._host = null;
        this._done = null;
        this._composing = false;
        this._onKey = null; this._onBlur = null; this._onCompStart = null; this._onCompEnd = null;
    }

    _paint() {
        if (!this._ink || this._input) return;
        this._ink.textContent = (this._glyph == null) ? "" : this._glyph;
    }

    render(host) {
        this._el = host;
        _hanEnsureStyle();
        _hanAddClass(host, "han-glyph");
        var ink = document.createElement("span");
        ink.className = "han-ink";
        host.appendChild(ink);
        this._ink = ink;
        this._paint();
        return this;
    }

    set(glyph) {
        this._glyph = (typeof glyph === "string" && glyph.length) ? glyph : null;
        this._paint();
        return this;
    }

    value() { return this._glyph; }

    onSelect(mode) { this._mode = mode; }
    mode() { return this._mode; }

    mayTakeControl() {
        return !!this._onCommit && !!this._el && !this._input;
    }

    takeControl(host) {
        var self = this;
        return new Promise(function (resolve) {
            self._done = resolve;
            self._host = host || self._el;

            var box = document.createElement("div");
            box.className = "han-glyph han-editing";
            var input = document.createElement("input");
            input.className = "han-input";
            input.type = "text";
            input.value = (self._glyph == null) ? "" : self._glyph;
            box.appendChild(input);
            self._box = box;
            self._input = input;
            self._host.appendChild(box);

            self._composing = false;
            self._onCompStart = function () { self._composing = true; };
            self._onCompEnd   = function () { self._composing = false; };
            self._onKey = function (e) {
                if (e.stopPropagation) e.stopPropagation();
                // An IME's Enter takes the candidate; it is not ours to commit on.
                if (e.isComposing || self._composing || e.keyCode === 229) return;
                if (e.key === "Enter") {
                    if (e.preventDefault) e.preventDefault();
                    self._end(true);
                } else if (e.key === "Escape") {
                    if (e.preventDefault) e.preventDefault();
                    self._end(false);
                }
            };
            self._onBlur = function () { self._end(false); };
            input.addEventListener("compositionstart", self._onCompStart);
            input.addEventListener("compositionend", self._onCompEnd);
            input.addEventListener("keydown", self._onKey);
            input.addEventListener("blur", self._onBlur);
            if (input.focus) input.focus();
            if (input.select) input.select();
        });
    }

    /** Listeners off first, so removing a focused input cannot re-enter here through its own blur. */
    _end(accept) {
        var input = this._input, box = this._box;
        if (!input) return;
        var text = input.value;
        input.removeEventListener("keydown", this._onKey);
        input.removeEventListener("blur", this._onBlur);
        input.removeEventListener("compositionstart", this._onCompStart);
        input.removeEventListener("compositionend", this._onCompEnd);
        this._onKey = null; this._onBlur = null; this._onCompStart = null; this._onCompEnd = null;
        this._input = null; this._box = null; this._host = null;
        if (box && box.parentNode) {
            try { box.parentNode.removeChild(box); }
            catch (e) { /* already gone — the grid took its host down */ }
        }
        this._paint();
        if (accept && this._onCommit) {
            try { this._onCommit(text); }
            catch (e) { console.error("[HanCell] onCommit threw:", e); }
        }
        var done = this._done;
        this._done = null;
        if (done) done();
    }

    dispose() {
        if (this._input) this._end(false);
        this._el = null;
        this._ink = null;
    }
}
