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
//     a slot   { glyph, kind }
//              glyph  what the slot shows — one character, or TWO punctuation
//                     marks — or null for an empty slot
//              kind   'han' | 'punct' | 'empty'
//
// The rules, small enough to state:
//   · one character, one slot, left to right, `cols` to a row, then wrap;
//   · TWO PUNCTUATION MARKS SHARE A SLOT: a mark that follows a slot holding
//     one lone mark joins it — even when that mark took the last square of
//     a row and the row has already closed, since joining costs no slot —
//     and nothing joins across a line. That is the whole rule for now: a
//     mark that falls at the start of a row starts a slot there like
//     anything else. Where a mark at the end of a line should go instead is
//     the next iteration, and it is the grid's question, not this file's;
//   · a newline ends the row where it stands, and the row is padded;
//   · a newline right after a wrap is the wrap — it adds no blank row — but
//     a second newline does, because that is a blank line;
//   · an empty text is one empty row, so there is always a square to show.
//
// Characters are CODE POINTS (Array.from), not UTF-16 units, so a glyph from
// a supplementary plane is one slot and not two.
//
// The article is edited as TEXT, elsewhere; this function only ever reads
// it. Not yet here, deliberately: the squeezed leading and trailing
// punctuation columns, and any notion of width other than "one square" — a
// pair of marks shares a square; it does not narrow one.
// =============================================================================

// CJK punctuation and symbols U+3001–303F (not U+3000, the ideographic space,
// which is a square of its own); fullwidth ASCII punctuation U+FF01–FF0F,
// FF1A–FF20, FF3B–FF40, FF5B–FF65; general punctuation U+2010–2027 and
// 2030–205E (dashes, quotes, the ellipsis — not the spaces); and U+00B7, the
// middle dot of a name. Written as the characters, so the ranges read.
var _HAN_PUNCT = /^[、-〿！-／：-＠［-｀｛-･‐-‧‰-⁞·]$/;

function hanIsPunct(ch) {
    return typeof ch === "string" && _HAN_PUNCT.test(ch);
}

function hanLayout(text, cols) {
    cols = (cols > 0) ? cols : 9;
    var chars = Array.from(String(text == null ? "" : text).replace(/\r\n?/g, "\n"));
    var rows = [], row = [], justWrapped = false, glyphs = 0;
    var lastSlot = null;                       // the slot placed last — a mark may join it

    function pad(r) {
        while (r.length < cols) r.push({ glyph: null, kind: "empty" });
        return r;
    }

    for (var i = 0; i < chars.length; i++) {
        var ch = chars[i];
        if (ch === "\n") {
            if (row.length === 0 && justWrapped) { justWrapped = false; lastSlot = null; continue; }
            rows.push(pad(row));
            row = [];
            justWrapped = false;
            lastSlot = null;                   // nothing joins across a line
            continue;
        }
        var punct = hanIsPunct(ch);
        if (punct && lastSlot && lastSlot.kind === "punct" && Array.from(lastSlot.glyph).length === 1) {
            lastSlot.glyph += ch;              // a lone mark takes this one as its partner
            glyphs++;
            continue;
        }
        var slot = { glyph: ch, kind: punct ? "punct" : "han" };
        row.push(slot);
        lastSlot = slot;
        glyphs++;
        justWrapped = false;
        if (row.length === cols) { rows.push(row); row = []; justWrapped = true; }
    }
    if (row.length > 0) rows.push(pad(row));
    if (rows.length === 0) rows.push(pad([]));

    return { rows: rows, cols: cols, glyphs: glyphs, length: chars.length };
}
