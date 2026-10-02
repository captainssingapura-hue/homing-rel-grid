// =============================================================================
// JsonDisplay — the JSON Tree bench's display: the kit's JsonTreeView over the
// bench's one document, live. What the json-text party says is parsed into its
// own document; one line per change feeds the view the last value that parsed.
// A frame round the port, a bar of the view's verbs, a readout of the cursor's
// pointer and the value under it. Not joined, it shows the sample.
//
//   new JsonDisplay(container, params)   params: none
//   (its kind declares, in Java, the type it joins: json-text)
//   (the rest is a BenchWidget's)
// =============================================================================

class JsonDisplay extends BenchWidget {
    constructor(container, params) {
        super(container, "jsonDisplay", "JSON tree");
        var self = this, body = this.body;
        var hint = this.el("hint", "div", wb_hint, body);
        var bar = this.el("bar", "div", wb_bar, body);
        // The frame goes round the SCROLLPORT: the tree draws none of its own.
        var frame = this.el("frame", "div", null, body);
        css.addClass(frame, wb_frame, hrg_frame, hrg_lit);
        var port = this.el("port", "div", wb_port, frame);
        this._status = this.el("status", "div", wb_status, body);
        this._activated = null;
        this._note = "";
        this._view = null;

        hint.textContent = "JSON TREE — the kit’s viewer over the bench’s one document, live: a JSON value is already an outline, so the document answers the tree’s places by JSON pointer and a cell per node, and the tree does the tree. Every container is closed at first under an open root; →, Space and the caret unfold. Edit in the Input pane and watch: a member typed appears, a member deleted goes, the folds and the cursor stay by pointer.";

        var store = this._store = new JsonDocStore();
        // ONE LINE: the view is the tree's host, and makes its two branches under the one it is handed.
        this._view = new JsonTreeView({
            container: port,
            branch: this.branch.createBranch("json"),
            value: store.value(),
            label: "JSON tree",
            onCursorMoved: function () { self._report(); },
            onArranged:    function () { self._report(); },
            onActivated:   function (pointer) { self._activated = pointer; self._report(); }
        });
        // ONE LINE per change: the last value that parsed, whenever the text parsed.
        this._unsub = store.subscribe(function () { if (store.error() === null) self._view.set(store.value()); self._report(); });
        this.keysTo = this._view;

        var view = this._view, n = 0;
        var verb = function (label, fn) { self.button("verb" + (++n), label, function () { fn(); self._report(); view.focus(); }, bar); };
        verb("open all", function () { view.openAll(); });
        verb("open two deep", function () { view.openAll(1); });
        verb("close all", function () { view.closeAll(); });
        // A navigator's move: a pointer typed, the path opened by the document, the cursor put on it.
        var pointer = this.el("pointer", "input", wb_input, bar);
        pointer.setAttribute("placeholder", "/releases/0/notes/2");
        pointer.setAttribute("aria-label", "a JSON pointer to reveal");
        pointer.value = "/releases/0/notes/2";
        verb("reveal", function () { self._note = view.selectPointer(pointer.value) ? "" : "no such node: " + pointer.value; });
        this._report();
    }

    joinParties(given) {
        var party = given[JSON_TEXT.name], self = this;
        if (!party) return [];
        var member = party.join("jsonDisplay", { Text: function (m) { if (m.text !== self._store.text()) self._store.set(m.text); } });
        member.tell({ kind: "CurrentRequested" });
        return [member];
    }

    /** A value said in a word for the readout: a string quoted, a container as a count, the rest as it is. */
    static said(v) {
        if (typeof v === "string") return JSON.stringify(v);
        if (v !== null && typeof v === "object") return Array.isArray(v) ? "array[" + v.length + "]" : "object{" + Object.keys(v).length + "}";
        return String(v);
    }

    _report() {
        var view = this._view;
        if (!view) return;                                   // the first pass is reported from inside the view's constructor
        var p = view.cursor(), rows = view.rows();
        this._status.textContent = rows + " row" + (rows === 1 ? "" : "s")
                                 + "   |   cursor " + (p === null ? "—" : (p === "" ? "(root)" : p))
                                 + (p === null ? "" : "   |   value " + JsonDisplay.said(view.valueAt(p)))
                                 + (this._activated === null ? "" : "   |   activated " + this._activated)
                                 + (this._store.error() === null ? "" : "   |   showing the last value that parsed")
                                 + (this._note ? "   |   " + this._note : "");
    }

    disposed() {
        if (this._unsub) { this._unsub(); this._unsub = null; }
        this._view.destroy();
    }
}
