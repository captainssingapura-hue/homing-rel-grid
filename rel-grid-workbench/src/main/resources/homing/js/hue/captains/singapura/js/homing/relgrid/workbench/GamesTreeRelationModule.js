// =============================================================================
// GamesTreeRelation — the Games Tree bench's TREE RELATION: the catalogue as a
// three-level tree, type → series → title, over the store that exists. DOMAIN
// CODE: it answers the tree's two questions — view(), the places to present
// now, and cellFor(key), the cell for a node — and, on the channel, the
// unfold and the fold, by flipping a fold state OF ITS OWN and answering the
// whole View. It knows nothing of rows, carets or cursors, and the word
// "tree" here means the shape of the catalogue, not a component.
//
// LAZY BY DEFAULT. The relation begins with the types, closed; a series is
// asked for when its type is unfolded, a title when its series is. Nothing
// under a closed node is ever placed — or built: a cell is made on first ask
// and kept, so the domain's cell count is exactly the nodes ever shown.
//
// THREE WAYS TO ANSWER, on purpose, so the bench shows all three:
//   · most types answer at once — the question resolves on a microtask, and
//     the tree never shows a mask;
//   · one type (Strategy) answers LATE, after a delay, with a note on the
//     mask's panel meanwhile — the tree is locked and washed, as for a copy;
//   · one type (Puzzle) answers NOTHING, then fetches and TELLS — the tree is
//     never locked, and the children appear when the owner is told.
// While either fetch runs THE NODE ITSELF SPINS: the cell is the domain's, so
// the domain marks it busy when the question arrives and clear when the
// children land. The tree knows nothing of it.
//
//   new GamesTreeRelation(store, { branch, onViewChanged?, slow?, told?, delay? })
//       branch: the relation's OWN, unactivated when handed; it activates
//   relation.view() / cellFor(key)                 the tree relation
//   relation.answer(question, mask) → thenable     the channel, as the host wires it
//   relation.open(key) / close(key) / closeAll()   the domain's own fold state, for a host's controls
//   relation.isOpen(key) / cell(key) / cellCount() / describe()
//
// Keys: 't:' + type; 's:' + type + '|' + series (a series may recur under two
// types); the game's own pk for a title.
// =============================================================================

var _WB_TREE_SLOW = "Strategy", _WB_TREE_TOLD = "Puzzle";

class GamesTreeRelation {
    constructor(store, opts) {
        var o = opts || {};
        if (!o.branch) throw new Error("[GamesTreeRelation] opts.branch is required: the cells mint on the domain's branch");
        this._store = store;
        this._branch = o.branch;
        this._slowType = o.slow === undefined ? _WB_TREE_SLOW : o.slow;
        this._toldType = o.told === undefined ? _WB_TREE_TOLD : o.told;
        this._delay = o.delay || 900;
        this._onViewChanged = o.onViewChanged || null;
        this._owner = Object.freeze({ toString: function () { return "games tree relation"; } });
        this._branch.activate(this._owner);            // its own: unactivated when handed; a cell's sub-branch is the cell's

        // ── the structure, from the store: types in order of count, series in order of first release ──
        var types = [], byType = new Map();            // type → { series: [name…], byName: Map name → [pk…] }
        store.pks().forEach(function (pk) {
            var t = store.get(pk, "type"), s = store.get(pk, "series") || store.get(pk, "title");
            var entry = byType.get(t);
            if (!entry) { entry = { series: [], byName: new Map(), count: 0 }; byType.set(t, entry); types.push(t); }
            var games = entry.byName.get(s);
            if (!games) { games = []; entry.byName.set(s, games); entry.series.push(s); }
            games.push(pk);
            entry.count++;
        });
        types.sort(function (a, b) { return byType.get(b).count - byType.get(a).count || a.localeCompare(b); });
        this._types = types;
        this._byType = byType;
        this._open = new Set();
        this._cells = new Map();
        this._seq = 0;
    }

    typeKey(t) { return "t:" + t; }
    seriesKey(t, s) { return "s:" + t + "|" + s; }

    view() { return this._places(); }

    cellFor(key) {
        if (!this._known(key)) throw new Error("[GamesTreeRelation] no such node: " + key);
        var c = this._cells.get(key);
        if (!c) {
            c = new RelTreeTextCell({ branch: this._branch.createBranch("n" + (++this._seq)), text: this._textOf(key) });
            this._cells.set(key, c);
        }
        return c;
    }

