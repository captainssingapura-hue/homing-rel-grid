package hue.captains.singapura.js.homing.reltree;

import hue.captains.singapura.js.homing.core.util.SvgGroupContentProvider;
import hue.captains.singapura.js.homing.relgrid.RelGridTestDom;

/**
 * The tree's test harness: the grid's stub DOM and the real DomOpsParty,
 * reused in test scope, and a FIXTURE of the tree's own — a small nested
 * tree held by a domain that answers places lazily, cells on its own branch,
 * and a channel that answers unfold and fold by flipping its own fold state.
 *
 * <p>The fixture's tree: {@code a} with {@code a1} and {@code a2}, {@code a2}
 * with {@code a2x} and {@code a2y}; {@code b} a leaf; {@code c} with
 * {@code c1}. Everything closed to begin with, as a lazy tree is.</p>
 */
public final class RelTreeTestDom {

    private RelTreeTestDom() {}

    public static final String DIR = "/homing/js/hue/captains/singapura/js/homing/reltree/";

    public static final String CHANNEL  = RelGridTestDom.CHANNEL;
    public static final String PROTOCOL = RelGridTestDom.PROTOCOL;
    public static final String[] PARTY  = RelGridTestDom.PARTY;
    public static final String DOM_STUB = RelGridTestDom.DOM_STUB;

    /** The typed SVG's module, exactly as the server generates it from the assets: the caret as a string constant. */
    public static final String SVGS = String.join("\n", new SvgGroupContentProvider<>(RelTreeSvgs.INSTANCE).content());

    public static final String[] MODULES = {
            "RelTreePlacesModule.js", "RelTreeRowsModule.js", "RelTreeCellsModule.js", "RelTreeLayoutModule.js",
            "RelTreeCursorModule.js", "RelTreeChannelModule.js", "RelTreeGesturesModule.js",
            "RelTreeStockCellsModule.js", "RelTreeModule.js" };

    /** The tree's and the stock cell's handles, for every test that loads the tree's modules. */
    public static final String STYLES = RelGridTestDom.handles(RelTreeStyles.INSTANCE, RelTreeStockStyles.INSTANCE);

    /**
     * The stub has no DOMParser; the tree's caret is a typed SVG parsed through
     * one. This fake answers a bare svg element for any markup, so a row's caret
     * holds exactly one child, as it does in a browser.
     */
    public static final String DOM_PARSER = """
            class DOMParser {
                parseFromString(markup, type) { var svg = makeEl('svg'); svg._markup = markup; return { documentElement: svg }; }
            }
            """;

