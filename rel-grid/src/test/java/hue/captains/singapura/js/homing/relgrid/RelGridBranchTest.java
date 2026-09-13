package hue.captains.singapura.js.homing.relgrid;

import hue.captains.singapura.js.homing.ssjs.test.JsModuleTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * RFC 0044 — the grid mints nothing raw. Every element it makes is minted
 * through the DomOpsParty on the branch it was handed — its own, the host's
 * to dissolve — and what comes and goes is minted on a sub-branch made for it
 * and dissolved with it. Held to with the REAL party, not a stub: unique
 * names, the activation gate, and dissolve's release are the party's own.
 *
 * <p>What is checked: the chrome that lives as long as the grid is on the
 * branch itself; an arrangement's slots are on a {@code slots} sub-branch
 * that the next arrangement dissolves — the old slots are released, the cell
 * hosts ride into the new ones; an overlay, the mask and a drag's guide each
 * come on a sub-branch and go with it; destroy() dissolves everything the
 * grid made for itself and leaves the branch's own elements to the host; and
 * one branch is one grid's — a second grid on it is refused by name.</p>
 */
class RelGridBranchTest extends JsModuleTestBase {

    @BeforeEach
    void setup() {
        js = buildContext();
        js.eval("js", RelGridTestDom.DOM_STUB);
        for (String m : RelGridTestDom.PARTY) loadModule(m);
        loadModule(RelGridTestDom.PROTOCOL);
        loadModule(RelGridTestDom.SELECTION);
        for (String m : RelGridTestDom.MODULES) loadModule(RelGridTestDom.DIR + m);
        js.eval("js", RelGridTestDom.FIXTURE);
    }

    private boolean evalBool(String expr) { return js.eval("js", expr).asBoolean(); }
    private void act(String src) { js.eval("js", src); }

