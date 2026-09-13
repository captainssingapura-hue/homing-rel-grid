// =============================================================================
// GamesHeaderCells — the Games Catalogue's HEADER CELLS: nouns the relation
// answers to headerFor(column), placed by whoever arranges into the header
// slot and never told anything after. DOMAIN CODE; the word "grid" does not
// appear in it. Each is a label that is a button — click to sort, shift-click
// to sort by this after what is already sorted — a caret that shows the
// order held, and a funnel that opens a POPOVER to choose rows by this
// column: contains, for text; a range, for numbers; any of, for a set.
//
//   new GamesHeaderCell({ branch, column, owner, popoverHost })
//       owner: { label(col), kind(col), sortOf(col), sortCount(), filterOf(col), distinct(col),
//                toggleSort(col, additive), setFilter(col, spec) }
//   headerElement()        the cell's element, minted once on its branch
//   paint()                redraw the caret and the funnel from what the owner holds now
//   dispose()              the owner's to call; dissolves the branch, popover and all
//
// The popover is the cell's own, minted on a sub-branch of its branch and
// dissolved when it closes, and put in the element the owner names as its
// popover host — the bench's root — placed by the header's own rectangle.
// Every gesture in it changes the owner's conditions at once; the owner
// tells whoever arranges, and paints every header from the new state.
// =============================================================================

class GamesHeaderCell {

    constructor(opts) {
        var self = this;
        this._branch = opts.branch;
        this._branch.activate(this);
        this._column = opts.column;
        this._owner = opts.owner;
        this._host = opts.popoverHost || null;
        this._el = this._branch.createElement("head", "div");
        css.addClass(this._el, wb_gh);
        this._sortBtn = this._branch.createElement("sort", "button");
        css.addClass(this._sortBtn, wb_gh_sort, wb_gh_sort_hot);
        this._sortBtn.setAttribute("type", "button");
        this._label = this._branch.createElement("label", "span");
        css.addClass(this._label, wb_gh_label);
        this._label.textContent = this._owner.label(this._column);
        this._caret = this._branch.createElement("caret", "span");
        css.addClass(this._caret, wb_gh_caret);
        this._order = this._branch.createElement("order", "span");
        css.addClass(this._order, wb_gh_order);
        this._sortBtn.appendChild(this._label);
        this._sortBtn.appendChild(this._caret);
        this._sortBtn.appendChild(this._order);
        this._sortBtn.addEventListener("click", function (e) {
            if (e.preventDefault) e.preventDefault();
            self._owner.toggleSort(self._column, !!e.shiftKey);
        });
        this._filterBtn = this._branch.createElement("filter", "button");
        css.addClass(this._filterBtn, wb_gh_filter, wb_gh_filter_hot);
        this._filterBtn.setAttribute("type", "button");
        this._filterBtn.setAttribute("aria-label", "filter " + this._owner.label(this._column));
        this._filterBtn.textContent = "▾";
        this._filterBtn.addEventListener("click", function (e) {
            if (e.preventDefault) e.preventDefault();
            if (self._pop) self.closePopover(); else self.openPopover();
        });
        this._el.appendChild(this._sortBtn);
        this._el.appendChild(this._filterBtn);
        this._pop = null;                                  // { branch, el, onDown } while open
        this.paint();
    }

    headerElement() { return this._el; }
    column() { return this._column; }

    /** The caret from the sort held, the funnel from the filter held. */
    paint() {
        var s = this._owner.sortOf(this._column), f = this._owner.filterOf(this._column);
        this._caret.textContent = s ? (s.dir === "desc" ? "▼" : "▲") : "";
        this._order.textContent = (s && this._owner.sortCount() > 1) ? String(s.index + 1) : "";
        css.toggleClass(this._sortBtn, wb_gh_sort_on, !!s);
        css.toggleClass(this._filterBtn, wb_gh_filter_on, !!f);
        return this;
    }

    // ── the popover: the cell's own ────────────────────────────────────────

