// =============================================================================
// HanLayout — the Han Article bench's RENDERING ENGINE, first iteration: text
// in, a fixed number of square slots per row out. DOMAIN CODE and a PURE
// FUNCTION: it knows nothing of any grid, holds nothing, and can be run
// headlessly on any string.
//
//   hanLayout(text, cols) → { rows, cols, glyphs, length }
//
//     rows     an array of rows; every row is EXACTLY `cols` slots long
//     a slot   { glyph, at, len }
//              glyph  the character shown, or null for an empty slot
//              at     where in the text (in code points) a commit to this slot
//                     goes: the glyph's own index, or the insertion point for
//                     an empty slot
//              len    1 for a glyph — a commit REPLACES it — and 0 for an
//                     empty slot — a commit INSERTS there
//
// The rules, small enough to state:
//   · one character, one slot, left to right, `cols` to a row, then wrap;
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
// Not yet here, deliberately: punctuation sharing a slot, the squeezed
// leading and trailing punctuation columns, and any notion of width other
// than "one glyph, one square". Those are the next iterations.
// =============================================================================

function hanLayout(text, cols) {
    cols = (cols > 0) ? cols : 9;
    var chars = Array.from(String(text == null ? "" : text).replace(/\r\n?/g, "\n"));
    var rows = [], row = [], justWrapped = false, glyphs = 0;

    function pad(r, at) {
        while (r.length < cols) r.push({ glyph: null, at: at, len: 0 });
        return r;
    }

    for (var i = 0; i < chars.length; i++) {
        var ch = chars[i];
        if (ch === "\n") {
            if (row.length === 0 && justWrapped) { justWrapped = false; continue; }
            rows.push(pad(row, i));            // the blank tail inserts before the break
            row = [];
            justWrapped = false;
            continue;
        }
        row.push({ glyph: ch, at: i, len: 1 });
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
