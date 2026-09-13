// =============================================================================
// DishViews — how the dishes come to be in an ORDER, and which of them are
// shown: the bench's answer to the view handover (RFC 0050 · Episode 2), the
// second question the grid waits for. DOMAIN CODE; the grid is not on the
// path, and nothing here could put it there.
//
// The grid asked RelGridViewHandover — a question about the table as a whole,
// carrying nothing — and lent the mask's panel. What it wants back is a View:
// the store's pks, in order, possibly fewer. It will present exactly that.
// Everything between is here: a fixed set of PROFILES, each a sort and a
// filter over the store's values; the panel a person picks one on; and the
// memory of which one this table is under.
//
//   dishViewProfiles()                        the set, in the order the panel lists them
//   createDishViews(store, { onChanged? })    one per table: what it is under, and its View
//       .profiles()                           the set
//       .held()                               the profile this table is under
//       .viewFor(key)                         a profile's View over the store NOW: pks
//       .choose(key)                          hold it, and answer its View
//       .describe()                           one line for the status: the explanation
//   dishViewPanel(views, question, mask, { branch, onChosen? })
//       → Promise<RelGridView | undefined>
//
// THE EXPLANATION IS HERE, NOT IN THE GRID. The grid holds nothing about why
// its rows are in this order — it handed the arrangement over — so the only
// party that can say "cheapest first, 6 of 6" is this one, and it says it in
// the table's own status line. A View is a reading of the store at the
// moment it was asked for; an edit afterwards changes a cell and never an
// arrangement, so a row can outgrow its profile and stay until the next ask.
// That is the model, not a gap: re-asking is a gesture.
//
// The panel is the grid's BOX and this file's CONTENT, exactly as the copy
// panel is: an element minted on a branch of its own for the session and
// HANDED to the grid — mask.panel(element) — which places it in the box. A
// list; ↑ ↓ move, Enter or a click chooses, 1–9 choose by number, Escape
// cancels. The choice settles the promise once — a View for a profile,
// nothing for cancel — the session's branch is dissolved, and the grid
// takes its box down.
// =============================================================================

var _WB_VIEW_STYLE_ID = "bench-view-style";
var _WB_VIEW_CSS = [
    ".wb-view{display:flex;flex-direction:column;gap:6px;height:100%;box-sizing:border-box;",
    "  padding:10px 14px;font:12px sans-serif;outline:none;}",
    ".wb-view-head{display:flex;align-items:baseline;gap:8px;white-space:nowrap;overflow:hidden;}",
    ".wb-view-title{font-size:14px;font-weight:600;}",
    ".wb-view-sub{color:var(--color-text-muted);font-size:11px;overflow:hidden;text-overflow:ellipsis;}",
    // The list fills what the head and foot leave, and scrolls if the box is short.
    ".wb-view-list{flex:1 1 auto;min-height:0;overflow-y:auto;margin:0;padding:0;list-style:none;",
    "  border:1px solid var(--color-border);border-radius:5px;}",
    ".wb-view-item{display:flex;align-items:baseline;gap:8px;padding:4px 10px;cursor:pointer;",
    "  border-bottom:1px solid var(--color-border);outline:none;}",
    ".wb-view-item:last-child{border-bottom:0;}",
    ".wb-view-item:hover,.wb-view-item:focus{background:color-mix(in srgb, var(--color-accent) 12%, transparent);}",
    ".wb-view-item.wb-view-held .wb-view-label::after{content:' \\2713';color:var(--color-accent);}",
    ".wb-view-num{width:1.2em;color:var(--color-text-muted);font-size:11px;}",
    ".wb-view-label{flex:0 0 auto;font-weight:600;}",
    ".wb-view-rule{flex:1 1 auto;color:var(--color-text-muted);font-size:11px;white-space:nowrap;",
    "  overflow:hidden;text-overflow:ellipsis;}",
    ".wb-view-count{flex:0 0 auto;color:var(--color-text-muted);font-size:11px;font-variant-numeric:tabular-nums;}",
    ".wb-view-foot{display:flex;align-items:center;gap:10px;color:var(--color-text-muted);font-size:10px;",
    "  white-space:nowrap;}",
    ".wb-view-keys{flex:1 1 auto;text-align:right;overflow:hidden;text-overflow:ellipsis;}",
    ".wb-view-cancel{font:inherit;font-size:11px;padding:2px 9px;border:1px solid var(--color-border);",
    "  border-radius:4px;background:transparent;color:var(--color-text-primary);cursor:pointer;}"
].join("\n");

function _wbViewEnsureStyle() {
    if (typeof document === "undefined" || !document.head) return;
    if (document.getElementById && document.getElementById(_WB_VIEW_STYLE_ID)) return;
    var s = document.createElement("style");
    s.id = _WB_VIEW_STYLE_ID;
    s.textContent = _WB_VIEW_CSS;
    document.head.appendChild(s);
}

