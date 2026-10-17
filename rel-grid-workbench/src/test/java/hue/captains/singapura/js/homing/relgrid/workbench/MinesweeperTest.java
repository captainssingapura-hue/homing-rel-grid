package hue.captains.singapura.js.homing.relgrid.workbench;

import hue.captains.singapura.js.homing.design.semantic.DesignStyle;
import hue.captains.singapura.js.homing.design.semantic.LocalStyleRead;
import hue.captains.singapura.js.homing.design.styling.InstalledStyling;
import hue.captains.singapura.js.homing.relgrid.RelGridTestDom;
import hue.captains.singapura.js.homing.relgrid.frame.GridFrame;
import hue.captains.singapura.js.homing.relgrid.frame.GridFrameComponentModule;
import hue.captains.singapura.js.homing.relgrid.house.GridFrameBaseModule;
import hue.captains.singapura.js.homing.workspace.tree.TreeBaseModule;
import hue.captains.singapura.js.homing.docview.widgets.DocViewBaseModule;
import hue.captains.singapura.js.homing.ssjs.test.JsModuleTestBase;
import hue.captains.singapura.js.homing.ui.styles.HouseStyles;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The Minesweeper bench, in its parts: the board alone - mines laid at the first reveal and never under it nor beside
 * it, the flood, flags, the chord, a board lost and a board won, a seed that repeats - then the board as a relation under
 * the real grid, which leaves every key a move is pressed by to bubble on to the game; and Vista's design of the frame,
 * complete in every house style. Runs on the grid's own test DOM and the real party.
 */
class MinesweeperTest extends JsModuleTestBase {

    /** The house's styling, installed as a site installs it at bootstrap: the board's local style is drawn in its styles. */
    @BeforeAll
    static void theHousesStyling_installed() { InstalledStyling.install(HouseStyles.INSTANCE.styling(List.of(GridFrameBaseModule.INSTANCE, MinesweeperBaseModule.INSTANCE, TreeBaseModule.INSTANCE, DocViewBaseModule.INSTANCE))); }

    private static final String BENCH_DIR = RelGridTestDom.DIR + "workbench/";

    private static final String HELPERS = """
            // Where the mines lie: the board's own, read for the test.
            function minesOf(b) { var out = []; b._mine.forEach(function (m, k) { if (m) out.push(k); }); return out; }
            function shownOf(b) {
                var out = [];
                for (var r = 0; r < b.rows(); r++) for (var c = 0; c < b.cols(); c++) out.push(b.tile(r, c).shown);
                return out;
            }
            """;

    @BeforeEach
    void setup() {
        js = buildContext();
        js.eval("js", RelGridTestDom.DOM_STUB);
        js.eval("js", RelGridTestDom.STYLES + RelGridTestDom.handles(MinesweeperStyles.INSTANCE));
        for (String m : RelGridTestDom.PARTY) loadModule(m);
        loadModule(RelGridTestDom.SELECTION);
        loadModule(RelGridTestDom.CHANNEL);
        loadModule(RelGridTestDom.PROTOCOL);
        for (String m : RelGridTestDom.MODULES) loadModule(RelGridTestDom.DIR + m);
        loadModule(BENCH_DIR + "MinesweeperBoardModule.js");
        loadModule(BENCH_DIR + "MineCellModule.js");
        loadModule(BENCH_DIR + "MinesweeperRelationModule.js");
        js.eval("js", HELPERS);
    }

    private boolean evalBool(String expr) { return js.eval("js", expr).asBoolean(); }

