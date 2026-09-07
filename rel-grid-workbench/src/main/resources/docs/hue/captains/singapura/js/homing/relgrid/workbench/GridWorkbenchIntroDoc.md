# The Grid Workbenches

A studio of its own, for benching the Relation Grid.

---

## What a bench is

> A **bench** is a workspace of grid **specimens** that a person docks side by side, and it
> exists to ask **one question** a demo cannot answer.

A demo shows the grid working; a bench tries to make it fail. Each specimen is a **control** for
the others, and docking two side by side is the whole point — which is why specimens are not
pinned: two copies of the *same* specimen, differently arranged, is a comparison worth having.

## Replicating Tables

The first bench on the episode-2 grid, and the proof of its root principle: the grid captures
intents and arranges cells, and **holds no value**.

Open **Benches** below, then the picker with **➕**. Dock the **Dish editor** and two or three
**Dish followers** side by side.

### What to try

- In the editor, click or arrow to a cell — that is **shallow**, the grid's cursor. Press
  **Enter** or double-click to go **deep**: the grid hands the cell control. Type, then **Enter**
  to commit or **Escape** to cancel; the cell hands control back. Every follower updates.
- Press **bump every popularity** — a domain push, six commits at once.
- **Reload the page.** The store is persisted; every table comes back current.
- Drag a header's right edge, or press **Alt+←/→** on a column. Widths are the grid's own
  geometry — held by column, applied in place — and are not remembered, because remembering is
  the domain's half.
- Press **re-arrange** on any table: the slots are rebuilt and the same cells are placed again.
  Nothing is re-created.

### What just happened, and what did not

Shallow is the grid's; deep is entered only through the grid, by Enter or a double-click, and the
cell owns the keyboard until it releases. The grid never learns whether it committed or
cancelled — only that it ended.

The path of an edit is **cell → store → every relation → each relation's own cells**. The grid
is not on it. It was handed a relation at construction — `pks()`, `columns()`,
`cellFor(pk, column)` — and has not been spoken to since. There is no `subscribe`, no
`updateCell`, no commit callback, and the grid's own build fails if any of its modules so much
as mentions a value.

The readout under each table is **domain state** — the store's revision and the relation's cell
count. It reads nothing from the grid either.

### Entering a pane

A workspace pane is **inert until you enter it**: click the pane, press **Enter** once to enter
it, then click a cell. Without that first Enter the keystrokes stay with the workspace and the
grid looks unresponsive, which is not a defect any bench is about. Double-click needs no pane
Enter at all.

## Adding a bench

1. Write the specimens as widgets, beside the ones in `relgrid.workbench`.
2. Declare a `WorkspaceSpec` with a new `kind()`, listing those widgets as `WidgetEntry`s —
   grouped and described. `ReplicatingTablesSpec` is the worked example.
3. Add the spec to `GridWorkbenchStudio.BENCHES`. That is the whole registration: naming the
   singleton runs its static initializer, and nothing else needs editing.

**There is no leaf per bench, deliberately.** The workspace chrome serialises the whole registry
to the client, so the workspace controls already offer cross-kind switching between every
registered bench. A catalogue tile each would restate a list the substrate carries.
