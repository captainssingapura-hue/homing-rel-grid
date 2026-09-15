package hue.captains.singapura.js.homing.relgrid.group;

import hue.captains.singapura.js.homing.relgrid.RelGridTestDom;
import hue.captains.singapura.js.homing.ssjs.test.JsModuleTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * RFC 0050 · Episode 2 — the group: an ordered list of tables, each an
 * ordinary grid that does not know it is in one.
 *
 * <p>What is proved. Members are identities in order, with a fence above each
 * and one trailing, and an empty fence takes no height; every member is wired
 * exactly as it would be alone — its own ask reaches its own domain, its
 * cursor and selection are its own; the header is the first member's and
 * the rest show none unless told otherwise; a resize in any member levels
 * the siblings through their own verbs and is reported once, the group's
 * widths start every member, and a member that refused a width while locked
 * is levelled when free; a member with no rows keeps its identity and its
 * fence; and destroy destroys the members and disposes no fence.</p>
 */
class RelGridGroupTest extends JsModuleTestBase {

    private static final String GROUP_DIR = "/homing/js/hue/captains/singapura/js/homing/relgrid/group/";
    private static final String[] GROUP = { "RelGridGroupMintModule.js", "RelGridGroupWalkModule.js", "RelGridGroupModule.js" };

    /** The grid's DOM, plus real attributes: the group addresses its boxes and fences by data-member. */
    static final String ATTRIBUTES = """
            var __makeEl = makeEl;
            makeEl = function (tag) {
                var el = __makeEl(tag); el._a = {};
                el.setAttribute = function (k, v) { this._a[k] = String(v); };
                el.getAttribute = function (k) { return (this._a[k] == null) ? null : this._a[k]; };
                return el;
            };
            """;

    static final String FIXTURE = """
            function relationOf(rows, opts) {
                opts = opts || {};
                var data = {}, cells = new Map(), commits = [];
                var cellsB = hostBranch(), seq = 0;                        // the DOMAIN's branch, activated as a relation activates its own: a sub-branch per cell
                rows.forEach(function (r) { data[r[0]] = { ingredient: r[1], calories: r[2] }; });
                var relation = {
                    view:    function (intent) { return intent ? null : Object.keys(data); },
                    columns: function () { return ['ingredient', 'calories']; },
                    labels:  opts.labels ? function () { return opts.labels; } : undefined,   // law 86: the relation's
                    cellFor: function (pk, col) {
                        var k = pk + ' ' + col, c = cells.get(k);
                        if (!c) {
                            c = new RelGridTextCell({
                                branch: cellsB.createBranch('c' + (++seq)),
                                value: data[pk][col],
                                onCommit: opts.editable ? function (text) { commits.push(pk + ' ' + col + ' ' + text); data[pk][col] = text; c.set(text); } : undefined
                            });
                            cells.set(k, c);
                        }
                        return c;
                    },
                    commits: commits
                };
                return relation;
            }
            function fenceWithButton(name) {
                var f = fenceCell(name);
                var b = makeEl('button'); b.textContent = name + ' button'; f.el.appendChild(b); f.button = b;
                return f;
            }
            // A fence is a NOUN: its element is its own, asked for once; it is handed nothing.
            function fenceCell(name) {
                var el = makeEl('div'); el.textContent = name;
                var f = { name: name, el: el, asked: 0, disposed: 0, told: [],
                          fenceElement: function () { f.asked++; return el; },
                          onFolded: function (on) { f.told.push(on); },
                          dispose: function () { f.disposed++; } };
                return f;
            }
            // A member spec: an id, ordinary grid options over its own relation, and
            // recorders for what its own host would hear.
            function memberSpec(id, rows, extra) {
                extra = extra || {};
                var sent = [], resized = [], arranged = [], ended = [];
                var spec = {
                    id: id,
                    grid: {
                        relation: relationOf(rows, { editable: !!extra.editable, labels: extra.labels }),
                        ask: function (q, mask) { sent.push(q); return Promise.resolve(); },
                        onColumnResized: function (c, px) { resized.push(c + ' ' + px); },
                        onArranged: function (k) { arranged.push(k); },
                        onControlReleased: function (pk, c) { ended.push(pk + ' ' + c); },
                        clipboard: { write: function () { return Promise.resolve(); } }
                    },
                    fence: extra.fence === null ? undefined : fenceCell('fence of ' + id),
                    sent: sent, resized: resized, arranged: arranged, ended: ended
                };
                if (extra.header) spec.grid.header = extra.header;
                return spec;
            }
            function groupFixture(opts) {
                opts = opts || {};
                var container = makeEl('div'), reports = [];
                var specs = opts.specs || [
                    memberSpec('a', [['mapo', 'tofu', 480], ['coq', 'chicken', 610]]),
                    memberSpec('b', [['fish', 'cod', 560]]),
                    memberSpec('c', [['sauer', 'pork', 650], ['burger', 'beef', 780], ['carbo', 'pasta', 720]])
                ];
                var trailing = opts.trailing === undefined ? fenceCell('trailing') : opts.trailing;
                var folds = [];
                var branch = testBranch();
                var group = new RelGridGroup({
                    container: container,
                    branch: branch,
                    members: specs,
                    fence: trailing,
                    columnWidths: opts.columnWidths,
                    header: opts.header,
                    stickyHeader: opts.stickyHeader,
                    folded: opts.folded,
                    onColumnResized: function (c, px) { reports.push(c + ' ' + px); },
                    onFolded: function (id, on) { folds.push(id + (on ? ' folded' : ' unfolded')); },
                    label: 'a group'
                });
                function root() { return container.children[0]; }
                function kinds() { return root().children.map(function (el) { return el.className.split(' ')[0]; }).join(' '); }
                // The group's own header, when it has one: its box, and the table in it.
                function headerBox() { var b = root().children[0]; return (b && has(b, 'hrg-group-header')) ? b : null; }
                function headerTable() { var b = headerBox(); return b ? b.children[0].children[0] : null; }
                function has(el, c) { return (el.className || '').split(' ').indexOf(c) >= 0; }
                // container > group > (fence | member)*; member > wrap > table
                function tableOf(id) { var g = group.member(id); return g ? g.el() : null; }
                function theadOf(id) { var t = tableOf(id); for (var k = 0; k < t.children.length; k++) if (t.children[k].tagName === 'thead') return t.children[k]; return null; }
                function tdOf(id, i, j) { var t = tableOf(id); for (var k = 0; k < t.children.length; k++) if (t.children[k].tagName === 'tbody') return t.children[k].children[i].children[j]; return null; }
                function click(id, i, j) {
                    var td = tdOf(id, i, j);
                    td.dispatch('mousedown', {}); document.dispatch('mouseup', {}); td.dispatch('click', {});
                }
                function key(id, k, mods) {
                    mods = mods || {};
                    tableOf(id).dispatch('keydown', { key: k, altKey: !!mods.alt, shiftKey: !!mods.shift, ctrlKey: !!mods.ctrl });
                }
                // An arrow pressed wherever the focus is — a table, or a fence.
                function arrow(dir) {
                    var ev = { key: dir === 'down' ? 'ArrowDown' : 'ArrowUp', prevented: false };
                    ev.preventDefault = function () { ev.prevented = true; };
                    (document.activeElement || root()).dispatch('keydown', ev);
                    return ev.prevented;
                }
                function cursorOf(id) { var c = group.member(id).cursor(); return c ? c.pk + '/' + c.column : 'none'; }
                function tab(shift) {
                    var ev = { key: 'Tab', shiftKey: !!shift, prevented: false };
                    ev.preventDefault = function () { ev.prevented = true; };
                    (document.activeElement || root()).dispatch('keydown', ev);
                    return ev.prevented;
                }
                function focused() {
                    var a = document.activeElement;
                    if (!a) return 'nothing';
                    for (var k = 0; k < specs.length; k++) {
                        if (a === tableOf(specs[k].id)) return 'table ' + specs[k].id;
                        var fh = group.fence(specs[k].id);
                        for (var p = a; p; p = p.parentNode) if (p === fh) return 'fence ' + specs[k].id + (a === fh ? '' : ' control');
                    }
                    for (var q = a; q; q = q.parentNode) if (q === group.fence(null)) return 'fence trailing' + (a === group.fence(null) ? '' : ' control');
                    return 'elsewhere';
                }
                return { group: group, container: container, branch: branch, specs: specs, trailing: trailing, reports: reports, folds: folds,
                         tab: tab, arrow: arrow, cursorOf: cursorOf, focused: focused,
                         headerBox: headerBox, headerTable: headerTable,
                         boxOf: function (id) { return group.member(id).el().parentNode.parentNode; },
                         root: root, kinds: kinds, has: has, tableOf: tableOf, theadOf: theadOf, tdOf: tdOf, click: click, key: key,
                         spec: function (id) { for (var k = 0; k < specs.length; k++) if (specs[k].id === id) return specs[k]; return null; } };
            }
            """;