// ── the profiles ──────────────────────────────────────────────────────────
//
// Each is a KEEP (which rows) and an ORDER (a list of keys, each a column
// and a direction). Ties fall back to base order, because the sort is stable
// and the rows arrive in base order. Text compares case-insensitively;
// absence sorts last either way.

function _wbCmp(a, b) {
    var an = (a === null || a === undefined || a === ""), bn = (b === null || b === undefined || b === "");
    if (an || bn) return an && bn ? 0 : (an ? 1 : -1);
    if (typeof a === "number" && typeof b === "number") return a - b;
    var as = String(a).toLowerCase(), bs = String(b).toLowerCase();
    return as < bs ? -1 : (as > bs ? 1 : 0);
}
function _wbOrder(keys) {
    return function (x, y) {
        for (var k = 0; k < keys.length; k++) {
            var c = _wbCmp(x.row[keys[k].column], y.row[keys[k].column]);
            if (c !== 0) return keys[k].direction === "desc" ? -c : c;
        }
        return x.at - y.at;                        // base order, explicitly: stability is not assumed
    };
}

var _WB_EUROPE = { French: true, English: true, German: true, Italian: true };
var _WB_PROFILES = [
    { key: "base",        label: "As entered",
      rule: "every dish, in the order the store keeps",
      keep: function () { return true; },                          order: [] },
    { key: "cheapest",    label: "Cheapest first",
      rule: "every dish · price ↑",
      keep: function () { return true; },                          order: [{ column: "price", direction: "asc" }] },
    { key: "popular",     label: "Most popular",
      rule: "every dish · popularity ↓, then sold ↓",
      keep: function () { return true; },                          order: [{ column: "popularity", direction: "desc" }, { column: "sold", direction: "desc" }] },
    { key: "rated",       label: "Best rated, then cheapest",
      rule: "every dish · stars ↓, then price ↑",
      keep: function () { return true; },                          order: [{ column: "stars", direction: "desc" }, { column: "price", direction: "asc" }] },
    { key: "light",       label: "Light dishes",
      rule: "calories under 650 · calories ↑",
      keep: function (r) { return typeof r.calories === "number" && r.calories < 650; },
                                                                   order: [{ column: "calories", direction: "asc" }] },
    { key: "european",    label: "European kitchens",
      rule: "French, English, German, Italian · style ↑, then ingredient ↑",
      keep: function (r) { return _WB_EUROPE[r.style] === true; },
                                                                   order: [{ column: "style", direction: "asc" }, { column: "ingredient", direction: "asc" }] },
    { key: "bestsellers", label: "Best sellers, dearest first",
      rule: "sold 60 or more · price ↓",
      keep: function (r) { return typeof r.sold === "number" && r.sold >= 60; },
                                                                   order: [{ column: "price", direction: "desc" }] }
];

/** The set, copied shallowly: keys, labels and rules are read; the functions are shared. */
function dishViewProfiles() { return _WB_PROFILES.slice(); }

function _wbProfile(key) {
    for (var k = 0; k < _WB_PROFILES.length; k++) if (_WB_PROFILES[k].key === key) return _WB_PROFILES[k];
    return null;
}

/**
 * One per table: the profile it is under, and the View for any profile over
 * the store as it is now. Reads the STORE, not the cells — a profile is a
 * statement about values, and the store is where values are.
 */
function createDishViews(store, opts) {
    opts = opts || {};
    var held = "base";

    function rows() {
        var pks = store.pks(), cols = store.columns(), out = [];
        for (var i = 0; i < pks.length; i++) {
            var row = {};
            for (var c = 0; c < cols.length; c++) row[cols[c]] = store.get(pks[i], cols[c]);
            out.push({ pk: pks[i], row: row, at: i });
        }
        return out;
    }
    function viewFor(key) {
        var p = _wbProfile(key);
        if (!p) throw new Error("[DishViews] no profile '" + key + "'");
        var kept = rows().filter(function (r) { return p.keep(r.row); });
        kept.sort(_wbOrder(p.order));
        return kept.map(function (r) { return r.pk; });
    }
    function describe() {
        var p = _wbProfile(held), n = viewFor(held).length, all = store.pks().length;
        return p.label + " — " + p.rule + " · " + n + " of " + all;
    }

    return {
        profiles: dishViewProfiles,
        held:     function () { return held; },
        viewFor:  viewFor,
        choose:   function (key) {
            var pks = viewFor(key);                 // throws before anything is held
            held = key;
            if (opts.onChanged) opts.onChanged(key);
            return pks;
        },
        describe: describe
    };
}

var _wbViewSeq = 0;

/**
 * The choice, handed to the grid's panel: the profiles as a list, the held
 * one marked, each with its rule and how many dishes it shows right now.
 * Settles ONCE — a View for the chosen profile, nothing for Cancel or Escape
 * — dissolves its own branch, and the grid takes the box down.
 */
