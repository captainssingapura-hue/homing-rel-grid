package hue.captains.singapura.js.homing.relgrid.workbench;

import hue.captains.singapura.js.homing.core.Crate;
import hue.captains.singapura.js.homing.core.CrateEntry;
import hue.captains.singapura.js.homing.core.StandardJsModuleType;
import hue.captains.singapura.js.homing.core.js.CoreJsCrate;
import hue.captains.singapura.js.homing.relgrid.RelGridCrate;
import hue.captains.singapura.js.homing.relgrid.group.RelGridGroupCrate;
import hue.captains.singapura.js.homing.relgrid.protocol.RelGridProtocolCrate;
import hue.captains.singapura.js.homing.reltree.RelTreeCrate;
import hue.captains.singapura.js.homing.server.ServerCrate;
import hue.captains.singapura.js.homing.studio.base.StudioBaseCrate;
import hue.captains.singapura.js.homing.studio.workspace.StudioWorkspaceCrate;
import hue.captains.singapura.js.homing.workspace.WorkspaceCrate;
import hue.captains.singapura.js.homing.workspace.codecs.WorkspaceCodecsCrate;
import hue.captains.singapura.js.homing.workspace.persistence.WorkspacePersistenceCrate;
import hue.captains.singapura.js.homing.workspace.shell.WorkspaceShellCrate;

import java.util.List;

/**
 * RFC 0044 — the {@link Crate} for {@code rel-grid-workbench}: every served JS
 * module of the bench declared, and every framework crate it imports from
 * named in {@link #requires()}. Catalogues, specs and the server are not JS
 * modules and are not crated.
 */
public final class RelGridWorkbenchCrate implements Crate {

    public static final RelGridWorkbenchCrate INSTANCE = new RelGridWorkbenchCrate();

    private RelGridWorkbenchCrate() {}

    @Override public String name() { return "rel-grid-workbench"; }

    @Override
    public List<Crate> requires() {
        return List.of(
                CoreJsCrate.INSTANCE,
                ServerCrate.INSTANCE,
                StudioBaseCrate.INSTANCE,
                WorkspaceCrate.INSTANCE,
                WorkspaceCodecsCrate.INSTANCE,
                WorkspacePersistenceCrate.INSTANCE,
                WorkspaceShellCrate.INSTANCE,
                StudioWorkspaceCrate.INSTANCE,
                // The grid under test, and the protocol it speaks: a domain answers
                // through the protocol, so the bench depends on it directly.
                RelGridCrate.INSTANCE,
                RelGridProtocolCrate.INSTANCE,
                // And the group the two group benches stack their tables in.
                RelGridGroupCrate.INSTANCE,
                // And the tree view, the second component on the same doctrine.
                RelTreeCrate.INSTANCE);
    }

    @Override
    public List<CrateEntry> entries() {
        return List.of(
                // The looks, typed: generated CSS modules — the bench's chrome, and one per domain module that draws.
                CrateEntry.of(WorkbenchStyles.INSTANCE,   StandardJsModuleType.GENERATED_CSS),
                CrateEntry.of(DishStarsStyles.INSTANCE,   StandardJsModuleType.GENERATED_CSS),
                CrateEntry.of(DishCopyStyles.INSTANCE,    StandardJsModuleType.GENERATED_CSS),
                CrateEntry.of(DishViewStyles.INSTANCE,    StandardJsModuleType.GENERATED_CSS),
                CrateEntry.of(HanCellStyles.INSTANCE,     StandardJsModuleType.GENERATED_CSS),
                CrateEntry.of(HanFenceStyles.INSTANCE,    StandardJsModuleType.GENERATED_CSS),
                CrateEntry.of(OutletFenceStyles.INSTANCE, StandardJsModuleType.GENERATED_CSS),
                CrateEntry.of(GamesStyles.INSTANCE,       StandardJsModuleType.GENERATED_CSS),
                // Replicating Tables: a persisted store, a relation with a role and
                // a cell manager, three editors with different rights, and their
                // followers. The grid is on none of the edit path.
                CrateEntry.of(DishStore.INSTANCE),
                CrateEntry.of(DishStarsCellModule.INSTANCE),
                CrateEntry.of(DishRelation.INSTANCE),
                CrateEntry.of(DishClipboardFormats.INSTANCE, StandardJsModuleType.PURE_LOGIC),   // strings in, strings out: no DOM
                CrateEntry.of(GamesStore.INSTANCE,           StandardJsModuleType.PURE_LOGIC),   // the catalogue, parsed: no DOM
                CrateEntry.of(GamesConditions.INSTANCE,      StandardJsModuleType.PURE_LOGIC),   // conditions in, keys out: no DOM
                CrateEntry.of(DishClipboard.INSTANCE),
                CrateEntry.of(DishViews.INSTANCE),
                CrateEntry.of(DishChefWidget.INSTANCE),
                CrateEntry.of(DishNutritionistWidget.INSTANCE),
                CrateEntry.of(DishManagerWidget.INSTANCE),
                CrateEntry.of(DishFollowerWidget.INSTANCE),
                // Outlets: a ledger, a relation and fences per outlet, and the group widget.
                CrateEntry.of(SalesStore.INSTANCE),
                CrateEntry.of(OutletRelation.INSTANCE),
                CrateEntry.of(OutletsWidget.INSTANCE),
                // Han Article: an article as rows of square slots, a cell per
                // square, an editor and its displays over one persisted string.
                CrateEntry.of(HanLayout.INSTANCE),
                CrateEntry.of(HanStore.INSTANCE),
                CrateEntry.of(HanCellModule.INSTANCE),
                CrateEntry.of(HanRelation.INSTANCE),
                CrateEntry.of(HanEditorWidget.INSTANCE),
                CrateEntry.of(HanDisplayWidget.INSTANCE),
                CrateEntry.of(HanStressWidget.INSTANCE),
                // Articles: fences for poems, and the group widget.
                CrateEntry.of(HanFences.INSTANCE),
                CrateEntry.of(HanArticlesWidget.INSTANCE),
                // Endless Table: a window over a million rows, and the widget that measures it.
                CrateEntry.of(EndlessRelation.INSTANCE),
                CrateEntry.of(EndlessWidget.INSTANCE),
                // Games Catalogue: header cells of the relation's own, sorting and filtering for itself.
                CrateEntry.of(GamesColumnMenuModule.INSTANCE),
                CrateEntry.of(GamesHeaderCells.INSTANCE),
                CrateEntry.of(GamesRelation.INSTANCE),
                CrateEntry.of(GamesWidget.INSTANCE),
                // Games Tree: the catalogue as a lazy tree over the tree view.
                CrateEntry.of(GamesTreeRelation.INSTANCE),
                CrateEntry.of(GamesTreeWidget.INSTANCE));
    }
}