    @Test
    void theFirstRevealIsNeverAMine_norBesideOne_andOpensTheEmptyGround() {
        assertTrue(evalBool("""
                (() => {
                    for (var seed = 1; seed <= 40; seed++) {
                        var b = new MinesweeperBoard({ seed: seed }), r = seed % 9, c = (seed * 7) % 9;
                        if (b.state() !== 'ready' || minesOf(b).length !== 0) return false;     // not laid until the first reveal
                        var changed = b.reveal(r, c), mines = minesOf(b);
                        if (mines.length !== 10 || b.state() !== 'playing') return false;
                        // never under it nor beside it: so it counts nothing, and the flood opens round it
                        if (mines.some(function (k) { return Math.abs(Math.floor(k / 9) - r) <= 1 && Math.abs(k % 9 - c) <= 1; })) return false;
                        var clear = (Math.min(r + 1, 8) - Math.max(r - 1, 0) + 1) * (Math.min(c + 1, 8) - Math.max(c - 1, 0) + 1);   // the 3x3, cut at an edge
                        if (b.tile(r, c).shown !== 'revealed' || b.tile(r, c).count !== 0 || changed.length < clear) return false;
                        // the flood stops at the numbers: every tile opened is no mine, and every opened zero's neighbours are open
                        for (var k = 0; k < 81; k++) {
                            var t = b.tile(Math.floor(k / 9), k % 9);
                            if (t.shown === 'revealed' && mines.indexOf(k) >= 0) return false;
                            if (t.shown !== 'revealed' || t.count !== 0) continue;
                            for (var dr = -1; dr <= 1; dr++) for (var dc = -1; dc <= 1; dc++) {
                                var rr = Math.floor(k / 9) + dr, cc = k % 9 + dc;
                                if (rr >= 0 && cc >= 0 && rr < 9 && cc < 9 && b.tile(rr, cc).shown !== 'revealed') return false;
                            }
                        }
                    }
                    // the same seed lays the same board
                    var x = new MinesweeperBoard({ seed: 7 }), y = new MinesweeperBoard({ seed: 7 });
                    x.reveal(4, 4); y.reveal(4, 4);
                    return minesOf(x).join() === minesOf(y).join();
                })()"""), "mines laid at the first reveal, clear of it, the flood opening the empty ground");
    }

    @Test
    void flags_theChord_andABoardLost() {
        assertTrue(evalBool("""
                (() => {
                    var b = new MinesweeperBoard({ seed: 3 });
                    b.reveal(0, 0);
                    var mines = minesOf(b), at = function (k) { return { r: Math.floor(k / 9), c: k % 9 }; };
                    // A revealed number, and the mines round it.
                    var num = -1;
                    for (var k = 0; k < 81 && num < 0; k++) { var t = b.tile(at(k).r, at(k).c); if (t.shown === 'revealed' && t.count > 0) num = k; }
                    if (num < 0) return false;
                    var n = at(num), round = [];
                    for (var dr = -1; dr <= 1; dr++) for (var dc = -1; dc <= 1; dc++) {
                        var rr = n.r + dr, cc = n.c + dc;
                        if ((dr || dc) && rr >= 0 && cc >= 0 && rr < 9 && cc < 9) round.push(rr * 9 + cc);
                    }
                    var near = round.filter(function (k) { return mines.indexOf(k) >= 0; });
                    // A chord short of flags does nothing.
                    if (b.chord(n.r, n.c).length !== 0) return false;
                    // Flags: on, counted, off again; a revealed tile takes none.
                    if (b.flag(at(near[0]).r, at(near[0]).c).length !== 1 || b.minesLeft() !== 9) return false;
                    if (b.tile(at(near[0]).r, at(near[0]).c).shown !== 'flagged') return false;
                    if (b.flag(n.r, n.c).length !== 0) return false;
                    if (b.flag(at(near[0]).r, at(near[0]).c).length !== 1 || b.minesLeft() !== 10) return false;
                    // Every mine round it flagged: the chord opens the rest round it, and nothing is lost.
                    near.forEach(function (k) { b.flag(at(k).r, at(k).c); });
                    b.chord(n.r, n.c);
                    if (b.state() === 'lost') return false;
                    if (round.some(function (k) { return mines.indexOf(k) < 0 && b.tile(at(k).r, at(k).c).shown !== 'revealed'; })) return false;
                    // A flag in the wrong place, then a mine revealed: lost - the boom, the rest of the mines, the wrong flag.
                    var safe = -1, mine = -1;
                    for (var k2 = 0; k2 < 81; k2++) {
                        var s = b.tile(at(k2).r, at(k2).c).shown;
                        if (safe < 0 && s === 'hidden' && mines.indexOf(k2) < 0) safe = k2;
                        if (mine < 0 && s === 'hidden' && mines.indexOf(k2) >= 0) mine = k2;
                    }
                    b.flag(at(safe).r, at(safe).c);
                    var changed = b.reveal(at(mine).r, at(mine).c);
                    if (b.state() !== 'lost' || b.tile(at(mine).r, at(mine).c).shown !== 'boom') return false;
                    if (b.tile(at(safe).r, at(safe).c).shown !== 'wrong') return false;
                    if (mines.some(function (k) { var s = b.tile(at(k).r, at(k).c).shown; return k !== mine && s !== 'mine' && s !== 'flagged'; })) return false;
                    if (changed.length < 2) return false;
                    // Over: no move but restart, which changes every position and lays nothing.
                    if (b.reveal(at(safe).r, at(safe).c).length || b.flag(0, 0).length || b.chord(n.r, n.c).length) return false;
                    if (b.restart().length !== 81 || b.state() !== 'ready' || b.minesLeft() !== 10) return false;
                    return shownOf(b).every(function (s) { return s === 'hidden'; });
                })()"""), "flags counted, the chord only with its flags down, a lost board showing what it hid");
    }

