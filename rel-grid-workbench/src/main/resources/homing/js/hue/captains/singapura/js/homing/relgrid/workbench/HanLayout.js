// =============================================================================
// HanLayout — the Han Article bench's RENDERING ENGINE: text in, rows of
// square slots out, a fixed number per row. DOMAIN CODE and a PURE
// FUNCTION: it knows nothing of any grid, holds nothing, and can be run
// headlessly on any string.
//
//   hanLayout(text, cols) → { rows, cols, glyphs, length, usesLead, usesTrail, spanKey }
//   hanIsPunct(ch)        → is this one character a punctuation mark?
//   hanIsOpener(ch)       → is it an OPENING bracket — a mark that may not end a line?
//   hanIsNarrow(ch)       → is it a character that takes HALF a square — Latin, digits, spaces?
//
//     a row    { lead, cells, trail }
//              cells  EXACTLY `cols` slots, the squares
//              lead   a slot or null — the half-square BEFORE the first square
//              trail  a slot or null — the half-square AFTER the last square
//     a slot   { glyph, kind, span? }
//              glyph  what the slot shows — one character, TWO punctuation
//                     marks, or a RUN of narrow characters — or null for an
//                     empty square
//              kind   'han' | 'punct' | 'latin' | 'covered' | 'empty'
//              span   for a run: how many squares it reaches over, its own
//                     included; the squares after it are 'covered'
//     usesLead, usesTrail    whether any row has one — the columns to show
//     spanKey                the spans of every row, as one string: when it
//                            changes, the arrangement has
//
// The rules, small enough to state:
//   · one character, one square, left to right, `cols` to a row, then wrap;
//   · TWO PUNCTUATION MARKS SHARE A SQUARE: a mark that follows a square
//     holding one lone mark joins it — even when that mark took the last
//     square of a row and the row has already closed, since joining costs
//     no square — and nothing joins across a line;
//   · A CLOSING MARK MAY NOT START A LINE. When a row is full and the next
//     character is a mark that is not an opener, it is SQUEEZED into the
//     row's trailing half-square rather than starting the next row. One
//     mark fits there; a further mark starts the next row after all;
//   · AN OPENING BRACKET MAY NOT END A LINE. When an opener would take the
//     last square of a row, the row closes with that square empty and the
//     opener goes into the NEXT row's leading half-square, ahead of its
//     first square, so the line it opens still has all its squares;
//   · NARROW CHARACTERS RUN TWO TO A SQUARE. A run — a maximal sequence of
//     Latin letters, digits, spaces and ASCII marks — is ONE slot reaching
//     over ⌈length/2⌉ squares; the squares it reaches over are 'covered'.
//     A run that does not fit what is left of the row moves whole to the next
//     row if it fits a row at all, and is broken at the row's end otherwise;
//   · a newline ends the row where it stands, and the row is padded;
//   · a newline right after a wrap is the wrap — it adds no blank row — but
//     a second newline does, because that is a blank line;
//   · an empty text is one empty row, so there is always a square to show.
//
// The two half-squares are what this bench asks of the grid: two columns a
// relation declares and shows only when some row uses them, each half the
// width of a square. Where they sit, and that a column may be that narrow,
// is the grid's; what goes in them is decided here.
//
// Characters are CODE POINTS (Array.from), not UTF-16 units, so a glyph from
// a supplementary plane is one square and not two. The article is edited as
// TEXT, elsewhere; this function only ever reads it.
// =============================================================================

// CJK punctuation and symbols U+3001–303F (not U+3000, the ideographic space,
// which is a square of its own); fullwidth ASCII punctuation U+FF01–FF0F,
// FF1A–FF20, FF3B–FF40, FF5B–FF65; general punctuation U+2010–2027 and
// 2030–205E (dashes, quotes, the ellipsis — not the spaces); and U+00B7, the
// middle dot of a name. Written as the characters, so the ranges read.
var _HAN_PUNCT = /^[、-〿！-／：-＠［-｀｛-･‐-‧‰-⁞·]$/;

// The opening brackets: a mainland-style font keeps their ink in the RIGHT
// half of the em, and none of them may end a line.
var _HAN_OPENERS = "「『（《〈【〔｛［";

function hanIsPunct(ch) {
    return typeof ch === "string" && _HAN_PUNCT.test(ch);
}

function hanIsOpener(ch) {
    return typeof ch === "string" && ch.length > 0 && _HAN_OPENERS.indexOf(ch) >= 0;
}

