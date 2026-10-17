package hue.captains.singapura.js.homing.relgrid.prose;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.ui.look.CastingsModule;
import hue.captains.singapura.js.homing.ui.look.LookModule;

import java.util.List;

/**
 * The cells of a document's table: a data cell and a header cell, each wearing the house's data or header cell but for
 * its rules, which the table's design draws on the slots - a strong cell a header cell's weight, a muted one a caption's
 * ink, a badge the house's badge of its tone. Their text is drawn by the table's inline drawing.
 */
public record ProseCellsModule() implements DomModule<ProseCellsModule> {

    public static final ProseCellsModule INSTANCE = new ProseCellsModule();

    public record ProseCell() implements Exportable._Class<ProseCellsModule> {}
    public record ProseHeader() implements Exportable._Class<ProseCellsModule> {}

    @Override public ImportsFor<ProseCellsModule> imports() {
        return ImportsFor.<ProseCellsModule>builder()
                .add(new ModuleImports<>(List.of(new ProseStyles.prose_cell(), new ProseStyles.prose_head(), new ProseStyles.prose_left(),
                        new ProseStyles.prose_center(), new ProseStyles.prose_right(), new ProseStyles.prose_badge()), ProseStyles.INSTANCE))
                // the house's cells, captions and badges, worn
                .add(new ModuleImports<>(List.of(new LookModule.Look()), LookModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new CastingsModule.ROLE()), CastingsModule.INSTANCE))
                .build();
    }

    @Override public ExportsOf<ProseCellsModule> exports() { return new ExportsOf<>(INSTANCE, List.of(new ProseCell(), new ProseHeader())); }
}