    @BeforeEach
    void setup() {
        js = buildContext();
        js.eval("js", RelGridTestDom.DOM_STUB);
        js.eval("js", RelGridTestDom.STYLES);
        js.eval("js", RelGridTestDom.handles(RelGridGroupStyles.INSTANCE));
        for (String m : RelGridTestDom.PARTY) loadModule(m);
        js.eval("js", ATTRIBUTES);
        loadModule(RelGridTestDom.CHANNEL);
        loadModule(RelGridTestDom.PROTOCOL);
        loadModule(RelGridTestDom.SELECTION);
        for (String m : RelGridTestDom.MODULES) loadModule(RelGridTestDom.DIR + m);
        for (String m : GROUP) loadModule(GROUP_DIR + m);
        js.eval("js", FIXTURE);
    }

    private boolean evalBool(String expr) { return js.eval("js", expr).asBoolean(); }
    private void act(String src) { js.eval("js", src); }

    @Test
    void membersAreIdentitiesInOrderWithAFenceAboveEachAndOneTrailing() {
        assertTrue(evalBool("""
                (() => {
                    var f = groupFixture();
                    if (f.group.members().join(',') !== 'a,b,c') return false;
                    // The group's header first, above everything; then fence a, member a, fence b,
                    // member b, fence c, member c, and the trailing fence.
                    if (f.kinds() !== 'hrg-group-header hrg-fence hrg-member hrg-fence hrg-member hrg-fence hrg-member hrg-fence') return false;
                    if (f.root().getAttribute('aria-label') !== 'a group') return false;
                    // Each fence's own element was placed in its slot — asked for once — and each member's box carries its id.
                    for (var k = 0; k < 3; k++) {
                        var id = 'abc'[k], spec = f.spec(id);
                        if (spec.fence.asked !== 1 || f.group.fence(id).children[0] !== spec.fence.el) return false;
                        if (f.group.fence(id).getAttribute('data-member') !== id) return false;
                        if (f.group.fence(id).children[0].textContent !== 'fence of ' + id) return false;
                        if (f.group.member(id).el().parentNode.parentNode.getAttribute('data-member') !== id) return false;
                    }
                    if (f.group.fence(null).children[0] !== f.trailing.el || f.group.fence(null).getAttribute('data-member') !== null) return false;
                    return f.group.member('nope') === null && f.group.fence('nope') === null;
                })()"""), "a group is its members in order, a fence above each and one trailing, each handed to the domain");
    }

