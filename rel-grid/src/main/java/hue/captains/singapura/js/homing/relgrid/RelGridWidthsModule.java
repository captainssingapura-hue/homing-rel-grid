package hue.captains.singapura.js.homing.relgrid;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.relgrid.protocol.RelGridProtocolModule;

import java.util.List;

/**
 * RFC 0050 · Episode 2 — {@code RelGridWidths}: column widths, geometry the
 * grid's alone (map 7). Held by column identity, answered by position for the
 * layout to apply in place; a request is bounded at normalisation and the
 * bounded request is what is held; a column the relation does not have is
 * refused. Imports nothing: it is arithmetic over the view maps it is handed.
 */
public record RelGridWidthsModule() implements DomModule<RelGridWidthsModule> {

    public record RelGridWidths() implements Exportable._Constant<RelGridWidthsModule> {}

    public static final RelGridWidthsModule INSTANCE = new RelGridWidthsModule();

    @Override
    public ImportsFor<RelGridWidthsModule> imports() {
        return ImportsFor.<RelGridWidthsModule>builder()

                .build();
    }

    @Override public ExportsOf<RelGridWidthsModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new RelGridWidths()));
    }
}
