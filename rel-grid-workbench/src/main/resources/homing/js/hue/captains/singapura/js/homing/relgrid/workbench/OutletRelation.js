// =============================================================================
// OutletRelation — one outlet's book as a root relation over the SalesStore:
// identities, columns, and a cell manager, plus the FENCES the Outlets bench
// puts between its tables. DOMAIN CODE; the words "grid" and "group" do not
// appear in it, and nothing in it could tell either anything.
//
//   createOutletRelation(store, outletId)
//     · pks() are the dishes; columns() are the ledger's four; every column is
//       declared read-only — sales are the only thing that move a book;
//     · cellFor(pk, col) builds a text cell ONCE per identity and keeps it,
//       showing the ledger's value formatted for reading;
//     · it subscribes to the store and set()s its own cells when this outlet's
//       values change — a sale at another outlet is not this book's business,
//       and the table that placed these cells is never told.
//
//   createOutletFence(store, outletId)     what goes in the slot ABOVE an outlet's table:
//                                          its name, and its published totals — kept
//                                          current by the store, the way a cell is
//   createLedgerFence(store)               the slot below the last: every outlet together
//
// A fence is a domain object handed a host, exactly as a cell is; what it
// draws, and whether it carries a control, is decided here and nowhere else.
// =============================================================================

var _WB_FENCE_STYLE_ID = "bench-fence-style";
var _WB_FENCE_CSS = [
    ".wb-fence{display:flex;align-items:baseline;gap:12px;padding:10px 10px 4px;font:12px sans-serif;",
    "  color:var(--color-text-primary);}",
    ".wb-fence-name{font-size:14px;font-weight:600;}",
    ".wb-fence-totals{color:var(--color-text-muted);font-size:11px;font-variant-numeric:tabular-nums;}",
    ".wb-fence.wb-fence-ledger{border-top:2px solid var(--color-border);margin-top:6px;padding-top:8px;}"
].join("\n");

function _wbFenceEnsureStyle() {
    if (typeof document === "undefined" || !document.head) return;
    if (document.getElementById && document.getElementById(_WB_FENCE_STYLE_ID)) return;
    var s = document.createElement("style");
    s.id = _WB_FENCE_STYLE_ID;
    s.textContent = _WB_FENCE_CSS;
    document.head.appendChild(s);
}

function _wbMoney(n) { return (Math.round(n * 100) / 100).toFixed(2); }
function _wbOutletShown(col, v) {
    if (v === undefined || v === null) return "";
    return (col === "revenue") ? _wbMoney(v) : String(v);
}
function _wbTotalsLine(t) { return t ? t.sold + " sold · " + _wbMoney(t.revenue) + " taken" : ""; }
function _wbOutletName(store, id) {
    var os = store.outlets();
    for (var k = 0; k < os.length; k++) if (os[k].id === id) return os[k].name;
    return id;
}

function createOutletRelation(store, outletId) {
    if (!_wbOutletName(store, outletId) || !store.totals(outletId)) throw new Error("[OutletRelation] unknown outlet: " + outletId);
    var cells = new Map();
    var unsubscribe = store.subscribe(function (outlet, pk, col, v) {
        if (outlet !== outletId || pk === null) return;
        var c = cells.get(pk + " " + col);
        if (c) c.set(_wbOutletShown(col, v));
    });
    return {
        pks:     function () { return store.dishes(); },
        columns: function () { return store.columns(); },
        // A book is read, never edited: every column is a constraint, so the
        // table never asks any cell here whether it may take control.
        readOnlyColumns: function () { return store.columns(); },
        cellFor: function (pk, col) {
            var k = pk + " " + col, c = cells.get(k);
            if (!c) {
                c = new RelGridTextCell({ value: _wbOutletShown(col, store.get(outletId, pk, col)) });
                cells.set(k, c);
            }
            return c;
        },
        outlet:    function () { return outletId; },
        name:      function () { return _wbOutletName(store, outletId); },
        totals:    function () { return store.totals(outletId); },
        cellCount: function () { return cells.size; },
        dispose: function () {
            unsubscribe();
            cells.forEach(function (c) { if (typeof c.dispose === "function") c.dispose(); });
            cells.clear();
        }
    };
}

/** The slot above an outlet's table: its name, and its totals kept current. */
function createOutletFence(store, outletId) {
    var name = _wbOutletName(store, outletId), totals = null, unsubscribe = null;
    return {
        render: function (host) {
            _wbFenceEnsureStyle();
            var root = document.createElement("div");
            root.className = "wb-fence";
            var n = document.createElement("span");
            n.className = "wb-fence-name";
            n.textContent = name;
            totals = document.createElement("span");
            totals.className = "wb-fence-totals";
            totals.textContent = _wbTotalsLine(store.totals(outletId));
            root.appendChild(n); root.appendChild(totals);
            host.appendChild(root);
            unsubscribe = store.subscribe(function (outlet, pk, col, v) {
                if (outlet === outletId && pk === null && col === "totals") totals.textContent = _wbTotalsLine(v);
            });
        },
        dispose: function () { if (unsubscribe) unsubscribe(); unsubscribe = null; }
    };
}

/** The slot below the last table: the ledger — every outlet together. */
function createLedgerFence(store) {
    var totals = null, unsubscribe = null;
    return {
        render: function (host) {
            _wbFenceEnsureStyle();
            var root = document.createElement("div");
            root.className = "wb-fence wb-fence-ledger";
            var n = document.createElement("span");
            n.className = "wb-fence-name";
            n.textContent = "All outlets";
            totals = document.createElement("span");
            totals.className = "wb-fence-totals";
            totals.textContent = _wbTotalsLine(store.totals(null));
            root.appendChild(n); root.appendChild(totals);
            host.appendChild(root);
            unsubscribe = store.subscribe(function (outlet, pk, col) {
                if (pk === null && col === "totals") totals.textContent = _wbTotalsLine(store.totals(null));
            });
        },
        dispose: function () { if (unsubscribe) unsubscribe(); unsubscribe = null; }
    };
}
