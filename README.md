# homing-rel-grid

The Relation Grid — RFC 0050 · Episode 2 — as its own project.

A grid that **captures intents and arranges cells, and holds no value.** A relation is
identities, columns and a cell manager:

```
pks()   columns()   cellFor(pk, column)
```

The domain owns every cell for its whole life and updates it directly. Edit, commit and update
are the domain's operations; they never change an arrangement, so the grid has no part in them.

## Modules

| module | what |
|---|---|
| `rel-grid` | the primitive — `RelGrid`, its seam (`RelGridViewMaps`), layout, cells registry, header drag, and a stock cell that is deliberately domain-side. Plain JS classes with no runtime dependency on anything; packaged as homing `DomModule`s for this stack. |
| `rel-grid-workbench` | a solo studio of benches that try to make the grid fail. `GridWorkbenchServer` on 8083. |

## What is here, round 1

- arrangement over an immutable root relation — the grid asks `cellFor` once per identity and only places and detaches after; it never disposes
- a cursor that is an identity while presented and a position when not
- **shallow and deep, enforced by the grid**: a click or an arrow moves the cursor and the cell is only told; Enter or a double-click hands the cell control until it calls `release()`; the grid never learns commit from cancel
- column widths — held by identity, applied by position, in place; bounded at normalisation; snapshot and restore; persisted by nobody here

Not here, by decision: sort and filter (the relation's, answered with a View), selection ranges, and remembering widths — later rounds and the domain's half.

## The one law that is mechanical

`RelGridValueFreeTest` scans every grid module, comments stripped, for every verb by which a
value could cross — `relation.get`, `adapter`, `.update(`, `subscribe`, `updateCell`, `value`,
`getValue`, `columnMeta`, `compare(`, `commit`, `preview`, `effectiveType` — and fails the
build on the first one.

## Build and run

```bash
mvn install
```

```bash
cd rel-grid-workbench && mvn compile exec:java -Dexec.mainClass=hue.captains.singapura.js.homing.relgrid.workbench.GridWorkbenchServer
```

Builds against the released homing core (`homing.core.version` in the root pom, currently
`0.8.1`), so a clean machine needs nothing installed first. To build against local core work:

```bash
mvn -Dhoming.core.version=LOCAL-SNAPSHOT install
```
