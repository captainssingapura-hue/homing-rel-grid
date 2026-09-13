// =============================================================================
// RelGridWidthsModule — RFC 0050 · Episode 2's COLUMN WIDTHS: geometry, the
// grid's alone (map 7). Held by column IDENTITY, answered by POSITION for the
// layout to apply in place — no arrangement runs, because no identity and no
// order moved. A request is bounded at normalisation and the BOUNDED request
// is what is held and reported; a column the relation does not have is
// refused (drift). The grid persists nothing: what is held is a report, and
// remembering it is the host's.
//
//   new RelGridWidths({ maps, minColumnWidth? })
//   hold(column, px)     → { px, changed } with the bounded width, or null when refused
//   get(column)          what is HELD, or null — never what was measured
//   snapshot()           every held width, by column: a plain object the host may keep
//   positional()         px | null per presented column, in view order — the layout's form
//   stepped(column, dir) the width one keyboard step (Alt+arrow) from what is held, or the default
//
// The floor of the legal range is the host's, within reason. A column
// narrower than the default is a host's deliberate geometry — a half-square
// for a squeezed mark — and not something a drag should be able to reach by
// accident, which is what the default protects.
// =============================================================================

var _HRG_MIN_W = 40, _HRG_MAX_W = 2000;   // the legal range — normalisation's bound
var _HRG_MIN_W_FLOOR = 8;                 // and the least a host may lower the floor to
var _HRG_DEFAULT_W = 120;                 // what a keyboard resize starts from when nothing is held
var _HRG_KEY_STEP = 10;                   // one Alt+arrow

class RelGridWidths {

    constructor(opts) {
        opts = opts || {};
        this._maps = opts.maps;
        this._widths = new Map();         // column → px, identity-keyed; positional only at the layout
        var minW = Number(opts.minColumnWidth);
        this._minW = isFinite(minW) ? Math.max(_HRG_MIN_W_FLOOR, Math.min(_HRG_MAX_W, minW)) : _HRG_MIN_W;
    }

    /**
     * Hold a width for a column. Refused — null — for a column the relation
     * does not have, or a request that is not a number. Otherwise the bounded
     * width, and whether anything changed: the same width held again is
     * nothing to apply and nothing to report.
     */
    hold(column, px) {
        if (this._maps.baseColumns().indexOf(column) < 0) return null;
        var n = Number(px);
        if (!isFinite(n)) return null;
        var bounded = Math.max(this._minW, Math.min(_HRG_MAX_W, n));
        var changed = this._widths.get(column) !== bounded;
        if (changed) this._widths.set(column, bounded);
        return { px: bounded, changed: changed };
    }

    get(column) { return this._widths.has(column) ? this._widths.get(column) : null; }

    snapshot() {
        var out = {};
        this._widths.forEach(function (px, c) { out[c] = px; });
        return out;
    }

    positional() {
        var out = [], m = this._maps;
        for (var j = 0; j < m.cols(); j++) {
            var c = m.columnAt(j);
            out.push(this._widths.has(c) ? this._widths.get(c) : null);
        }
        return out;
    }

    stepped(column, dir) {
        var held = this.get(column);
        return ((held == null) ? _HRG_DEFAULT_W : held) + (dir > 0 ? _HRG_KEY_STEP : -_HRG_KEY_STEP);
    }
}
