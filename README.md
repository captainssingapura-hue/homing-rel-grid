# homing-rel-grid

The Relation Grid — RFC 0050 · Episode 2 — and, on the same doctrine, the Tree View — RFC 0050 ·
Episode 3 — as their own project.

A grid that **captures intents and arranges cells, and holds no value.** A relation is a View
answered on request, columns and a cell manager — one root, never an enumeration:

```
view(intent?)   columns()   cellFor(pk, column)      — and, optionally —
readOnlyColumns()   labels()   headerFor(column)
```

`view()` answers the rows to present now — the whole of a static relation, a window of an
endless one — and the grid holds exactly that: the row axis is the View and has no base.
`view({ by: n })` is the same seam asked to move: keys back, or nothing, and the rows stay.
Membership is the relation's: `cellFor` throws for an identity it does not own, and the grid
asks for every identity before a slot moves, so a stranger refuses a View whole.

The domain owns every cell for its whole life and updates it directly. Edit, commit and update
are the domain's operations; they never change an arrangement, so the grid has no part in them.
What a header says is the relation's too — `labels()`, or a header cell from `headerFor(column)`
placed in the header slot as a cell is placed in a body slot — and when the relation's View
changes underneath, the host **tells** the grid so, and the grid asks `view()` again.

A tree view is the second component built this way: a tree relation answers **places** —
`{ key, depth, fold }`, the tree structure as the binding contract — and manages cells, one per
node; the tree owns the rows, the indent, the caret, the cursor and the keys; fold and unfold are
questions on the ask channel, answered with the whole View or nothing; lazy by default.

```
view()   cellFor(key)          — the tree relation; and on the channel —
RelTreeUnfold(key)  RelTreeFold(key)   →   RelTreeView(places) | nothing
```

## Modules

