package hue.captains.singapura.js.homing.relgrid.workbench;

import hue.captains.singapura.js.homing.studio.starter.StudioStarterFixtures;
import hue.captains.singapura.js.homing.studio.base.Bootstrap;
import hue.captains.singapura.js.homing.studio.base.DefaultRuntimeParams;
import hue.captains.singapura.js.homing.studio.base.Umbrella;

/**
 * RFC 0012 — launches the grid workbenches as a <b>solo</b> studio: one
 * {@link Umbrella.Solo} over {@link GridWorkbenchStudio}, its own chrome, its
 * own port.
 *
 * <p>Solo rather than another leaf under the demo umbrella, because a bench and
 * a demo have different audiences. A demo shows the grid working; a bench tries
 * to make it fail, and reads badly in a list of things that work. Keeping them
 * apart also keeps the bench set free to grow — a bench per question is the
 * design, so the count only goes up.</p>
 *
 * <p>Landing: {@code /}. Port defaults to {@code 8083}
 * ({@code -Dworkbench.port=...}), kept clear of the demo studio's 8082, the
 * conformance studio's 8091 and the framework studio's 8090, so all of them run
 * side by side.</p>
 *
 * <p>Benches for the <i>old</i> grid — the Table Workbench's scrolling
 * specimens — stay with the demo, which owns that grid. This studio benches
 * the Relation Grid only.</p>
 */
public final class GridWorkbenchServer {

    private static final int DEFAULT_PORT = 8083;

    private GridWorkbenchServer() {}

    public static void main(String[] args) {
        var kinds = GridWorkbenchStudio.benches().stream().map(b -> b.kind()).toList();
        System.out.println("[grid-workbench] " + kinds.size() + " bench(es): " + String.join(", ", kinds));

        var umbrella = new Umbrella.Solo<>(GridWorkbenchStudio.INSTANCE);
        int port = Integer.getInteger("workbench.port", DEFAULT_PORT);
        // The starter's fixtures, with the keyframes the deployed crates name installed in every theme.
        new Bootstrap<>(new WorkbenchFixtures<>(new StudioStarterFixtures<>(umbrella)), new DefaultRuntimeParams(port)).start();
    }
}
