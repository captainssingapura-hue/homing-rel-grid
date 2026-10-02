// =============================================================================
// HanFences — what the Articles bench puts BETWEEN its poems: a title above
// each, an illustration where a zero-row member stands, a colophon after the
// last. DOMAIN CODE; a fence is a NOUN, as a cell is: it owns its element,
// minted on the branch it was handed, and answers fenceElement() once to
// whoever places it — and nothing here knows what placed it.
//
//   new HanTitleFence({ branch, title, author })     the title band above a poem
//   new HanOrnamentFence({ branch, glyph, note })    an illustration: one large faint glyph and a note
//   new HanColophonFence({ branch, text })           a closing line
//
// Drawn to sit over the squares' width: the same serif as the squares, and
// the same grey as their hairlines, so the fences read as the manuscript's
// own margins rather than as chrome around it.
// =============================================================================

// THE LOOKS ARE TYPED — HanFenceStyles, applied through the css manager.

/** A fence over a branch of its own: minted once on first ask, dissolved on dispose. A kind of fence builds into it. */
class HanFence {
    constructor(opts, kind) {
        var o = opts || {};
        if (!o.branch) throw new Error("[HanFences] opts.branch is required: the fence's own");
        this._branch = o.branch;
        this._kind = kind;
        this._root = null;
        this._branch.activate({ toString: function () { return "HanFence " + css.className(kind); } });
    }

    fenceElement() {
        if (this._root) return this._root;
        this._root = this._branch.createElement("fence", "div");
        css.addClass(this._root, wb_hanf, this._kind);
        this._build(this._root);
        return this._root;
    }

    dispose() { this._root = null; this._branch.dissolve(); }

    /** What a kind of fence draws into its root. Nothing, unless said. */
    _build(root) {}

    _el(name, cls, text) {
        var x = this._branch.createElement(name, "span");
        css.addClass(x, cls);
        if (text != null) x.textContent = text;
        return x;
    }
}

class HanTitleFence extends HanFence {
    constructor(opts) { super(opts, wb_hanf_title); this._title = (opts && opts.title) || ""; this._author = (opts && opts.author) || ""; }
    _build(root) {
        root.appendChild(this._el("title", wb_hanf_t, this._title));
        root.appendChild(this._el("author", wb_hanf_a, this._author));
    }
}

class HanOrnamentFence extends HanFence {
    constructor(opts) { super(opts, wb_hanf_orn); this._glyph = (opts && opts.glyph) || "\u273F"; this._note = (opts && opts.note) || ""; }
    _build(root) {
        root.appendChild(this._el("glyph", wb_hanf_g, this._glyph));
        if (this._note) root.appendChild(this._el("note", wb_hanf_n, this._note));
    }
}

class HanColophonFence extends HanFence {
    constructor(opts) { super(opts, wb_hanf_colophon); this._text = (opts && opts.text) || ""; }
    _build(root) { root.textContent = this._text; }
}
