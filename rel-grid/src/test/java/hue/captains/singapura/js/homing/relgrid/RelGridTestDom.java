package hue.captains.singapura.js.homing.relgrid;

/**
 * The headless DOM the e2 tests run on, and the fixture relation they share.
 *
 * <p>The stub is deliberately <b>faithful</b> in the places a lenient one hid a
 * real crash: {@code removeChild} of a non-child throws, as the real DOM does;
 * moving focus fires {@code blur} on the element that loses it; and removing
 * the focused element fires its {@code blur} <i>during</i> the removal, as
 * Chromium does. {@code document.activeElement} tracks focus. Events dispatch
 * to registered listeners with {@code preventDefault} and
 * {@code stopPropagation} present, and bubble to the parent chain. Elements
 * carry a {@code style} with custom-property support and a
 * {@code getBoundingClientRect} fed by {@code _rl / _rr} so a header drag can
 * be driven headlessly; {@code document} takes listeners of its own, which is
 * where a drag listens for the pointer. Timers are <b>fake</b>: {@code setTimeout}
 * arms, nothing fires until a test calls {@code runTimers()}, so the mask's
 * delay and hold are driven rather than waited for.</p>
 */
public final class RelGridTestDom {

    private RelGridTestDom() {}

    public static final String DIR = "/homing/js/hue/captains/singapura/js/homing/relgrid/";

    /** The protocol's classes are generated, and live in their own jar. */
    public static final String PROTOCOL =
            "/homing/js/hue/captains/singapura/js/homing/relgrid/protocol/RelGridProtocolModule.js";

    public static final String[] MODULES = {
            "RelGridViewMapsModule.js", "RelGridHeaderDragModule.js", "RelGridLayoutModule.js", "RelGridCellsModule.js",
            "RelGridStockCellsModule.js", "RelGridModule.js" };

    /** The selection lives in its own module, and its own jar. */
    public static final String SELECTION =
            "/homing/js/hue/captains/singapura/js/homing/relgrid/selection/RelGridSelectionModule.js";

    /** The DomOpsParty — the real one, from homing-core-js: base first, then the levels and the root singleton. */
    public static final String[] PARTY = {
            "/homing/js/hue/captains/singapura/js/homing/core/js/DomOpsPartyBaseModule.js",
            "/homing/js/hue/captains/singapura/js/homing/core/js/DomOpsPartyModule.js" };

