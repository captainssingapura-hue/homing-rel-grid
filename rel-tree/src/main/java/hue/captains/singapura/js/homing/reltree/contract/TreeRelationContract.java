package hue.captains.singapura.js.homing.reltree.contract;

import java.util.List;

/**
 * RFC 0050 · Episode 3-ext1 — the tree relation: what a domain hands a tree,
 * and the <b>whole</b> of what the tree may ask of it. The root of ext8 read
 * for a tree: a View answered on request and a cell manager, nothing that
 * enumerates, nothing that carries a value.
 *
 * <h2>The tree structure is the binding</h2>
 *
 * <p>{@code view()} answers the <b>places</b> to present now, in order — a
 * pre-ordered outline, {@code { key, depth, fold }} each, a bare key standing
 * for a leaf at depth 0. That is the tree structure, not a flattening of it: a
 * parent is the nearest preceding place one level shallower and a first child
 * the next place one level deeper, so there is no {@code parent(key)} and no
 * {@code children(key)}. Lazy by default: the roots, closed, and the rest by
 * asking — {@code RelTreeUnfold} on the channel, answered with the whole View
 * to present next, or nothing. The tree checks well-formedness at the door and
 * refuses a malformed answer whole.</p>
 *
 * <h2>Membership is the relation's</h2>
 *
 * <p>{@code cellFor(key)} refuses a stranger by throwing; the tree asks for
 * every presented key before a row moves, so a refusal refuses the View whole.
 * A cell is asked for once per presentation and forgotten when its key leaves
 * the View — the domain's {@code cellFor} is the keeper (ext8, laws 244–245).
 * The tree never disposes what it did not create.</p>
 *
 * <p>Nothing about a node's text, icon or meaning is on this contract, on
 * purpose: a tree that could read a node's text would sort by it, search by
 * it and type-ahead to it, and the node is opaque to the tree.</p>
 */
public interface TreeRelationContract {
    List<Object> view();                       // the places to present now, in order: { key, depth, fold }, or bare keys
    Object       cellFor(Object key);          // the cell manager: the domain's cell for one node; throws for a stranger

    /** The method names the JS relation object must expose. Nothing about values. */
    String[] METHOD_NAMES = { "view", "cellFor" };
}
