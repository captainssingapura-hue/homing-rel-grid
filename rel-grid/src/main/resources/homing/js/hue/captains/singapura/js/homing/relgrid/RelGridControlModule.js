// =============================================================================
// RelGridControlModule — RFC 0050 · Episode 2's DEEP: the grid hands control
// to a cell, and takes it back. Shallow and deep, enforced here and nowhere
// else.
//
// Deep is entered ONLY through the grid — Enter or a double-click on the
// cursor's cell — and the offer is made in TWO STAGES:
//   1. the column's constraint, declared once by the relation. A constrained
//      column's cell is NEVER ASKED (map 16, law 112).
//   2. cell.mayTakeControl(), asked afresh every time. Only an explicit true
//      is a yes.
// Then, and only then: cell.editorElement() — the editor, an element the cell
// OWNS — is placed by the grid in an anchor over the slot but OUTSIDE the
// table; then cell.takeControl(), which MUST answer a thenable; then the
// focus goes to the editor. The grid is deep until that thenable SETTLES,
// resolved or rejected alike, because an edit that blew up still ended; it
// never learns what happened inside, and resumes: shallow, the anchor down,
// the focus on the table. Structure refuses always; a cell refuses
// sometimes; neither substitutes for the other (law 113). Two stages rather
// than one because bulk edit and paste must ask whether a cell is writable
// WITHOUT opening it.
//
// Nothing is marked deep until the cell has answered, so there is no
// optimistic state to roll back — and a settle cannot arrive before the
// bookkeeping is done, because a thenable's callbacks never run in the task
// that created it.
//
//   new RelGridControl({ layout, cells, readOnly, tell, onTaken?, onReleased? })
//   may(id)              both stages, without opening anything
//   take(at)             at: { id, i, j } — the handover; true when control was taken
//   isDeep() / destroy()
// =============================================================================

class RelGridControl {

    constructor(opts) {
        this._layout = opts.layout;
        this._cells = opts.cells;
        this._readOnly = opts.readOnly;           // a Set of columns never asked
        this._tell = opts.tell;                   // (id, mode): the cursor's, so a cell hears one voice
        this._onTaken = opts.onTaken || null;
        this._onReleased = opts.onReleased || null;
        this._deep = false;                       // the one fact the grid holds about editing
        this._destroyed = false;
    }

    isDeep() { return this._deep; }

    /**
     * May this cell take control right now? The two refusals, in order: the
     * column's declared constraint, answered without touching the cell at
     * all, and then the cell's own judgement. A cell missing any of the three
     * control methods is offered nothing.
     */
    may(id) {
        if (this._readOnly.has(id.column)) return false;   // structural: never asked
        var entry = this._cells.get(id.pk, id.column);
        var cell = entry && entry.cell;
        if (!cell) return false;
        if (typeof cell.mayTakeControl !== "function" || typeof cell.editorElement !== "function"
                || typeof cell.takeControl !== "function") return false;
        var may;
        try { may = cell.mayTakeControl(); }
        catch (e) { console.error("[RelGrid] cell.mayTakeControl threw:", e); return false; }
        return may === true;                               // only an explicit yes
    }

    /** The handover. Both stages must pass, and the cell must answer with a thenable. */
    take(at) {
        var id = at.id;
        if (!this.may(id)) return false;
        var cell = this._cells.get(id.pk, id.column).cell;
        // The editor is the cell's noun: asked for, placed in the grid's anchor,
        // and only then is control offered — so the cell arms an editor that is
        // already where it will be seen.
        var editor;
        try { editor = cell.editorElement(); }
        catch (e) { console.error("[RelGrid] cell.editorElement threw:", e); return false; }
        if (!editor || typeof editor !== "object" || typeof editor.appendChild !== "function") {
            console.error("[RelGrid] cell.editorElement must answer an element; got:", editor);
            return false;
        }
        var host = this._layout.openOverlay(at.i, at.j);
        if (!host) return false;
        host.appendChild(editor);
        var out;
        try { out = cell.takeControl(); }
        catch (e) {
            console.error("[RelGrid] cell.takeControl threw:", e);
            this._layout.closeOverlay();
            return false;
        }
        if (!out || typeof out.then !== "function") {
            // A contract violation, not a decline: it already said it may.
            // Recorded and refused, because waiting for a settle that cannot
            // come would leave the grid inert with nothing to show for it.
            console.error("[RelGrid] cell.takeControl must answer a thenable; got:", out);
            this._layout.closeOverlay();
            return false;
        }
        this._deep = true;
        this._layout.setDeep(true);
        if (typeof editor.focus === "function") editor.focus({ preventScroll: true });   // the keys are the cell's now
        this._tell(id, "deep");
        if (this._onTaken) {
            try { this._onTaken(id.pk, id.column); }
            catch (e) { console.error("[RelGrid] onControlTaken threw:", e); }
        }
        var self = this;
        out.then(function () { self._resume(id); },
                 function (e) {
                     console.error("[RelGrid] the cell's control ended in a rejection:", e);
                     self._resume(id);
                 });
        return true;
    }

    /** The cell handed control back. What happened inside is not the grid's. */
    _resume(id) {
        // A settle can arrive after the grid is gone, or after something else
        // has already resumed it. Neither may resurrect a dead grid or fire a
        // second report.
        if (this._destroyed || !this._deep) return;
        this._deep = false;
        this._layout.closeOverlay();           // the editor goes with the session
        this._layout.setDeep(false);
        this._tell(id, "shallow");
        this._layout.focus();                  // the keyboard host takes the keys again
        if (this._onReleased) {
            try { this._onReleased(id.pk, id.column); }
            catch (e) { console.error("[RelGrid] onControlReleased threw:", e); }
        }
    }

    /** A late settle must not resume a dead grid. */
    destroy() { this._destroyed = true; }
}
