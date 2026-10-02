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
import hue.captains.singapura.js.homing.relgrid.protocol.RelGridProtocolModule;

import java.util.List;

/**
 * The bench's <b>view profiles</b> and the <b>panel</b> a person picks one
 * on — the domain's answer to {@code RelGridViewHandover}, the second question
 * the grid waits for (RFC 0050 · Episode 2).
 *
 * <p>One export, {@code DishViews}. {@code DishViews.profiles()} is the fixed set — a keep
 * and an order each, over the store's values. {@code new DishViews(store)} is one table's
 * memory of which profile it is under, and computes any profile's View over the store as it
 * is now. {@code views.answer(question, mask, { branch })} mints the list on a branch of its
 * own, hands it to the grid's panel — {@code mask.panel(element)} — and answers a promise — a
 * {@code RelGridView} for the chosen profile, nothing for Cancel — that the
 * grid presents exactly as given.</p>
 *
 * <p>The explanation lives here: the grid holds nothing about why its rows
 * are in this order, and {@code views.describe()} is the one line that says
 * so, in the table's own status. Imports the protocol and nothing of the
 * grid.</p>
 */
public record DishViewsModule() implements DomModule<DishViewsModule> {

    public record DishViews() implements BranchComponent<DishViewsModule>, NeedKeyboard {
        @Override public String summary() { return "The order of the dishes, chosen on the grid's panel from a set of profiles; the explanation kept here."; }
        @Override public List<KeyBinding> keys() {
            return List.of(KeyBinding.of(Key.DIGIT_1, "the first profile chosen - and so on, to 9"), KeyBinding.of(Key.ARROW_UP, "the previous profile"), KeyBinding.of(Key.ARROW_DOWN, "the next profile"), KeyBinding.of(Key.ENTER, "the profile focused chosen"), KeyBinding.of(Key.ESCAPE, "the handover cancelled"));
        }
    }

    public static final DishViewsModule INSTANCE = new DishViewsModule();

    @Override
    public ImportsFor<DishViewsModule> imports() {
        return ImportsFor.<DishViewsModule>builder()
                .add(new ModuleImports<>(
                        List.of(new RelGridProtocolModule.RelGridView()),
                        RelGridProtocolModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new DishViewStyles.wb_view(), new DishViewStyles.wb_view_head(),
                        new DishViewStyles.wb_view_title(), new DishViewStyles.wb_view_sub(), new DishViewStyles.wb_view_list(),
                        new DishViewStyles.wb_view_item(), new DishViewStyles.wb_view_item_end(), new DishViewStyles.wb_view_item_hot(),
                        new DishViewStyles.wb_view_held(), new DishViewStyles.wb_view_tick(), new DishViewStyles.wb_view_num(),
                        new DishViewStyles.wb_view_label(), new DishViewStyles.wb_view_rule(), new DishViewStyles.wb_view_count(),
                        new DishViewStyles.wb_view_foot(), new DishViewStyles.wb_view_keys(), new DishViewStyles.wb_view_cancel()),
                        DishViewStyles.INSTANCE))
                .build();
    }

    @Override public ExportsOf<DishViewsModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new DishViews()));
    }
}
