package hue.captains.singapura.js.homing.relgrid.workbench;

import hue.captains.singapura.js.homing.component.BranchComponent;
import hue.captains.singapura.js.homing.component.keyboard.NeedKeyboard;
import hue.captains.singapura.js.homing.component.keyboard.KeyBinding;
import hue.captains.singapura.js.homing.component.keyboard.Key;
import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;

import java.util.List;

/**
 * The bench's <b>copier</b> and its <b>copy panel</b> — the domain's answer to
 * {@code RelGridCopyRequested}, the first question the grid waits for
 * (RFC 0050 · Episode 2, map 6 and ext6).
 *
 * <p>One export, {@code DishClipboard}: one per table, made over its relation and the branch its
 * panels go on; {@code answer(question, mask)} mints the choice on a branch of its own and hands
 * it to the grid's panel —
 * {@code mask.panel(element)} — and answers a promise — content for a format,
 * nothing for Cancel — that the grid writes. The content itself is composed by
 * {@code DishClipboardFormats}, pure logic over the relation's own cells.</p>
 *
 * <p>Imports the formats and nothing of the grid: a domain answers through
 * the protocol, and depends on nothing else to do it.</p>
 */
public record DishClipboardModule() implements DomModule<DishClipboardModule> {

    public record DishClipboard() implements BranchComponent<DishClipboardModule>, NeedKeyboard {
        @Override public String summary() { return "The copy's format, chosen on the grid's panel: TSV, CSV or HTML, a header toggle and a live preview."; }
        @Override public List<KeyBinding> keys() {
            return List.of(KeyBinding.of(Key.T, "copied as TSV"), KeyBinding.of(Key.C, "copied as CSV"), KeyBinding.of(Key.H, "copied as HTML"), KeyBinding.of(Key.ARROW_LEFT, "the previous format"), KeyBinding.of(Key.ARROW_RIGHT, "the next format"), KeyBinding.of(Key.ENTER, "copied in the format focused"), KeyBinding.of(Key.ESCAPE, "the copy cancelled"));
        }
    }

    public static final DishClipboardModule INSTANCE = new DishClipboardModule();

    @Override
    public ImportsFor<DishClipboardModule> imports() {
        return ImportsFor.<DishClipboardModule>builder()
                .add(new ModuleImports<>(List.of(new DishClipboardFormatsModule.DishClipboardFormats()), DishClipboardFormatsModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new DishCopyStyles.wb_copy(), new DishCopyStyles.wb_copy_head(),
                        new DishCopyStyles.wb_copy_title(), new DishCopyStyles.wb_copy_sub(), new DishCopyStyles.wb_copy_opts(),
                        new DishCopyStyles.wb_copy_opt(), new DishCopyStyles.wb_copy_opt_hot(), new DishCopyStyles.wb_copy_hint(),
                        new DishCopyStyles.wb_copy_preview(), new DishCopyStyles.wb_copy_foot(), new DishCopyStyles.wb_copy_label(),
                        new DishCopyStyles.wb_copy_check(), new DishCopyStyles.wb_copy_keys(), new DishCopyStyles.wb_copy_cancel()),
                        DishCopyStyles.INSTANCE))
                .build();
    }

    @Override public ExportsOf<DishClipboardModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new DishClipboard()));
    }
}