    @Test
    void anEmptyFenceTakesNoHeightAndAnIdMustBeUniqueAndPresent() {
        assertTrue(evalBool("""
                (() => {
                    var f = groupFixture({ specs: [memberSpec('a', [['mapo', 'tofu', 480]], { fence: null }), memberSpec('b', [['fish', 'cod', 560]])], trailing: null });
                    if (!f.has(f.group.fence('a'), 'hrg-fence-empty') || f.has(f.group.fence('b'), 'hrg-fence-empty')) return false;
                    if (!f.has(f.group.fence(null), 'hrg-fence-empty')) return false;
                    var threw = function (specs) { try { groupFixture({ specs: specs }); return false; } catch (e) { return String(e); } };
                    if (!/duplicate member id/.test(threw([memberSpec('a', []), memberSpec('a', [])]))) return false;
                    if (!/has no id/.test(threw([memberSpec('', [])]))) return false;
                    var bad = memberSpec('x', []); bad.fence = { dispose: function () {} };
                    if (!/must answer fenceElement/.test(threw([bad]))) return false;
                    var worse = memberSpec('x', []); worse.fence = { fenceElement: function () { return 'not an element'; } };
                    if (!/must answer an element/.test(threw([worse]))) return false;
                    var noGrid = memberSpec('y', []); noGrid.grid = null;
                    return /has no grid options/.test(threw([noGrid]));
                })()"""), "an unfilled fence is hidden; ids are required and unique; a fence must answer its element");
    }

    @Test
    void everyMemberIsWiredAsItWouldBeAlone() {
        assertTrue(evalBool("""
                (() => {
                    var f = groupFixture();
                    var told = function (id) { return f.spec(id).sent.filter(function (q) { return q instanceof RelGridSelectionChanged; }).length; };
                    var before = { a: told('a'), b: told('b'), c: told('c') };           // each was told once, at its own construction
                    // A gesture in one member is that member's: its cursor, its selection,
                    // and its selection notification to ITS domain — nobody else hears.
                    f.click('a', 1, 0);
                    f.click('c', 2, 1);
                    var ga = f.group.member('a'), gc = f.group.member('c'), gb = f.group.member('b');
                    if (ga.cursor().pk !== 'coq' || gc.cursor().pk !== 'carbo' || gb.cursor().pk !== 'fish') return false;
                    f.key('c', 'ArrowUp', { shift: true });
                    if (gc.selectionCount() !== 1 || ga.selectionCount() !== 0) return false;
                    if (told('a') <= before.a || told('c') <= before.c || told('b') !== before.b) return false;
                    // A copy in a member asks that member's own domain, with that member's blocks.
                    if (!gc.copy()) return false;
                    var q = f.spec('c').sent[f.spec('c').sent.length - 1];
                    if (!(q instanceof RelGridCopyRequested) || q.blocks[0].pks.join(',') !== 'burger,carbo') return false;
                    if (f.spec('a').sent.some(function (x) { return x instanceof RelGridCopyRequested; })) return false;
                    // And a handover, likewise — and two questions may be out at once, one
                    // per member, because each member's lock is its own.
                    if (!ga.handoverView()) return false;
                    var h = f.spec('a').sent[f.spec('a').sent.length - 1];
                    if (!(h instanceof RelGridViewHandover) || !gc.isPending() || !ga.isPending()) return false;
                    return !gb.isPending() && gb.copy() === true;
                })()"""), "cursor, selection, copy and handover are each member's own, reaching its own domain");
    }

    @Test
    void oneHeaderIsTheGroupsOwnTableAboveEveryFenceAndEachIsEveryMembersOwn() {
        assertTrue(evalBool("""
                (() => {
                    var f = groupFixture();
                    // 'group' (the default): a header table of the group's own at the top, presenting
                    // nothing, with the first member's labels; no member shows one.
                    var ht = f.headerTable();
                    if (!ht || ht.tagName !== 'table') return false;
                    var thead = null, tbody = null;
                    for (var k = 0; k < ht.children.length; k++) { if (ht.children[k].tagName === 'thead') thead = ht.children[k]; if (ht.children[k].tagName === 'tbody') tbody = ht.children[k]; }
                    if (!thead || thead.children[0].children.length !== 2 || tbody.children.length !== 0) return false;
                    if (thead.children[0].children[0].textContent !== 'ingredient') return false;
                    if (f.theadOf('a') !== null || f.theadOf('b') !== null || f.theadOf('c') !== null) return false;
                    // Labels come from the first member.
                    var l = groupFixture({ specs: [memberSpec('a', [['mapo', 'tofu', 480]], { labels: { ingredient: 'Dish' } }), memberSpec('b', [['fish', 'cod', 560]])] });
                    var lt = l.headerTable(), lthead = null;
                    for (var q = 0; q < lt.children.length; q++) if (lt.children[q].tagName === 'thead') lthead = lt.children[q];
                    if (lthead.children[0].children[0].textContent !== 'Dish') return false;
                    // 'each': no table of the group's; every member keeps its own header option.
                    var g = groupFixture({ header: 'each' });
                    if (g.headerBox() !== null) return false;
                    if (g.theadOf('a') === null || g.theadOf('b') === null || g.theadOf('c') === null) return false;
                    var h = groupFixture({ header: 'each', specs: [memberSpec('a', [['mapo', 'tofu', 480]], { header: { show: false } }), memberSpec('b', [['fish', 'cod', 560]])] });
                    if (h.theadOf('a') !== null || h.theadOf('b') === null) return false;
                    // Anything else is a mistake.
                    try { groupFixture({ header: 'first' }); return false; } catch (e) { return /'group' or 'each'/.test(String(e)); }
                })()"""), "header:'group' is one table of the group's above every fence; header:'each' is every member's own");
    }