    // THE CHANNEL, as the host wires it: a question in, a thenable out. The three ways.
    answer(question, mask) {
        var self = this;
        if (question instanceof RelTreeUnfold) {
            var key = question.key, t = key.slice(0, 2) === "t:" ? key.slice(2) : null;
            if (t === this._toldType) {                          // nothing now; fetched, then told
                this._busy(key, true);
                setTimeout(function () { self._busy(key, false); self._open.add(key); self._tell(); }, this._delay);
                return Promise.resolve();
            }
            if (t === this._slowType) {                          // late, with a note on the panel meanwhile
                var nb = this._branch.createBranch("note" + (++this._seq));      // the note's own branch: dissolved when the answer comes
                nb.activate(this._owner);
                var note = nb.createElement("note", "div");
                note.textContent = "Fetching the " + t + " series… (the tree is locked and washed; this is the domain's note on the tree's panel)";
                mask.panel(note);
                this._busy(key, true);
                return new Promise(function (resolve) {
                    setTimeout(function () { self._busy(key, false); self._open.add(key); resolve(new RelTreeView(self._places())); nb.dissolve(); }, self._delay);
                });
            }
            this._open.add(key);
            return Promise.resolve(new RelTreeView(this._places()));
        }
        if (question instanceof RelTreeFold) {
            this._open.delete(question.key);                     // the cells under it are kept: a node that returns answers the same one
            return Promise.resolve(new RelTreeView(this._places()));
        }
        return Promise.resolve();                                // a notification: heard, not answered
    }

    // The domain's own fold state, for a host's controls: change it, and tell.
    open(key) { if (this._known(key)) this._open.add(key); }
    close(key) { this._open.delete(key); }
    closeAll() { this._open.clear(); }
    isOpen(key) { return this._open.has(key); }
    cellCount() { return this._cells.size; }
    /** For a host's readout, or a test: the cell as made, or null. */
    cell(key) { return this._cells.get(key) || null; }
    types() { return this._types.slice(); }

    describe() {
        var n = this._places().length, o = this._open.size, c = this._cells.size;
        return n + " node" + (n === 1 ? "" : "s") + " presented, " + o + " open, " + c + " cell" + (c === 1 ? "" : "s") + " made";
    }

    _places() {
        var out = [], self = this;
        this._types.forEach(function (t) {
            var tk = self.typeKey(t), entry = self._byType.get(t);
            out.push({ key: tk, depth: 0, fold: self._open.has(tk) ? "open" : "closed" });
            if (!self._open.has(tk)) return;
            entry.series.forEach(function (s) {
                var sk = self.seriesKey(t, s), games = entry.byName.get(s);
                out.push({ key: sk, depth: 1, fold: self._open.has(sk) ? "open" : "closed" });
                if (self._open.has(sk)) games.forEach(function (pk) { out.push({ key: pk, depth: 2, fold: "leaf" }); });
            });
        });
        return out;
    }

    // ── what a node says: the domain's cell, a stock text cell with the node's line ──
    _textOf(key) {
        var store = this._store, byType = this._byType;
        if (key.slice(0, 2) === "t:") {
            var t = key.slice(2), e = byType.get(t);
            return t + " — " + e.count + " release" + (e.count === 1 ? "" : "s") + " in " + e.series.length + " series";
        }
        if (key.slice(0, 2) === "s:") {
            var parts = key.slice(2).split("|"), games = byType.get(parts[0]).byName.get(parts[1]);
            var first = store.get(games[0], "year"), last = store.get(games[games.length - 1], "year");
            return parts[1] + " — " + games.length + (games.length === 1 ? " release, " : " releases, ") + (first === last ? first : first + "–" + last);
        }
        var year = store.get(key, "year"), platform = store.get(key, "platform"), score = store.get(key, "score");
        return store.get(key, "title") + " · " + year + " · " + platform + (score === null ? "" : " · " + score);
    }

    _known(key) {
        var byType = this._byType;
        if (key.slice(0, 2) === "t:") return byType.has(key.slice(2));
        if (key.slice(0, 2) === "s:") { var p = key.slice(2).split("|"); return byType.has(p[0]) && byType.get(p[0]).byName.has(p[1]); }
        return this._store.get(key, "title") !== undefined;
    }

    _tell() { if (this._onViewChanged) this._onViewChanged(); }

    // The node's own cell shows the wait: made on first ask, so it exists by the time it is asked about.
    _busy(key, on) {
        var c = this._cells.get(key);
        if (c) c.setBusy(on);
    }
}
