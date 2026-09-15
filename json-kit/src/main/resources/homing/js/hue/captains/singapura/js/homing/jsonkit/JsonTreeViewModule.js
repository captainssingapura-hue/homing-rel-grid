// =============================================================================
// JsonTreeViewModule — the JSON viewer, whole: a RelTree over a JsonDocument,
// composed here so a host that wants a JSON value on screen writes one line
// and never learns what a place, a channel or a pointer is. The kit's
// out-of-the-box offering; the document and the cell beneath it are exported
// too, for a host that composes its own tree.
//
// The view is the tree's HOST, and keeps the host's discipline: it is handed
// a branch of its own and makes exactly two beneath it — 'tree', handed
// whole and unactivated to the RelTree, and 'document', the document's, for
// its cells — and neither side ever sees the other's. The view mints nothing
// of its own. destroy() takes the tree and the document down — the document
// dissolves its own branch, the view the tree's; the branch it was handed is
// the host's to dissolve.
//
//   new JsonTreeView({
//       container,        // where the tree mounts — the host's scrolling element; the tree draws no frame
//       branch,           // the view's OWN branch, handed unactivated
//       value?,           // the JSON value, already parsed; undefined for nothing yet
//       title?,           // the root row's name — "$" by default
//       openDepth?,       // containers open at first, by depth — 1 (the root) by default
//       maxString?,       // a string printed up to this many characters — 80 by default
//       label?,           // aria-label on the tree
//       onActivated?,     // (pointer) — Enter or a double-click reached the node
//       onCursorMoved?    // (pointer)
//   });
//   set(value)               a new value: the folds kept where a container still stands, the cursor by pointer
//   value() / valueAt(pointer)
//   open(pointer) / close(pointer) / openAll(depth?) / closeAll()
//   selectPointer(pointer)   the ancestors opened and the cursor put on the node, revealed; false for no such node
//   cursor()                 the pointer under the cursor, or null
//   focus() / el() / destroy()
// =============================================================================

class JsonTreeView {

    constructor(opts) {
        opts = opts || {};
        if (!opts.container) throw new Error("[JsonTreeView] opts.container is required");
        if (!opts.branch) throw new Error("[JsonTreeView] opts.branch is required: the view's own");
        var self = this;
        this._branch = opts.branch;
        this._branch.activate(this);
        this._treeBranch = this._branch.createBranch("tree");           // the tree's: handed whole, it activates
        this._documentBranch = this._branch.createBranch("document");   // the document's: it activates, its cells beneath
        this._document = createJsonDocument(opts.value, {
            branch: this._documentBranch, title: opts.title, openDepth: opts.openDepth, maxString: opts.maxString
        });
        this._tree = new RelTree({
            container: opts.container,
            branch: this._treeBranch,
            relation: this._document,                                    // the tree's word for what answers its two questions
            label: opts.label,
            ask: function (question, mask) { return self._document.answer(question, mask); },
            onActivated: opts.onActivated || undefined,
            onCursorMoved: opts.onCursorMoved || undefined
        });
    }

    /** The document told, unasked: the tree asks view() again — the cursor kept by pointer, the folds by the document. */
    _tell() { return this._tree.tell(new RelTreeViewChanged()); }

    set(value) { this._document.set(value); this._tell(); return this; }
    value() { return this._document.value(); }
    valueAt(pointer) { return this._document.valueAt(pointer); }

    open(pointer) { this._document.open(pointer); this._tell(); return this; }
    close(pointer) { this._document.close(pointer); this._tell(); return this; }
    openAll(depth) { this._document.openAll(depth); this._tell(); return this; }
    closeAll() { this._document.closeAll(); this._tell(); return this; }

    /** The road a navigator takes: the document opens the path, the tree is told, then asked for the cursor. */
    selectPointer(pointer) {
        if (!this._document.openTo(pointer)) return false;
        this._tell();
        return this._tree.selectNode(pointer);
    }

    cursor() { return this._tree.cursor(); }
    focus() { this._tree.focus(); }
    el() { return this._tree.el(); }

    /** The tree down (it disposes nothing, and its branch is this host's to dissolve); the document disposes its cells and dissolves its own. */
    destroy() {
        this._tree.destroy();
        this._document.dispose();
        this._branch.dissolveBranch("tree");
    }
}
