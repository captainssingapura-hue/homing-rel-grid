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
//   createGamesTreeRelation(store, { branch, onViewChanged?, slow?, told?, delay? })
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

function createGamesTreeRelation(store, opts) {
    opts = opts || {};
    var branch = opts.branch;
    if (!branch) throw new Error("[GamesTreeRelation] opts.branch is required: the cells mint on the domain's branch");
    var slowType = opts.slow === undefined ? _WB_TREE_SLOW : opts.slow;
    var toldType = opts.told === undefined ? _WB_TREE_TOLD : opts.told;
    var delay = opts.delay || 900;
    var owner = Object.freeze({ toString: function () { return "games tree relation"; } });
    branch.activate(owner);                        // its own: unactivated when handed; a cell's sub-branch is the cell's

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

    var open = new Set(), cells = new Map(), seq = 0;

    function typeKey(t) { return "t:" + t; }
    function seriesKey(t, s) { return "s:" + t + "|" + s; }
    function places() {
        var out = [];
        types.forEach(function (t) {
            var tk = typeKey(t), entry = byType.get(t);
            out.push({ key: tk, depth: 0, fold: open.has(tk) ? "open" : "closed" });
            if (!open.has(tk)) return;
            entry.series.forEach(function (s) {
                var sk = seriesKey(t, s), games = entry.byName.get(s);
                out.push({ key: sk, depth: 1, fold: open.has(sk) ? "open" : "closed" });
                if (open.has(sk)) games.forEach(function (pk) { out.push({ key: pk, depth: 2, fold: "leaf" }); });
            });
        });
        return out;
    }

    // ── what a node says: the domain's cell, a stock text cell with the node's line ──
    function textOf(key) {
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
    function known(key) {
        if (key.slice(0, 2) === "t:") return byType.has(key.slice(2));
        if (key.slice(0, 2) === "s:") { var p = key.slice(2).split("|"); return byType.has(p[0]) && byType.get(p[0]).byName.has(p[1]); }
        return store.get(key, "title") !== undefined;
    }

    function tell() {
        if (opts.onViewChanged) opts.onViewChanged();
    }
    // The node's own cell shows the wait: made on first ask, so it exists by the time it is asked about.
    function busy(key, on) {
        var c = cells.get(key);
        if (c) c.setBusy(on);
    }

    var relation = {
        view: function () { return places(); },
        cellFor: function (key) {
            if (!known(key)) throw new Error("[GamesTreeRelation] no such node: " + key);
            var c = cells.get(key);
            if (!c) {
                c = new RelTreeTextCell({ branch: branch.createBranch("n" + (++seq)), text: textOf(key) });
                cells.set(key, c);
            }
            return c;
        },
        // THE CHANNEL, as the host wires it: a question in, a thenable out. The three ways.
        answer: function (question, mask) {
            if (question instanceof RelTreeUnfold) {
                var key = question.key, t = key.slice(0, 2) === "t:" ? key.slice(2) : null;
                if (t === toldType) {                          // nothing now; fetched, then told
                    busy(key, true);
                    setTimeout(function () { busy(key, false); open.add(key); tell(); }, delay);
                    return Promise.resolve();
                }
                if (t === slowType) {                          // late, with a note on the panel meanwhile
                    var nb = branch.createBranch("note" + (++seq));      // the note's own branch: dissolved when the answer comes
                    nb.activate(owner);
                    var note = nb.createElement("note", "div");
                    note.textContent = "Fetching the " + t + " series… (the tree is locked and washed; this is the domain's note on the tree's panel)";
                    mask.panel(note);
                    busy(key, true);
                    return new Promise(function (resolve) {
                        setTimeout(function () { busy(key, false); open.add(key); resolve(new RelTreeView(places())); nb.dissolve(); }, delay);
                    });
                }
                open.add(key);
                return Promise.resolve(new RelTreeView(places()));
            }
            if (question instanceof RelTreeFold) {
                open.delete(question.key);                       // the cells under it are kept: a node that returns answers the same one
                return Promise.resolve(new RelTreeView(places()));
            }
            return Promise.resolve();                            // a notification: heard, not answered
        },
        // The domain's own fold state, for a host's controls: change it, and tell.
        open: function (key) { if (known(key)) open.add(key); },
        close: function (key) { open.delete(key); },
        closeAll: function () { open.clear(); },
        isOpen: function (key) { return open.has(key); },
        cellCount: function () { return cells.size; },
        cell: function (key) { return cells.get(key) || null; },      // for a host's readout, or a test: the cell as made, or null
        types: function () { return types.slice(); },
        typeKey: typeKey, seriesKey: seriesKey,
        describe: function () {
            var n = places().length, o = open.size;
            return n + " node" + (n === 1 ? "" : "s") + " presented, " + o + " open, " + cells.size + " cell" + (cells.size === 1 ? "" : "s") + " made";
        }
    };
    return relation;
}
