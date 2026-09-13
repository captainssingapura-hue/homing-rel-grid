// =============================================================================
// HanCell — one square, one glyph. DOMAIN CODE, the Han Article bench's cell,
// written against the cell contract and shipped with nothing. A DISPLAY cell:
// the article is edited as text, elsewhere, and a square only ever shows what
// the layout put in it.
//
// STRICTLY SQUARE, by construction rather than by a number: the cell fills
// the width the grid gives its column and is exactly as tall as it is wide
// (aspect-ratio 1), so the column's width is the only geometry anyone sets
// and the square follows it — through a resize, through a theme, through
// anything. The glyph is sized from the square's HEIGHT (container units),
// which is the one measure every kind of slot shares: a half-square is half
// as wide as a square but as tall, a run is n squares wide but one tall, and
// all of them draw their ink at 68% of the row.
//
// A FIXED SIDE, when a host wants one: set `--han-side` on any ancestor and
// the height is that instead of the width, for every slot, so the ROW NEVER
// MOVES when a column does — a narrow column clips its glyph, a wide one has
// slack, and the ink stays one size. The height being explicit, the aspect
// ratio has nothing left to decide. A table whose columns resize wants this;
// the article, whose nine columns are one width, does not.
//
// PUNCTUATION shares a square: a slot may hold two marks, and a lone mark
// takes the left half. Each mark is drawn in a half-width box with the
// font's half-width alternates (halt) asked for, which is what makes 。，
// sit in half an em in a font that has them. In a font that does not — and
// the one this machine renders with does not — the box CLIPS the full-width
// mark to its half, and which half survives is chosen by where the ink is:
// a mainland-style font keeps 。，、：；！？ in the left of the em and the
// opening brackets 「『（《〈【〔 in the right, so those are aligned right and
// everything else left. Measured, not assumed: a pair of halves left at
// their min-content width grew to a full em each and overflowed the square.
//
// A NOUN: the square is the cell's own element, minted on the branch it was
// handed — a DomOpsParty branch of its own, its owner's gift — and the grid
// places it. The contract, the display half of it:
//
//   cellElement()       the square, minted once; the grid places it
//   set(glyph)          the OWNER changed it; repaint
//   onSelect(mode)      'none' | 'shallow' | 'deep' — pure lifecycle
//   dispose()           the owner's, never the grid's; dissolves the branch
//
// A NARROW cell is a half-square: the leading or trailing column's, half as
// wide as a square and as tall as one, holding one squeezed mark at the same
// size the squares draw theirs. Which half of the em survives the clip is
// decided as for a pair — an opener keeps its right, anything else its left.
//
// A RUN of narrow characters — Latin, digits — is one cell reaching over
// several squares, two characters a square. It answers colSpan() with its
// reach, and the grid, built with mergedCells, lays a host over the squares
// it reaches across and places the cell there; the cell fills that host, and
// the squares it covers hold cells of their own that show nothing. The run's
// letters are sized so two of them sit in a square.
//
// No mayTakeControl and no takeControl, on purpose: a cell offering neither
// is never asked, so Enter on a square does nothing and the grid stays
// shallow. Editing is the text editor's.
//
// The ink is three spans the cell mints once and keeps — a mark for a
// character or a run, and two halves for punctuation — and a paint ATTACHES
// the ones the slot's kind needs and detaches the rest. Nothing is re-minted
// and nothing is wiped: a slot changes kind as the article moves under it,
// and the spans simply change places.
//
//   new HanCell({ branch, glyph?, narrow?, span? })
// =============================================================================

// THE LOOKS ARE TYPED — HanCellStyles, applied through the css manager. What
// the old sheet said with a descendant selector — a run's ink in the Latin
// serif, a half-square's box at full width — is a custom property the kind
// sets on the square and the ink reads.

class HanCell {

    constructor(opts) {
        opts = opts || {};
        if (!opts.branch) throw new Error("[HanCell] opts.branch is required: the cell's own");
        this._branch = opts.branch;
        this._branch.activate(this);
        this._el = null;
        this._ink = null;
        this._mark = null;         // one character, or a run
        this._halves = null;       // two half-width boxes, for marks
        this._glyph = (typeof opts.glyph === "string" && opts.glyph.length) ? opts.glyph : null;
        this._narrow = opts.narrow === true;
        this._span = (opts.span > 1) ? Math.floor(opts.span) : 1;
        this._mode = "none";
    }

    /** How many squares this cell reaches over — read by a grid built with mergedCells. */
    colSpan() { return this._span; }

    _detach(child) { if (child.parentNode) child.parentNode.removeChild(child); }

    /**
     * A character is the square's text, in the mark. A mark, or a pair of
     * marks, is one half-width box each, side by side; a lone mark leaves its
     * right half empty. The spans are attached or detached, never rebuilt,
     * because a slot changes kind as the article moves under it.
     */
    _paint() {
        if (!this._ink) return;
        var ink = this._ink, el = this._el, mark = this._mark, halves = this._halves;
        // The reach: a run's box is as wide as its squares; anything else is one.
        // The number itself is the grid's business (colSpan); the class is enough
        // for the drawing, which is sized from the height and not the reach.
        css.toggleClass(el, han_run, this._span > 1);
        var g = this._glyph;
        if (g == null) {
            this._detach(mark); this._detach(halves[0]); this._detach(halves[1]);
            css.removeClass(ink, han_punct);
            return;
        }
        var marks = Array.from(g);
        if (this._span > 1 || (marks.length === 1 && !hanIsPunct(marks[0]))) {
            this._detach(halves[0]); this._detach(halves[1]);
            mark.textContent = g;
            ink.appendChild(mark);
            css.removeClass(ink, han_punct);
            return;
        }
        this._detach(mark);
        css.addClass(ink, han_punct);
        for (var k = 0; k < 2; k++) {
            if (k >= marks.length) { this._detach(halves[k]); continue; }
            css.toggleClass(halves[k], han_open, hanIsOpener(marks[k]));
            halves[k].textContent = marks[k];
            ink.appendChild(halves[k]);
        }
    }

    /** The square, minted once on the cell's branch with its ink; the grid places it. */
    cellElement() {
        if (this._el) return this._el;
        var b = this._branch;
        var el = b.createElement("cell", "div");
        css.addClass(el, han_glyph);
        if (this._narrow) css.addClass(el, han_narrow);
        var ink = b.createElement("ink", "span");
        css.addClass(ink, han_ink);
        el.appendChild(ink);
        this._mark = b.createElement("mark", "span");
        this._halves = [b.createElement("half-0", "span"), b.createElement("half-1", "span")];
        css.addClass(this._halves[0], han_half);
        css.addClass(this._halves[1], han_half);
        this._el = el;
        this._ink = ink;
        this._paint();
        return el;
    }

    /** The owner changed it — and, for a run, how far it reaches. */
    set(glyph, span) {
        this._glyph = (typeof glyph === "string" && glyph.length) ? glyph : null;
        this._span = (span > 1) ? Math.floor(span) : 1;
        this._paint();
        return this;
    }

    value()  { return this._glyph; }
    narrow() { return this._narrow; }

    onSelect(mode) { this._mode = mode; }
    mode() { return this._mode; }

    dispose() {
        this._el = null;
        this._ink = null;
        this._mark = null;
        this._halves = null;
        this._branch.dissolve();
    }
}
