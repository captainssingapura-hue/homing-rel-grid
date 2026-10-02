package hue.captains.singapura.js.homing.relgrid.workbench;

import hue.captains.singapura.js.homing.docview.app.DocViewLeaves;
import hue.captains.singapura.js.homing.site.catalogue.L0_Catalogue;
import hue.captains.singapura.js.homing.site.catalogue.Leaf;
import hue.captains.singapura.js.homing.site.mpa.Mpa;
import hue.captains.singapura.js.homing.tree.NodeName;
import hue.captains.singapura.js.homing.workspace.site.GroupedWorkspacePageModule;

import java.util.List;

/**
 * The workbench's root: what a bench is and why these are their own, read in DocView; and the
 * benches, the group's page - each bench a workspace of specimens docked side by side.
 */
public record GridWorkbenchCatalogue() implements L0_Catalogue<GridWorkbenchCatalogue> {

    public static final GridWorkbenchCatalogue INSTANCE = new GridWorkbenchCatalogue();

    @Override public String name()    { return "Grid Workbenches"; }
    @Override public String summary() { return "Benches for the Relation Grid, each asking one question the companion demos cannot. A bench is a workspace of specimens docked side by side, where each specimen is a control for the others - not a gallery of things that work, but a set of shapes built to make a defect visible."; }

    @Override
    public List<Leaf<GridWorkbenchCatalogue>> leaves(Mpa mpa) {
        var group = Benches.SITE.home();
        return List.of(
                // What a bench is, why these are their own, and how to add one.
                DocViewLeaves.viewed(this, mpa, GridWorkbenchIntroDoc.INSTANCE),
                // The way in: the group's page, landing on its default bench; every other bench a switch away.
                Leaf.of(this, new NodeName(group.id().value()), group.title(),
                                "Open the benches. Each is arranged the first time; dock more specimens from the picker - each a control for "
                              + "the others, and comparing them is the point. Switch between benches from the workspace controls.",
                                mpa.page(GridBenchesApp.INSTANCE, new GroupedWorkspacePageModule.Params(group.id().value(), true, "")))
                        .badge("WORKSPACE").icon("🧪"));
    }
}
