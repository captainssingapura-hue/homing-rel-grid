package hue.captains.singapura.js.homing.relgrid;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.relgrid.protocol.RelGridProtocolModule;

import java.util.List;

/**
 * RFC 0050 · Episode 2 — {@code RelGridCursor}: an identity the grid holds,
 * with the position it was last seen at as the fallback when the identity
 * leaves the presented space. It tells its cell its mode, paints itself through
 * the layout, reports a move, and follows itself into view when the grid has
 * the keyboard. Imports nothing: it works over the maps, layout and cells it
 * is handed.
 */
public record RelGridCursorModule() implements DomModule<RelGridCursorModule> {

    public record RelGridCursor() implements Exportable._Constant<RelGridCursorModule> {}

    public static final RelGridCursorModule INSTANCE = new RelGridCursorModule();

    @Override
    public ImportsFor<RelGridCursorModule> imports() {
        return ImportsFor.<RelGridCursorModule>builder()

                .build();
    }

    @Override public ExportsOf<RelGridCursorModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new RelGridCursor()));
    }
}
