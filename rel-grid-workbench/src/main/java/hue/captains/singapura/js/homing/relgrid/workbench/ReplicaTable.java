package hue.captains.singapura.js.homing.relgrid.workbench;

import hue.captains.singapura.js.homing.core.Importable;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.relgrid.RelGridModule;
import hue.captains.singapura.js.homing.relgrid.protocol.RelGridProtocolModule;

import java.util.List;

/**
 * The body shared by every Replicating Tables widget: one {@code RelGrid}
 * over a {@code createDishRelation(dishStoreShared(), { role })}. The widgets
 * differ only in the role, and the role decides which columns' cells are
 * built with a commit target. The grid is constructed identically for all of
 * them and cannot tell them apart.
 *
 * <p>Read the JS for what is <i>absent</i>. The grid is constructed with a
 * relation and never spoken to again: no subscribe, no updateCell, no
 * commit callback, no list of editable columns. Every table on the page
 * moves when any editor commits or the shop trades, and the path is
 * store → relation → cell. The grid is not on it.</p>
 */
final class ReplicaTable {

    private ReplicaTable() {}

    static List<ModuleImports<? extends Importable>> imports() {
        return List.of(
                new ModuleImports<>(List.of(new RelGridModule.RelGrid()), RelGridModule.INSTANCE),
                // The protocol, so the bench can recognise what the grid tells it. The
                // widget imports this and NOT the grid's internals — a domain answers
                // through the protocol, and depends on nothing else to do it.
                new ModuleImports<>(
                        List.of(new RelGridProtocolModule.RelGridSelectionChanged()),
                        RelGridProtocolModule.INSTANCE),
                new ModuleImports<>(List.of(new DishStore.dishStoreShared()), DishStore.INSTANCE),
                new ModuleImports<>(List.of(new DishRelation.createDishRelation()), DishRelation.INSTANCE),
                new ModuleImports<>(
                        List.of(new WorkbenchStyles.wb_root(), new WorkbenchStyles.wb_host(),
                                new WorkbenchStyles.wb_hint(), new WorkbenchStyles.wb_bar(),
                                new WorkbenchStyles.wb_btn(), new WorkbenchStyles.wb_status()),
                        WorkbenchStyles.INSTANCE));
    }