    public static final String DOM_STUB = """
            var __focused = null;
            function makeStyle() {
                var props = {};
                return {
                    setProperty: function (k, v) { props[k] = v; },
                    removeProperty: function (k) { delete props[k]; },
                    getPropertyValue: function (k) { return props[k] || ''; }
                };
            }
            function makeListening(target) {
                target._listeners = {};
                target.addEventListener = function (t, fn) { (this._listeners[t] = this._listeners[t] || []).push(fn); };
                target.removeEventListener = function (t, fn) {
                    var l = this._listeners[t] || [], i = l.indexOf(fn); if (i >= 0) l.splice(i, 1);
                };
                // Dispatch with bubbling, like the real thing; blur does not bubble.
                target.dispatch = function (t, ev) {
                    ev = ev || {};
                    if (!ev.target) ev.target = this;                    // as a browser sets it: where it was dispatched
                    if (!ev.preventDefault) ev.preventDefault = function () {};
                    if (!ev.stopPropagation) { ev._stopped = false; ev.stopPropagation = function () { ev._stopped = true; }; }
                    var node = this;
                    while (node && !ev._stopped) {
                        (node._listeners[t] || []).slice().forEach(function (fn) { fn(ev); });
                        if (t === 'blur') break;
                        node = node.parentNode;
                    }
                };
                return target;
            }
            function makeEl(tag) {
                var el = {
                    tagName: tag, id: "", className: "", textContent: "", value: "",
                    children: [], parentNode: null, style: makeStyle(),
                    appendChild: function (c) {
                        if (c.parentNode) c.parentNode.removeChild(c);
                        c.parentNode = this; this.children.push(c); return c;
                    },
                    // Faithful: a non-child THROWS, and removing the focused element
                    // fires blur DURING the removal, before parentNode is cleared.
                    removeChild: function (c) {
                        var i = this.children.indexOf(c);
                        if (i < 0) throw new Error("NotFoundError: the node to be removed is no longer a child of this node");
                        this.children.splice(i, 1);
                        if (__focused === c) { __focused = null; c.dispatch('blur', {}); }
                        c.parentNode = null; return c;
                    },
                    replaceChild: function (nu, old) {
                        var i = this.children.indexOf(old);
                        if (nu.parentNode) nu.parentNode.removeChild(nu);
                        this.children[i] = nu; old.parentNode = null;
                        nu.parentNode = this; return old;
                    },
                    // What a branch's dissolve calls on every element it owns.
                    remove: function () { if (this.parentNode) this.parentNode.removeChild(this); },
                    // Focus moves: the element losing it is told, as in a browser.
                    focus: function () {
                        var prev = __focused; if (prev === this) return;
                        __focused = this;
                        if (prev) prev.dispatch('blur', {});
                    },
                    select: function () {},
                    setAttribute: function () {}, getAttribute: function () { return null; },
                    // Geometry for a header drag: _rl / _rr (and _rt / _rb) set by a test; 100 x 20 otherwise.
                    getBoundingClientRect: function () {
                        var l = this._rl || 0, r = (this._rr != null) ? this._rr : l + 100;
                        var t = this._rt || 0, b = (this._rb != null) ? this._rb : t + 20;
                        return { left: l, right: r, top: t, bottom: b, width: r - l, height: b - t };
                    },
                    get firstChild() { return this.children[0] || null; },
                    get firstElementChild() { return this.children[0] || null; }
                };
                return makeListening(el);
            }
            var document = makeListening({
                head: makeEl("head"),
                body: makeEl("body"),
                createElement: function (tag) { return makeEl(tag); },
                getElementById: function (id) {
                    for (var i = 0; i < this.head.children.length; i++)
                        if (this.head.children[i].id === id) return this.head.children[i];
                    return null;
                },
                get activeElement() { return __focused; },
                parentNode: null
            });
            var console = console || { error: function () {} };
            // Fake timers. Armed here, fired by the test, in the order they were
            // due: the grid's mask delay and hold are driven, never waited for.
            var __timers = [], __tid = 0;
            function setTimeout(fn, ms) { __timers.push({ id: ++__tid, fn: fn, ms: ms || 0 }); return __tid; }
            function clearTimeout(id) {
                for (var k = 0; k < __timers.length; k++) if (__timers[k].id === id) { __timers.splice(k, 1); return; }
            }
            function runTimers() {
                var due = __timers.slice().sort(function (a, b) { return a.ms - b.ms || a.id - b.id; });
                __timers = [];
                for (var k = 0; k < due.length; k++) due[k].fn();
                return due.length;
            }
            function pendingTimers() { return __timers.length; }
            // A branch of the real party for one grid (or group) to own — what a
            // host would hand it. Activated, as a host's branch is; named uniquely.
            var __branchSeq = 0, __branchOwner = { toString: function () { return 'test host'; } };
            function testBranch() {
                var b = domOpsParty.createBranch('t' + (++__branchSeq));
                b.activate(__branchOwner);
                return b;
            }
            """;

