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

var _WB_HANF_STYLE_ID = "bench-han-fence-style";
var _WB_HANF_CSS = [
    ".wb-hanf{font-family:'Noto Serif CJK SC','Source Han Serif SC','Songti SC','SimSun','PMingLiU',serif;",
    "  color:var(--color-text-primary);padding:6px 4px 4px;}",
    ".wb-hanf-title{display:flex;align-items:baseline;gap:10px;}",
    ".wb-hanf-title .wb-hanf-t{font-size:18px;letter-spacing:0.15em;}",
    ".wb-hanf-title .wb-hanf-a{font-size:12px;color:var(--color-text-muted);letter-spacing:0.1em;}",
    ".wb-hanf-orn{display:flex;flex-direction:column;align-items:center;gap:2px;padding:10px 0 8px;}",
    ".wb-hanf-orn .wb-hanf-g{font-size:44px;line-height:1;color:color-mix(in srgb, var(--color-text-muted) 45%, transparent);}",
    ".wb-hanf-orn .wb-hanf-n{font-size:11px;color:var(--color-text-muted);letter-spacing:0.2em;}",
    ".wb-hanf-colophon{text-align:center;font-size:12px;color:var(--color-text-muted);letter-spacing:0.3em;padding:10px 0 4px;}"
].join("\n");

function _wbHanfEnsureStyle() {
    if (typeof document === "undefined" || !document.head) return;
    if (document.getElementById && document.getElementById(_WB_HANF_STYLE_ID)) return;
    var s = document.createElement("style");
    s.id = _WB_HANF_STYLE_ID;
    s.textContent = _WB_HANF_CSS;
    document.head.appendChild(s);
}

/** A fence over a branch of its own: minted once on first ask, dissolved on dispose. */
function _wbHanfFence(opts, kind, build) {
    opts = opts || {};
    if (!opts.branch) throw new Error("[HanFences] opts.branch is required: the fence's own");
    var b = opts.branch, root = null;
    b.activate({ toString: function () { return "HanFence " + kind; } });
    var el = function (name, cls, text) {
        var x = b.createElement(name, "span");
        x.className = cls;
        if (text != null) x.textContent = text;
        return x;
    };
    return {
        fenceElement: function () {
            if (root) return root;
            _wbHanfEnsureStyle();
            root = b.createElement("fence", "div");
            root.className = "wb-hanf " + kind;
            build(root, el);
            return root;
        },
        dispose: function () { root = null; b.dissolve(); }
    };
}

function createHanTitleFence(opts) {
    opts = opts || {};
    return _wbHanfFence(opts, "wb-hanf-title", function (root, el) {
        root.appendChild(el("title", "wb-hanf-t", opts.title || ""));
        root.appendChild(el("author", "wb-hanf-a", opts.author || ""));
    });
}

function createHanOrnamentFence(opts) {
    opts = opts || {};
    return _wbHanfFence(opts, "wb-hanf-orn", function (root, el) {
        root.appendChild(el("glyph", "wb-hanf-g", opts.glyph || "\u273F"));
        if (opts.note) root.appendChild(el("note", "wb-hanf-n", opts.note));
    });
}

function createHanColophonFence(opts) {
    opts = opts || {};
    return _wbHanfFence(opts, "wb-hanf-colophon", function (root) {
        root.textContent = opts.text || "";
    });
}
