package hue.captains.singapura.js.homing.relgrid.workbench;

import hue.captains.singapura.js.homing.core.Importable;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.relgrid.RelGridModule;

import java.util.List;

/**
 * The body shared by the Han Article widgets: one {@code RelGrid} over a
 * {@code createHanRelation(hanStoreShared(), { cols: 9, editable })}, with no
 * header, its nine columns set to one width so that every cell is a square.
 * The editor and the display differ in {@code editable} and nothing else; the
 * grid is constructed identically and cannot tell them apart.
 *
 * <p>What the host does that the dish bench's host did not: it drives the
 * <b>row view</b>. A grid reads its relation's identities once, at
 * construction, and keeps them — so the relation declares a capacity of rows
 * and the host presents the prefix in use, through the view-maps seam, at
 * construction and again whenever a commit changes the row count. That is an
 * arrangement, and the one thing the relation cannot do for itself; a commit
 * that keeps the row count is a value change, and the relation's own cells
 * take it. (Sort and filter will drive the same seam through the channel in a
 * later round; today the host calls it.)</p>
 */
final class HanArticle {

    private HanArticle() {}

    /** The square's side, in pixels. The column is this wide; the cell is as tall as it is wide. */
    static final int SIDE = 48;
    static final int COLS = 9;

    static List<ModuleImports<? extends Importable>> imports() {
        return List.of(
                new ModuleImports<>(List.of(new RelGridModule.RelGrid()), RelGridModule.INSTANCE),
                new ModuleImports<>(List.of(new HanStore.hanStoreShared()), HanStore.INSTANCE),
                new ModuleImports<>(List.of(new HanRelation.createHanRelation()), HanRelation.INSTANCE),
                new ModuleImports<>(
                        List.of(new WorkbenchStyles.wb_root(), new WorkbenchStyles.wb_han_host(),
                                new WorkbenchStyles.wb_hint(), new WorkbenchStyles.wb_bar(),
                                new WorkbenchStyles.wb_btn(), new WorkbenchStyles.wb_status()),
                        WorkbenchStyles.INSTANCE));
    }

    static List<String> bodyJs(boolean editable) {
        return List.of(
                "    var EDITABLE = " + editable + ", COLS = " + COLS + ", SIDE = " + SIDE + ";",
                "    var owner = Object.freeze({ toString: function () { return 'han-' + (EDITABLE ? 'editor' : 'display'); } });",
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
                "",
                "    var cellsB = branch.createBranch('cells');",
                "    cellsB.activate(owner);",
                "",
                "    // The domain: one shared, persisted article; a relation over it that lays",
                "    // it out nine squares to a row and owns one cell per square.",
                "    var store = hanStoreShared();",
                "    var relation = createHanRelation(store, { cols: COLS, editable: EDITABLE, capacity: 200 });",
                "",
                "    hint.textContent = EDITABLE",
                "        ? 'EDITOR \\u2014 nine squares to a row, one glyph each. Click or arrow to a square (shallow); Enter to edit (deep). Type one character to replace the glyph, several to insert, none to delete; Enter commits, Escape cancels. A Chinese input method composes in the square, and its own Enter is left to it. The display follows through the store.'",
                "        : 'DISPLAY \\u2014 the same article, read-only, over the same store. It moves when the editor commits, and its grid was never told.';",
                "",
                "    var grid = new RelGrid({",
                "        container: host,",
                "        branch: cellsB,",
                "        relation: relation,",
                "        header: { show: false },",
                "        // Present the rows in use, not the capacity: the relation declares",
                "        // two hundred rows so the article may grow, and shows a handful.",
                "        rowView: relation.presented(),",
                "        label: 'Han article \\u2014 ' + (EDITABLE ? 'editor' : 'display')",
                "    });",
                "    // Squares: every column the same width. The cell makes itself as tall as",
                "    // it is wide, so this one number is the whole geometry.",
                "    var widths = {};",
                "    for (var k = 0; k < COLS; k++) widths['c' + k] = SIDE;",
                "    grid.setColumnWidths(widths);",
                "",
                "    function report() {",
                "        status.textContent = 'revision ' + store.revision()",
                "                           + '   |   ' + relation.glyphs() + ' glyphs in ' + relation.rows() + ' rows of ' + COLS + ' (capacity ' + relation.capacity() + ')'",
                "                           + '   |   cells owned ' + relation.cellCount();",
                "    }",
                "    // A row appearing or disappearing is an ARRANGEMENT; the relation cannot",
                "    // tell the grid, so the host that owns both presents the new prefix.",
                "    // Anything else is the relation's own cells moving.",
                "    var rows = relation.rows();",
                "    var unsub = store.subscribe(function () {",
                "        if (relation.rows() !== rows) { rows = relation.rows(); grid.viewMaps().setRowView(relation.presented()); }",
                "        report();",
                "    });",
                "    report();",
                "",
                "    var btnSeq = 0;",
                "    function btn(text, fn) {",
                "        var x = branch.createElement('btn' + (++btnSeq), 'button');",
                "        css.addClass(x, wb_btn);",
                "        x.textContent = text;",
                "        x.addEventListener('click', function () { fn(); report(); });",
                "        bar.appendChild(x);",
                "    }",
                "    if (EDITABLE) btn('reset to the poem', function () { store.reset(); });",
                "    btn('re-arrange (grid.reapply)', function () { grid.reapply(); });",
                "",
                "    return { root: root, setActive: function (active) {} };"
        );
    }
}
