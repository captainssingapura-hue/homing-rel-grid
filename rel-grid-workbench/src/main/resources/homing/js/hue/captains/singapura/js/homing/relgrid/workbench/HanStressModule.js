// =============================================================================
// HanStress — hypothetical text over the Han Article's engine, with the header
// shown so every column resizes: the side is fixed, so a column resizes only
// its width and no row moves. A narrow column clips its glyph, a wide one has
// slack, and the merged cells and half-squares must follow. Its own article,
// in memory: nobody's, and never the bench's.
//
// THE SELF-CHECK reads the geometry the browser laid out, reaching every
// element through the party - the grid's slots and merged hosts on its branch,
// the cells on the domain's - never through a DOM lookup: every merged host
// must sit exactly over the union of the slots beneath it, and every row stay
// one side tall.
//
//   new HanStress(container, params)   params: none
//   (the rest is a BenchWidget's)
// =============================================================================

var _HAN_STRESS_SEED = "布局引擎 stress 测试：ab，cdef。\n"
                     + "「Internationalization」与 i18n（国际化）……\n"
                     + "Column widths: 24, 48, 72 px。行高随之变化。\n"
                     + "The quick brown fox 跳过 lazy dog！\n"
                     + "x 一 yz 二 abc 三 defg 四 hijkl 五。";
var _HAN_STRESS_HAN = "布局引擎测试行高随之变化汉字方格跨过宽度";
var _HAN_STRESS_MARKS = "，。！？「」";

class HanStress extends BenchWidget {
    constructor(container, params) {
        super(container, "hanStress", "Han article stress");
        var self = this, body = this.body, COLS = 9, SIDE = 48, HALF = 24;
        this._side = SIDE;
        var hint = this.el("hint", "div", wb_hint, body);
        var bar = this.el("bar", "div", wb_bar, body);
        var host = this._host = this.el("host", "div", wb_han_host, body);
        this._status = this.el("status", "div", wb_status, body);
        this._check = this.el("check", "div", wb_status, body);
        var gridB = this._gridB = this.branch.createBranch("grid");
        var domainB = this.branch.createBranch("domain");
        domainB.activate(Object.freeze({ toString: function () { return "han-stress"; } }));

        hint.textContent = "STRESS — hypothetical text over the same engine, with the header shown so every column resizes: drag a header’s right edge, or Alt+←/→ on the cursor’s column, down to 12px. The side is fixed, so a column resizes only its width and no row moves: a narrow column clips its glyph, a wide one has slack, and the merged cells and half-squares must follow. The readout below measures every merged host against the slots beneath it after each change and reports the largest drift, and that every row is still one side tall.";

        // Its own store, in memory: nobody's article, and never the bench's.
        var store = this._store = new HanStore(_HAN_STRESS_SEED);
        var relation = this._relation = new HanRelation(store, { cols: COLS, branch: domainB.createBranch("cells") });

        // The side is a number here, not the column's width: the rows hold still while the
        // columns move, which is what makes a resize a stress of the layout and not of the reader.
        host.style.setProperty("--han-side", SIDE + "px");
        this._grid = null;
        this._pending = false;
        this._grid = new RelGrid({
            container: host,
            branch: gridB,
            relation: relation,
            header: { show: true, sticky: true },          // the labels are the relation's
            columnView: relation.presentedColumns(),
            minColumnWidth: 12,
            mergedCells: true,
            onColumnResized: function () { self._afterChange(); },
            onArranged: function () { self._afterChange(); },
            label: "Han article — stress"
        });
        var grid = this._grid, squares = this._squares = {};
        for (var q = 0; q < COLS; q++) squares["c" + q] = SIDE;
        relation.narrowColumns().forEach(function (c) { squares[c] = HALF; });
        grid.setColumnWidths(squares);
        this.keysTo = grid;

        var rows = relation.rows(), shownCols = relation.presentedColumns().join(), spans = relation.spanKey();
        this._unsub = store.subscribe(function () {
            var arranged = false;
            if (relation.rows() !== rows) { rows = relation.rows(); grid.viewMaps().setRowView(relation.presented()); arranged = true; }
            var cols = relation.presentedColumns().join();
            if (cols !== shownCols) { shownCols = cols; grid.viewMaps().setColumnView(relation.presentedColumns()); arranged = true; }
            if (relation.spanKey() !== spans) { spans = relation.spanKey(); if (!arranged) grid.reapply(); }
            self._afterChange();
        });
        this._afterChange();

        var n = 0;
        var act = function (label, fn) { self.button("act" + (++n), label, function () { fn(); self._afterChange(); }, bar); };
        act("widen all +8", function () { self._eachWidth(function (c, w) { return w + 8; }); });
        act("narrow all −8", function () { self._eachWidth(function (c, w) { return w - 8; }); });
        act("random widths", function () { self._eachWidth(function (c) { var half = c === "lead" || c === "trail"; return Math.round((half ? 12 : 16) + Math.random() * (half ? 28 : 64)); }); });
        act("reset widths", function () { grid.setColumnWidths(squares); });
        // Hypothetical text, regenerated: characters and runs of random length, so the spans move
        // under the same widths.
        act("random text", function () { store.set(HanStress.randomText()); });
        act("seed text", function () { store.reset(); });
    }

