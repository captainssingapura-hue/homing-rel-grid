// =============================================================================
// RelGridCopiedModule — RFC 0050 · Episode 2, appendix The Copied Range: THE
// COPIED STATE, kept with the cursor and the selection - nothing, or the blocks
// of the last copy the grid wrote, by identity (map 6, law 47). It means COPIED
// FROM HERE, LAST, never "on the clipboard": a page cannot see the clipboard
// change, and another application's copy never reaches the grid.
//
// The state decides nothing about looks: the facade sets and clears it - a copy
// written sets it, Escape's first stage, a cell given control and the grid's
// end clear it - and the overlays show it.
//
// WHERE IT STANDS, under the View as it is now: each block a rectangle of the
// presented space. Its columns, every one presented and side by side in order;
// its rows, the presented ones a run in order, and the absent ones at its ends
// only - a window shows part of a tall block. A block whose identities no
// longer make a rectangle - sorted apart, filtered through, a column hidden -
// does not stand, and the copy is cancelled; one wholly out of the window is
// kept, and drawn nowhere.
//
//   new RelGridCopied()
//   set(blocks) / clear() → whether there was one / has() / blocks() / identities()
//   rects(maps) → [{ i0, j0, i1, j1 }] each block stands as, or null when one no longer stands
//   show(maps, paint) → paint(rects) where it stands; a block that no longer stands cancels the copy
// =============================================================================

class RelGridCopied {

    constructor() { this._blocks = null; }

    /** The blocks of a copy written: the last copy, replacing any. */
    set(blocks) {
        this._blocks = (blocks && blocks.length) ? blocks.slice() : null;
        return this;
    }

    /** Nothing copied any more. Whether there was a copy to cancel. */
    clear() {
        var had = this._blocks !== null;
        this._blocks = null;
        return had;
    }

    has() { return this._blocks !== null; }

    /** The blocks, as identities: pks down, columns across. */
    blocks() { return this._blocks ? this._blocks.slice() : []; }

    /** The blocks as plain identities, a host's to keep. */
    identities() { return this.blocks().map(function (b) { return { pks: b.pks.slice(), columns: b.columns.slice() }; }); }

    /** Shown where it stands under the maps: paint(rects). A block that no longer stands cancels the copy, and nothing is painted. */
    show(maps, paint) {
        var rects = this.rects(maps);
        if (rects === null) { this.clear(); rects = []; }
        paint(rects);
        return this;
    }

    /** Each block as the presented rectangle it stands as; null when one no longer stands. */
    rects(maps) {
        var out = [];
        if (!this._blocks) return out;
        for (var k = 0; k < this._blocks.length; k++) {
            var r = RelGridCopied._stands(this._blocks[k], maps);
            if (r === null) return null;
            if (r !== undefined) out.push(r);
        }
        return out;
    }

    /** A block's rectangle; undefined for one wholly out of the window; null for one that no longer stands. */
    static _stands(block, maps) {
        var cols = block.columns, pks = block.pks, j0 = maps.colOf(cols[0]);
        for (var c = 0; c < cols.length; c++) if (maps.colOf(cols[c]) !== j0 + c || j0 < 0) return null;
        var first = -1, last = -1;
        for (var p = 0; p < pks.length; p++) {
            if (maps.rowOf(pks[p]) < 0) continue;
            if (first < 0) first = p;
            last = p;
        }
        if (first < 0) return undefined;                        // out of the window: kept, drawn nowhere
        var i0 = maps.rowOf(pks[first]);
        for (var q = first; q <= last; q++) if (maps.rowOf(pks[q]) !== i0 + (q - first)) return null;
        return { i0: i0, j0: j0, i1: i0 + (last - first), j1: j0 + cols.length - 1 };
    }
}
