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
//   new JsonDocument(value, { branch, title?, openDepth?, maxString? })
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


class JsonDocument {
    constructor(value, opts) {
        var o = opts || {};
        if (!o.branch) throw new Error("[JsonDocument] opts.branch is required: the cells mint on the document's branch");
        this._branch = o.branch;
        this._title = (o.title === undefined || o.title === null) ? "$" : String(o.title);
        var openDepth = (typeof o.openDepth === "number") ? o.openDepth : 1;
        this._maxString = (typeof o.maxString === "number") ? o.maxString : 80;
        this._branch.activate(Object.freeze({ toString: function () { return "json document"; } }));   // its own: unactivated when handed
        this._root = value;
        this._open = new Set();
        this._cells = new Map();
        this._seq = 0;
        this._rootMeant = false;                                  // the root is MEANT open: reopened when a container stands there again
        if (openDepth > 0) this.openAll(openDepth - 1);
    }

    view() { return this._places(); }

    cellFor(pointer) {
        var v = this._at(pointer);
        if (v === _JK_MISSING) throw new Error("[JsonDocument] no such node: " + JSON.stringify(pointer));   // a stranger is refused here
        var c = this._cells.get(pointer);
        if (!c) {
            c = new JsonNodeCell({ branch: this._branch.createBranch("n" + (++this._seq)),
                                   name: pointer === "" ? this._title : _jkNameOf(pointer),
                                   index: this._indexed(pointer),
                                   value: v, maxString: this._maxString });
            this._cells.set(pointer, c);
        }
        return c;
    }

    // THE CHANNEL, as the host wires it: a question in, a thenable out — at once, from the value in hand.
    answer(question, mask) {
        if (question instanceof RelTreeUnfold) { this._open.add(question.key); if (question.key === "") this._rootMeant = true; return Promise.resolve(new RelTreeView(this._places())); }
        if (question instanceof RelTreeFold)   { this._open.delete(question.key); if (question.key === "") this._rootMeant = false; return Promise.resolve(new RelTreeView(this._places())); }
        return Promise.resolve();                                 // a notification: heard, not answered
    }

    set(v) { this._root = v; this._prune(); this._rekey(); }
    value() { return this._root; }
    valueAt(pointer) { var v = this._at(pointer); return v === _JK_MISSING ? undefined : v; }

    open(pointer) { if (pointer === "") this._rootMeant = true; if (this._childrenOf(pointer).length > 0) this._open.add(pointer); }
    close(pointer) { if (pointer === "") this._rootMeant = false; this._open.delete(pointer); }

    /** Every container above a pointer opened, so the node can be presented; the pointer itself is left as it is. */
    openTo(pointer) {
        if (this._at(pointer) === _JK_MISSING) return false;
        var i = 0;
        this._rootMeant = true;
        this._open.add("");
        while ((i = pointer.indexOf("/", i + 1)) > 0) this._open.add(pointer.slice(0, i));
        return true;
    }

    /** Every container down to a depth (the root is depth 0), opened; no depth means all of them. */
    openAll(depth) {
        var limit = (typeof depth === "number") ? depth : Infinity, self = this;
        this._rootMeant = limit >= 0;
        var walk = function (pointer, d) {
            if (d > limit) return;
            var kids = self._childrenOf(pointer);
            if (kids.length === 0) return;
            self._open.add(pointer);
            for (var i = 0; i < kids.length; i++) walk(kids[i], d + 1);
        };
        walk("", 0);
    }

    closeAll() { this._rootMeant = false; this._open.clear(); }
    isOpen(pointer) { return this._open.has(pointer); }
    cell(pointer) { return this._cells.get(pointer) || null; }
    cellCount() { return this._cells.size; }
    dispose() { this._letGo(); this._branch.dissolve(); }

    _at(pointer) { return _jkAt(this._root, pointer); }

    _childrenOf(pointer) {
        var v = this._at(pointer), out = [];
        if (Array.isArray(v)) { for (var i = 0; i < v.length; i++) out.push(pointer + "/" + i); }
        else if (_jkIsContainer(v)) { var ks = Object.keys(v); for (var k = 0; k < ks.length; k++) out.push(pointer + "/" + _jkEscape(ks[k])); }
        return out;
    }

    _places() {
        var self = this;
        return RelTreePlaces.outline([""], function (p) { return self._childrenOf(p); }, function (p) { return self._open.has(p); });
    }

    /** The fold set after a new value: a pointer stays only while a container still stands at it; the root as it is meant. */
    _prune() {
        var self = this;
        this._open.forEach(function (pointer) { if (self._childrenOf(pointer).length === 0) self._open.delete(pointer); });
        if (this._rootMeant && this._childrenOf("").length > 0) this._open.add("");
    }

    _letGo() {
        this._cells.forEach(function (c) { c.dispose(); });
        this._cells.clear();
    }

    _indexed(pointer) { return pointer !== "" && Array.isArray(this._at(pointer.slice(0, pointer.lastIndexOf("/")))); }

    /**
     * The cells after a new value. A cell is asked for ONCE per presentation and kept by the tree
     * while its pointer stays presented — so a cell whose pointer still resolves is SET to the new
     * value, in place, and only one whose pointer is gone (or whose name changed from a key to an
     * index) is disposed and forgotten; the tree asks for it afresh if the pointer returns.
     */
    _rekey() {
        var self = this;
        this._cells.forEach(function (c, pointer) {
            var v = self._at(pointer);
            if (v === _JK_MISSING || self._indexed(pointer) !== c.isIndex()) { c.dispose(); self._cells.delete(pointer); }
            else c.set({ value: v });
        });
    }
}