| module | what |
|---|---|
| `rel-channel` | the ask channel's **core**, the family's: one function the host gave, a notification handed over and never waited on, a question waited on with the person stopped — the mask session with its delay and hold, the panel handle, one at a time. Reaches a presentation only through a six-function surface. Requires nothing. |
| `rel-grid-protocol` | the ask **protocol**: every question and answer declared once as a Java record, its JS class generated at build time — the grid's kinds (selection, copy, the view handover, the group's fold, the view changed) and the tree's (place, view, unfold, fold, cursor changed, activated, view changed). Data only; a domain answers through it without depending on the grid. |
| `rel-grid-selection` | the **selection**: an ordered list of ranges over positions, in a module that imports nothing at all. |
| `rel-grid` | the **grid** — `RelGrid`, the facade, composing: its seam (`RelGridViewMaps`), the layout (`RelGridLayout` over `RelGridSlots`, `RelGridOverlays`, `RelGridReveal`, `RelGridHeaderDrag`), the cells and header-cells registries, the cursor, the gestures, the handover of control, the channel's grid customers, the widths, the window, the merged cells, the stock clipboard writer — one module each, every one under 250 effective lines — and a stock text cell that is deliberately domain-side. Plain JS classes with no runtime dependency on anything; packaged as homing `DomModule`s for this stack. |
| `rel-grid-group` | the **group** — `RelGridGroup`: an ordered list of tables, each an ordinary `RelGrid` that does not know it is in one, with a fence (a slot the domain fills) between every two and around the ends; a header that sticks as a box. Depends on `rel-grid`, never the reverse. |
| `rel-tree` | the **tree view** — `RelTree`, the facade, composing: the places (`RelTreePlaces`), the rows (positions, a branch each, grown at the tail), the cells registry, the cursor, the gestures, the channel's tree customers, the layout with its mask and panel; a stock text cell with a busy ring; the tree's typed SVG (a caret, two folders). Imports the channel core and the protocol, nothing of the grid's table. |
| `rel-grid-workbench` | a solo studio of **benches** that try to make the components fail — Replicating Tables, Han Article, the Endless Table (a window of twenty over a million rows measured through the party), the Games Catalogue (724 releases sorted and filtered from header cells that are the relation's own), and the Games Tree (the same catalogue as a lazy three-level tree, the fold and unfold answered three ways). `GridWorkbenchServer` on 8083. |

## What is here

- **Arrangement over a root relation** — the grid asks `cellFor` once per presentation, places, and on leaving the View detaches and forgets; it never disposes, and its registry is exactly the presented cells. A million-row relation costs the grid a window's worth of slots and cells, measured through the party.
- **A cursor** that is an identity while presented and a position when not; **selection ranges** over positions, never merged, told to the domain; **copy** as a question the domain answers with clipboard content.
- **Shallow and deep, enforced by the grid**: a click or an arrow moves the cursor and the cell is only told; Enter or a double-click hands the cell control until it calls `release()`; the grid never learns commit from cancel.
- **The ask channel**: a question locks and masks the presentation until the answer lands; the domain may draw on the mask's panel; a notification is handed over and never waited on. The view handover, copy, the tree's fold and unfold ride it.
- **Header cells that are the relation's** — sort and filter are the domain's, answered with a View; the grid places the header cell, captures nothing on it but its own resize handle, and is told when the View changed. The Games Catalogue's column menu is the demonstration a provided layer will be cut from.
- **Column widths** held by identity, applied by position, in place; **merged cells**; a header that shows or not and **sticks** or not; a group's header sticks as a box.
- **Row numbers** as a gutter the grid owns — a position each, locked to the left of whatever scrolls the table, a press on one selecting the row; not a column, so nothing addressed by column sees it.
- **The tree view**: places checked at the door and refused whole; the caret and the folders as typed SVG in `currentColor`, so the theme reaches them; ↑↓ Home End PgUp PgDn, → and ← as a tree's, Enter and Space; `selectNode` for a navigator following a URL, the domain opening the path and telling.

Not here, by decision: a **provided** sort-and-filter layer (the Games bench shows the parts; the decorator is the next cut), the tree's window (`view({ by })` on a tree — reserved, answers nothing), multi-select on the tree, and the tree table — the tree's row axis lent to the grid, designed once both stand.

## The one law that is mechanical

`RelGridValueFreeTest` scans every grid module, comments stripped, for every verb by which a
value could cross — `relation.get`, `adapter`, `.update(`, `subscribe`, `updateCell`, `value`,
`getValue`, `columnMeta`, `compare(`, `commit`, `preview`, `effectiveType` — and fails the
build on the first one. `RelTreeValueFreeTest` does the same for the tree, and adds the words a
tree would reach for first: `text(`, `label(`, `icon(`, `find(`, `search`. The node is opaque.

## Two branches

A host that composes a grid or a tree over a domain makes **exactly two** DomOpsParty branches
under its own — `grid` (or `tree`) and `domain` — and neither side ever sees the other's. The
first is handed to the component whole and unactivated: it activates it and mints everything it
makes on it or a sub-branch of it — chrome, slots, rows, overlays, mask, a member grid's own.
`domain` is the host's to divide: a part for the relation's cells, one per fence, one for the
panels a question is answered on; each domain object activates the part it is handed and
dissolves it on dispose. What crosses between the two is an element: the grid asks a cell for
`cellElement()` once and places it in a slot, a header cell for `headerElement()`, a fence for
`fenceElement()`, an editor for `editorElement()`, and a domain hands `mask.panel(element)` what
it drew. The host creates both branches and dissolves both; it activates neither side's own.

## Typed looks

There is no stylesheet in any module. Every class the grid, the group, the tree or a bench
module wears is a `CssClass` record in a `CssGroup` — `RelGridStyles`, `RelGridStockStyles`,
`RelGridGroupStyles`, `RelTreeStyles`, `RelTreeStockStyles`, and one per bench module that draws
— applied through the `css` manager the server injects. The substrate renders one rule per
class, so a state that has to reach every slot beneath it is a custom property the state class
sets on an ancestor and the slot's class reads: `hrg_lit` (the wrapper under `:focus-within`)
sets `--hrg-cursor-color`, `hrg_deep` sets `--hrg-cursor-style`, a group's `hrg_dormant` sets
both cursor and selection transparent, `hrg_ov_ellipsis` publishes `--hrg-text-overflow` for a
text cell to honour; a tree row wears `--hrt-depth` and the sheet turns it into an indent with
`--hrt-indent`. Geometry the layout measures rides `--hrg-left / --hrg-top / --hrg-width /
--hrg-height / --hrg-table-w` on the element; a column's width `--hrg-col-w` on its `<col>`;
the gutter's `--hrg-gutter-w`. Those properties are the components' published vocabulary: a
host or a theme may set them on any ancestor.

**Icons are typed SVG.** The tree's caret and folders are `SvgBeing`s of `RelTreeSvgs`, line
icons in `currentColor`, parsed into a span the tree owns — so a theme colours them through
tokens, which no emoji allows.

### The frame is the host's to take

A component frames its own wrap and lights it while it holds the focus. A host that scrolls the
table wants the light around the **scrollport** — scrollbar inside it — which only the host can
draw, since the scroller is the host's: it wears `hrg_frame` and `hrg_lit` on a non-scrolling
wrapper the port fills and passes `frame: false`, and the grid draws none. The workbench's
`wb_frame` / `wb_port` pair is the pattern.

### The busy ring, and the keyframes it needs — read this before you take `rel-tree`

The tree's stock text cell has `setBusy(on)`: a ring after the text while a domain fetches a
node's children. The ring **turns by a CSS animation** — the class half, `hrt_text_cell_busy`,
is typed and ships with the crate; the `@keyframes` half cannot ride a `CssClass`, because an
at-rule has no home in a class rule and the typed sheet has no primitive for one yet. So the
crate **declares** the movement as raw CSS, `RelTreeStockStyles.KEYFRAMES`, and the
**deployment installs it** in every theme's globals. A deployment that does not gets a ring that
does not turn — silently, since CSS treats a missing keyframes as nothing to play.

The workbench's `WorkbenchFixtures` is the recipe: wrap the starter's `Fixtures`, override
`themeRegistry()`, and append `RelTreeStockStyles.KEYFRAMES` to the Component chunk of every
theme's `ThemeGlobals`. Three dozen lines; `GridWorkbenchBootsTest` checks every theme carries
it. This is a proof of concept for a typed `CssKeyframes` a class would *depend on* — a missing
one caught by the compiler, the movement themeable — which is a wish on the core, recorded in
RFC 0050 · Episode 3-ext1 §10.

## The RFC 0044 ledger

Every module that draws is a `CONSUMER` under the full DOM-owner discipline (a grid or a tree is
a component a widget composes, not a pane the shell is made of — nothing here is a primitive);
the ones that compute — the view maps, the places, the selection, the protocol, the clipboard's
formats, the Games store and conditions — are `PURE_LOGIC` and may touch no DOM at all. Each
crate's conformance test sweeps its modules under the homing rule set and grades them against
`src/test/resources/rfc0044-ledger.txt`, a committed list of the violations that were there
before the sweep began. A finding not in the ledger fails the build; so does a ledger line whose
violation is gone. The ledger only shrinks — and every ledger is empty: the 102 findings the
sweep began with are gone, `rel-tree` and `rel-channel` were begun with none, and any new one
fails the build. To rewrite a ledger — deliberately, never to silence a fresh violation:

```bash
mvn install -Dhoming.conformance.record=true
```

## Build and run

```bash
mvn install
```

```bash
cd rel-grid-workbench && mvn compile exec:java -Dexec.mainClass=hue.captains.singapura.js.homing.relgrid.workbench.GridWorkbenchServer
```

Builds against the released homing core (`homing.core.version` in the root pom, currently
`0.8.2`), so a clean machine needs nothing installed first. To build against local core work:

```bash
mvn -Dhoming.core.version=LOCAL-SNAPSHOT install
```

The design record — the episodes, their extensions, the feature maps and the case studies — is
in the companion studio, `homing-self-studio`, under RFC 0050.
