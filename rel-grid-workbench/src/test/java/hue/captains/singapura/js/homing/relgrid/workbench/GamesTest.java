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
                    branch: testBranch(), popoverHost: host,
                    onViewChanged: function () { told.push(relation.describe()); }
                });
                function header(col) { return relation.headerFor(col); }
                function sortBtn(col) { return header(col).headerElement().children[0]; }
                function funnel(col) { return header(col).headerElement().children[1]; }
                function caret(col) { return sortBtn(col).children[1].textContent; }
                function order(col) { return sortBtn(col).children[2].textContent; }
                return { relation: relation, host: host, told: told, header: header, sortBtn: sortBtn, funnel: funnel, caret: caret, order: order,
                         click: function (col, mods) { sortBtn(col).dispatch('click', { shiftKey: !!(mods && mods.shift) }); } };
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
                    // Described, for a status line.
                    if (gamesDescribe(c) !== 'as catalogued') return false;
                    return gamesDescribe(gamesSetFilter(c5, 'type', { in: ['RPG'] })) === 'sorted by year \\u2193, then title \\u2191; where type in {RPG}';
                })()"""), "conditions are values; absent sorts last either way; filters as named");
    }

    @Test
    void theRelationsHeaderCellsSortAndFilterAndTellTheOwner() {
        assertTrue(evalBool("""
                (() => {
                    var g = games(), r = g.relation;
                    if (r.view().length !== r.count() || r.count() !== catalogue().size()) return false;
                    if (r.view({ by: 1 }) !== null) return false;                                  // a catalogue is the whole of itself
                    if (typeof r.pks !== 'undefined' || r.readOnlyColumns().length !== 9) return false;
                    if (r.labels().sales !== 'sales (M)') return false;
                    // A header cell per column, once, on its own sub-branch: a label that is a button, a caret, a funnel.
                    var h = g.header('year');
                    if (h !== r.headerFor('year') || r.headerCount() !== 1) return false;
                    if (g.sortBtn('year').children[0].textContent !== 'year' || g.caret('year') !== '' || g.funnel('year').textContent !== '\\u25BE') return false;
                    // A click sorts: the caret shows it, the View follows, the owner is told.
                    g.click('year');
                    if (g.caret('year') !== '\\u25B2' || g.told.length !== 1 || g.told[0] !== 'sorted by year \\u2191') return false;
                    if (r.view()[0] !== 'g001') return false;
                    g.click('year');
                    if (g.caret('year') !== '\\u25BC' || catalogue().get(r.view()[0], 'year') !== 2020) return false;
                    // Shift-click adds a key after it; the order numbers show; a plain click elsewhere replaces all.
                    g.click('title', { shift: true });
                    if (g.order('year') !== '1' || g.order('title') !== '2' || g.caret('title') !== '\\u25B2') return false;
                    if (r.conditions().sort.length !== 2) return false;
                    g.click('score');
                    if (g.caret('year') !== '' || g.order('title') !== '' || r.conditions().sort.length !== 1) return false;
                    // The funnel opens a popover in the host, on the cell's own branch; typing filters live; close dissolves it.
                    var pop = g.header('series').openPopover();
                    if (!pop || g.host.children.indexOf(pop) < 0 || pop.children[0].textContent !== 'series') return false;
                    var input = pop.children[1];
                    input.value = 'need for speed'; input.dispatch('input', {});
                    if (r.count() !== 20 || !r.describe().match(/where series contains/)) return false;
                    if (!css.hasClass(g.funnel('series'), wb_gh_filter_on)) return false;
                    g.header('series').closePopover();
                    if (g.host.children.length !== 0 || pop.parentNode !== null) return false;
                    // A number range and a set, through the popovers' own controls.
                    var yp = g.header('year').openPopover(), row = yp.children[1];
                    row.children[0].value = '2015'; row.children[0].dispatch('input', {});
                    if (r.count() !== 3) return false;                                             // Need for Speed (2015), Payback, Heat
                    g.header('year').closePopover();
                    var tp = g.header('type').openPopover(), list = tp.children[1];
                    if (list.children.length < 20) return false;
                    var racing = null;
                    for (var i = 0; i < list.children.length; i++) if (/^Racing/.test(list.children[i].children[1].textContent)) racing = list.children[i].children[0];
                    racing.checked = true; racing.dispatch('change', {});
                    if (r.conditions().filters.type.in.join() !== 'Racing' || r.count() !== 3) return false;
                    g.header('type').closePopover();
                    // Clear: everything as catalogued, every header plain, the owner told.
                    var before = g.told.length;
                    r.clear();
                    return r.count() === catalogue().size() && g.caret('score') === '' && !css.hasClass(g.funnel('series'), wb_gh_filter_on) && g.told.length === before + 1;
                })()"""), "header cells sort and filter the relation's own View and tell the owner every time");
    }

    @Test
    void theGridOverItIsToldUnaskedAndTheHeaderCellsStayPut() {
        assertTrue(evalBool("""
                (() => {
                    var g = games(), r = g.relation, container = makeEl('div'), gridB = testBranch(), told = [];
                    // The bench's wiring: the relation tells its owner; the owner tells the grid, unasked.
                    var grid = null;
                    var rel2 = createGamesRelation(catalogue(), { branch: testBranch(), popoverHost: g.host,
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
                    rel2.headerFor('score').headerElement().children[0].dispatch('click', {});
                    rel2.headerFor('score').headerElement().children[0].dispatch('click', {});             // desc
                    if (told.join() !== 'true,true') return false;
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