    static randomText() {
        var out = [], H = _HAN_STRESS_HAN, M = _HAN_STRESS_MARKS;
        for (var line = 0; line < 4; line++) {
            var s = "";
            for (var t = 0; t < 8; t++) {
                var r = Math.random();
                if (r < 0.5) s += H.charAt(Math.floor(Math.random() * H.length));
                else if (r < 0.65) s += M.charAt(Math.floor(Math.random() * M.length));
                else { var len = 1 + Math.floor(Math.random() * 9), w = ""; for (var i = 0; i < len; i++) w += String.fromCharCode(97 + Math.floor(Math.random() * 26)); s += w; }
            }
            out.push(s);
        }
        return out.join("\n");
    }

    /** Every width changed at once - the host sized for the widths ABOUT to be held, so the grid measures the final geometry the first time. */
    _eachWidth(fn) {
        var held = this._grid.columnWidths(), next = {}, squares = this._squares, side = this._side;
        this._relation.columns().forEach(function (c) { var w = held[c] != null ? held[c] : (squares[c] || side); next[c] = fn(c, w); });
        var total = 0;
        this._relation.presentedColumns().forEach(function (c) { total += next[c]; });
        this._host.style.setProperty("--wb-han-width", (total + 2) + "px");
        this._grid.setColumnWidths(next);
    }

    /** The host is exactly as wide as the columns shown, at the widths held. */
    _sizeHost() {
        var held = this._grid.columnWidths(), w = 0, shown = this._relation.presentedColumns(), side = this._side;
        for (var k = 0; k < shown.length; k++) w += (held[shown[k]] != null ? held[shown[k]] : side);
        this._host.style.setProperty("--wb-han-width", (w + 2) + "px");
    }

    /** The elements on a branch of the grid's, by a name's pattern: reached through the party. */
    _elementsOf(branchName, pattern) {
        var b = this._gridB.getBranch(branchName);
        if (!b) return [];
        return b.listElements().filter(function (e) { return pattern.test(e.name); }).map(function (e) { return b.getElement(e.name); });
    }

    /** THE SELF-CHECK, measured after the browser has laid out. */
    _measure() {
        var tds = this._elementsOf("slots", /^td-\d+-\d+$/), hosts = this._elementsOf("merged", /^merged-\d+$/);
        var worst = 0, bad = [], side = this._side;
        var boxes = tds.map(function (td) { return td.getBoundingClientRect(); });
        // Every merged host sits exactly over the union of the slots beneath it: same top, same
        // height, left at the first slot's left, right at the last slot's right.
        hosts.forEach(function (h) {
            var r = h.getBoundingClientRect(), under = [];
            boxes.forEach(function (b) {
                var cx = (b.left + b.right) / 2, cy = (b.top + b.bottom) / 2;
                if (cx > r.left && cx < r.right && cy > r.top && cy < r.bottom) under.push(b);
            });
            if (!under.length) { bad.push("a host over no slot"); return; }
            var left = Math.min.apply(null, under.map(function (b) { return b.left; }));
            var right = Math.max.apply(null, under.map(function (b) { return b.right; }));
            var d = Math.max(Math.abs(r.left - left), Math.abs(r.right - right), Math.abs(r.top - under[0].top), Math.abs(r.height - under[0].height));
            worst = Math.max(worst, d);
            if (d > 1) bad.push(h.textContent.trim() + " off by " + d.toFixed(1) + "px");
        });
        // And the rows: one side tall, every one, whatever the columns did - each slot's square.
        var rowsOk = true, tallest = 0;
        tds.forEach(function (td) {
            var g = td.children[0];
            if (!g) return;
            var hgt = g.getBoundingClientRect().height;
            tallest = Math.max(tallest, hgt);
            if (Math.abs(hgt - side) > 0.6) rowsOk = false;
        });
        this._check.textContent = "hosts " + hosts.length + "   |   largest drift " + worst.toFixed(2) + "px"
                                + (bad.length ? "   |   OFF: " + bad.join(", ") : "   |   every host on its slots")
                                + "   |   rows " + (rowsOk ? "one side tall" : "NOT one side (tallest " + tallest.toFixed(1) + "px)");
    }

    _report() {
        var held = this._grid.columnWidths(), parts = [], r = this._relation;
        r.presentedColumns().forEach(function (c) { parts.push(c + ":" + (held[c] != null ? held[c] : "-")); });
        this._status.textContent = r.glyphs() + " glyphs in " + r.rows() + " rows   |   widths " + parts.join(" ");
    }

    _afterChange() {
        if (!this._grid) return;                       // the first arrangement runs inside the grid's constructor
        this._sizeHost();
        this._report();
        if (this._pending) return;
        this._pending = true;
        var self = this, raf = (typeof requestAnimationFrame === "function") ? requestAnimationFrame : function (f) { setTimeout(f, 16); };
        raf(function () { raf(function () { self._pending = false; if (self.alive) self._measure(); }); });
    }

    disposed() {
        if (this._unsub) { this._unsub(); this._unsub = null; }
        this._grid.destroy();
        this._relation.dispose();
    }
}