function dishViewPanel(views, question, mask, opts) {
    opts = opts || {};
    if (!opts.branch) throw new Error("[DishViews] opts.branch is required: the panel's own");
    _wbViewEnsureStyle();
    var b = opts.branch.createBranch("view-" + (++_wbViewSeq));   // this session's, dissolved with it
    b.activate({ toString: function () { return "dishViewPanel"; } });
    var mint = function (name, tag) { return b.createElement(name, tag); };

    return new Promise(function (resolve) {
        var settled = false;
        function settle(answer) {
            if (settled) return;
            settled = true;
            resolve(answer);
            b.dissolve();                                     // the elements go; the grid's box follows
        }
        function choose(key) {
            if (settled) return;
            var pks = views.choose(key);
            if (opts.onChosen) opts.onChosen(key, pks.length);
            settle(new RelGridView(pks));
        }

        var root = mint("root", "div");
        root.className = "wb-view";
        root.tabIndex = -1;

        var head = mint("head", "div");
        head.className = "wb-view-head";
        var title = mint("title", "span");
        title.className = "wb-view-title";
        title.textContent = "Arrange the dishes";
        var sub = mint("sub", "span");
        sub.className = "wb-view-sub";
        sub.textContent = "the grid handed the order of its rows over, and is waiting";
        head.appendChild(title); head.appendChild(sub);
        root.appendChild(head);

        var list = mint("list", "ul");
        list.className = "wb-view-list";
        var items = [], profiles = views.profiles(), heldKey = views.held();
        profiles.forEach(function (p, idx) {
            var li = mint("item-" + p.key, "li");
            li.className = "wb-view-item" + (p.key === heldKey ? " wb-view-held" : "");
            li.tabIndex = -1;
            li.profile = p.key;                                  // a property, so a test can read it back
            li.setAttribute("role", "option");
            li.setAttribute("aria-selected", p.key === heldKey ? "true" : "false");
            var num = mint("num-" + p.key, "span");
            num.className = "wb-view-num";
            num.textContent = String(idx + 1);
            var label = mint("label-" + p.key, "span");
            label.className = "wb-view-label";
            label.textContent = p.label;
            var rule = mint("rule-" + p.key, "span");
            rule.className = "wb-view-rule";
            rule.textContent = p.rule;
            var count = mint("count-" + p.key, "span");
            count.className = "wb-view-count";
            count.textContent = views.viewFor(p.key).length + " dishes";
            li.appendChild(num); li.appendChild(label); li.appendChild(rule); li.appendChild(count);
            li.addEventListener("click", function () { choose(p.key); });
            list.appendChild(li);
            items.push(li);
        });
        root.appendChild(list);

        var foot = mint("foot", "div");
        foot.className = "wb-view-foot";
        var note = mint("note", "span");
        note.textContent = "a reading of the store now; an edit later changes a cell, not the order";
        var keys = mint("keys", "span");
        keys.className = "wb-view-keys";
        keys.textContent = "1–9  ↑↓  Enter  Esc";
        var cancel = mint("cancel", "button");
        cancel.className = "wb-view-cancel";
        cancel.type = "button";
        cancel.textContent = "Cancel";
        cancel.addEventListener("click", function () { settle(undefined); });
        foot.appendChild(note); foot.appendChild(keys); foot.appendChild(cancel);
        root.appendChild(foot);

        function focused() {
            var at = items.indexOf(document.activeElement);
            return at < 0 ? Math.max(0, items.map(function (li) { return li.profile; }).indexOf(heldKey)) : at;
        }
        root.addEventListener("keydown", function (e) {
            if (e.stopPropagation) e.stopPropagation();          // ours; the grid is not listening anyway
            var key = e.key;
            if (typeof key === "string" && key.length === 1 && key >= "1" && key <= "9" && !e.ctrlKey && !e.metaKey && !e.altKey) {
                var n = key.charCodeAt(0) - 49;
                if (n < items.length) { if (e.preventDefault) e.preventDefault(); choose(items[n].profile); }
                return;
            }
            if (key === "Escape") {
                if (e.preventDefault) e.preventDefault();
                settle(undefined);
            } else if (key === "ArrowUp" || key === "ArrowDown") {
                if (e.preventDefault) e.preventDefault();
                var next = (focused() + (key === "ArrowDown" ? 1 : items.length - 1)) % items.length;
                if (items[next].focus) items[next].focus();
            } else if (key === "Enter") {
                // Handled here rather than left to any default, because a host shell
                // that owns Enter for its panes (the workbench does) may cancel it.
                if (e.preventDefault) e.preventDefault();
                choose(items[focused()].profile);
            }
        });

        mask.panel(root);                                     // the grid places it in its box
        var start = items[Math.max(0, items.map(function (li) { return li.profile; }).indexOf(heldKey))];
        if (start && start.focus) start.focus();
    });
}
