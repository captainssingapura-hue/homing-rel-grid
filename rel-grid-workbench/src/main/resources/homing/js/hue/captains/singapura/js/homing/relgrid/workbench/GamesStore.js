// =============================================================================
// GamesStore — the Games Catalogue bench's store: seven hundred-odd releases,
// 1990 to 2020, read from the catalogue's pipe-separated text. DOMAIN CODE;
// the word "grid" does not appear in it. Read-only: the catalogue is a fact,
// and what this bench exercises is ordering and choosing, not editing.
//
//   createGamesStore(psv)          the catalogue: a header line, then a row a line, '|'-separated;
//                                  lines that start with # are remarks
//   store.pks()                    'g001', 'g002', … in the catalogue's order
//   store.columns()                the header's names, in order
//   store.kind(col)                'text' | 'number' | 'set' — how a column's values compare and are chosen
//   store.get(pk, col)             the value: a number for a number column, null where the
//                                  catalogue left it blank; a string otherwise
//   store.distinct(col)            a set column's values with their counts, most frequent first
//   store.size()
//
// Numbers are numbers here — year, sales, score — so a sort compares them as
// numbers and a filter ranges over them; a blank is null, deliberately, so a
// sort has to say what it does with absence rather than sorting '' as text.
// =============================================================================

var _WB_GAMES_KINDS = { year: "number", sales: "number", score: "number", platform: "set", type: "set" };

function createGamesStore(psv) {
    var lines = String(psv || "").split(/\r?\n/), columns = null, rows = new Map(), pks = [];
    for (var k = 0; k < lines.length; k++) {
        var line = lines[k];
        if (!line || line.charAt(0) === "#") continue;
        var parts = line.split("|");
        if (!columns) { columns = parts.slice(1); continue; }             // the header: 'id' and then the columns
        var pk = parts[0], row = {};
        for (var c = 0; c < columns.length; c++) {
            var raw = parts[c + 1] === undefined ? "" : parts[c + 1];
            row[columns[c]] = (_WB_GAMES_KINDS[columns[c]] === "number") ? (raw === "" ? null : Number(raw)) : raw;
        }
        rows.set(pk, row);
        pks.push(pk);
    }
    columns = columns || [];
    var distinct = {};

    return {
        pks:     function () { return pks.slice(); },
        columns: function () { return columns.slice(); },
        kind:    function (col) { return _WB_GAMES_KINDS[col] || "text"; },
        get:     function (pk, col) { var r = rows.get(pk); return r ? r[col] : undefined; },
        size:    function () { return pks.length; },
        distinct: function (col) {
            if (!distinct[col]) {
                var counts = new Map();
                for (var i = 0; i < pks.length; i++) { var v = rows.get(pks[i])[col]; counts.set(v, (counts.get(v) || 0) + 1); }
                var out = [];
                counts.forEach(function (n, v) { out.push({ value: v, count: n }); });
                out.sort(function (a, b) { return b.count - a.count || String(a.value).localeCompare(String(b.value)); });
                distinct[col] = out;
            }
            return distinct[col].slice();
        }
    };
}
