package hue.captains.singapura.js.homing.jsonkit;

import hue.captains.singapura.js.homing.relgrid.RelGridTestDom;

/**
 * The kit's test harness — the kit's modules and handles over the tree's harness
 * (the grid's stub DOM, the real DomOpsParty, the tree's modules), loaded by
 * {@link JsonKitTestBase} — and a FIXTURE: a {@code JsonTreeView} over a value, with the rows read back
 * as a person would see them.
 *
 * <p>The stub's {@code textContent} is a field, not a walk of the children, so
 * a cell's line is read by summing its spans.</p>
 */
public final class JsonKitTestDom {

    private JsonKitTestDom() {}

    public static final String DIR = "/homing/js/hue/captains/singapura/js/homing/jsonkit/";

    public static final String[] MODULES = { "JsonNodeCellModule.js", "JsonDocumentModule.js", "JsonTreeViewModule.js" };

    /** The kit's handles, exactly as the server would emit them. */
    public static final String STYLES = RelGridTestDom.handles(JsonTreeStyles.INSTANCE);

    public static final String FIXTURE = """
            // A value the tests share: every kind, an escape, an empty container, nesting three deep.
            var SAMPLE = { name: "kit", n: 3, ok: true, none: null, list: [1, { deep: "x" }], "a/b": {}, empty: [] };

            function textOf(el) {                                  // the stub keeps textContent per element: sum the spans
                var s = el.textContent || '';
                for (var i = 0; i < el.children.length; i++) s += textOf(el.children[i]);
                return s;
            }
            function jsonFixture(opts) {
                opts = opts || {};
                var branch = testBranch(), container = makeEl('div');
                var activated = [], moves = [], arranged = [];
                var view = new JsonTreeView({
                    container: container, branch: branch,
                    value: opts.hasOwnProperty('value') ? opts.value : SAMPLE,
                    title: opts.title, openDepth: opts.openDepth, maxString: opts.maxString,
                    onActivated:   function (k) { activated.push(k); },
                    onCursorMoved: function (k) { moves.push(k); },
                    onArranged:    function (k) { arranged.push(k); }
                });
                function wrap() { return container.children[0]; }
                function treeEl() { return wrap().children[0]; }
                function row(i) { return treeEl().children[i]; }
                function caret(i) { return row(i).children[0]; }
                function cellEl(i) { return row(i).children[1] || null; }
                function glyph(i) {
                    var cls = (caret(i).className || '').split(' ');
                    return cls.indexOf('hrt-caret-leaf') >= 0 ? '' : cls.indexOf('hrt-caret-open') >= 0 ? '\\u25BE' : '\\u25B8';
                }
                // The rows as drawn: caret glyph, the cell's line, @depth — top to bottom.
                function drawn() {
                    var out = [];
                    for (var i = 0; i < treeEl().children.length; i++) {
                        var c = cellEl(i);
                        out.push(glyph(i) + (c ? textOf(c) : '-') + '@' + row(i).style.getPropertyValue('--hrt-depth'));
                    }
                    return out.join(' | ');
                }
                function shown() {                                  // the presented pointers, in order
                    var p = view._tree.places(), out = [];
                    for (var i = 0; i < p.rows(); i++) out.push(p.keyAt(i));
                    return out.join(' ');
                }
                function key(k) {
                    var ev = { key: k, prevented: false }; ev.preventDefault = function () { ev.prevented = true; };
                    treeEl().dispatch('keydown', ev); return ev.prevented;
                }
                function click(i) { row(i).dispatch('click', {}); }
                function dblclick(i) { row(i).dispatch('dblclick', {}); }
                function clickCaret(i) { caret(i).dispatch('click', {}); }
                function cellClass(i) { return (cellEl(i).className || '').split(' ').sort().join(' '); }
                function valueClass(i) { return (cellEl(i).children[2].className || '').split(' ').sort().join(' '); }
                return { view: view, branch: branch, container: container, activated: activated, moves: moves, arranged: arranged,
                         wrap: wrap, treeEl: treeEl, row: row, caret: caret, cellEl: cellEl,
                         drawn: drawn, shown: shown, key: key, click: click, dblclick: dblclick, clickCaret: clickCaret,
                         cellClass: cellClass, valueClass: valueClass };
            }
            """;
}
