package hue.captains.singapura.js.homing.relgrid.workbench;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;

import java.util.List;

/**
 * What the Articles bench puts between its poems: a title above each, an
 * illustration where a zero-row member stands, a colophon after the last.
 * Fences are nouns, as cells are: each owns its element, minted on the branch
 * it was handed, and answers {@code fenceElement()} to whoever places it; this
 * module imports nothing.
 */
public record HanFences() implements DomModule<HanFences> {

    public record createHanTitleFence()    implements Exportable._Constant<HanFences> {}
    public record createHanOrnamentFence() implements Exportable._Constant<HanFences> {}
    public record createHanColophonFence() implements Exportable._Constant<HanFences> {}

    public static final HanFences INSTANCE = new HanFences();

    @Override public ImportsFor<HanFences> imports() {
        return ImportsFor.<HanFences>builder()
                .add(new ModuleImports<>(List.of(new HanFenceStyles.wb_hanf(), new HanFenceStyles.wb_hanf_title(),
                        new HanFenceStyles.wb_hanf_t(), new HanFenceStyles.wb_hanf_a(), new HanFenceStyles.wb_hanf_orn(),
                        new HanFenceStyles.wb_hanf_g(), new HanFenceStyles.wb_hanf_n(), new HanFenceStyles.wb_hanf_colophon()),
                        HanFenceStyles.INSTANCE))
                .build();
    }

    @Override public ExportsOf<HanFences> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new createHanTitleFence(), new createHanOrnamentFence(), new createHanColophonFence()));
    }
}
