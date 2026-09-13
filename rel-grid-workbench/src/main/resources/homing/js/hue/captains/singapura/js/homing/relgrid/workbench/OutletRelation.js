// =============================================================================
// OutletRelation — one outlet's book as a root relation over the SalesStore:
// identities, columns, and a cell manager, plus the FENCES the Outlets bench
// puts between its tables. DOMAIN CODE; the words "grid" and "group" do not
// appear in it, and nothing in it could tell either anything.
//
//   createOutletRelation(store, outletId, { branch })
//     · branch is the relation's OWN — unactivated when handed; it activates, and
//       dispose() dissolves — and every cell is given a sub-branch of it to own;
//     · pks() are the dishes; columns() are the ledger's four; every column is
//       declared read-only — sales are the only thing that move a book;
//     · cellFor(pk, col) builds a text cell ONCE per identity and keeps it,
//       showing the ledger's value formatted for reading;
//     · it subscribes to the store and set()s its own cells when this outlet's
//       values change — a sale at another outlet is not this book's business,
//       and the table that placed these cells is never told.
//
//   createOutletFence(store, outletId, { branch, tell, folded? })
//                                          what goes in the slot ABOVE an outlet's table:
//                                          a FOLD TOGGLE, its name, and its published
//                                          totals — kept current by the store, the way
//                                          a cell is
//   createLedgerFence(store, { branch })   the slot below the last: every outlet together
//
// A fence is a NOUN, exactly as a cell is: it owns its element, minted on the
// branch it was handed, and answers fenceElement() once to whoever places
// it; what it draws, and whether it carries a control, is decided here and
// nowhere else. The toggle is the first control that TELLS: pressed, it sends
// a RelGridGroupFold down the tell closure the HOST built the fence with —
// the channel's other direction, an answer nobody asked for — and whatever
// is at the other end folds the member below. The fence is told onFolded(on)
// whenever that member folds by any road, and draws its toggle from what it
// was last told — folded at construction, then every onFolded — never from
// what it did itself.
// =============================================================================

var _WB_FENCE_STYLE_ID = "bench-fence-style";
var _WB_FENCE_CSS = [
    ".wb-fence{display:flex;align-items:baseline;gap:12px;padding:10px 10px 4px;font:12px sans-serif;",
    "  color:var(--color-text-primary);}",
    ".wb-fence-name{font-size:14px;font-weight:600;}",
    // The toggle: a triangle, down while the book shows and right while it is folded.
    ".wb-fence-fold{border:0;background:transparent;cursor:pointer;padding:0 4px;margin:0;",
    "  font:inherit;font-size:11px;line-height:1;color:var(--color-text-muted);width:1.4em;text-align:center;}",
    ".wb-fence-fold:hover{color:var(--color-accent);}",
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

function createOutletRelation(store, outletId, opts) {
    opts = opts || {};
    if (!_wbOutletName(store, outletId) || !store.totals(outletId)) throw new Error("[OutletRelation] unknown outlet: " + outletId);
    if (!opts.branch) throw new Error("[OutletRelation] opts.branch is required: the relation's own");
    var branch = opts.branch, cellSeq = 0;
    branch.activate({ toString: function () { return "OutletRelation " + outletId; } });   // its own: unactivated when handed
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
                c = new RelGridTextCell({ branch: branch.createBranch("c" + (++cellSeq)),
                                          value: _wbOutletShown(col, store.get(outletId, pk, col)) });
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
            branch.dissolve();
        }
    };
}

/**
 * The slot above an outlet's table: a fold toggle, its name, and its totals
 * kept current. tell(message) is the host's closure — pressed, the toggle
 * sends a RelGridGroupFold down it; a fence built without one has an inert
 * toggle. folded is what the fence is told it is at first (false by default).
 */
function createOutletFence(store, outletId, opts) {
    opts = opts || {};
    if (!opts.branch) throw new Error("[OutletFence] opts.branch is required: the fence's own");
    var b = opts.branch, tell = (typeof opts.tell === "function") ? opts.tell : null;
    var name = _wbOutletName(store, outletId), state = opts.folded === true;
    var root = null, totals = null, toggle = null, unsubscribe = null;
    var owner = { toString: function () { return "OutletFence " + outletId; } };
    b.activate(owner);
    function paint() {
        if (!toggle) return;
        toggle.textContent = state ? "\u25B8" : "\u25BE";                  // ▸ folded · ▾ shown
        toggle.setAttribute("aria-expanded", state ? "false" : "true");
        toggle.setAttribute("aria-label", (state ? "Unfold " : "Fold ") + name);
    }
    return {
        /** The fence's element, minted once on its branch; whoever holds the slot places it. */
        fenceElement: function () {
            if (root) return root;
            _wbFenceEnsureStyle();
            root = b.createElement("fence", "div");
            root.className = "wb-fence";
            toggle = b.createElement("fold", "button");
            toggle.className = "wb-fence-fold";
            toggle.type = "button";
            toggle.addEventListener("click", function () {
                // Pressed: TELL, unasked, the other way round from what this fence was
                // last told it is; what it becomes comes back as onFolded.
                if (tell) tell(new RelGridGroupFold(outletId, !state));
            });
            paint();
            var n = b.createElement("name", "span");
            n.className = "wb-fence-name";
            n.textContent = name;
            totals = b.createElement("totals", "span");
            totals.className = "wb-fence-totals";
            totals.textContent = _wbTotalsLine(store.totals(outletId));
            root.appendChild(toggle); root.appendChild(n); root.appendChild(totals);
            unsubscribe = store.subscribe(function (outlet, pk, col, v) {
                if (outlet === outletId && pk === null && col === "totals") totals.textContent = _wbTotalsLine(v);
            });
            return root;
        },
        /** The member below folded or unfolded — by this toggle, the host's verb, or a fold-all. */
        onFolded: function (folded) { state = folded === true; paint(); },
        folded: function () { return state; },
        dispose: function () { if (unsubscribe) unsubscribe(); unsubscribe = null; root = null; toggle = null; b.dissolve(); }
    };
}

/** The slot below the last table: the ledger — every outlet together. */
function createLedgerFence(store, opts) {
    opts = opts || {};
    if (!opts.branch) throw new Error("[LedgerFence] opts.branch is required: the fence's own");
    var b = opts.branch, root = null, totals = null, unsubscribe = null;
    var owner = { toString: function () { return "LedgerFence"; } };
    b.activate(owner);
    return {
        fenceElement: function () {
            if (root) return root;
            _wbFenceEnsureStyle();
            root = b.createElement("fence", "div");
            root.className = "wb-fence wb-fence-ledger";
            var n = b.createElement("name", "span");
            n.className = "wb-fence-name";
            n.textContent = "All outlets";
            totals = b.createElement("totals", "span");
            totals.className = "wb-fence-totals";
            totals.textContent = _wbTotalsLine(store.totals(null));
            root.appendChild(n); root.appendChild(totals);
            unsubscribe = store.subscribe(function (outlet, pk, col) {
                if (pk === null && col === "totals") totals.textContent = _wbTotalsLine(store.totals(null));
            });
            return root;
        },
        dispose: function () { if (unsubscribe) unsubscribe(); unsubscribe = null; root = null; b.dissolve(); }
    };
}
