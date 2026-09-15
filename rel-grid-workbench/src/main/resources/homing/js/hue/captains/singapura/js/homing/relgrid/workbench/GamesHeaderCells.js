// =============================================================================
// GamesHeaderCells — the Games Catalogue's HEADER CELLS: nouns the relation
// answers to headerFor(column), placed by whoever arranges into the header
// slot and never told anything after. DOMAIN CODE; the word "grid" does not
// appear in it. Each is a label, an INDICATION — a caret for the direction
// this column sorts in, its number among the keys, a mark while a filter is
// held — and one control: the ▾ that opens the column's menu, where sorting
// and filtering are done, as a spreadsheet does it. The label and the caret
// are not buttons: what a header says is not what a header does.
//
//   new GamesHeaderCell({ branch, column, owner, menuHost })
//       owner: what the menu needs (see GamesColumnMenu), and label(col), sortOf(col),
//              sortCount(), filterOf(col) for the indication
//   headerElement()        the cell's element, minted once on its branch
//   paint()                redraw the indication from what the owner holds now
//   openMenu() / closeMenu()   the menu, on a sub-branch of the cell's, gone when it closes
//   dispose()              the owner's to call; dissolves the branch, menu and all
// =============================================================================

class GamesHeaderCell {

    constructor(opts) {
        var self = this;
        this._branch = opts.branch;
        this._branch.activate(this);
        this._column = opts.column;
        this._owner = opts.owner;
        this._host = opts.menuHost || null;
        this._menu = null;
        this._el = this._branch.createElement("head", "div");
        css.addClass(this._el, wb_gh);
        this._label = this._branch.createElement("label", "span");
        css.addClass(this._label, wb_gh_label);
        this._label.textContent = this._owner.label(this._column);
        this._caret = this._branch.createElement("caret", "span");
        css.addClass(this._caret, wb_gh_caret);
        this._order = this._branch.createElement("order", "span");
        css.addClass(this._order, wb_gh_order);
        this._mark = this._branch.createElement("mark", "span");
        css.addClass(this._mark, wb_gh_mark);
        this._mark.setAttribute("title", "filtered");
        this._button = this._branch.createElement("menu-btn", "button");
        css.addClass(this._button, wb_gh_menu, wb_gh_menu_hot);
        this._button.setAttribute("type", "button");
        this._button.setAttribute("aria-label", "sort and filter " + this._owner.label(this._column));
        this._button.setAttribute("aria-haspopup", "menu");
        this._button.textContent = "▾";
        this._button.addEventListener("click", function (e) {
            if (e.preventDefault) e.preventDefault();
            if (self._menu) self.closeMenu(); else self.openMenu();
        });
        this._el.appendChild(this._label);
        this._el.appendChild(this._caret);
        this._el.appendChild(this._order);
        this._el.appendChild(this._mark);
        this._el.appendChild(this._button);
        this.paint();
    }

    headerElement() { return this._el; }
    column() { return this._column; }

    /** The indication: the caret from the sort held, the number among the keys, the mark from the filter. */
    paint() {
        var s = this._owner.sortOf(this._column), f = this._owner.filterOf(this._column);
        this._caret.textContent = s ? (s.dir === "desc" ? "▼" : "▲") : "";
        this._order.textContent = (s && this._owner.sortCount() > 1) ? String(s.index + 1) : "";
        this._mark.textContent = f ? "🔍" : "";        // a magnifier: a filter is held on this column
        css.toggleClass(this._button, wb_gh_menu_on, !!(s || f));
        return this;
    }

    openMenu() {
        if (this._menu || !this._host) return null;
        var self = this;
        this._menu = new GamesColumnMenu({
            branch: this._branch.createBranch("menu"), host: this._host, anchor: this._el,
            column: this._column, owner: this._owner,
            onClose: function () { self._menu = null; css.toggleClass(self._button, wb_gh_menu_open, false); }
        });
        css.toggleClass(this._button, wb_gh_menu_open, true);
        return this._menu;
    }

    closeMenu() { if (this._menu) this._menu.close(); }

    dispose() {
        this.closeMenu();
        this._branch.dissolve();
    }
}
