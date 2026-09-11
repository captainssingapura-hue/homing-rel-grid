package hue.captains.singapura.js.homing.relgrid.workbench;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;

import java.util.List;

/**
 * One square, one glyph — the Han Article bench's cell, written against
 * {@code RelGridCellContract}. Strictly square by construction: it fills the
 * column's width and is exactly as tall as it is wide, so the column's width is
 * the only geometry anybody sets. Edits with an {@code <input>}, because that
 * is what an IME composes into, and leaves Enter to the IME while a
 * composition is open. Two punctuation marks share a square, each drawn in
 * a half-width box with the font's half-width alternates asked for.
 */
public record HanCellModule() implements DomModule<HanCellModule> {

    public record HanCell() implements Exportable._Class<HanCellModule> {}

    public static final HanCellModule INSTANCE = new HanCellModule();

    @Override
    public ImportsFor<HanCellModule> imports() {
        // What a mark is, is the engine's to say; the cell only draws it.
        return ImportsFor.<HanCellModule>builder()
                .add(new ModuleImports<>(List.of(new HanLayout.hanIsPunct()), HanLayout.INSTANCE))
                .build();
    }

    @Override public ExportsOf<HanCellModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new HanCell()));
    }
}