    /** A relation with NO get: identities, columns, and a manager that owns its cells — on a branch of its own. */
    public static final String FIXTURE = """
            function fixture(opts) {
                opts = opts || {};
                var data = {
                    mapo: { ingredient: 'tofu',    calories: 480 },
                    coq:  { ingredient: 'chicken', calories: 610 },
                    fish: { ingredient: 'cod',     calories: 560 }
                };
                var cells = new Map(), asked = 0, commits = [];
                var readOnly = opts.readOnly || null;   // the relation's COLUMN constraint
                // The DOMAIN's branch: every cell's element is minted under it, never under the grid's.
                var cellsBranch = testBranch(), cellSeq = 0, mints = 0;
                var relation = {
                    pks:     function () { return Object.keys(data); },
                    columns: function () { return ['ingredient', 'calories']; },
                    cellFor: function (pk, col) {
                        asked++;
                        var k = pk + ' ' + col, c = cells.get(k);
                        if (!c) {
                            c = new RelGridTextCell({
                                branch: cellsBranch.createBranch('c' + (++cellSeq)),   // the cell's own, from the DOMAIN's branch
                                value: data[pk][col],
                                // An editable relation: the cell reports to its OWNER, and the
                                // owner decides — here, accept and set() the cell.
                                onCommit: opts.editable ? function (text) {
                                    commits.push(pk + ' ' + col + ' ' + text);
                                    relation.change(pk, col, text);
                                } : undefined
                            });
                            // The grid asks a cell for its element ONCE: counted here, as the domain would see it.
                            var element = c.cellElement.bind(c);
                            c.cellElement = function () { mints++; return element(); };
                            cells.set(k, c);
                        }
                        return c;
                    },
                    // The one optional declaration: columns the grid must never ask.
                    // Read once, at construction, and only when a test asks for it.
                    readOnlyColumns: readOnly ? function () { return readOnly.slice(); } : undefined,
                    cell: function (pk, col) { return cells.get(pk + ' ' + col) || null; },
                    // The DOMAIN changed something: it updates its own cell. Nobody tells the grid.
                    change: function (pk, col, v) {
                        data[pk][col] = v;
                        var c = cells.get(pk + ' ' + col);
                        if (c) c.set(v);
                    }
                };
                var branch = testBranch();                          // the grid's own, as a host would hand it
                var container = makeEl('div');
                var arranged = [], moves = [], started = [], ended = [], resized = [], sent = [];
                var written = [], copied = [], handles = [], edges = [];
                var grid = new RelGrid({
                    container: container, branch: branch, relation: relation,
                    onArranged:      function (k) { arranged.push(k); },
                    onCursorMoved:   function (pk, col) { moves.push(pk + ' ' + col); },
                    onControlTaken:    function (pk, col) { started.push(pk + " " + col); },
                    onControlReleased: function (pk, col) { ended.push(pk + " " + col); },
                    onColumnResized: function (col, px) { resized.push(col + ' ' + px); },
                    onCopied: function (c) { copied.push(c); },
                    onEdge: function (d) { edges.push(d); },
                    // THE CHANNEL. Every question arrives here with the mask handle; the
                    // fixture records both and, unless a test says otherwise, answers with
                    // a resolved promise — nothing for a notification, and nothing (absence)
                    // for a question.
                    ask: opts.noAsk ? undefined : function (q, mask) {
                        sent.push(q); handles.push(mask);
                        return opts.ask ? opts.ask(q, mask) : Promise.resolve();
                    },
                    // The clipboard is a recorder: what the grid would have written.
                    clipboard: opts.clipboard || { write: function (c) { written.push(c); return Promise.resolve(); } },
                    // What a header drag's guide spans, when a test names it.
                    resizeGuide: opts.resizeGuide || undefined
                });
                // Structure-aware helpers: the table is colgroup, thead, tbody.
                // container > WRAPPER > table. The wrapper is the grid's own, and is
                // where an editor overlay lives while a cell holds control.
                function wrap()    { return container.children[0]; }
                function table()   { return wrap().children[0]; }
                function overlay() { return wrap().children[1] || null; }
                // The mask is whichever wrapper child wears hrg-mask; the panel is its child.
                function mask() {
                    var w = wrap();
                    for (var k = 0; k < w.children.length; k++)
                        if ((w.children[k].className || '').split(' ').indexOf('hrg-mask') >= 0) return w.children[k];
                    return null;
                }
                function panel() { var m = mask(); return (m && m.children[0]) || null; }
                function part(tag) { var t = table(); for (var k = 0; k < t.children.length; k++) if (t.children[k].tagName === tag) return t.children[k]; return null; }
                function tbody() { return part('tbody'); }
                function headerRow() { return part('thead').children[0]; }
                function thAt(j) { return headerRow().children[j]; }
                function colWidth(j) { return part('colgroup').children[j].style.getPropertyValue('--hrg-col-w'); }
                function td(i, j) { return tbody().children[i].children[j]; }
                function cellEl(pk, col) {
                    var at = grid.viewMaps().locate(pk, col);
                    return at ? td(at.i, at.j).children[0] : null;
                }
                function key(k, mods) {
                    mods = mods || {};
                    table().dispatch('keydown', { key: k, altKey: !!mods.alt, shiftKey: !!mods.shift, ctrlKey: !!mods.ctrl });
                }
                // A click as a browser actually sends one: mousedown, mouseup, click.
                // Dispatching the click alone is not faithful — the grid arms a
                // press-drag on mousedown, and a harness that skips it hides the
                // arming from every test that uses it.
                function click(i, j, mods) {
                    mods = mods || {};
                    var ev = { shiftKey: !!mods.shift, ctrlKey: !!mods.ctrl };
                    td(i, j).dispatch('mousedown', ev);
                    document.dispatch('mouseup', {});
                    td(i, j).dispatch('click', ev);
                }
                // A press-drag, as a browser sends it: mousedown, a mousemove per
                // slot the pointer reaches, mouseup at the document, and then the
                // click the browser fires for a press and release in one slot.
                function drag(from, through, mods) {
                    mods = mods || {};
                    td(from[0], from[1]).dispatch('mousedown', { shiftKey: !!mods.shift, ctrlKey: !!mods.ctrl });
                    for (var k = 0; k < through.length; k++)
                        td(through[k][0], through[k][1]).dispatch('mousemove', {});
                    document.dispatch('mouseup', {});
                    var last = through.length ? through[through.length - 1] : from;
                    // The browser fires click only when press and release share a slot.
                    if (last[0] === from[0] && last[1] === from[1])
                        td(from[0], from[1]).dispatch('click', { shiftKey: !!mods.shift, ctrlKey: !!mods.ctrl });
                }
                // What the layout has actually painted, as 'i,j' in row-major order.
                // A class check rather than a regex: \b in a Java text block is a
                // BACKSPACE, not a word boundary, and the difference is silent.
                function painted() {
                    var out = [];
                    for (var a = 0; a < grid.viewMaps().rows(); a++) {
                        for (var b = 0; b < grid.viewMaps().cols(); b++) {
                            var parts = (td(a, b).className || '').split(' ');
                            for (var q = 0; q < parts.length; q++)
                                if (parts[q] === 'hrg-sel') { out.push(a + ',' + b); break; }
                        }
                    }
                    return out.join(' ');
                }
                return { grid: grid, relation: relation, container: container, data: data,
                         table: table, tbody: tbody, headerRow: headerRow, thAt: thAt, colWidth: colWidth,
                         td: td, cellEl: cellEl, key: key, click: click, drag: drag, painted: painted,
                         wrap: wrap, overlay: overlay, mask: mask, panel: panel,
                         sent: sent, handles: handles, written: written, copied: copied, edges: edges,
                         // The last COPY question, or null. (asked() is the cellFor count.)
                         copyAsked: function () {
                             for (var k = sent.length - 1; k >= 0; k--)
                                 if (sent[k] instanceof RelGridCopyRequested) return sent[k];
                             return null;
                         },
                         // The last view HANDOVER, or null.
                         handoverAsked: function () {
                             for (var k = sent.length - 1; k >= 0; k--)
                                 if (sent[k] instanceof RelGridViewHandover) return sent[k];
                             return null;
                         },
                         // The presented rows, as pks top to bottom.
                         rowsShown: function () {
                             var out = [];
                             for (var a = 0; a < grid.viewMaps().rows(); a++) out.push(grid.viewMaps().resolve(a, 0).pk);
                             return out.join(',');
                         },
                         // The ranges of the last selection notification, as 'i0,j0..i1,j1'.
                         told: function () {
                             for (var k = sent.length - 1; k >= 0; k--) {
                                 var q = sent[k];
                                 if (q instanceof RelGridSelectionChanged) {
                                     var out = [];
                                     for (var m = 0; m < q.ranges.length; m++) {
                                         var r = q.ranges[m];
                                         out.push(r.i0 + ',' + r.j0 + '..' + r.i1 + ',' + r.j1);
                                     }
                                     return out.join(' ');
                                 }
                             }
                             return null;
                         },
                         arranged: arranged, moves: moves, started: started, ended: ended, commits: commits, resized: resized,
                         // how many times the grid asked a cell for its element: once each, ever
                         mints: function () { return mints; },
                         branch: branch, cellsBranch: cellsBranch, asked: function () { return asked; } };
            }
            """;
}
