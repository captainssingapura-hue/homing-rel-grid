package hue.captains.singapura.js.homing.relgrid.cells;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.ui.elements.TextCellModule;
import hue.captains.singapura.js.homing.ui.look.CastingsModule;

import java.util.List;

/**
 * RFC 0050 · Episode 2 — the stock text cell, one of the out-of-the-box cells (Appendix: The Frame). A
 * <b>domain-side</b> cell a relation may build its cell manager from, through the same factory as any domain's:
 * a {@code RelGridTextCell} holds what it shows and is told to change by whoever owns it ({@code set}), never by
 * the grid. Its room and its overflow are its own; its words are a text cell's, the house's component, cast as a
 * data cell's value (RFC 0067, D42). The grid never imports this module; a relation does.
 */
public record RelGridStockCellsModule() implements DomModule<RelGridStockCellsModule> {

    public record RelGridTextCell() implements Exportable._Constant<RelGridStockCellsModule> {}

    public static final RelGridStockCellsModule INSTANCE = new RelGridStockCellsModule();

    @Override public ImportsFor<RelGridStockCellsModule> imports() {
        return ImportsFor.<RelGridStockCellsModule>builder()
                .add(new ModuleImports<>(List.of(new RelGridStockStyles.hrg_text(), new RelGridStockStyles.hrg_text_ro(),
                        new RelGridStockStyles.hrg_text_edit(), new RelGridStockStyles.hrg_text_ellipsis(),
                        new RelGridStockStyles.hrg_text_clip(), new RelGridStockStyles.hrg_text_wrap()), RelGridStockStyles.INSTANCE))
                // its words: the house's text cell, cast as a data cell's value (D42)
                .add(new ModuleImports<>(List.of(new TextCellModule.TextCell()), TextCellModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new CastingsModule.ROLE()), CastingsModule.INSTANCE))
                .build();
    }

    @Override public ExportsOf<RelGridStockCellsModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new RelGridTextCell()));
    }
}
