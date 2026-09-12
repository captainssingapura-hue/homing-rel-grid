// =============================================================================
// HanFences — what the Articles bench puts BETWEEN its poems: a title above
// each, an illustration where a zero-row member stands, a colophon after the
// last. DOMAIN CODE; a fence is a domain object handed a host, as a cell is,
// and nothing here knows what minted the host.
//
//   createHanTitleFence({ title, author })   the title band above a poem
//   createHanOrnamentFence({ glyph, note })  an illustration: one large faint glyph and a note
//   createHanColophonFence(text)             a closing line
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

function _wbHanfEl(cls, text) {
    var el = document.createElement("div");
    el.className = cls;
    if (text != null) el.textContent = text;
    return el;
}

function createHanTitleFence(opts) {
    opts = opts || {};
    return {
        render: function (host) {
            _wbHanfEnsureStyle();
            var root = _wbHanfEl("wb-hanf wb-hanf-title");
            var t = document.createElement("span"); t.className = "wb-hanf-t"; t.textContent = opts.title || "";
            var a = document.createElement("span"); a.className = "wb-hanf-a"; a.textContent = opts.author || "";
            root.appendChild(t); root.appendChild(a);
            host.appendChild(root);
        },
        dispose: function () {}
    };
}

function createHanOrnamentFence(opts) {
    opts = opts || {};
    return {
        render: function (host) {
            _wbHanfEnsureStyle();
            var root = _wbHanfEl("wb-hanf wb-hanf-orn");
            root.appendChild(_wbHanfEl("wb-hanf-g", opts.glyph || "✿"));
            if (opts.note) root.appendChild(_wbHanfEl("wb-hanf-n", opts.note));
            host.appendChild(root);
        },
        dispose: function () {}
    };
}

function createHanColophonFence(text) {
    return {
        render: function (host) {
            _wbHanfEnsureStyle();
            host.appendChild(_wbHanfEl("wb-hanf wb-hanf-colophon", text || ""));
        },
        dispose: function () {}
    };
}
