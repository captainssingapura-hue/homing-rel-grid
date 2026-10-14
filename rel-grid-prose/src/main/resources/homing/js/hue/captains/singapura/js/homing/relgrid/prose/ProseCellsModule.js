// =============================================================================
// ProseCellsModule — the cells of a document's table (RFC 0050 · Episode 2,
// Appendix: The Frame; rel-grid-prose): a data cell and a header cell, nouns
// as every cell is - each minted once on the branch it was handed, placed by
// the grid in a slot, told nothing it must answer, never offered control.
//
// A CELL IS A CONTAINER. A data cell wears the house's data cell and a header
// cell the house's header cell, whole: their room, and nothing of the table's
// rules - which the table's design draws on the slots, where a row keeps them
// level however its cells wrap - nor of the look of their words. Their words
// wear their own: a header's and a strong cell's the house's strong words, a
// muted cell's its muted words, a badge the house's badge of its tone. Their
// own: the alignment, the wrapping, and the badge's box.
//
// WHAT A CELL'S TEXT BECOMES is the table's to say (inline): plain text by
// default; a document hands in its own inline drawing - its links, its code.
// WHAT A COPY OF IT CARRIES is what it drew, never what it was told: a bold
// cell copies as its words, a link as its text, a badge as its word - on one
// line, its runs of white space one space, so a value never breaks a row of
// tab-separated text.
//
//   new ProseCell({ branch, text, inline, align?, emphasis?, badge?, colSpan? })
//       emphasis: 'strong' | 'muted'   badge: 'success' | 'warning' | 'danger'
//   new ProseHeader({ branch, title, inline, align? })
//   align: 'left' | 'center' | 'right'; none said, as the slot has it
//   cell.copyValue()   header.copyValue()   what it drew, as one line of text
// =============================================================================

/** A leaf of the house worn on an element, whole: one look per leaf, made once. */
function _proseWear(leaf, el) {
    var looks = _proseWear._looks || (_proseWear._looks = {});
    var look = looks[leaf] || (looks[leaf] = new Look(Object.freeze({ LEAF: leaf }), ROLE.STANDALONE));
    look.wear(el);
    return el;
}

/** What an element drew, as one line: its text, every run of white space one space. */
function _proseDrawn(el) { return String(el.textContent || "").replace(/\s+/g, " ").trim(); }

function _proseAligned(el, align) {
    if (align === "left") css.addClass(el, prose_left);
    else if (align === "center") css.addClass(el, prose_center);
    else if (align === "right") css.addClass(el, prose_right);
    return el;
}

class ProseCell {

    constructor(opts) {
        opts = opts || {};
        if (!opts.branch) throw new Error("[ProseCell] opts.branch is required: the cell's own");
        if (typeof opts.inline !== "function") throw new Error("[ProseCell] opts.inline is required: how its text is drawn");
        this._branch = opts.branch;
        this._branch.activate(this);
        this._text = (opts.text === undefined || opts.text === null) ? "" : String(opts.text);
        this._inline = opts.inline;
        this._align = opts.align || null;
        this._emphasis = opts.emphasis || null;
        this._badge = opts.badge || null;
        this._span = (opts.colSpan > 1) ? opts.colSpan : 1;
        this._el = null;
    }

    /** The cell's element, minted once: the house's data cell, a container, its words drawn by the table's inline in it. */
    cellElement() {
        if (this._el) return this._el;
        var el = this._el = this._branch.createElement("cell", "div");
        css.addClass(el, prose_cell);
        _proseAligned(el, this._align);
        _proseWear("data-cell", el);                                                                // the cell: its room
        var into = el;
        if (this._emphasis) {                                                                       // its words said strongly, or under the rest
            into = this._branch.createElement("words", "span");
            _proseWear(this._emphasis, into);
            el.appendChild(into);
        }
        if (this._badge) {
            var badge = this._branch.createElement("badge", "span");
            css.addClass(badge, prose_badge);
            _proseWear(this._badge + "-badge", badge);                                              // the house's badge of its tone
            into.appendChild(badge);
            into = badge;
        }
        this._inline(this._text, into);
        return el;
    }

    /** Its value to copy: what it drew, as one line - a bold cell's words, a link's text, a badge's word. */
    copyValue() { return _proseDrawn(this.cellElement()); }

    /** The columns it reaches across: one, or the span it was given. */
    colSpan() { return this._span; }

    onSelect() {}

    /** The owner's to call: the table, when it is done. */
    dispose() {
        this._el = null;
        this._branch.dissolve();
    }
}

class ProseHeader {

    constructor(opts) {
        opts = opts || {};
        if (!opts.branch) throw new Error("[ProseHeader] opts.branch is required: the header cell's own");
        if (typeof opts.inline !== "function") throw new Error("[ProseHeader] opts.inline is required: how its title is drawn");
        this._branch = opts.branch;
        this._branch.activate(this);
        this._title = (opts.title === undefined || opts.title === null) ? "" : String(opts.title);
        this._inline = opts.inline;
        this._align = opts.align || null;
        this._el = null;
    }

    /** The header cell's element, minted once: the house's header cell, a container, its title drawn by the table's inline in strong words. */
    headerElement() {
        if (this._el) return this._el;
        var el = this._el = this._branch.createElement("head", "div");
        css.addClass(el, prose_head);
        _proseAligned(el, this._align);
        _proseWear("header-cell", el);                                                              // the cell: its room
        var words = this._branch.createElement("words", "span");
        _proseWear("strong", words);                                                                // its title, said strongly
        el.appendChild(words);
        this._inline(this._title, words);
        return el;
    }

    /** Its value to copy: the title it drew, as one line. */
    copyValue() { return _proseDrawn(this.headerElement()); }

    dispose() {
        this._el = null;
        this._branch.dissolve();
    }
}