    @Test
    void aStickyGroupHeaderIsTheBoxAndEveryMemberRevealsClearOfIt() {
        assertTrue(evalBool("""
                (() => {
                    var f = groupFixture({ stickyHeader: true }), box = f.headerBox();
                    // The BOX sticks, not the header's cells: a cell sticks only within its own
                    // table, and that table is one row tall. Members show no header at all.
                    if (!box || !f.has(box, 'hrg-group-header-sticky')) return false;
                    var ht = f.headerTable(), th = null;
                    for (var k = 0; k < ht.children.length; k++) if (ht.children[k].tagName === 'thead') th = ht.children[k].children[0].children[0];
                    if (css.hasClass(th, hrg_sticky)) return false;
                    if (!groupFixture().has(groupFixture().headerBox(), 'hrg-group-header') || groupFixture().has(groupFixture().headerBox(), 'hrg-group-header-sticky')) return false;
                    if (groupFixture({ header: 'each', stickyHeader: true }).headerBox() !== null) return false;   // 'each': the group adds nothing
                    // 'each': no header sticks, whatever a member's own spec says — the group's
                    // header sticks, or none does, so no header is ever stuck to a table scrolling away.
                    var e = groupFixture({ header: 'each', stickyHeader: true,
                                           specs: [memberSpec('a', [['mapo', 'tofu', 480]], { header: { sticky: true } }), memberSpec('b', [['fish', 'cod', 560]])] });
                    var eth = e.theadOf('a').children[0].children[0];
                    if (css.hasClass(eth, hrg_sticky) || !css.hasClass(eth, hrg_th)) return false;
                    if (e.spec('a').grid.header.sticky !== true) return false;   // the host's spec is untouched: the group amended its own copy
                    // Geometry: the group's container scrolls; the header box is 30px tall; member
                    // a's rows sit at 200 + 20i less what is scrolled. Scrolled by hand to 120, row 0
                    // is 80..100 — twenty above the port, and thirty more under the stuck header.
                    // Up from row 1 to it must clear both: a plain group would move 20, this 50.
                    var port = f.container;
                    port.scrollTop = 0; port.scrollLeft = 0; port.clientHeight = 200; port.clientWidth = 300;
                    port.scrollHeight = 2000; port.scrollWidth = 300;
                    port.getBoundingClientRect = function () { return { top: 100, bottom: 300, left: 0, right: 300, width: 300, height: 200 }; };
                    box._rt = 0; box._rb = 30;
                    for (var i = 0; i < 2; i++) for (var j = 0; j < 2; j++) (function (td, i) {
                        td.getBoundingClientRect = function () { var top = 200 + 20 * i - port.scrollTop; return { top: top, bottom: top + 20, left: 0, right: 100, width: 100, height: 20 }; };
                    })(f.tdOf('a', i, j), i);
                    f.click('a', 1, 0); f.tableOf('a').focus();
                    port.scrollTop = 120;
                    f.key('a', 'ArrowUp');
                    return port.scrollTop === 70 && f.cursorOf('a') === 'mapo/ingredient';    // 80 - (100 + 30) = -50
                })()"""), "stickyHeader sticks the group's header box, and a member's cursor is revealed clear of it");
    }

    @Test
    void oneHeaderMeansOneColumnListAndADragOnItLevelsEveryMember() {
        assertTrue(evalBool("""
                (() => {
                    // A member declaring other columns cannot sit under one header.
                    var odd = memberSpec('z', []);
                    odd.grid.relation.columns = function () { return ['ingredient', 'kcal']; };
                    try { groupFixture({ specs: [memberSpec('a', [['mapo', 'tofu', 480]]), odd] }); return false; }
                    catch (e) { if (!/one header means one column list/.test(String(e))) return false; }
                    // Under 'each' it may: nothing is shared but widths, which drop what a member lacks.
                    var ok = groupFixture({ header: 'each', specs: [memberSpec('a', [['mapo', 'tofu', 480]]), odd] });
                    if (ok.group.members().join(',') !== 'a,z') return false;
                    // A drag on the group's header is a resize of every member, reported once.
                    var f = groupFixture({ columnWidths: { ingredient: 120, calories: 100 } });
                    var ht = f.headerTable(), thead = null;
                    for (var k = 0; k < ht.children.length; k++) if (ht.children[k].tagName === 'thead') thead = ht.children[k];
                    var th = thead.children[0].children[1]; th._rl = 300; th._rr = 400;
                    var handle = th.children[th.children.length - 1];
                    if (handle.className !== 'hrg-resize-handle') return false;
                    handle.dispatch('mousedown', { clientX: 398 });
                    document.dispatch('mousemove', { clientX: 448 });                // +50 from the held 100
                    document.dispatch('mouseup', {});
                    var held = function (id) { return f.group.member(id).columnWidth('calories'); };
                    if (held('a') !== 150 || held('b') !== 150 || held('c') !== 150) return false;
                    if (f.reports.join('|') !== 'calories 150' || f.group.columnWidths().calories !== 150) return false;
                    // And the other way: a member's resize levels the header too.
                    f.group.member('c').setColumnWidth('ingredient', 90);
                    var hcol = ht.children[0].children[0];                        // colgroup > col
                    return hcol.style.getPropertyValue('--hrg-col-w') === '90px' && f.reports.length === 2;
                })()"""), "the group's header shares the members' column list, and its drag is everyone's resize");
    }