    @Test
    void everySafeTileRevealed_isWon_theMinesFlagged() {
        assertTrue(evalBool("""
                (() => {
                    var b = new MinesweeperBoard({ seed: 11 });
                    b.reveal(4, 4);
                    var mines = minesOf(b);
                    for (var k = 0; k < 81; k++) if (mines.indexOf(k) < 0) b.reveal(Math.floor(k / 9), k % 9);
                    if (b.state() !== 'won' || b.minesLeft() !== 0) return false;
                    return mines.every(function (k) { return b.tile(Math.floor(k / 9), k % 9).shown === 'flagged'; });
                })()"""), "every safe tile revealed: won, and every mine flagged");
    }

    @Test
    void theBoardAsARelation_underTheGrid_leavesTheMovesKeysToBubble() {
        assertTrue(evalBool("""
                (() => {
                    var rel = new MinesweeperRelation({ branch: testBranch(), rows: 9, cols: 9 });
                    if (rel.view().length !== 9 || rel.view({ by: 1 }) !== null) return false;            // one View: the board stands
                    if (rel.columns().join() !== rel.readOnlyColumns().join() || rel.columns().length !== 9) return false;
                    if (rel.at('r3', 'c5').r !== 3 || rel.at('r3', 'c5').c !== 5 || rel.at('r9', 'c0') !== null || rel.at('x', 'c0') !== null) return false;
                    try { rel.cellFor('r0', 'c9'); return false; } catch (e) { if (!/no such tile/.test(String(e))) return false; }
                    var container = makeEl('div'), heard = [];
                    container.addEventListener('keydown', function (e) { if (!e.defaultPrevented) heard.push(e.key); });
                    var grid = new RelGrid({ container: container, branch: testBranch(), relation: rel, design: TEST_DESIGN, header: { show: false } });
                    var table = container.children[0].children[0];
                    function key(k) {
                        var ev = { key: k, defaultPrevented: false };
                        ev.preventDefault = function () { ev.defaultPrevented = true; };
                        table.dispatch('keydown', ev);
                        return ev.defaultPrevented;
                    }
                    // A tile takes no control: Enter is not the grid's, nor Space, F, C or R - each bubbles on to the game.
                    if (key('Enter') || key(' ') || key('f') || key('c') || key('r')) return false;
                    if (heard.join('|') !== 'Enter| |f|c|r' || grid.isDeep()) return false;
                    if (!key('ArrowRight') || grid.cursor().column !== 'c1') return false;             // the arrows are still the grid's
                    // A tile shows what the board says of it, and is found by its element.
                    var b = new MinesweeperBoard({ seed: 5 });
                    var changed = b.reveal(0, 0);
                    rel.show(changed, b);
                    var el = rel.cellFor('r0', 'c0').cellElement();
                    if (el.className.indexOf('ms-revealed') < 0 || el.className.indexOf('ms-hidden') >= 0) return false;
                    var at = rel.atElement(el);
                    return at.r === 0 && at.c === 0 && rel.atElement(container) === null && rel.cellFor('r0', 'c0').shown() === 'revealed';
                })()"""), "the board as one standing View; the move keys left to bubble; a tile shown as the board says");
    }

    @Test
    void vista_isCompleteOverTheFrame_inEveryHouseStyle() {
        LocalStyleRead vista = InstalledStyling.get().localStyle(MinesweeperVista.INSTANCE);   // drawn in every house style as the styling was made
        assertEquals(GridFrame.INSTANCE, vista.branch());
        assertEquals(GridFrameComponentModule.INSTANCE.components(), vista.concretes().stream().map(c -> c.virtual()).toList(),
                "a concrete leaf for every leaf of the frame, in the frame's order");
        for (String slug : InstalledStyling.get().styles().stream().map(DesignStyle::token).toList()) {
            String css = MinesweeperVistaSheet.INSTANCE.css(slug);
            assertTrue(css.contains(".minesweeper-vista___grid-edge__shape-corner {\n    border-radius: 10px;\n}"), slug + ":\n" + css);
            assertTrue(css.contains(".minesweeper-vista___grid-edge__color-outline {"), slug + ": the edge glows while the board holds the keys");
            assertTrue(css.contains(".minesweeper-vista___question-panel__shape-corner {"), slug + ": a question's glass plate");
            assertTrue(css.contains(".minesweeper-vista___held-cursor-border__color-outline {\n    outline-color: var(--fp-accent);\n}"),
                    slug + ": the cursor in the accent, as the palette worn defines it");
        }
    }
}
