// =============================================================================
// ExtentCard — the Colour Extents bench's card: one card wearing three scaled
// words and one slider setting its extent. The property is a registered
// number, so the card's transition on it tweens the colours.
//
// Nothing here names a colour: the design owns every anchor, the card owns
// the number.
//
//   new ExtentCard(container, params)   params: none
//   (the rest is a BenchWidget's)
// =============================================================================

class ExtentCard extends BenchWidget {
    constructor(container, params) {
        super(container, "extentCard", "A card at an extent");
        var self = this, body = this.body;
        this.el("hint", "div", wb_hint, body,
            "ONE NUMBER, THREE WORDS — a card wearing its surface, its edge and its ink at an extent. The slider sets css.extent on the card " +
            "and the three move in step: elevation from sunk through the page to raised, the edge from calm through the hairline to " +
            "warning, the ink from muted through the body to the accent. The extent is a registered number, so the card’s transition on it " +
            "tweens the colours — no keyframes, no script.");

        var card = this._card = this.el("card", "div", wb_extent_card, body);
        this.el("title", "div", wb_extent_card_title, card, "A card at an extent");
        this.el("line", "div", null, card, "Surface, edge and ink follow one number. Drag the slider; the colours arrive over 400 ms.");

        var slider = this.el("slider", "div", wb_extent_slider, body);
        var range = this._range = this.el("range", "input", wb_extent_range, slider);
        range.type = "range"; range.min = "-1"; range.max = "1"; range.step = "0.01"; range.value = "1";
        range.setAttribute("aria-label", "extent");
        this._readout = this.el("readout", "span", null, slider);
        range.addEventListener("input", function () { self._apply(); });
        this.keysTo = range;
        this._apply();

        // three stops, for a click: the meaning turned the other way, neutral, the word
        var bar = this.el("bar", "div", wb_bar, body);
        [-1, 0, 1].forEach(function (x, k) {
            var label = x === -1 ? "−1  turned the other way" : x === 0 ? "0  neutral" : "+1  the word";
            self.button("stop" + k, label, function () { range.value = String(x); self._apply(); }, bar);
        });
    }

    _apply() {
        var x = Number(this._range.value);
        css.extent(this._card, x);
        this._readout.textContent = "extent " + (x >= 0 ? "+" : "") + x.toFixed(2);
    }
}
