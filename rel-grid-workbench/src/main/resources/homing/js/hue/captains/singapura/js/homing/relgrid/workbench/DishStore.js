// =============================================================================
// DishStore — the Replicating Tables bench's persisted store. DOMAIN CODE:
// the single source of truth for the six dishes, saved to localStorage on
// every change, telling its subscribers what changed. It knows nothing of any
// grid, and nothing in this file could tell one anything.
//
//   dishStoreShared()   the page's one store — every table shares it
//   createDishStore()   a fresh store (tests, or a second independent set)
//
//   store.pks() / columns() / get(pk, col)
//   store.writableColumns()      what ANY editor may write — the store's rule
//
// `stars` is a HEALTH RATING, 1 to 5, and the nutritionist's to set. It is
// writable like any other column; what makes it interesting is that its cell
// is not a text cell — see DishStarsCellModule.
//   store.commit(pk, col, v)     write + persist + notify — the EDIT SEAM;
//                                refuses a column nobody may write
//   store.sell(pk, n)            n more sold — the only thing that moves popularity
//   store.trade()                a day of trade: a few sales of every dish
//   store.subscribe(fn)          fn(pk, col, v); returns the unsubscribe
//   store.revision()             how many changes so far
//   store.reset()                back to the seed, notifying every cell
//
// Two columns nobody edits, for two different reasons. `sold` is a SOURCE
// that only sales move. `popularity` is DERIVED from it — each dish's sales
// as a share of the best seller's, 0–100 — so a sale re-derives popularity
// for EVERY dish, and subscribers hear about each cell that actually changed.
// The derived value is never persisted; it is recomputed from what was.
//
// WHO may write a writable column is not decided here: that is each
// relation's rule (DishRelation's roles). This store only says what may be
// written at all.
// =============================================================================

function createDishStore() {
    var KEY = 'bench.replicatingTables.dishes.v3';
    var COLS = ['ingredient', 'style', 'calories', 'stars', 'price', 'sold', 'popularity'];
    var WRITABLE = ['ingredient', 'style', 'calories', 'stars', 'price'];
    var SEED = {
        mapo:   { ingredient: 'tofu',    style: 'Chinese', calories: 480, stars: 4, price: 9.5,  sold: 71 },
        coq:    { ingredient: 'chicken', style: 'French',  calories: 610, stars: 2, price: 18,   sold: 64 },
        fish:   { ingredient: 'cod',     style: 'English', calories: 560, stars: 5, price: 12,   sold: 58 },
        sauer:  { ingredient: 'pork',    style: 'German',  calories: 650, stars: 2, price: 14,   sold: 49 },
        burger: { ingredient: 'beef',    style: 'USA',     calories: 780, stars: 1, price: 11,   sold: 88 },
        carbo:  { ingredient: 'pasta',   style: 'Italian', calories: 720, stars: 3, price: 13,   sold: 77 }
    };
    function copy(o) { return JSON.parse(JSON.stringify(o)); }
    /** What was persisted must have every seeded dish with a numeric `sold`; anything else is ignored. */
    function wellFormed(d) {
        if (!d || typeof d !== 'object') return false;
        var pks = Object.keys(SEED);
        for (var i = 0; i < pks.length; i++) {
            var row = d[pks[i]];
            if (!row || typeof row !== 'object' || typeof row.sold !== 'number') return false;
        }
        return true;
    }
    function load() {
        try {
            var raw = (typeof localStorage !== 'undefined') ? localStorage.getItem(KEY) : null;
            var parsed = raw ? JSON.parse(raw) : null;
            return wellFormed(parsed) ? parsed : null;
        } catch (e) { return null; }
    }
    function persist() {
        try { if (typeof localStorage !== 'undefined') localStorage.setItem(KEY, JSON.stringify(data)); }
        catch (e) { /* a private window, or quota — the store still works in memory */ }
    }
    var data = load() || copy(SEED);
    var subs = [], revision = 0;
    function notify(pk, col, v) {
        subs.slice().forEach(function (fn) {
            try { fn(pk, col, v); } catch (e) { console.error('[DishStore] subscriber threw:', e); }
        });
    }
    /** DERIVED: each dish's sales as a share of the best seller's, 0–100. */
    function derive(d) {
        var max = 0, pks = Object.keys(d), out = {};
        pks.forEach(function (pk) { if (d[pk].sold > max) max = d[pk].sold; });
        pks.forEach(function (pk) { out[pk] = max > 0 ? Math.round(100 * d[pk].sold / max) : 0; });
        return out;
    }
    var popularity = derive(data);
    /** The source moved: re-derive, and tell subscribers about each derived cell that changed. */
    function rederive() {
        var next = derive(data);
        Object.keys(next).forEach(function (pk) {
            if (next[pk] !== popularity[pk]) { popularity[pk] = next[pk]; notify(pk, 'popularity', next[pk]); }
        });
    }
    var api = {
        pks:     function () { return Object.keys(data); },
        columns: function () { return COLS.slice(); },
        writableColumns: function () { return WRITABLE.slice(); },
        get: function (pk, col) {
            if (!data[pk]) return undefined;
            return (col === 'popularity') ? popularity[pk] : data[pk][col];
        },
        /** The edit seam. Whoever commits here is an editor; whoever subscribes follows. */
        commit: function (pk, col, v) {
            if (!data[pk] || WRITABLE.indexOf(col) < 0) return false;    // nobody writes sold or popularity
            data[pk][col] = v;
            revision++;
            persist();
            notify(pk, col, v);
            return true;
        },
        /** A sale: the one thing that moves `sold`, and through it `popularity`. */
        sell: function (pk, n) {
            n = Math.floor(Number(n));
            if (!data[pk] || !(n > 0)) return false;
            data[pk].sold += n;
            revision++;
            persist();
            notify(pk, 'sold', data[pk].sold);
            rederive();
            return true;
        },
        /** A day of trade: up to five of each dish. `random` is injectable for a deterministic day. */
        trade: function (random) {
            random = random || Math.random;
            Object.keys(data).forEach(function (pk) {
                var n = Math.floor(random() * 6);
                if (n > 0) api.sell(pk, n);
            });
        },
        subscribe: function (fn) {
            subs.push(fn);
            return function () { var i = subs.indexOf(fn); if (i >= 0) subs.splice(i, 1); };
        },
        revision: function () { return revision; },
        reset: function () {
            data = copy(SEED);
            popularity = derive(data);
            revision++;
            persist();
            Object.keys(data).forEach(function (pk) {
                COLS.forEach(function (c) { notify(pk, c, api.get(pk, c)); });
            });
        }
    };
    return api;
}

/** The page's one store, so every table on it replicates the same data. */
function dishStoreShared() {
    var g = (typeof window !== 'undefined') ? window : globalThis;
    if (!g.__benchDishStore) g.__benchDishStore = createDishStore();
    return g.__benchDishStore;
}
