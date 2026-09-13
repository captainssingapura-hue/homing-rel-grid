package hue.captains.singapura.js.homing.relgrid.workbench;

import hue.captains.singapura.js.homing.relgrid.RelGridTestDom;
import hue.captains.singapura.js.homing.ssjs.test.JsModuleTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The Games Catalogue: a relation that sorts and filters FOR ITSELF, from
 * header cells of its own. The store over the real catalogue; the conditions
 * as pure logic with their judgements named; the relation and its header
 * cells without any grid — a click sorts, a shift-click adds a key, a funnel
 * opens a popover on the cell's own branch, every change tells the owner;
 * and then the grid over it, told unasked and asking again, with the header
 * cells staying put in the header slots while the rows reorder beneath them.
 * Runs on the grid's own test DOM and the real party.
 */
class GamesTest extends JsModuleTestBase {

    private static final String BENCH_DIR = RelGridTestDom.DIR + "workbench/";

    /** The typed classes the games modules import, as the server would emit them. */
    private static final String STYLES = RelGridTestDom.handles(GamesStyles.INSTANCE);

    private static final String HELPERS = """
            var PSV = %s;
            function catalogue() { return createGamesStore(PSV); }
            // A relation over the catalogue, its owner's callbacks recorded, a popover host of its own.
            function games(opts) {
                opts = opts || {};
                var host = makeEl('div'), told = [];
                var relation = createGamesRelation(opts.store || catalogue(), {
                    branch: testBranch(), menuHost: host,
                    onViewChanged: function () { told.push(relation.describe()); }
                });
                function header(col) { return relation.headerFor(col); }
                function part(col, k) { return header(col).headerElement().children[k]; }   // label, caret, order, mark, the menu's button
                function caret(col) { return part(col, 1).textContent; }
                function order(col) { return part(col, 2).textContent; }
                function mark(col) { return part(col, 3).textContent; }
                function menuBtn(col) { return part(col, 4); }
                // The menu, opened from the header's button, and its parts by name on the cell's 'menu' sub-branch.
                function open(col) { menuBtn(col).dispatch('click', {}); return header(col)._menu; }
                function menuEl(col, name) { return relation.branchOf ? null : header(col)._branch.getBranch('menu').getElement(name); }
                function rows(col) { var list = menuEl(col, 'list'), out = []; for (var i = 0; i < list.children.length; i++) out.push(list.children[i]); return out; }
                function rowOf(col, value) { var r = rows(col); for (var i = 0; i < r.length; i++) if (r[i].children[1].textContent === String(value)) return r[i]; return null; }
                return { relation: relation, host: host, told: told, header: header, caret: caret, order: order, mark: mark, menuBtn: menuBtn,
                         open: open, menuEl: menuEl, rows: rows, rowOf: rowOf,
                         shown: function (col) { return rows(col).filter(function (r) { return !css.hasClass(r, wb_gmenu_hidden); }); } };
            }
            """.formatted(HanStressWidget.jsString(GamesDataset.psv()));

    @BeforeEach
    void setup() {
        js = buildContext();
        js.eval("js", RelGridTestDom.DOM_STUB);
        js.eval("js", RelGridTestDom.STYLES);
        js.eval("js", STYLES);
        for (String m : RelGridTestDom.PARTY) loadModule(m);
        loadModule(RelGridTestDom.SELECTION);
        loadModule(RelGridTestDom.PROTOCOL);
        for (String m : RelGridTestDom.MODULES) loadModule(RelGridTestDom.DIR + m);
        loadModule(BENCH_DIR + "GamesStore.js");
        loadModule(BENCH_DIR + "GamesConditions.js");
        loadModule(BENCH_DIR + "GamesColumnMenuModule.js");
        loadModule(BENCH_DIR + "GamesHeaderCells.js");
        loadModule(BENCH_DIR + "GamesRelation.js");
        js.eval("js", HELPERS);
    }

    private boolean evalBool(String expr) { return js.eval("js", expr).asBoolean(); }

