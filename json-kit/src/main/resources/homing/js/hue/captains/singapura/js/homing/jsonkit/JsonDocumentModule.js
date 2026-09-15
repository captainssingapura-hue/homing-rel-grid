// =============================================================================
// JsonDocumentModule — a JSON value as what a tree view asks its domain for:
// the PLACES to present now, and a CELL per node. DOMAIN-SIDE code that ships
// with the kit, holding a value as any domain does; the tree never sees it.
//
// THE MAPPING IS ONE TO ONE. A JSON value already is an outline: an object's
// members and an array's elements are a node's children, in the order the
// value holds them. So nothing structural is built here — no node objects, no
// parent links. The tree's own pure helper, RelTreePlaces.outline, turns the
// value into places from two functions of the document's: childrenOf(pointer)
// and isOpen(pointer). The KEY of a node is its JSON POINTER (RFC 6901): ""
// for the root, "/a/0/b~1c" for a member — "~" escaped as "~0", "/" as "~1".
//
// LAZY under the fold: nothing beneath a closed container is placed or
// asked for; an unfold or a fold is answered at once, from the value in hand.
// A container with nothing in it is a LEAF — there is nothing to unfold.
//
//   createJsonDocument(value, { branch, title?, openDepth?, maxString? })
//       branch     the document's OWN, unactivated when handed; it activates, and every cell
//                  is given a sub-branch of it
//       title      what the root row is called — "$" by default
//       openDepth  containers open at first, by depth: 1 (default) opens the root only; 0 none
//       maxString  a string is printed up to this many characters — 80 by default
//   view() / cellFor(pointer)              the tree's two questions (TreeRelationContract)
//   answer(question, mask) → thenable      the channel, as the host wires it: unfold and fold answered
//                                          at once with the whole View; a notification heard
//   set(value)                             a new value: the fold set kept where a container still stands (the root
//                                          reopened if it was meant open); a cell whose pointer still resolves is set
//                                          to the new value, one whose pointer is gone disposed — the host TELLS afterwards
//   value() / valueAt(pointer)             the value, whole or at a pointer (undefined for none)
//   open(pointer) / close(pointer) / openTo(pointer) / openAll(depth?) / closeAll()
//                                          the document's own fold state, for a host's controls
//   isOpen(pointer) / cell(pointer) / cellCount() / dispose()
//
// A JS object's key order is what JSON.parse gave it — integer-like keys
// first, ascending, then the rest in document order. That is the platform's
// rule, not the document's, and a member order a host must keep is a host's
// to serialise around.
// =============================================================================

var _JK_MISSING = {};                                             // the sentinel for a pointer that resolves to nothing

