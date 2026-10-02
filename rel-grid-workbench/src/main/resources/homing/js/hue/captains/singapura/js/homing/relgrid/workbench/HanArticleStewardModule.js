// =============================================================================
// HanArticleSteward — the steward of a Han article party: the one member that
// does I/O, keeping the bench's article in the browser's storage across
// visits. Hired by the page's root Han article party, and by nothing linked
// below it. A page without storage still edits; it just forgets.
//
//   new HanArticleSteward(tell)
//   steward.reactors   Load - the article kept, told back as Loaded; Save { text } - kept
// =============================================================================

var _HAN_ARTICLE_KEY = "bench.hanArticle.text.v1";

class HanArticleSteward {
    constructor(tell) {
        this._tell = tell;
        var self = this;
        this.reactors = Object.freeze({
            Load: function () { self._load(); },
            Save: function (m) { self._save(m.text); }
        });
    }

    _load() {
        var raw = null;
        try { raw = (typeof localStorage !== "undefined") ? localStorage.getItem(_HAN_ARTICLE_KEY) : null; }
        catch (e) { raw = null; }
        this._tell(typeof raw === "string" ? { kind: "Loaded", text: raw, found: true } : { kind: "Loaded", text: "", found: false });
    }

    _save(text) {
        try { if (typeof localStorage !== "undefined") localStorage.setItem(_HAN_ARTICLE_KEY, text); }
        catch (e) { /* a page without storage still edits; it just forgets */ }
    }
}
