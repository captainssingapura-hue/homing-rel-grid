// =============================================================================
// EndlessRelation — a root relation over an identity space too large to list:
// N rows 'r0'…'r{N-1}' whose values are a function of the identity, made up
// when asked, and a WINDOW of W of them answered as the View. DOMAIN CODE;
// the word "grid" does not appear in it.
//
//   createEndlessRelation({ branch, rows?, window?, columns? })
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

/** A value from an identity and a column, deterministic: the same row reads the same twice. */
function _wbEndlessValue(k, col) {
    if (col === "id")    return k;
    if (col === "name")  return _WB_ENDLESS_NAMES[k % _WB_ENDLESS_NAMES.length] + " " + (k % 97);
    if (col === "qty")   return (k * 7919) % 1000;
    if (col === "price") return (((k * 31) % 9000) + 100) / 100;
    if (col === "note")  return _WB_ENDLESS_NOTES[(k * 13) % _WB_ENDLESS_NOTES.length];
    return "";
}

function createEndlessRelation(opts) {
    opts = opts || {};
    if (!opts.branch) throw new Error("[EndlessRelation] opts.branch is required: the relation's own");
    var branch = opts.branch, owner = { toString: function () { return "EndlessRelation"; } };
    branch.activate(owner);                        // its own: unactivated when handed; a row's sub-branch is its own too
    var N = (opts.rows > 0) ? Math.floor(opts.rows) : 1000000;
    var W = Math.min(N, (opts.window > 0) ? Math.floor(opts.window) : 20);
    var columns = (opts.columns || ["id", "name", "qty", "price", "note"]).slice();
    var at = 0;                                    // where the window stands: the View answered last begins here
    var held = new Map();                          // k → { branch, cells: Map(col → cell) }
    var edits = new Map();                         // "k col" → what was committed: the domain's, outliving the cell
    var mints = 0, frees = 0;

    function keys(from) {
        var out = [];
        for (var k = from; k < from + W; k++) out.push("r" + k);
        return out;
    }
    function indexOf(pk) {
        if (!/^r\d+$/.test(pk)) return -1;
        var k = Number(pk.slice(1));
        return (k < N) ? k : -1;
    }
    function shown(k, col) {
        var key = k + " " + col;
        return edits.has(key) ? edits.get(key) : _wbEndlessValue(k, col);
    }
    function free(k) {
        var row = held.get(k);
        if (!row) return;
        row.cells.forEach(function (c) { c.dispose(); });
        branch.dissolveBranch("row-" + k);
        held.delete(k);
        frees++;
    }
    /** The retention rule: a row in neither the View answered last nor the one being answered goes. */
    function retain(lastAt, nextAt) {
        var gone = [];
        held.forEach(function (row, k) {
            var inLast = k >= lastAt && k < lastAt + W, inNext = k >= nextAt && k < nextAt + W;
            if (!inLast && !inNext) gone.push(k);
        });
        for (var g = 0; g < gone.length; g++) free(gone[g]);
    }

    return {
        view: function (intent) {
            if (!intent) { retain(at, at); return keys(at); }
            var by = Math.trunc(Number(intent.by) || 0);
            var next = Math.max(0, Math.min(N - W, at + by));
            if (next === at) return null;                   // an end, or no movement: the rows stay
            retain(at, next);
            at = next;
            return keys(at);
        },
        columns: function () { return columns.slice(); },
        readOnlyColumns: function () { return ["id"]; },
        cellFor: function (pk, col) {
            var k = indexOf(pk);
            if (k < 0) throw new Error("[EndlessRelation] no such row: " + pk);
            if (columns.indexOf(col) < 0) throw new Error("[EndlessRelation] no such column: " + col);
            var row = held.get(k);
            if (!row) {
                row = { branch: branch.createBranch("row-" + k), cells: new Map() };
                row.branch.activate(owner);
                held.set(k, row);
            }
            var cell = row.cells.get(col);
            if (!cell) {
                mints++;
                cell = new RelGridTextCell({
                    branch: row.branch.createBranch(col),
                    value: shown(k, col),
                    onCommit: (col === "id") ? undefined : function (text) {
                        edits.set(k + " " + col, text);
                        cell.set(text);
                    }
                });
                row.cells.set(col, cell);
            }
            return cell;
        },
        at:        function () { return at; },
        rows:      function () { return N; },
        window:    function () { return W; },
        held:      function () { return held.size; },
        heldRows:  function () { var out = []; held.forEach(function (row, k) { out.push(k); }); return out.sort(function (a, b) { return a - b; }); },
        cellCount: function () { var n = 0; held.forEach(function (row) { n += row.cells.size; }); return n; },
        mints:     function () { return mints; },
        frees:     function () { return frees; },
        edited:    function (pk, col) { var key = indexOf(pk) + " " + col; return edits.has(key) ? edits.get(key) : null; },
        dispose: function () {
            held.forEach(function (row) { row.cells.forEach(function (c) { c.dispose(); }); });
            held.clear();
            branch.dissolve();
        }
    };
}
