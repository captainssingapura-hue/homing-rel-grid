package hue.captains.singapura.js.homing.relgrid.workbench;

import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.workspace.groups.core.models.ArrangedWidget;
import hue.captains.singapura.js.homing.workspace.groups.core.models.Arrangement;
import hue.captains.singapura.js.homing.workspace.groups.core.models.SplitGrid;
import hue.captains.singapura.js.homing.workspace.groups.core.models.WorkspaceArrangements;
import hue.captains.singapura.js.homing.workspace.parties.PartyType;
import hue.captains.singapura.js.homing.workspace.widgets.NoParams;
import hue.captains.singapura.js.homing.workspace.widgets.WidgetDeclaration;
import hue.captains.singapura.js.homing.workspace.widgets.WidgetQuery;
import hue.captains.singapura.js.homing.workspace.widgets.WorkspaceDeclaration;

import java.util.List;

/**
 * The Replicating Tables bench: the proof that a grid holding no value needs no telling - three
 * editors with different rights and any number of followers, every table a replica of the one
 * store the {@code dishes} party keeps; and the outlets, three books stacked in a group.
 */
public final class ReplicatingTablesBench {

    private ReplicatingTablesBench() {}

    /** The chef: edits ingredient and style. One per bench. */
    public record Chef() implements WidgetDeclaration<NoParams> {
        public static final Chef INSTANCE = new Chef();
        @Override public String kind() { return "dish-chef"; }
        @Override public String title() { return "Chef"; }
        @Override public Class<NoParams> paramsType() { return NoParams.class; }
        @Override public WidgetQuery<NoParams> query() { return new NoParams.Query(); }
        @Override public List<PartyType<?>> parties() { return List.of(Dishes.TYPE); }
        @Override public boolean single() { return true; }
        @Override public ModuleImports<?> constructs() { return new ModuleImports<>(List.of(new DishChefModule.DishChef()), DishChefModule.INSTANCE); }
    }

    /** The nutritionist: edits calories and the rating. One per bench. */
    public record Nutritionist() implements WidgetDeclaration<NoParams> {
        public static final Nutritionist INSTANCE = new Nutritionist();
        @Override public String kind() { return "dish-nutritionist"; }
        @Override public String title() { return "Nutritionist"; }
        @Override public Class<NoParams> paramsType() { return NoParams.class; }
        @Override public WidgetQuery<NoParams> query() { return new NoParams.Query(); }
        @Override public List<PartyType<?>> parties() { return List.of(Dishes.TYPE); }
        @Override public boolean single() { return true; }
        @Override public ModuleImports<?> constructs() { return new ModuleImports<>(List.of(new DishNutritionistModule.DishNutritionist()), DishNutritionistModule.INSTANCE); }
    }

    /** The shop manager: edits price, and runs a day of trade. One per bench. */
    public record Manager() implements WidgetDeclaration<NoParams> {
        public static final Manager INSTANCE = new Manager();
        @Override public String kind() { return "dish-manager"; }
        @Override public String title() { return "Shop manager"; }
        @Override public Class<NoParams> paramsType() { return NoParams.class; }
        @Override public WidgetQuery<NoParams> query() { return new NoParams.Query(); }
        @Override public List<PartyType<?>> parties() { return List.of(Dishes.TYPE); }
        @Override public boolean single() { return true; }
        @Override public ModuleImports<?> constructs() { return new ModuleImports<>(List.of(new DishManagerModule.DishManager()), DishManagerModule.INSTANCE); }
    }

    /** A follower: a read-only replica. Dock several. */
    public record Follower() implements WidgetDeclaration<NoParams> {
        public static final Follower INSTANCE = new Follower();
        @Override public String kind() { return "dish-follower"; }
        @Override public String title() { return "Follower"; }
        @Override public Class<NoParams> paramsType() { return NoParams.class; }
        @Override public WidgetQuery<NoParams> query() { return new NoParams.Query(); }
        @Override public List<PartyType<?>> parties() { return List.of(Dishes.TYPE); }
        @Override public ModuleImports<?> constructs() { return new ModuleImports<>(List.of(new DishFollowerModule.DishFollower()), DishFollowerModule.INSTANCE); }
    }

    /** The outlets: three books stacked in a group, a ledger of their own. */
    public record Outlets() implements WidgetDeclaration<NoParams> {
        public static final Outlets INSTANCE = new Outlets();
        @Override public String kind() { return "outlets"; }
        @Override public String title() { return "Outlets"; }
        @Override public Class<NoParams> paramsType() { return NoParams.class; }
        @Override public WidgetQuery<NoParams> query() { return new NoParams.Query(); }
        @Override public ModuleImports<?> constructs() { return new ModuleImports<>(List.of(new OutletsModule.Outlets()), OutletsModule.INSTANCE); }
    }

    /** The bench, a workspace: {@code replicating-tables}. */
    public record Workspace() implements WorkspaceDeclaration {
        public static final Workspace INSTANCE = new Workspace();
        @Override public String name() { return "replicating-tables"; }
        @Override public List<WidgetDeclaration<?>> kinds() {
            return List.of(Chef.INSTANCE, Nutritionist.INSTANCE, Manager.INSTANCE, Follower.INSTANCE, Outlets.INSTANCE);
        }
    }

    /** The first time: the three editors above, a follower and the outlets below - each a control for the others. */
    public static final WorkspaceArrangements<Workspace> ARRANGED = WorkspaceArrangements.of(Workspace.INSTANCE,
            Arrangement.of(Workspace.INSTANCE,
                    SplitGrid.of(SplitGrid.column(
                            SplitGrid.row(SplitGrid.region("chef", "chef"), SplitGrid.region("nutritionist", "nutritionist"), SplitGrid.region("manager", "manager")),
                            SplitGrid.row(SplitGrid.region("follower", "follower"), SplitGrid.region("outlets", "outlets")))),
                    ArrangedWidget.of("chef", Chef.INSTANCE.kind()),
                    ArrangedWidget.of("nutritionist", Nutritionist.INSTANCE.kind()),
                    ArrangedWidget.of("manager", Manager.INSTANCE.kind()),
                    ArrangedWidget.of("follower", Follower.INSTANCE.kind()),
                    ArrangedWidget.of("outlets", Outlets.INSTANCE.kind())));
}
