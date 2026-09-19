// =============================================================================
// ExtentBench — the Colour Extents bench's two panes, as JS in a file: each
// widget's body is one line, a call in here.
//
//   mountExtentTable(branch)   → { root, setActive, partyDeregister }
//       rows are scaled words, columns are extents from -1 to 1: every cell is
//       an element wearing one pair, with css.extent(el, x) and nothing else
//   mountExtentCard(branch)    → { root, setActive, partyDeregister }
//       one card wearing three scaled words and one slider setting its extent;
//       the property is a registered number, so the card's transition on it
//       tweens the colours
//
// Nothing here names a colour. The design owns every anchor; the bench owns
// the numbers — and by claiming six pairs in extents(), obliges every design
// registered in the studio to anchor them, which is the point of the table.
// =============================================================================

var _WB_EXTENTS = [-1, -0.75, -0.5, -0.25, 0, 0.25, 0.5, 0.75, 1];

/** The six rows: a label, the pair's class, and what a cell shows. */
var _WB_EXTENT_ROWS = [
    { label: 'success · ink',    cls: function () { return wb_x_success_ink; },     text: '★★★' },
    { label: 'danger · surface', cls: function () { return wb_x_danger_surface; },  text: '' },
    { label: 'primary · surface',cls: function () { return wb_x_primary_surface; }, text: '' },
    { label: 'primary · ink',    cls: function () { return wb_x_primary_ink; },     text: 'Aa' },
    { label: 'raised · surface', cls: function () { return wb_x_raised_surface; },  text: '' },
    { label: 'warning · edge',   cls: function () { return wb_x_warning_edge; },    text: '' }
];

function _wbExtentHint(branch, text) {
    var root = branch.createElement('root', 'div');
    css.addClass(root, wb_root);
    var hint = branch.createElement('hint', 'div');
    css.addClass(hint, wb_hint);
    hint.textContent = text;
    root.appendChild(hint);
    return root;
}

function mountExtentTable(branch) {
    var root = _wbExtentHint(branch,
        'COLOUR EXTENTS — six words that scale, at nine extents each. A row is one design pair; a cell is an element wearing it with ' +
        'css.extent(el, x) and nothing else. The design owns the three anchors — the meaning turned the other way at −1, neutral at 0, ' +
        'the word itself at 1 — and the sheet interpolates between them, the pole by the sign and then from neutral by the magnitude. ' +
        'Switch the theme: the numbers stay, every colour changes. The table has no empty cell because the completeness test does not let a design leave one.');

    var table = branch.createElement('table', 'div');
    css.addClass(table, wb_extent_table);
    root.appendChild(table);

    // the head row: a blank corner, then the extents
    var corner = branch.createElement('corner', 'div');
    table.appendChild(corner);
    _WB_EXTENTS.forEach(function (x, j) {
        var h = branch.createElement('head' + j, 'div');
        css.addClass(h, wb_extent_head);
        h.textContent = String(x);
        table.appendChild(h);
    });

    _WB_EXTENT_ROWS.forEach(function (row, i) {
        var label = branch.createElement('label' + i, 'div');
        css.addClass(label, wb_extent_label);
        label.textContent = row.label;
        table.appendChild(label);
        _WB_EXTENTS.forEach(function (x, j) {
            var cell = branch.createElement('cell' + i + '_' + j, 'div');
            css.addClass(cell, wb_extent_cell, row.cls());
            cell.textContent = row.text;
            cell.title = row.label + ' at ' + x;
            css.extent(cell, x);                      // the one number the bench sets
            table.appendChild(cell);
        });
    });

    return { root: root, setActive: function (active) {}, partyDeregister: function () {} };
}

function mountExtentCard(branch) {
    var root = _wbExtentHint(branch,
        'ONE NUMBER, THREE WORDS — a card wearing its surface, its edge and its ink at an extent. The slider sets css.extent on the card ' +
        'and the three move in step: elevation from recessed through the page to raised, the edge from all-clear through the hairline to ' +
        'warning, the ink from muted through the body to the accent. The extent is a registered number, so the card’s transition on it ' +
        'tweens the colours — no keyframes, no script.');

    var card = branch.createElement('card', 'div');
    css.addClass(card, wb_extent_card);
    var title = branch.createElement('title', 'div');
    css.addClass(title, wb_extent_card_title);
    title.textContent = 'A card at an extent';
    card.appendChild(title);
    var line = branch.createElement('line', 'div');
    line.textContent = 'Surface, edge and ink follow one number. Drag the slider; the colours arrive over 400 ms.';
    card.appendChild(line);
    root.appendChild(card);

    var slider = branch.createElement('slider', 'div');
    css.addClass(slider, wb_extent_slider);
    var range = branch.createElement('range', 'input');
    range.type = 'range'; range.min = '-1'; range.max = '1'; range.step = '0.01'; range.value = '1';
    range.setAttribute('aria-label', 'extent');
    css.addClass(range, wb_extent_range);
    slider.appendChild(range);
    var readout = branch.createElement('readout', 'span');
    slider.appendChild(readout);
    root.appendChild(slider);

    function apply() {
        var x = Number(range.value);
        css.extent(card, x);
        readout.textContent = 'extent ' + (x >= 0 ? '+' : '') + x.toFixed(2);
    }
    range.addEventListener('input', apply);
    apply();

    // three stops, for a click: the meaning turned the other way, neutral, the word
    var bar = branch.createElement('bar', 'div');
    css.addClass(bar, wb_bar);
    [-1, 0, 1].forEach(function (x, k) {
        var b = branch.createElement('stop' + k, 'button');
        css.addClass(b, wb_btn);
        b.textContent = x === -1 ? '−1  turned the other way' : x === 0 ? '0  neutral' : '+1  the word';
        b.addEventListener('click', function () { range.value = String(x); apply(); });
        bar.appendChild(b);
    });
    root.appendChild(bar);

    return { root: root, setActive: function (active) {}, partyDeregister: function () {} };
}
