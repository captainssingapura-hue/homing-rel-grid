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
//   new RelGridHeaderDrag({ table, onColResize(j, px) }).wire(th, j)
// =============================================================================

class RelGridHeaderDrag {

    constructor(opts) {
        opts = opts || {};
        if (!opts.table) throw new Error("[RelGridHeaderDrag] opts.table is required");
        this._table = opts.table;
        this._onColResize = opts.onColResize || null;
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
            self._start(j, rect.right - rect.left, e.clientX);
        });
        return this;
    }

    _makeGuide(atX) {
        var rect = this._table.getBoundingClientRect ? this._table.getBoundingClientRect() : null;
        if (!document.body || !rect) return null;
        var guide = document.createElement("div");
        guide.className = "hrg-resize-guide";
        guide.style.setProperty("--hrg-guide-top", rect.top + "px");
        guide.style.setProperty("--hrg-guide-h", rect.height + "px");
        guide.style.setProperty("--hrg-guide-x", atX + "px");
        document.body.appendChild(guide);
        return guide;
    }

    _start(j, startW, startX) {
        var self = this, lastX = startX;
        var guide = this._makeGuide(startX);
        function onMove(e) {
            lastX = e.clientX;
            if (guide) guide.style.setProperty("--hrg-guide-x", lastX + "px");
        }
        function teardown() {
            document.removeEventListener("mousemove", onMove);
            document.removeEventListener("mouseup", onUp);
            document.removeEventListener("keydown", onKey, true);
            if (guide && guide.parentNode) guide.parentNode.removeChild(guide);
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
