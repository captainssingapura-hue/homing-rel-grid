// =============================================================================
// HanLayout — the Han Article bench's RENDERING ENGINE: text in, rows of
// square slots out, a fixed number per row. DOMAIN CODE and a PURE
// FUNCTION: it knows nothing of any grid, holds nothing, and can be run
// headlessly on any string.
//
//   hanLayout(text, cols) → { rows, cols, glyphs, length }
//   hanIsPunct(ch)        → is this one character a punctuation mark?
//
//     rows     an array of rows; every row is EXACTLY `cols` slots long
//     a slot   { glyph, at, len, kind }
//              glyph  what the slot shows — one character, or TWO punctuation
//                     marks — or null for an empty slot
//              at     where in the text (in code points) a commit to this slot
//                     goes: the first glyph's index, or the insertion point for
//                     an empty slot
//              len    how many code points a commit REPLACES: 1 for a
//                     character, 2 for a pair of marks, 0 for an empty slot,
//                     where a commit INSERTS
//              kind   'han' | 'punct' | 'empty'
//
// The rules, small enough to state:
//   · one character, one slot, left to right, `cols` to a row, then wrap;
//   · TWO PUNCTUATION MARKS SHARE A SLOT: a mark that follows a slot holding
//     one lone mark joins it, in the same row. That is the whole rule for
//     now — a mark that falls at the start of a row starts a slot there like
//     anything else. Where a mark at the end of a line should go instead is
//     the next iteration, and it is the grid's question, not this file's;
//   · a newline ends the row where it stands, and the row is padded; the
//     padding slots insert BEFORE the newline, so typing in the blank tail of
//     a line extends that line rather than the next;
//   · a newline right after a wrap is the wrap — it adds no blank row — but
//     a second newline does, because that is a blank line;
//   · there is always somewhere to type: if the last row is full, one empty
//     row follows it, whose slots append at the end.
//
// Characters are CODE POINTS (Array.from), not UTF-16 units, so a glyph from
// a supplementary plane is one slot and not two — and `at` is in the same
// units, which is what the store splices by.
//
// Not yet here, deliberately: the squeezed leading and trailing punctuation
// columns, and any notion of width other than "one square" — a pair of marks
// shares a square; it does not narrow one. Those are the next iterations.
// =============================================================================

// CJK punctuation and symbols (not the ideographic space, which is a square
// of its own), fullwidth ASCII punctuation, general punctuation (dashes,
// quotes, the ellipsis — not the spaces), and the middle dot of a name.
var _HAN_PUNCT = /^[\u3001-\u303F\uFF01-\uFF0F\uFF1A-\uFF20\uFF3B-\uFF40\uFF5B-\uFF65\u2010-\u2027\u2030-\u205E\u00B7]$/;

function hanIsPunct(ch) {
    return typeof ch === "string" && _HAN_PUNCT.test(ch);
}

function hanLayout(text, cols) {
    cols = (cols > 0) ? cols : 9;
    var chars = Array.from(String(text == null ? "" : text).replace(/\r\n?/g, "\n"));
    var rows = [], row = [], justWrapped = false, glyphs = 0;
    var lastSlot = null;                       // the slot placed last — a mark may join it

    function pad(r, at) {
        while (r.length < cols) r.push({ glyph: null, at: at, len: 0, kind: "empty" });
        return r;
    }

    for (var i = 0; i < chars.length; i++) {
        var ch = chars[i];
        if (ch === "\n") {
            if (row.length === 0 && justWrapped) { justWrapped = false; lastSlot = null; continue; }
            rows.push(pad(row, i));            // the blank tail inserts before the break
            row = [];
            justWrapped = false;
            lastSlot = null;                   // nothing joins across a line
            continue;
        }
        var punct = hanIsPunct(ch);
        // A lone mark takes this one as its partner — even when it was the ninth
        // square and the row has already closed, since joining costs no slot.
        if (punct && lastSlot && lastSlot.kind === "punct" && lastSlot.len === 1) {
            lastSlot.glyph += ch;
            lastSlot.len = 2;
            glyphs++;
            continue;
        }
        var slot = { glyph: ch, at: i, len: 1, kind: punct ? "punct" : "han" };
        row.push(slot);
        lastSlot = slot;
        glyphs++;
        justWrapped = false;
        if (row.length === cols) { rows.push(row); row = []; justWrapped = true; }
    }
    if (row.length > 0) rows.push(pad(row, chars.length));

    // Always somewhere to type.
    var last = rows.length ? rows[rows.length - 1] : null;
    var full = !!last;
    if (last) for (var k = 0; k < last.length; k++) if (last[k].glyph === null) { full = false; break; }
    if (!last || full) rows.push(pad([], chars.length));

    return { rows: rows, cols: cols, glyphs: glyphs, length: chars.length };
}
