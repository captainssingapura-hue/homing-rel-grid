package hue.captains.singapura.js.homing.relgrid.protocol;

import hue.captains.singapura.js.homing.codec.ObjectDefinition;

import java.util.List;

/**
 * Every type the ask protocol carries, in the order the generated module
 * declares them.
 *
 * <p><b>Order is dependency order</b>, because the emitted module reads top to
 * bottom and a forward reference would throw at evaluation. Nothing sorts it;
 * the test that evaluates the generated module is what makes a mistake here
 * immediate.</p>
 *
 * <p>This list and {@link RelGridProtocolModule}'s exports are one list, bound
 * by a conformance test — ext5's law 210.</p>
 */
public final class RelGridProtocolManifest {

    private RelGridProtocolManifest() {}

    public static final List<ObjectDefinition<?>> ENTRIES = List.of(
            // A rectangle of positions: no dependencies of its own.
            ObjectDefinition.of(RelGridRange.class),
            // The first kind, and a notification: it carries ranges, so it
            // comes after them.
            ObjectDefinition.of(RelGridSelectionChanged.class),
            // A range resolved to identities: depends on nothing.
            ObjectDefinition.of(RelGridBlock.class),
            // The first question the grid waits for — it carries blocks.
            ObjectDefinition.of(RelGridCopyRequested.class),
            // Its answer: finished content, and no dependencies.
            ObjectDefinition.of(RelGridClipboardContent.class));
}