    @Test
    void aResizeInAnyMemberLevelsTheSiblingsAndIsReportedOnce() {
        assertTrue(evalBool("""
                (() => {
                    var f = groupFixture();
                    // Through the member's own verb — the header drag lands here too.
                    if (!f.group.member('b').setColumnWidth('calories', 200)) return false;
                    var held = function (id) { return f.group.member(id).columnWidth('calories'); };
                    if (held('a') !== 200 || held('b') !== 200 || held('c') !== 200) return false;
                    // The group reported ONCE; every member's own host heard its own report once.
                    if (f.reports.join('|') !== 'calories 200') return false;
                    if (f.spec('a').resized.join('|') !== 'calories 200' || f.spec('b').resized.join('|') !== 'calories 200' || f.spec('c').resized.join('|') !== 'calories 200') return false;
                    if (f.group.columnWidths().calories !== 200) return false;
                    // A bounded request is levelled as bounded — the members bound, the group holds what they accepted.
                    f.group.member('a').setColumnWidth('ingredient', 5);
                    if (held('c') !== 200 || f.group.member('c').columnWidth('ingredient') !== 40) return false;
                    if (f.group.columnWidths().ingredient !== 40) return false;
                    // The same width again is nothing: no report.
                    f.group.member('c').setColumnWidth('calories', 200);
                    return f.reports.length === 2;
                })()"""), "a resize anywhere is every member's, through their own verbs, and the group reports once");
    }

    @Test
    void theGroupsWidthsStartEveryMemberAndALockedMemberIsLevelledWhenFree() {
        act("""
                var W = groupFixture({ columnWidths: { ingredient: 150 }, specs: [
                    memberSpec('a', [['mapo', 'tofu', 480]]),
                    memberSpec('b', [['fish', 'cod', 560]], { editable: true })
                ]});
                """);
        assertTrue(evalBool("""
                (() => {
                    var held = function (id, c) { return W.group.member(id).columnWidth(c); };
                    if (held('a', 'ingredient') !== 150 || held('b', 'ingredient') !== 150) return false;
                    if (W.reports.length !== 0) return false;                    // the group's own widths are not a report
                    // b goes deep; a resize in a cannot move b's column under its editor.
                    W.click('b', 0, 0);
                    if (!W.group.member('b').takeControlAtCursor() || !W.group.member('b').isDeep()) return false;
                    W.group.member('a').setColumnWidth('ingredient', 220);
                    if (held('a', 'ingredient') !== 220 || held('b', 'ingredient') !== 150) return false;
                    if (W.group.columnWidths().ingredient !== 220) return false;   // the group holds what was asked and accepted by a
                    // b ends its edit: on its own release report it is brought level.
                    var editor = W.group.member('b').el().parentNode.children[1].children[0];   // the overlay's input
                    editor.dispatch('keydown', { key: 'Escape' });
                    return true;
                })()"""), "the group's widths start every member; a deep member refuses and is not moved under its editor");
        assertTrue(evalBool("""
                (() => {
                    if (W.group.member('b').isDeep()) return false;
                    return W.group.member('b').columnWidth('ingredient') === 220 && W.reports.join('|') === 'ingredient 220';
                })()"""), "and is levelled the moment it is free, without a second report");
    }

    @Test
    void groupSetColumnWidthsAppliesToEveryMemberAndHoldsWhatTheyAccepted() {
        assertTrue(evalBool("""
                (() => {
                    var f = groupFixture();
                    f.group.setColumnWidths({ ingredient: 90, calories: 3000, nothing: 50 });
                    var held = function (id, c) { return f.group.member(id).columnWidth(c); };
                    if (held('a', 'ingredient') !== 90 || held('c', 'ingredient') !== 90) return false;
                    if (held('b', 'calories') !== 2000) return false;               // bounded by the member
                    var snap = f.group.columnWidths();
                    if (snap.ingredient !== 90 || snap.calories !== 2000 || 'nothing' in snap) return false;   // drift dropped
                    return f.reports.length === 0;                                 // the host asked; nothing to report
                })()"""), "the group's setColumnWidths is every member's, bounded by them, unknown columns dropped");
    }

    @Test
    void aMemberWithNoRowsKeepsItsIdentityAndItsFence() {
        assertTrue(evalBool("""
                (() => {
                    var f = groupFixture({ specs: [
                        memberSpec('poem', [['r0', '月', 1]]),
                        memberSpec('moon', []),                                 // nothing to present
                        memberSpec('next', [['r1', '床', 2]])
                    ]});
                    if (f.group.members().join(',') !== 'poem,moon,next') return false;
                    var g = f.group.member('moon');
                    if (g.viewMaps().rows() !== 0 || g.cursor() !== null) return false;
                    if (f.group.fence('moon').children[0].textContent !== 'fence of moon') return false;
                    // Its box is there, holding an empty table; its width still levels with the rest.
                    f.group.member('poem').setColumnWidth('calories', 77);
                    return g.columnWidth('calories') === 77;
                })()"""), "an illustration is a member with nothing to present: identity, fence, geometry, no rows");
    }