// What takes a WHOLE square: the CJK ideographs and their extensions, kana,
// hangul, the fullwidth forms, the ideographic space, and every mark. What
// is left — Latin, digits, spaces, ASCII marks — is narrow, two to a square.
var _HAN_WIDE = /^[\u2E80-\u9FFF\uAC00-\uD7AF\uF900-\uFAFF\uFF00-\uFFEF\u3000]$|^[\uD840-\uD87F][\uDC00-\uDFFF]$/;

function hanIsNarrow(ch) {
    return typeof ch === "string" && ch.length > 0 && ch !== "\n" && !hanIsPunct(ch) && !_HAN_WIDE.test(ch);
}

function hanLayout(text, cols) {
    cols = (cols > 0) ? cols : 9;
    var chars = Array.from(String(text == null ? "" : text).replace(/\r\n?/g, "\n"));
    var rows = [], glyphs = 0, usesLead = false, usesTrail = false;
    var spanKey = [];
    var row = newRow();
    var lastRow = null;                        // the row closed last — a closer may squeeze into it
    var justWrapped = false;
    var lastSlot = null;                       // the square placed last — a mark may join it

    function newRow() { return { lead: null, cells: [], trail: null }; }
    function close(r) {
        while (r.cells.length < cols) r.cells.push({ glyph: null, kind: "empty" });
        rows.push(r);
        lastRow = r;
        var k = "";
        for (var c = 0; c < r.cells.length; c++) k += (r.cells[c].span || 1);
        spanKey.push(k);
    }
    // A run of narrow characters as one slot, and the squares it reaches over.
    function placeRun(run) {
        var need = Math.ceil(run.length / 2);
        var slot = { glyph: run.join(""), kind: "latin", span: need };
        row.cells.push(slot);
        for (var k = 1; k < need; k++) row.cells.push({ glyph: null, kind: "covered" });
        lastSlot = slot;
        glyphs += run.length;
        justWrapped = false;
        if (row.cells.length >= cols) { close(row); row = newRow(); justWrapped = true; }
    }

    for (var i = 0; i < chars.length; i++) {
        var ch = chars[i];
        if (ch === "\n") {
            if (row.cells.length === 0 && row.lead === null && justWrapped) { justWrapped = false; lastSlot = null; continue; }
            close(row);
            row = newRow();
            justWrapped = false;
            lastSlot = null;                   // nothing joins across a line
            continue;
        }
        if (hanIsNarrow(ch)) {
            // Gather the run, then fit it: whole in what is left, whole on the next
            // row, or broken at the row's end — two characters a square.
            var run = [ch];
            while (i + 1 < chars.length && hanIsNarrow(chars[i + 1])) run.push(chars[++i]);
            while (run.length > 0) {
                var left = cols - row.cells.length;
                var need = Math.ceil(run.length / 2);
                if (need <= left) { placeRun(run); run = []; }
                else if (need <= cols && row.cells.length > 0) { close(row); row = newRow(); justWrapped = false; lastSlot = null; }
                else { placeRun(run.slice(0, left * 2)); run = run.slice(left * 2); }
            }
            continue;
        }
        var punct = hanIsPunct(ch);
        if (punct && lastSlot && lastSlot.kind === "punct" && Array.from(lastSlot.glyph).length === 1) {
            lastSlot.glyph += ch;              // a lone mark takes this one as its partner
            glyphs++;
            continue;
        }
        if (punct && !hanIsOpener(ch) && justWrapped && row.cells.length === 0 && row.lead === null
                && lastRow && lastRow.trail === null) {
            // A closer at the head of a line is squeezed back into the trailing
            // half-square of the line it belongs to. Nothing joins it there.
            lastRow.trail = { glyph: ch, kind: "punct" };
            usesTrail = true;
            glyphs++;
            lastSlot = null;
            continue;
        }
        if (hanIsOpener(ch) && row.cells.length === cols - 1) {
            // An opener in the last square would end the line. The line closes
            // with that square empty, and the opener leads the next line.
            close(row);
            row = newRow();
            row.lead = { glyph: ch, kind: "punct" };
            usesLead = true;
            glyphs++;
            justWrapped = false;
            lastSlot = null;
            continue;
        }
        var slot = { glyph: ch, kind: punct ? "punct" : "han" };
        row.cells.push(slot);
        lastSlot = slot;
        glyphs++;
        justWrapped = false;
        if (row.cells.length === cols) { close(row); row = newRow(); justWrapped = true; }
    }
    if (row.cells.length > 0 || row.lead !== null) close(row);
    if (rows.length === 0) close(newRow());

    return { rows: rows, cols: cols, glyphs: glyphs, length: chars.length,
             usesLead: usesLead, usesTrail: usesTrail, spanKey: spanKey.join("|") };
}
