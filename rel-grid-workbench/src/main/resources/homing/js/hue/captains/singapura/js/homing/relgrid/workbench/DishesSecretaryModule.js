// =============================================================================
// DishesSecretary — the secretary of a dishes party: the Replicating Tables
// bench's one store of dishes. It does no I/O: the dishes kept across visits
// are asked of the steward the first time a member wants them, and handed to
// the steward after every change. What a change may be is the store's rule
// (DishStore.committed / DishStore.sold), applied here to the data alone.
// Pure: no DOM, no clock, no dice; the state handed in is never changed.
//
//   state  { dishes: the store's data | null - not yet loaded, revision, loading,
//            lastChangedBy: a member's id | null, refused: n, recentUnknown: [{ kind, from }] }
//
//   CurrentRequested     loaded: State to the member that asked, alone. Not yet: Load to the
//                        steward, once; every member hears the State it brings
//   Loaded { dishes, found }  the kept dishes when well formed, the seed otherwise - unless a
//                        member changed them meanwhile; State to every member
//   Commit { pk, column, value }  the store's commit, the value parsed from JSON; refused -
//                        no such dish, a column nobody writes, a value that does not parse -
//                        counted, nothing said. Accepted: revision + 1, State to every
//                        member, Save to the steward
//   Sell { pk, n }       the store's sale, likewise
//   Reset                the seed, likewise
//   Load / Save, back from "unrouted" - no steward to take them: a Load is answered with
//                        the seed, a Save is dropped
//   anything else        kept in recentUnknown, nothing done: State is the party's own word
// =============================================================================

var DishesSecretary = {

    initial: { dishes: null, revision: 0, loading: false, lastChangedBy: null, refused: 0, recentUnknown: [] },

    /** How many unknown messages are kept. */
    UNKNOWN_KEPT: 5,

    behavior: function (state, envelope) {
        var m = envelope.message, S = DishesSecretary;
        switch (m.kind) {
            case "CurrentRequested":
                if (state.dishes !== null) return { newState: state, actions: [S._state(state, envelope.from)] };
                if (state.loading) return { newState: state, actions: [] };
                return { newState: S._with(state, { loading: true }), actions: [{ kind: "SendToSteward", message: { kind: "Load" } }] };

            case "Loaded": {
                if (state.dishes !== null) return { newState: S._with(state, { loading: false }), actions: [] };   // a member changed them meanwhile
                var kept = m.found ? S._parse(m.dishes) : null;
                return S._loaded(state, DishStore.wellFormed(kept) ? kept : DishStore.seed());
            }

            case "Commit": {
                var value = S._parse(m.value);
                var committed = (state.dishes !== null && value !== undefined) ? DishStore.committed(state.dishes, m.pk, m.column, value) : null;
                return committed ? S._changed(state, committed, envelope.from) : S._refused(state);
            }

            case "Sell": {
                var sold = state.dishes !== null ? DishStore.sold(state.dishes, m.pk, m.n) : null;
                return sold ? S._changed(state, sold, envelope.from) : S._refused(state);
            }

            case "Reset":
                return S._changed(state, DishStore.seed(), envelope.from);

            case "Load":                                     // back from "unrouted": no steward keeps the dishes
                return state.dishes !== null ? { newState: state, actions: [] } : S._loaded(state, DishStore.seed());

            case "Save":                                     // back from "unrouted": nothing to keep them
                return { newState: state, actions: [] };

            default: {
                var unknown = state.recentUnknown.concat([{ kind: m.kind, from: envelope.from }]).slice(-S.UNKNOWN_KEPT);
                return { newState: S._with(state, { recentUnknown: unknown }), actions: [] };
            }
        }
    },

    _with: function (state, changes) {
        var next = { dishes: state.dishes, revision: state.revision, loading: state.loading, lastChangedBy: state.lastChangedBy,
                     refused: state.refused, recentUnknown: state.recentUnknown };
        for (var k in changes) if (Object.prototype.hasOwnProperty.call(changes, k)) next[k] = changes[k];
        return next;
    },

    /** JSON text to a value - or undefined, when it does not parse. */
    _parse: function (text) {
        try { return JSON.parse(text); } catch (e) { return undefined; }
    },

    _state: function (state, to) {
        var message = { kind: "State", dishes: JSON.stringify(state.dishes), revision: state.revision };
        return to ? { kind: "SendToMember", to: to, message: message } : { kind: "BroadcastToMembers", message: message };
    },

    _loaded: function (state, dishes) {
        var next = DishesSecretary._with(state, { dishes: dishes, loading: false });
        return { newState: next, actions: [DishesSecretary._state(next, null)] };
    },

    _changed: function (state, dishes, from) {
        var next = DishesSecretary._with(state, { dishes: dishes, revision: state.revision + 1, loading: false, lastChangedBy: from });
        return { newState: next, actions: [DishesSecretary._state(next, null), { kind: "SendToSteward", message: { kind: "Save", dishes: JSON.stringify(dishes) } }] };
    },

    _refused: function (state) {
        return { newState: DishesSecretary._with(state, { refused: state.refused + 1 }), actions: [] };
    }
};