    @Test
    void theStoreReadsTheWholeCatalogueWithNumbersAsNumbersAndBlanksAsNull() {
        assertTrue(evalBool("""
                (() => {
                    var s = catalogue();
                    if (s.size() !== %d || s.pks()[0] !== 'g001') return false;
                    if (s.columns().join() !== 'title,series,year,platform,type,developer,publisher,sales,score') return false;
                    if (s.kind('year') !== 'number' || s.kind('platform') !== 'set' || s.kind('title') !== 'text') return false;
                    if (s.get('g001', 'title') !== 'Super Mario World' || s.get('g001', 'year') !== 1990 || s.get('g001', 'sales') !== 20.6) return false;
                    if (s.get('g001', 'score') !== null) return false;                     // blank before Metacritic: null, not ''
                    if (s.get('g098', 'score') !== 99) return false;                       // Ocarina of Time
                    if (s.get('nope', 'title') !== undefined) return false;
                    var types = s.distinct('type');
                    if (types[0].value !== 'Sports' || types[0].count < 100) return false;   // the annuals, as a real catalogue has them
                    var fifa = s.pks().filter(function (pk) { return s.get(pk, 'series') === 'FIFA'; });
                    return fifa.length === 28;                                              // one a year, 1993 to 2020
                })()""".formatted(GamesDataset.rows())), "the catalogue parses whole: numbers as numbers, blanks as null, the annuals a row a year");
    }

