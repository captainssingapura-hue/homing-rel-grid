package hue.captains.singapura.js.homing.relgrid.workbench;

import hue.captains.singapura.js.homing.catalogue.site.CatalogueSiteCrate;
import hue.captains.singapura.js.homing.core.Crate;
import hue.captains.singapura.js.homing.core.CrateEntry;
import hue.captains.singapura.js.homing.core.StandardJsModuleType;
import hue.captains.singapura.js.homing.core.js.CoreJsCrate;
import hue.captains.singapura.js.homing.design.DesignCrate;
import hue.captains.singapura.js.homing.docview.app.DocViewAppCrate;
import hue.captains.singapura.js.homing.docview.site.DocViewSiteCrate;
import hue.captains.singapura.js.homing.jsonkit.JsonKitCrate;
import hue.captains.singapura.js.homing.relgrid.RelGridCrate;
import hue.captains.singapura.js.homing.relgrid.group.RelGridGroupCrate;
import hue.captains.singapura.js.homing.relgrid.protocol.RelGridProtocolCrate;
import hue.captains.singapura.js.homing.reltree.RelTreeCrate;
import hue.captains.singapura.js.homing.server.ServerCrate;
import hue.captains.singapura.js.homing.workspace.parties.WorkspacePartiesCrate;
import hue.captains.singapura.js.homing.workspace.site.WorkspaceSiteCrate;
import hue.captains.singapura.js.homing.workspace.widgets.WorkspaceWidgetsCrate;

import java.util.List;

