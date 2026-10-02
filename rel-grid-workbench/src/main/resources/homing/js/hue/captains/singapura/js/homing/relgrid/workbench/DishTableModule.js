// =============================================================================
// DishTable — a table of the Replicating Tables bench, FOR A ROLE: chef,
// nutritionist, manager or follower. Its own DishStore is a replica of the
// bench's one store - the dishes party's: what the party says comes into it,
// and what this table's editors commit is told to the party and comes back.
// Its relation hands a commit target only to the cells this role may write,
// and keeps every cell current itself; the grid is told nothing, ever. Not
// joined, the table writes its own store, alone.
//
// EXACTLY TWO BRANCHES under the widget's, and neither side ever sees the
// other's. 'grid' is handed to the grid whole, unactivated. 'domain' is the
// widget's to divide: the relation's cells on 'cells', the panels the domain
// draws for the grid's questions on sessions under 'panels'.
//
//   new DishTable(container, params, role)   what DishChef, DishNutritionist,
//                                            DishManager and DishFollower make
//   (their kinds declare, in Java, the type they join: dishes)
//   (the rest is a BenchWidget's)
// =============================================================================

class DishTable extends BenchWidget {
    constructor(container, params, role) {
        super(container, "dish-" + role, "Dish list — " + role);
        var self = this, body = this.body, ROLE = role;
        this._role = role;
        this._member = null;
        var hint = this.el("hint", "div", wb_hint, body);
        var bar = this.el("bar", "div", wb_bar, body);
        // The frame goes round the SCROLLPORT - a non-scrolling wrapper the port fills - so the
        // light and the hairline sit outside the scrollbar; the grid inside draws no frame of its own.
        var frame = this.el("frame", "div", null, body);
        css.addClass(frame, wb_frame, hrg_frame, hrg_lit);
        var host = this.el("host", "div", wb_port, frame);
        this._status = this.el("status", "div", wb_status, body);
        // The selection, copy and view readouts: DOMAIN-owned. The grid tells what is selected,
        // writes what was chosen, hands the order over - and this decides what each is worth saying.
        this._selOut = this.el("sel", "div", wb_status, body);
        this._copyOut = this.el("copy", "div", wb_status, body, "clipboard ← nothing yet. Select, then Ctrl+C.");
        this._viewOut = this.el("view", "div", wb_status, body);
        var gridB = this.branch.createBranch("grid");
        var domainB = this.branch.createBranch("domain");
        var owner = Object.freeze({ toString: function () { return "replica-" + ROLE; } });
        domainB.activate(owner);
        var panelsB = domainB.createBranch("panels");
        panelsB.activate(owner);

        // The domain: its replica of the bench's store; a relation over it FOR A ROLE; the
        // views this table is under; the clipboard it answers a copy with.
        var store = this._store = new DishStore();
        var relation = this._relation = new DishRelation(store, { role: ROLE, branch: domainB.createBranch("cells") });
        var EDITS = relation.editableColumns();
        this._views = new DishViews(store, { onChanged: function () { self._showView(); } });
        this._lastCopy = "";
        this._clipboard = new DishClipboard(relation, {
            branch: panelsB,
            onChosen: function (format, cells) { self._lastCopy = format.toUpperCase() + " · " + cells + (cells === 1 ? " cell" : " cells"); }
        });
        this._panelsB = panelsB;
        this._toldCount = 0;

        hint.textContent = EDITS.length
            ? ROLE.toUpperCase() + " — edits " + EDITS.join(" and ") + " only. Click or arrow to a cell (shallow); Enter or double-click to edit (deep). Two ways nothing opens: on sold and popularity the grid never even asks, because the relation declared those columns read-only; everywhere else it asks and the cell for this role says no. Enter commits to the bench’s store; the store tells every table; each relation updates its own cells. The grid is never told what happened."
            : "FOLLOWER — read-only. It moves when any editor commits or the shop trades, and its grid was never spoken to after construction. Select and Ctrl+C to copy: the grid asks, this table draws the choice, the grid writes it. The arrange button below (or Alt+Enter on the table) hands the ORDER of the rows over: the grid asks, this table picks a profile, the grid presents what comes back and cannot say why.";

        // The grid: given a relation and a channel, and never spoken to again. It does not know
        // the role, and there is nothing in its construction that could carry it.
        var grid = this._grid = new RelGrid({
            container: host,
            branch: gridB,
            relation: relation,
            header: { sticky: true },          // the host scrolls; the header stays (map 12)
            frame: false,                      // the host frames the scrollport; see above
            ask: function (question, mask) { return self._ask(question, mask); },
            // A REPORT: the grid wrote this. What it is was decided above.
            onCopied: function (content) {
                self._copyOut.textContent = "clipboard ← " + self._lastCopy + " · " + content.text.length + " chars of text"
                                          + (content.html != null ? " + " + content.html.length + " of html" : "");
            },
            label: "Dish list — " + ROLE
        });
        this.keysTo = grid;

        this._unsub = store.subscribe(function () { self._report(); self._showView(); });
        this._report();
        this._showView();

        var n = 0;
        var act = function (label, fn) { self.button("act" + (++n), label, function () { fn(); self._report(); }, bar); };
        if (ROLE === "manager") {
            // The shop trades. Sales are the only thing that moves sold, and popularity is
            // re-derived from them for every dish - a domain push no editor could make by editing.
            act("a day of trade (sales move popularity)", function () { store.trade(); });
            act("reset store to seed", function () { store.reset(); });
        }
        act("re-arrange (grid.reapply)", function () { grid.reapply(); });
        // THE BENCH'S OWN CONTROL for the domain's arrangement - not the grid's, because a
        // domain's arrangement is not a property of any column. It calls the verb; the grid
        // asks; this table answers on the panel.
        act("arrange the rows… (grid.handoverView)", function () { grid.handoverView(); });
    }

