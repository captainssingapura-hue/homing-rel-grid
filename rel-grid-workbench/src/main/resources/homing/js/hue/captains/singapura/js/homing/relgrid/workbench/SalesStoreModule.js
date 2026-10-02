// =============================================================================
// SalesStore — the Outlets bench's LEDGER: the same six dishes sold at three
// outlets, each outlet its own book. DOMAIN CODE: the single source of truth
// for what was sold where, telling its subscribers what changed. It knows
// nothing of any grid or any group.
//
//   new SalesStore(opts?)         a ledger: each Outlets widget keeps its own; opts.now() and
//                                 opts.random() replace the clock and the dice
//
//   store.outlets()               [{ id, name }], in the order the shop lists them
//   store.dishes()                the six pks
//   store.columns()               ['dish', 'sold', 'revenue', 'lastSale']
//   store.get(outlet, pk, col)
//   store.totals(outlet)          { sold, revenue } — DERIVED and published; null outlet = the ledger
//   store.sell(outlet, pk, n)     n more of a dish at an outlet: sold, revenue and lastSale move,
//                                 the outlet's totals are re-derived — the FEED SEAM
//   store.trade(outlet?)          a burst: a few sales of every dish, at one outlet or all
//   store.subscribe(fn)           fn(outlet, pk, col, v) per cell that changed, and
//                                 fn(outlet, null, 'totals', totals) per outlet whose totals did
//   store.reset()                 back to the seed, notifying every cell
//   store.revision()              how many changes so far
//
// In memory, on purpose: a ledger is a session's, and the round that feeds it
// live will feed it from here. Nothing is edited — sales are the only thing
// that move it — so every cell over it is read-only, and what is interesting
// is what moves them: a sale at Harbour moves Harbour's cells and Harbour's
// totals, and no other outlet hears a thing.
// =============================================================================

var SALES_OUTLETS = [
    { id: 'downtown', name: 'Downtown' },
    { id: 'airport',  name: 'Airport' },
    { id: 'harbour',  name: 'Harbour' }
];
var SALES_DISHES = {
    mapo:   { dish: 'tofu',    price: 9.5 },
    coq:    { dish: 'chicken', price: 18 },
    fish:   { dish: 'cod',     price: 12 },
    sauer:  { dish: 'pork',    price: 14 },
    burger: { dish: 'beef',    price: 11 },
    carbo:  { dish: 'pasta',   price: 13 }
};
// How many of each were sold at each outlet before the day began.
var SALES_SEED = {
    downtown: { mapo: 24, coq: 11, fish: 17, sauer: 9,  burger: 31, carbo: 22 },
    airport:  { mapo: 8,  coq: 19, fish: 6,  sauer: 14, burger: 40, carbo: 12 },
    harbour:  { mapo: 15, coq: 7,  fish: 29, sauer: 5,  burger: 18, carbo: 16 }
};
var SALES_COLS = ["dish", "sold", "revenue", "lastSale"];

class SalesStore {
    constructor(opts) {
        var o = opts || {};
        this._now = o.now || function () { return new Date(); };
        this._random = o.random || Math.random;
        this._subs = [];
        this._revision = 0;
        this._data = SalesStore._seed();
        this._totals = this._deriveAll();
    }

    outlets() { return SALES_OUTLETS.map(function (o) { return { id: o.id, name: o.name }; }); }
    dishes()  { return Object.keys(SALES_DISHES); }
    columns() { return SALES_COLS.slice(); }
    priceOf(pk) { return SALES_DISHES[pk] ? SALES_DISHES[pk].price : undefined; }
    revision() { return this._revision; }

    get(outlet, pk, col) {
        var data = this._data;
        if (!data[outlet] || !data[outlet][pk]) return undefined;
        if (col === "dish") return SALES_DISHES[pk].dish;
        return data[outlet][pk][col];
    }

    totals(outlet) {
        var totals = this._totals;
        if (outlet == null) {
            var all = { sold: 0, revenue: 0 };
            SALES_OUTLETS.forEach(function (o) { all.sold += totals[o.id].sold; all.revenue += totals[o.id].revenue; });
            return all;
        }
        return totals[outlet] ? { sold: totals[outlet].sold, revenue: totals[outlet].revenue } : undefined;
    }

    /** The feed seam: a sale, and everything it moves. */
    sell(outlet, pk, n) {
        var data = this._data;
        if (!data[outlet] || !data[outlet][pk]) return false;
        n = Number(n);
        if (!isFinite(n) || n <= 0) return false;
        var row = data[outlet][pk];
        row.sold += n;
        row.revenue += n * SALES_DISHES[pk].price;
        row.lastSale = this._clock();
        this._revision++;
        this._notify(outlet, pk, "sold", row.sold);
        this._notify(outlet, pk, "revenue", row.revenue);
        this._notify(outlet, pk, "lastSale", row.lastSale);
        this._rederive(outlet);
        return true;
    }

    /** A burst of trade: nought to three of every dish, at one outlet or every one. */
    trade(outlet) {
        var self = this, where = outlet ? [outlet] : SALES_OUTLETS.map(function (o) { return o.id; });
        where.forEach(function (o) {
            Object.keys(SALES_DISHES).forEach(function (pk) {
                var n = Math.floor(self._random() * 4);
                if (n > 0) self.sell(o, pk, n);
            });
        });
    }

    subscribe(fn) {
        var subs = this._subs;
        subs.push(fn);
        return function () { var i = subs.indexOf(fn); if (i >= 0) subs.splice(i, 1); };
    }

    reset() {
        var self = this;
        this._data = SalesStore._seed();
        this._revision++;
        SALES_OUTLETS.forEach(function (o) {
            Object.keys(SALES_DISHES).forEach(function (pk) {
                var row = self._data[o.id][pk];
                self._notify(o.id, pk, "sold", row.sold);
                self._notify(o.id, pk, "revenue", row.revenue);
                self._notify(o.id, pk, "lastSale", row.lastSale);
            });
            self._rederive(o.id);
        });
    }

    _clock() {
        var d = this._now(), p = function (n) { return (n < 10 ? "0" : "") + n; };
        return p(d.getHours()) + ":" + p(d.getMinutes()) + ":" + p(d.getSeconds());
    }

    static _seed() {
        var out = {};
        SALES_OUTLETS.forEach(function (o) {
            out[o.id] = {};
            Object.keys(SALES_DISHES).forEach(function (pk) {
                var sold = SALES_SEED[o.id][pk];
                out[o.id][pk] = { sold: sold, revenue: sold * SALES_DISHES[pk].price, lastSale: "\u2014" };
            });
        });
        return out;
    }

    _derive(outlet) {
        var t = { sold: 0, revenue: 0 }, data = this._data;
        Object.keys(SALES_DISHES).forEach(function (pk) { t.sold += data[outlet][pk].sold; t.revenue += data[outlet][pk].revenue; });
        return t;
    }

    _deriveAll() {
        var out = {}, self = this;
        SALES_OUTLETS.forEach(function (o) { out[o.id] = self._derive(o.id); });
        return out;
    }

    _notify(outlet, pk, col, v) {
        this._subs.slice().forEach(function (fn) {
            try { fn(outlet, pk, col, v); } catch (e) { console.error("[SalesStore] subscriber threw:", e); }
        });
    }

    _rederive(outlet) {
        var next = this._derive(outlet), was = this._totals[outlet];
        if (next.sold !== was.sold || next.revenue !== was.revenue) {
            this._totals[outlet] = next;
            this._notify(outlet, null, "totals", { sold: next.sold, revenue: next.revenue });
        }
    }
}
