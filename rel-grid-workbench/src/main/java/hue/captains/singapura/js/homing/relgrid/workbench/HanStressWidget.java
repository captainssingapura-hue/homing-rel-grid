package hue.captains.singapura.js.homing.relgrid.workbench;

import hue.captains.singapura.js.homing.core.Importable;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.relgrid.RelGridModule;
import hue.captains.singapura.js.homing.workspace.LifecycleHint;
import hue.captains.singapura.js.homing.workspace.WorkspaceWidget;

import java.util.List;

/**
 * A stress specimen for the layout engine: the Han relation over hypothetical
 * text, with the header shown so every column can be <b>resized</b> — by
 * dragging a header's edge or with Alt+arrows — and a floor low enough to make
 * squares small. The side is fixed ({@code --han-side}), so a column resizes
 * only its width and the rows never move. The content is nobody's; what is
 * under test is that the merged cells' hosts, the half-squares and the squares
 * keep their places while the geometry under them changes.
 *
 * <p>It checks itself. After every change it measures every merged host
 * against the slots beneath it — the union of the first and the last must be
 * the host, to within a pixel — and reports the largest drift; and it checks
 * that every row is still one side tall. That is the layout's claim, verified
 * where it is made rather than assumed.</p>
 */
public final class HanStressWidget extends WorkspaceWidget<WorkspaceWidget._None, HanStressWidget> {

    public static final HanStressWidget INSTANCE = new HanStressWidget();

    private HanStressWidget() {}

    private record construct() implements WorkspaceWidget._Construct<_None, HanStressWidget> {}

    @Override protected _Construct<_None, HanStressWidget> construct() { return new construct(); }
    @Override public Class<_None> paramsType() { return _None.class; }
    @Override public String title() { return "Stress"; }
    @Override public LifecycleHint lifecycleHint() { return LifecycleHint.MULTI; }

    @Override
    protected List<ModuleImports<? extends Importable>> bodyImports() {
        return List.of(
                new ModuleImports<>(List.of(new RelGridModule.RelGrid()), RelGridModule.INSTANCE),
                new ModuleImports<>(List.of(new HanStore.createHanStore()), HanStore.INSTANCE),
                new ModuleImports<>(List.of(new HanRelation.createHanRelation()), HanRelation.INSTANCE),
                new ModuleImports<>(
                        List.of(new WorkbenchStyles.wb_root(), new WorkbenchStyles.wb_han_host(),
                                new WorkbenchStyles.wb_hint(), new WorkbenchStyles.wb_bar(),
                                new WorkbenchStyles.wb_btn(), new WorkbenchStyles.wb_status()),
                        WorkbenchStyles.INSTANCE));
    }

    /** Hypothetical: runs of every length, pairs, squeezes, a run longer than a row. */
    static final String SEED =
            "布局引擎 stress 测试：ab，cdef。\n"
          + "「Internationalization」与 i18n（国际化）……\n"
          + "Column widths: 24, 48, 72 px。行高随之变化。\n"
          + "The quick brown fox 跳过 lazy dog！\n"
          + "x 一 yz 二 abc 三 defg 四 hijkl 五。";

