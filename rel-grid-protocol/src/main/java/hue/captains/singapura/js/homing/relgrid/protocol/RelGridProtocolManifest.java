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
            ObjectDefinition.of(RelGridClipboardContent.class),
            // The second question the grid waits for: the rows' arrangement,
            // handed to the domain. It carries nothing; no dependencies.
            ObjectDefinition.of(RelGridViewHandover.class),
            // Its answer: a View — the root's pks, in order. No dependencies.
            ObjectDefinition.of(RelGridView.class),
            // The group's first kind, and the first that travels the other way:
            // the domain telling the group to fold a member. No dependencies.
            ObjectDefinition.of(RelGridGroupFold.class),
            // The first told to a GRID: its View changed underneath it — ask again.
            // Carries nothing, on purpose. No dependencies.
            ObjectDefinition.of(RelGridViewChanged.class),
            // THE TREE'S (Episode 3-ext1) — the protocol crate is the family's. A
            // place: a node and where it stands; no dependencies. A tree's View
            // carries places, so it comes after them.
            ObjectDefinition.of(RelTreePlace.class),
            ObjectDefinition.of(RelTreeView.class),
            // The two questions a tree waits for, both answered with a View or nothing.
            ObjectDefinition.of(RelTreeUnfold.class),
            ObjectDefinition.of(RelTreeFold.class),
            // The tree's two notifications, and what is told to it. No dependencies.
            ObjectDefinition.of(RelTreeCursorChanged.class),
            ObjectDefinition.of(RelTreeActivated.class),
            ObjectDefinition.of(RelTreeViewChanged.class));
}
