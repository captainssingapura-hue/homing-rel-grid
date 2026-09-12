// =============================================================================
// RelGridHeaderDragModule — RFC 0050 · Episode 2's header pointer gesture, round 1:
// RESIZE. An 8px handle element on the header's right edge (hover shows
// col-resize); STAGED — a guide line tracks the pointer, one request is
// reported on release, Escape abandons and reports nothing. Nothing in the
// cell tree is touched mid-gesture.
//
// Positional only — it reports (j, px) and knows no identity. The facade
// translates j to a column, bounds the request, holds it, applies it. That
// split is map 7's capture rule: the gesture MINTS; it never applies.
//
//   new RelGridHeaderDrag({ table, onColResize(j, px), heldWidth?(j), extent? }).wire(th, j)
//
// A drag starts from the width the column HOLDS when it holds one — what is
// seen is what the browser made of it, and a fixed layout in a wider box
// than its columns' sum gives every column more than it asked for, so a
// drag that started from the seen width would move the wrong way. The guide
// line spans the extent the host names, or the table — and an extent may be
// SEVERAL boxes, one segment of line each: a host that stacks tables under
// one header wants the line down each table and not across what sits
// between them.
// =============================================================================

/**
 * What is SEEN of a box, top to bottom: its rect, clipped by every ancestor
 * that scrolls or clips and by the viewport. A guide drawn down the whole
 * box would run on past the pane that cuts the box off — a table, or a stack
 * of them, is routinely taller than the pane it sits in.
 */
function _hrgSeenSpan(el) {
    var r = el.getBoundingClientRect(), top = r.top, bottom = r.bottom;
    if (typeof getComputedStyle === "function") {
        for (var p = el.parentNode; p && p.getBoundingClientRect; p = p.parentNode) {
            var cs = getComputedStyle(p);
            if (!cs) continue;
            if (/(auto|scroll|hidden|clip)/.test(String(cs.overflowY) + " " + String(cs.overflow))) {
                var pr = p.getBoundingClientRect();
                if (pr.top > top) top = pr.top;
                if (pr.bottom < bottom) bottom = pr.bottom;
            }
        }
    }
    if (typeof window !== "undefined" && window.innerHeight) {
        if (top < 0) top = 0;
        if (bottom > window.innerHeight) bottom = window.innerHeight;
    }
    return { top: top, height: bottom > top ? bottom - top : 0 };
}

class RelGridHeaderDrag {

    constructor(opts) {
        opts = opts || {};
        if (!opts.table) throw new Error("[RelGridHeaderDrag] opts.table is required");
        this._table = opts.table;
        this._onColResize = opts.onColResize || null;
        this._heldWidth = opts.heldWidth || null;              // (j) → px | null: the width held, if any
        this._extent = opts.extent || null;                    // what the guide spans — an element or a list; the table when absent
    }

    /** Wire the resize handle onto a freshly minted header cell. */
    wire(th, j) {
        if (!this._onColResize) return this;
        var self = this;
        var handle = document.createElement("span");
        handle.className = "hrg-resize-handle";
        th.appendChild(handle);
        handle.addEventListener("mousedown", function (e) {
            var rect = th.getBoundingClientRect ? th.getBoundingClientRect() : null;
            if (!rect || e.clientX == null) return;              // no geometry: inert (headless)
            if (e.preventDefault) e.preventDefault();
            if (e.stopPropagation) e.stopPropagation();          // the header body is not armed
            var held = self._heldWidth ? self._heldWidth(j) : null;
            self._start(j, held != null ? held : rect.right - rect.left, e.clientX);
        });
        return this;
    }

    /**
     * The guide: one segment of line per box of the extent, each down what is
     * SEEN of its box — not past the pane, and not across whatever sits
     * between two boxes. Answers the list of segments; empty when headless.
     */
    _makeGuide(atX) {
        var over = this._extent || this._table;
        var boxes = Array.isArray(over) ? over : [over], out = [];
        if (!document.body) return out;
        for (var k = 0; k < boxes.length; k++) {
            if (!boxes[k] || !boxes[k].getBoundingClientRect) continue;
            var span = _hrgSeenSpan(boxes[k]);
            if (span.height <= 0) continue;                    // out of view: no segment
            var guide = document.createElement("div");
            guide.className = "hrg-resize-guide";
            guide.style.setProperty("--hrg-guide-top", span.top + "px");
            guide.style.setProperty("--hrg-guide-h", span.height + "px");
            guide.style.setProperty("--hrg-guide-x", atX + "px");
            document.body.appendChild(guide);
            out.push(guide);
        }
        return out;
    }

    _start(j, startW, startX) {
        var self = this, lastX = startX;
        var guides = this._makeGuide(startX);
        function onMove(e) {
            lastX = e.clientX;
            for (var k = 0; k < guides.length; k++) guides[k].style.setProperty("--hrg-guide-x", lastX + "px");
        }
        function teardown() {
            document.removeEventListener("mousemove", onMove);
            document.removeEventListener("mouseup", onUp);
            document.removeEventListener("keydown", onKey, true);
            for (var k = 0; k < guides.length; k++)
                if (guides[k].parentNode) guides[k].parentNode.removeChild(guides[k]);
        }
        // One request, on release. Whole pixels: a header's rect is fractional
        // and a request is something a host may keep — normalisation only
        // bounds, so the gesture is where the number is made clean.
        function onUp() { teardown(); self._onColResize(j, Math.round(startW + (lastX - startX))); }
        function onKey(e) {                                       // Escape ABANDONS — nothing was minted
            if (e.key === "Escape") { teardown(); if (e.stopPropagation) e.stopPropagation(); }
        }
        document.addEventListener("mousemove", onMove);
        document.addEventListener("mouseup", onUp);
        document.addEventListener("keydown", onKey, true);
    }
}
