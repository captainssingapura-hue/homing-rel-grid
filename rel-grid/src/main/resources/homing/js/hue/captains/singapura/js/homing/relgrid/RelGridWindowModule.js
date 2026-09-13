// =============================================================================
// RelGridWindowModule — RFC 0050 · Episode 2's WINDOW: the relation's own seam,
// view(intent), asked to MOVE. One root: the View is answered, never listed,
// and the same question with { by: n } answers the View n rows on — or
// nothing, the rows staying as they are. This is the one place the grid asks
// the relation anything after construction, and it asks for keys.
//
// The grid never learns whether there is anything beyond what it shows. A
// static relation answers nothing to every movement — its View is the whole
// of it — and the grid then reports the edge, or leaves the wheel to the
// browser, exactly as it always did. An endless relation answers a window,
// which the facade presents on the same slots; the relation clamps at its
// own ends and answers nothing there.
//
//   new RelGridWindow({ relation, maps })
//   move(n)      ask view({ by: n }) and present the answer. True when the rows moved;
//                false for 0, for nothing back, for the same rows back, and — loudly,
//                on the console — for an answer that is not a list.
// =============================================================================

/** The same rows, in the same order. */
function _hrgSameRows(a, b) {
    if (a.length !== b.length) return false;
    for (var k = 0; k < a.length; k++) if (a[k] !== b[k]) return false;
    return true;
}

class RelGridWindow {

    constructor(opts) {
        this._relation = opts.relation;
        this._maps = opts.maps;
    }

    /**
     * The View n rows on (negative: back), presented. A relation that refuses
     * an identity in its own answer refuses the View whole — the maps undo it
     * and the refusal travels on, as it does for any remap.
     */
    move(n) {
        n = Math.trunc(Number(n) || 0);
        if (!n) return false;
        var keys;
        try { keys = this._relation.view({ by: n }); }
        catch (e) { console.error("[RelGrid] relation.view({ by }) threw:", e); return false; }
        if (keys == null) return false;                       // nowhere to go: the rows stay
        if (!Array.isArray(keys)) {
            console.error("[RelGrid] relation.view({ by }) must answer rows, as a list, or nothing; got:", keys);
            return false;
        }
        if (_hrgSameRows(keys, this._maps.rowView())) return false;
        this._maps.setRowView(keys);                          // → arrange("rows")
        return true;
    }
}
