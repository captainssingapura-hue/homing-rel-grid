package hue.captains.singapura.js.homing.relgrid.group.contract;

/**
 * RFC 0050 · Episode 2 — a FENCE CELL: what the domain puts in the slot the
 * group mints between two members, or around the ends.
 *
 * <p>The group hands the slot over exactly as a table hands a slot to a cell:
 * {@link #render(Object)} once, into an element the group minted, and what
 * goes in it is the domain's — a title, a published total, an illustration, a
 * control. The group never reads what was drawn, never asks the fence
 * anything, and never calls {@link #dispose()}: disposal is the owner's,
 * as it is for a cell.</p>
 */
public interface RelGridFenceContract {
    void render(Object host);   // mount once into the element the group minted
    void dispose();             // the owner's, never the group's

    String[] METHOD_NAMES = { "render", "dispose" };
}
