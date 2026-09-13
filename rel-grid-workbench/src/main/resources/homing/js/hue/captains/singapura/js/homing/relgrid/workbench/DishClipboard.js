// =============================================================================
// DishClipboard — the panel a person chooses a copy's FORMAT on. DOMAIN CODE,
// and the bench's answer to the first question the grid waits for (RFC 0050 ·
// Episode 2, map 6 and ext6).
//
// The grid asked with IDENTITIES — one block per range, pks down and columns
// across — and it will write whatever comes back. What comes back is composed
// by DishClipboardFormats (pure logic: the three forms over the relation's
// own cells); what is here is the choice, drawn for the grid's panel.
//
//   dishCopyPanel(relation, question, mask, { branch, onChosen? })
//       → Promise<RelGridClipboardContent | undefined>
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

// WHAT IS WRITTEN is composed elsewhere: dishClipboardContent, in
// DishClipboardFormats — pure logic, no DOM — is what the preview shows and
// what a choice answers with. This module draws the choice, and nothing else.

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
