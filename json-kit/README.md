# json-kit — a JSON viewer on the tree view

The first out-of-the-box offering on RFC 0050's tree view: a JSON value on screen, one row per
node, folded lazily, in the theme's colours, in one line of a host's code.

```js
var view = new JsonTreeView({ container: port, branch: branch.createBranch('json'), value: parsed });
view.set(anotherValue);          // later: the folds and the cursor kept wherever the node still stands
```

A JSON value already is an outline — an object's members and an array's elements are a node's
children, in the order the value holds them — so the kit builds nothing structural. A
**document** answers the tree's two questions from the value in hand: the *places* to present
now, keyed by JSON pointer, and a *cell* per node. The tree does the tree.

This is **domain-side code that ships**, as the tree's stock text cell does: it holds a value,
which a tree never may. It is not value-free, on purpose.

## Depend on it

```xml
<dependency>
    <groupId>io.github.captainssingapura-hue.homing.ssjs</groupId>
    <artifactId>json-kit</artifactId>          <!-- → rel-tree → rel-channel, rel-grid-protocol -->
    <version>LOCAL-SNAPSHOT</version>
</dependency>
```

Crate: add `JsonKitCrate.INSTANCE` to your crate's `requires()`; it requires `RelTreeCrate` and
`RelGridProtocolCrate`. In a widget, import the view: `new ModuleImports<>(List.of(new
JsonTreeViewModule.JsonTreeView()), JsonTreeViewModule.INSTANCE)`.

## A widget, whole

```java
@Override
protected List<String> constructBodyJs() {
    return List.of(
        "    var root = branch.createElement('root', 'div');",
        "    var port = branch.createElement('port', 'div');       // the scrolling element — yours",
        "    root.appendChild(port);",
        "    var view = new JsonTreeView({",
        "        container: port,",
        "        branch: branch.createBranch('json'),              // the view's OWN branch, handed unactivated",
        "        value: JSON.parse(text),                          // any JSON value, or undefined for nothing yet",
        "        label: 'Settings',",
        "        onActivated: function (pointer) { /* Enter or a double-click reached the node */ }",
        "    });",
        "    return { root: root, setActive: function () {},",
        "             partyDeregister: function () { view.destroy(); branch.dissolveBranch('json'); } };"
    );
}
```

