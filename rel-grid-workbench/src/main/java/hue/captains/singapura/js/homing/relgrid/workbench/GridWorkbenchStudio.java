package hue.captains.singapura.js.homing.relgrid.workbench;

import hue.captains.singapura.js.homing.core.AppModule;
import hue.captains.singapura.js.homing.studio.base.Studio;
import hue.captains.singapura.js.homing.studio.base.app.StudioBrand;
import hue.captains.singapura.js.homing.workspace.shell.WorkspaceSpec;

import java.util.List;

/**
 * RFC 0012 — the grid workbench studio's typed bundle. Home is
 * {@link GridWorkbenchHome}; standalone brand labelled "Homing · grid bench".
 *
 * <p><b>{@link #BENCHES} is the whole registration.</b> A bench is added by
 * putting its {@code WorkspaceSpec} in that list and nowhere else. Referencing a
 * spec's singleton runs its static initializer, which registers it with
 * {@code WorkspaceSpecRegistry}; the registry is serialised whole into
 * {@code GenericWorkspaceChrome}, which carries every kind to the client so the
 * workspace control modal can offer <b>cross-kind switching</b>. A registered
 * bench is reachable and switchable without a catalogue leaf of its own.</p>
 *
 * <p><b>No intrinsic apps.</b> Every bench is {@code GenericWorkspace} under a
 * different {@code ws_kind}, which the starter already serves — so this bundle
 * contributes specs and a catalogue, and no {@link AppModule} of its own.</p>
 */
public record GridWorkbenchStudio() implements Studio<GridWorkbenchHome> {

    /**
     * Every bench in this studio, in the order a person meets them. The first is
     * the landing kind for {@link GridWorkbenchHome}'s single workspace entry;
     * the rest are reached by switching kinds from inside the workspace.
     *
     * <p>Class-load is the registration: naming a spec's singleton here runs its
     * static initializer before any request can reach the chrome.</p>
     */
    private static final List<WorkspaceSpec> BENCHES = List.of(
            // Replicating tables: the proof that a grid holding no value needs
            // no telling — one editor, any followers, one persisted store.
            ReplicatingTablesSpec.INSTANCE,
            // Han Article: a WYSIWYG Chinese article in strictly square cells,
            // nine to a row — the bench that will ask the grid for half-width
            // punctuation columns.
            HanArticleSpec.INSTANCE,
            // Endless Table: the stress bench for the two branches — a window
            // of twenty over a million rows, measured through the party.
            EndlessTableSpec.INSTANCE
    );

    public static final GridWorkbenchStudio INSTANCE = new GridWorkbenchStudio();

    /** The benches, in order. Reading this guarantees they are registered. */
    public static List<WorkspaceSpec> benches() { return BENCHES; }

    /** The kind the studio's workspace entry lands on — the first bench. */
    public static String landingKind() { return BENCHES.get(0).kind(); }

    @Override
    public GridWorkbenchHome home() { return GridWorkbenchHome.INSTANCE; }

    /** None: a bench is GenericWorkspace, which the starter already serves. */
    @Override
    public List<AppModule<?, ?>> apps() { return List.of(); }

    @Override
    public StudioBrand standaloneBrand() {
        return new StudioBrand("Homing · grid bench", GridWorkbenchHome.class);
    }
}
