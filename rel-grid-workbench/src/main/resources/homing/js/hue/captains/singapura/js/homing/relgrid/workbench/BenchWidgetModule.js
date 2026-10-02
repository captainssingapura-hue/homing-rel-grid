// =============================================================================
// BenchWidget — what every bench widget is to its host, said once; a bench
// widget extends it and builds what it shows into `body`.
//
// A self-contained widget (the Workspace & Widgets doctrines): made with the
// container its host lends it and its params, and nothing else; its DomOps and
// focus parties its own, offered as roots for its host to graft; the parties
// it shares with the other widgets of its bench declared by type in Java, and
// joined after it is made. Not joined, it works alone, on what it has.
//
// THE KEYS. A bench widget is a logical member of the keyboard's party; what
// takes keys natively inside it - a grid's cells, a text box, a button - is the
// native world's. A press anywhere in it claims the keys for it. Given the keys
// by a call, they go on to `keysTo`, when it names something that takes them
// (a grid); Escape that nothing native took gives them back.
//
//   class EndlessTable extends BenchWidget {
//       constructor(container, params) { super(container, "endlessTable", "Endless table"); ... }
//       joinParties(given) { return [ given[X.name].join("me", { Kind: m => ... }) ]; }   memberships
//       disposed() { ...what it holds beside its branch... }
//   }
//   widget.root  widget.roots { dom, focus }  widget.focus
//   widget.branch       its own DomOps party: what it mints on
//   widget.body         where it builds: the root's scroller, a column of what it shows
//   widget.keysTo       what the keys go on to when given by a call, or null
//   widget.join(given)  widget.leave()   given: { [type name]: party }; a second join without a leave is refused
//   widget.activate()   widget.granted(by)   widget.keyDown(ev)   widget.dispose()
//   widget.el(name, tag, cls, into?, text?)   an element of its own, its class worn, appended
//   widget.button(name, label, onClick, into)  a native button of its own
// =============================================================================

var _benchWidgets = 0;

class BenchWidget {
    constructor(container, name, label) {
        if (!container || typeof container.appendChild !== "function") throw new Error("[" + name + "] a container is required: the one its host lends it");
        var n = name + "-" + (++_benchWidgets);
        this._name = name;
        this._dom = domOpsParties.mobile(n);
        this._dom.activate(Object.freeze({ toString: function () { return name; } }));
        var root = this._dom.createElement("root", "div");
        css.addClass(root, wg_fill);
        root.setAttribute("role", "region");
        root.setAttribute("aria-label", label);
        var body = this._dom.createElement("body", "div");
        css.addClass(body, wg_scroll, wb_root);
        root.appendChild(body);
        this.root = root;
        this.body = body;
        this.keysTo = null;
        this._focusParty = focusParties.mobile(n);
        this.focus = this._focusParty.root.join(name, this);
        this._off = Keys.claimOn(root, this.focus);
        this.roots = Object.freeze({ dom: this._dom, focus: this._focusParty });
        this._members = [];
        this._joined = false;
        this.alive = true;
        container.appendChild(root);
    }

    /** The widget's own branch: what it mints its elements and sub-branches on. */
    get branch() { return this._dom; }

    /** Joined to what it shares with its bench: the memberships it made. None, unless said. */
    joinParties(given) { return []; }

    /** Called as it is disposed, before its branch goes. */
    disposed() {}

    join(given) {
        if (this._joined) throw new Error("[" + this._name + "] joined already: leave first");
        this._joined = true;
        this._members = (this.joinParties(given || {}) || []).filter(function (m) { return m; });
    }

    leave() {
        this._members.forEach(function (m) { m.leave(); });
        this._members = [];
        this._joined = false;
    }

    /** An element on its own branch: its class worn, appended to `into` when given, its text set when given. */
    el(name, tag, cls, into, text) {
        var e = this._dom.createElement(name, tag);
        if (cls) css.addClass(e, cls);
        if (text != null) e.textContent = text;
        if (into) into.appendChild(e);
        return e;
    }

    /** A native button of its own: its press is the pointer's way; Enter and Space on it, the native world's. */
    button(name, label, onClick, into) {
        var b = this.el(name, "button", wb_btn, into, label);
        b.type = "button";
        b.addEventListener("click", onClick);
        return b;
    }

    activate() { Keys.claim(this.focus); }

    /** Given the keys: on to what takes them - unless the browser's focus arriving there is what gave them. */
    granted(by) { if (by !== "native" && this.keysTo && typeof this.keysTo.focus === "function") this.keysTo.focus(); }

    /** Holding the keys, nothing natively focused: Escape gives them back. */
    keyDown(ev) {
        if (ev.key === "Escape") { Keys.yield(this.focus); return true; }
        return false;
    }

    dispose() {
        this.alive = false;
        this.leave();
        this.disposed();
        if (this._off) { this._off(); this._off = null; }
        this._focusParty.dissolve();
        this._dom.dissolve();
    }
}
