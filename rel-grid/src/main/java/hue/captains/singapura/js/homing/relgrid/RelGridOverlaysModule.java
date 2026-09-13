package hue.captains.singapura.js.homing.relgrid;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;

import java.util.List;

/**
 * RFC 0050 · Episode 2 — {@code RelGridOverlays}: what the layout lays OVER
 * its slots, in the wrapper's coordinates — the editor's anchor, the mask and
 * the panel in it, a merged cell's host. Each is minted on a sub-branch of
 * the grid's own, made for the occasion and dissolved with it; the panel is
 * the grid's box for the domain's element, placed as a cell's is in a slot.
 * Geometry rides custom properties the element's class reads.
 */
public record RelGridOverlaysModule() implements DomModule<RelGridOverlaysModule> {

    public record RelGridOverlays() implements Exportable._Constant<RelGridOverlaysModule> {}

    public static final RelGridOverlaysModule INSTANCE = new RelGridOverlaysModule();

    @Override
    public ImportsFor<RelGridOverlaysModule> imports() {
        return ImportsFor.<RelGridOverlaysModule>builder()
                .add(new ModuleImports<>(List.of(
                        new RelGridStyles.hrg_edit(), new RelGridStyles.hrg_mask(), new RelGridStyles.hrg_panel(),
                        new RelGridStyles.hrg_merge(), new RelGridStyles.hrg_lead(), new RelGridStyles.hrg_covered(),
                        new RelGridStyles.hrg_group_end()), RelGridStyles.INSTANCE))
                .build();
    }

    @Override public ExportsOf<RelGridOverlaysModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new RelGridOverlays()));
    }
}
