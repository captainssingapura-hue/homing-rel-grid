package hue.captains.singapura.js.homing.relgrid.workbench;

import hue.captains.singapura.js.homing.core.EsModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.relgrid.protocol.RelGridProtocolModule;

import java.util.List;

/**
 * What a selection of dishes is WORTH on a clipboard, composed: PURE LOGIC,
 * strings in and strings out, no DOM anywhere. {@code dishClipboardContent
 * (relation, blocks, format)} reads the relation's own cells (law 48) and
 * composes TSV, CSV or HTML — a rating a number in text, the stars a person
 * saw in HTML; {@code dishStarsHtml(value, stars)} is that rating's markup. A
 * clipboard leaves the page, so its HTML carries its own looks inline, literal
 * colours and all; that is data here, not a view, which is why it is a
 * pure-logic module and not part of one that draws.
 *
 * <p>Imports the protocol's content record and nothing else.</p>
 */
public record DishClipboardFormatsModule() implements EsModule<DishClipboardFormatsModule> {

    public record DishClipboardFormats() implements Exportable._Class<DishClipboardFormatsModule> {}

    public static final DishClipboardFormatsModule INSTANCE = new DishClipboardFormatsModule();

    @Override
    public ImportsFor<DishClipboardFormatsModule> imports() {
        return ImportsFor.<DishClipboardFormatsModule>builder()
                .add(new ModuleImports<>(List.of(new RelGridProtocolModule.RelGridClipboardContent()), RelGridProtocolModule.INSTANCE))
                .build();
    }

    @Override public ExportsOf<DishClipboardFormatsModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new DishClipboardFormats()));
    }
}