    @Test
    void theChromeIsOnTheBranchAndTheSlotsOnASubBranchOfIt() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture(), b = f.branch;
                    // What lives as long as the grid: minted on the branch itself, by name.
                    var names = b.listElements().map(function (e) { return e.name; }).sort().join(' ');
                    if (names !== 'colgroup header-row table thead wrap') return false;
                    if (b.getElement('table') !== f.table() || b.getElement('wrap') !== f.wrap()) return false;
                    // What one arrangement makes: on 'slots' — and nothing of a cell's: the
                    // cells' elements are on the DOMAIN's branch, and the grid mints none.
                    if (b.listBranches().sort().join(' ') !== 'slots') return false;
                    var slots = b.getBranch('slots'), rows = f.grid.viewMaps().rows(), cols = f.grid.viewMaps().cols();
                    // cols + ths + the body + trs + tds — and the resize handles, one per header
                    if (slots.elementCount !== cols + cols + 1 + rows + rows * cols + cols) return false;
                    if (slots.getElement('td-0-0') !== f.td(0, 0) || slots.getElement('th-1') !== f.thAt(1)) return false;
                    // Each cell's element is in its slot, and each is the domain's: minted on a
                    // sub-branch of the domain's branch, one per cell.
                    if (f.cellsBranch.branchCount !== rows * cols || f.cellsBranch.elementCount !== 0) return false;
                    var c = f.cellsBranch.getBranch('c1');
                    return c.getElement('cell') === f.td(0, 0).children[0];
                })()"""), "the grid's chrome is the branch's; an arrangement's slots and the cell hosts are on sub-branches of it");
    }

    @Test
    void anArrangementDissolvesTheLastOnesSlotsAndTheCellsRideIntoTheNew() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture(), b = f.branch;
                    var oldTd = f.td(0, 0), oldBody = f.tbody(), host = f.cellEl('mapo', 'ingredient');
                    var oldSlots = b.getBranch('slots');
                    f.grid.viewMaps().setRowView(['fish', 'coq', 'mapo']);   // an arrangement: the structure is rebuilt
                    f.grid.reapply();
                    // The old slots branch is gone and its elements released: the old
                    // body is out of the table, the old slot out of its row.
                    if (b.getBranch('slots') === oldSlots || oldSlots.elementCount !== 0) return false;
                    if (oldBody.parentNode !== null || oldTd.parentNode !== null) return false;
                    if (f.tbody() === oldBody) return false;
                    // The cell's element was never asked for again: the same element, in the new slot.
                    if (f.cellEl('mapo', 'ingredient') !== host || host.parentNode !== f.td(2, 0)) return false;
                    return b.listBranches().sort().join(' ') === 'slots' && f.mints() === 6;   // asked once each, ever
                })()"""), "a rebuild dissolves the last arrangement's slots; the cell hosts are the same elements, re-placed");
    }

    @Test
    void anOverlayTheMaskAndAGuideEachComeOnASubBranchAndGoWithIt() {
        act("""
                var F = fixture({ editable: true, ask: function (q, mask) {
                    if (!(q instanceof RelGridCopyRequested)) return Promise.resolve();
                    mask.panel(makeEl('div'));
                    return new Promise(function () {});                        // the domain thinks
                }});
                // The editor's overlay: up on Enter, on a sub-branch of its own — and the
                // editor in it is the CELL's element, from the domain's branch.
                F.click(0, 0); F.key('Enter');
                var OV = F.branch.getBranch('overlay');
                var OV_UP = !!OV && OV.getElement('overlay') === F.overlay()
                         && F.overlay().children[0] === F.cellsBranch.getBranch('c1').getElement('editor');
                F.overlay().children[0].dispatch('keydown', { key: 'Escape' });   // the cell cancels; the settle is a microtask
                """);
        assertTrue(evalBool("""
                (() => {
                    var f = F, b = f.branch;
                    if (!OV_UP) return false;
                    if (b.hasBranch('overlay') || f.overlay() !== null) return false;   // dissolved with the edit
                    // A drag's guide: a sub-branch per gesture, dissolved on release.
                    var th = f.thAt(1); th._rl = 300; th._rr = 400;
                    var handle = th.children[th.children.length - 1];
                    handle.dispatch('mousedown', { clientX: 398 });
                    if (b.listBranches().filter(function (n) { return /^guide-/.test(n); }).length !== 1) return false;
                    if (document.body.children.length !== 1) return false;    // the one segment, over the table
                    document.dispatch('mouseup', {});
                    if (b.listBranches().filter(function (n) { return /^guide-/.test(n); }).length !== 0) return false;
                    if (document.body.children.length !== 0) return false;
                    // The mask and its panel, while a question is pending.
                    f.click(0, 0);
                    if (!f.grid.copy()) return false;
                    var mb = b.getBranch('mask');
                    if (!mb || mb.getElement('mask') !== f.mask()) return false;
                    if (mb.getBranch('panel').getElement('panel') !== f.panel()) return false;   // the panel: its own, under the mask's
                    return b.listBranches().sort().join(' ') === 'mask slots';
                })()"""), "the overlay, a guide and the mask are each a sub-branch: minted for the occasion, dissolved with it");
    }

    @Test
    void destroyDissolvesWhatTheGridMadeForItselfAndTheHostDissolvesTheRest() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture({ editable: true }), b = f.branch, wrap = f.wrap();
                    f.click(0, 0); f.key('Enter');                              // an overlay is up
                    if (!b.hasBranch('overlay')) return false;
                    f.grid.destroy();
                    // Every sub-branch is gone; the branch's own elements are still its —
                    // the wrapper is out of the container, but the host has not dissolved.
                    if (b.branchCount !== 0 || b.elementCount !== 5) return false;
                    if (wrap.parentNode !== null || f.container.children.length !== 0) return false;
                    if (b.getElement('wrap') !== wrap) return false;
                    b.dissolve();                                              // the host's move
                    return b.elementCount === 0 && !domOpsParty.hasBranch(b.name);
                })()"""), "destroy() dissolves the grid's sub-branches; the branch itself is the host's to dissolve");
    }

    @Test
    void oneBranchIsOneGrids() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture();
                    var refused = null;
                    try { new RelGrid({ container: makeEl('div'), branch: f.branch, relation: f.relation }); }
                    catch (e) { refused = String(e); }
                    if (!refused || !/"table" is already in use/.test(refused)) return false;
                    // And a branch nobody activated is refused by the party before the grid mints a thing.
                    var dormant = domOpsParty.createBranch('dormant'), gate = null;
                    try { new RelGrid({ container: makeEl('div'), branch: dormant, relation: f.relation }); }
                    catch (e) { gate = String(e); }
                    return !!gate && /has not been activated/.test(gate);
                })()"""), "a second grid on a branch is refused by name; an unactivated branch is refused at the gate");
    }
}