    openPopover() {
        if (this._pop || !this._host) return null;
        var self = this, pb = this._branch.createBranch("pop"), col = this._column, kind = this._owner.kind(col);
        pb.activate(this);
        var el = pb.createElement("pop", "div");
        css.addClass(el, wb_gpop);
        el.setAttribute("role", "dialog");
        var title = pb.createElement("title", "div");
        css.addClass(title, wb_gpop_title);
        title.textContent = this._owner.label(col);
        el.appendChild(title);
        var held = this._owner.filterOf(col) || {};
        if (kind === "number") this._numberControls(pb, el, held);
        else if (kind === "set") this._setControls(pb, el, held);
        else this._textControls(pb, el, held);
        var actions = pb.createElement("actions", "div");
        css.addClass(actions, wb_gpop_actions);
        var clear = pb.createElement("clear", "button");
        css.addClass(clear, wb_gpop_btn);
        clear.setAttribute("type", "button");
        clear.textContent = "clear";
        clear.addEventListener("click", function () { self._owner.setFilter(col, null); self.closePopover(); });
        var close = pb.createElement("close", "button");
        css.addClass(close, wb_gpop_btn);
        close.setAttribute("type", "button");
        close.textContent = "close";
        close.addEventListener("click", function () { self.closePopover(); });
        actions.appendChild(clear);
        actions.appendChild(close);
        el.appendChild(actions);
        el.addEventListener("keydown", function (e) { if (e.key === "Escape") { if (e.stopPropagation) e.stopPropagation(); self.closePopover(); } });
        // Placed by the header's own rectangle, in the viewport's coordinates.
        var r = this._el.getBoundingClientRect ? this._el.getBoundingClientRect() : null;
        if (r && el.style && el.style.setProperty) {
            var left = Math.max(4, Math.min(r.left, ((typeof window !== "undefined" && window.innerWidth) || 100000) - 280));
            el.style.setProperty("--wb-gpop-left", Math.round(left) + "px");
            el.style.setProperty("--wb-gpop-top", Math.round(r.bottom + 2) + "px");
        }
        this._host.appendChild(el);
        // A press anywhere else closes it — heard at the document, as a drag's release is.
        var onDown = function (e) {
            var n = e.target;
            while (n) { if (n === el || n === self._filterBtn) return; n = n.parentNode; }
            self.closePopover();
        };
        if (typeof document !== "undefined") document.addEventListener("mousedown", onDown);
        this._pop = { branch: pb, el: el, onDown: onDown };
        css.toggleClass(this._filterBtn, wb_gh_filter_open, true);
        return el;
    }

    closePopover() {
        if (!this._pop) return;
        if (typeof document !== "undefined") document.removeEventListener("mousedown", this._pop.onDown);
        this._pop.branch.dissolve();
        this._pop = null;
        css.toggleClass(this._filterBtn, wb_gh_filter_open, false);
    }

    _textControls(pb, el, held) {
        var self = this, col = this._column;
        var input = pb.createElement("contains", "input");
        css.addClass(input, wb_gpop_input);
        input.setAttribute("placeholder", "contains…");
        input.value = held.contains || "";
        input.addEventListener("input", function () {
            var text = String(input.value || "").trim();
            self._owner.setFilter(col, text ? { contains: text } : null);
        });
        el.appendChild(input);
        if (input.focus) input.focus({ preventScroll: true });
    }

    _numberControls(pb, el, held) {
        var self = this, col = this._column;
        var row = pb.createElement("range", "div");
        css.addClass(row, wb_gpop_row);
        var min = pb.createElement("min", "input"), max = pb.createElement("max", "input");
        css.addClass(min, wb_gpop_input); css.addClass(max, wb_gpop_input);
        min.setAttribute("type", "number"); max.setAttribute("type", "number");
        min.setAttribute("placeholder", "from"); max.setAttribute("placeholder", "to");
        min.value = held.min != null ? String(held.min) : "";
        max.value = held.max != null ? String(held.max) : "";
        var apply = function () {
            var lo = min.value === "" ? null : Number(min.value), hi = max.value === "" ? null : Number(max.value);
            if (lo !== null && !isFinite(lo)) lo = null;
            if (hi !== null && !isFinite(hi)) hi = null;
            self._owner.setFilter(col, (lo === null && hi === null) ? null : { min: lo, max: hi });
        };
        min.addEventListener("input", apply);
        max.addEventListener("input", apply);
        row.appendChild(min);
        row.appendChild(max);
        el.appendChild(row);
        if (min.focus) min.focus({ preventScroll: true });
    }

    _setControls(pb, el, held) {
        var self = this, col = this._column, values = this._owner.distinct(col), chosen = (held.in || []).slice();
        var list = pb.createElement("list", "div");
        css.addClass(list, wb_gpop_list);
        var boxes = [];
        var apply = function () {
            var on = [];
            for (var b = 0; b < boxes.length; b++) if (boxes[b].box.checked) on.push(boxes[b].value);
            self._owner.setFilter(col, on.length ? { in: on } : null);
        };
        for (var k = 0; k < values.length; k++) {
            var v = values[k].value, label = pb.createElement("opt-" + k, "label");
            css.addClass(label, wb_gpop_check);
            var box = pb.createElement("box-" + k, "input");
            box.setAttribute("type", "checkbox");
            box.checked = chosen.indexOf(v) >= 0;
            box.addEventListener("change", apply);
            var text = pb.createElement("text-" + k, "span");
            text.textContent = String(v === "" ? "(none)" : v) + "  " + values[k].count;
            label.appendChild(box);
            label.appendChild(text);
            list.appendChild(label);
            boxes.push({ box: box, value: v });
        }
        el.appendChild(list);
    }

    dispose() {
        this.closePopover();
        this._branch.dissolve();
    }
}
