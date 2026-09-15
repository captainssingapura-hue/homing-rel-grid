// =============================================================================
// JsonNodeCellModule — the cell for one JSON node: what a member LOOKS LIKE
// in the tree, which is the kit's whole translation of a value into a row.
// A noun, exactly as the tree's stock text cell is: it owns its element,
// minted once on the branch it was handed — a sub-branch of the document's —
// and answers RelTreeCellContract; the tree places it after its caret and
// never reads it.
//
// Three spans: the NAME (a member's key, or an element's index), the colon,
// and the VALUE printed by kind — a string quoted and cut at maxString, a
// number as it is, true/false/null as they are, a container as a count:
// {3}, [5], {} and [] — each wearing its kind as a typed class, so a theme
// colours the kinds through tokens.
//
//   new JsonNodeCell({ branch, name, index?, value, maxString? })
//   cellElement()        the span, minted once on the cell's branch
//   onSelect(mode)       'none' | 'shallow' — the current row's cell is weighted
//   set({ name?, value }) the document changed what this node is; repaint
//   text()               the line as printed, for a host's readout or a test
//   isIndex()            whether the name is an element's index
//   dispose()            the domain's to call: dissolves the cell's branch
// =============================================================================

/** A value printed for a row: kind and text. */
function _jkPrint(v, maxString) {
    if (v === null) return { kind: "literal", text: "null" };
    if (typeof v === "boolean") return { kind: "literal", text: v ? "true" : "false" };
    if (typeof v === "number") return { kind: "number", text: String(v) };
    if (typeof v === "string") {
        var s = JSON.stringify(v);
        if (s.length > maxString + 2) s = s.slice(0, maxString + 1) + "…\"";
        return { kind: "string", text: s };
    }
    if (Array.isArray(v)) return { kind: "container", text: v.length === 0 ? "[]" : "[" + v.length + "]" };
    if (typeof v === "object") { var n = Object.keys(v).length; return { kind: "container", text: n === 0 ? "{}" : "{" + n + "}" }; }
    return { kind: "literal", text: String(v) };                  // not JSON at all: printed anyway, never thrown at
}

var _JK_KIND_CLASS = null;                                        // kind → class handle, built once the handles exist

function _jkKindClass(kind) {
    if (!_JK_KIND_CLASS) _JK_KIND_CLASS = { string: jk_string, number: jk_number, literal: jk_literal, container: jk_container };
    return _JK_KIND_CLASS[kind];
}

class JsonNodeCell {

    constructor(opts) {
        opts = opts || {};
        if (!opts.branch) throw new Error("[JsonNodeCell] opts.branch is required: a cell mints on the domain's branch");
        this._branch = opts.branch;
        this._branch.activate(this);
        this._name = (opts.name === undefined || opts.name === null) ? "" : String(opts.name);
        this._index = opts.index === true;
        this._value = opts.value;
        this._maxString = (typeof opts.maxString === "number") ? opts.maxString : 80;
        this._el = null;
        this._nameEl = null;
        this._valueEl = null;
        this._kind = null;
    }

    cellElement() {
        if (this._el) return this._el;
        var el = this._branch.createElement("cell", "span");
        css.addClass(el, jk_cell);
        this._nameEl = this._branch.createElement("name", "span");
        css.addClass(this._nameEl, this._index ? jk_index : jk_key);
        var colon = this._branch.createElement("colon", "span");
        css.addClass(colon, jk_punct);
        colon.textContent = ": ";
        this._valueEl = this._branch.createElement("value", "span");
        el.appendChild(this._nameEl); el.appendChild(colon); el.appendChild(this._valueEl);
        this._el = el;
        this._paint();
        return el;
    }

    _paint() {
        if (!this._el) return;
        var p = _jkPrint(this._value, this._maxString);
        this._nameEl.textContent = this._name;
        if (this._kind !== p.kind) {
            if (this._kind) css.removeClass(this._valueEl, _jkKindClass(this._kind));
            css.addClass(this._valueEl, _jkKindClass(p.kind));
            this._kind = p.kind;
        }
        this._valueEl.textContent = p.text;
    }

    onSelect(mode) {
        if (this._el) css.toggleClass(this._el, jk_cell_current, mode === "shallow");
    }

    /** The document changed what this node is: its name, its value, or both. */
    set(change) {
        change = change || {};
        if (change.name !== undefined) this._name = String(change.name);
        if (Object.prototype.hasOwnProperty.call(change, "value")) this._value = change.value;
        this._paint();
        return this;
    }

    text() { return this._name + ": " + _jkPrint(this._value, this._maxString).text; }
    isIndex() { return this._index; }

    dispose() {
        this._branch.dissolve();
        this._el = null;
        this._nameEl = null;
        this._valueEl = null;
    }
}
