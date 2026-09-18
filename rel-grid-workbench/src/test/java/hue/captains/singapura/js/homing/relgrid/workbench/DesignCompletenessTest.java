package hue.captains.singapura.js.homing.relgrid.workbench;

import hue.captains.singapura.js.homing.core.CssGroup;
import hue.captains.singapura.js.homing.core.Theme;
import hue.captains.singapura.js.homing.design.Deployment;
import hue.captains.singapura.js.homing.design.Design;
import hue.captains.singapura.js.homing.design.DesignClass;
import hue.captains.singapura.js.homing.server.ServedModules;
import hue.captains.singapura.js.homing.studio.themes.StudioThemeRegistry;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * RFC 0065 — the completeness law over what the grid, the tree, the group,
 * the kit and the benches wear or read: every pair has a word from every
 * design the studio registers, and every word is valid for its target — no
 * pair missing, no reference dangling, no colour literal off the colour
 * plane. The findings are the test, one deployment per look.
 */
class DesignCompletenessTest {

    /** Everything the workbench's closure wears or reads, off the served groups — the same set the renderer cuts to. */
    static Set<DesignClass<?>> worn() {
        var served = ServedModules.of(List.of(RelGridWorkbenchCrate.INSTANCE));
        var groups = new ArrayList<CssGroup<?>>();
        for (var m : served.byName().values()) if (m instanceof CssGroup<?> g) groups.add(g);
        return Deployment.wornBy(groups);
    }

    @Test
    void theWorkbenchWearsTheGridsWords() {
        var worn = worn();
        assertTrue(worn.size() > 20, "the grid, the tree and the benches wear a few dozen pairs; found " + worn.size());
    }

    @Test
    void everyDesign_answersEveryPairTheWorkbenchWears_validly() {
        var worn = worn();
        for (Theme t : StudioThemeRegistry.INSTANCE.themes()) {
            Design d = (Design) t;
            var r = Deployment.of(worn, d).resolve();
            assertEquals(List.of(), r.findings(), () -> d.slug() + ": " + r.findings());
            assertEquals(worn.size(), r.impls().size(), d.slug() + " resolved fewer pairs than worn");
        }
    }
}
