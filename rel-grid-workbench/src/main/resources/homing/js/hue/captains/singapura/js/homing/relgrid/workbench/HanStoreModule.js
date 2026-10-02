// =============================================================================
// HanStore — an article: ONE STRING, telling its subscribers when it changed.
// DOMAIN CODE; nothing here knows a grid exists, and nothing here persists:
// the bench's one article is the han-article party's, kept by its steward, and
// each widget's store is its own copy of what the party says.
//
//   new HanStore(seed?)       the article, opening on the seed - the poem, unless given
//   HanStore.SEED             the poem
//
//   store.text()              the article, as it is
//   store.set(text)           the whole article - the EDIT SEAM; answers whether it changed
//   store.reset()             back to the seed
//   store.subscribe(fn)       fn(text); returns the unsubscribe
//   store.revision()          how many changes so far
// =============================================================================

var _HAN_SEED = "月落乌啼霜满天，\n江枫渔火对愁眠。\n姑苏城外寒山寺，\n夜半钟声到客船。";

class HanStore {
    constructor(seed) {
        this._seed = (typeof seed === "string") ? seed : _HAN_SEED;
        this._text = this._seed;
        this._revision = 0;
        this._subs = [];
    }

    static get SEED() { return _HAN_SEED; }

    text()     { return this._text; }
    revision() { return this._revision; }
    set(t)     { return this._change(String(t == null ? "" : t)); }
    reset()    { return this._change(this._seed); }

    subscribe(fn) {
        var subs = this._subs;
        subs.push(fn);
        return function () { var i = subs.indexOf(fn); if (i >= 0) subs.splice(i, 1); };
    }

    _change(next) {
        if (next === this._text) return false;
        this._text = next;
        this._revision++;
        for (var k = 0; k < this._subs.length; k++) {
            try { this._subs[k](next); } catch (e) { console.error("[HanStore] subscriber threw:", e); }
        }
        return true;
    }
}