    @Test
    void destroyDestroysEveryMemberAndDisposesNoFence() {
        assertTrue(evalBool("""
                (() => {
                    var f = groupFixture();
                    var ta = f.tableOf('a'), b = f.branch;
                    if (b.isOwnerAlive !== true) return false;                     // the group activated what it was handed
                    if (b.listBranches().sort().join(' ') !== 'grid-0 grid-1 grid-2 grid-header') return false;   // a sub-branch per grid
                    if (b.getBranch('grid-1').isOwnerAlive !== true) return false;   // and each member's grid its own
                    f.group.destroy();
                    if (f.container.children.length !== 0) return false;
                    if (ta.parentNode !== null || b.branchCount !== 0) return false;   // the members' branches dissolved by the group: the tables released
                    if (b.elementCount !== 1 + 4 + 3 + 1) return false;           // root, fences, boxes, header box: still the branch's, the host's to dissolve
                    // Fence cells are the domain's: not disposed by the group.
                    for (var k = 0; k < 3; k++) if (f.spec('abc'[k]).fence.disposed !== 0) return false;
                    if (f.trailing.disposed !== 0) return false;
                    // A late resize report from a dead group moves nothing and says nothing.
                    f.group.member('a').setColumnWidth('calories', 300);
                    return f.reports.length === 0;
                })()"""), "destroy takes the members and the root down, and leaves the fences to their owner");
    }
    @Test
    void foldHidesAMembersBoxAndTouchesNothingInside() {
        assertTrue(evalBool("""
                (() => {
                    var f = groupFixture();
                    var gb = f.group.member('b');
                    f.click('b', 0, 1);                                          // a cursor in b, before it folds
                    var cell = f.tdOf('b', 0, 0).children[0];
                    if (f.group.fold('b', true) !== true) return false;
                    if (!f.group.folded('b') || f.group.folded('a')) return false;
                    // The box wears the fold and the fence above it says so; the table is as it was.
                    if (!f.has(f.boxOf('b'), 'hrg-folded') || !f.has(f.group.fence('b'), 'hrg-fence-folded')) return false;
                    if (f.has(f.boxOf('a'), 'hrg-folded')) return false;
                    if (gb.cursor().pk !== 'fish' || gb.cursor().column !== 'calories') return false;
                    if (f.tdOf('b', 0, 0).children[0] !== cell || gb.viewMaps().rows() !== 1) return false;
                    // Reported once to the host, and once to the fence above it; a's fence heard nothing.
                    if (f.folds.join('|') !== 'b folded') return false;
                    if (f.spec('b').fence.told.join(',') !== 'true' || f.spec('a').fence.told.length !== 0) return false;
                    // The same again is nothing. Unfold: everything back, one report.
                    if (f.group.fold('b', true) !== false || f.folds.length !== 1) return false;
                    if (f.group.fold('b', false) !== true) return false;
                    if (f.has(f.boxOf('b'), 'hrg-folded') || f.has(f.group.fence('b'), 'hrg-fence-folded')) return false;
                    if (f.folds.join('|') !== 'b folded|b unfolded' || f.spec('b').fence.told.join(',') !== 'true,false') return false;
                    // A folded member still levels with its siblings: geometry is the group's, folded or not.
                    f.group.fold('c', true);
                    f.group.member('a').setColumnWidth('calories', 180);
                    if (f.group.member('c').columnWidth('calories') !== 180) return false;
                    try { f.group.fold('nope', true); return false; } catch (e) { return /no member/.test(String(e)); }
                })()"""), "fold hides the box and marks the fence; the table inside is untouched; each change is reported once");
    }

    @Test
    void foldAllAndFoldedAtFirst() {
        assertTrue(evalBool("""
                (() => {
                    var f = groupFixture({ folded: ['c', 'ghost'] });         // an id that is no member is drift, dropped
                    if (!f.group.folded('c') || f.group.folded('ghost') || f.group.folded('a')) return false;
                    if (!f.has(f.boxOf('c'), 'hrg-folded') || !f.has(f.group.fence('c'), 'hrg-fence-folded')) return false;
                    if (f.folds.length !== 0) return false;                     // the start is not a report
                    f.group.foldAll(true);
                    if (f.folds.join('|') !== 'a folded|b folded') return false; // c was folded already: no report
                    if (!f.group.folded('a') || !f.group.folded('b') || !f.group.folded('c')) return false;
                    f.group.foldAll(false);
                    return f.folds.join('|') === 'a folded|b folded|a unfolded|b unfolded|c unfolded'
                        && !f.has(f.boxOf('c'), 'hrg-folded');
                })()"""), "foldAll folds every member with a report per change; folded-at-first hides before anything is seen");
    }

