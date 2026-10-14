// =============================================================================
// ProseCellsModule — the cells of a document's table (RFC 0050 · Episode 2,
// Appendix: The Frame; rel-grid-prose): a data cell and a header cell, nouns
// as every cell is - each minted once on the branch it was handed, placed by
// the grid in a slot, told nothing it must answer, never offered control.
//
// THEIR LOOKS ARE THE DOCUMENT'S. A data cell wears the house's data cell and
// a header cell the house's header cell - their room, ink, face and weight -
// all but the rules, which the table's design draws on the slots, where a row
// keeps them level however its cells wrap. A strong cell takes a header cell's
// weight, a muted one a caption's ink, and a badge is the house's badge of its
// tone. Their own: the alignment, the wrapping, and the badge's box.
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

/** A leaf of the house worn on an element, part of it or whole: one look per leaf, made once. */
function _proseWear(leaf, el, part) {
    var looks = _proseWear._looks || (_proseWear._looks = {});
    var look = looks[leaf] || (looks[leaf] = new Look(Object.freeze({ LEAF: leaf }), ROLE.STANDALONE));
    look.wear(el, part);
    return el;
}

/** The rules are the table's design's, drawn on the slots: a cell wears none of its leaf's. */
var _PROSE_NO_RULES = Object.freeze(["shape-border", "color-border"]);

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

    /** The cell's element, minted once: the house's data cell, its text drawn by the table's inline. */
    cellElement() {
        if (this._el) return this._el;
        var el = this._el = this._branch.createElement("cell", "div");
        css.addClass(el, prose_cell);
        _proseAligned(el, this._align);
        var except = _PROSE_NO_RULES.slice();
        if (this._emphasis === "strong") except.push("type-weight");
        if (this._emphasis === "muted") except.push("color-text");
        _proseWear("data-cell", el, { except: except });
        if (this._emphasis === "strong") _proseWear("header-cell", el, { only: ["type-weight"] });   // a strong cell: a header's weight
        if (this._emphasis === "muted") _proseWear("caption", el, { only: ["color-text"] });         // a muted one: a caption's ink
        var into = el;
        if (this._badge) {
            into = this._branch.createElement("badge", "span");
            css.addClass(into, prose_badge);
            _proseWear(this._badge + "-badge", into);                                               // the house's badge of its tone
            el.appendChild(into);
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

    /** The header cell's element, minted once: the house's header cell, its title drawn by the table's inline. */
    headerElement() {
        if (this._el) return this._el;
        var el = this._el = this._branch.createElement("head", "div");
        css.addClass(el, prose_head);
        _proseAligned(el, this._align);
        _proseWear("header-cell", el, { except: _PROSE_NO_RULES.slice() });
        this._inline(this._title, el);
        return el;
    }

    /** Its value to copy: the title it drew, as one line. */
    copyValue() { return _proseDrawn(this.headerElement()); }

    dispose() {
        this._el = null;
        this._branch.dissolve();
    }
}
