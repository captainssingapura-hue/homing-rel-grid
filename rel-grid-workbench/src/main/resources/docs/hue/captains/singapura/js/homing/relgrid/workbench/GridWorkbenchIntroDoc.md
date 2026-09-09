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

Open **Benches** below, then the picker with **➕**. Dock the three editors — **Chef**,
**Nutritionist**, **Shop manager** — and a **Follower** or two, side by side. Every one of them
is the same grid, constructed the same way, over the same persisted store. What differs is the
**relation** each was handed, and a relation is built for a role.

| table | may edit | because |
|---|---|---|
| Chef | ingredient, style | the kitchen's to say |
| Nutritionist | calories | only a nutritionist may say |
| Shop manager | price | only the shop may say |
| Follower | nothing | a replica |
| *anyone* | never `sold`, never `popularity` | `sold` moves only by selling; `popularity` is **derived** from it |

### What to try

- In the **Nutritionist**, click or arrow to a *calories* cell — that is **shallow**, the grid's
  cursor. Press **Enter** or double-click to go **deep**: the grid hands the cell control. Type,
  then **Enter** to commit or **Escape** to cancel; the cell hands control back. Every other
  table updates.
- Now do the same on a *price* cell in the Nutritionist. Nothing opens. The grid asked the cell;
  the cell said no; the grid stayed shallow. Do it in the **Shop manager** and it opens.
- Press **a day of trade** in the Shop manager. Sales move `sold`, and `popularity` is
  re-derived for **every** dish — each dish's sales as a share of the best seller's — so one
  dish selling well lowers everyone else's. Nobody edited a popularity cell, and nobody can:
  the store refuses a commit to `sold` or `popularity` whichever relation asks.
- **Reload the page.** Edits and sales are persisted; `popularity` is recomputed, not stored.
- Drag a header's right edge, or press **Alt+←/→** on a column. Widths are the grid's own
  geometry — held by column, applied in place — and are not remembered, because remembering is
  the domain's half.
- Press **re-arrange** on any table: the slots are rebuilt and the same cells are placed again.
  Nothing is re-created.

### Two ways to say no, and which one this is

RFC 0050 · Episode 2 gives the grid two ways to refuse an edit (map 15, law 105). A **declared
constraint** is structural — a column read-only by design — and the grid never asks
(map 16). A **cell that declines** is situational — *this* person may not, *this* value is
derived — and is decided each time it is asked (law 113). Neither substitutes for the other.

This bench is the second kind, and it needed **no change to the grid**. The relation's cell
manager hands a commit target only to the cells its role may write; a cell without one declines
`beginEdit`, and the grid, having asked, accepts the answer. What nobody may write at all is the
store's rule, one layer down, and the store refuses it regardless of who asks. The grid cannot
tell the editors apart, because nothing in its construction carries the role.

The declared constraint — read-only by structure, the grid not even asking — is the half not
built in round 1.

A cell that cannot edit marks its host `hrg-text-ro` and is painted muted, because an
uneditable cell has no resting affordance to be missing — a person would otherwise discover it
by pressing Enter and watching nothing happen. That is map 16's case for naming the property
(law 116), and the stock cell does the naming since only the cell knows.

### What just happened, and what did not

Shallow is the grid's; deep is entered only through the grid, by Enter or a double-click, and the
cell owns the keyboard until it releases. The grid never learns whether it committed or
cancelled — only that it ended.

The path of an edit is **cell → store → every relation → each relation's own cells**, and the
path of a sale is **store → every relation → each relation's own cells**. The grid is on
neither. It was handed a relation at construction — `pks()`, `columns()`,
`cellFor(pk, column)` — and has not been spoken to since. There is no `subscribe`, no
`updateCell`, no commit callback, no list of editable columns, and the grid's own build fails if
any of its modules so much as mentions a value.

The readout under each table is **domain state** — the store's revision, the relation's cell
count and what it may edit. It reads nothing from the grid either.

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
