// =============================================================================
// RelGridLabelsModule — RFC 0050 · Episode 2's stock HEADER cell, one of the
// out-of-the-box cells (rel-grid-cells, Appendix: The Frame): a header cell
// that shows a label, and nothing else. The grid writes no header text of its
// own: what a column is called is said by a header cell the relation answers
// through headerFor(column), and a relation that only has labels answers
// these.
//
// A HEADER CELL IS A NOUN, as a cell is: its element is minted once on the
// branch it was handed, and the grid places it in the header slot its column
// maps to. It is told nothing and offered nothing; a click on it is the
// header's own, and this one wires none.
//
//   new RelGridLabelHeader({ branch, label, overflow? })
//     · branch — the header cell's OWN branch, unactivated; dispose() dissolves it
//     · overflow — 'ellipsis' (the default), 'clip' or 'wrap', as the text cell's
//   headerElement() / set(label) / label() / dispose()
//
//   new RelGridLabels({ branch, labels?, overflow? })
//     · branch — its OWN branch, unactivated: it activates it, and each
//       header it makes is minted on a sub-branch of it; dispose() dissolves it
//     · labels — column → text; a column not named is called by its name
//   headerFor(column)     the label header for a column, made once and kept:
//                         what a relation answers the grid with
//   dispose()             every header made, and the branch
//
// ITS ROOM AND ITS OVERFLOW ARE ITS OWN — RelGridStockStyles: hrg_label, and
// the overflow's class. ITS INK, FACE AND WEIGHT ARE THE HOUSE'S: it wears the
// house's header cell, as a table's header does.
// =============================================================================

/** What a label does with text too wide for its column: ellipsis unless it says otherwise. */
function _hrgLabelOverflow(choice) {
    if (choice === undefined || choice === null || choice === "ellipsis") return hrg_text_ellipsis;
    if (choice === "clip") return hrg_text_clip;
    if (choice === "wrap") return hrg_text_wrap;
    throw new Error("[RelGridLabelHeader] overflow is 'ellipsis', 'clip' or 'wrap', not " + JSON.stringify(choice));
}

class RelGridLabelHeader {

    constructor(opts) {
        opts = opts || {};
        if (!opts.branch) throw new Error("[RelGridLabelHeader] opts.branch is required: the header cell's own");
        this._branch = opts.branch;
        this._branch.activate(this);
        this._label = (opts.label === undefined || opts.label === null) ? "" : String(opts.label);
        this._overflow = _hrgLabelOverflow(opts.overflow);
        this._el = null;
    }

    /** The header cell's element, minted once on its branch: the label, in the house's header ink and weight. */
    headerElement() {
        if (!this._el) {
            this._el = this._branch.createElement("label", "div");
            css.addClass(this._el, hrg_label, this._overflow);
            if (!RelGridLabelHeader._look) RelGridLabelHeader._look = new Look(Object.freeze({ LEAF: "header-cell" }), ROLE.STANDALONE);
            RelGridLabelHeader._look.wear(this._el, { only: ["color-ink", "type-face", "type-weight", "type-scale", "type-treatment"] });
            this._el.textContent = this._label;
        }
        return this._el;
    }

    /** The owner renamed the column. */
    set(label) {
        this._label = (label === undefined || label === null) ? "" : String(label);
        if (this._el) this._el.textContent = this._label;
        return this;
    }

    label() { return this._label; }

    /** The owner's to call, never the grid's. Dissolves the header cell's branch. */
    dispose() {
        this._el = null;
        this._branch.dissolve();
    }
}

class RelGridLabels {

    constructor(opts) {
        opts = opts || {};
        if (!opts.branch) throw new Error("[RelGridLabels] opts.branch is required: the headers are minted on it");
        this._branch = opts.branch;
        this._branch.activate(this);
        this._labels = opts.labels || {};
        this._overflow = opts.overflow;
        this._made = new Map();      // column → its label header, made once and kept
        this._seq = 0;
    }

    /** The label header for a column: what the relation answers the grid's headerFor(column) with. */
    headerFor(column) {
        var cell = this._made.get(column);
        if (!cell) {
            var label = Object.prototype.hasOwnProperty.call(this._labels, column) ? this._labels[column] : column;
            cell = new RelGridLabelHeader({ branch: this._branch.createBranch("label-" + (++this._seq)), label: label, overflow: this._overflow });
            this._made.set(column, cell);
        }
        return cell;
    }

    /** Every header made, disposed, and the branch dissolved; the owner's to call when its relation is done. */
    dispose() {
        this._made.forEach(function (cell) { cell.dispose(); });
        this._made.clear();
        this._branch.dissolve();
    }
}
