// =============================================================================
// OutletRelation — one outlet's book as a root relation over the SalesStore:
// identities, columns, and a cell manager, plus the FENCES the Outlets bench
// puts between its tables. DOMAIN CODE; the words "grid" and "group" do not
// appear in it, and nothing in it could tell either anything.
//
//   new OutletRelation(store, outletId, { branch })
//     · branch is the relation's OWN — unactivated when handed; it activates, and
//       dispose() dissolves — and every cell is given a sub-branch of it to own;
//     · view() answers the dishes — one root, and a book has nowhere to move;
//       columns() are the ledger's four; every column is
//       declared read-only — sales are the only thing that move a book;
//     · cellFor(pk, col) builds a text cell ONCE per identity and keeps it,
//       showing the ledger's value formatted for reading;
//     · it subscribes to the store and set()s its own cells when this outlet's
//       values change — a sale at another outlet is not this book's business,
//       and the table that placed these cells is never told.
//
//   new OutletFence(store, outletId, { branch, tell, folded? })
//                                          what goes in the slot ABOVE an outlet's table:
//                                          a FOLD TOGGLE, its name, and its published
//                                          totals — kept current by the store, the way
//                                          a cell is
//   new LedgerFence(store, { branch })   the slot below the last: every outlet together
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

// THE LOOKS ARE TYPED — OutletFenceStyles, applied through the css manager.

class OutletRelation {
    constructor(store, outletId, opts) {
        var o = opts || {}, self = this;
        if (!OutletRelation.nameOf(store, outletId) || !store.totals(outletId)) throw new Error("[OutletRelation] unknown outlet: " + outletId);
        if (!o.branch) throw new Error("[OutletRelation] opts.branch is required: the relation's own");
        this._store = store;
        this._outletId = outletId;
        this._branch = o.branch;
        this._cellSeq = 0;
        this._branch.activate({ toString: function () { return "OutletRelation " + outletId; } });   // its own: unactivated when handed
        this._cells = new Map();
        this._unsubscribe = store.subscribe(function (outlet, pk, col, v) {
            if (outlet !== outletId || pk === null) return;
            var c = self._cells.get(pk + " " + col);
            if (c) c.set(OutletRelation.shown(col, v));
        });
    }

    static money(n) { return (Math.round(n * 100) / 100).toFixed(2); }

    static shown(col, v) {
        if (v === undefined || v === null) return "";
        return (col === "revenue") ? OutletRelation.money(v) : String(v);
    }

    static totalsLine(t) { return t ? t.sold + " sold · " + OutletRelation.money(t.revenue) + " taken" : ""; }

    static nameOf(store, id) {
        var os = store.outlets();
        for (var k = 0; k < os.length; k++) if (os[k].id === id) return os[k].name;
        return id;
    }

    view(intent) { return intent ? null : this._store.dishes(); }   // one root: the whole book; a movement goes nowhere

    labels() { return { dish: "dish", sold: "sold", revenue: "revenue", lastSale: "last sale" }; }   // what a column is called: the relation's (law 86)

    columns() { return this._store.columns(); }

    // A book is read, never edited: every column is a constraint, so the
    // table never asks any cell here whether it may take control.
    readOnlyColumns() { return this._store.columns(); }

    cellFor(pk, col) {
        var store = this._store;
        if (store.dishes().indexOf(pk) < 0) throw new Error("[OutletRelation] no such dish: " + pk);   // a stranger is refused here
        var k = pk + " " + col, c = this._cells.get(k);
        if (!c) {
            c = new RelGridTextCell({ branch: this._branch.createBranch("c" + (++this._cellSeq)),
                                      value: OutletRelation.shown(col, store.get(this._outletId, pk, col)) });
            this._cells.set(k, c);
        }
        return c;
    }

    outlet()    { return this._outletId; }
    name()      { return OutletRelation.nameOf(this._store, this._outletId); }
    totals()    { return this._store.totals(this._outletId); }
    cellCount() { return this._cells.size; }

    dispose() {
        this._unsubscribe();
        this._cells.forEach(function (c) { if (typeof c.dispose === "function") c.dispose(); });
        this._cells.clear();
        this._branch.dissolve();
    }
}

