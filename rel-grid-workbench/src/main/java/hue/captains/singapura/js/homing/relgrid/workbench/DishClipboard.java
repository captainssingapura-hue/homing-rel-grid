package hue.captains.singapura.js.homing.relgrid.workbench;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.relgrid.protocol.RelGridProtocolModule;

import java.util.List;

/**
 * The bench's <b>copier</b> and its <b>copy panel</b> — the domain's answer to
 * {@code RelGridCopyRequested}, the first question the grid waits for
 * (RFC 0050 · Episode 2, map 6 and ext6).
 *
 * <p>Two exports. {@code dishClipboardContent(relation, blocks, format)} is
 * pure over the relation's own cells and composes TSV, CSV or HTML; it reads
 * cells rather than the store (law 48), which is how a rating comes out as a
 * number in text and as stars in HTML. {@code dishCopyPanel(relation, question,
 * mask, { branch })} mints the choice on a branch of its own and hands it to the
 * grid's panel — {@code mask.panel(element)} — and answers a promise — content
 * for a format, nothing for Cancel — that the grid writes.</p>
 *
 * <p>Imports the protocol and nothing of the grid: a domain answers through
 * the protocol, and depends on nothing else to do it.</p>
 */
public record DishClipboard() implements DomModule<DishClipboard> {

    public record dishClipboardContent() implements Exportable._Constant<DishClipboard> {}
    public record dishCopyPanel()        implements Exportable._Constant<DishClipboard> {}

    public static final DishClipboard INSTANCE = new DishClipboard();

    @Override
    public ImportsFor<DishClipboard> imports() {
        return ImportsFor.<DishClipboard>builder()
                .add(new ModuleImports<>(
                        List.of(new RelGridProtocolModule.RelGridClipboardContent()),
                        RelGridProtocolModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new DishCopyStyles.wb_copy(), new DishCopyStyles.wb_copy_head(),
                        new DishCopyStyles.wb_copy_title(), new DishCopyStyles.wb_copy_sub(), new DishCopyStyles.wb_copy_opts(),
                        new DishCopyStyles.wb_copy_opt(), new DishCopyStyles.wb_copy_opt_hot(), new DishCopyStyles.wb_copy_hint(),
                        new DishCopyStyles.wb_copy_preview(), new DishCopyStyles.wb_copy_foot(), new DishCopyStyles.wb_copy_label(),
                        new DishCopyStyles.wb_copy_check(), new DishCopyStyles.wb_copy_keys(), new DishCopyStyles.wb_copy_cancel()),
                        DishCopyStyles.INSTANCE))
                .build();
    }

    @Override public ExportsOf<DishClipboard> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new dishClipboardContent(), new dishCopyPanel()));
    }
}
