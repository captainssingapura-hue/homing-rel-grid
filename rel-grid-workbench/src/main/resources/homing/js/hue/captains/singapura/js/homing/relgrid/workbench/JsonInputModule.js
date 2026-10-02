// =============================================================================
// JsonInput — the JSON Tree bench's input: a plain textarea over the bench's
// one document. Every keystroke tells the json-text party the text; the party
// says it to every widget of the bench, this one too, and each parses what it
// hears. Not joined, it writes its own document alone.
//
//   new JsonInput(container, params)   params: none
//   (its kind declares, in Java, the type it joins: json-text)
//   (the rest is a BenchWidget's)
// =============================================================================

class JsonInput extends BenchWidget {
    constructor(container, params) {
        super(container, "jsonInput", "JSON input");
        var self = this, body = this.body;
        var hint = this.el("hint", "div", wb_hint, body);
        var bar = this.el("bar", "div", wb_bar, body);
        var text = this._text = this.el("text", "textarea", wb_json_text, body);
        text.setAttribute("spellcheck", "false");
        text.setAttribute("aria-label", "JSON text");
        this._status = this.el("status", "div", wb_status, body);
        this._member = null;

        hint.textContent = "JSON INPUT — a plain textarea over the bench’s one document. Every keystroke tells the bench the text; the Display pane shows the last value that parsed and keeps it while the text is broken. Type inside a string, add a member, delete a whole array: the tree keeps its folds and its cursor by pointer wherever the node still stands.";

        // Its own document: what it last heard, parsed - or, not joined, what it typed.
        var store = this._store = new JsonDocStore();
        text.value = store.text();
        this._unsub = store.subscribe(function () { if (text.value !== store.text()) text.value = store.text(); self._report(); });
        // THE SEAM: the textarea tells the bench, live; what the bench says comes back into the store.
        text.addEventListener("input", function () { self._set(text.value); });
        this.keysTo = text;

        var n = 0;
        var act = function (label, fn) { self.button("act" + (++n), label, fn, bar); };
        act("the sample", function () { self._set(JsonDocStore.SAMPLE); });
        act("pretty", function () { if (store.error() === null) self._set(JSON.stringify(store.value(), null, 2)); });
        act("minify", function () { if (store.error() === null) self._set(JSON.stringify(store.value())); });
        act("a big array (10,000)", function () {
            var a = [];
            for (var i = 0; i < 10000; i++) a.push({ i: i, sq: i * i, even: i % 2 === 0 });
            self._set(JSON.stringify({ rows: a, count: a.length }));
        });
        act("clear", function () { self._set(""); });
        this._revision = 0;
        this._report();
    }

    joinParties(given) {
        var party = given[JSON_TEXT.name], self = this;
        if (!party) return [];
        this._member = party.join("jsonInput", { Text: function (m) { self._revision = m.revision; self._store.set(m.text); } });
        this._member.tell({ kind: "CurrentRequested" });
        return [this._member];
    }

    leave() { super.leave(); this._member = null; }

    /** The text is now this: told to the bench when joined, set here alone when not. */
    _set(text) {
        if (this._member) this._member.tell({ kind: "SetText", text: String(text) });
        else { this._revision++; this._store.set(text); }
    }

    _report() {
        var s = this._store, err = s.error();
        this._status.textContent = (err ? "✗ " + err : "✓ parsed") + "   |   revision " + this._revision + "   |   " + s.text().length + " characters";
    }

    disposed() { if (this._unsub) { this._unsub(); this._unsub = null; } }
}
