// =============================================================================
// JsonTextSecretary — the secretary of a JSON text party: the one document the
// JSON Tree bench's widgets share, as its text, opening on the sample. Pure: no
// DOM, no clock; the state handed in is never changed.
//
//   state  { text, revision, lastSetBy: a member's id | null, recentUnknown: [{ kind, from }] }
//
//   SetText { text }     text := that text, revision + 1; Text to every member - the one
//                        who set it too, so every widget hears the same story
//   CurrentRequested     Text to the member that asked, alone
//   anything else        kept in recentUnknown, nothing done: Text is the party's own word
// =============================================================================

var JsonTextSecretary = {

    initial: { text: JsonDocStore.SAMPLE, revision: 0, lastSetBy: null, recentUnknown: [] },

    /** How many unknown messages are kept. */
    UNKNOWN_KEPT: 5,

    behavior: function (state, envelope) {
        var m = envelope.message;
        switch (m.kind) {
            case "SetText": {
                var next = { text: m.text, revision: state.revision + 1, lastSetBy: envelope.from, recentUnknown: state.recentUnknown };
                return { newState: next, actions: [{ kind: "BroadcastToMembers", message: { kind: "Text", text: next.text, revision: next.revision } }] };
            }
            case "CurrentRequested":
                return { newState: state, actions: [{ kind: "SendToMember", to: envelope.from, message: { kind: "Text", text: state.text, revision: state.revision } }] };
            default: {
                var unknown = state.recentUnknown.concat([{ kind: m.kind, from: envelope.from }]).slice(-JsonTextSecretary.UNKNOWN_KEPT);
                return { newState: { text: state.text, revision: state.revision, lastSetBy: state.lastSetBy, recentUnknown: unknown }, actions: [] };
            }
        }
    }
};
