package hue.captains.singapura.js.homing.relgrid.workbench;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;

import java.util.List;

/**
 * What the Articles bench puts between its poems: a title above each, an
 * illustration where a zero-row member stands, a colophon after the last.
 * Fences are domain objects handed a host, as cells are; this module imports
 * nothing.
 */
public record HanFences() implements DomModule<HanFences> {

    public record createHanTitleFence()    implements Exportable._Constant<HanFences> {}
    public record createHanOrnamentFence() implements Exportable._Constant<HanFences> {}
    public record createHanColophonFence() implements Exportable._Constant<HanFences> {}

    public static final HanFences INSTANCE = new HanFences();

    @Override public ImportsFor<HanFences> imports() { return ImportsFor.noImports(); }

    @Override public ExportsOf<HanFences> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new createHanTitleFence(), new createHanOrnamentFence(), new createHanColophonFence()));
    }
}
