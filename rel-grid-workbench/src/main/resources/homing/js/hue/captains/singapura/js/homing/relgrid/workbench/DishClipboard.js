// =============================================================================
// DishClipboard — what a selection of dishes is WORTH on a clipboard, and the
// panel a person chooses that on. DOMAIN CODE, and the bench's answer to the
// first question the grid waits for (RFC 0050 · Episode 2, map 6 and ext6).
//
// The grid asked with IDENTITIES — one block per range, pks down and columns
// across — and it will write whatever comes back. Everything between those
// two is here: reading the relation's own cells, composing three forms, and
// drawing the choice on the panel the grid minted.
//
//   dishClipboardContent(relation, blocks, format, { headers? })
//       → RelGridClipboardContent          format: 'tsv' | 'csv' | 'html'
//   dishCopyPanel(relation, question, mask, { branch, onChosen? })
//       → Promise<RelGridClipboardContent | undefined>
//
// The copier reads CELLS, not the store (map 6 law 48): a cell knows what it
// is worth on a clipboard better than a store does, which is exactly what the
// rating proves. In text it is a number, because that is what a spreadsheet
// can add up; in HTML it is the stars a person saw, because that is what they
// meant to copy. A cell that offers clipboardHtml() is taken at its word;
// one that does not is escaped.
//
// The panel is the grid's BOX and this file's CONTENT. Its size was decided
// by the grid — a golden rectangle over the table — and nothing here reads or
// changes it; this HANDS the grid an element, mask.panel(element), minted on
// a branch of its own for the session — a sub-branch of the branch the host
// gave — and the grid places it in the box. The person's choice settles the
// promise: content for a format, nothing for Cancel or Escape; then the
// session's branch is dissolved, and the grid takes its box down. The grid
// never learns which.
// =============================================================================

// THE LOOKS ARE TYPED — DishCopyStyles, applied through the css manager.

var _WB_COPY_FORMATS = [
    { key: "tsv",  label: "TSV",  hint: "tab-separated — pastes into a spreadsheet",  hotkey: "t" },
    { key: "csv",  label: "CSV",  hint: "comma-separated, quoted where it must be",       hotkey: "c" },
    { key: "html", label: "HTML", hint: "a table — and the stars stay stars",         hotkey: "h" }
];

/** A field for a delimited line: quoted only when it has to be, RFC 4180 style. */
function _wbDelimited(v, delim) {
    var s = (v == null) ? "" : String(v);
    var must = s.indexOf(delim) >= 0 || s.indexOf('"') >= 0 || s.indexOf("\n") >= 0 || s.indexOf("\r") >= 0;
    return must ? '"' + s.replace(/"/g, '""') + '"' : s;
}

function _wbEscapeHtml(v) {
    return String(v == null ? "" : v)
        .replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;").replace(/"/g, "&quot;");
}

/** What the relation's OWN cell says it holds. Identities in, worth out. */
function _wbCellOf(relation, pk, col) { return relation.cellFor(pk, col); }
function _wbCellText(cell) {
    var v = (cell && typeof cell.value === "function") ? cell.value() : null;
    return (v == null) ? "" : v;
}
function _wbCellHtml(cell) {
    if (cell && typeof cell.clipboardHtml === "function") return cell.clipboardHtml();
    return _wbEscapeHtml(_wbCellText(cell));
}

function _wbHtmlTable(relation, block, headers) {
    var out = ['<table style="border-collapse:collapse;font:13px sans-serif">'];
    if (headers) {
        out.push("<thead><tr>");
        for (var c = 0; c < block.columns.length; c++)
            out.push('<th style="text-align:left;padding:2px 8px;border-bottom:1px solid #999">'
                     + _wbEscapeHtml(block.columns[c]) + "</th>");
        out.push("</tr></thead>");
    }
    out.push("<tbody>");
    for (var r = 0; r < block.pks.length; r++) {
        out.push("<tr>");
        for (var k = 0; k < block.columns.length; k++) {
            var cell = _wbCellOf(relation, block.pks[r], block.columns[k]);
            var numeric = typeof _wbCellText(cell) === "number";
            out.push('<td style="padding:2px 8px' + (numeric ? ";text-align:right" : "") + '">'
                     + _wbCellHtml(cell) + "</td>");
        }
        out.push("</tr>");
    }
    out.push("</tbody></table>");
    return out.join("");
}