    /** @param role 'chef' | 'nutritionist' | 'manager' | 'follower' — a DishRelation role */
    static List<String> bodyJs(String role) {
        return List.of(
                "    var ROLE = '" + role + "';",
                "    var owner = Object.freeze({ toString: function () { return 'replica-' + ROLE; } });",
                "",
                "    var root = branch.createElement('root', 'div');",
                "    css.addClass(root, wb_root);",
                "    var hint = branch.createElement('hint', 'div');",
                "    css.addClass(hint, wb_hint);",
                "    root.appendChild(hint);",
                "    var bar = branch.createElement('bar', 'div');",
                "    css.addClass(bar, wb_bar);",
                "    root.appendChild(bar);",
                "    var host = branch.createElement('host', 'div');",
                "    css.addClass(host, wb_host);",
                "    root.appendChild(host);",
                "    var status = branch.createElement('status', 'div');",
                "    css.addClass(status, wb_status);",
                "    root.appendChild(status);",
                "    // The selection readout. DOMAIN-owned: the grid tells us what is",
                "    // selected and this decides what that is worth saying.",
                "    var selOut = branch.createElement('sel', 'div');",
                "    css.addClass(selOut, wb_status);",
                "    root.appendChild(selOut);",
                "",
                "    var cellsB = branch.createBranch('cells');",
                "    cellsB.activate(owner);",
                "",
                "    // The domain: one shared, persisted store; a relation over it FOR A ROLE,",
                "    // whose cell manager hands a commit target only to the cells this role",
                "    // may write, and keeps every cell current itself.",
                "    var store = dishStoreShared();",
                "    var relation = createDishRelation(store, { role: ROLE });",
                "    var EDITS = relation.editableColumns();",
                "",
                "    hint.textContent = EDITS.length",
                "        ? ROLE.toUpperCase() + ' \u2014 edits ' + EDITS.join(' and ') + ' only. Click or arrow to a cell (shallow); Enter or double-click to edit (deep). On any other cell nothing opens: the grid asked, the cell declined, the grid stayed shallow. Enter commits to the store; the store tells every relation; each updates its own cells. The grid is never told what happened.'",
                "        : 'FOLLOWER \u2014 read-only. It moves when any editor commits or the shop trades, and its grid was never spoken to after construction.';",
                "",
                "    // THE CHANNEL, and its first customer. The grid asks; the domain answers",
                "    // — except that a selection notification expects NO answer, so nothing is",
                "    // answered and the grid does not wait for one. It is handled on a",
                "    // MICROTASK on purpose: by the time this runs the grid has painted and",
                "    // returned, which is what fire-and-forget means and what a synchronous",
                "    // callback could never have shown.",
                "    var toldCount = 0;",
                "    function ask(question) {",
                "        if (!(question instanceof RelGridSelectionChanged)) return Promise.resolve();",
                "        toldCount++;",
                "        var q = question;",
                "        return Promise.resolve().then(function () { showSelection(q); });",
                "    }",
                "    function showSelection(q) {",
                "        var cells = 0, lines = [];",
                "        for (var k = 0; k < q.ranges.length; k++) {",
                "            var r = q.ranges[k];",
                "            var n = (r.i1 - r.i0 + 1) * (r.j1 - r.j0 + 1);",
                "            cells += n;",
                "            lines.push('   rows ' + r.i0 + '\\u2013' + r.i1",
                "                     + ' \\u00d7 cols ' + r.j0 + '\\u2013' + r.j1 + '   (' + n + ')');",
                "        }",
                "        var many = q.ranges.length !== 1;",
                "        selOut.textContent = 'told by the grid \\u00d7 ' + toldCount",
                "                           + '   |   ' + q.ranges.length + (many ? ' ranges' : ' range')",
                "                           + '   |   ' + cells + (cells === 1 ? ' cell' : ' cells') + '\\n'",
                "                           + lines.join('\\n');",
                "    }",
                "",
                "    // The grid: given a relation and a channel, and never spoken to again. It",
                "    // does not know the role, and there is nothing in its construction that",
                "    // could carry it.",
                "    var grid = new RelGrid({",
                "        container: host,",
                "        branch: cellsB,",
                "        relation: relation,",
                "        ask: ask,",
                "        label: 'Dish list \u2014 ' + ROLE",
                "    });",
                "",
                "    // The readout is DOMAIN state: the store's revision, the relation's cell",
                "    // count and what it may edit. Nothing here reads the grid.",
                "    function report() {",
                "        status.textContent = 'store revision ' + store.revision()",
                "                           + '   |   cells owned by this relation ' + relation.cellCount()",
                "                           + '   |   ' + (EDITS.length ? 'edits ' + EDITS.join(', ') : 'read-only');",
                "    }",
                "    var unsub = store.subscribe(function () { report(); });",
                "    report();",
                "",
                "    var btnSeq = 0;",
                "    function btn(text, fn) {",
                "        var x = branch.createElement('btn' + (++btnSeq), 'button');",
                "        css.addClass(x, wb_btn);",
                "        x.textContent = text;",
                "        x.addEventListener('click', function () { fn(); report(); });",
                "        bar.appendChild(x);",
                "    }",
                "    if (ROLE === 'manager') {",
                "        // The shop trades. Sales are the only thing that moves sold, and",
                "        // popularity is re-derived from them for every dish — a domain push",
                "        // that no editor could make by editing.",
                "        btn('a day of trade (sales move popularity)', function () { store.trade(); });",
                "        btn('reset store to seed', function () { store.reset(); });",
                "    }",
                "    btn('re-arrange (grid.reapply)', function () { grid.reapply(); });",
                "",
                "    return { root: root, setActive: function (active) {} };"
        );
    }
}
