// =============================================================================
// JsonDocStore — a JSON document: a TEXT, parsed on every set, and the last
// value that parsed. DOMAIN CODE, no DOM. Each JSON bench widget keeps its
// own; what they share is the text, through the bench's json-text party, and
// each parses what it hears.
//
//   new JsonDocStore(text?)       the sample when no text is given
//   JsonDocStore.SAMPLE           the text the bench opens with
//
//   store.text()                  the text as typed
//   store.set(text)               parse it: the value moves only when the text parses; the
//                                 error is kept otherwise, and the subscribers are told either way
//   store.value()                 the last value that parsed (undefined until one has)
//   store.error()                 the parse error's message for the current text, or null
//   store.revision()              how many times set() was called
//   store.subscribe(fn)           fn(store) after every set; answers the unsubscribe
//   store.sample()                the text the bench opens with
// =============================================================================

var _WB_JSON_SAMPLE = JSON.stringify({
    name: "json-kit",
    version: "0.1.0",
    description: "A JSON viewer on the tree view — a value is what the tree asks for, one row per node.",
    keywords: ["json", "tree", "viewer", "homing"],
    stable: false,
    homepage: null,
    scripts: { build: "mvn -o -q install", bench: "mvn -Pworkbench -pl rel-grid-workbench exec:java" },
    dependencies: { "rel-tree": "LOCAL-SNAPSHOT", "rel-channel": "LOCAL-SNAPSHOT", "rel-grid-protocol": "LOCAL-SNAPSHOT" },
    "odd/key": { "~tilde": true, "": "an empty name" },
    releases: [
        { version: "0.1.0", date: "2026-09-15", notes: ["the value", "the cell", "the view"], size: 238 },
        { version: "0.2.0", date: null, notes: [], size: 0 }
    ],
    limits: { maxString: 80, openDepth: 1, pi: 3.14159, big: 1e21, negative: -42 }
}, null, 2);

class JsonDocStore {
    constructor(text) {
        this._text = (text === undefined) ? _WB_JSON_SAMPLE : String(text);
        this._value = undefined;
        this._error = null;
        this._revision = 0;
        this._subscribers = [];
        this._parse();
    }

    static get SAMPLE() { return _WB_JSON_SAMPLE; }

    text()     { return this._text; }
    value()    { return this._value; }
    error()    { return this._error; }
    revision() { return this._revision; }
    sample()   { return _WB_JSON_SAMPLE; }

    set(t) {
        this._text = String(t);
        this._revision++;
        this._parse();
        this._notify();
    }

    subscribe(fn) {
        var subs = this._subscribers;
        subs.push(fn);
        return function () { var i = subs.indexOf(fn); if (i >= 0) subs.splice(i, 1); };
    }

    _parse() {
        try { this._value = JSON.parse(this._text); this._error = null; }
        catch (e) { this._error = (e && e.message) ? e.message : String(e); }
    }

    _notify() {
        var self = this;
        this._subscribers.slice().forEach(function (fn) {
            try { fn(self); } catch (e) { console.error("[JsonDocStore] subscriber threw:", e); }
        });
    }
}
