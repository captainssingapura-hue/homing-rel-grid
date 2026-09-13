package hue.captains.singapura.js.homing.relgrid;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.relgrid.protocol.RelGridProtocolModule;

import java.util.List;

/**
 * RFC 0050 · Episode 2 — {@code RelGridGestures}: what a click, a press-drag
 * and a key MEAN, and the selection they route (map 5). The layout reports raw
 * facts by position; here they become intents on the cursor and the selection,
 * inert while the grid is locked. Imports nothing: the verbs a key stands for
 * are the facade's, handed in.
 */
public record RelGridGesturesModule() implements DomModule<RelGridGesturesModule> {

    public record RelGridGestures() implements Exportable._Constant<RelGridGesturesModule> {}

    public static final RelGridGesturesModule INSTANCE = new RelGridGesturesModule();

    @Override
    public ImportsFor<RelGridGesturesModule> imports() {
        return ImportsFor.<RelGridGesturesModule>builder()

                .build();
    }

    @Override public ExportsOf<RelGridGesturesModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new RelGridGestures()));
    }
}
