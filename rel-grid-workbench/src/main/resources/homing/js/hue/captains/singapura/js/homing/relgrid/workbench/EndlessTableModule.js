// =============================================================================
// EndlessTable — the stress bench for the two branches: a window of twenty over
// a million rows, the grid never told there are more, measured through the
// party. Its own data: an EndlessRelation over its own branch.
//
// EXACTLY TWO BRANCHES under the widget's: 'grid', handed whole to the grid,
// which activates it and mints everything it makes on it; 'domain', the
// widget's to divide - here one part, 'cells', the relation's own. Dissolving
// both is the widget's.
//
// THE SELF-CHECK reads the party's numbers, not the DOM's: the grid's branch
// must hold a constant count on the slots it started with, the domain's at
// most two windows of rows, and the registry exactly the window.
//
//   new EndlessTable(container, params)   params: none
//   (the rest is a BenchWidget's)
// =============================================================================

class EndlessTable extends BenchWidget {
    constructor(container, params) {
        super(container, "endlessTable", "Endless table");
        var N = 1000000, W = 20, self = this, body = this.body;
        var hint = this.el("hint", "div", wb_hint, body);
        var bar = this.el("bar", "div", wb_bar, body);
        var host = this.el("host", "div", wb_window_host, body);
        this._status = this.el("status", "div", wb_status, body);
        this._check = this.el("check", "div", wb_status, body);
        this._gridB = this.branch.createBranch("grid");
        this._domainB = this.branch.createBranch("domain");
        this._domainB.activate(Object.freeze({ toString: function () { return "endless"; } }));

        hint.textContent = "ENDLESS — a million rows, twenty shown, and the grid never told there are more. The wheel over the table, ↑/↓ at the top or bottom row, PageUp/PageDown, and the buttons ask the relation for the View a few rows on; the grid arranges what comes back on the same twenty rows of slots. Every column but id edits (Enter), and an edit survives its row being scrolled away and back: the value is the relation’s. The readouts below are the party’s numbers, not the DOM’s: the grid’s branch must hold a constant count on the slots it started with, the domain’s at most two windows of rows, and the registry exactly the window.";

        this._N = N;
        this._W = W;
        this._relation = new EndlessRelation({ branch: this._domainB.createBranch("cells"), rows: N, window: W });
        this._cols = this._relation.columns().length;
        this._edges = 0;
        this._steps = 0;
        this._lastMs = 0;
        this._burst = null;
        this._grid = null;
        this._grid = new RelGrid({
            container: host,
            branch: this._gridB,
            relation: this._relation,
            header: { show: true, sticky: true },
            label: "Endless",
            onArranged: function () { self._afterChange(); },
            onCursorMoved: function () { self._report(); },
            onEdge: function () { self._edges++; self._report(); }
        });
        this._grid.setColumnWidths({ id: 70, name: 140, qty: 64, price: 70, note: 130 });
        this.keysTo = this._grid;
        this._slots0 = this._gridB.getBranch("slots");
        this._gridCount0 = EndlessTable._countTree(this._gridB);
        this._report();
        this._measure();

        var press = function (fn) { return function () { fn(); self._grid.focus(); }; };
        this.button("down1", "↓ 1 row", press(function () { self._step(1); }), bar);
        this.button("up1", "↑ 1 row", press(function () { self._step(-1); }), bar);
        this.button("downPage", "↓ page", press(function () { self._step(W); }), bar);
        this.button("upPage", "↑ page", press(function () { self._step(-W); }), bar);
        this.button("start", "to start", press(function () { self._step(-N); }), bar);
        this.button("end", "to end", press(function () { self._step(N); }), bar);
        this.button("burstRows", "burst: 300 rows down", press(function () { self._run("300 x 1 row", 300, function () { return 1; }); }), bar);
        this.button("burstPages", "burst: 100 pages", press(function () { self._run("100 x page", 100, function (k) { return (k % 2) ? -W : W; }); }), bar);
        this.button("burstJumps", "burst: 100 random jumps", press(function () { self._run("100 random jumps", 100, function () { return Math.floor((Math.random() - 0.5) * 2 * N); }); }), bar);
    }

    /** The party's own count of a subtree: elements on the branch, and on every branch under it. */
    static _countTree(b) {
        var n = b.elementCount;
        b.listBranches().forEach(function (name) { n += EndlessTable._countTree(b.getBranch(name)); });
        return n;
    }

    static _now() { return (typeof performance !== "undefined" && performance.now) ? performance.now() : Date.now(); }

    /** THE SELF-CHECK, on the party's numbers. The grid's side is a constant; the domain's is bounded; the registry is the window; every slot holds one cell. */
    _measure() {
        var W = this._W, cols = this._cols;
        var gridCount = EndlessTable._countTree(this._gridB), domainCount = EndlessTable._countTree(this._domainB);
        var held = this._relation.held(), registry = this._grid.cells().size();
        var slots = this._gridB.getBranch("slots"), kept = slots === this._slots0, one = true;
        for (var i = 0; i < W && one; i++) for (var j = 0; j < cols; j++) {
            var td = slots.getElement("td-" + i + "-" + j);
            if (!td || td.children.length !== 1) { one = false; break; }
        }
        var bad = [];
        if (gridCount !== this._gridCount0) bad.push("grid elements " + gridCount + ", were " + this._gridCount0);
        if (!kept) bad.push("slots re-minted");
        if (held > 2 * W) bad.push("rows held " + held + " > " + (2 * W));
        if (registry !== W * cols) bad.push("registry " + registry + " != " + (W * cols));
        if (!one) bad.push("a slot without exactly one cell");
        this._check.textContent = "grid elements " + gridCount + " (constant)   |   slots " + (kept ? "kept" : "RE-MINTED")
                                + "   |   domain elements " + domainCount + "   |   rows held " + held + " (bound " + (2 * W) + ")"
                                + "   |   registry " + registry + " (= " + (W * cols) + ")"
                                + "   |   " + (bad.length ? "OFF: " + bad.join(", ") : "every invariant holds")
                                + "   |   last step " + this._lastMs.toFixed(2) + "ms" + (this._burst ? "   |   " + this._burst : "");
    }

    _report() {
        var r = this._relation, c = this._grid ? this._grid.cursor() : null;
        this._status.textContent = "window " + this._W + " of " + this._N + " at row " + r.at()
                                 + "   |   steps " + this._steps + "   |   edges " + this._edges
                                 + "   |   cells minted " + r.mints() + ", rows freed " + r.frees()
                                 + "   |   cursor " + (c ? c.pk + "/" + c.column : "—");
    }

    _afterChange() {
        if (!this._grid) return;                       // the first arrangement runs inside the grid's constructor
        this._steps++;
        this._report();
        this._measure();
    }

    /** Every step through the grid's own twin of the wheel, timed. */
    _step(by) {
        var t0 = EndlessTable._now(), moved = this._grid.scrollRows(by);
        this._lastMs = EndlessTable._now() - t0;
        if (!moved) { this._report(); this._measure(); }   // an end: nothing arranged, still measured
        return moved;
    }

    _run(label, times, byAt) {
        var t0 = EndlessTable._now(), moved = 0;
        for (var k = 0; k < times; k++) if (this._grid.scrollRows(byAt(k))) moved++;
        var ms = EndlessTable._now() - t0;
        this._burst = label + ": " + moved + " of " + times + " steps in " + ms.toFixed(0) + "ms, " + (ms / times).toFixed(2) + "ms each";
        this._report();
        this._measure();
    }

    disposed() {
        this._grid.destroy();
        this._relation.dispose();
    }
}
