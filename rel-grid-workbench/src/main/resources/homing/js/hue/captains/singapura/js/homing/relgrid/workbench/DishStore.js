// =============================================================================
// DishStore — the Replicating Tables bench's persisted store. DOMAIN CODE:
// the single source of truth for the six dishes, saved to localStorage on
// every commit, telling its subscribers what changed. It knows nothing of any
// grid, and nothing in this file could tell one anything.
//
//   dishStoreShared()   the page's one store — every table shares it
//   createDishStore()   a fresh store (tests, or a second independent set)
//
//   store.pks() / columns() / get(pk, col)
//   store.commit(pk, col, v)     write + persist + notify — the EDIT SEAM
//   store.subscribe(fn)          fn(pk, col, v); returns the unsubscribe
//   store.revision()             how many commits so far
//   store.reset()                back to the seed, notifying every cell
// =============================================================================

function createDishStore() {
    var KEY = 'bench.replicatingTables.dishes';
    var COLS = ['ingredient', 'style', 'calories', 'price', 'popularity'];
    var SEED = {
        mapo:   { ingredient: 'tofu',    style: 'Chinese', calories: 480, price: 9.5,  popularity: 71 },
        coq:    { ingredient: 'chicken', style: 'French',  calories: 610, price: 18,   popularity: 64 },
        fish:   { ingredient: 'cod',     style: 'English', calories: 560, price: 12,   popularity: 58 },
        sauer:  { ingredient: 'pork',    style: 'German',  calories: 650, price: 14,   popularity: 49 },
        burger: { ingredient: 'beef',    style: 'USA',     calories: 780, price: 11,   popularity: 88 },
        carbo:  { ingredient: 'pasta',   style: 'Italian', calories: 720, price: 13,   popularity: 77 }
    };
    function copy(o) { return JSON.parse(JSON.stringify(o)); }
    function load() {
        try {
            var raw = (typeof localStorage !== 'undefined') ? localStorage.getItem(KEY) : null;
            var parsed = raw ? JSON.parse(raw) : null;
            return (parsed && typeof parsed === 'object') ? parsed : null;
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
    return {
        pks:     function () { return Object.keys(data); },
        columns: function () { return COLS.slice(); },
        get:     function (pk, col) { return data[pk] ? data[pk][col] : undefined; },
        /** The edit seam. Whoever commits here is the editor; whoever subscribes follows. */
        commit: function (pk, col, v) {
            if (!data[pk] || COLS.indexOf(col) < 0) return false;
            data[pk][col] = v;
            revision++;
            persist();
            notify(pk, col, v);
            return true;
        },
        subscribe: function (fn) {
            subs.push(fn);
            return function () { var i = subs.indexOf(fn); if (i >= 0) subs.splice(i, 1); };
        },
        revision: function () { return revision; },
        reset: function () {
            data = copy(SEED);
            revision++;
            persist();
            Object.keys(data).forEach(function (pk) {
                COLS.forEach(function (c) { notify(pk, c, data[pk][c]); });
            });
        }
    };
}

/** The page's one store, so every table on it replicates the same data. */
function dishStoreShared() {
    var g = (typeof window !== 'undefined') ? window : globalThis;
    if (!g.__benchDishStore) g.__benchDishStore = createDishStore();
    return g.__benchDishStore;
}
