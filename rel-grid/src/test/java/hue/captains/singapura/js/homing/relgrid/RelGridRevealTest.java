package hue.captains.singapura.js.homing.relgrid;

import hue.captains.singapura.js.homing.ssjs.test.JsModuleTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The viewport follows the cursor — ported from episode 1, which had it, and
 * which episode 2's rewrite had dropped.
 *
 * <p>The geometry is a stub's and deterministic: a scrollport 45px tall over
 * rows 20px tall, so the third row is below the fold. The follow is checked
 * as arithmetic — the least delta, and none when the slot already shows —
 * and so is what it deliberately does not do: a click, Ctrl+A and an
 * arrangement move nothing, and a grid the keyboard has left does not move
 * the page. A programmatic {@code selectCell} follows regardless, because a
 * host that asked for a cell means to see it.</p>
 */
class RelGridRevealTest extends JsModuleTestBase {

    /** A scrollport with real scroll geometry, and slots whose rects move with its scrollTop. */
    private static final String GEOMETRY = """
            function ported(f) {
                var port = f.container;                              // the grid's container is the port
                port.scrollTop = 0; port.scrollLeft = 0;
                port.clientHeight = 45; port.clientWidth = 300;
                port.scrollHeight = 1000; port.scrollWidth = 300;    // it scrolls: content overflows
                port.getBoundingClientRect = function () { return { top: 100, bottom: 145, left: 0, right: 300, width: 300, height: 45 }; };
                // Every slot's rect follows the port: row i sits at 100 + 20i, minus what is scrolled.
                var rows = f.grid.viewMaps().rows(), cols = f.grid.viewMaps().cols();
                for (var i = 0; i < rows; i++) for (var j = 0; j < cols; j++) (function (td, i, j) {
                    td.getBoundingClientRect = function () {
                        var top = 100 + 20 * i - port.scrollTop;
                        return { top: top, bottom: top + 20, left: 100 * j - port.scrollLeft, right: 100 * j + 100 - port.scrollLeft, width: 100, height: 20 };
                    };
                })(f.td(i, j), i, j);
                return port;
            }
            """;

    @BeforeEach
    void setup() {
        js = buildContext();
        js.eval("js", RelGridTestDom.DOM_STUB);
        js.eval("js", RelGridTestDom.STYLES);
        for (String m : RelGridTestDom.PARTY) loadModule(m);
        loadModule(RelGridTestDom.CHANNEL);
        loadModule(RelGridTestDom.PROTOCOL);
        loadModule(RelGridTestDom.SELECTION);
        for (String m : RelGridTestDom.MODULES) loadModule(RelGridTestDom.DIR + m);
        js.eval("js", RelGridTestDom.FIXTURE);
        js.eval("js", GEOMETRY);
    }

    private boolean evalBool(String expr) { return js.eval("js", expr).asBoolean(); }

