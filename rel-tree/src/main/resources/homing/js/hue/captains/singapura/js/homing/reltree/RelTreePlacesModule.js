// =============================================================================
// RelTreePlacesModule — RFC 0050 · Episode 3-ext1's PLACES: the tree structure
// as the tree holds it, which is exactly what the relation answered and
// nothing more. A place is a node and where it stands — { key, depth, fold }
// — and a pre-ordered list of them is an outline, and an outline is a tree:
// a parent is the nearest preceding place one level shallower, a first child
// the next place one level deeper. So nothing about the structure is asked;
// it is read off the answer. PURE LOGIC: no DOM, no relation, no channel.
//
// COERCION, once, at the door: a bare key is a place at depth 0, a leaf; a
// place-like object — the protocol's RelTreePlace, or anything with key,
// depth and fold — is taken as it is, its fold defaulting to leaf.
//
// WELL-FORMEDNESS, checked as membership is, before anything moves: the
// first place at depth 0; depth rising by at most one from the place before;
// a closed node and a leaf followed by nothing deeper; every key once. A
// malformed answer throws here, and the caller refuses the View whole. An
// open node followed by nothing deeper is an open, empty node — allowed.
//
//   RelTreePlaces.coerce(list)      places from keys and place-likes; throws for not-a-list
//   RelTreePlaces.check(places)     throws for a malformed outline
//   RelTreePlaces.outline(roots, childrenOf, isOpen)   a domain's helper: the places of a nested tree —
//                                   pre-order, the open nodes' children presented — from three functions
//                                   of the domain's own; childrenOf answers a list (empty for a leaf)
//   new RelTreePlaces(places)       the presented places; every read below is over them
//   swap(places)                    the next presented places; answers the previous, for an undo
//   rows() / at(i) / keyAt(i) / depthAt(i) / foldAt(i) / indexOf(key) / keys()
//   parentOf(i)                     the parent's index, or -1 at the root level
//   firstChildOf(i)                 the first presented child's index, or -1
// =============================================================================

var _HRT_FOLDS = { leaf: true, closed: true, open: true };

class RelTreePlaces {

    /** A bare key is a place at depth 0, a leaf; a place-like is taken as it is. */
    static coerce(list) {
        if (!Array.isArray(list)) throw new Error("[RelTree] a View is a list of places or keys; got: " + list);
        var out = [];
        for (var k = 0; k < list.length; k++) {
            var p = list[k];
            if (p !== null && typeof p === "object") {
                if (p.key === undefined || p.key === null) throw new Error("[RelTree] a place names its node: " + JSON.stringify(p));
                var depth = (p.depth === undefined) ? 0 : p.depth;
                var fold = (p.fold === undefined || p.fold === null) ? "leaf" : p.fold;
                out.push({ key: p.key, depth: depth, fold: fold });
            } else {
                out.push({ key: p, depth: 0, fold: "leaf" });
            }
        }
        return out;
    }

    /** The outline must hold together; the first offence is the message. */
    static check(places) {
        var seen = new Set(), prev = null;
        for (var i = 0; i < places.length; i++) {
            var p = places[i];
            if (typeof p.depth !== "number" || p.depth < 0 || p.depth !== Math.floor(p.depth))
                throw new Error("[RelTree] place " + i + " (" + p.key + "): depth must be a whole number, not " + p.depth);
            if (!_HRT_FOLDS[p.fold])
                throw new Error("[RelTree] place " + i + " (" + p.key + "): fold is leaf, closed or open, not " + p.fold);
            if (seen.has(p.key)) throw new Error("[RelTree] place " + i + ": key " + p.key + " is presented twice");
            seen.add(p.key);
            if (!prev) {
                if (p.depth !== 0) throw new Error("[RelTree] the first place is at depth 0, not " + p.depth);
            } else if (p.depth > prev.depth + 1) {
                throw new Error("[RelTree] place " + i + " (" + p.key + "): depth " + p.depth + " after depth " + prev.depth + " — a level was skipped");
            } else if (p.depth > prev.depth && prev.fold !== "open") {
                throw new Error("[RelTree] place " + i + " (" + p.key + "): deeper than " + prev.key + ", which is " + prev.fold + " and has nothing under it");
            }
            prev = p;
        }
        return places;
    }

    /**
     * The outline of a nested tree, for a domain that holds one: pre-order over
     * the roots, a node's children after it when isOpen(key) says so. A pure
     * function of the domain's three answers; the tree never calls it.
     */
    static outline(roots, childrenOf, isOpen) {
        var out = [];
        function walk(keys, depth) {
            for (var k = 0; k < keys.length; k++) {
                var key = keys[k], children = childrenOf(key) || [];
                if (children.length === 0) { out.push({ key: key, depth: depth, fold: "leaf" }); continue; }
                var open = !!isOpen(key);
                out.push({ key: key, depth: depth, fold: open ? "open" : "closed" });
                if (open) walk(children, depth + 1);
            }
        }
        walk(roots || [], 0);
        return out;
    }

    constructor(places) {
        this._places = [];
        this._index = new Map();
        this.swap(places || []);
    }

    /** The next presented places, checked; the previous are answered so a refusal can put them back. */
    swap(places) {
        RelTreePlaces.check(places);
        var previous = this._places;
        this._places = places.slice();
        this._index = new Map();
        for (var i = 0; i < places.length; i++) this._index.set(places[i].key, i);
        return previous;
    }

    rows()      { return this._places.length; }
    at(i)       { return this._places[i] || null; }
    keyAt(i)    { var p = this._places[i]; return p ? p.key : null; }
    depthAt(i)  { var p = this._places[i]; return p ? p.depth : -1; }
    foldAt(i)   { var p = this._places[i]; return p ? p.fold : null; }
    indexOf(key) { var i = this._index.get(key); return (i === undefined) ? -1 : i; }
    keys()      { var out = []; for (var i = 0; i < this._places.length; i++) out.push(this._places[i].key); return out; }

    /** The nearest preceding place one level shallower; -1 at the root level, or off the list. */
    parentOf(i) {
        var p = this._places[i];
        if (!p || p.depth === 0) return -1;
        for (var k = i - 1; k >= 0; k--) if (this._places[k].depth === p.depth - 1) return k;
        return -1;                                   // a window that began mid-subtree: the parent is not presented
    }

    /** The next place, when it is one level deeper; -1 otherwise. */
    firstChildOf(i) {
        var p = this._places[i], n = this._places[i + 1];
        return (p && n && n.depth === p.depth + 1) ? i + 1 : -1;
    }
}