function _jkEscape(token) { return String(token).replace(/~/g, "~0").replace(/\//g, "~1"); }
function _jkUnescape(token) { return token.replace(/~1/g, "/").replace(/~0/g, "~"); }
function _jkIsContainer(v) { return v !== null && typeof v === "object"; }

/** The value at a pointer, or the sentinel. */
function _jkAt(root, pointer) {
    if (pointer === "") return root;
    if (typeof pointer !== "string" || pointer.charAt(0) !== "/") return _JK_MISSING;
    var v = root, tokens = pointer.split("/");
    for (var i = 1; i < tokens.length; i++) {
        var t = _jkUnescape(tokens[i]);
        if (Array.isArray(v)) {
            if (!/^(0|[1-9][0-9]*)$/.test(t)) return _JK_MISSING;
            var n = Number(t);
            if (n >= v.length) return _JK_MISSING;
            v = v[n];
        } else if (_jkIsContainer(v) && Object.prototype.hasOwnProperty.call(v, t)) {
            v = v[t];
        } else return _JK_MISSING;
    }
    return v;
}

/** The last token of a pointer, unescaped — a member's name or an element's index. */
function _jkNameOf(pointer) {
    var i = pointer.lastIndexOf("/");
    return i < 0 ? "" : _jkUnescape(pointer.slice(i + 1));
}

function createJsonDocument(value, opts) {
    opts = opts || {};
    var branch = opts.branch;
    if (!branch) throw new Error("[JsonDocument] opts.branch is required: the cells mint on the document's branch");
    var title = (opts.title === undefined || opts.title === null) ? "$" : String(opts.title);
    var openDepth = (typeof opts.openDepth === "number") ? opts.openDepth : 1;
    var maxString = (typeof opts.maxString === "number") ? opts.maxString : 80;
    var owner = Object.freeze({ toString: function () { return "json document"; } });
    branch.activate(owner);                                       // its own: unactivated when handed

    var root = value, open = new Set(), cells = new Map(), seq = 0;
    var rootMeant = false;                                        // the root is MEANT open: reopened when a container stands there again

    function at(pointer) { return _jkAt(root, pointer); }
    function childrenOf(pointer) {
        var v = at(pointer), out = [];
        if (Array.isArray(v)) { for (var i = 0; i < v.length; i++) out.push(pointer + "/" + i); }
        else if (_jkIsContainer(v)) { var ks = Object.keys(v); for (var k = 0; k < ks.length; k++) out.push(pointer + "/" + _jkEscape(ks[k])); }
        return out;
    }
    function isOpen(pointer) { return open.has(pointer); }
    function places() { return RelTreePlaces.outline([""], childrenOf, isOpen); }

    /** Every container down to a depth (the root is depth 0), opened; no depth means all of them. */
    function openAll(depth) {
        var limit = (typeof depth === "number") ? depth : Infinity;
        rootMeant = limit >= 0;
        function walk(pointer, d) {
            if (d > limit) return;
            var kids = childrenOf(pointer);
            if (kids.length === 0) return;
            open.add(pointer);
            for (var i = 0; i < kids.length; i++) walk(kids[i], d + 1);
        }
        walk("", 0);
    }
    /** The fold set after a new value: a pointer stays only while a container still stands at it; the root as it is meant. */
    function prune() {
        open.forEach(function (pointer) { if (childrenOf(pointer).length === 0) open.delete(pointer); });
        if (rootMeant && childrenOf("").length > 0) open.add("");
    }
    function letGo() {
        cells.forEach(function (c) { c.dispose(); });
        cells.clear();
    }
    function indexed(pointer) { return pointer !== "" && Array.isArray(at(pointer.slice(0, pointer.lastIndexOf("/")))); }
    /**
     * The cells after a new value. A cell is asked for ONCE per presentation and kept by the tree
     * while its pointer stays presented — so a cell whose pointer still resolves is SET to the new
     * value, in place, and only one whose pointer is gone (or whose name changed from a key to an
     * index) is disposed and forgotten; the tree asks for it afresh if the pointer returns.
     */
    function rekey() {
        cells.forEach(function (c, pointer) {
            var v = at(pointer);
            if (v === _JK_MISSING || indexed(pointer) !== c.isIndex()) { c.dispose(); cells.delete(pointer); }
            else c.set({ value: v });
        });
    }

    if (openDepth > 0) openAll(openDepth - 1);

    return {
        view: function () { return places(); },
        cellFor: function (pointer) {
            var v = at(pointer);
            if (v === _JK_MISSING) throw new Error("[JsonDocument] no such node: " + JSON.stringify(pointer));   // a stranger is refused here
            var c = cells.get(pointer);
            if (!c) {
                c = new JsonNodeCell({ branch: branch.createBranch("n" + (++seq)),
                                       name: pointer === "" ? title : _jkNameOf(pointer),
                                       index: indexed(pointer),
                                       value: v, maxString: maxString });
                cells.set(pointer, c);
            }
            return c;
        },
        // THE CHANNEL, as the host wires it: a question in, a thenable out — at once, from the value in hand.
        answer: function (question, mask) {
            if (question instanceof RelTreeUnfold) { open.add(question.key); if (question.key === "") rootMeant = true; return Promise.resolve(new RelTreeView(places())); }
            if (question instanceof RelTreeFold)   { open.delete(question.key); if (question.key === "") rootMeant = false; return Promise.resolve(new RelTreeView(places())); }
            return Promise.resolve();                             // a notification: heard, not answered
        },
        set: function (v) { root = v; prune(); rekey(); },
        value: function () { return root; },
        valueAt: function (pointer) { var v = at(pointer); return v === _JK_MISSING ? undefined : v; },
        open: function (pointer) { if (pointer === "") rootMeant = true; if (childrenOf(pointer).length > 0) open.add(pointer); },
        close: function (pointer) { if (pointer === "") rootMeant = false; open.delete(pointer); },
        /** Every container above a pointer opened, so the node can be presented; the pointer itself is left as it is. */
        openTo: function (pointer) {
            if (at(pointer) === _JK_MISSING) return false;
            var i = 0;
            rootMeant = true;
            open.add("");
            while ((i = pointer.indexOf("/", i + 1)) > 0) open.add(pointer.slice(0, i));
            return true;
        },
        openAll: openAll,
        closeAll: function () { rootMeant = false; open.clear(); },
        isOpen: isOpen,
        cell: function (pointer) { return cells.get(pointer) || null; },
        cellCount: function () { return cells.size; },
        dispose: function () { letGo(); branch.dissolve(); }
    };
}
