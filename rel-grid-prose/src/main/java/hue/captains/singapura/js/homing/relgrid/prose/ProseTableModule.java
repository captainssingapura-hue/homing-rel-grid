package hue.captains.singapura.js.homing.relgrid.prose;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.relgrid.RelGridModule;
import hue.captains.singapura.js.homing.relgrid.house.HouseProseModule;
import hue.captains.singapura.js.homing.relgrid.protocol.RelGridProtocolModule;
import hue.captains.singapura.js.homing.ui.look.CastingsModule;
import hue.captains.singapura.js.homing.ui.look.LookModule;

import java.util.List;

/**
 * RFC 0050 · Episode 2 — Appendix: The Frame: a document's table, out of the box. Columns and rows of text handed in,
 * and a grid that reads as a document's table - made with the house's document table ({@code HouseProse}), its cells
 * the house's data and header cells, its caption the house's caption. Read only; a copy carries what its cells say.
 */
public record ProseTableModule() implements DomModule<ProseTableModule> {

    public static final ProseTableModule INSTANCE = new ProseTableModule();

    public record ProseTable() implements Exportable._Class<ProseTableModule> {}

    @Override public ImportsFor<ProseTableModule> imports() {
        return ImportsFor.<ProseTableModule>builder()
                .add(new ModuleImports<>(List.of(new RelGridModule.RelGrid()), RelGridModule.INSTANCE))
                // the design it is made with, unless it is handed another
                .add(new ModuleImports<>(List.of(new HouseProseModule.HouseProse()), HouseProseModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new ProseCellsModule.ProseCell(), new ProseCellsModule.ProseHeader()), ProseCellsModule.INSTANCE))
                // a copy: the question it answers, and its answer
                .add(new ModuleImports<>(List.of(new RelGridProtocolModule.RelGridCopyRequested(), new RelGridProtocolModule.RelGridClipboardContent()),
                        RelGridProtocolModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new ProseStyles.prose_table(), new ProseStyles.prose_caption(), new ProseStyles.prose_box()),
                        ProseStyles.INSTANCE))
                // the house's table and caption, worn
                .add(new ModuleImports<>(List.of(new LookModule.Look()), LookModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new CastingsModule.ROLE()), CastingsModule.INSTANCE))
                .build();
    }

    @Override public ExportsOf<ProseTableModule> exports() { return new ExportsOf<>(INSTANCE, List.of(new ProseTable())); }
}
