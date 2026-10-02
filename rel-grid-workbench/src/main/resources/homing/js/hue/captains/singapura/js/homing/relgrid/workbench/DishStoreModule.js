// =============================================================================
// DishStore — the Replicating Tables bench's dishes: six of them, telling
// subscribers what changed. DOMAIN CODE: it knows nothing of any grid, and
// nothing in this file could tell one anything.
//
// Each table keeps its OWN DishStore - a replica of the bench's one store,
// which is the "dishes" party's, kept across visits by its steward. Joined,
// a table's store WRITES THROUGH: commit, sell and reset are told to the
// party, and the store changes only when the party says the dishes are now
// these (apply). Not joined, it writes itself, in memory.
//
//   new DishStore(data?)         the seed, unless the dishes are given
//   DishStore.seed()             a copy of the seed
//   DishStore.wellFormed(data)   every seeded dish there, each with a numeric `sold`
//   DishStore.committed(data, pk, col, v) / DishStore.sold(data, pk, n)
//                                the two changes, pure: the next data, or null when refused
//
//   store.pks() / columns() / get(pk, col)
//   store.writableColumns()      what ANY editor may write — the store's rule
//   store.commit(pk, col, v)     the EDIT SEAM; refuses a column nobody may write
//   store.sell(pk, n)            n more sold — the only thing that moves popularity
//   store.trade(random?)         a day of trade: a few sales of every dish
//   store.reset()                back to the seed
//   store.subscribe(fn)          fn(pk, col, v) per cell that changed; returns the unsubscribe
//   store.revision()             how many changes so far
//   store.writeTo(writer)        joined: { commit(pk, col, v), sell(pk, n), reset() } - or null, alone
//   store.apply(data, revision)  the dishes are now these: every cell that changed is told
//   store.data()                 the dishes, copied
//
// `stars` is a HEALTH RATING, 1 to 5, and the nutritionist's to set. It is
// writable like any other column; what makes it interesting is that its cell
// is not a text cell — see DishStarsCellModule.
//
// Two columns nobody edits, for two different reasons. `sold` is a SOURCE
// that only sales move. `popularity` is DERIVED from it — each dish's sales
// as a share of the best seller's, 0–100 — so a sale re-derives popularity
// for EVERY dish, and subscribers hear about each cell that actually changed.
// The derived value is never kept; it is recomputed from what was.
//
// WHO may write a writable column is not decided here: that is each
// relation's rule (DishRelation's roles). This store only says what may be
// written at all.
// =============================================================================

var _DISH_COLS = ["ingredient", "style", "calories", "stars", "price", "sold", "popularity"];
var _DISH_WRITABLE = ["ingredient", "style", "calories", "stars", "price"];
var _DISH_SEED = {
    mapo:   { ingredient: "tofu",    style: "Chinese", calories: 480, stars: 4, price: 9.5,  sold: 71 },
    coq:    { ingredient: "chicken", style: "French",  calories: 610, stars: 2, price: 18,   sold: 64 },
    fish:   { ingredient: "cod",     style: "English", calories: 560, stars: 5, price: 12,   sold: 58 },
    sauer:  { ingredient: "pork",    style: "German",  calories: 650, stars: 2, price: 14,   sold: 49 },
    burger: { ingredient: "beef",    style: "USA",     calories: 780, stars: 1, price: 11,   sold: 88 },
    carbo:  { ingredient: "pasta",   style: "Italian", calories: 720, stars: 3, price: 13,   sold: 77 }
};

class DishStore {
    constructor(data) {
        this._data = DishStore.wellFormed(data) ? DishStore._copy(data) : DishStore.seed();
        this._popularity = DishStore._derive(this._data);
        this._subs = [];
        this._revision = 0;
        this._writer = null;
    }

    static seed() { return DishStore._copy(_DISH_SEED); }

    /** What is kept must have every seeded dish with a numeric `sold`; anything else is ignored. */
    static wellFormed(d) {
        if (!d || typeof d !== "object") return false;
        var pks = Object.keys(_DISH_SEED);
        for (var i = 0; i < pks.length; i++) {
            var row = d[pks[i]];
            if (!row || typeof row !== "object" || typeof row.sold !== "number") return false;
        }
        return true;
    }

