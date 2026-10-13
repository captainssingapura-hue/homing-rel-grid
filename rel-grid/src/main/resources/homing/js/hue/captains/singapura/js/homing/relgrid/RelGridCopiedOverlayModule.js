// =============================================================================
// RelGridCopiedOverlayModule — RFC 0050 · Episode 2, appendix The Copied Range:
// the COPIED STATE SHOWN - an overlay over each block the grid last copied, and
// none when nothing is. Its being there is the state shown; nothing is toggled
// on it. It decides nothing: the facade hands it the rectangles the state stands
// as, and it draws them.
//
// Each overlay is TWO PARTS, each one element wearing its leaf of the frame:
//   THE AREA   - a box over the block's slots (CopiedArea): a tint at most, the
//                cells showing through.
//   THE BORDER - a line round the area, on the block's edge (CopiedBorder): a
//                vector rectangle, never filled, so a design's effect can move
//                its dashes.
// What each looks like - a colour, an animation effect, or nothing - is the
// design's; the overlay wears the leaves and learns no class.
//
// Mechanics only: placed in the wrapper's coordinates over the slots, so it
// scrolls with the table; stacked over the slots' marks and a merged cell's host,
// under the header, the editor's anchor and the mask; never in the pointer's way;
// hidden from a screen reader; placed again whenever the slots move. Minted on a
// sub-branch of the grid's own, dissolved with it.
//
//   new RelGridCopiedOverlay({ branch, wrap, slotAt, design })
//   paint(rects) / place() / close() / boxes()
// =============================================================================

class RelGridCopiedOverlay {

    constructor(opts) {
        this._branch = opts.branch;          // the grid's own; the overlays are a sub-branch of it
        this._wrap = opts.wrap;
        this._slotAt = opts.slotAt;          // (i, j) → td | null, the layout's
        this._design = opts.design;
        this._sub = null;
        this._boxes = [];                    // { rect, area, line }
        this._n = 0;
    }

    /** The overlays for these rectangles of the presented space, and no others. */
    paint(rects) {
        this.close();
        if (!rects || !rects.length) return this;
        var sub = this._sub = this._branch.createBranch("copied");
        sub.activate(this);
        for (var k = 0; k < rects.length; k++) {
            var n = ++this._n;
            var area = sub.createElement("copied-" + n, "div");
            css.addClass(area, hrg_copied);
            area.setAttribute("aria-hidden", "true");
            this._design.wear(GRID_FRAME.COPIED_AREA, area);
            var svg = sub.createSvgElement("copied-line-" + n, "svg");
            css.addClass(svg, hrg_copied_line);
            var line = sub.createSvgElement("copied-rect-" + n, "rect");
            line.setAttribute("width", "100%");
            line.setAttribute("height", "100%");
            css.addClass(line, hrg_copied_rect);
            this._design.wear(GRID_FRAME.COPIED_BORDER, line);
            svg.appendChild(line);
            area.appendChild(svg);
            this._wrap.appendChild(area);
            this._boxes.push({ rect: rects[k], area: area, line: line });
        }
        return this.place();
    }

    /** Each overlay over its block's slots, as they are now. */
    place() {
        var w = this._wrap.getBoundingClientRect();
        for (var k = 0; k < this._boxes.length; k++) {
            var b = this._boxes[k], r = b.rect;
            var a = this._slotAt(r.i0, r.j0), z = this._slotAt(r.i1, r.j1) || a;
            if (!a) continue;
            var ra = a.getBoundingClientRect(), rz = z.getBoundingClientRect(), st = b.area.style;
            if (!st || !st.setProperty) continue;
            st.setProperty("--hrg-left", (ra.left - w.left) + "px");
            st.setProperty("--hrg-top", (ra.top - w.top) + "px");
            st.setProperty("--hrg-width", (rz.right - ra.left) + "px");
            st.setProperty("--hrg-height", (rz.bottom - ra.top) + "px");
        }
        return this;
    }

    /** Every overlay down - their branch dissolved. */
    close() {
        if (this._sub) { this._sub.dissolve(); this._sub = null; }
        this._boxes = [];
        return this;
    }

    /** What is drawn: each overlay's rectangle and its two parts. */
    boxes() {
        return this._boxes.map(function (b) { return { rect: b.rect, area: b.area, line: b.line }; });
    }
}
