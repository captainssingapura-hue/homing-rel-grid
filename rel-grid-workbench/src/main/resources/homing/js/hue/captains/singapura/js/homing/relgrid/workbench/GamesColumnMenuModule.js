// =============================================================================
// GamesColumnMenu — the Games Catalogue's COLUMN MENU: what opens from a header
// cell's ▾, the way a spreadsheet's does. DOMAIN CODE; the word "grid" does
// not appear in it. Sorting at the top — ascending, descending, cleared, each
// applied at once and the menu gone, with "then by" to keep the keys already
// held — and below it the filter: a SEARCH that narrows the column's values,
// the values with their counts and a box each, "(select all)" over whatever
// the search shows, a range for a number column; STAGED until OK, so a table
// never empties under a person still choosing; Escape, Cancel or a press
// outside discards.
//
//   new GamesColumnMenu({ branch, host, anchor, column, owner, onClose })
//       branch: the menu's own — the header cell hands it a sub-branch, activated here, dissolved on close
//       host:   where the menu's element goes (the bench's root); it is placed by the anchor's rectangle
//       anchor: the header cell's element
//       owner:  { label(col), kind(col), sortOf(col), sortCount(), filterOf(col), values(col),
//                 setSort(col, dir, additive), setFilter(col, spec) }
//               values(col): [{ value, count }] among the rows the OTHER columns' filters pass —
//               what a spreadsheet lists, so a choice narrows the next choice
//       onClose(): told once, whenever it closes, however it closed
//   element() / close()
//
// What OK applies: for a number column, the range if either end is given, and
// the values chosen if not all are; for the rest, the values chosen if not all
// are. Every value chosen is no filter at all.
// =============================================================================

class GamesColumnMenu {

    constructor(opts) {
        var self = this;
        this._branch = opts.branch;
        this._branch.activate(this);
        this._host = opts.host;
        this._column = opts.column;
        this._owner = opts.owner;
        this._onClose = opts.onClose || null;
        this._closed = false;
        var b = this._branch, col = this._column, kind = this._owner.kind(col);
        this._el = b.createElement("menu", "div");
        css.addClass(this._el, wb_gmenu);
        this._el.setAttribute("role", "menu");
        // ── sorting, at the top ──
        var held = this._owner.sortOf(col);
        this._item("asc",  (kind === "number" ? "Sort smallest to largest" : "Sort A to Z"), held && held.dir === "asc",  function () { self._sort("asc"); });
        this._item("desc", (kind === "number" ? "Sort largest to smallest" : "Sort Z to A"), held && held.dir === "desc", function () { self._sort("desc"); });
        this._thenBy = this._check("then", "then by — keep the sort already held", false, null);
        if (held) this._item("nosort", "Clear sort of this column", false, function () { self._owner.setSort(col, null, true); self.close(); });   // this key goes; the others stay
        this._el.appendChild(this._sep("sep1"));
        // ── the filter: search, the values, a range for numbers ──
        var filter = this._owner.filterOf(col) || {};
        if (kind === "number") this._range(filter);
        this._search = b.createElement("search", "input");
        css.addClass(this._search, wb_gmenu_search);
        this._search.setAttribute("type", "search");
        this._search.setAttribute("placeholder", "search " + this._owner.label(col) + "…");
        this._search.setAttribute("aria-label", "search values");
        this._search.addEventListener("input", function () { self._narrow(); });
        this._el.appendChild(this._search);
        this._values = this._owner.values(col);
        this._chosen = new Map();                                  // value → true while chosen (staged)
        var all = !Array.isArray(filter.in);
        for (var k = 0; k < this._values.length; k++) this._chosen.set(this._values[k].value, all || filter.in.indexOf(this._values[k].value) >= 0);
        this._all = this._check("all", "(select all)", false, function (on) { self._selectShown(on); });
        this._list = b.createElement("list", "div");
        css.addClass(this._list, wb_gmenu_list);
        this._boxes = [];
        for (var v = 0; v < this._values.length; v++) this._valueRow(v);
        this._el.appendChild(this._list);
        this._narrow();
        // ── the actions ──
        var actions = b.createElement("actions", "div");
        css.addClass(actions, wb_gmenu_actions);
        this._button(actions, "clear", "Clear filter", false, function () { self._owner.setFilter(col, null); self.close(); });
        this._button(actions, "cancel", "Cancel", false, function () { self.close(); });
        this._button(actions, "ok", "OK", true, function () { self._apply(); });
        this._el.appendChild(actions);
        this._el.addEventListener("keydown", function (e) {
            if (e.key === "Escape") { if (e.stopPropagation) e.stopPropagation(); self.close(); }
            else if (e.key === "Enter" && e.target === self._search) { if (e.preventDefault) e.preventDefault(); self._apply(); }
        });
        // Placed by the anchor's rectangle, in the viewport's coordinates, inside the window.
        var r = opts.anchor && opts.anchor.getBoundingClientRect ? opts.anchor.getBoundingClientRect() : null;
        if (r && this._el.style && this._el.style.setProperty) {
            var width = ((typeof window !== "undefined" && window.innerWidth) || 100000);
            this._el.style.setProperty("--wb-gmenu-left", Math.round(Math.max(4, Math.min(r.left, width - 300))) + "px");
            this._el.style.setProperty("--wb-gmenu-top", Math.round(r.bottom + 2) + "px");
        }
        this._host.appendChild(this._el);
        this._onDown = function (e) {
            var n = e.target;
            while (n) { if (n === self._el || n === opts.anchor) return; n = n.parentNode; }
            self.close();
        };
        if (typeof document !== "undefined") document.addEventListener("mousedown", this._onDown);
        if (this._search.focus) this._search.focus({ preventScroll: true });
    }

