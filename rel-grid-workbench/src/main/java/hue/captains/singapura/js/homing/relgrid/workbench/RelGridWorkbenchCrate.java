package hue.captains.singapura.js.homing.relgrid.workbench;

import hue.captains.singapura.js.homing.core.Crate;
import hue.captains.singapura.js.homing.core.CrateEntry;
import hue.captains.singapura.js.homing.core.js.CoreJsCrate;
import hue.captains.singapura.js.homing.relgrid.RelGridCrate;
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
                // The grid under test.
                RelGridCrate.INSTANCE);
    }

    @Override
    public List<CrateEntry> entries() {
        return List.of(
                CrateEntry.of(WorkbenchStyles.INSTANCE),
                // Replicating Tables: a persisted store, a relation with a role and
                // a cell manager, three editors with different rights, and their
                // followers. The grid is on none of the edit path.
                CrateEntry.of(DishStore.INSTANCE),
                CrateEntry.of(DishRelation.INSTANCE),
                CrateEntry.of(DishChefWidget.INSTANCE),
                CrateEntry.of(DishNutritionistWidget.INSTANCE),
                CrateEntry.of(DishManagerWidget.INSTANCE),
                CrateEntry.of(DishFollowerWidget.INSTANCE));
    }
}
