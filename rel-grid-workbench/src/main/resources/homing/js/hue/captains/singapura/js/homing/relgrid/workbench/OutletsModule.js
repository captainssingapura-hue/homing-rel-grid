// =============================================================================
// Outlets — the same six dishes sold at three outlets, one table each, stacked
// in a GROUP. Every table is an ordinary grid wired as it would be alone: its
// own relation over the ledger, its own cells kept current by the ledger. The
// group adds what separate tables cannot agree on by themselves - column
// widths - and the fences between them, which are the domain's: a name and its
// totals above each book, the ledger below the last. Its ledger is its own, in
// memory: a ledger is a session's.
//
// THE GROUP IS A VALUE: its header mode is fixed for its life, so the mode
// toggle tears the group down and builds it again the other way - new
// relations, new fences, new cells - carrying over only what the host keeps of
// a group's state: its widths and which members were folded.
//
//   new Outlets(container, params)   params: none
//   (the rest is a BenchWidget's)
// =============================================================================

class Outlets extends BenchWidget {
    constructor(container, params) {
        super(container, "outlets", "Outlets");
        var self = this, body = this.body;
        this.el("hint", "div", wb_hint, body,
            "OUTLETS — the same six dishes sold at three outlets, one table each, stacked in a GROUP. Every table is an ordinary grid wired as it would be alone: its own relation over the ledger, its own cells kept current by the store, its own cursor and selection. The group adds the one thing separate tables cannot agree on by themselves — column widths: drag a header edge on the first table, or Alt+←/→ on any, and every table follows — and the fences between them, which are the domain’s: a name and its totals above each book, the ledger below the last. Trade at one outlet and only that book and that fence move. The ▾ on a fence folds its book: the group hides the box and the table inside never knows — the toggle is the domain’s, and it TELLS the group through a closure this bench wired into its fence.");
        var bar = this.el("bar", "div", wb_bar, body);
        this._host = this.el("host", "div", wb_host, body);
        this._status = this.el("status", "div", wb_status, body);
        this._owner = Object.freeze({ toString: function () { return "outlets"; } });
        this._current = null;                    // { group, relations, fences, mode }
        this._modeBtn = null;

        // The domain: a ledger of its own; a relation per outlet over it, each keeping its own
        // cells current; a fence per outlet, kept current the same way.
        var store = this._ledger = new SalesStore();
        this._build("group", this._teardown());
        this._unsub = store.subscribe(function () { self._report(); });

        var n = 0;
        var act = function (label, fn) { return self.button("act" + (++n), label, function () { fn(); self._report(); }, bar); };
        act("a burst of trade (every outlet)", function () { store.trade(); });
        store.outlets().forEach(function (o) { act("trade at " + o.name, function () { store.trade(o.id); }); });
        act("reset ledger", function () { store.reset(); });
        // The host's own road to the same fold: the group's verbs, no fence involved.
        act("fold all", function () { if (self._current) self._current.group.foldAll(true); });
        act("unfold all", function () { if (self._current) self._current.group.foldAll(false); });
        // The header mode is the group's for life: toggling is a new group, the old one's widths
        // and folds carried over.
        this._modeBtn = act("header: group → each", function () {
            var next = (self._current && self._current.mode === "group") ? "each" : "group";
            self._build(next, self._teardown());
        });
        this._report();
    }

    _build(mode, kept) {
        var self = this, store = this._ledger, relations = {}, fences = [];
        // EXACTLY TWO BRANCHES under the widget's, both dissolved at teardown. 'grid' is handed
        // whole to the group; 'domain' is the widget's to divide - a part per relation for its
        // cells, and one per fence for what it draws.
        var gridB = this.branch.createBranch("grid");
        var domainB = this.branch.createBranch("domain");
        domainB.activate(this._owner);
        var members = store.outlets().map(function (o) {
            var relation = new OutletRelation(store, o.id, { branch: domainB.createBranch("cells-" + o.id) });
            relations[o.id] = relation;
            // The fence's toggle TELLS through a closure this host wires onto the group it is
            // about to build: the fence knows the host, the host knows the group.
            var fence = new OutletFence(store, o.id, {
                branch: domainB.createBranch("fence-" + o.id),
                tell: function (message) { return self._current ? self._current.group.tell(message) : false; },
                folded: kept.folded.indexOf(o.id) >= 0
            });
            fences.push(fence);
            // Ordinary grid options - the same a table alone would take.
            return { id: o.id, fence: fence, grid: { relation: relation, label: "Outlet — " + o.name } };
        });
        var ledger = new LedgerFence(store, { branch: domainB.createBranch("fence-ledger") });
        fences.push(ledger);
        var group = new RelGridGroup({
            container: this._host,
            branch: gridB,
            members: members,
            fence: ledger,
            header: mode,                      // 'group': one header at the top; 'each': one per table
            stickyHeader: true,                // 'group': the one header stays while the books scroll under it
            columnWidths: kept.widths,
            folded: kept.folded,
            // REPORTS: widths once per change however many tables moved; a fold per member.
            onColumnResized: function () { self._report(); },
            onFolded: function () { self._report(); },
            label: "Outlets"
        });
        this._current = { group: group, relations: relations, fences: fences, mode: mode };
        this.keysTo = group;
    }

    /**
     * What the host keeps of a group's state, and the rest taken down: the group destroyed (it
     * disposes no fence), the fences and relations disposed by their owner, which is this
     * widget, and both branches dissolved so the names are free again.
     */
    _teardown() {
        var c = this._current;
        if (!c) return { widths: { dish: 140, sold: 80, revenue: 110, lastSale: 110 }, folded: [] };
        var kept = { widths: c.group.columnWidths(), folded: c.group.members().filter(function (id) { return c.group.folded(id); }) };
        this._current = null;
        this.keysTo = null;
        c.group.destroy();
        c.fences.forEach(function (f) { f.dispose(); });
        Object.keys(c.relations).forEach(function (id) { c.relations[id].dispose(); });
        this.branch.dissolveBranch("grid");
        this.branch.dissolveBranch("domain");
        return kept;
    }

    _report() {
        var c = this._current;
        if (!c) return;
        var group = c.group, relations = c.relations;
        var w = group.columnWidths(), parts = [];
        Object.keys(w).forEach(function (col) { parts.push(col + " " + w[col]); });
        var cells = 0;
        Object.keys(relations).forEach(function (id) { cells += relations[id].cellCount(); });
        var folded = group.members().filter(function (id) { return group.folded(id); });
        this._status.textContent = "ledger revision " + this._ledger.revision() + "   |   header: " + c.mode
                                 + "   |   tables " + group.members().join(", ")
                                 + "   |   folded " + (folded.length ? folded.join(", ") : "none")
                                 + "   |   cells owned " + cells + "   |   widths " + parts.join(" · ");
        if (this._modeBtn) this._modeBtn.textContent = "header: " + c.mode + " → " + (c.mode === "group" ? "each" : "group");
    }

    disposed() {
        if (this._unsub) { this._unsub(); this._unsub = null; }
        this._teardown();
    }
}