    element() { return this._el; }

    // ── the pieces ─────────────────────────────────────────────────────────

    _item(name, text, on, fn) {
        var el = this._branch.createElement(name, "button");
        css.addClass(el, wb_gmenu_item, wb_gmenu_item_hot);
        el.setAttribute("type", "button");
        el.setAttribute("role", "menuitem");
        if (on) css.addClass(el, wb_gmenu_item_on);
        el.textContent = (on ? "✓ " : "") + text;
        el.addEventListener("click", fn);
        this._el.appendChild(el);
        return el;
    }

    _check(name, text, on, fn) {
        var label = this._branch.createElement(name, "label");
        css.addClass(label, wb_gmenu_check);
        var box = this._branch.createElement(name + "-box", "input");
        box.setAttribute("type", "checkbox");
        box.checked = !!on;
        if (fn) box.addEventListener("change", function () { fn(!!box.checked); });
        var span = this._branch.createElement(name + "-text", "span");
        span.textContent = text;
        label.appendChild(box);
        label.appendChild(span);
        this._el.appendChild(label);
        return box;
    }

    _sep(name) {
        var el = this._branch.createElement(name, "div");
        css.addClass(el, wb_gmenu_sep);
        return el;
    }

    _button(into, name, text, primary, fn) {
        var el = this._branch.createElement(name, "button");
        css.addClass(el, wb_gmenu_btn);
        if (primary) css.addClass(el, wb_gmenu_btn_primary);
        el.setAttribute("type", "button");
        el.textContent = text;
        el.addEventListener("click", fn);
        into.appendChild(el);
        return el;
    }

    _range(filter) {
        var row = this._branch.createElement("range", "div");
        css.addClass(row, wb_gmenu_range);
        this._min = this._branch.createElement("min", "input");
        this._max = this._branch.createElement("max", "input");
        css.addClass(this._min, wb_gmenu_search); css.addClass(this._max, wb_gmenu_search);
        this._min.setAttribute("type", "number"); this._max.setAttribute("type", "number");
        this._min.setAttribute("placeholder", "from"); this._max.setAttribute("placeholder", "to");
        this._min.value = filter.min != null ? String(filter.min) : "";
        this._max.value = filter.max != null ? String(filter.max) : "";
        row.appendChild(this._min);
        row.appendChild(this._max);
        this._el.appendChild(row);
    }

    _valueRow(k) {
        var self = this, v = this._values[k].value;
        var label = this._branch.createElement("v" + k, "label");
        css.addClass(label, wb_gmenu_check);
        var box = this._branch.createElement("v" + k + "-box", "input");
        box.setAttribute("type", "checkbox");
        box.checked = !!this._chosen.get(v);
        box.addEventListener("change", function () { self._chosen.set(v, !!box.checked); self._paintAll(); });
        var text = this._branch.createElement("v" + k + "-text", "span");
        text.textContent = String(v === "" ? "(blank)" : v);
        var count = this._branch.createElement("v" + k + "-count", "span");
        css.addClass(count, wb_gmenu_count);
        count.textContent = String(this._values[k].count);
        label.appendChild(box);
        label.appendChild(text);
        label.appendChild(count);
        this._list.appendChild(label);
        this._boxes.push({ value: v, box: box, label: label, shown: true });
    }

    // ── the search, the selection, the apply ───────────────────────────────

    /** The search narrows what is listed; "(select all)" then means all of what is shown. */
    _narrow() {
        var needle = String(this._search.value || "").toLowerCase();
        for (var k = 0; k < this._boxes.length; k++) {
            var b = this._boxes[k];
            b.shown = needle === "" || String(b.value).toLowerCase().indexOf(needle) >= 0;
            css.toggleClass(b.label, wb_gmenu_hidden, !b.shown);
        }
        this._paintAll();
    }

    _selectShown(on) {
        for (var k = 0; k < this._boxes.length; k++) {
            var b = this._boxes[k];
            if (!b.shown) continue;
            b.box.checked = on;
            this._chosen.set(b.value, on);
        }
        this._paintAll();
    }

    _paintAll() {
        var shown = 0, on = 0;
        for (var k = 0; k < this._boxes.length; k++) if (this._boxes[k].shown) { shown++; if (this._chosen.get(this._boxes[k].value)) on++; }
        this._all.checked = shown > 0 && on === shown;
        this._all.indeterminate = on > 0 && on < shown;
    }

    _sort(dir) {
        this._owner.setSort(this._column, dir, !!this._thenBy.checked);
        this.close();
    }

    _apply() {
        var col = this._column, spec = null, chosen = [], all = true;
        this._chosen.forEach(function (on, v) { if (on) chosen.push(v); else all = false; });
        if (!all) spec = { in: chosen };
        if (this._min) {
            var lo = this._min.value === "" ? null : Number(this._min.value), hi = this._max.value === "" ? null : Number(this._max.value);
            if (lo !== null && !isFinite(lo)) lo = null;
            if (hi !== null && !isFinite(hi)) hi = null;
            if (lo !== null || hi !== null) { spec = spec || {}; spec.min = lo; spec.max = hi; }
        }
        this._owner.setFilter(col, spec);
        this.close();
    }

    close() {
        if (this._closed) return;
        this._closed = true;
        if (typeof document !== "undefined") document.removeEventListener("mousedown", this._onDown);
        this._branch.dissolve();
        if (this._onClose) this._onClose();
    }
}