/**
 * The content, composed. Text is always there — TSV for 'tsv' and 'html'
 * alike, since a spreadsheet takes tabs from text/plain, and CSV for 'csv'.
 * HTML rides beside it only for 'html'. Blocks are kept apart: a blank line
 * in text, a table each in HTML. Nothing is refused here; the bench copies
 * any shape it is handed and lets the paste target make of it what it will.
 */
function dishClipboardContent(relation, blocks, format, opts) {
    opts = opts || {};
    var headers = opts.headers !== false;
    var delim = (format === "csv") ? "," : "\t";
    var textParts = [], htmlParts = [];
    for (var b = 0; b < blocks.length; b++) {
        var block = blocks[b], lines = [], row, c;
        if (headers) {
            row = [];
            for (c = 0; c < block.columns.length; c++) row.push(_wbDelimited(block.columns[c], delim));
            lines.push(row.join(delim));
        }
        for (var r = 0; r < block.pks.length; r++) {
            row = [];
            for (c = 0; c < block.columns.length; c++)
                row.push(_wbDelimited(_wbCellText(_wbCellOf(relation, block.pks[r], block.columns[c])), delim));
            lines.push(row.join(delim));
        }
        textParts.push(lines.join("\n"));
        if (format === "html") htmlParts.push(_wbHtmlTable(relation, block, headers));
    }
    return new RelGridClipboardContent(textParts.join("\n\n"),
                                       (format === "html") ? htmlParts.join("\n") : undefined);
}

function _wbBlocksCells(blocks) {
    var n = 0;
    for (var k = 0; k < blocks.length; k++) n += blocks[k].pks.length * blocks[k].columns.length;
    return n;
}

var _wbCopySeq = 0;

/**
 * The choice, handed to the grid's panel. Three formats, a header toggle, a
 * live preview of what the chosen format would write, and Cancel. Keys: T, C,
 * H choose; ← → move between the formats; Enter takes the focused one; Escape
 * cancels. Settles ONCE — content or nothing — dissolves its own branch, and
 * the grid takes the box down.
 */