    @Test
    void aFenceTellsTheGroupThroughTheHostAndAnUnknownTellIsRefused() {
        assertTrue(evalBool("""
                (() => {
                    var errors = [];
                    console.error = function () { errors.push(Array.prototype.slice.call(arguments).join(' ')); };
                    var f = groupFixture();
                    var fence = f.spec('b').fence;
                    // The domain's control, pressed: it TELLS, unasked, with a protocol value —
                    // through the closure the HOST wired into it, onto the group's own verb.
                    // A fence has no handle: it reads nothing of the group, and is told
                    // what it needs through onFolded.
                    var tell = function (m) { return f.group.tell(m); };          // what a host builds a fence with
                    if (fence.handle !== undefined) return false;
                    if (tell(new RelGridGroupFold('b', true)) !== true) return false;
                    if (!f.group.folded('b')) return false;
                    if (f.folds.join('|') !== 'b folded' || fence.told.join(',') !== 'true') return false;
                    // The same road from the trailing fence's control: it names any member.
                    if (tell(new RelGridGroupFold('a', true)) !== true || !f.group.folded('a')) return false;
                    // The host's tell IS the road.
                    if (f.group.tell(new RelGridGroupFold('b', false)) !== true || f.group.folded('b')) return false;
                    // A member that is not here, and a kind that is not understood: recorded and refused.
                    if (f.group.tell(new RelGridGroupFold('nope', true)) !== false) return false;
                    if (f.group.tell({ some: 'thing' }) !== false) return false;
                    if (!errors.some(function (e) { return /not here/.test(e); }) || !errors.some(function (e) { return /not understand/.test(e); })) return false;
                    // A fence without onFolded is simply not told.
                    var g = groupFixture({ specs: [memberSpec('x', [['mapo', 'tofu', 480]])] });
                    delete g.spec('x').fence.onFolded;
                    return g.group.fold('x', true) === true && g.folds.join('|') === 'x folded';
                })()"""), "tell is the channel's other direction: a fence's control reaches it through the host, and lands on the same fold");
    }
    @Test
    void oneCursorIsTheActiveMembersAndFocusMovesIt() {
        assertTrue(evalBool("""
                (() => {
                    var f = groupFixture();
                    // Every member has a cursor of its own; the group presents the first's. The
                    // others are DORMANT — painted so, and their slots inherit the transparency.
                    if (f.group.active() !== 'a') return false;
                    if (!f.has(f.boxOf('a'), 'hrg-active') || f.has(f.boxOf('b'), 'hrg-active')) return false;
                    if (f.has(f.boxOf('a'), 'hrg-dormant') || !f.has(f.boxOf('b'), 'hrg-dormant') || !f.has(f.boxOf('c'), 'hrg-dormant')) return false;
                    if (f.group.member('b').cursor() === null) return false;              // b's is there, unshown
                    // The focus arriving in a table — observed at the root, never asked of the table — moves it.
                    f.tableOf('c').dispatch('focusin', {});
                    if (f.group.active() !== 'c' || f.has(f.boxOf('a'), 'hrg-active') || !f.has(f.boxOf('c'), 'hrg-active')) return false;
                    if (!f.has(f.boxOf('a'), 'hrg-dormant') || f.has(f.boxOf('c'), 'hrg-dormant')) return false;
                    // A fence as the stop: EVERY member is dormant while it is, and the active one wakes after.
                    f.group.fence('b').dispatch('focusin', {});
                    if (!f.has(f.root(), 'hrg-on-fence') || !f.has(f.boxOf('c'), 'hrg-dormant')) return false;
                    if (!f.has(f.group.fence('b'), 'hrg-fence-cursor') || !f.has(f.group.fence('b'), 'hrg-lit')) return false;
                    f.tableOf('c').dispatch('focusin', {});
                    if (f.has(f.root(), 'hrg-on-fence') || f.has(f.boxOf('c'), 'hrg-dormant')) return false;
                    // The host's verb moves it and gives the table the focus.
                    if (f.group.activate('b') !== true) return false;
                    if (f.group.active() !== 'b' || document.activeElement !== f.tableOf('b')) return false;
                    if (f.has(f.boxOf('c'), 'hrg-active') || !f.has(f.boxOf('b'), 'hrg-active')) return false;
                    // The same again is fine; a stranger is refused.
                    return f.group.activate('b') === true && f.group.activate('nope') === false && f.group.active() === 'b';
                })()"""), "one cursor: the active member's, following the focus or the host's activate()");
    }

    @Test
    void tabWalksFencesAndTablesInOrderAndWrapsWithinTheGroup() {
        assertTrue(evalBool("""
                (() => {
                    var f = groupFixture();
                    f.group.activate('a');
                    var walk = [];
                    for (var k = 0; k < 7; k++) { if (!f.tab(false)) return false; walk.push(f.focused()); }
                    // From a's table: b's fence, b's table, c's fence, c's table, the trailing fence — then round
                    // to a's fence, a's table. Every stop consumed the key.
                    if (walk.join(' > ') !== 'fence b > table b > fence c > table c > fence trailing > fence a > table a') return false;
                    if (f.group.active() !== 'a') return false;                          // a table stop makes its member active
                    // And back, from a's table: a's fence, then round to the trailing fence.
                    var back = [];
                    for (var j = 0; j < 2; j++) { f.tab(true); back.push(f.focused()); }
                    if (back.join(' > ') !== 'fence a > fence trailing') return false;
                    // A modified Tab is not the group's.
                    var ev = { key: 'Tab', ctrlKey: true, prevented: false }; ev.preventDefault = function () { ev.prevented = true; };
                    f.tableOf('a').dispatch('keydown', ev);
                    return !ev.prevented;
                })()"""), "Tab walks fence, table, fence, table … and wraps; Shift+Tab walks back");
    }

