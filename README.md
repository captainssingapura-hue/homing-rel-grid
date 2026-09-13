# homing-rel-grid

The Relation Grid — RFC 0050 · Episode 2 — as its own project.

A grid that **captures intents and arranges cells, and holds no value.** A relation is
a View answered on request, columns and a cell manager — one root, never an enumeration:

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

## Modules

| module | what |
|---|---|
| `rel-grid` | the grid — `RelGrid`, the facade, composing: its seam (`RelGridViewMaps`), the layout (`RelGridLayout` over `RelGridSlots`, `RelGridOverlays`, `RelGridReveal`, `RelGridHeaderDrag`), the cells registry, the cursor, the gestures, the handover of control, the ask channel, the widths, the window (the relation's seam asked to move), the header cells' registry, the merged cells, the stock clipboard writer — one module each, every one under 250 effective lines — and a stock cell that is deliberately domain-side. Plain JS classes with no runtime dependency on anything; packaged as homing `DomModule`s for this stack. |
| `rel-grid-group` | the group — `RelGridGroup`: an ordered list of tables, each an ordinary `RelGrid` that does not know it is in one, with a fence (a slot the domain fills) between every two and around the ends. Its minting (`RelGridGroupMint`) and its one cursor (`RelGridGroupWalk`) are modules of their own. Shares column geometry through the members' public verbs; depends on `rel-grid`, never the reverse. |
| `rel-grid-workbench` | a solo studio of benches that try to make the grid fail — Replicating Tables, Han Article, the Endless Table (a window of twenty over a million rows measured through the party), and the Games Catalogue (724 releases sorted and filtered from header cells that are the relation's own). `GridWorkbenchServer` on 8083. |

## What is here, round 1

- arrangement over a root relation — the grid asks `cellFor` once per presentation, places, and on leaving the View detaches and forgets; it never disposes, and its registry is exactly the presented cells
- a cursor that is an identity while presented and a position when not
- **shallow and deep, enforced by the grid**: a click or an arrow moves the cursor and the cell is only told; Enter or a double-click hands the cell control until it calls `release()`; the grid never learns commit from cancel
- column widths — held by identity, applied by position, in place; bounded at normalisation; snapshot and restore; persisted by nobody here
- a header that shows or not, and **sticks** or not (map 12: grid chrome, the host's, read once) — on the header cells, with the cursor revealed clear of it; a group's header sticks as a box and hands its height to every member as the band its cursor clears

Not here, by decision: sort and filter (the relation's, answered with a View), selection ranges, and remembering widths — later rounds and the domain's half.

## The one law that is mechanical

`RelGridValueFreeTest` scans every grid module, comments stripped, for every verb by which a
value could cross — `relation.get`, `adapter`, `.update(`, `subscribe`, `updateCell`, `value`,
`getValue`, `columnMeta`, `compare(`, `commit`, `preview`, `effectiveType` — and fails the
build on the first one.

## Two branches

A host that composes a grid over a domain makes **exactly two** DomOpsParty branches under its
own — `grid` and `domain` — and neither side ever sees the other's. `grid` is handed to the
grid (or the group) whole and unactivated: the grid activates it and mints everything it makes
on it or a sub-branch of it — chrome, slots, overlays, mask, a member grid's own. `domain` is
the host's to divide: a part for the relation's cells, one per fence, one for the panels a
question is answered on; each domain object activates the part it is handed and dissolves it
on dispose. What crosses between the two is an element: the grid asks a cell for
`cellElement()` once and places it in a slot, a fence for `fenceElement()`, an editor for
`editorElement()`, and a domain hands `mask.panel(element)` what it drew. The host creates
both branches and dissolves both; it activates neither side's own.

## Typed looks

There is no stylesheet in any module. Every class the grid, the group or a bench module wears is
a `CssClass` record in a `CssGroup` — `RelGridStyles`, `RelGridStockStyles`, `RelGridGroupStyles`,
and one per bench module that draws — applied through the `css` manager the server injects. The
substrate renders one rule per class, so a state that has to reach every slot beneath it is a
custom property the state class sets on an ancestor and the slot's class reads: `hrg_lit`
(the wrapper under `:focus-within`) sets `--hrg-cursor-color`, `hrg_deep` sets
`--hrg-cursor-style`, a group's `hrg_dormant` sets both cursor and selection transparent,
`hrg_ov_ellipsis` publishes `--hrg-text-overflow` for a text cell to honour. Geometry the
layout measures rides `--hrg-left / --hrg-top / --hrg-width / --hrg-height / --hrg-table-w`
on the element; a column's width `--hrg-col-w` on its `<col>`. Those properties are the
grid's published vocabulary: a host or a theme may set them on any ancestor.

## The RFC 0044 ledger

Every module that draws is a `CONSUMER` under the full DOM-owner discipline (the grid is a
component a widget composes, not a pane the shell is made of — nothing here is a primitive);
the ones that compute — the view maps, the selection, the protocol, the clipboard's
formats — are `PURE_LOGIC` and may touch no DOM at all. Each crate's conformance test
sweeps its modules under the homing rule set and grades them against
`src/test/resources/rfc0044-ledger.txt`, a committed list of the violations that were there
before the sweep began. A finding not in the ledger fails the build; so does a ledger line
whose violation is gone. The ledger only shrinks — and every ledger is empty now: the 102
findings the sweep began with are gone, and any new one fails the build. To rewrite a
ledger — deliberately, never to silence a fresh violation:

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
