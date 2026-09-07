package hue.captains.singapura.js.homing.relgrid.workbench;

import hue.captains.singapura.js.homing.core.Importable;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.relgrid.workbench.WorkbenchStyles;
import hue.captains.singapura.js.homing.relgrid.RelGridModule;

import java.util.List;

/**
 * The body shared by the Replicating Tables bench's two widgets: one
 * {@code RelGrid} over a {@code createDishRelation(dishStoreShared(), …)},
 * differing only in whether the relation is editable.
 *
 * <p>Read the JS for what is <i>absent</i>. The grid is constructed with a
 * relation and never spoken to again: no subscribe, no updateCell, no
 * commit callback. Every follower on the page moves when the editor commits,
 * and the path is store → relation → cell. The grid is not on it.</p>
 */
final class ReplicaTable {

    private ReplicaTable() {}

    static List<ModuleImports<? extends Importable>> imports() {
        return List.of(
                new ModuleImports<>(List.of(new RelGridModule.RelGrid()), RelGridModule.INSTANCE),
                new ModuleImports<>(List.of(new DishStore.dishStoreShared()), DishStore.INSTANCE),
                new ModuleImports<>(List.of(new DishRelation.createDishRelation()), DishRelation.INSTANCE),
                new ModuleImports<>(
                        List.of(new WorkbenchStyles.wb_root(), new WorkbenchStyles.wb_host(),
                                new WorkbenchStyles.wb_hint(), new WorkbenchStyles.wb_bar(),
                                new WorkbenchStyles.wb_btn(), new WorkbenchStyles.wb_status()),
                        WorkbenchStyles.INSTANCE));
    }

    /**
     * @param role     'editor' | 'follower' — the hint line, and whether cells commit
     * @param editable whether this table's relation commits to the store
     */
    static List<String> bodyJs(String role, boolean editable) {
        return List.of(
                "    var owner = Object.freeze({ toString: function () { return 'replica-" + role + "'; } });",
                "    var EDITABLE = " + editable + ";",
                "",
                "    var root = branch.createElement('root', 'div');",
                "    css.addClass(root, wb_root);",
                "    var hint = branch.createElement('hint', 'div');",
                "    css.addClass(hint, wb_hint);",
                "    hint.textContent = EDITABLE",
                "        ? 'EDITOR \\u2014 click or arrow to a cell (shallow), then Enter or double-click to edit (deep). Enter commits to the store, Escape cancels; the store tells every relation; each relation updates its own cells. The grid is never told what happened.'",
                "        : 'FOLLOWER \\u2014 read-only. It moves when the editor commits, and its grid was never spoken to after construction.';",
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
                "",
                "    var cellsB = branch.createBranch('cells');",
                "    cellsB.activate(owner);",
                "",
                "    // The domain: one shared, persisted store; a relation over it whose",
                "    // cell manager owns the cells and keeps them current itself.",
                "    var store = dishStoreShared();",
                "    var relation = createDishRelation(store, { editable: EDITABLE });",
                "",
                "    // The grid: given a relation, and never spoken to again.",
                "    var grid = new RelGrid({",
                "        container: host,",
                "        branch: cellsB,",
                "        relation: relation,",
                "        label: 'Dish list \\u2014 ' + (EDITABLE ? 'editor' : 'follower')",
                "    });",
                "",
                "    // The readout is DOMAIN state: the store's revision, and the relation's",
                "    // cell count. Nothing here reads the grid.",
                "    function report() {",
                "        status.textContent = 'store revision ' + store.revision()",
                "                           + '   |   cells owned by this relation ' + relation.cellCount()",
                "                           + '   |   ' + (EDITABLE ? 'commits on Enter' : 'read-only');",
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
                "    if (EDITABLE) {",
                "        btn('reset store to seed', function () { store.reset(); });",
                "        btn('bump every popularity (domain push)', function () {",
                "            store.pks().forEach(function (pk) {",
                "                store.commit(pk, 'popularity', (Number(store.get(pk, 'popularity')) || 0) + 1);",
                "            });",
                "        });",
                "    }",
                "    btn('re-arrange (grid.reapply)', function () { grid.reapply(); });",
                "",
                "    return { root: root, setActive: function (active) {} };"
        );
    }
}
