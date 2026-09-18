package hue.captains.singapura.js.homing.relgrid.workbench;

import hue.captains.singapura.js.homing.core.AppModule;
import hue.captains.singapura.js.homing.core.Crate;
import hue.captains.singapura.js.homing.core.Theme;
import hue.captains.singapura.js.homing.server.ThemeRegistry;
import hue.captains.singapura.js.homing.studio.base.Fixtures;
import hue.captains.singapura.js.homing.studio.base.Studio;
import hue.captains.singapura.js.homing.studio.base.Umbrella;
import hue.captains.singapura.js.homing.studio.base.app.StudioBrand;
import hue.captains.singapura.js.homing.studio.starter.StudioStarterFixtures;
import hue.captains.singapura.tao.http.action.GetAction;
import hue.captains.singapura.tao.http.action.PostAction;
import io.vertx.ext.web.RoutingContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The workbench's fixtures: the starter's, with the workbench's crate root
 * added — RFC 0044: a served module must be declared in a registered crate,
 * and {@link RelGridWorkbenchCrate} declares the benches and requires the
 * grid family's crates, so the starter's closure plus this one root is all a
 * name may resolve to here.
 *
 * <p>Until homing 0.8.3 this record also joined every theme's globals with
 * the {@code @keyframes} the deployed crates name ({@link
 * hue.captains.singapura.js.homing.reltree.RelTreeStockStyles#KEYFRAMES}) —
 * a proof of concept for a typed keyframes the class would depend on. RFC
 * 0066 retired the globals sheet the join was made in, so the join is gone
 * with it and the busy ring does not turn until the framework gives a
 * movement a typed home; the class that plays it is unchanged.</p>
 */
public record WorkbenchFixtures<S extends Studio<?>>(StudioStarterFixtures<S> starter) implements Fixtures<S> {

    @Override public Umbrella<S> umbrella() { return starter.umbrella(); }
    @Override public List<AppModule<?, ?>> harnessApps() { return starter.harnessApps(); }
    @Override public Map<String, GetAction<RoutingContext, ?, ?, ?>> harnessGetActions() { return starter.harnessGetActions(); }
    @Override public Map<String, PostAction<RoutingContext, ?, ?, ?>> harnessPostActions() { return starter.harnessPostActions(); }
    @Override public Theme defaultTheme() { return starter.defaultTheme(); }
    @Override public StudioBrand brand() { return starter.brand(); }
    @Override public NodeChrome chromeFor(Umbrella<S> umbrella) { return starter.chromeFor(umbrella); }
    @Override public ThemeRegistry themeRegistry() { return starter.themeRegistry(); }

    /** The starter's crate roots, and the workbench's. */
    @Override public List<Crate> crates() {
        var all = new ArrayList<Crate>(starter.crates());
        all.add(RelGridWorkbenchCrate.INSTANCE);
        return List.copyOf(all);
    }
}