/**
 * The {@link Crate} for {@code rel-grid-workbench}: every served JS module of the benches
 * declared - the domain each bench brings, its widgets, the parties they share, the benches'
 * page - and every crate it imports from named in {@link #requires()}. The catalogue, the
 * declarations and the server are not JS modules and are not crated.
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
                DesignCrate.INSTANCE,
                // The grid under test, and the protocol it speaks: a domain answers
                // through the protocol, so the bench depends on it directly.
                RelGridCrate.INSTANCE,
                RelGridProtocolCrate.INSTANCE,
                // And the group the two group benches stack their tables in.
                RelGridGroupCrate.INSTANCE,
                // And the tree view, the second component on the same doctrine.
                RelTreeCrate.INSTANCE,
                // And the JSON kit, the first out-of-the-box offering on it.
                JsonKitCrate.INSTANCE,
                // The workspace each bench is: its widgets' contract, their parties, the grouped page.
                WorkspaceWidgetsCrate.INSTANCE,
                WorkspacePartiesCrate.INSTANCE,
                WorkspaceSiteCrate.INSTANCE,
                // The site: a catalogue, its introduction read in DocView.
                CatalogueSiteCrate.INSTANCE,
                DocViewSiteCrate.INSTANCE,
                DocViewAppCrate.INSTANCE);
    }

    @Override
    public List<CrateEntry> entries() {
        return List.of(
                // The looks, typed: generated CSS modules - the bench's chrome, and one per domain module that draws.
                CrateEntry.of(WorkbenchStyles.INSTANCE),
                CrateEntry.of(DishStarsStyles.INSTANCE),
                CrateEntry.of(DishCopyStyles.INSTANCE),
                CrateEntry.of(DishViewStyles.INSTANCE),
                CrateEntry.of(HanCellStyles.INSTANCE),
                CrateEntry.of(HanFenceStyles.INSTANCE),
                CrateEntry.of(OutletFenceStyles.INSTANCE),
                CrateEntry.of(GamesStyles.INSTANCE),
                // What every bench widget is to its host.
                CrateEntry.of(BenchWidgetModule.INSTANCE),
                // Replicating Tables: a store each table keeps a replica of, the dishes party
                // its tables share it through - its secretary and the steward that keeps it -
                // a relation with a role and a cell manager, three editors with different
                // rights, and their followers. The grid is on none of the edit path.
                CrateEntry.of(DishStoreModule.INSTANCE, StandardJsModuleType.PURE_LOGIC),
                CrateEntry.of(DishStarsCellModule.INSTANCE),
                CrateEntry.of(DishRelationModule.INSTANCE),
                CrateEntry.of(DishClipboardFormatsModule.INSTANCE, StandardJsModuleType.PURE_LOGIC),   // strings in, strings out: no DOM
                CrateEntry.of(DishClipboardModule.INSTANCE),
                CrateEntry.of(DishViewsModule.INSTANCE),
                CrateEntry.of(DishesModule.INSTANCE, StandardJsModuleType.PURE_LOGIC),
                CrateEntry.of(DishesSecretaryModule.INSTANCE, StandardJsModuleType.SECRETARY),
                CrateEntry.of(DishesStewardModule.INSTANCE),
                CrateEntry.of(DishTableModule.INSTANCE),
                CrateEntry.of(DishChefModule.INSTANCE),
                CrateEntry.of(DishNutritionistModule.INSTANCE),
                CrateEntry.of(DishManagerModule.INSTANCE),
                CrateEntry.of(DishFollowerModule.INSTANCE),
                // Outlets: a ledger, a relation and fences per outlet.
                CrateEntry.of(SalesStoreModule.INSTANCE, StandardJsModuleType.PURE_LOGIC),
                CrateEntry.of(OutletRelationModule.INSTANCE),
                CrateEntry.of(OutletsModule.INSTANCE),
                // Han Article: an article as rows of square slots, a cell per square, an
                // editor and its displays over one string the Han article party keeps.
                CrateEntry.of(HanLayoutModule.INSTANCE, StandardJsModuleType.PURE_LOGIC),
                CrateEntry.of(HanStoreModule.INSTANCE, StandardJsModuleType.PURE_LOGIC),
                CrateEntry.of(HanCellModule.INSTANCE),
                CrateEntry.of(HanRelationModule.INSTANCE),
                CrateEntry.of(HanArticleModule.INSTANCE, StandardJsModuleType.PURE_LOGIC),
                CrateEntry.of(HanArticleSecretaryModule.INSTANCE, StandardJsModuleType.SECRETARY),
                CrateEntry.of(HanArticleStewardModule.INSTANCE),
                CrateEntry.of(HanArticleViewModule.INSTANCE),
                CrateEntry.of(HanEditorModule.INSTANCE),
                CrateEntry.of(HanDisplayModule.INSTANCE),
                CrateEntry.of(HanStressModule.INSTANCE),
                // Articles: fences for poems.
                CrateEntry.of(HanFencesModule.INSTANCE),
                CrateEntry.of(HanArticlesModule.INSTANCE),
                // Endless Table: a window over a million rows, and the widget that measures it.
                CrateEntry.of(EndlessRelationModule.INSTANCE),
                CrateEntry.of(EndlessTableModule.INSTANCE),
                // Games Catalogue: header cells of the relation's own, sorting and filtering for itself.
                CrateEntry.of(GamesStoreModule.INSTANCE,      StandardJsModuleType.PURE_LOGIC),   // the catalogue, parsed: no DOM
                CrateEntry.of(GamesConditionsModule.INSTANCE, StandardJsModuleType.PURE_LOGIC),   // conditions in, keys out: no DOM
                CrateEntry.of(GamesColumnMenuModule.INSTANCE),
                CrateEntry.of(GamesHeaderCells.INSTANCE),
                CrateEntry.of(GamesRelationModule.INSTANCE),
                CrateEntry.of(GamesDatasetModule.INSTANCE, StandardJsModuleType.PURE_LOGIC),
                CrateEntry.of(GamesCatalogueModule.INSTANCE),
                // Games Tree: the catalogue as a lazy tree over the tree view.
                CrateEntry.of(GamesTreeRelationModule.INSTANCE),
                CrateEntry.of(GamesTreeModule.INSTANCE),
                // JSON Tree: the kit's viewer over one document, typed in beside it.
                CrateEntry.of(JsonDocStoreModule.INSTANCE, StandardJsModuleType.PURE_LOGIC),
                CrateEntry.of(JsonTextModule.INSTANCE, StandardJsModuleType.PURE_LOGIC),
                CrateEntry.of(JsonTextSecretaryModule.INSTANCE, StandardJsModuleType.SECRETARY),
                CrateEntry.of(JsonInputModule.INSTANCE),
                CrateEntry.of(JsonDisplayModule.INSTANCE),
                // The Colour Extents bench: its looks, its two widgets.
                CrateEntry.of(ExtentStyles.INSTANCE),
                CrateEntry.of(ExtentTableModule.INSTANCE),
                CrateEntry.of(ExtentCardModule.INSTANCE),
                // The benches' page: their manifests, where they are filed and how arranged, and the app.
                CrateEntry.of(BenchesModule.INSTANCE),
                CrateEntry.of(BenchLayoutModule.INSTANCE, StandardJsModuleType.PURE_LOGIC),
                CrateEntry.of(GridBenchesApp.INSTANCE, StandardJsModuleType.CONSUMER));
    }
}
