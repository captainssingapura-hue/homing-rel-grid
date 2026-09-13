package hue.captains.singapura.js.homing.relgrid;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;

import java.util.List;

/**
 * RFC 0050 · Episode 2 — {@code RelGridHeaders}: the header cells' registry,
 * grid side — the twin of {@link RelGridCellsModule} one band up. Keyed by
 * column: the header cell the relation answered {@code headerFor} with, and
 * the element it owns, asked once per presentation of the column and placed
 * into the grid's header slot. Detach forgets; nothing is disposed. Imports
 * nothing: it holds references and places elements.
 */
public record RelGridHeadersModule() implements DomModule<RelGridHeadersModule> {

    public record RelGridHeaders() implements Exportable._Constant<RelGridHeadersModule> {}

    public static final RelGridHeadersModule INSTANCE = new RelGridHeadersModule();

    @Override public ImportsFor<RelGridHeadersModule> imports() { return ImportsFor.noImports(); }

    @Override public ExportsOf<RelGridHeadersModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new RelGridHeaders()));
    }
}