    @Test
    void theConditionsAreDataAndTheJudgementsAreTheOnesNamed() {
        assertTrue(evalBool("""
                (() => {
                    var s = catalogue(), v = function (pk, c) { return s.get(pk, c); }, k = function (c) { return s.kind(c); };
                    var c = gamesConditions();
                    if (c.sort.length !== 0 || Object.keys(c.filters).length !== 0) return false;
                    // A click cycles none → asc → desc → none, and not additive it is the only key.
                    var c1 = gamesToggleSort(c, 'year', false);
                    if (c1.sort.length !== 1 || c1.sort[0].column !== 'year' || c1.sort[0].dir !== 'asc' || c.sort.length !== 0) return false;
                    var c2 = gamesToggleSort(c1, 'year', false);
                    if (c2.sort[0].dir !== 'desc') return false;
                    if (gamesToggleSort(c2, 'year', false).sort.length !== 0) return false;
                    var c3 = gamesToggleSort(c1, 'score', false);
                    if (c3.sort.length !== 1 || c3.sort[0].column !== 'score') return false;
                    // Additive: joins after what is there; cycling where it already is keeps its place.
                    var c4 = gamesToggleSort(c1, 'title', true);
                    if (c4.sort.map(function (x) { return x.column + ':' + x.dir; }).join() !== 'year:asc,title:asc') return false;
                    var c5 = gamesToggleSort(c4, 'year', true);
                    if (c5.sort.map(function (x) { return x.column + ':' + x.dir; }).join() !== 'year:desc,title:asc') return false;
                    if (gamesToggleSort(c5, 'year', true).sort.map(function (x) { return x.column; }).join() !== 'title') return false;
                    if (gamesSortOf(c5, 'title').index !== 1 || gamesSortOf(c5, 'sales') !== null) return false;
                    // A menu sets a direction outright: not additive, the only key (or none); additive, it joins or turns in place.
                    if (gamesSetSort(c5, 'sales', 'desc', false).sort.map(function (x) { return x.column + ':' + x.dir; }).join() !== 'sales:desc') return false;
                    if (gamesSetSort(c5, 'sales', 'desc', true).sort.map(function (x) { return x.column + ':' + x.dir; }).join() !== 'year:desc,title:asc,sales:desc') return false;
                    if (gamesSetSort(c5, 'year', 'asc', true).sort.map(function (x) { return x.column + ':' + x.dir; }).join() !== 'year:asc,title:asc') return false;
                    if (gamesSetSort(c5, 'year', null, true).sort.map(function (x) { return x.column; }).join() !== 'title') return false;
                    if (gamesSetSort(c5, 'year', null, false).sort.length !== 0) return false;
                    // The View: filtered, then sorted; absent LAST either way; ties in base order.
                    var byScoreDesc = gamesApply(gamesToggleSort(gamesToggleSort(c, 'score', false), 'score', false), s.pks(), v, k);
                    if (v(byScoreDesc[0], 'score') !== 99) return false;                                  // Ocarina first
                    if (v(byScoreDesc[byScoreDesc.length - 1], 'score') !== null) return false;         // the unrated last
                    var byScoreAsc = gamesApply(gamesToggleSort(c, 'score', false), s.pks(), v, k);
                    if (v(byScoreAsc[byScoreAsc.length - 1], 'score') !== null || v(byScoreAsc[0], 'score') === null) return false;
                    var byYear = gamesApply(c1, s.pks(), v, k);
                    if (byYear[0] !== 'g001' || v(byYear[byYear.length - 1], 'year') !== 2020) return false;    // stable: g001 keeps its place among 1990
                    // Filters: contains (case aside), a range either end open, any of; absence never passes.
                    var f = gamesSetFilter(c, 'series', { contains: 'fifa' });
                    if (gamesApply(f, s.pks(), v, k).length !== 28) return false;
                    var f2 = gamesSetFilter(f, 'year', { min: 2010, max: null });
                    if (gamesApply(f2, s.pks(), v, k).length !== 11) return false;                       // FIFA 11 to FIFA 21
                    var f3 = gamesSetFilter(c, 'type', { in: ['Shooter', 'Racing'] });
                    var n = gamesApply(f3, s.pks(), v, k).length;
                    if (n !== s.distinct('type').filter(function (d) { return d.value === 'Shooter' || d.value === 'Racing'; }).reduce(function (a, d) { return a + d.count; }, 0)) return false;
                    var f4 = gamesSetFilter(c, 'score', { min: 0, max: 100 });
                    if (gamesApply(f4, s.pks(), v, k).length !== s.size() - s.pks().filter(function (pk) { return v(pk, 'score') === null; }).length) return false;
                    if (Object.keys(gamesSetFilter(f, 'series', null).filters).length !== 0) return false;
                    // Values chosen on a text column, and on a number column beside a range.
                    var f5 = gamesSetFilter(c, 'title', { in: ['Doom', 'Doom II', 'Doom 3'] });
                    if (gamesApply(f5, s.pks(), v, k).length !== 3) return false;
                    var f6 = gamesSetFilter(c, 'year', { min: 1998, max: 1999, in: [1998] });
                    if (gamesApply(f6, s.pks(), v, k).some(function (pk) { return v(pk, 'year') !== 1998; })) return false;
                    // Described, for a status line.
                    if (gamesDescribe(c) !== 'as catalogued') return false;
                    return gamesDescribe(gamesSetFilter(c5, 'type', { in: ['RPG'] })) === 'sorted by year \\u2193, then title \\u2191; where type in {RPG}';
                })()"""), "conditions are values; absent sorts last either way; filters as named");
    }

