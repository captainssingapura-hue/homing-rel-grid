package hue.captains.singapura.js.homing.relgrid.workbench;

import hue.captains.singapura.js.homing.catalogue.site.CatalogueRoutes;
import hue.captains.singapura.js.homing.docview.site.DocRoutes;
import hue.captains.singapura.js.homing.workspace.log.store.FileCheckpointStorage;
import hue.captains.singapura.js.homing.workspace.log.store.StoredCheckpointKeeper;
import hue.captains.singapura.js.homing.workspace.shell.server.WorkspaceServer;
import hue.captains.singapura.tao.http.config.HostConfig;
import hue.captains.singapura.tao.http.vertx.VertxActionHost;

import java.nio.file.Path;

/**
 * Runs the grid workbenches: the catalogue, its introduction read in DocView, the benches' page,
 * and the checkpoints that keep each bench's state. {@code mvn -Pworkbench -pl rel-grid-workbench
 * exec:java}, on 8083 unless {@code -Dworkbench.port} says otherwise; checkpoints kept under
 * {@code target/workspace-checkpoints} unless {@code -Dworkspace.checkpoints} says where.
 */
public final class GridWorkbenchServer {

    private static final int DEFAULT_PORT = 8083;

    private GridWorkbenchServer() {}

    public static void main(String[] args) {
        int port = Integer.getInteger("workbench.port", DEFAULT_PORT);
        var storage = new FileCheckpointStorage(Path.of(System.getProperty("workspace.checkpoints", "target/workspace-checkpoints")));
        var site = GridWorkbenchSite.MPA.registry(GridWorkbenchSite.INSTANCE);
        var routes = WorkspaceServer.with(DocRoutes.with(CatalogueRoutes.with(site, GridWorkbenchSite.ROUTER), GridWorkbenchSite.ROUTER),
                new StoredCheckpointKeeper(storage));
        new VertxActionHost(routes, HostConfig.http(port)).start()
                .onSuccess(s -> System.out.println("[grid-workbench] " + Benches.ALL.size() + " bench(es) - http://localhost:" + s.actualPort()
                        + "/ - checkpoints kept under " + storage.root()))
                .onFailure(err -> { err.printStackTrace(); System.exit(1); });
    }
}
