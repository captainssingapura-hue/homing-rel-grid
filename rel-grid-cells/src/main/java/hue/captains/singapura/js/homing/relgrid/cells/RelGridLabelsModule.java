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
 * RFC 0050 · Episode 2 — the stock header cell that shows a label, one of the out-of-the-box cells (Appendix: The
 * Frame). The grid writes no header text of its own: a relation answers {@code headerFor(column)}, and one that
 * only has labels answers these - {@code RelGridLabels} makes a {@code RelGridLabelHeader} per column, once. Its
 * room and its overflow are its own; its ink and weight are the house's header cell's, worn.
 */
public record RelGridLabelsModule() implements DomModule<RelGridLabelsModule> {

    public record RelGridLabelHeader() implements Exportable._Class<RelGridLabelsModule> {}
    public record RelGridLabels() implements Exportable._Class<RelGridLabelsModule> {}

    public static final RelGridLabelsModule INSTANCE = new RelGridLabelsModule();

    @Override public ImportsFor<RelGridLabelsModule> imports() {
        return ImportsFor.<RelGridLabelsModule>builder()
                .add(new ModuleImports<>(List.of(new RelGridStockStyles.hrg_label(), new RelGridStockStyles.hrg_text_ellipsis(),
                        new RelGridStockStyles.hrg_text_clip(), new RelGridStockStyles.hrg_text_wrap()), RelGridStockStyles.INSTANCE))
                // its words: the house's text cell, cast as a header cell's title (D42)
                .add(new ModuleImports<>(List.of(new TextCellModule.TextCell()), TextCellModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new CastingsModule.ROLE()), CastingsModule.INSTANCE))
                .build();
    }

    @Override public ExportsOf<RelGridLabelsModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new RelGridLabelHeader(), new RelGridLabels()));
    }
}
