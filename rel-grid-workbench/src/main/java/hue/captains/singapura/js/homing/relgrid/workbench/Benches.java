package hue.captains.singapura.js.homing.relgrid.workbench;

import hue.captains.singapura.js.homing.workspace.groups.core.models.GroupedWorkspace;
import hue.captains.singapura.js.homing.workspace.groups.core.models.WorkspaceGroup;
import hue.captains.singapura.js.homing.workspace.groups.core.models.WorkspaceGroups;
import hue.captains.singapura.js.homing.workspace.groups.core.models.WorkspaceKind;
import hue.captains.singapura.js.homing.workspace.site.GroupedWorkspaces;
import hue.captains.singapura.js.homing.workspace.widgets.WorkspaceDeclaration;

import java.util.List;

/**
 * The benches: each a workspace of specimens docked side by side, each asking one question a
 * demo cannot - filed in one group, {@code benches}, by what they ask of the grid, and each
 * arranged the first time it opens.
 */
public final class Benches {

    private Benches() {}

    /** Every bench, in the order a person meets them. */
    public static final List<WorkspaceDeclaration> ALL = List.of(
            ReplicatingTablesBench.Workspace.INSTANCE,
            HanArticleBench.Workspace.INSTANCE,
            GamesBenches.CatalogueBench.INSTANCE,
            GamesBenches.TreeBench.INSTANCE,
            EndlessBench.Workspace.INSTANCE,
            JsonTreeBench.Workspace.INSTANCE,
            ColourExtentsBench.Workspace.INSTANCE);

    /** The one group, its sections by what a bench asks of the grid. */
    public static final WorkspaceGroups GROUPS = WorkspaceGroups.of(
            WorkspaceGroup.of("benches", "Grid benches")
                    .section("Editing", GroupedWorkspace.of(ReplicatingTablesBench.Workspace.INSTANCE.name(), "Replicating Tables"),
                                        GroupedWorkspace.of(HanArticleBench.Workspace.INSTANCE.name(), "Han Article"))
                    .section("Ordering", GroupedWorkspace.of(GamesBenches.CatalogueBench.INSTANCE.name(), "Games Catalogue"),
                                         GroupedWorkspace.of(GamesBenches.TreeBench.INSTANCE.name(), "Games Tree"))
                    .section("Scale", GroupedWorkspace.of(EndlessBench.Workspace.INSTANCE.name(), "Endless Table"))
                    .section("Offerings", GroupedWorkspace.of(JsonTreeBench.Workspace.INSTANCE.name(), "JSON Tree"))
                    .section("Substrate", GroupedWorkspace.of(ColourExtentsBench.Workspace.INSTANCE.name(), "Colour Extents"))
                    .defaultTo(WorkspaceKind.of(ReplicatingTablesBench.Workspace.INSTANCE.name()))
                    .build());

    /** What the site serves: each bench declared, each filed, each arranged the first time. */
    public static final GroupedWorkspaces SITE = new GroupedWorkspaces(GROUPS, ALL)
            .arranged(ReplicatingTablesBench.ARRANGED, HanArticleBench.ARRANGED, GamesBenches.CATALOGUE_ARRANGED, GamesBenches.TREE_ARRANGED, EndlessBench.ARRANGED, JsonTreeBench.ARRANGED, ColourExtentsBench.ARRANGED);
}
