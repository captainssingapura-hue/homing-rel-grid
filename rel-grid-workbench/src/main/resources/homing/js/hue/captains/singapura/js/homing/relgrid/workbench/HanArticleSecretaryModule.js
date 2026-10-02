// =============================================================================
// HanArticleSecretary — the secretary of a Han article party: the one article
// the Han Article bench's editor and displays share. It does no I/O: the
// article kept across visits is asked of the steward the first time a member
// wants it, and handed to the steward after every change. Pure: no DOM, no
// clock; the state handed in is never changed.
//
//   state  { text: the article | null - not yet loaded, revision, loading,
//            lastSetBy: a member's id | null, recentUnknown: [{ kind, from }] }
//
//   CurrentRequested     the article loaded: Text to the member that asked, alone. Not yet:
//                        Load to the steward, once; every member hears the Text it brings
//   Loaded { text, found }  the kept article, or the poem when none was kept - unless a member
//                        set one meanwhile; Text to every member
//   SetText { text }     the article := that text, revision + 1; Text to every member, the
//                        one who set it too; Save to the steward. The same text again is nothing
//   Reset                as SetText with the poem
//   Load / Save, back from "unrouted" - no steward to take them: a Load is answered with the
//                        poem, a Save is dropped
//   anything else        kept in recentUnknown, nothing done: Text is the party's own word
// =============================================================================

var HanArticleSecretary = {

    initial: { text: null, revision: 0, loading: false, lastSetBy: null, recentUnknown: [] },

    /** How many unknown messages are kept. */
    UNKNOWN_KEPT: 5,

    behavior: function (state, envelope) {
        var m = envelope.message, S = HanArticleSecretary;
        switch (m.kind) {
            case "CurrentRequested":
                if (state.text !== null) return { newState: state, actions: [S._text(state, envelope.from)] };
                if (state.loading) return { newState: state, actions: [] };
                return { newState: S._with(state, { loading: true }), actions: [{ kind: "SendToSteward", message: { kind: "Load" } }] };

            case "Loaded":
                if (state.text !== null) return { newState: S._with(state, { loading: false }), actions: [] };   // a member set one meanwhile
                return S._loaded(state, m.found ? m.text : HanStore.SEED);

            case "SetText":
                return S._set(state, m.text, envelope.from);

            case "Reset":
                return S._set(state, HanStore.SEED, envelope.from);

            case "Load":                                     // back from "unrouted": no steward keeps the article
                return state.text !== null ? { newState: state, actions: [] } : S._loaded(state, HanStore.SEED);

            case "Save":                                     // back from "unrouted": nothing to keep it
                return { newState: state, actions: [] };

            default: {
                var unknown = state.recentUnknown.concat([{ kind: m.kind, from: envelope.from }]).slice(-S.UNKNOWN_KEPT);
                return { newState: S._with(state, { recentUnknown: unknown }), actions: [] };
            }
        }
    },

    _with: function (state, changes) {
        var next = { text: state.text, revision: state.revision, loading: state.loading, lastSetBy: state.lastSetBy, recentUnknown: state.recentUnknown };
        for (var k in changes) if (Object.prototype.hasOwnProperty.call(changes, k)) next[k] = changes[k];
        return next;
    },

    _text: function (state, to) {
        var message = { kind: "Text", text: state.text, revision: state.revision };
        return to ? { kind: "SendToMember", to: to, message: message } : { kind: "BroadcastToMembers", message: message };
    },

    _loaded: function (state, text) {
        var next = HanArticleSecretary._with(state, { text: text, loading: false });
        return { newState: next, actions: [HanArticleSecretary._text(next, null)] };
    },

    _set: function (state, text, from) {
        if (text === state.text) return { newState: state, actions: [] };
        var next = HanArticleSecretary._with(state, { text: text, revision: state.revision + 1, loading: false, lastSetBy: from });
        return { newState: next, actions: [HanArticleSecretary._text(next, null), { kind: "SendToSteward", message: { kind: "Save", text: text } }] };
    }
};
