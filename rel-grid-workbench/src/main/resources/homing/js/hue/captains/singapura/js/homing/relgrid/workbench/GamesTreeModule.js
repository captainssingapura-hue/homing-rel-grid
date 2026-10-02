// =============================================================================
// GamesTree — the Games Tree bench: the tree view's first bench, the same
// catalogue as a lazy three-level tree, the fold and the unfold asked on the
// channel. Its own data: a GamesStore over the catalogue, a GamesTreeRelation
// over it.
//
// EXACTLY TWO BRANCHES under the widget's: 'tree', handed whole to the tree;
// 'domain', the widget's to divide - 'cells', the relation's own. The tree
// owns the rows, the indent, the carets, the cursor and the keys; the relation
// owns every cell and its own fold state; the widget only wires the two.
//
//   new GamesTree(container, params)   params: none
//   (the rest is a BenchWidget's)
// =============================================================================

class GamesTree extends BenchWidget {
    constructor(container, params) {
        super(container, "gamesTree", "Games tree");
        var self = this, body = this.body;
        var hint = this.el("hint", "div", wb_hint, body);
        var bar = this.el("bar", "div", wb_bar, body);
        // The frame goes round the SCROLLPORT - a non-scrolling wrapper the port fills - so the
        // light and the hairline sit outside the scrollbar; the tree inside draws no frame of its own.
        var frame = this.el("frame", "div", null, body);
        css.addClass(frame, wb_frame, hrg_frame, hrg_lit);
        var host = this.el("host", "div", wb_port, frame);
        this._status = this.el("status", "div", wb_status, body);
        var treeB = this.branch.createBranch("tree");
        var domainB = this.branch.createBranch("domain");
        domainB.activate(Object.freeze({ toString: function () { return "games tree"; } }));

        this._tree = null;
        this._activated = null;
        var store = this._store = new GamesStore(GAMES_CATALOGUE);
        var relation = this._relation = new GamesTreeRelation(store, {
            branch: domainB.createBranch("cells"),
            // The relation fetched what an unfold asked for and answered nothing at the time:
            // the common parent tells the tree so, unasked, and the tree asks view() again.
            onViewChanged: function () { if (self._tree) self._tree.tell(new RelTreeViewChanged()); self._report(); }
        });

        hint.textContent = "GAMES TREE — the catalogue’s " + store.size() + " releases as TYPE → SERIES → TITLE, lazily: the types are closed at first, and nothing under a closed node is asked for. The tree view owns the rows, the indent, the carets, the cursor and the keys (↑↓, → to unfold or step in, ← to fold or step out, Space, Enter, Home, End); the relation owns every cell and its own fold state. An unfold and a fold are QUESTIONS on the ask channel, answered with the whole View. Three types answer three ways: most at once; STRATEGY late, with the tree locked and washed and the domain’s note on the panel; PUZZLE with nothing, then a tell when its series arrive — the tree never locks.";

        this._tree = new RelTree({
            container: host,
            branch: treeB,
            relation: relation,
            label: "Games tree",
            folder: true,                              // the fold state a second way: a closed or an open folder after the caret
            // THE CHANNEL: the relation answers; this widget only wires the two together.
            // Reported after this turn: a question answered at once has settled by then and never reads as pending.
            ask: function (question, mask) { var out = relation.answer(question, mask); setTimeout(function () { if (self.alive) self._report(); }, 0); return out; },
            onArranged: function () { self._report(); },
            onCursorMoved: function () { self._report(); },
            onActivated: function (key) { self._activated = key; self._report(); }
        });
        this.keysTo = this._tree;
        this._report();

        var tree = this._tree, n = 0;
        var act = function (label, fn) { self.button("act" + (++n), label, function () { fn(); self._report(); tree.focus(); }, bar); };
        // The navigator's move: the DOMAIN opens the ancestors - it owns the fold state - tells,
        // and asks for the cursor. The tree opens nothing on its behalf.
        act("reveal Need for Speed: Most Wanted (2005)", function () {
            relation.open(relation.typeKey("Racing"));
            relation.open(relation.seriesKey("Racing", "Need for Speed"));
            tree.tell(new RelTreeViewChanged());
            var pk = store.pks().filter(function (k) { return store.get(k, "title") === "Need for Speed: Most Wanted" && store.get(k, "year") === 2005; })[0];
            if (pk) tree.selectNode(pk);
        });
        act("open every type", function () { relation.types().forEach(function (t) { relation.open(relation.typeKey(t)); }); tree.tell(new RelTreeViewChanged()); });
        act("close all", function () { relation.closeAll(); tree.tell(new RelTreeViewChanged()); });
    }

    _report() {
        var t = this._tree, c = t ? t.cursor() : null;
        this._status.textContent = this._relation.describe()
                                 + "   |   cursor " + (c === null ? "—" : c)
                                 + (this._activated === null ? "" : "   |   activated " + this._activated)
                                 + (t && t.isPending() ? "   |   PENDING — locked" : "");
    }

    disposed() {
        this._tree.destroy();
    }
}
