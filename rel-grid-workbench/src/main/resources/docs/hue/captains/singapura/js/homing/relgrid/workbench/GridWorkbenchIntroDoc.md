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
| Nutritionist | calories, **stars** | only a nutritionist may say |
| Shop manager | price | only the shop may say |
| Follower | nothing | a replica |
| *anyone* | never `sold`, never `popularity` | `sold` moves only by selling; `popularity` is **derived** from it |

**`stars` is a health rating, 1 to 5, and it is not a text cell.** It shows ★★★★☆ and edits
with a **dropdown**, committing when the selection *changes* rather than on Enter. It is the
bench's own cell — written against the cell contract, shipped with nothing — and it is here
because until it existed the stock text cell was the only implementation, so the contract had
only ever been proved against the one thing it was written for.

### What to try

- In the **Nutritionist**, click or arrow to a *calories* cell — that is **shallow**, the grid's
  cursor. Press **Enter** or double-click to go **deep**: the grid hands the cell control. Type,
  then **Enter** to commit or **Escape** to cancel; the cell hands control back. Every other
  table updates.
- In the same table, open a **stars** cell. What appears is not a text box, and not a form
  element at all: a **panel three times wider and four times taller than the cell**, hung off it.
  Press **→** for one more star and **←** for one fewer; **↑** and **↓** are dead on purpose,
  because a rating has one axis. **Enter** commits, **Escape** cancels, and clicking a star does
  both at once. Nothing is written until you commit.
- While that panel is open, look at what did *not* happen: no column moved, no row grew, and the
  cursor stayed where it was. The editor is not in the table at all — the grid mints an anchor
  over the cell, outside the table, and the cell hangs whatever it likes off it. That is why a
  panel this size costs the arrangement nothing, and why those arrow keys never reach the grid.
  Watch the other tables as well: they are text-cell tables, and the rating still moves in all of
  them, because what travels is the store's value and not the editor.
- Now do the same on a *price* cell in the Nutritionist. Nothing opens — the grid asked the cell,
  the cell said no, the grid stayed shallow. Do it in the **Shop manager** and it opens.
- Try it on *sold* or *popularity* in any table. Nothing opens there either, but for the other
  reason: the relation declares those columns read-only, so the grid **never asks at all** and
  no cell is consulted. Structure refuses always; a cell refuses sometimes; neither stands in
  for the other, which is why the bench shows both.
- Press **a day of trade** in the Shop manager. Sales move `sold`, and `popularity` is
  re-derived for **every** dish — each dish's sales as a share of the best seller's — so one
  dish selling well lowers everyone else's. Nobody edited a popularity cell, and nobody can:
  the store refuses a commit to `sold` or `popularity` whichever relation asks.
- **Reload the page.** Edits and sales are persisted; `popularity` is recomputed, not stored.
- **Select a range.** Press and drag across the cells, or shift-click, or hold **Shift** and use
  the arrows. **Ctrl**-click or ctrl-drag adds another range beside the first, and **Ctrl+A**
  takes the whole table. A bare click or arrow starts over. Watch the cursor while you extend:
  it does not move, because a selection reaches further and the cursor is not what reaches.
  With nothing selected the selection *is* the cursor's own cell, which is why one cell is
  always tinted. Copy is the one thing that consumes it so far; bulk editing is a later round.
- **Copy it.** Press **Ctrl+C** in any table — the Follower too, since reading is not editing.
  The table dims and a **panel** appears over it: three formats, a header toggle, a live
  preview, and Cancel. **T**, **C** or **H** choose; **← →** move between them; **Enter**
  takes the one shown; **Esc** cancels. While the panel is up, try an arrow key or a click on
  the table: nothing. The grid has asked a question and is **waiting on the answer**, and
  until it has one the person is stopped — not slowed, not queued — because an answer
  describes the state it was asked about, and a cursor that moved in the meantime would be
  answered about the wrong cells. Choose **HTML** and paste into a spreadsheet or a mail: the
  rating arrives as **stars**, because that is what you saw. Choose **TSV** and it arrives as
  a number, because that is what a spreadsheet can add up.
