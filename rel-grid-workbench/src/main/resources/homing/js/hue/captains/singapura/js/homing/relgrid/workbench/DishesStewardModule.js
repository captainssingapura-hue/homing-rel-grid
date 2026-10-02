// =============================================================================
// DishesSteward — the steward of a dishes party: the one member that does I/O,
// keeping the Replicating Tables bench's dishes in the browser's storage across
// visits. Hired by the page's root dishes party, and by nothing linked below
// it. A private window, or a full quota: the bench still works, in memory.
//
//   new DishesSteward(tell)
//   steward.reactors   Load - the dishes kept, told back as Loaded; Save { dishes } - kept
// =============================================================================

var _DISHES_KEY = "bench.replicatingTables.dishes.v3";

class DishesSteward {
    constructor(tell) {
        this._tell = tell;
        var self = this;
        this.reactors = Object.freeze({
            Load: function () { self._load(); },
            Save: function (m) { self._save(m.dishes); }
        });
    }

    _load() {
        var raw = null;
        try { raw = (typeof localStorage !== "undefined") ? localStorage.getItem(_DISHES_KEY) : null; }
        catch (e) { raw = null; }
        this._tell(typeof raw === "string" ? { kind: "Loaded", dishes: raw, found: true } : { kind: "Loaded", dishes: "", found: false });
    }

    _save(dishes) {
        try { if (typeof localStorage !== "undefined") localStorage.setItem(_DISHES_KEY, dishes); }
        catch (e) { /* a private window, or quota - the bench still works in memory */ }
    }
}
