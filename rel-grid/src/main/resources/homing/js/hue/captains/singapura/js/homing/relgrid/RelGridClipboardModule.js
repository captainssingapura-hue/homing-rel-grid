// =============================================================================
// RelGridClipboardModule — RFC 0050 · Episode 2's stock CLIPBOARD WRITER: what the
// grid writes a copy's answer with when the host names no writer of its own.
// Two roads to the same clipboard, and never a silence.
//
//   new RelGridClipboard({ branch, navigator?, ClipboardItem?, Blob?, document? })
//       .write(content) → thenable        content: RelGridClipboardContent { text, html? }
//
// The BRANCH is the grid's: the command road needs one element, a scratch
// textarea off-screen, minted on a sub-branch made for the write and
// dissolved after it. The rest is what the page has unless the caller says
// otherwise — an explicit null is a road closed.
// =============================================================================

/**
 * The stock clipboard writer. Two roads to the same clipboard, both open only
 * while the person's activation is fresh — and it is, because the answer was
 * produced by their click on the domain's panel:
 *
 *   1. the async Clipboard API. Rich content goes as one item carrying both
 *      forms, so a spreadsheet takes the table and an editor takes the text;
 *      without an html, or without ClipboardItem, the plain form alone.
 *   2. the copy COMMAND, when the first is absent or refused. An insecure
 *      origin has no navigator.clipboard at all, and an embedded page can be
 *      denied it by policy while the command — gated on activation, not on
 *      policy — still works. Both forms ride the copy event's clipboardData.
 *
 * Neither road is a silence: a write that fails on both is a rejection, which
 * the grid records.
 */
class RelGridClipboard {
    constructor(env) {
        // What the page has, unless the caller says otherwise — an explicit null
        // is a road closed. The BRANCH is the grid's: the command road needs one
        // element, and it is minted on a sub-branch made for the write.
        var e = env || {};
        var given = function (k, page) { return e[k] !== undefined ? e[k] : page; };
        this._env = {
            navigator:     given("navigator",     (typeof navigator !== "undefined") ? navigator : null),
            ClipboardItem: given("ClipboardItem", (typeof ClipboardItem !== "undefined") ? ClipboardItem : null),
            Blob:          given("Blob",          (typeof Blob !== "undefined") ? Blob : null),
            document:      given("document",      (typeof document !== "undefined") ? document : null),
            branch:        e.branch || null
        };
    }

    /** The write: the async API first, the copy command when it is absent or refused; a rejection when both are. */
    write(content) {
        var self = this;
        return this._modern(content).then(null, function (why) {
            if (self._command(content)) return;
            throw new Error("the clipboard refused both roads — " + (why && why.message ? why.message : why));
        });
    }

    _modern(content) {
        var env = this._env, cb = env.navigator ? env.navigator.clipboard : null;
        if (!cb) return Promise.reject(new Error("navigator.clipboard is absent (not a secure context?)"));
        if (content.html != null && typeof env.ClipboardItem === "function" && typeof env.Blob === "function"
                && typeof cb.write === "function") {
            var item = new env.ClipboardItem({
                "text/plain": new env.Blob([content.text], { type: "text/plain" }),
                "text/html":  new env.Blob([content.html], { type: "text/html" })
            });
            return cb.write([item]);
        }
        if (typeof cb.writeText !== "function") return Promise.reject(new Error("navigator.clipboard cannot write"));
        return cb.writeText(content.text);
    }

    _command(content) {
        var env = this._env, doc = env.document;
        if (!doc || typeof doc.execCommand !== "function" || !doc.body || !env.branch) return false;
        var took = false;
        // The command fires a copy event; answering it is how both forms are
        // set. Captured, so nothing on the page sees a copy it did not make.
        var onCopy = function (e) {
            var data = e.clipboardData;
            if (!data || typeof data.setData !== "function") return;
            data.setData("text/plain", content.text);
            if (content.html != null) data.setData("text/html", content.html);
            if (e.preventDefault) e.preventDefault();
            took = true;
        };
        // The command needs a selection to act on; a textarea off-screen is one,
        // minted on a branch of its own for the write and dissolved after it.
        var scratch = env.branch.createBranch("copy-scratch");
        scratch.activate(this);
        var ta = scratch.createElement("scratch", "textarea");
        css.addClass(ta, hrg_scratch);                        // off-screen, by its class
        ta.textContent = content.text;
        ta.setAttribute("aria-hidden", "true");
        // Selecting the textarea takes the focus, and this runs AFTER the grid
        // has already handed the focus back to the table — so what was focused
        // is put back, or the person is left typing into nothing.
        var prev = doc.activeElement || null;
        doc.addEventListener("copy", onCopy, true);
        doc.body.appendChild(ta);
        var ran = false;
        try { ta.select(); ran = doc.execCommand("copy"); }
        catch (e) { ran = false; }
        scratch.dissolve();                                   // the textarea goes with it
        doc.removeEventListener("copy", onCopy, true);
        if (prev && prev.focus) {
            try { prev.focus({ preventScroll: true }); } catch (e) { /* gone; nothing to restore */ }
        }
        return ran && took;
    }
}