- Look at what the panel is. Its **box** is the grid's — a golden rectangle, sized to what you
  can see of the table's host and centred on it — and its **content** is the bench's, drawn
  by the same relation that owns the cells. The grid handed over identities (which dishes,
  which columns) and got back finished text; it composed nothing, read nothing, and wrote
  what it was given. The readout under the table says what was chosen and how much was
  written; the first half is the bench remembering, the second half is the grid reporting.
- Drag a header's right edge, or press **Alt+←/→** on a column. Widths are the grid's own
  geometry — held by column, applied in place — and are not remembered, because remembering is
  the domain's half.
- Press **re-arrange** on any table: the slots are rebuilt and the same cells are placed again.
  Nothing is re-created.

### Two ways to say no, and both are here

RFC 0050 · Episode 2 gives the grid two ways to refuse (map 15, law 105), and this bench shows
each of them working beside the other.

A **declared constraint** is structural — a column read-only by design — and the grid **never
asks**: no cell is consulted, no call is made (map 16, law 112). The relation declares
`readOnlyColumns()` once, at construction, and `sold` and `popularity` are in it. Their cells
are perfectly willing, and are never given the chance to say so.

A **cell that declines** is situational — *this* person may not, *this* value is derived — and
is decided afresh each time it is asked (law 113). The relation's cell manager hands a commit
target only to the cells its role may write; a cell without one answers no. The grid asks and
accepts the answer, and cannot tell the editors apart, because nothing in its construction
carries the role.

Neither substitutes for the other. A constraint cannot express "not today"; a cell's judgement
cannot loosen a constraint, because a constrained cell is never asked. That is why the offer is
made in **two stages** — and why the first stage is public: bulk edit and paste have to know
whether a cell is writable *without opening it*, and a single take-it-or-leave-it call could not
answer that, since asking would be taking.

A cell that cannot edit marks its host `hrg-text-ro` and is painted muted, because an
uneditable cell has no resting affordance to be missing — a person would otherwise discover it
by pressing Enter and watching nothing happen. That is map 16's case for naming the property
(law 116), and the stock cell does the naming since only the cell knows.

### What just happened, and what did not

Shallow is the grid's; deep is entered only through the grid, by Enter or a double-click, and the
cell owns the keyboard until it is finished. What it hands the grid is a **promise**, not a
callback the grid handed out — so the grid exposes nothing a cell could store, fire twice, or
fire after the grid is gone, and "finished" settles exactly once because that is what a promise
does. Resolved or rejected makes no difference: an edit that blew up still ended. The grid never
learns whether it committed or cancelled — only that it ended.

The path of an edit is **cell → store → every relation → each relation's own cells**, and the
path of a sale is **store → every relation → each relation's own cells**. The grid is on
neither. It was handed a relation at construction — `pks()`, `columns()`,
`cellFor(pk, column)` — and has not been spoken to since. There is no `subscribe`, no
`updateCell`, no commit callback, no list of editable columns, and the grid's own build fails if
any of its modules so much as mentions a value.

The readout under each table is **domain state** — the store's revision, the relation's cell
count and what it may edit. It reads nothing from the grid either.

### The first question the grid waits for

Selection notifications were the channel's first customer, and they were the cheap kind: the
grid told the domain and did not wait. Copy is the other kind. The grid asks
`RelGridCopyRequested` — the selection resolved to identities, one block per range — and it
**owes the person nothing until the domain answers**, so it masks the table and refuses every
intent in the meantime (ext6, laws 220–221). The domain may ask the grid for the mask's
**panel** and draw on it; here it draws the three formats. The answer is a
`RelGridClipboardContent` — text, and an html when there is one — or nothing, for Cancel.

The grid writes the answer through the async Clipboard API under the activation the person's
click gave it, and when a page is denied that API by policy — an embedded page, say — through
the copy command instead, which is gated on activation rather than policy. Both roads carry
both forms. That is why copy is no longer the channel's synchronous exception: nothing here
needs the browser's copy event, and everything here needs a panel.

The panel's **geometry** is a rule, not a guess: a golden rectangle — width φ times height —
no wider than the seen area over φ and no taller than its height over φ, whichever binds,
centred, with a floor so a small table still gets a panel that can hold something and a
ceiling so a vast one does not get a page. The seen area is the host's box, clipped to the
window; a table three rows tall in a host with room below gets its panel in the middle of the
host, not overflowing the table.

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
