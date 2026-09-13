package hue.captains.singapura.js.homing.relgrid;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.relgrid.protocol.RelGridProtocolModule;

import java.util.List;

/**
 * RFC 0050 · Episode 2 — the stock CLIPBOARD WRITER the grid writes a copy's
 * answer with when the host names none: the async Clipboard API, and the copy
 * command where a page is denied it. Its one element, a scratch textarea, is
 * minted on a sub-branch of the grid's own for the write and dissolved after.
 */
public record RelGridClipboardModule() implements DomModule<RelGridClipboardModule> {

    public record createRelGridClipboard() implements Exportable._Constant<RelGridClipboardModule> {}

    public static final RelGridClipboardModule INSTANCE = new RelGridClipboardModule();

    @Override
    public ImportsFor<RelGridClipboardModule> imports() {
        return ImportsFor.<RelGridClipboardModule>builder()
                .add(new ModuleImports<>(List.of(new RelGridStyles.hrg_scratch()), RelGridStyles.INSTANCE))
                .build();
    }

    @Override public ExportsOf<RelGridClipboardModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new createRelGridClipboard()));
    }
}