/**
 * The slot above an outlet's table: a fold toggle, its name, and its totals
 * kept current. tell(message) is the host's closure — pressed, the toggle
 * sends a RelGridGroupFold down it; a fence built without one has an inert
 * toggle. folded is what the fence is told it is at first (false by default).
 */
class OutletFence {
    constructor(store, outletId, opts) {
        var o = opts || {};
        if (!o.branch) throw new Error("[OutletFence] opts.branch is required: the fence's own");
        this._store = store;
        this._outletId = outletId;
        this._b = o.branch;
        this._tell = (typeof o.tell === "function") ? o.tell : null;
        this._name = OutletRelation.nameOf(store, outletId);
        this._state = o.folded === true;
        this._root = null;
        this._toggle = null;
        this._unsubscribe = null;
        this._b.activate({ toString: function () { return "OutletFence " + outletId; } });
    }

    /** The fence's element, minted once on its branch; whoever holds the slot places it. */
    fenceElement() {
        if (this._root) return this._root;
        var b = this._b, self = this, outletId = this._outletId;
        var root = this._root = b.createElement("fence", "div");
        css.addClass(root, wb_fence);
        var toggle = this._toggle = b.createElement("fold", "button");
        css.addClass(toggle, wb_fence_fold, wb_fence_fold_hot);
        toggle.type = "button";
        toggle.addEventListener("click", function () {
            // Pressed: TELL, unasked, the other way round from what this fence was
            // last told it is; what it becomes comes back as onFolded.
            if (self._tell) self._tell(new RelGridGroupFold(outletId, !self._state));
        });
        this._paint();
        var n = b.createElement("name", "span");
        css.addClass(n, wb_fence_name);
        n.textContent = this._name;
        var totals = b.createElement("totals", "span");
        css.addClass(totals, wb_fence_totals);
        totals.textContent = OutletRelation.totalsLine(this._store.totals(outletId));
        root.appendChild(toggle); root.appendChild(n); root.appendChild(totals);
        this._unsubscribe = this._store.subscribe(function (outlet, pk, col, v) {
            if (outlet === outletId && pk === null && col === "totals") totals.textContent = OutletRelation.totalsLine(v);
        });
        return root;
    }

    /** The member below folded or unfolded — by this toggle, the host's verb, or a fold-all. */
    onFolded(folded) { this._state = folded === true; this._paint(); }

    folded() { return this._state; }

    dispose() {
        if (this._unsubscribe) this._unsubscribe();
        this._unsubscribe = null; this._root = null; this._toggle = null;
        this._b.dissolve();
    }

    _paint() {
        var toggle = this._toggle, state = this._state;
        if (!toggle) return;
        toggle.textContent = state ? "▸" : "▾";                  // folded, shown
        toggle.setAttribute("aria-expanded", state ? "false" : "true");
        toggle.setAttribute("aria-label", (state ? "Unfold " : "Fold ") + this._name);
    }
}

/** The slot below the last table: the ledger — every outlet together. */
class LedgerFence {
    constructor(store, opts) {
        var o = opts || {};
        if (!o.branch) throw new Error("[LedgerFence] opts.branch is required: the fence's own");
        this._store = store;
        this._b = o.branch;
        this._root = null;
        this._unsubscribe = null;
        this._b.activate({ toString: function () { return "LedgerFence"; } });
    }

    fenceElement() {
        if (this._root) return this._root;
        var b = this._b, store = this._store;
        var root = this._root = b.createElement("fence", "div");
        css.addClass(root, wb_fence, wb_fence_ledger);
        var n = b.createElement("name", "span");
        css.addClass(n, wb_fence_name);
        n.textContent = "All outlets";
        var totals = b.createElement("totals", "span");
        css.addClass(totals, wb_fence_totals);
        totals.textContent = OutletRelation.totalsLine(store.totals(null));
        root.appendChild(n); root.appendChild(totals);
        this._unsubscribe = store.subscribe(function (outlet, pk, col) {
            if (pk === null && col === "totals") totals.textContent = OutletRelation.totalsLine(store.totals(null));
        });
        return root;
    }

    dispose() {
        if (this._unsubscribe) this._unsubscribe();
        this._unsubscribe = null; this._root = null;
        this._b.dissolve();
    }
}
