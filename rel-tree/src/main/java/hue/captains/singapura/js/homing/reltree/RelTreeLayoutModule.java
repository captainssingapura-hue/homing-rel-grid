package hue.captains.singapura.js.homing.reltree;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;

import java.util.List;

/**
 * RFC 0050 · Episode 3-ext1 — {@code RelTreeLayout}: the tree's chrome on its own
 * branch — the wrap, the tree element that is the keyboard host, the rows, the
 * mask and the panel — and the paint of the current row. The channel's
 * SURFACE: the six functions the mask needs.
 */
public record RelTreeLayoutModule() implements DomModule<RelTreeLayoutModule> {
    public record RelTreeLayout() implements Exportable._Constant<RelTreeLayoutModule> {}
    public static final RelTreeLayoutModule INSTANCE = new RelTreeLayoutModule();
    @Override public ImportsFor<RelTreeLayoutModule> imports() {
        return ImportsFor.<RelTreeLayoutModule>builder()
                .add(new ModuleImports<>(List.of(new RelTreeRowsModule.RelTreeRows()), RelTreeRowsModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new RelTreeStyles.hrt_wrap(), new RelTreeStyles.hrt_tree(), new RelTreeStyles.hrt_mask(),
                        new RelTreeStyles.hrt_panel(), new RelTreeStyles.hrt_current(), new RelTreeStyles.hrt_masked()), RelTreeStyles.INSTANCE))
                .build();
    }
    @Override public ExportsOf<RelTreeLayoutModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new RelTreeLayout()));
    }
}
