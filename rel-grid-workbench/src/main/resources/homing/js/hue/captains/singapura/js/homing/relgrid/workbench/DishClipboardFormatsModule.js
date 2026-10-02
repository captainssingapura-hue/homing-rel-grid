// =============================================================================
// DishClipboardFormats — what a selection of dishes is WORTH on a clipboard,
// composed. PURE LOGIC: strings in, strings out, no DOM anywhere — and that is
// why the HTML a spreadsheet or a mail will paste is written here, as data,
// and nowhere a view is built. A clipboard leaves the page, so its HTML
// carries its own looks inline, literal colours and all: a theme token means
// nothing in a spreadsheet.
//
//   DishClipboardFormats.content(relation, blocks, format, { headers? })
//       → RelGridClipboardContent          format: 'tsv' | 'csv' | 'html'
//   DishClipboardFormats.starsHtml(value, stars)   a rating as HTML: the stars a person saw, in gold
//
// The copier reads CELLS, not the store (map 6 law 48): a cell knows what it
// is worth on a clipboard better than a store does, which is exactly what the
// rating proves. In text it is a number, because that is what a spreadsheet
// can add up; in HTML it is the stars a person saw, because that is what they
// meant to copy. A cell that offers clipboardHtml() is taken at its word;
// one that does not is escaped.
// =============================================================================

class DishClipboardFormats {
    /** A field for a delimited line: quoted only when it has to be, RFC 4180 style. */
    static _delimited(v, delim) {
        var s = (v == null) ? "" : String(v);
        var must = s.indexOf(delim) >= 0 || s.indexOf('"') >= 0 || s.indexOf("\n") >= 0 || s.indexOf("\r") >= 0;
        return must ? '"' + s.replace(/"/g, '""') + '"' : s;
    }

    static _escapeHtml(v) {
        return String(v == null ? "" : v)
            .replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;").replace(/"/g, "&quot;");
    }

    /** What the relation's OWN cell says it holds. Identities in, worth out. */
    static _cellOf(relation, pk, col) { return relation.cellFor(pk, col); }
    static _cellText(cell) {
        var v = (cell && typeof cell.value === "function") ? cell.value() : null;
        return (v == null) ? "" : v;
    }
    static _cellHtml(cell) {
        if (cell && typeof cell.clipboardHtml === "function") return cell.clipboardHtml();
        return DishClipboardFormats._escapeHtml(DishClipboardFormats._cellText(cell));
    }

    static _htmlTable(relation, block, headers) {
        var out = ['<table style="border-collapse:collapse;font:13px sans-serif">'];
        if (headers) {
            out.push("<thead><tr>");
            for (var c = 0; c < block.columns.length; c++)
                out.push('<th style="text-align:left;padding:2px 8px;border-bottom:1px solid #999">'
                         + DishClipboardFormats._escapeHtml(block.columns[c]) + "</th>");
            out.push("</tr></thead>");
        }
        out.push("<tbody>");
        for (var r = 0; r < block.pks.length; r++) {
            out.push("<tr>");
            for (var k = 0; k < block.columns.length; k++) {
                var cell = DishClipboardFormats._cellOf(relation, block.pks[r], block.columns[k]);
                var numeric = typeof DishClipboardFormats._cellText(cell) === "number";
                out.push('<td style="padding:2px 8px' + (numeric ? ";text-align:right" : "") + '">'
                         + DishClipboardFormats._cellHtml(cell) + "</td>");
            }
            out.push("</tr>");
        }
        out.push("</tbody></table>");
        return out.join("");
    }

    /**
     * The content, composed. Text is always there — TSV for 'tsv' and 'html'
     * alike, since a spreadsheet takes tabs from text/plain, and CSV for 'csv'.
     * HTML rides beside it only for 'html'. Blocks are kept apart: a blank line
     * in text, a table each in HTML. Nothing is refused here; the bench copies
     * any shape it is handed and lets the paste target make of it what it will.
     */
    static content(relation, blocks, format, opts) {
        opts = opts || {};
        var headers = opts.headers !== false;
        var delim = (format === "csv") ? "," : "\t";
        var textParts = [], htmlParts = [];
        for (var b = 0; b < blocks.length; b++) {
            var block = blocks[b], lines = [], row, c;
            if (headers) {
                row = [];
                for (c = 0; c < block.columns.length; c++) row.push(DishClipboardFormats._delimited(block.columns[c], delim));
                lines.push(row.join(delim));
            }
            for (var r = 0; r < block.pks.length; r++) {
                row = [];
                for (c = 0; c < block.columns.length; c++)
                    row.push(DishClipboardFormats._delimited(DishClipboardFormats._cellText(DishClipboardFormats._cellOf(relation, block.pks[r], block.columns[c])), delim));
                lines.push(row.join(delim));
            }
            textParts.push(lines.join("\n"));
            if (format === "html") htmlParts.push(DishClipboardFormats._htmlTable(relation, block, headers));
        }
        return new RelGridClipboardContent(textParts.join("\n\n"),
                                           (format === "html") ? htmlParts.join("\n") : undefined);
    }

    /**
     * A rating on a clipboard that takes HTML: the stars a person SAW, not the
     * number underneath. The stars cell answers its clipboardHtml() with this,
     * over its own value and the stars it draws.
     */
    static starsHtml(value, stars) {
        return '<span style="color:#e0a300;letter-spacing:2px" title="' + DishClipboardFormats._escapeHtml(value) + ' of 5">'
             + DishClipboardFormats._escapeHtml(stars) + "</span>";
    }
}