    @Test
    void theColumnMenuSortsAtOnceAndFiltersStagedUntilOkAndTellsTheOwner() {
        assertTrue(evalBool("""
                (() => {
                    var g = games(), r = g.relation, N = catalogue().size();
                    if (r.view().length !== N || r.view({ by: 1 }) !== null) return false;   // a catalogue is the whole of itself
                    if (typeof r.pks !== 'undefined' || r.readOnlyColumns().length !== 9 || r.labels().sales !== 'sales (M)') return false;
                    // A header cell per column, once, on its own sub-branch: a label, an indication, one button.
                    var h = g.header('year');
                    if (h !== r.headerFor('year') || r.headerCount() !== 1) return false;
                    var el = h.headerElement();
                    if (el.children.length !== 5 || el.children[0].textContent !== 'year' || el.children[0].tagName !== 'span') return false;
                    if (g.caret('year') !== '' || g.mark('year') !== '' || g.menuBtn('year').tagName !== 'button') return false;
                    // The label is an indication: a click on it does nothing at all.
                    el.children[0].dispatch('click', {}); el.children[1].dispatch('click', {});
                    if (g.told.length !== 0 || r.conditions().sort.length !== 0) return false;
                    // The ▾ opens the menu in the host, on the cell's 'menu' sub-branch: sorting first, a search, the values.
                    var m = g.open('year');
                    if (!m || g.host.children.indexOf(m.element()) < 0 || !h._branch.getBranch('menu')) return false;
                    if (g.menuEl('year', 'asc').textContent !== 'Sort smallest to largest' || g.menuEl('year', 'desc').textContent !== 'Sort largest to smallest') return false;
                    if (g.menuEl('year', 'nosort') !== null) return false;                            // nothing to clear yet
                    if (g.rows('year').length !== 31 || g.rows('year')[0].children[1].textContent !== '1990' || g.rows('year')[0].children[2].textContent !== '4') return false;
                    if (!g.menuEl('year', 'all-box').checked) return false;                            // no filter: every value chosen
                    // A sort choice applies at once and closes the menu; the caret says so; the owner is told.
                    g.menuEl('year', 'desc').dispatch('click', {});
                    if (h._menu !== null || h._branch.getBranch('menu') !== null || g.host.children.length !== 0) return false;
                    if (g.caret('year') !== '\u25BC' || g.told.join() !== 'sorted by year \u2193' || catalogue().get(r.view()[0], 'year') !== 2020) return false;
                    // "then by" keeps what is held: title joins as the second key; the numbers say which is which.
                    g.open('title');
                    g.menuEl('title', 'then-box').checked = true;
                    g.menuEl('title', 'asc').dispatch('click', {});
                    if (g.order('year') !== '1' || g.order('title') !== '2' || r.conditions().sort.length !== 2) return false;
                    // The menu of a sorted column marks the direction held and offers to clear it.
                    g.open('year');
                    if (!css.hasClass(g.menuEl('year', 'desc'), wb_gmenu_item_on) || g.menuEl('year', 'nosort') === null) return false;
                    g.menuEl('year', 'nosort').dispatch('click', {});
                    if (g.caret('year') !== '' || r.conditions().sort.map(function (x) { return x.column; }).join() !== 'title') return false;
                    // THE FILTER IS STAGED. The search narrows the list; "(select all)" means what is shown;
                    // unchecking changes nothing until OK — the table never empties under a chooser.
                    g.open('series');
                    var search = g.menuEl('series', 'search');
                    search.value = 'need'; search.dispatch('input', {});
                    if (g.shown('series').length !== 1 || g.shown('series')[0].children[1].textContent !== 'Need for Speed') return false;
                    var told = g.told.length;
                    g.menuEl('series', 'all-box').checked = false; g.menuEl('series', 'all-box').dispatch('change', {});
                    if (r.count() !== N || g.told.length !== told) return false;                          // staged
                    search.value = ''; search.dispatch('input', {});
                    if (g.shown('series').length !== g.rows('series').length) return false;
                    if (g.rowOf('series', 'Need for Speed').children[0].checked || !g.rowOf('series', 'FIFA').children[0].checked) return false;
                    g.menuEl('series', 'ok').dispatch('click', {});
                    if (r.count() !== N - 20 || g.mark('series') === '' || g.told.length !== told + 1) return false;   // every series but Need for Speed
                    if (!/one of \\d+ values/.test(r.describe())) return false;
                    // Cancel discards; Escape discards; a press outside discards.
                    g.open('series');
                    g.rowOf('series', 'FIFA').children[0].checked = false; g.rowOf('series', 'FIFA').children[0].dispatch('change', {});
                    g.menuEl('series', 'cancel').dispatch('click', {});
                    if (r.count() !== N - 20) return false;
                    g.open('series'); g.menuEl('series', 'menu').dispatch('keydown', { key: 'Escape' });
                    if (g.header('series')._menu !== null) return false;
                    g.open('series'); document.dispatch('mousedown', { target: makeEl('div') });
                    if (g.header('series')._menu !== null) return false;
                    // Clear filter applies at once.
                    g.open('series'); g.menuEl('series', 'clear').dispatch('click', {});
                    if (r.count() !== N || g.mark('series') !== '') return false;
                    // A number column: a range, and the values listed are those the OTHER filters pass.
                    r.setFilter('type', { in: ['Racing'] });
                    g.open('year');
                    if (g.rows('year').length !== 29) return false;                                     // the years with a racing game
                    g.menuEl('year', 'min').value = '2015'; g.menuEl('year', 'min').dispatch('input', {});
                    g.menuEl('year', 'ok').dispatch('click', {});
                    if (r.conditions().filters.year.min !== 2015 || r.conditions().filters.year.max !== null || r.count() !== 16) return false;
                    if (!/type in \\{Racing\\} and year 2015\u2013/.test(r.describe())) return false;   // filters in the order they were set
                    // Clear: everything as catalogued, every indication plain, the owner told.
                    told = g.told.length;
                    r.clear();
                    return r.count() === N && g.caret('title') === '' && g.mark('year') === '' && g.told.length === told + 1;
                })()"""), "the column menu sorts at once, stages the filter until OK, and tells the owner every time");
    }