    @Test
    void arrowingBelowTheFoldBringsThePortAlongByTheLeastAmount() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture(), port = ported(f);
                    f.click(0, 0); f.table().focus();                    // the grid has the keyboard
                    f.key('ArrowDown');                                  // row 1: 120..140, inside 100..145 — nothing moves
                    if (port.scrollTop !== 0) return false;
                    f.key('ArrowDown');                                  // row 2: 140..160 — 15px past the fold
                    if (port.scrollTop !== 15) return false;
                    f.key('ArrowDown');                                  // the edge: nothing to reveal, nothing moves
                    if (port.scrollTop !== 15) return false;
                    f.key('ArrowUp'); f.key('ArrowUp');                  // row 0 is now 85..105: 15px above the top
                    return port.scrollTop === 0;
                })()"""), "the port follows the cursor by the least movement, and not at all while it already shows");
    }

    @Test
    void aShiftExtensionFollowsTheRangesFarCornerNotTheCursor() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture(), port = ported(f);
                    f.click(0, 0); f.table().focus();
                    f.key('ArrowDown', { shift: true });
                    if (port.scrollTop !== 0) return false;
                    f.key('ArrowDown', { shift: true });                 // the far corner is row 2, below the fold
                    if (port.scrollTop !== 15) return false;
                    return f.grid.cursor().pk === 'mapo';                // the cursor never moved; the corner did
                })()"""), "extending reveals the edge that moves, as a spreadsheet does");
    }

    @Test
    void aStickyHeaderAndAHostsBandAreClearedByTheReveal() {
        assertTrue(evalBool("""
                (() => {
                    // Map 12: a header that sticks is chrome the host asks for, on the header
                    // cells; and a slot revealed under it is not revealed — the follow clears it.
                    var plain = fixture(), s = fixture({ header: { sticky: true } });
                    if (css.hasClass(plain.thAt(0), hrg_sticky) || !css.hasClass(s.thAt(0), hrg_sticky) || !css.hasClass(s.thAt(1), hrg_sticky)) return false;
                    if (!css.hasClass(s.thAt(0), hrg_th)) return false;                  // still a header cell
                    // The header row is 20px tall (the stub's rect). Scrolled by hand to 40: row 2
                    // sits at the port's top, under the header. Up to row 1 — 80..100, twenty above
                    // the port — must clear the header too: 40 more than a plain grid moves.
                    var port = ported(s);
                    s.click(2, 0); s.table().focus();
                    port.scrollTop = 40;
                    s.key('ArrowUp');
                    if (port.scrollTop !== 0) return false;                             // 80 - (100 + 20) = -40
                    var p = ported(plain);
                    plain.click(2, 0); plain.table().focus();
                    p.scrollTop = 40;
                    plain.key('ArrowUp');
                    if (p.scrollTop !== 20) return false;                               // 80 - 100 = -20: no header to clear
                    // A band the HOST keeps above the table — a group's header — is named as a
                    // function, asked when the follow needs it, and cleared the same way.
                    var band = 10, h = fixture({ stickyInset: function () { return band; } });
                    var hp = ported(h);
                    h.click(2, 0); h.table().focus();
                    hp.scrollTop = 40;
                    h.key('ArrowUp');
                    if (hp.scrollTop !== 10) return false;                              // 80 - (100 + 10) = -30
                    // A band of nothing is nothing: the plain 20 again.
                    band = 0; hp.scrollTop = 40; h.click(2, 0); h.key('ArrowUp');
                    return hp.scrollTop === 20;
                })()"""), "the follow clears the grid's own sticky header and any band the host keeps above it");
    }

    @Test
    void whatDoesNotFollowClicksSelectAllAndArrangements() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture(), port = ported(f);
                    f.click(0, 0); f.table().focus();
                    port.scrollTop = 15;                                 // scrolled by hand: row 2 in view, row 0 cut
                    f.click(1, 0);                                       // a click is on something already seen
                    if (port.scrollTop !== 15) return false;
                    f.key('a', { ctrl: true });                          // Ctrl+A is no place the person went
                    if (port.scrollTop !== 15) return false;
                    f.grid.viewMaps().setRowView(['fish', 'coq', 'mapo']);   // an arrangement: the cursor keeps its identity, the port stays
                    if (port.scrollTop !== 15) return false;
                    f.grid.reapply();
                    return port.scrollTop === 15;
                })()"""), "a click, Ctrl+A and an arrangement move nothing");
    }

    @Test
    void aGridTheKeyboardHasLeftDoesNotMoveThePageButSelectCellFollowsRegardless() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture(), port = ported(f);
                    f.click(0, 0);
                    var elsewhere = makeEl('input'); document.body.appendChild(elsewhere); elsewhere.focus();
                    f.table().dispatch('keydown', { key: 'ArrowDown' }); // a stray key at an unfocused table
                    f.table().dispatch('keydown', { key: 'ArrowDown' });
                    if (port.scrollTop !== 0) return false;              // the cursor moved; the page did not
                    if (f.grid.cursor().pk !== 'fish') return false;
                    f.grid.selectCell('mapo', 'ingredient');
                    if (port.scrollTop !== 0) return false;              // row 0 shows: nothing to do
                    f.grid.selectCell('fish', 'calories');               // row 2, and the keyboard is elsewhere
                    return port.scrollTop === 15;
                })()"""), "without the focus the keyboard's moves do not scroll; a host's selectCell shows its cell regardless");
    }
}