    joinParties(given) {
        var party = given[DISHES.name], self = this;
        if (!party) return [];
        var member = this._member = party.join("dish-" + this._role, {
            State: function (m) {
                var data;
                try { data = JSON.parse(m.dishes); } catch (e) { return; }
                self._store.apply(data, m.revision);
                self._report();
            }
        });
        // Joined, the store writes through: what an editor commits goes to the bench, and comes back.
        this._store.writeTo({
            commit: function (pk, col, v) { member.tell({ kind: "Commit", pk: pk, column: col, value: JSON.stringify(v) }); },
            sell:   function (pk, n) { member.tell({ kind: "Sell", pk: pk, n: n }); },
            reset:  function () { member.tell({ kind: "Reset" }); }
        });
        member.tell({ kind: "CurrentRequested" });
        return [member];
    }

    leave() {
        super.leave();
        this._member = null;
        this._store.writeTo(null);
    }

    // THE CHANNEL, and its customers. The grid asks; the domain answers. A selection
    // notification expects NO answer, handled on a microtask on purpose: by then the grid has
    // painted and returned, which is what fire-and-forget means. A copy request and a view
    // handover are the other kind: the grid WAITS, and the domain answers on the grid's panel.
    _ask(question, mask) {
        var self = this;
        if (question instanceof RelGridSelectionChanged) {
            this._toldCount++;
            return Promise.resolve().then(function () { if (self.alive) self._showSelection(question); });
        }
        if (question instanceof RelGridCopyRequested) return this._clipboard.answer(question, mask);
        if (question instanceof RelGridViewHandover) return this._views.answer(question, mask, { branch: this._panelsB });
        return Promise.resolve();      // not understood: answered with nothing
    }

    _showSelection(q) {
        var cells = 0, lines = [];
        for (var k = 0; k < q.ranges.length; k++) {
            var r = q.ranges[k], n = (r.i1 - r.i0 + 1) * (r.j1 - r.j0 + 1);
            cells += n;
            lines.push("   rows " + r.i0 + "–" + r.i1 + " × cols " + r.j0 + "–" + r.j1 + "   (" + n + ")");
        }
        var many = q.ranges.length !== 1;
        this._selOut.textContent = "told by the grid × " + this._toldCount
                                 + "   |   " + q.ranges.length + (many ? " ranges" : " range")
                                 + "   |   " + cells + (cells === 1 ? " cell" : " cells") + "\n" + lines.join("\n");
    }

    /** The readout is DOMAIN state: the store's revision, the relation's cell count and what it may edit. Nothing here reads the grid. */
    _report() {
        var edits = this._relation.editableColumns();
        this._status.textContent = "store revision " + this._store.revision()
                                 + "   |   cells owned by this relation " + this._relation.cellCount()
                                 + "   |   " + (edits.length ? "edits " + edits.join(", ") : "read-only")
                                 + (this._member ? "" : "   |   alone: this table’s own store");
    }

    /** The explanation of the order - the one thing the grid cannot give, because it holds nothing about it. */
    _showView() {
        this._viewOut.textContent = "view ← " + this._views.describe() + "   |   the arrange button, or Alt+Enter on the table, to change";
    }

    disposed() {
        if (this._unsub) { this._unsub(); this._unsub = null; }
        this._grid.destroy();
        this._relation.dispose();
    }
}