function dishCopyPanel(relation, question, mask, opts) {
    opts = opts || {};
    if (!opts.branch) throw new Error("[DishClipboard] opts.branch is required: the panel's own");
    var blocks = question.blocks, cells = _wbBlocksCells(blocks);
    var b = opts.branch.createBranch("copy-" + (++_wbCopySeq));   // this session's, dissolved with it
    b.activate({ toString: function () { return "dishCopyPanel"; } });
    var mint = function (name, tag) { return b.createElement(name, tag); };

    return new Promise(function (resolve) {
        var settled = false;
        function settle(content) {
            if (settled) return;
            settled = true;
            resolve(content);
            b.dissolve();                                     // the elements go; the grid's box follows
        }
        function choose(format) {
            if (settled) return;
            var content = dishClipboardContent(relation, blocks, format, { headers: headersBox.checked });
            if (opts.onChosen) opts.onChosen(format, cells);
            settle(content);
        }

        var root = mint("root", "div");
        css.addClass(root, wb_copy);
        root.tabIndex = -1;

        var head = mint("head", "div");
        css.addClass(head, wb_copy_head);
        var title = mint("title", "span");
        css.addClass(title, wb_copy_title);
        title.textContent = "Copy " + cells + (cells === 1 ? " cell" : " cells");
        var sub = mint("sub", "span");
        css.addClass(sub, wb_copy_sub);
        sub.textContent = blocks.length + (blocks.length === 1 ? " range" : " ranges")
                        + " · " + relation.role() + "’s table · the grid is waiting";
        head.appendChild(title); head.appendChild(sub);
        root.appendChild(head);

        var opts_ = mint("formats", "div");
        css.addClass(opts_, wb_copy_opts);
        var buttons = [];
        var hint = mint("hint", "div");
        css.addClass(hint, wb_copy_hint);
        var preview = mint("preview", "pre");
        css.addClass(preview, wb_copy_preview);
        var headersBox = mint("headers", "input");
        css.addClass(headersBox, wb_copy_check);
        headersBox.type = "checkbox";
        headersBox.checked = true;
        var shown = "tsv";                              // the format the preview shows

        function showPreview(format) {
            shown = format;
            for (var i = 0; i < _WB_COPY_FORMATS.length; i++)
                if (_WB_COPY_FORMATS[i].key === format) hint.textContent = _WB_COPY_FORMATS[i].label + " — " + _WB_COPY_FORMATS[i].hint;
            var content = dishClipboardContent(relation, blocks, format, { headers: headersBox.checked });
            var body = (format === "html") ? content.html : content.text;
            var lines = body.split("\n");
            preview.textContent = lines.slice(0, 4).join("\n") + (lines.length > 4 ? "\n…" : "");
        }

        _WB_COPY_FORMATS.forEach(function (f, idx) {
            var btn = mint("format-" + f.key, "button");
            css.addClass(btn, wb_copy_opt, wb_copy_opt_hot);
            btn.type = "button";
            btn.format = f.key;                           // a property, so a test can read it back
            btn.textContent = f.label;
            btn.addEventListener("click", function () { choose(f.key); });
            btn.addEventListener("focus", function () { showPreview(f.key); });
            btn.addEventListener("mouseenter", function () { showPreview(f.key); });
            opts_.appendChild(btn);
            buttons.push(btn);
        });
        root.appendChild(opts_);
        root.appendChild(hint);
        root.appendChild(preview);

        var foot = mint("foot", "div");
        css.addClass(foot, wb_copy_foot);
        var label = mint("headers-label", "label");
        css.addClass(label, wb_copy_label);
        label.appendChild(headersBox);
        var lt = mint("headers-text", "span"); lt.textContent = "column headers";
        label.appendChild(lt);
        headersBox.addEventListener("change", function () { showPreview(shown); });
        var keys = mint("keys", "span");
        css.addClass(keys, wb_copy_keys);
        keys.textContent = "T·C·H  ←→  Enter  Esc";
        var cancel = mint("cancel", "button");
        css.addClass(cancel, wb_copy_cancel);
        cancel.type = "button";
        cancel.textContent = "Cancel";
        cancel.addEventListener("click", function () { settle(undefined); });
        foot.appendChild(label); foot.appendChild(keys); foot.appendChild(cancel);
        root.appendChild(foot);

        root.addEventListener("keydown", function (e) {
            if (e.stopPropagation) e.stopPropagation();       // ours; the grid is not listening anyway
            var key = e.key, k = (typeof key === "string") ? key.toLowerCase() : "";
            for (var i = 0; i < _WB_COPY_FORMATS.length; i++) {
                if (k === _WB_COPY_FORMATS[i].hotkey && !e.ctrlKey && !e.metaKey && !e.altKey) {
                    if (e.preventDefault) e.preventDefault();
                    choose(_WB_COPY_FORMATS[i].key);
                    return;
                }
            }
            if (key === "Escape") {
                if (e.preventDefault) e.preventDefault();
                settle(undefined);
            } else if (key === "ArrowLeft" || key === "ArrowRight") {
                if (e.preventDefault) e.preventDefault();
                var at = buttons.indexOf(document.activeElement);
                var next = (at < 0) ? 0 : (at + (key === "ArrowRight" ? 1 : buttons.length - 1)) % buttons.length;
                if (buttons[next].focus) buttons[next].focus();
                showPreview(buttons[next].format);        // the focus listener does this too; not every DOM fires it
            } else if (key === "Enter") {
                // Enter takes whatever the preview shows — which is the focused
                // format when one is focused. Handled here rather than left to the
                // button's own Enter-is-click, because a host shell that owns Enter
                // for its panes (the workbench does) may cancel that default.
                if (e.preventDefault) e.preventDefault();
                choose(shown);
            }
        });

        mask.panel(root);                                     // the grid places it in its box
        showPreview("tsv");
        if (buttons[0].focus) buttons[0].focus();
    });
}
