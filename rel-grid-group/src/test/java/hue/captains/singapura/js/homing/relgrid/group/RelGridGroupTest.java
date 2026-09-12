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

    private static final String GROUP = "/homing/js/hue/captains/singapura/js/homing/relgrid/group/RelGridGroupModule.js";

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
                rows.forEach(function (r) { data[r[0]] = { ingredient: r[1], calories: r[2] }; });
                var relation = {
                    pks:     function () { return Object.keys(data); },
                    columns: function () { return ['ingredient', 'calories']; },
                    cellFor: function (pk, col) {
                        var k = pk + ' ' + col, c = cells.get(k);
                        if (!c) {
                            c = new RelGridTextCell({
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
            function fenceCell(name) {
                var f = { name: name, rendered: null, disposed: 0,
                          render: function (host) { f.rendered = host; var el = makeEl('div'); el.textContent = name; host.appendChild(el); },
                          dispose: function () { f.disposed++; } };
                return f;
            }
            // A member spec: an id, ordinary grid options over its own relation, and
            // recorders for what its own host would hear.
            function memberSpec(id, rows, extra) {
                extra = extra || {};
                var sent = [], resized = [], arranged = [], ended = [];
                var branch = { createElement: function (n, t) { return makeEl(t); } };
                var spec = {
                    id: id,
                    grid: {
                        branch: branch,
                        relation: relationOf(rows, { editable: !!extra.editable }),
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
                var group = new RelGridGroup({
                    container: container,
                    members: specs,
                    fence: trailing,
                    columnWidths: opts.columnWidths,
                    sharedHeader: opts.sharedHeader,
                    onColumnResized: function (c, px) { reports.push(c + ' ' + px); },
                    label: 'a group'
                });
                function root() { return container.children[0]; }
                function kinds() { return root().children.map(function (el) { return el.className.split(' ')[0]; }).join(' '); }
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
                return { group: group, container: container, specs: specs, trailing: trailing, reports: reports,
                         root: root, kinds: kinds, has: has, tableOf: tableOf, theadOf: theadOf, tdOf: tdOf, click: click, key: key,
                         spec: function (id) { for (var k = 0; k < specs.length; k++) if (specs[k].id === id) return specs[k]; return null; } };
            }
            """;

    @BeforeEach
    void setup() {
        js = buildContext();
        js.eval("js", RelGridTestDom.DOM_STUB);
        js.eval("js", ATTRIBUTES);
        loadModule(RelGridTestDom.PROTOCOL);
        loadModule(RelGridTestDom.SELECTION);
        for (String m : RelGridTestDom.MODULES) loadModule(RelGridTestDom.DIR + m);
        loadModule(GROUP);
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
                    // fence a, member a, fence b, member b, fence c, member c, trailing fence.
                    if (f.kinds() !== 'hrg-fence hrg-member hrg-fence hrg-member hrg-fence hrg-member hrg-fence') return false;
                    if (f.root().getAttribute('aria-label') !== 'a group') return false;
                    // Each fence was handed to its cell, and each member's box carries its id.
                    for (var k = 0; k < 3; k++) {
                        var id = 'abc'[k], spec = f.spec(id);
                        if (spec.fence.rendered !== f.group.fence(id)) return false;
                        if (f.group.fence(id).getAttribute('data-member') !== id) return false;
                        if (f.group.fence(id).children[0].textContent !== 'fence of ' + id) return false;
                        if (f.group.member(id).el().parentNode.parentNode.getAttribute('data-member') !== id) return false;
                    }
                    if (f.trailing.rendered !== f.group.fence(null) || f.group.fence(null).getAttribute('data-member') !== null) return false;
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
                    if (!/must render/.test(threw([bad]))) return false;
                    var noGrid = memberSpec('y', []); noGrid.grid = null;
                    return /has no grid options/.test(threw([noGrid]));
                })()"""), "an unfilled fence is hidden; ids are required and unique; a fence must render");
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
    void theHeaderIsTheFirstMembersAndTheRestShowNoneUnlessToldOtherwise() {
        assertTrue(evalBool("""
                (() => {
                    var f = groupFixture();
                    if (f.theadOf('a') === null || f.theadOf('b') !== null || f.theadOf('c') !== null) return false;
                    var g = groupFixture({ sharedHeader: false });
                    if (g.theadOf('a') === null || g.theadOf('b') === null || g.theadOf('c') === null) return false;
                    // A first member that hides its own header keeps it hidden: the group forces nothing on.
                    var h = groupFixture({ specs: [memberSpec('a', [['mapo', 'tofu', 480]], { header: { show: false } }), memberSpec('b', [['fish', 'cod', 560]])] });
                    return h.theadOf('a') === null && h.theadOf('b') === null;
                })()"""), "one header: the first member's; sharedHeader:false leaves every member its own");
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
                    var ta = f.tableOf('a');
                    f.group.destroy();
                    if (f.container.children.length !== 0) return false;
                    if (ta.parentNode.parentNode !== null) return false;         // the member's own destroy took its wrapper out of the box
                    // Fence cells are the domain's: not disposed by the group.
                    for (var k = 0; k < 3; k++) if (f.spec('abc'[k]).fence.disposed !== 0) return false;
                    if (f.trailing.disposed !== 0) return false;
                    // A late resize report from a dead group moves nothing and says nothing.
                    f.group.member('a').setColumnWidth('calories', 300);
                    return f.reports.length === 0;
                })()"""), "destroy takes the members and the root down, and leaves the fences to their owner");
    }
}
