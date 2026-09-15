// =============================================================================
// JsonDocStore — the JSON Tree bench's one document: a TEXT, parsed on every
// set, and the last value that parsed. The Input widget writes the text; the
// Display widget shows the value; neither knows the other. DOMAIN CODE, no DOM.
//
//   jsonDocStoreShared()          the page's one document — every JSON bench widget shares it
//   createJsonDocStore(text?)     a fresh one (tests)
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
    description: "A JSON viewer on the tree view — a document is what the tree asks for, one row per node.",
    keywords: ["json", "tree", "viewer", "homing"],
    stable: false,
    homepage: null,
    scripts: { build: "mvn -o -q install", bench: "mvn -pl rel-grid-workbench exec:java" },
    dependencies: { "rel-tree": "LOCAL-SNAPSHOT", "rel-channel": "LOCAL-SNAPSHOT", "rel-grid-protocol": "LOCAL-SNAPSHOT" },
    "odd/key": { "~tilde": true, "": "an empty name" },
    releases: [
        { version: "0.1.0", date: "2026-09-15", notes: ["the document", "the cell", "the view"], size: 238 },
        { version: "0.2.0", date: null, notes: [], size: 0 }
    ],
    limits: { maxString: 80, openDepth: 1, pi: 3.14159, big: 1e21, negative: -42 }
}, null, 2);

function createJsonDocStore(text) {
    var current = (text === undefined) ? _WB_JSON_SAMPLE : String(text);
    var value, error = null, revision = 0, subscribers = [];
    function parse() {
        try { value = JSON.parse(current); error = null; }
        catch (e) { error = (e && e.message) ? e.message : String(e); }
    }
    parse();
    function notify(store) {
        subscribers.slice().forEach(function (fn) {
            try { fn(store); } catch (e) { console.error("[JsonDocStore] subscriber threw:", e); }
        });
    }
    var store = {
        text: function () { return current; },
        set: function (t) { current = String(t); revision++; parse(); notify(store); },
        value: function () { return value; },
        error: function () { return error; },
        revision: function () { return revision; },
        sample: function () { return _WB_JSON_SAMPLE; },
        subscribe: function (fn) {
            subscribers.push(fn);
            return function () { var i = subscribers.indexOf(fn); if (i >= 0) subscribers.splice(i, 1); };
        }
    };
    return store;
}

function jsonDocStoreShared() {
    var g = (typeof window !== 'undefined') ? window : globalThis;
    if (!g.__benchJsonDocStore) g.__benchJsonDocStore = createJsonDocStore();
    return g.__benchJsonDocStore;
}
