package hue.captains.singapura.js.homing.relgrid.workbench;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.relgrid.prose.ProseTableModule;
import hue.captains.singapura.js.homing.ui.elements.Heading2Module;
import hue.captains.singapura.js.homing.ui.elements.LedeModule;
import hue.captains.singapura.js.homing.ui.look.CastingsModule;
import hue.captains.singapura.js.homing.ui.look.LookModule;

import java.util.List;

/**
 * The Document Table bench's pages: a document's tables as a document has them - a heading, its ledes, and
 * {@code ProseTable}s between, the house's words throughout. Components on the branch each is handed; the bench's
 * widgets show them.
 */
public record ProsePagesModule() implements DomModule<ProsePagesModule> {

    public static final ProsePagesModule INSTANCE = new ProsePagesModule();

    public record ProseNotesPage() implements Exportable._Class<ProsePagesModule> {}
    public record ProseReferencePage() implements Exportable._Class<ProsePagesModule> {}
    public record ProseFiguresPage() implements Exportable._Class<ProsePagesModule> {}

    @Override public ImportsFor<ProsePagesModule> imports() {
        return ImportsFor.<ProsePagesModule>builder()
                .add(new ModuleImports<>(List.of(new ProseTableModule.ProseTable()), ProseTableModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new Heading2Module.Heading2()), Heading2Module.INSTANCE))
                .add(new ModuleImports<>(List.of(new LedeModule.Lede()), LedeModule.INSTANCE))
                // the house's code, which the reference's inline drawing sets its options in
                .add(new ModuleImports<>(List.of(new LookModule.Look()), LookModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new CastingsModule.ROLE()), CastingsModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new WorkbenchStyles.wb_page()), WorkbenchStyles.INSTANCE))
                .build();
    }

    @Override public ExportsOf<ProsePagesModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new ProseNotesPage(), new ProseReferencePage(), new ProseFiguresPage()));
    }
}