    @Test
    void aFoldedTableAndAnUnfilledFenceAreNotStopsAndAControlIsLandedOn() {
        assertTrue(evalBool("""
                (() => {
                    var g = groupFixture({ specs: [
                        memberSpec('a', [['mapo', 'tofu', 480]], { fence: null }),          // no fence above a
                        memberSpec('b', [['fish', 'cod', 560]]),
                        memberSpec('c', [['sauer', 'pork', 650]])
                    ], trailing: null });                                                 // and none below c
                    g.group.fold('b', true);                                              // b's table is folded away
                    g.group.activate('a');
                    var walk = [];
                    for (var k = 0; k < 4; k++) { g.tab(false); walk.push(g.focused()); }
                    // a's table → b's fence (its table is folded, skipped) → c's fence → c's table → round to a's table
                    // (a has no fence above it, and there is no trailing one).
                    if (walk.join(' > ') !== 'fence b > fence c > table c > table a') return false;
                    // A fence with a control of its own is still the stop itself — it wears the
                    // cursor, the member shows none — and Enter presses the control.
                    var spec = memberSpec('x', [['mapo', 'tofu', 480]]);
                    spec.fence = fenceWithButton('x');
                    var pressed = 0;
                    var h = groupFixture({ specs: [spec, memberSpec('y', [['fish', 'cod', 560]])], trailing: null });
                    spec.fence.button.addEventListener('click', function () { pressed++; });
                    h.group.activate('y');
                    h.tab(false);                                                          // round to x's fence: the fence
                    if (h.focused() !== 'fence x' || document.activeElement !== h.group.fence('x')) return false;
                    if (!h.has(h.group.fence('x'), 'hrg-fence-cursor') || !h.has(h.root(), 'hrg-on-fence')) return false;
                    if (!h.has(h.boxOf('y'), 'hrg-active')) return false;                 // y is still the active member...
                    var ev = { key: 'Enter', prevented: false }; ev.preventDefault = function () { ev.prevented = true; };
                    h.group.fence('x').dispatch('keydown', ev);
                    if (pressed !== 1 || !ev.prevented) return false;
                    h.tab(false);                                                          // ...until x's table is entered
                    if (h.focused() !== 'table x' || h.group.active() !== 'x') return false;
                    return !h.has(h.group.fence('x'), 'hrg-fence-cursor') && !h.has(h.root(), 'hrg-on-fence');
                })()"""), "folded tables and empty fences are skipped; a fence is its own stop, wears the cursor, and Enter presses its control");
    }
    @Test
    void arrowsStepOverAnEdgeOneStopAtATimeAndNeverWrap() {
        assertTrue(evalBool("""
                (() => {
                    var f = groupFixture();
                    f.group.activate('a');
                    f.click('a', 0, 1);                                          // mapo / calories, a's top row
                    // Up from the top row: the fence above. Down from the fence: back in, first row, same column.
                    if (!f.arrow('up') || f.focused() !== 'fence a') return false;
                    if (!f.arrow('down') || f.focused() !== 'table a' || f.cursorOf('a') !== 'mapo/calories') return false;
                    // Down through a: the second row is a's own move; the next is the edge, and b's fence.
                    f.arrow('down');
                    if (f.cursorOf('a') !== 'coq/calories' || f.focused() !== 'table a') return false;
                    f.arrow('down');
                    if (f.focused() !== 'fence b' || f.group.active() !== 'a') return false;
                    // Into b from above: its first row, still in calories; b is active now.
                    f.arrow('down');
                    if (f.focused() !== 'table b' || f.group.active() !== 'b' || f.cursorOf('b') !== 'fish/calories') return false;
                    // b has one row: down is its edge at once — c's fence — and up from there enters b on its LAST row.
                    f.arrow('down');
                    if (f.focused() !== 'fence c') return false;
                    f.arrow('up');
                    if (f.focused() !== 'table b' || f.cursorOf('b') !== 'fish/calories') return false;
                    // Down to c's bottom, then the trailing fence, and no further: arrows never wrap.
                    f.arrow('down'); f.arrow('down');
                    if (f.focused() !== 'table c' || f.cursorOf('c') !== 'sauer/calories') return false;
                    f.arrow('down'); f.arrow('down'); f.arrow('down');
                    if (f.focused() !== 'fence trailing') return false;
                    if (f.arrow('down') !== false || f.focused() !== 'fence trailing') return false;
                    // Up from the trailing fence: c's LAST row. And a's fence is the top: up there goes nowhere.
                    f.arrow('up');
                    if (f.focused() !== 'table c' || f.cursorOf('c') !== 'carbo/calories') return false;
                    f.group.activate('a'); f.click('a', 0, 0);
                    f.arrow('up');
                    if (f.focused() !== 'fence a') return false;
                    return f.arrow('up') === false && f.focused() === 'fence a';
                })()"""), "up from the top row is the fence above, down from the bottom the fence below; a fence's arrow enters the next table on its near row");
    }

    @Test
    void arrowsKeepTheColumnAndSkipWhatIsNotAStop() {
        assertTrue(evalBool("""
                (() => {
                    // b folded and c's fence empty: from a's bottom, down is b's fence, then straight into c.
                    var f = groupFixture({ specs: [
                        memberSpec('a', [['mapo', 'tofu', 480], ['coq', 'chicken', 610]]),
                        memberSpec('b', [['fish', 'cod', 560]]),
                        memberSpec('c', [['sauer', 'pork', 650], ['burger', 'beef', 780]], { fence: null }),
                        memberSpec('m', [])                                      // nothing to present: never a stop
                    ]});
                    f.group.fold('b', true);
                    f.group.activate('a');
                    f.click('a', 1, 0);                                          // coq / ingredient, a's bottom row
                    f.arrow('down');
                    if (f.focused() !== 'fence b') return false;
                    f.arrow('down');                                             // b's table is folded, c's fence unfilled: c itself
                    if (f.focused() !== 'table c' || f.group.active() !== 'c' || f.cursorOf('c') !== 'sauer/ingredient') return false;
                    // c's bottom: m's fence is the next stop, m's table (no rows) never is, then the trailing fence.
                    f.arrow('down'); f.arrow('down');
                    if (f.focused() !== 'fence m') return false;
                    f.arrow('down');
                    if (f.focused() !== 'fence trailing') return false;
                    // Tab agrees about what is a stop.
                    f.tab(false);
                    if (f.focused() !== 'fence a') return false;
                    f.tab(false); f.tab(false);
                    if (f.focused() !== 'fence b') return false;
                    f.tab(false);
                    return f.focused() === 'table c';
                })()"""), "arrows and Tab walk the same stops: folded tables, empty tables and unfilled fences are none");
    }
}