    @Test
    void theGridOverItIsToldUnaskedAndTheHeaderCellsStayPut() {
        assertTrue(evalBool("""
                (() => {
                    var g = games(), r = g.relation, container = makeEl('div'), gridB = testBranch(), told = [];
                    // The bench's wiring: the relation tells its owner; the owner tells the grid, unasked.
                    var grid = null;
                    var rel2 = createGamesRelation(catalogue(), { branch: testBranch(), menuHost: g.host,
                                                                   onViewChanged: function () { if (grid) told.push(grid.tell(new RelGridViewChanged())); } });
                    grid = new RelGrid({ container: container, branch: gridB, relation: rel2, header: { show: true, sticky: true } });
                    var table = container.children[0].children[0], thead = table.children[1], tbody = table.children[2];
                    var ths = thead.children[0].children;
                    if (ths.length !== 9 || ths[2].children.indexOf(rel2.headerFor('year').headerElement()) < 0) return false;
                    if (tbody.children.length !== catalogue().size() || grid.cells().size() !== catalogue().size() * 9) return false;
                    // The cursor on Ocarina; then sort by score from the header: told, re-asked, reordered — and
                    // the cursor keeps its identity, the header cells their slots, the slots their identity.
                    var i98 = rel2.view().indexOf('g098');
                    tbody.children[i98].children[0].dispatch('mousedown', {}); document.dispatch('mouseup', {}); tbody.children[i98].children[0].dispatch('click', {});
                    if (grid.cursor().pk !== 'g098') return false;
                    var th2 = ths[2], slots = gridB.getBranch('slots');
                    rel2.headerFor('score').headerElement().children[4].dispatch('click', {});             // the menu
                    rel2.headerFor('score')._branch.getBranch('menu').getElement('desc').dispatch('click', {});
                    if (told.join() !== 'true') return false;
                    if (tbody.children[0].children[0].children[0].textContent !== 'The Legend of Zelda: Ocarina of Time') return false;
                    if (grid.cursor().pk !== 'g098' || grid.viewMaps().rowOf('g098') !== 0) return false;
                    if (thead.children[0].children[2] !== th2 || gridB.getBranch('slots') !== slots) return false;       // the same shape: kept
                    if (ths[8].children.indexOf(rel2.headerFor('score').headerElement()) < 0) return false;
                    // A filter shrinks the View: a new shape, fresh slots, the header cells re-placed, the rest forgotten.
                    rel2.setFilter('series', { contains: 'zelda' });
                    if (tbody.parentNode !== null || table.children[2].children.length !== 13) return false;
                    if (grid.cells().size() !== 13 * 9 || rel2.cellCount() < 13 * 9) return false;
                    if (table.children[1].children[0].children[8].children.indexOf(rel2.headerFor('score').headerElement()) < 0) return false;
                    return grid.cursor().pk === 'g098';
                })()"""), "the grid is told, asks view() again, and the relation's header cells stay in the header slots");
    }
}
