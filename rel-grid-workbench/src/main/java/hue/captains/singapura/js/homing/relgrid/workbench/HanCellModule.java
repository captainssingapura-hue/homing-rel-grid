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
 * the only geometry anybody sets. Two punctuation marks share a square, each
 * drawn in a half-width box with the font's half-width alternates asked for.
 * A display cell: it offers neither half of the handover, so the grid never
 * asks it, and the article is edited as text elsewhere. A <b>narrow</b> cell
 * is the half-square of the leading or trailing column: half as wide, as
 * tall, one squeezed mark at the size the squares draw theirs.
 */
public record HanCellModule() implements DomModule<HanCellModule> {

    public record HanCell() implements Exportable._Class<HanCellModule> {}

    public static final HanCellModule INSTANCE = new HanCellModule();

    @Override
    public ImportsFor<HanCellModule> imports() {
        // What a mark is, is the engine's to say; the cell only draws it.
        return ImportsFor.<HanCellModule>builder()
                .add(new ModuleImports<>(List.of(new HanLayout.hanIsPunct(), new HanLayout.hanIsOpener()), HanLayout.INSTANCE))
                .add(new ModuleImports<>(List.of(new HanCellStyles.han_glyph(), new HanCellStyles.han_ink(), new HanCellStyles.han_punct(),
                        new HanCellStyles.han_half(), new HanCellStyles.han_open(), new HanCellStyles.han_narrow(),
                        new HanCellStyles.han_run()), HanCellStyles.INSTANCE))
                .build();
    }

    @Override public ExportsOf<HanCellModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new HanCell()));
    }
}
