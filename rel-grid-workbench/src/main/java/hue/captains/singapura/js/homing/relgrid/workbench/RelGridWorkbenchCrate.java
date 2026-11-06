package hue.captains.singapura.js.homing.relgrid.workbench;

import hue.captains.singapura.js.homing.catalogue.site.CatalogueSiteCrate;
import hue.captains.singapura.js.homing.core.Crate;
import hue.captains.singapura.js.homing.core.CrateEntry;
import hue.captains.singapura.js.homing.core.StandardJsModuleType;
import hue.captains.singapura.js.homing.core.js.CoreJsCrate;
import hue.captains.singapura.js.homing.docview.app.DocViewAppCrate;
import hue.captains.singapura.js.homing.docview.site.DocViewSiteCrate;
import hue.captains.singapura.js.homing.jsonkit.JsonKitCrate;
import hue.captains.singapura.js.homing.relgrid.RelGridCrate;
import hue.captains.singapura.js.homing.relgrid.group.RelGridGroupCrate;
import hue.captains.singapura.js.homing.relgrid.protocol.RelGridProtocolCrate;
import hue.captains.singapura.js.homing.relgrid.house.RelGridHouseCrate;
import hue.captains.singapura.js.homing.relgrid.cells.RelGridCellsCrate;
import hue.captains.singapura.js.homing.relgrid.prose.RelGridProseCrate;
import hue.captains.singapura.js.homing.reltree.RelTreeCrate;
import hue.captains.singapura.js.homing.server.ServerCrate;
import hue.captains.singapura.js.homing.workspace.parties.WorkspacePartiesCrate;
import hue.captains.singapura.js.homing.workspace.site.WorkspaceSiteCrate;
import hue.captains.singapura.js.homing.workspace.widgets.WorkspaceWidgetsCrate;
import hue.captains.singapura.js.homing.ui.elements.UiElementsCrate;
import hue.captains.singapura.js.homing.ui.look.UiLookCrate;

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
                // The grid under test, and the protocol it speaks: a domain answers
                // through the protocol, so the bench depends on it directly.
                RelGridCrate.INSTANCE, RelGridHouseCrate.INSTANCE, RelGridCellsCrate.INSTANCE,
                RelGridProtocolCrate.INSTANCE,
                // And the group the two group benches stack their tables in.
                RelGridGroupCrate.INSTANCE,
                // And the tree view, the second component on the same doctrine.
                RelTreeCrate.INSTANCE,
                // And the JSON kit, the first out-of-the-box offering on it.
                JsonKitCrate.INSTANCE,
                // And the document table, the second: its pages in the house's words.
                RelGridProseCrate.INSTANCE, UiElementsCrate.INSTANCE, UiLookCrate.INSTANCE,
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
                new CrateEntry(WorkbenchStyles.INSTANCE),
                new CrateEntry(DishStarsStyles.INSTANCE),
                new CrateEntry(DishCopyStyles.INSTANCE),
                new CrateEntry(DishViewStyles.INSTANCE),
                new CrateEntry(HanCellStyles.INSTANCE),
                new CrateEntry(HanFenceStyles.INSTANCE),
                new CrateEntry(OutletFenceStyles.INSTANCE),
                new CrateEntry(GamesStyles.INSTANCE),
                // What every bench widget is to its host.
                new CrateEntry(BenchWidgetModule.INSTANCE),
                // Replicating Tables: a store each table keeps a replica of, the dishes party
                // its tables share it through - its secretary and the steward that keeps it -
                // a relation with a role and a cell manager, three editors with different
                // rights, and their followers. The grid is on none of the edit path.
                new CrateEntry(DishStoreModule.INSTANCE, StandardJsModuleType.PURE_LOGIC),
                new CrateEntry(DishStarsCellModule.INSTANCE),
                new CrateEntry(DishRelationModule.INSTANCE),
                new CrateEntry(DishClipboardFormatsModule.INSTANCE, StandardJsModuleType.PURE_LOGIC),   // strings in, strings out: no DOM
                new CrateEntry(DishClipboardModule.INSTANCE),
                new CrateEntry(DishViewsModule.INSTANCE),
                new CrateEntry(DishesModule.INSTANCE, StandardJsModuleType.PURE_LOGIC),
                new CrateEntry(DishesSecretaryModule.INSTANCE, StandardJsModuleType.SECRETARY),
                new CrateEntry(DishesStewardModule.INSTANCE),
                new CrateEntry(DishTableModule.INSTANCE),
                new CrateEntry(DishChefModule.INSTANCE),
                new CrateEntry(DishNutritionistModule.INSTANCE),
                new CrateEntry(DishManagerModule.INSTANCE),
                new CrateEntry(DishFollowerModule.INSTANCE),
                // Outlets: a ledger, a relation and fences per outlet.
                new CrateEntry(SalesStoreModule.INSTANCE, StandardJsModuleType.PURE_LOGIC),
                new CrateEntry(OutletRelationModule.INSTANCE),
                new CrateEntry(OutletsModule.INSTANCE),
                // Han Article: an article as rows of square slots, a cell per square, an
                // editor and its displays over one string the Han article party keeps.
                new CrateEntry(HanLayoutModule.INSTANCE, StandardJsModuleType.PURE_LOGIC),
                new CrateEntry(HanStoreModule.INSTANCE, StandardJsModuleType.PURE_LOGIC),
                new CrateEntry(HanCellModule.INSTANCE),
                new CrateEntry(HanRelationModule.INSTANCE),
                new CrateEntry(HanArticleModule.INSTANCE, StandardJsModuleType.PURE_LOGIC),
                new CrateEntry(HanArticleSecretaryModule.INSTANCE, StandardJsModuleType.SECRETARY),
                new CrateEntry(HanArticleStewardModule.INSTANCE),
                new CrateEntry(HanArticleViewModule.INSTANCE),
                new CrateEntry(HanEditorModule.INSTANCE),
                new CrateEntry(HanDisplayModule.INSTANCE),
                new CrateEntry(HanStressModule.INSTANCE),
                // Articles: fences for poems.
                new CrateEntry(HanFencesModule.INSTANCE),
                new CrateEntry(HanArticlesModule.INSTANCE),
                // Endless Table: a window over a million rows, and the widget that measures it.
                new CrateEntry(EndlessRelationModule.INSTANCE),
                new CrateEntry(EndlessTableModule.INSTANCE),
                // Games Catalogue: header cells of the relation's own, sorting and filtering for itself.
                new CrateEntry(GamesStoreModule.INSTANCE,      StandardJsModuleType.PURE_LOGIC),   // the catalogue, parsed: no DOM
                new CrateEntry(GamesConditionsModule.INSTANCE, StandardJsModuleType.PURE_LOGIC),   // conditions in, keys out: no DOM
                new CrateEntry(GamesColumnMenuModule.INSTANCE),
                new CrateEntry(GamesHeaderCells.INSTANCE),
                new CrateEntry(GamesRelationModule.INSTANCE),
                new CrateEntry(GamesDatasetModule.INSTANCE, StandardJsModuleType.PURE_LOGIC),
                new CrateEntry(GamesCatalogueModule.INSTANCE),
                // Games Tree: the catalogue as a lazy tree over the tree view.
                new CrateEntry(GamesTreeRelationModule.INSTANCE),
                new CrateEntry(GamesTreeModule.INSTANCE),
                // JSON Tree: the kit's viewer over one document, typed in beside it.
                new CrateEntry(JsonDocStoreModule.INSTANCE, StandardJsModuleType.PURE_LOGIC),
                new CrateEntry(JsonTextModule.INSTANCE, StandardJsModuleType.PURE_LOGIC),
                new CrateEntry(JsonTextSecretaryModule.INSTANCE, StandardJsModuleType.SECRETARY),
                new CrateEntry(JsonInputModule.INSTANCE),
                new CrateEntry(JsonDisplayModule.INSTANCE),
                new CrateEntry(ProsePagesModule.INSTANCE),
                new CrateEntry(ProseNotesModule.INSTANCE),
                new CrateEntry(ProseReferenceModule.INSTANCE),
                new CrateEntry(ProseFiguresModule.INSTANCE),
                // The Colour Extents bench: its looks, its two widgets.
                new CrateEntry(ExtentStyles.INSTANCE),
                new CrateEntry(ExtentTableModule.INSTANCE),
                new CrateEntry(ExtentCardModule.INSTANCE),
                // Minesweeper: the game pure, its tiles the cells, Vista's design of the frame, the widget that glues them.
                new CrateEntry(MinesweeperStyles.INSTANCE),
                new CrateEntry(MinesweeperBoardModule.INSTANCE, StandardJsModuleType.PURE_LOGIC),   // the game alone: no DOM
                new CrateEntry(MineCellModule.INSTANCE),
                new CrateEntry(MinesweeperRelationModule.INSTANCE),
                new CrateEntry(MinesweeperVistaSheet.INSTANCE),
                new CrateEntry(MinesweeperVistaModule.INSTANCE),
                new CrateEntry(MinesweeperGameModule.INSTANCE),
                // The benches' page: their manifests, where they are filed and how arranged, and the app.
                new CrateEntry(BenchesModule.INSTANCE),
                new CrateEntry(BenchLayoutModule.INSTANCE, StandardJsModuleType.PURE_LOGIC),
                new CrateEntry(GridBenchesApp.INSTANCE, StandardJsModuleType.CONSUMER));
    }
}
