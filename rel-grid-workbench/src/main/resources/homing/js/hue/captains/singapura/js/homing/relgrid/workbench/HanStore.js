// =============================================================================
// HanStore — the Han Article bench's persisted store: ONE STRING, the article,
// saved to localStorage on every change and telling its subscribers when it
// changed. DOMAIN CODE; nothing here knows a grid exists.
//
//   hanStoreShared()          the page's one article — the editor and every display share it
//   createHanStore(seed?)     a fresh store (tests, or a second article)
//
//   store.text()              the article, as it is
//   store.set(text)           the whole article — the EDIT SEAM, which is the
//                             text editor's, and only its
//   store.reset()             back to the seed
//   store.subscribe(fn)       fn(text); returns the unsubscribe
//   store.revision()          how many changes so far
// =============================================================================

var HAN_SEED = "月落乌啼霜满天，\n江枫渔火对愁眠。\n姑苏城外寒山寺，\n夜半钟声到客船。";

function createHanStore(seed) {
    var KEY = "bench.hanArticle.text.v1";
    var SEED = (typeof seed === "string") ? seed : HAN_SEED;
    var text, revision = 0, subs = [];

    function load() {
        try {
            var raw = (typeof localStorage !== "undefined") ? localStorage.getItem(KEY) : null;
            return (typeof raw === "string") ? raw : null;
        } catch (e) { return null; }
    }
    function save() {
        try { if (typeof localStorage !== "undefined") localStorage.setItem(KEY, text); }
        catch (e) { /* a page without storage still edits; it just forgets */ }
    }
    function notify() {
        for (var k = 0; k < subs.length; k++) {
            try { subs[k](text); } catch (e) { console.error("[HanStore] subscriber threw:", e); }
        }
    }
    function change(next) {
        if (next === text) return false;
        text = next;
        revision++;
        save();
        notify();
        return true;
    }

    var stored = load();
    text = (stored !== null) ? stored : SEED;

    return {
        text:     function () { return text; },
        set:      function (t) { return change(String(t == null ? "" : t)); },
        reset:    function () { return change(SEED); },
        subscribe: function (fn) {
            subs.push(fn);
            return function () { var i = subs.indexOf(fn); if (i >= 0) subs.splice(i, 1); };
        },
        revision: function () { return revision; }
    };
}

function hanStoreShared() {
    var g = (typeof window !== "undefined") ? window : globalThis;
    if (!g.__benchHanStore) g.__benchHanStore = createHanStore();
    return g.__benchHanStore;
}
