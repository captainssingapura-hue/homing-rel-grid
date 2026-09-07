package hue.captains.singapura.js.homing.relgrid.workbench;

import hue.captains.singapura.js.homing.studio.base.Doc;
import hue.captains.singapura.js.homing.studio.base.DocProvider;
import hue.captains.singapura.js.homing.studio.base.app.DocReader;
import hue.captains.singapura.js.homing.studio.base.app.Entry;
import hue.captains.singapura.js.homing.studio.base.app.L0_Catalogue;
import hue.captains.singapura.js.homing.studio.base.app.Navigable;
import hue.captains.singapura.js.homing.workspace.shell.GenericWorkspace;

import java.util.List;

/**
 * Home catalogue for the grid workbench studio: the note that says what a bench
 * is, and <b>one</b> door into the benches.
 *
 * <p><b>Not one tile per bench.</b> Registering a {@code WorkspaceSpec} is
 * already enough to reach it: {@code GenericWorkspaceChrome} serialises the
 * whole registry to the client, so the workspace control modal offers
 * cross-kind switching between every registered bench. A leaf per bench would
 * restate a list the substrate already carries, and would then have to be kept
 * in step with it. {@link GridWorkbenchStudio#BENCHES} is the one list.</p>
 *
 * <p>The single entry below lands on the first bench and exists because a studio
 * needs a way in — from there the modal reaches the rest.</p>
 */
public record GridWorkbenchHome() implements L0_Catalogue<GridWorkbenchHome>, DocProvider {

    public static final GridWorkbenchHome INSTANCE = new GridWorkbenchHome();

    @Override public String name()    { return "Grid Workbenches"; }
    @Override public String summary() { return "Benches for the Relation Grid, each asking one question the companion demos cannot. A bench is a workspace of specimens docked side by side, where each specimen is a control for the others — not a gallery of things that work, but a set of shapes built to make a defect visible."; }

    @Override public List<Entry<GridWorkbenchHome>> leaves() {
        return List.of(
                // What a bench is, why these are their own studio, and how to add one.
                Entry.of(this, DocReader.INSTANCE,
                        new DocReader.Params(GridWorkbenchIntroDoc.INSTANCE.uuid().toString()),
                        GridWorkbenchIntroDoc.INSTANCE),
                // The way in. Lands on the first bench; every other registered
                // bench is one kind-switch away inside the workspace itself.
                Entry.of(this, new Navigable<>(
                        GenericWorkspace.INSTANCE,
                        new GenericWorkspace.Params(GridWorkbenchStudio.landingKind()),
                        "Benches",
                        "Open the workbench. Dock specimens from the picker, two at a time — "
                      + "each is a control for the others, and comparing them is the point. "
                      + "Switch between benches from the workspace controls."))
        );
    }

    /** Required so the intro doc is reachable through the studio's DocRegistry
     *  — {@code Entry.OfDoc} validates reachability at boot. */
    @Override public List<Doc> docs() {
        return List.of(GridWorkbenchIntroDoc.INSTANCE);
    }
}
