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
//   new GamesRelation(store, { branch, menuHost, onViewChanged })
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


class GamesRelation {
    constructor(store, opts) {
        var o = opts || {}, self = this;
        if (!o.branch) throw new Error("[GamesRelation] opts.branch is required: the relation's own");
        this._store = store;
        this._branch = o.branch;
        this._branch.activate({ toString: function () { return "GamesRelation"; } });
        this._menuHost = o.menuHost || null;
        this._onViewChanged = (typeof o.onViewChanged === "function") ? o.onViewChanged : null;
        this._columns = store.columns();
        this._cells = new Map();
        this._headers = new Map();
        this._cellSeq = 0;
        this._conditions = GamesConditions.empty();
        this._view = null;                               // the View is computed once per change, kept
        // What a header cell and its menu may ask and do — the relation as their OWNER.
        this._headerOwner = {
            label:      function (col) { return self._labelOf(col); },
            kind:       function (col) { return self._kindOf(col); },
            sortOf:     function (col) { return GamesConditions.sortOf(self._conditions, col); },
            sortCount:  function () { return self._conditions.sort.length; },
            filterOf:   function (col) { return self._conditions.filters[col] || null; },
            values:     function (col) { return self.values(col); },
            setSort:    function (col, dir, additive) { self.setSort(col, dir, additive); },
            toggleSort: function (col, additive) { self.toggleSort(col, additive); },
            setFilter:  function (col, spec) { self.setFilter(col, spec); }
        };
    }

    /** A value as the catalogue shows it: a number as it is, sales to one place, absence as nothing. */
    static shown(col, v) {
        if (v === null || v === undefined) return "";
        if (col === "sales") return Number(v).toFixed(1);
        return v;
    }

    view(intent) { return intent ? null : this._current().slice(); }

    columns() { return this._columns.slice(); }

    labels() {
        var out = {};
        for (var k = 0; k < this._columns.length; k++) out[this._columns[k]] = this._labelOf(this._columns[k]);
        return out;
    }

    readOnlyColumns() { return this._columns.slice(); }

    cellFor(pk, col) {
        if (this._store.get(pk, col) === undefined) throw new Error("[GamesRelation] no such game: " + pk);
        if (this._columns.indexOf(col) < 0) throw new Error("[GamesRelation] no such column: " + col);
        var key = pk + " " + col, c = this._cells.get(key);
        if (!c) {
            c = new RelGridTextCell({ branch: this._branch.createBranch("c" + (++this._cellSeq)), value: GamesRelation.shown(col, this._store.get(pk, col)) });
            this._cells.set(key, c);
        }
        return c;
    }

    headerFor(col) {
        if (this._columns.indexOf(col) < 0) throw new Error("[GamesRelation] no such column: " + col);
        var h = this._headers.get(col);
        if (!h) {
            h = new GamesHeaderCell({ branch: this._branch.createBranch("h-" + col), column: col, owner: this._headerOwner, menuHost: this._menuHost });
            this._headers.set(col, h);
        }
        return h;
    }

    conditions()     { return GamesConditions.copy(this._conditions); }
    setConditions(c) { this._changed(GamesConditions.copy(c || GamesConditions.empty())); }
    setSort(col, dir, additive) { this._changed(GamesConditions.setSort(this._conditions, col, dir, additive)); }
    toggleSort(col, additive)   { this._changed(GamesConditions.toggleSort(this._conditions, col, additive)); }
    setFilter(col, spec)        { this._changed(GamesConditions.setFilter(this._conditions, col, spec)); }
    clear()          { this._changed(GamesConditions.empty()); }
    describe()       { return GamesConditions.describe(this._conditions, this._labelOf.bind(this)); }
    count()          { return this._current().length; }
    cellCount()      { return this._cells.size; }
    headerCount()    { return this._headers.size; }

    /** The values of a column with their counts, among the rows every OTHER filter passes. */
    values(col) {
        var store = this._store, valueOf = this._valueOf.bind(this), kindOf = this._kindOf.bind(this);
        var others = GamesConditions.setFilter(this._conditions, col, null);
        var pks = Object.keys(others.filters).length ? GamesConditions.apply({ sort: [], filters: others.filters }, store.pks(), valueOf, kindOf) : store.pks();
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

    dispose() {
        this._headers.forEach(function (h) { h.dispose(); });
        this._headers.clear();
        this._cells.forEach(function (c) { c.dispose(); });
        this._cells.clear();
        this._branch.dissolve();
    }

    _valueOf(pk, col) { return this._store.get(pk, col); }
    _kindOf(col) { return this._store.kind(col); }
    _labelOf(col) { return _WB_GAMES_LABELS[col] || col; }

    _current() {
        if (!this._view) this._view = GamesConditions.apply(this._conditions, this._store.pks(), this._valueOf.bind(this), this._kindOf.bind(this));
        return this._view;
    }

    /** The conditions changed: a new View, every header repainted, the owner told. */
    _changed(next) {
        this._conditions = next;
        this._view = null;
        this._headers.forEach(function (h) { h.paint(); });
        if (this._onViewChanged) {
            try { this._onViewChanged(); }
            catch (e) { console.error("[GamesRelation] onViewChanged threw:", e); }
        }
    }
}
