// =============================================================================
// EndlessRelation — a root relation over an identity space too large to list:
// N rows 'r0'…'r{N-1}' whose values are a function of the identity, made up
// when asked, and a WINDOW of W of them answered as the View. DOMAIN CODE;
// the word "grid" does not appear in it.
//
//   new EndlessRelation({ branch, rows?, window?, columns? })
//
//   · branch is the relation's OWN — unactivated when handed; it activates,
//     and dispose() dissolves — with a sub-branch per ROW held, and a cell's
//     own sub-branch under that. Whoever arranges never sees it.
//   · view() answers the window: W rows from where it stands. view({ by: n })
//     moves it n rows, clamped to the space, and answers NOTHING at an end —
//     the rows stay. ONE ROOT: nothing here lists the space, and N may be a
//     million; the relation weighs nothing until a row is asked for.
//   · cellFor(pk, col) mints a text cell once per identity and keeps it while
//     the row is within TWO Views: the one answered last and the one being
//     answered. A row in neither is FREED — its cells disposed, its branch
//     dissolved, its identity forgotten here. Whoever arranges a View does so
//     before asking for the next, so a row in neither is placed nowhere; that
//     is the whole of the retention rule, and it bounds what this relation
//     holds at 2W rows however far the window travels. A stranger — an
//     identity outside the space — is refused at cellFor.
//   · Every column but id is editable: a commit is kept by identity, so an
//     edit survives its row being freed and asked for again — the value is
//     the domain's, and the cell was only ever showing it.
//   · at() / rows() / window() / held() / cellCount() / mints() / frees() —
//     the gauges a bench reads; none of them is anything an arranger asks.
// =============================================================================

var _WB_ENDLESS_NAMES = ["Anise", "Basil", "Caraway", "Dill", "Fennel", "Ginger", "Hyssop", "Juniper",
                         "Lovage", "Mace", "Nutmeg", "Oregano", "Paprika", "Rue", "Saffron", "Tarragon"];
var _WB_ENDLESS_NOTES = ["", "on order", "seasonal", "house blend", "", "discontinued", "from the garden", ""];

class EndlessRelation {
    constructor(opts) {
        var o = opts || {};
        if (!o.branch) throw new Error("[EndlessRelation] opts.branch is required: the relation's own");
        this._branch = o.branch;
        this._owner = { toString: function () { return "EndlessRelation"; } };
        this._branch.activate(this._owner);           // its own: unactivated when handed; a row's sub-branch is its own too
        this._N = (o.rows > 0) ? Math.floor(o.rows) : 1000000;
        this._W = Math.min(this._N, (o.window > 0) ? Math.floor(o.window) : 20);
        this._columns = (o.columns || ["id", "name", "qty", "price", "note"]).slice();
        this._at = 0;                                  // where the window stands: the View answered last begins here
        this._held = new Map();                        // k → { branch, cells: Map(col → cell) }
        this._edits = new Map();                       // "k col" → what was committed: the domain's, outliving the cell
        this._mints = 0;
        this._frees = 0;
    }

    /** A value from an identity and a column, deterministic: the same row reads the same twice. */
    static value(k, col) {
        if (col === "id")    return k;
        if (col === "name")  return _WB_ENDLESS_NAMES[k % _WB_ENDLESS_NAMES.length] + " " + (k % 97);
        if (col === "qty")   return (k * 7919) % 1000;
        if (col === "price") return (((k * 31) % 9000) + 100) / 100;
        if (col === "note")  return _WB_ENDLESS_NOTES[(k * 13) % _WB_ENDLESS_NOTES.length];
        return "";
    }

    view(intent) {
        if (!intent) { this._retain(this._at, this._at); return this._keys(this._at); }
        var by = Math.trunc(Number(intent.by) || 0);
        var next = Math.max(0, Math.min(this._N - this._W, this._at + by));
        if (next === this._at) return null;                 // an end, or no movement: the rows stay
        this._retain(this._at, next);
        this._at = next;
        return this._keys(this._at);
    }

    columns() { return this._columns.slice(); }

    readOnlyColumns() { return ["id"]; }

    cellFor(pk, col) {
        var k = this._indexOf(pk), self = this;
        if (k < 0) throw new Error("[EndlessRelation] no such row: " + pk);
        if (this._columns.indexOf(col) < 0) throw new Error("[EndlessRelation] no such column: " + col);
        var row = this._held.get(k);
        if (!row) {
            row = { branch: this._branch.createBranch("row-" + k), cells: new Map() };
            row.branch.activate(this._owner);
            this._held.set(k, row);
        }
        var cell = row.cells.get(col);
        if (!cell) {
            this._mints++;
            cell = new RelGridTextCell({
                branch: row.branch.createBranch(col),
                value: this._shown(k, col),
                onCommit: (col === "id") ? undefined : function (text) {
                    self._edits.set(k + " " + col, text);
                    cell.set(text);
                }
            });
            row.cells.set(col, cell);
        }
        return cell;
    }

    at()        { return this._at; }
    rows()      { return this._N; }
    window()    { return this._W; }
    held()      { return this._held.size; }
    heldRows()  { var out = []; this._held.forEach(function (row, k) { out.push(k); }); return out.sort(function (a, b) { return a - b; }); }
    cellCount() { var n = 0; this._held.forEach(function (row) { n += row.cells.size; }); return n; }
    mints()     { return this._mints; }
    frees()     { return this._frees; }
    edited(pk, col) { var key = this._indexOf(pk) + " " + col; return this._edits.has(key) ? this._edits.get(key) : null; }

    dispose() {
        this._held.forEach(function (row) { row.cells.forEach(function (c) { c.dispose(); }); });
        this._held.clear();
        this._branch.dissolve();
    }

    _keys(from) {
        var out = [];
        for (var k = from; k < from + this._W; k++) out.push("r" + k);
        return out;
    }

    _indexOf(pk) {
        if (!/^r\d+$/.test(pk)) return -1;
        var k = Number(pk.slice(1));
        return (k < this._N) ? k : -1;
    }

    _shown(k, col) {
        var key = k + " " + col;
        return this._edits.has(key) ? this._edits.get(key) : EndlessRelation.value(k, col);
    }

    _free(k) {
        var row = this._held.get(k);
        if (!row) return;
        row.cells.forEach(function (c) { c.dispose(); });
        this._branch.dissolveBranch("row-" + k);
        this._held.delete(k);
        this._frees++;
    }

    /** The retention rule: a row in neither the View answered last nor the one being answered goes. */
    _retain(lastAt, nextAt) {
        var gone = [], W = this._W;
        this._held.forEach(function (row, k) {
            var inLast = k >= lastAt && k < lastAt + W, inNext = k >= nextAt && k < nextAt + W;
            if (!inLast && !inNext) gone.push(k);
        });
        for (var g = 0; g < gone.length; g++) this._free(gone[g]);
    }
}
