// =============================================================================
// HanArticleView — what the Han Article bench's editor and its displays are:
// article as rows of square slots, nine to a row, one character each, over a
// HanRelation over the widget's own copy of the bench's one article. What the
// han-article party says comes into the copy; the editor's textarea tells the
// party what it now is. Every display moves as the editor types, and no grid
// is ever told. Not joined, each shows - and the editor edits - an article of
// its own.
//
// EXACTLY TWO BRANCHES under the widget's: 'grid', handed whole to the grid;
// 'domain', the widget's to divide - here one part, 'cells', the relation's
// own. Dissolving both is the widget's.
//
//   new HanArticleView(container, params, editor)   what HanEditor and HanDisplay make
//   (their kinds declare, in Java, the type they join: han-article)
//   (the rest is a BenchWidget's)
// =============================================================================

var _HAN_COLS = 9, _HAN_SIDE = 48;

class HanArticleView extends BenchWidget {
    constructor(container, params, editor) {
        super(container, editor ? "hanEditor" : "hanDisplay", editor ? "Han article editor" : "Han article display");
        var self = this, body = this.body, COLS = _HAN_COLS, SIDE = _HAN_SIDE;
        this._editor = editor;
        this._member = null;
        this._revision = 0;
        var hint = this.el("hint", "div", wb_hint, body);
        var bar = this.el("bar", "div", wb_bar, body);
        var area = this._text = editor ? this.el("text", "textarea", wb_han_text, body) : null;
        if (area) { area.rows = 5; area.spellcheck = false; area.setAttribute("aria-label", "the article, as text"); }
        var host = this._host = this.el("host", "div", wb_han_host, body);
        this._status = this.el("status", "div", wb_status, body);
        var gridB = this.branch.createBranch("grid");
        var domainB = this.branch.createBranch("domain");
        domainB.activate(Object.freeze({ toString: function () { return "han-" + (editor ? "editor" : "display"); } }));

        // The domain: its own copy of the bench's article; a relation over it that lays it
        // out nine squares to a row and owns one cell per square.
        var store = this._store = new HanStore();
        var relation = this._relation = new HanRelation(store, { cols: COLS, branch: domainB.createBranch("cells") });

        hint.textContent = editor
            ? "EDITOR — the article as plain text above, and as it is rendered below: nine squares to a row, one character each, two punctuation marks to a square, Latin two letters to a square in one cell reaching over its squares. A closing mark that would start a line is squeezed into a half-square after the ninth; an opening bracket that would end a line leads the next from a half-square before the first. Those two columns appear only when a row uses them. Every keystroke re-flows every display on the bench; the squares themselves do not edit."
            : "DISPLAY — the same article, as the bench says it. It moves as the editor types, and its grid was never told.";

        var grid = this._grid = new RelGrid({
            container: host,
            branch: gridB,
            relation: relation,
            header: { show: false },
            // The rows in use are what the relation answers to view(); the columns
            // in use are handed as the column view, since columns are listed.
            columnView: relation.presentedColumns(),
            minColumnWidth: SIDE / 2,          // the half-squares are half a square wide
            mergedCells: true,                 // a run of Latin reaches over its squares
            label: "Han article — " + (editor ? "editor" : "display")
        });
        // Squares: every column the same width, and the half-squares half of it. The cell makes
        // itself as tall as the square is wide, so this one number is the whole geometry. The
        // host is as wide as the columns shown.
        var widths = this._widths = {};
        for (var k = 0; k < COLS; k++) widths["c" + k] = SIDE;
        relation.narrowColumns().forEach(function (c) { widths[c] = SIDE / 2; });
        grid.setColumnWidths(widths);
        this._sizeHost();
        this.keysTo = area || grid;

        // A row appearing or disappearing is an ARRANGEMENT; the relation cannot tell the grid,
        // so the host that owns both presents the new prefix. Anything else is the relation's own
        // cells moving.
        var rows = relation.rows(), shownCols = relation.presentedColumns().join(), spans = relation.spanKey();
        this._unsub = store.subscribe(function (t) {
            var arranged = false;
            if (relation.rows() !== rows) { rows = relation.rows(); grid.viewMaps().setRowView(relation.presented()); arranged = true; }
            var cols = relation.presentedColumns().join();
            if (cols !== shownCols) { shownCols = cols; grid.viewMaps().setColumnView(relation.presentedColumns()); self._sizeHost(); arranged = true; }
            // A run moving is an arrangement too: spans are read when cells are placed, so the
            // grid is asked to place them again.
            if (relation.spanKey() !== spans) { spans = relation.spanKey(); if (!arranged) grid.reapply(); }
            if (area && area.value !== t) area.value = t;     // a reset, or another editor
            self._report();
        });
        this._report();

        if (area) {
            // THE EDIT SEAM: the textarea tells the bench, live. Not while an input method is
            // composing - its intermediate text is not the article - and then once when the
            // composition ends.
            area.value = store.text();
            var composing = false;
            area.addEventListener("compositionstart", function () { composing = true; });
            area.addEventListener("compositionend", function () { composing = false; self._set(area.value); });
            area.addEventListener("input", function () { if (!composing) self._set(area.value); });
        }

        var n = 0;
        var act = function (label, fn) { self.button("act" + (++n), label, function () { fn(); self._report(); }, bar); };
        if (editor) act("reset to the poem", function () { self._reset(); });
        act("re-arrange (grid.reapply)", function () { grid.reapply(); });
    }

    joinParties(given) {
        var party = given[HAN_ARTICLE.name], self = this;
        if (!party) return [];
        this._member = party.join(this._editor ? "hanEditor" : "hanDisplay", {
            Text: function (m) { self._revision = m.revision; if (!self._store.set(m.text)) self._report(); }
        });
        this._member.tell({ kind: "CurrentRequested" });
        return [this._member];
    }

    leave() { super.leave(); this._member = null; }

    /** The article is now this: told to the bench when joined, set here alone when not. */
    _set(t) {
        if (this._member) this._member.tell({ kind: "SetText", text: String(t) });
        else this._store.set(t);
    }

    _reset() {
        if (this._member) this._member.tell({ kind: "Reset" });
        else this._store.reset();
    }

    /** The host is as wide as the columns shown, handed to its class as a number. */
    _sizeHost() {
        var w = 0, shown = this._relation.presentedColumns();
        for (var k = 0; k < shown.length; k++) w += this._widths[shown[k]];
        this._host.style.setProperty("--wb-han-width", (w + 2) + "px");
    }

    _report() {
        var r = this._relation;
        this._status.textContent = "revision " + (this._member ? this._revision : this._store.revision())
                                 + "   |   " + r.glyphs() + " glyphs in " + r.rows() + " rows of " + _HAN_COLS
                                 + "   |   cells owned " + r.cellCount();
    }

    disposed() {
        if (this._unsub) { this._unsub(); this._unsub = null; }
        this._grid.destroy();
        this._relation.dispose();
    }
}
