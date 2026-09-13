// =============================================================================
// HanFences — what the Articles bench puts BETWEEN its poems: a title above
// each, an illustration where a zero-row member stands, a colophon after the
// last. DOMAIN CODE; a fence is a NOUN, as a cell is: it owns its element,
// minted on the branch it was handed, and answers fenceElement() once to
// whoever places it — and nothing here knows what placed it.
//
//   createHanTitleFence({ branch, title, author })   the title band above a poem
//   createHanOrnamentFence({ branch, glyph, note })  an illustration: one large faint glyph and a note
//   createHanColophonFence({ branch, text })         a closing line
//
// Drawn to sit over the squares' width: the same serif as the squares, and
// the same grey as their hairlines, so the fences read as the manuscript's
// own margins rather than as chrome around it.
// =============================================================================

// THE LOOKS ARE TYPED — HanFenceStyles, applied through the css manager.

/** A fence over a branch of its own: minted once on first ask, dissolved on dispose. */
function _wbHanfFence(opts, kind, build) {
    opts = opts || {};
    if (!opts.branch) throw new Error("[HanFences] opts.branch is required: the fence's own");
    var b = opts.branch, root = null;
    b.activate({ toString: function () { return "HanFence " + css.className(kind); } });
    var el = function (name, cls, text) {
        var x = b.createElement(name, "span");
        css.addClass(x, cls);
        if (text != null) x.textContent = text;
        return x;
    };
    return {
        fenceElement: function () {
            if (root) return root;
            root = b.createElement("fence", "div");
            css.addClass(root, wb_hanf, kind);
            build(root, el);
            return root;
        },
        dispose: function () { root = null; b.dissolve(); }
    };
}

function createHanTitleFence(opts) {
    opts = opts || {};
    return _wbHanfFence(opts, wb_hanf_title, function (root, el) {
        root.appendChild(el("title", wb_hanf_t, opts.title || ""));
        root.appendChild(el("author", wb_hanf_a, opts.author || ""));
    });
}

function createHanOrnamentFence(opts) {
    opts = opts || {};
    return _wbHanfFence(opts, wb_hanf_orn, function (root, el) {
        root.appendChild(el("glyph", wb_hanf_g, opts.glyph || "\u273F"));
        if (opts.note) root.appendChild(el("note", wb_hanf_n, opts.note));
    });
}

function createHanColophonFence(opts) {
    opts = opts || {};
    return _wbHanfFence(opts, wb_hanf_colophon, function (root) {
        root.textContent = opts.text || "";
    });
}
