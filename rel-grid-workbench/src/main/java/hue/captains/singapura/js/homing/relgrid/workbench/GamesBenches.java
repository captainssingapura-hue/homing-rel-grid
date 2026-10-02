package hue.captains.singapura.js.homing.relgrid.workbench;

import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.workspace.groups.core.models.ArrangedWidget;
import hue.captains.singapura.js.homing.workspace.groups.core.models.Arrangement;
import hue.captains.singapura.js.homing.workspace.groups.core.models.SplitGrid;
import hue.captains.singapura.js.homing.workspace.groups.core.models.WorkspaceArrangements;
import hue.captains.singapura.js.homing.workspace.widgets.NoParams;
import hue.captains.singapura.js.homing.workspace.widgets.WidgetDeclaration;
import hue.captains.singapura.js.homing.workspace.widgets.WidgetQuery;
import hue.captains.singapura.js.homing.workspace.widgets.WorkspaceDeclaration;

import java.util.List;

/**
 * The two benches over the games catalogue: the Games Catalogue - sorting and filtering from
 * header cells that are the relation's own - and the Games Tree - the same catalogue as a lazy
 * tree on the tree view. Each a widget with its own data, filling its bench.
 */
public final class GamesBenches {

    private GamesBenches() {}

    /** The catalogue in the grid, declared: {@code games-catalogue}, no params, nothing shared. */
    public record Catalogue() implements WidgetDeclaration<NoParams> {
        public static final Catalogue INSTANCE = new Catalogue();
        @Override public String kind() { return "games-catalogue"; }
        @Override public String title() { return "Games catalogue"; }
        @Override public Class<NoParams> paramsType() { return NoParams.class; }
        @Override public WidgetQuery<NoParams> query() { return new NoParams.Query(); }
        @Override public ModuleImports<?> constructs() {
            return new ModuleImports<>(List.of(new GamesCatalogueModule.GamesCatalogue()), GamesCatalogueModule.INSTANCE);
        }
    }

    /** The catalogue in the tree view, declared: {@code games-tree}, no params, nothing shared. */
    public record Tree() implements WidgetDeclaration<NoParams> {
        public static final Tree INSTANCE = new Tree();
        @Override public String kind() { return "games-tree"; }
        @Override public String title() { return "Games tree"; }
        @Override public Class<NoParams> paramsType() { return NoParams.class; }
        @Override public WidgetQuery<NoParams> query() { return new NoParams.Query(); }
        @Override public ModuleImports<?> constructs() {
            return new ModuleImports<>(List.of(new GamesTreeModule.GamesTree()), GamesTreeModule.INSTANCE);
        }
    }

    /** The Games Catalogue bench: {@code games-catalogue}. */
    public record CatalogueBench() implements WorkspaceDeclaration {
        public static final CatalogueBench INSTANCE = new CatalogueBench();
        @Override public String name() { return "games-catalogue"; }
        @Override public List<WidgetDeclaration<?>> kinds() { return List.of(Catalogue.INSTANCE); }
    }

    /** The Games Tree bench: {@code games-tree}. */
    public record TreeBench() implements WorkspaceDeclaration {
        public static final TreeBench INSTANCE = new TreeBench();
        @Override public String name() { return "games-tree"; }
        @Override public List<WidgetDeclaration<?>> kinds() { return List.of(Tree.INSTANCE); }
    }

    /** The first time: the catalogue, filling its bench. */
    public static final WorkspaceArrangements<CatalogueBench> CATALOGUE_ARRANGED = WorkspaceArrangements.of(CatalogueBench.INSTANCE,
            Arrangement.of(CatalogueBench.INSTANCE, SplitGrid.of(SplitGrid.region("catalogue", "catalogue")),
                    ArrangedWidget.of("catalogue", Catalogue.INSTANCE.kind())));

    /** The first time: the tree, filling its bench. */
    public static final WorkspaceArrangements<TreeBench> TREE_ARRANGED = WorkspaceArrangements.of(TreeBench.INSTANCE,
            Arrangement.of(TreeBench.INSTANCE, SplitGrid.of(SplitGrid.region("tree", "tree")),
                    ArrangedWidget.of("tree", Tree.INSTANCE.kind())));
}
