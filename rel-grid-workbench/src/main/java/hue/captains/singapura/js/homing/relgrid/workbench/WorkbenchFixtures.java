package hue.captains.singapura.js.homing.relgrid.workbench;

import hue.captains.singapura.js.homing.core.AppModule;
import hue.captains.singapura.js.homing.core.Component;
import hue.captains.singapura.js.homing.core.Layer;
import hue.captains.singapura.js.homing.core.Theme;
import hue.captains.singapura.js.homing.core.ThemeGlobals;
import hue.captains.singapura.js.homing.core.ThemeVariables;
import hue.captains.singapura.js.homing.reltree.RelTreeStockStyles;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The workbench's fixtures: the starter's, with one thing added — every
 * theme's globals carry the <b>keyframes the deployed crates name</b>.
 *
 * <p>A CSS keyframe animation is two halves: a class that plays it, which the
 * typed sheet renders, and a {@code @keyframes} that defines the movement,
 * which it cannot — an at-rule has no home in a class rule. So a crate
 * declares its movement as raw CSS beside the class ({@link
 * RelTreeStockStyles#KEYFRAMES}) and the deployment installs it here, in the
 * Component chunk of each theme's globals, once per theme. This is a
 * <b>proof of concept</b> for a typed {@code CssKeyframes} the class would
 * depend on — checked at compile time, themeable — which is the framework's
 * to add; until then this is where the two halves are joined, and a
 * deployment that does not join them gets a ring that does not turn.</p>
 */
public record WorkbenchFixtures<S extends Studio<?>>(StudioStarterFixtures<S> starter) implements Fixtures<S> {

    /** Every movement a deployed crate names. One today. */
    static final List<String> KEYFRAMES = List.of(RelTreeStockStyles.KEYFRAMES);

    @Override public Umbrella<S> umbrella() { return starter.umbrella(); }
    @Override public List<AppModule<?, ?>> harnessApps() { return starter.harnessApps(); }
    @Override public Map<String, GetAction<RoutingContext, ?, ?, ?>> harnessGetActions() { return starter.harnessGetActions(); }
    @Override public Set<String> servableModuleClasses() { return starter.servableModuleClasses(); }
    @Override public Map<String, PostAction<RoutingContext, ?, ?, ?>> harnessPostActions() { return starter.harnessPostActions(); }
    @Override public Theme defaultTheme() { return starter.defaultTheme(); }
    @Override public StudioBrand brand() { return starter.brand(); }
    @Override public NodeChrome chromeFor(Umbrella<S> umbrella) { return starter.chromeFor(umbrella); }

    /** The starter's registry, its globals decorated. */
    @Override public ThemeRegistry themeRegistry() { return withKeyframes(starter.themeRegistry(), KEYFRAMES); }

    /** A registry whose every globals also carries the keyframes, in its Component chunk. */
    static ThemeRegistry withKeyframes(ThemeRegistry inner, List<String> keyframes) {
        var decorated = new ArrayList<ThemeGlobals<?>>();
        for (ThemeGlobals<?> g : inner.globals()) decorated.add(new Keyframed<>(g, String.join("\n", keyframes)));
        var globals = List.copyOf(decorated);
        return new ThemeRegistry() {
            @Override public List<Theme> themes() { return inner.themes(); }
            @Override public List<ThemeVariables<?>> variables() { return inner.variables(); }
            @Override public List<ThemeGlobals<?>> globals() { return globals; }
        };
    }

    /**
     * A theme's globals with the keyframes appended to the Component chunk —
     * or to the flat css, for a theme that keeps no chunks. The keyframes sit
     * inside the component layer, where the class that plays them renders too.
     */
    record Keyframed<TH extends Theme>(ThemeGlobals<TH> inner, String keyframes) implements ThemeGlobals<TH> {
        @Override public TH theme() { return inner.theme(); }
        @Override public String css() {
            return inner.chunks().isEmpty() ? inner.css() + "\n" + keyframes : inner.css();
        }
        @Override public Map<Class<? extends Layer>, String> chunks() {
            var chunks = inner.chunks();
            if (chunks.isEmpty()) return chunks;
            var out = new LinkedHashMap<Class<? extends Layer>, String>(chunks);
            out.merge(Component.class, keyframes, (was, more) -> was + "\n" + more);
            return out;
        }
    }
}