The view is the tree's **host** and keeps the host's discipline: under the branch it is handed it
makes exactly two — `tree`, handed whole to the `RelTree`, and `document`, for the cells — and
mints nothing of its own. `destroy()` takes the tree and the document down and dissolves the
two; the branch you handed it is yours to dissolve. Everything the view puts on screen is inside
`container`; it draws no frame — a host that wants the tree lit while it holds the focus wears
`hrg_frame` and `hrg_lit` (from `rel-grid`'s `RelGridStyles`) on a non-scrolling wrapper the
port fills, or draws its own.

## Options

| option | default | what |
|---|---|---|
| `container` | — | where the tree mounts; your scrolling element |
| `branch` | — | the view's own DomOpsParty branch, handed unactivated |
| `value` | `undefined` | the JSON value, already parsed. A scalar, `null` or an empty container is one leaf row |
| `title` | `"$"` | what the root row is called |
| `openDepth` | `1` | containers open at first, by depth: `0` none — the root closed; `1` the root; `2` the root and its containers; … |
| `maxString` | `80` | a string is printed up to this many characters, then `…"` |
| `label` | — | `aria-label` on the tree |
| `onActivated(pointer)` | — | Enter, or a double-click, reached the node |
| `onCursorMoved(pointer)` | — | the cursor moved |
| `onArranged(kind)` | — | after every presentation pass — a fold, a `set`, a reveal |

## Verbs

| verb | what |
|---|---|
| `set(value)` | a new value. The folds are kept wherever a container still stands at the pointer; the cursor is kept by pointer; a cell whose pointer still resolves is repainted in place, one whose pointer is gone is disposed. The root, if it was open and briefly stops being a container (you typed `7`), is open again when it is one |
| `value()` / `valueAt(pointer)` | the value, whole or at a pointer — `undefined` for a pointer to nothing |
| `open(pointer)` / `close(pointer)` | unfold or fold a container. A leaf, an empty container or a stranger: nothing |
| `openAll(depth?)` / `closeAll()` | every container down to a depth (the root is `0`); no depth means all of them. `closeAll` leaves the root row alone, closed |
| `selectPointer(pointer)` | a navigator's move: the ancestors opened, the tree told, the cursor put on the node and revealed. `false` for a pointer to nothing, and nothing moves |
| `cursor()` | the pointer under the cursor, or `null` |
| `rows()` | how many rows are presented |
| `focus()` / `el()` | the keyboard host, without scrolling; the tree's element |
| `destroy()` | see above |

The surface is pinned by `JsonTreeViewContract` — a conformance test holds the JS class's
prototype to exactly that list.

## Pointers

A node's key is its JSON pointer (RFC 6901): `""` the root, `/name` a member, `/list/0` an
element, `/odd~1key/~0tilde` the member `"~tilde"` of `"odd/key"` — `~` escaped as `~0`, `/` as
`~1`. Every verb that takes a pointer takes it in that form; every callback reports one. A
pointer is a string you never parse: what a node *is* — the value's kind — is `valueAt`.

Two things the platform decides, not the kit: an object's members come in the order
`JSON.parse` gave them — integer-like keys first, ascending, then document order — and an
array index is an element's position, so after `set` with an element removed every later
element has a new pointer, and the cursor — kept by pointer — stands on whichever element moved
into it.

## Keys, and what a press means

The tree owns the keyboard once it holds the focus (under the workspace shell, the first click
into a pane focuses the pane and the second lands in the tree):

| key | |
|---|---|
| ↑ ↓ | the row before, the row after |
| → | closed: unfold · open: to the first child · leaf: nothing |
| ← | open: fold · closed or leaf: to the parent |
| Space | closed: unfold · open: fold |
| Enter | activate — `onActivated(pointer)` |
| Home End PgUp PgDn | the first row, the last, a page |

A press on a row lands the cursor; a press on the caret lands it and folds or unfolds; a
double-click lands it and activates. An unfold or a fold is a question on the tree's ask channel
that the document answers at once, from the value in hand, so the tree never shows its mask.

## What a row looks like, and how a theme reaches it

A row is the tree's — the indent from `--hrt-depth`, the caret as typed SVG in `currentColor` —
and the cell is the kit's: three spans, the name, the colon and the value, each wearing a typed
class from `JsonTreeStyles`:

| class | on |
|---|---|
| `jk_cell` / `jk_cell_current` | the line; the current row's line, weighted |
| `jk_key` / `jk_index` | a member's name; an element's index, muted |
| `jk_punct` | the colon |
| `jk_string` | `"…"`, cut at `maxString` |
| `jk_number` | as it is |
| `jk_literal` | `true`, `false`, `null` |
| `jk_container` | `{3}`, `[5]`, `{}`, `[]` — a count, never the contents |

Every class **wears** a design's word (RFC 0065) and names no colour of its own: the line is
the code face at the code's size, a name and a number are the body ink, a position, a colon
and a count the muted ink, a string the primary ink, a literal the kicker's. There is no
stylesheet and no literal anywhere in the kit, so a design colours every kind without knowing
JSON exists, and a host that wants strings green has the class names to address.

## The lower layers, for a host that composes its own tree

The view is composition; the two things beneath it are exported too.

- **`createJsonDocument(value, { branch, title?, openDepth?, maxString? })`** — the document,
  answering `TreeRelationContract` (`view()`, `cellFor(pointer)`) plus `answer(question, mask)`
  for the channel and the fold verbs. Hand it to a `RelTree` as its `relation` and wire
  `ask: function (q, m) { return document.answer(q, m); }` — that is all the view does. Then
  `document.set(value)` followed by `tree.tell(new RelTreeViewChanged())` is the view's `set`.
- **`JsonNodeCell({ branch, name, index?, value, maxString? })`** — the cell, answering
  `RelTreeCellContract`; `set({ name?, value })` repaints; `text()` is the line as printed.

## Limits, on record

- **No window.** The tree presents every row a fold reveals; a container of ten thousand costs
  nothing closed and ten thousand rows open (about 300 ms in the bench). A large array as
  chunked pseudo-nodes — `[0…999]` — is the document's to add later; the tree would not change.
- **One cursor, no selection, no editing.** A tree cell is never entered. A viewer, not an editor.
- **Not value-free.** The kit holds the value; the tree it is on never sees it. The
  `RelTreeValueFreeTest` guards the tree, not the kit.

## See it

`rel-grid-workbench`, kind **JSON Tree**: an Input pane (a textarea that parses on every
keystroke) beside a Display pane (this view, live). Type inside a string, add a member, delete an
array, break the text — and watch what stays. `GridWorkbenchIntroDoc` has the walk-through.
