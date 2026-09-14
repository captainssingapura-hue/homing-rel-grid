package hue.captains.singapura.js.homing.relgrid.protocol;

import java.util.Set;

/**
 * A <b>place</b>: a node of a tree and where it stands — RFC 0050 · Episode
 * 3-ext1, the tree structure as the binding contract.
 *
 * <p>A pre-ordered list of places is an outline, and an outline is a tree:
 * a parent is the nearest preceding place one level shallower, a first child
 * the next place one level deeper, so nothing about the structure has to be
 * asked. {@code depth} is how far in, the roots at 0. {@code fold} says what
 * is under the node: {@code leaf} — nothing; {@code closed} — children the
 * tree does not present; {@code open} — children that follow it in the list.
 * Three words rather than a nullable, because the codec has no absence and
 * a tree should not have to guess.</p>
 *
 * <p>The key is the domain's identity for the node — the tree's
 * {@code cellFor(key)} is asked with exactly this — and nothing about its
 * text, icon or meaning crosses with it.</p>
 */
public record RelTreePlace(Object key, int depth, String fold) {

    public static final String LEAF = "leaf", CLOSED = "closed", OPEN = "open";
    private static final Set<String> FOLDS = Set.of(LEAF, CLOSED, OPEN);

    public RelTreePlace {
        if (key == null) throw new IllegalArgumentException("RelTreePlace names its node");
        if (depth < 0) throw new IllegalArgumentException("RelTreePlace.depth is 0 at the root and never negative: " + depth);
        if (fold == null || !FOLDS.contains(fold))
            throw new IllegalArgumentException("RelTreePlace.fold is leaf, closed or open: " + fold);
    }
}