    @Override
    protected List<String> constructBodyJs() {
        return List.of(
                "    var COLS = 9, SIDE = 48, HALF = 24;",
                "    var owner = Object.freeze({ toString: function () { return 'han-stress'; } });",
                "",
                "    var root = branch.createElement('root', 'div');",
                "    css.addClass(root, wb_root);",
                "    var hint = branch.createElement('hint', 'div');",
                "    css.addClass(hint, wb_hint);",
                "    root.appendChild(hint);",
                "    var bar = branch.createElement('bar', 'div');",
                "    css.addClass(bar, wb_bar);",
                "    root.appendChild(bar);",
                "    var host = branch.createElement('host', 'div');",
                "    css.addClass(host, wb_han_host);",
                "    root.appendChild(host);",
                "    var status = branch.createElement('status', 'div');",
                "    css.addClass(status, wb_status);",
                "    root.appendChild(status);",
                "    var check = branch.createElement('check', 'div');",
                "    css.addClass(check, wb_status);",
                "    root.appendChild(check);",
                "",
                "    // The grid's OWN branch: everything it mints is on it; dissolving it is this widget's.",
                "    var gridB = branch.createBranch('grid');",
                "    gridB.activate(owner);",
                "",
                "    hint.textContent = 'STRESS \\u2014 hypothetical text over the same engine, with the header shown so every column resizes: drag a header\\u2019s right edge, or Alt+\\u2190/\\u2192 on the cursor\\u2019s column, down to 12px. The side is fixed, so a column resizes only its width and no row moves: a narrow column clips its glyph, a wide one has slack, and the merged cells and half-squares must follow. The readout below measures every merged host against the slots beneath it after each change and reports the largest drift, and that every row is still one side tall.';",
                "",
                "    // Its own store, in memory: nobody\\u2019s article, and never the editor\\u2019s.",
                "    var store = createHanStore(" + jsString(SEED) + ", { key: null });",
                "    var relation = createHanRelation(store, { cols: COLS, capacity: 100 });",
                "    var labels = { lead: '\\u2039', trail: '\\u203a' };",
                "    for (var k = 0; k < COLS; k++) labels['c' + k] = String(k + 1);",
                "",
                "    // The side is a number here, not the column's width: the rows hold still",
                "    // while the columns move, which is what makes a resize a stress of the",
                "    // layout and not of the reader.",
                "    host.style.setProperty('--han-side', SIDE + 'px');",
                "    var grid = new RelGrid({",
                "        container: host,",
                "        branch: gridB,",
                "        relation: relation,",
                "        header: { show: true, labels: labels },",
                "        rowView: relation.presented(),",
                "        columnView: relation.presentedColumns(),",
                "        minColumnWidth: 12,",
                "        mergedCells: true,",
                "        onColumnResized: function () { afterChange(); },",
                "        onArranged: function () { afterChange(); },",
                "        label: 'Han article \\u2014 stress'",
                "    });",
                "    var squares = {};",
                "    for (var q = 0; q < COLS; q++) squares['c' + q] = SIDE;",
                "    relation.narrowColumns().forEach(function (c) { squares[c] = HALF; });",
                "    grid.setColumnWidths(squares);",
                "",
                "    // The host is exactly as wide as the columns shown, at the widths held.",
                "    function sizeHost() {",
                "        var held = grid.columnWidths(), w = 0, shown = relation.presentedColumns();",
                "        for (var k = 0; k < shown.length; k++) w += (held[shown[k]] != null ? held[shown[k]] : SIDE);",
                "        host.style.width = (w + 2) + 'px';",
                "    }",
                "",
                "    // THE SELF-CHECK. Every merged host must sit exactly over the union of the",
                "    // slots beneath it: same top, same height, left at the first slot\\u2019s left,",
                "    // right at the last slot\\u2019s right. Measured after the browser has laid out.",
                "    function measure() {",
                "        var table = grid.el(), wrap = table.parentNode;",
                "        var hosts = wrap.querySelectorAll('.hrg-merge'), worst = 0, n = 0, bad = [];",
                "        hosts.forEach(function (h) {",
                "            var r = h.getBoundingClientRect(); n++;",
                "            var under = [];",
                "            table.querySelectorAll('td.hrg-td').forEach(function (td) {",
                "                var b = td.getBoundingClientRect();",
                "                var cx = (b.left + b.right) / 2, cy = (b.top + b.bottom) / 2;",
                "                if (cx > r.left && cx < r.right && cy > r.top && cy < r.bottom) under.push(b);",
                "            });",
                "            if (!under.length) { bad.push('a host over no slot'); return; }",
                "            var left = Math.min.apply(null, under.map(function (b) { return b.left; }));",
                "            var right = Math.max.apply(null, under.map(function (b) { return b.right; }));",
                "            var d = Math.max(Math.abs(r.left - left), Math.abs(r.right - right), Math.abs(r.top - under[0].top), Math.abs(r.height - under[0].height));",
                "            worst = Math.max(worst, d);",
                "            if (d > 1) bad.push(h.textContent.trim() + ' off by ' + d.toFixed(1) + 'px');",
                "        });",
                "        // And the rows: one side tall, every one, whatever the columns did.",
                "        var rowsOk = true, tallest = 0;",
                "        table.querySelectorAll('td.hrg-td > .han-glyph').forEach(function (g) {",
                "            var hgt = g.getBoundingClientRect().height; tallest = Math.max(tallest, hgt);",
                "            if (Math.abs(hgt - SIDE) > 0.6) rowsOk = false;",
                "        });",
                "        check.textContent = 'hosts ' + n + '   |   largest drift ' + worst.toFixed(2) + 'px'",
                "                          + (bad.length ? '   |   OFF: ' + bad.join(', ') : '   |   every host on its slots')",
                "                          + '   |   rows ' + (rowsOk ? 'one side tall' : 'NOT one side (tallest ' + tallest.toFixed(1) + 'px)');",
                "    }",
                "    function report() {",
                "        var held = grid.columnWidths(), parts = [];",
                "        relation.presentedColumns().forEach(function (c) { parts.push(c + ':' + (held[c] != null ? held[c] : '-')); });",
                "        status.textContent = relation.glyphs() + ' glyphs in ' + relation.rows() + ' rows   |   widths ' + parts.join(' ');",
                "    }",
                "    var pending = false;",
                "    function afterChange() {",
                "        if (!grid) return;                       // the first arrangement runs inside the constructor",
                "        sizeHost(); report();",
                "        if (pending) return; pending = true;",
                "        var raf = (typeof requestAnimationFrame === 'function') ? requestAnimationFrame : function (f) { setTimeout(f, 16); };",
                "        raf(function () { raf(function () { pending = false; measure(); }); });",
                "    }",
                "",
                "    var rows = relation.rows(), shownCols = relation.presentedColumns().join(), spans = relation.spanKey();",
                "    store.subscribe(function () {",
                "        var arranged = false;",
                "        if (relation.rows() !== rows) { rows = relation.rows(); grid.viewMaps().setRowView(relation.presented()); arranged = true; }",
                "        var cols = relation.presentedColumns().join();",
                "        if (cols !== shownCols) { shownCols = cols; grid.viewMaps().setColumnView(relation.presentedColumns()); arranged = true; }",
                "        if (relation.spanKey() !== spans) { spans = relation.spanKey(); if (!arranged) grid.reapply(); }",
                "        afterChange();",
                "    });",
                "    afterChange();",
                "",
                "    var btnSeq = 0;",
                "    function btn(label, fn) {",
                "        var x = branch.createElement('btn' + (++btnSeq), 'button');",
                "        css.addClass(x, wb_btn);",
                "        x.textContent = label;",
                "        x.addEventListener('click', function () { fn(); afterChange(); });",
                "        bar.appendChild(x);",
                "    }",
                "    function eachWidth(fn) {",
                "        var held = grid.columnWidths(), next = {};",
                "        relation.columns().forEach(function (c) { var w = held[c] != null ? held[c] : (squares[c] || SIDE); next[c] = fn(c, w); });",
                "        // Size the host for the widths ABOUT to be held, so the grid measures the",
                "        // final geometry the first time — the report will size it again, harmlessly.",
                "        var total = 0; relation.presentedColumns().forEach(function (c) { total += next[c]; });",
                "        host.style.width = (total + 2) + 'px';",
                "        grid.setColumnWidths(next);",
                "    }",
                "    btn('widen all +8', function () { eachWidth(function (c, w) { return w + 8; }); });",
                "    btn('narrow all \\u22128', function () { eachWidth(function (c, w) { return w - 8; }); });",
                "    btn('random widths', function () { eachWidth(function (c) { var half = c === 'lead' || c === 'trail'; return Math.round((half ? 12 : 16) + Math.random() * (half ? 28 : 64)); }); });",
                "    btn('reset widths', function () { grid.setColumnWidths(squares); });",
                "    // Hypothetical text, regenerated: characters and runs of random length, so the",
                "    // spans move under the same widths.",
                "    var HAN = '\\u5e03\\u5c40\\u5f15\\u64ce\\u6d4b\\u8bd5\\u884c\\u9ad8\\u968f\\u4e4b\\u53d8\\u5316\\u6c49\\u5b57\\u65b9\\u683c\\u8de8\\u8fc7\\u5bbd\\u5ea6';",
                "    var MARK = '\\uff0c\\u3002\\uff01\\uff1f\\u300c\\u300d';",
                "    function randomText() {",
                "        var out = [];",
                "        for (var line = 0; line < 4; line++) {",
                "            var s = '';",
                "            for (var t = 0; t < 8; t++) {",
                "                var r = Math.random();",
                "                if (r < 0.5) s += HAN.charAt(Math.floor(Math.random() * HAN.length));",
                "                else if (r < 0.65) s += MARK.charAt(Math.floor(Math.random() * MARK.length));",
                "                else { var len = 1 + Math.floor(Math.random() * 9); var w = ''; for (var i = 0; i < len; i++) w += String.fromCharCode(97 + Math.floor(Math.random() * 26)); s += w; }",
                "            }",
                "            out.push(s);",
                "        }",
                "        return out.join('\\n');",
                "    }",
                "    btn('random text', function () { store.set(randomText()); });",
                "    btn('seed text', function () { store.reset(); });",
                "",
                "    return { root: root, setActive: function (active) {} };"
        );
    }

    /** A Java string as a single-quoted JS literal. */
    static String jsString(String s) {
        var sb = new StringBuilder("'");
        for (char c : s.toCharArray()) {
            if (c == '\\' || c == '\'') sb.append('\\').append(c);
            else if (c == '\n') sb.append("\\n");
            else if (c < 0x20 || c > 0x7e) sb.append(String.format("\\u%04x", (int) c));
            else sb.append(c);
        }
        return sb.append('\'').toString();
    }
}