    /** The data after a commit - or null: no such dish, or a column nobody writes. Pure. */
    static committed(data, pk, col, v) {
        if (!data[pk] || _DISH_WRITABLE.indexOf(col) < 0) return null;
        var next = DishStore._copy(data);
        next[pk][col] = v;
        return next;
    }

    /** The data after n more of a dish sold - or null: no such dish, or no sale. Pure. */
    static sold(data, pk, n) {
        n = Math.floor(Number(n));
        if (!data[pk] || !(n > 0)) return null;
        var next = DishStore._copy(data);
        next[pk].sold += n;
        return next;
    }

    pks()             { return Object.keys(this._data); }
    columns()         { return _DISH_COLS.slice(); }
    writableColumns() { return _DISH_WRITABLE.slice(); }
    revision()        { return this._revision; }
    data()            { return DishStore._copy(this._data); }

    get(pk, col) {
        if (!this._data[pk]) return undefined;
        return (col === "popularity") ? this._popularity[pk] : this._data[pk][col];
    }

    writeTo(writer) { this._writer = writer || null; }

    /** The edit seam. Whoever commits here is an editor; whoever subscribes follows. */
    commit(pk, col, v) {
        var next = DishStore.committed(this._data, pk, col, v);
        if (!next) return false;                                  // nobody writes sold or popularity
        if (this._writer) { this._writer.commit(pk, col, v); return true; }
        this.apply(next, this._revision + 1);
        return true;
    }

    /** A sale: the one thing that moves `sold`, and through it `popularity`. */
    sell(pk, n) {
        var next = DishStore.sold(this._data, pk, n);
        if (!next) return false;
        if (this._writer) { this._writer.sell(pk, Math.floor(Number(n))); return true; }
        this.apply(next, this._revision + 1);
        return true;
    }

    /** A day of trade: up to five of each dish. `random` is injectable for a deterministic day. */
    trade(random) {
        var r = random || Math.random, self = this;
        Object.keys(this._data).forEach(function (pk) {
            var n = Math.floor(r() * 6);
            if (n > 0) self.sell(pk, n);
        });
    }

    reset() {
        if (this._writer) { this._writer.reset(); return; }
        this.apply(DishStore.seed(), this._revision + 1);
    }

    subscribe(fn) {
        var subs = this._subs;
        subs.push(fn);
        return function () { var i = subs.indexOf(fn); if (i >= 0) subs.splice(i, 1); };
    }

    /**
     * The dishes are now these: every cell that changed is told - the sources first, then the
     * derived cells the change moved, as a sale is heard: what was sold, then what it re-derived.
     */
    apply(data, revision) {
        if (!DishStore.wellFormed(data)) return;
        var before = this._data, beforePop = this._popularity, self = this, pks;
        this._data = DishStore._copy(data);
        this._popularity = DishStore._derive(this._data);
        if (typeof revision === "number") this._revision = revision;
        pks = Object.keys(this._data);
        pks.forEach(function (pk) {
            _DISH_COLS.forEach(function (col) {
                if (col === "popularity") return;
                var was = before[pk] ? before[pk][col] : undefined, now = self._data[pk][col];
                if (was !== now) self._notify(pk, col, now);
            });
        });
        pks.forEach(function (pk) {
            if (beforePop[pk] !== self._popularity[pk]) self._notify(pk, "popularity", self._popularity[pk]);
        });
    }

    _notify(pk, col, v) {
        this._subs.slice().forEach(function (fn) {
            try { fn(pk, col, v); } catch (e) { console.error("[DishStore] subscriber threw:", e); }
        });
    }

    static _copy(o) { return JSON.parse(JSON.stringify(o)); }

    /** DERIVED: each dish's sales as a share of the best seller's, 0–100. */
    static _derive(d) {
        var max = 0, pks = Object.keys(d), out = {};
        pks.forEach(function (pk) { if (d[pk].sold > max) max = d[pk].sold; });
        pks.forEach(function (pk) { out[pk] = max > 0 ? Math.round(100 * d[pk].sold / max) : 0; });
        return out;
    }
}
