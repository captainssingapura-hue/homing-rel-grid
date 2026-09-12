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
 *
 * <p>Two questions the grid does ask, through the one channel: what a
 * selection is worth (copy), and — from the header's menu — how the rows
 * should be arranged. The second is answered here by a profile picked from a
 * list, and it is this table's answer: every table on the page is arranged
 * on its own, over the same store, because a View is the grid's transient
 * state and not the store's.</p>
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
                        List.of(new RelGridProtocolModule.RelGridSelectionChanged(),
                                new RelGridProtocolModule.RelGridCopyRequested(),
                                new RelGridProtocolModule.RelGridViewHandover()),
                        RelGridProtocolModule.INSTANCE),
                new ModuleImports<>(List.of(new DishStore.dishStoreShared()), DishStore.INSTANCE),
                new ModuleImports<>(List.of(new DishRelation.createDishRelation()), DishRelation.INSTANCE),
                new ModuleImports<>(List.of(new DishClipboard.dishCopyPanel()), DishClipboard.INSTANCE),
                new ModuleImports<>(List.of(new DishViews.createDishViews(), new DishViews.dishViewPanel()), DishViews.INSTANCE),
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
                "    // The copy readout. Also domain-owned: what was chosen is remembered",
                "    // here, and what was WRITTEN is reported by the grid.",
                "    var copyOut = branch.createElement('copy', 'div');",
                "    css.addClass(copyOut, wb_status);",
                "    copyOut.textContent = 'clipboard \u2190 nothing yet. Select, then Ctrl+C.';",
                "    root.appendChild(copyOut);",
                "    // The view readout. Domain-owned, and the only place the ORDER of the",
                "    // rows is explained: the grid handed it over and holds nothing about it.",
                "    var viewOut = branch.createElement('view', 'div');",
                "    css.addClass(viewOut, wb_status);",
                "    root.appendChild(viewOut);",
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
                "    // This table's view: which profile it is under. One per table, over the",
                "    // one store — two followers may be arranged two ways.",
                "    var views = createDishViews(store, { onChanged: function () { showView(); } });",
                "",
                "    hint.textContent = EDITS.length",
                "        ? ROLE.toUpperCase() + ' \u2014 edits ' + EDITS.join(' and ') + ' only. Click or arrow to a cell (shallow); Enter or double-click to edit (deep). Two ways nothing opens: on sold and popularity the grid never even asks, because the relation declared those columns read-only; everywhere else it asks and the cell for this role says no. Enter commits to the store; the store tells every relation; each updates its own cells. The grid is never told what happened.'",
                "        : 'FOLLOWER \u2014 read-only. It moves when any editor commits or the shop trades, and its grid was never spoken to after construction. Select and Ctrl+C to copy: the grid asks, this table draws the choice, the grid writes it. The \u25BE on a header (or Alt+Enter) hands the ORDER of the rows over: the grid asks, this table picks a profile, the grid presents what comes back and cannot say why.';",
                "",
                "    // THE CHANNEL, and its two customers. The grid asks; the domain answers.",
                "    //",
                "    // A selection notification expects NO answer, so nothing is answered and",
                "    // the grid does not wait. It is handled on a MICROTASK on purpose: by the",
                "    // time this runs the grid has painted and returned, which is what",
                "    // fire-and-forget means and what a synchronous callback could never show.",
                "    //",
                "    // A copy request is the other kind: the grid WAITS, and the person is",
                "    // stopped until it is answered. The second argument is the mask handle,",
                "    // and mask.panel() is the grid's box — a golden rectangle over the table",
                "    // — handed over for the domain to fill. The choice is made there, and the",
                "    // answer is what the grid writes. It learns nothing of how it was made.",
                "    //",
                "    // A view handover is the same kind, one level up: the grid hands over",
                "    // the ARRANGEMENT of its rows and waits. The same panel; a list of",
                "    // profiles on it; a View back — the pks, in order — or nothing. The grid",
                "    // presents exactly what it is handed, and the explanation stays here.",
                "    var toldCount = 0, lastCopy = '';",
                "    function ask(question, mask) {",
                "        if (question instanceof RelGridSelectionChanged) {",
                "            toldCount++;",
                "            var q = question;",
                "            return Promise.resolve().then(function () { showSelection(q); });",
                "        }",
                "        if (question instanceof RelGridCopyRequested) {",
                "            return dishCopyPanel(relation, question, mask.panel(), {",
                "                onChosen: function (format, cells) { lastCopy = format.toUpperCase() + ' \u00b7 ' + cells + (cells === 1 ? ' cell' : ' cells'); }",
                "            });",
                "        }",
                "        if (question instanceof RelGridViewHandover) {",
                "            return dishViewPanel(views, question, mask.panel());",
                "        }",
                "        return Promise.resolve();      // not understood: answered with nothing",
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
                "        columnOps: { handover: true },     // the \u25BE on every header: the order is the domain's to give",
                "        // A REPORT: the grid wrote this. What it is was decided above.",
                "        onCopied: function (content) {",
                "            copyOut.textContent = 'clipboard \u2190 ' + lastCopy + ' \u00b7 ' + content.text.length + ' chars of text'",
                "                                + (content.html != null ? ' + ' + content.html.length + ' of html' : '');",
                "        },",
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
                "    // The explanation of the order — the one thing the grid cannot give,",
                "    // because it holds nothing about it. Counted over the store as it is",
                "    // now, so after an edit it may say 5 while the table still shows 6: a",
                "    // View was a reading at the moment it was asked for.",
                "    function showView() {",
                "        viewOut.textContent = 'view \u2190 ' + views.describe() + '   |   \u25BE on a header, or Alt+Enter, to change';",
                "    }",
                "    var unsub = store.subscribe(function () { report(); showView(); });",
                "    report(); showView();",
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
