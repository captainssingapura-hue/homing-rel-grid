// =============================================================================
// GamesRelation — the Games Catalogue's root relation, and the bench's whole
// point: a relation that sorts and filters FOR ITSELF, with header cells of
// its own, over a store of its own. DOMAIN CODE; the word "grid" does not
// appear in it. Nothing arranges anything here — this answers questions:
// what rows to present now (view), what each column is (columns, labels),
// the cell for an identity (cellFor), the header cell for a column
// (headerFor) — and the header cells it answers with are the controls by
// which a person changes what view() will answer next.
//
//   createGamesRelation(store, { branch, menuHost, onViewChanged })
//       branch: the relation's OWN, unactivated when handed; it activates, and dispose() dissolves;
//               a sub-branch per cell, a sub-branch per header cell
//       menuHost: where a header cell puts its column menu — the bench's root
//       onViewChanged(): the relation's View changed underneath whoever presents it. The
//               relation cannot reach the presenter and does not try: it tells its OWNER,
//               which is the common parent, and the owner tells the presenter.
//
//   view(intent)           the catalogue filtered and sorted by the conditions held; a
//                          movement answers nothing — a catalogue is the whole of itself
//   columns() / labels() / readOnlyColumns()   the structure; every column is read
//   cellFor(pk, col)       a text cell showing the value as catalogued; a stranger refused
//   headerFor(col)         a GamesHeaderCell: label, indication, the menu's ▾ — once per column, kept
//   conditions() / setConditions(c)   the order and the choice, as data (GamesConditions)
//   setSort(col, dir, additive) / toggleSort(col, additive) / setFilter(col, spec) / clear()
//                          what a column menu calls, and a host's programmatic twins
//   values(col)            the column's values with counts among the rows the OTHER columns'
//                          filters pass — what a menu lists, so one choice narrows the next
//   count()                how many rows the View presents; store.size() is the whole
//   dispose()
// =============================================================================

var _WB_GAMES_LABELS = { title: "title", series: "series", year: "year", platform: "platform", type: "type",
                         developer: "developer", publisher: "publisher", sales: "sales (M)", score: "score" };

/** A value as the catalogue shows it: a number as it is, sales to one place, absence as nothing. */
function _wbGamesShown(col, v) {
    if (v === null || v === undefined) return "";
    if (col === "sales") return Number(v).toFixed(1);
    return v;
}

function createGamesRelation(store, opts) {
    opts = opts || {};
    if (!opts.branch) throw new Error("[GamesRelation] opts.branch is required: the relation's own");
    var branch = opts.branch, owner = { toString: function () { return "GamesRelation"; } };
    branch.activate(owner);
    var menuHost = opts.menuHost || null;
    var onViewChanged = (typeof opts.onViewChanged === "function") ? opts.onViewChanged : null;
    var columns = store.columns(), cells = new Map(), headers = new Map(), cellSeq = 0;
    var conditions = gamesConditions(), view = null;         // the View is computed once per change, kept

    function valueOf(pk, col) { return store.get(pk, col); }
    function kindOf(col) { return store.kind(col); }
    function labelOf(col) { return _WB_GAMES_LABELS[col] || col; }
    function current() {
        if (!view) view = gamesApply(conditions, store.pks(), valueOf, kindOf);
        return view;
    }
    /** The values of a column with their counts, among the rows every OTHER filter passes. */
    function values(col) {
        var others = gamesSetFilter(conditions, col, null);
        var pks = Object.keys(others.filters).length ? gamesApply({ sort: [], filters: others.filters }, store.pks(), valueOf, kindOf) : store.pks();
        var counts = new Map();
        for (var i = 0; i < pks.length; i++) { var v = valueOf(pks[i], col); counts.set(v, (counts.get(v) || 0) + 1); }
        var out = [];
        counts.forEach(function (n, v) { out.push({ value: v, count: n }); });
        var kind = kindOf(col);
        out.sort(function (a, b) {
            var aa = a.value === null || a.value === "", bb = b.value === null || b.value === "";
            if (aa !== bb) return aa ? 1 : -1;
            return kind === "number" ? a.value - b.value : String(a.value).localeCompare(String(b.value), undefined, { sensitivity: "base" });
        });
        return out;
    }
    /** The conditions changed: a new View, every header repainted, the owner told. */
    function changed(next) {
        conditions = next;
        view = null;
        headers.forEach(function (h) { h.paint(); });
        if (onViewChanged) {
            try { onViewChanged(); }
            catch (e) { console.error("[GamesRelation] onViewChanged threw:", e); }
        }
    }

    // What a header cell and its menu may ask and do — the relation as their OWNER.
    var headerOwner = {
        label:      labelOf,
        kind:       kindOf,
        sortOf:     function (col) { return gamesSortOf(conditions, col); },
        sortCount:  function () { return conditions.sort.length; },
        filterOf:   function (col) { return conditions.filters[col] || null; },
        values:     values,
        setSort:    function (col, dir, additive) { changed(gamesSetSort(conditions, col, dir, additive)); },
        toggleSort: function (col, additive) { changed(gamesToggleSort(conditions, col, additive)); },
        setFilter:  function (col, spec) { changed(gamesSetFilter(conditions, col, spec)); }
    };

    return {
        view:    function (intent) { return intent ? null : current().slice(); },
        columns: function () { return columns.slice(); },
        labels:  function () { var out = {}; for (var k = 0; k < columns.length; k++) out[columns[k]] = labelOf(columns[k]); return out; },
        readOnlyColumns: function () { return columns.slice(); },
        cellFor: function (pk, col) {
            if (store.get(pk, col) === undefined) throw new Error("[GamesRelation] no such game: " + pk);
            if (columns.indexOf(col) < 0) throw new Error("[GamesRelation] no such column: " + col);
            var key = pk + " " + col, c = cells.get(key);
            if (!c) {
                c = new RelGridTextCell({ branch: branch.createBranch("c" + (++cellSeq)), value: _wbGamesShown(col, store.get(pk, col)) });
                cells.set(key, c);
            }
            return c;
        },
        headerFor: function (col) {
            if (columns.indexOf(col) < 0) throw new Error("[GamesRelation] no such column: " + col);
            var h = headers.get(col);
            if (!h) {
                h = new GamesHeaderCell({ branch: branch.createBranch("h-" + col), column: col, owner: headerOwner, menuHost: menuHost });
                headers.set(col, h);
            }
            return h;
        },
        conditions:    function () { return gamesConditionsCopy(conditions); },
        setConditions: function (c) { changed(gamesConditionsCopy(c || gamesConditions())); },
        setSort:       headerOwner.setSort,
        toggleSort:    headerOwner.toggleSort,
        setFilter:     headerOwner.setFilter,
        values:        values,
        clear:         function () { changed(gamesConditions()); },
        describe:      function () { return gamesDescribe(conditions, labelOf); },
        count:         function () { return current().length; },
        cellCount:     function () { return cells.size; },
        headerCount:   function () { return headers.size; },
        dispose: function () {
            headers.forEach(function (h) { h.dispose(); });
            headers.clear();
            cells.forEach(function (c) { c.dispose(); });
            cells.clear();
            branch.dissolve();
        }
    };
}
