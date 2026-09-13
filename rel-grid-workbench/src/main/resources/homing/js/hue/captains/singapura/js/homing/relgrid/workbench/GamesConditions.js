// =============================================================================
// GamesConditions — the ORDER and the CHOICE of rows, as data. PURE LOGIC: no
// DOM anywhere; conditions in, keys out. This is what a domain holds when it
// sorts and filters for itself — the state a header cell's caret and funnel
// change, and the function that turns it and the store's values into a View.
//
//   gamesConditions()                                  the empty conditions: no sort, no filter
//   gamesToggleSort(c, column, additive)               a caret click: none → asc → desc → none. Not additive,
//                                                      the column becomes the ONLY key; additive (shift), it joins
//                                                      the keys after the ones there, or cycles where it already is
//   gamesSetFilter(c, column, spec)                    a filter on a column, or null to lift it:
//                                                        text    { contains: 'x' }          case-insensitive
//                                                        number  { min: 1998, max: 2004 }   either end open
//                                                        set     { in: ['PS4', 'PC'] }      any of
//   gamesApply(c, pks, valueOf, kindOf)                the View: the pks that pass every filter, in the
//                                                      keys' order, ties in base order (a stable sort)
//   gamesSortOf(c, column)                             { dir: 'asc' | 'desc', index } or null — what a caret shows
//   gamesDescribe(c, labelOf)                          the conditions as a sentence, for a status line
//   gamesConditionsCopy(c)                             a copy, for a domain that keeps its own
//
// Every function answers a NEW conditions value; none changes its argument.
//
// THE JUDGEMENTS, stated because they are judgements: a number compares as a
// number and text by locale, case aside; an ABSENT value sorts LAST in either
// direction — a missing score is not a low score, and a table that put the
// unrated first when sorting descending would be lying about them; a filter
// on a column with no value never passes it. Episode 2 warned these are
// choices a framework makes on the desk's behalf; here the desk makes them.
// =============================================================================

function gamesConditions() { return { sort: [], filters: {} }; }

function gamesConditionsCopy(c) {
    var filters = {};
    for (var k in c.filters) if (Object.prototype.hasOwnProperty.call(c.filters, k)) filters[k] = c.filters[k];
    return { sort: c.sort.map(function (s) { return { column: s.column, dir: s.dir }; }), filters: filters };
}

function gamesToggleSort(c, column, additive) {
    var next = gamesConditionsCopy(c), at = -1;
    for (var k = 0; k < next.sort.length; k++) if (next.sort[k].column === column) at = k;
    var was = at < 0 ? null : next.sort[at].dir;
    var dir = was === null ? "asc" : was === "asc" ? "desc" : null;
    if (!additive) next.sort = [];
    else if (at >= 0) next.sort.splice(at, 1);
    if (dir) {
        if (additive && at >= 0) next.sort.splice(at, 0, { column: column, dir: dir });
        else next.sort.push({ column: column, dir: dir });
    }
    return next;
}

function gamesSetFilter(c, column, spec) {
    var next = gamesConditionsCopy(c);
    if (spec == null) delete next.filters[column];
    else next.filters[column] = spec;
    return next;
}

function gamesSortOf(c, column) {
    for (var k = 0; k < c.sort.length; k++) if (c.sort[k].column === column) return { dir: c.sort[k].dir, index: k };
    return null;
}

/** Does one value pass one filter? Absence never passes. */
function _wbGcPasses(v, spec, kind) {
    if (v === null || v === undefined || v === "") return false;
    if (kind === "number") {
        if (spec.min != null && v < spec.min) return false;
        if (spec.max != null && v > spec.max) return false;
        return true;
    }
    if (kind === "set") return Array.isArray(spec.in) && spec.in.indexOf(v) >= 0;
    var needle = String(spec.contains == null ? "" : spec.contains).toLowerCase();
    return needle === "" || String(v).toLowerCase().indexOf(needle) >= 0;
}

/** -1, 0, 1 for one key: absent last either way, numbers as numbers, text by locale. */
function _wbGcCompare(a, b, kind, dir) {
    var aa = (a === null || a === undefined || a === ""), bb = (b === null || b === undefined || b === "");
    if (aa && bb) return 0;
    if (aa) return 1;
    if (bb) return -1;
    var r = (kind === "number") ? (a - b) : String(a).localeCompare(String(b), undefined, { sensitivity: "base" });
    return dir === "desc" ? -r : r;
}

function gamesApply(c, pks, valueOf, kindOf) {
    var out = [], k, column;
    for (var i = 0; i < pks.length; i++) {
        var pk = pks[i], ok = true;
        for (column in c.filters) {
            if (!Object.prototype.hasOwnProperty.call(c.filters, column)) continue;
            if (!_wbGcPasses(valueOf(pk, column), c.filters[column], kindOf(column))) { ok = false; break; }
        }
        if (ok) out.push({ pk: pk, at: i });
    }
    if (c.sort.length) {
        out.sort(function (x, y) {
            for (k = 0; k < c.sort.length; k++) {
                column = c.sort[k].column;
                var r = _wbGcCompare(valueOf(x.pk, column), valueOf(y.pk, column), kindOf(column), c.sort[k].dir);
                if (r) return r;
            }
            return x.at - y.at;                                   // ties in base order
        });
    }
    return out.map(function (e) { return e.pk; });
}

function gamesDescribe(c, labelOf) {
    var parts = [], name = function (col) { return labelOf ? labelOf(col) : col; };
    if (c.sort.length) parts.push("sorted by " + c.sort.map(function (s) { return name(s.column) + (s.dir === "desc" ? " ↓" : " ↑"); }).join(", then "));
    var fs = [];
    for (var col in c.filters) {
        if (!Object.prototype.hasOwnProperty.call(c.filters, col)) continue;
        var f = c.filters[col];
        if (f.in) fs.push(name(col) + " in {" + f.in.join(", ") + "}");
        else if (f.contains != null) fs.push(name(col) + " contains “" + f.contains + "”");
        else fs.push(name(col) + " " + (f.min != null ? f.min : "") + "–" + (f.max != null ? f.max : ""));
    }
    if (fs.length) parts.push("where " + fs.join(" and "));
    return parts.length ? parts.join("; ") : "as catalogued";
}
