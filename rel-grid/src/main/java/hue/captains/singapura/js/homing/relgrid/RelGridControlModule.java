package hue.captains.singapura.js.homing.relgrid;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.relgrid.protocol.RelGridProtocolModule;

import java.util.List;

/**
 * RFC 0050 · Episode 2 — {@code RelGridControl}: DEEP, enforced here and
 * nowhere else. The two-stage offer — the column's constraint, then the cell's
 * own mayTakeControl() — and the handover: the cell's editorElement() placed in
 * the layout's anchor, takeControl() answering a thenable the grid holds until
 * it settles, the focus on the editor, and the resume. Imports nothing.
 */
public record RelGridControlModule() implements DomModule<RelGridControlModule> {

    public record RelGridControl() implements Exportable._Constant<RelGridControlModule> {}

    public static final RelGridControlModule INSTANCE = new RelGridControlModule();

    @Override
    public ImportsFor<RelGridControlModule> imports() {
        return ImportsFor.<RelGridControlModule>builder()

                .build();
    }

    @Override public ExportsOf<RelGridControlModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new RelGridControl()));
    }
}
