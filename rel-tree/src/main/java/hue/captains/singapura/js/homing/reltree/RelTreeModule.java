package hue.captains.singapura.js.homing.reltree;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;

import java.util.List;
import hue.captains.singapura.js.homing.relgrid.protocol.RelGridProtocolModule;

/**
 * RFC 0050 · Episode 3-ext1 — {@code RelTree}: the facade, the only place the
 * two branches meet. It asks the relation for its places and its cells, holds
 * exactly the presented places, asks the fold and unfold on the channel, and
 * never asks for, holds, pushes or writes a value.
 */
public record RelTreeModule() implements DomModule<RelTreeModule> {
    public record RelTree() implements Exportable._Constant<RelTreeModule> {}
    public static final RelTreeModule INSTANCE = new RelTreeModule();
    @Override public ImportsFor<RelTreeModule> imports() {
        return ImportsFor.<RelTreeModule>builder()
                .add(new ModuleImports<>(List.of(new RelTreePlacesModule.RelTreePlaces()),     RelTreePlacesModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new RelTreeLayoutModule.RelTreeLayout()),     RelTreeLayoutModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new RelTreeCellsModule.RelTreeCells()),       RelTreeCellsModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new RelTreeCursorModule.RelTreeCursor()),     RelTreeCursorModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new RelTreeChannelModule.RelTreeChannel()),   RelTreeChannelModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new RelTreeGesturesModule.RelTreeGestures()), RelTreeGesturesModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new RelGridProtocolModule.RelTreeViewChanged()), RelGridProtocolModule.INSTANCE))
                .build();
    }
    @Override public ExportsOf<RelTreeModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new RelTree()));
    }
}
