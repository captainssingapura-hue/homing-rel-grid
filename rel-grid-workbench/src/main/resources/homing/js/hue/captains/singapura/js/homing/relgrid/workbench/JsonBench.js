// =============================================================================
// JsonBench — the JSON Tree bench's two panes, as JS in a file rather than
// lines in a Java string: each widget's body is one line, a call in here.
//
//   mountJsonInput(branch)     → { root, setActive, partyDeregister }
//       a plain textarea over the bench's one document: every keystroke writes
//       the store, which parses; the status says whether it parsed
//   mountJsonDisplay(branch)   → { root, setActive, partyDeregister }
//       the kit's JsonTreeView over the same document, live: one line builds
//       it, one line per change feeds it; a frame round the port, a bar of the
//       view's verbs, a readout of the cursor's pointer and the value under it
//
// branch is the widget's own; each pane mints its chrome on it and hands the
// view a sub-branch, 'json', dissolved with the pane.
// =============================================================================

/** The bench's chrome, the same for both panes: a hint, a bar, and a status line under whatever fills the middle. */
function _wbJsonChrome(branch) {
    var root = branch.createElement('root', 'div');
    css.addClass(root, wb_root);
    var hint = branch.createElement('hint', 'div');
    css.addClass(hint, wb_hint);
    root.appendChild(hint);
    var bar = branch.createElement('bar', 'div');
    css.addClass(bar, wb_bar);
    root.appendChild(bar);
    var seq = 0;
    function btn(label, fn) {
        var x = branch.createElement('btn' + (++seq), 'button');
        css.addClass(x, wb_btn);
        x.textContent = label;
        x.addEventListener('click', fn);
        bar.appendChild(x);
        return x;
    }
    return { root: root, hint: hint, bar: bar, btn: btn };
}

function mountJsonInput(branch) {
    var ui = _wbJsonChrome(branch);
    var text = branch.createElement('text', 'textarea');
    css.addClass(text, wb_json_text);
    text.setAttribute('spellcheck', 'false');
    text.setAttribute('aria-label', 'JSON text');
    ui.root.appendChild(text);
    var status = branch.createElement('status', 'div');
    css.addClass(status, wb_status);
    ui.root.appendChild(status);

    ui.hint.textContent = 'JSON INPUT — a plain textarea over the bench’s one document. Every keystroke writes the store, which parses; the Display pane shows the last value that parsed and keeps it while the text is broken. Type inside a string, add a member, delete a whole array: the tree keeps its folds and its cursor by pointer wherever the node still stands.';

    var store = jsonDocStoreShared();
    text.value = store.text();

    function report() {
        var err = store.error();
        status.textContent = (err ? '✗ ' + err : '✓ parsed') + '   |   revision ' + store.revision() + '   |   ' + store.text().length + ' characters';
    }
    // THE SEAM: the textarea writes the store, live; the store parses and tells its subscribers.
    text.addEventListener('input', function () { store.set(text.value); });
    var unsub = store.subscribe(function () { if (text.value !== store.text()) text.value = store.text(); report(); });

    ui.btn('the sample', function () { store.set(store.sample()); });
    ui.btn('pretty', function () { if (store.error() === null) store.set(JSON.stringify(store.value(), null, 2)); });
    ui.btn('minify', function () { if (store.error() === null) store.set(JSON.stringify(store.value())); });
    ui.btn('a big array (10,000)', function () {
        var a = [];
        for (var i = 0; i < 10000; i++) a.push({ i: i, sq: i * i, even: i % 2 === 0 });
        store.set(JSON.stringify({ rows: a, count: a.length }));
    });
    ui.btn('clear', function () { store.set(''); });
    report();

    return { root: ui.root, setActive: function (active) {}, partyDeregister: function () { unsub(); } };
}

/** A value said in a word for the readout: a string quoted, a container as a count, the rest as it is. */
function _wbJsonSaid(v) {
    if (typeof v === 'string') return JSON.stringify(v);
    if (v !== null && typeof v === 'object') return Array.isArray(v) ? 'array[' + v.length + ']' : 'object{' + Object.keys(v).length + '}';
    return String(v);
}

function mountJsonDisplay(branch) {
    var ui = _wbJsonChrome(branch);
    // The frame goes round the SCROLLPORT: the tree draws none of its own.
    var frame = branch.createElement('frame', 'div');
    css.addClass(frame, wb_frame, hrg_frame, hrg_lit);
    ui.root.appendChild(frame);
    var port = branch.createElement('port', 'div');
    css.addClass(port, wb_port);
    frame.appendChild(port);
    var status = branch.createElement('status', 'div');
    css.addClass(status, wb_status);
    ui.root.appendChild(status);

    ui.hint.textContent = 'JSON TREE — the kit’s viewer over the bench’s one document, live: a JSON value is already an outline, so the document answers the tree’s places by JSON pointer and a cell per node, and the tree does the tree. Every container is closed at first under an open root; →, Space and the caret unfold. Edit in the Input pane and watch: a member typed appears, a member deleted goes, the folds and the cursor stay by pointer.';

    var store = jsonDocStoreShared(), activated = null, note = '';
    // ONE LINE: the view is the tree's host, and makes its two branches under the one it is handed.
    var view = new JsonTreeView({
        container: port,
        branch: branch.createBranch('json'),
        value: store.value(),
        label: 'JSON tree',
        onCursorMoved: function () { report(); },
        onArranged:    function () { report(); },
        onActivated:   function (pointer) { activated = pointer; report(); }
    });
    // ONE LINE per change: the last value that parsed, whenever the text parsed.
    var unsub = store.subscribe(function () { if (store.error() === null) view.set(store.value()); report(); });

    function report() {
        if (!view) return;                                   // the first pass is reported from inside the constructor
        var p = view.cursor(), rows = view.rows();
        status.textContent = rows + ' row' + (rows === 1 ? '' : 's')
                           + '   |   cursor ' + (p === null ? '—' : (p === '' ? '(root)' : p))
                           + (p === null ? '' : '   |   value ' + _wbJsonSaid(view.valueAt(p)))
                           + (activated === null ? '' : '   |   activated ' + activated)
                           + (store.error() === null ? '' : '   |   showing the last value that parsed')
                           + (note ? '   |   ' + note : '');
    }
    function verb(label, fn) { ui.btn(label, function () { fn(); report(); view.focus(); }); }
    verb('open all', function () { view.openAll(); });
    verb('open two deep', function () { view.openAll(1); });
    verb('close all', function () { view.closeAll(); });
    // A navigator's move: a pointer typed, the path opened by the document, the cursor put on it.
    var pointer = branch.createElement('pointer', 'input');
    css.addClass(pointer, wb_input);
    pointer.setAttribute('placeholder', '/releases/0/notes/2');
    pointer.setAttribute('aria-label', 'a JSON pointer to reveal');
    pointer.value = '/releases/0/notes/2';
    ui.bar.appendChild(pointer);
    verb('reveal', function () { note = view.selectPointer(pointer.value) ? '' : 'no such node: ' + pointer.value; });
    report();

    return { root: ui.root, setActive: function (active) {},
             partyDeregister: function () { unsub(); view.destroy(); branch.dissolveBranch('json'); } };
}
