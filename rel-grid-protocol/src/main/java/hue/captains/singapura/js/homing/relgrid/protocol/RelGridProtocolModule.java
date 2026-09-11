package hue.captains.singapura.js.homing.relgrid.protocol;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;

import java.util.List;

/**
 * The ask protocol as a served JS module — RFC 0050 · Episode 2, ext4 law 202.
 *
 * <p>Its body is not in {@code src/main/resources}; it is produced at build
 * time by {@link RelGridProtocolGen} from the Java records named in
 * {@link RelGridProtocolManifest}, and read from {@code target/classes} as an
 * ordinary classpath resource.</p>
 *
 * <p>{@link ImportsFor#noImports()} on purpose: the protocol is data, so it
 * depends on nothing and both sides of the seam may depend on it. The exports
 * below are the manifest restated for the module system, and a conformance
 * test binds the two so they cannot drift.</p>
 */
public record RelGridProtocolModule() implements DomModule<RelGridProtocolModule> {

    public record RelGridRange()            implements Exportable._Class<RelGridProtocolModule> {}
    public record RelGridSelectionChanged() implements Exportable._Class<RelGridProtocolModule> {}
    public record RelGridBlock()            implements Exportable._Class<RelGridProtocolModule> {}
    public record RelGridCopyRequested()    implements Exportable._Class<RelGridProtocolModule> {}
    public record RelGridClipboardContent() implements Exportable._Class<RelGridProtocolModule> {}

    public static final RelGridProtocolModule INSTANCE = new RelGridProtocolModule();

    @Override public ImportsFor<RelGridProtocolModule> imports() { return ImportsFor.noImports(); }

    @Override public ExportsOf<RelGridProtocolModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(
                new RelGridRange(), new RelGridSelectionChanged(),
                new RelGridBlock(), new RelGridCopyRequested(), new RelGridClipboardContent()));
    }
}