    public static final String FIXTURE = """
            function fixture(opts) {
                opts = opts || {};
                // The DOMAIN's tree: children by key, an open set of its own. Nothing here knows a row.
                var kids = { a: ['a1', 'a2'], a2: ['a2x', 'a2y'], c: ['c1'] };
                var all = ['a', 'a1', 'a2', 'a2x', 'a2y', 'b', 'c', 'c1'];
                var open = new Set(opts.open || []);
                function childrenOf(k) { return kids[k] || []; }
                function isOpen(k) { return open.has(k); }
                function places() { return RelTreePlaces.outline(['a', 'b', 'c'], childrenOf, isOpen); }
                var cellsBranch = hostBranch(), cells = new Map(), asked = [], mints = 0, seq = 0;
                var relation = {
                    view: function () { return opts.view ? opts.view() : places(); },
                    cellFor: function (key) {
                        if (all.indexOf(key) < 0) throw new Error('[fixture] no such node: ' + key);
                        asked.push(key);
                        var c = cells.get(key);
                        if (!c) {
                            c = new RelTreeTextCell({ branch: cellsBranch.createBranch('c' + (++seq)), text: key.toUpperCase() });
                            var element = c.cellElement.bind(c);
                            c.cellElement = function () { mints++; return element(); };
                            cells.set(key, c);
                        }
                        return c;
                    },
                    // The domain's own fold state, flipped by the domain when it answers.
                    open: function (k) { open.add(k); }, close: function (k) { open.delete(k); },
                    isOpen: isOpen, places: places,
                    cell: function (k) { return cells.get(k) || null; },
                    // The cell's element WITHOUT asking it — a peek at the stock cell's own field, so a
                    // test can compare elements without counting as a mint.
                    elementOf: function (k) { var c = cells.get(k); return c ? c._el : null; }
                };
                var branch = testBranch(), container = makeEl('div');
                var sent = [], handles = [], arranged = [], moves = [], activated = [], edges = [];
                var tree = new RelTree({
                    container: container, branch: branch, relation: relation, caret: opts.caret, folder: opts.folder,
                    onArranged:    function (k) { arranged.push(k); },
                    onCursorMoved: function (k) { moves.push(k); },
                    onActivated:   function (k) { activated.push(k); },
                    onEdge:        function (d) { edges.push(d); },
                    // THE CHANNEL. Every question and notification arrives here; the fixture
                    // records it and, unless a test says otherwise, answers an unfold or a fold
                    // by flipping its own state and answering the whole View — at once.
                    ask: opts.noAsk ? undefined : function (q, mask) {
                        sent.push(q); handles.push(mask);
                        if (opts.ask) return opts.ask(q, mask);
                        if (q instanceof RelTreeUnfold) { open.add(q.key); return Promise.resolve(new RelTreeView(places())); }
                        if (q instanceof RelTreeFold)   { open.delete(q.key); return Promise.resolve(new RelTreeView(places())); }
                        return Promise.resolve();
                    }
                });
                function wrap() { return container.children[0]; }
                function treeEl() { return wrap().children[0]; }
                function row(i) { return treeEl().children[i]; }
                function caret(i) { return row(i).children[0]; }
                function folder(i) { return opts.folder ? row(i).children[opts.caret === false ? 0 : 1] : null; }
                function cellEl(i) { return row(i).children[(opts.caret === false ? 0 : 1) + (opts.folder ? 1 : 0)] || null; }
                function mask() {
                    var w = wrap();
                    for (var k = 0; k < w.children.length; k++)
                        if ((w.children[k].className || '').split(' ').indexOf('hrt-mask') >= 0) return w.children[k];
                    return null;
                }
                function panel() { var m = mask(); return (m && m.children[0]) || null; }
                function key(k) {
                    var ev = { key: k, prevented: false }; ev.preventDefault = function () { ev.prevented = true; };
                    treeEl().dispatch('keydown', ev); return ev.prevented;
                }
                function click(i) { row(i).dispatch('click', {}); }
                function dblclick(i) { row(i).dispatch('dblclick', {}); }
                function clickCaret(i) { caret(i).dispatch('click', {}); }
                // The rows as the person sees them: 'key:depth:fold' top to bottom.
                function shown() {
                    var out = [], p = tree.places();
                    for (var i = 0; i < p.rows(); i++) out.push(p.keyAt(i) + ':' + p.depthAt(i) + ':' + p.foldAt(i));
                    return out.join(' ');
                }
                // What the rows actually hold: the caret's state as the glyph it stands for — the
                // caret is an SVG now, so its state is its class — then the cell text, then the depth.
                function glyph(i) {
                    var cls = (caret(i).className || '').split(' ');
                    return cls.indexOf('hrt-caret-leaf') >= 0 ? '' : cls.indexOf('hrt-caret-open') >= 0 ? '\u25BE' : '\u25B8';
                }
                function drawn() {
                    var out = [];
                    for (var i = 0; i < treeEl().children.length; i++) {
                        var r = row(i), c = cellEl(i);
                        out.push((opts.caret === false ? '' : glyph(i)) + (c ? c.textContent : '-') + '@' + r.style.getPropertyValue('--hrt-depth'));
                    }
                    return out.join(' ');
                }
                function current() {
                    for (var i = 0; i < treeEl().children.length; i++)
                        if ((row(i).className || '').split(' ').indexOf('hrt-current') >= 0) return i;
                    return -1;
                }
                function lastSent(type) { for (var k = sent.length - 1; k >= 0; k--) if (sent[k] instanceof type) return sent[k]; return null; }
                return { tree: tree, relation: relation, container: container, branch: branch, cellsBranch: cellsBranch,
                         wrap: wrap, treeEl: treeEl, row: row, caret: caret, folder: folder, cellEl: cellEl, mask: mask, panel: panel,
                         key: key, click: click, dblclick: dblclick, clickCaret: clickCaret,
                         shown: shown, drawn: drawn, current: current, lastSent: lastSent,
                         sent: sent, handles: handles, arranged: arranged, moves: moves, activated: activated, edges: edges,
                         asked: function () { return asked.length; }, mints: function () { return mints; } };
            }
            """;
}
