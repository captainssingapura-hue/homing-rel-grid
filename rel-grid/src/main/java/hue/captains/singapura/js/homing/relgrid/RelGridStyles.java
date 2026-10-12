package hue.captains.singapura.js.homing.relgrid;

import hue.captains.singapura.js.homing.core.CssClass;
import hue.captains.singapura.js.homing.core.util.CssClassName;
import hue.captains.singapura.js.homing.core.CssGroup;

import java.util.List;

import static hue.captains.singapura.js.homing.design.Target.*;

/**
 * RFC 0050 · Episode 2 — the grid's MECHANICS and its FACTS, typed; never its looks. Every visual the grid shows
 * is the design's it is made with (Appendix: The Frame): the grid asks the design to wear a leaf of the frame
 * on each element it makes, and never learns a class.
 *
 * <p>What stays here is what no style could want otherwise: positioning, stickiness, stacking order, the width
 * variables, {@code user-select}, the resize pointer, the clipboard's off-screen scratch. And the grid's FACTS -
 * {@code hrg_cursor}, {@code hrg_sel}, {@code hrg_deep}, {@code hrg_masked}, a merged group's lead and covered
 * slots - as predicates with no body: a fact about an element, asserted by the grid and tested without looking
 * (ext3), never a style hook. The facts a design shows are reflected besides, where core's states read them:
 * {@code aria-selected}, {@code aria-current}.</p>
 *
 * <p>Geometry the layout measures - an overlay's box, the panel's, a merged cell's, the table's least width -
 * rides {@code --hrg-left / --hrg-top / --hrg-width / --hrg-height / --hrg-table-w} on the element, never an
 * inline style. A column's width rides {@code --hrg-col-w} on its {@code <col>}.</p>
 *
 * <p>Two classes outlive the grid's own use of them: {@code hrg_frame} and {@code hrg_lit}, and the colours they
 * are drawn in. The grid wears neither - its edge is its design's - but the tree's hosts and the group's fence
 * still do, until each has a frame of its own.</p>
 */
public record RelGridStyles() implements CssGroup<RelGridStyles> {

    public static final RelGridStyles INSTANCE = new RelGridStyles();

    // ── what the channels carry — the ring, the selection, the lattice — by name, for anything that draws from a channel.
    //    Each takes the ink the grid stands on, until the grid wears the house's look: a ring in the ink, a lattice
    //    and a selection washed with it - seen in every theme, naming no colour. ──

    public static final String RING_COLOUR  = "currentColor";
    public static final String RING_BORDER  = "currentColor";
    public static final String RING_WIDTH   = "2px";
    public static final String RING_STYLE   = "solid";
    public static final String RING_OFFSET  = "-2px";
    public static final String RING_GLOW    = "0 0 0 1px color-mix(in srgb, currentColor 20%, transparent)";
    public static final String LATTICE_LINE = "color-mix(in srgb, currentColor 18%, transparent)";
    public static final String SEL_SURFACE  = "color-mix(in srgb, currentColor 12%, transparent)";
    public static final String SEL_INK      = "inherit";

    // ── the wrapper: the positioned parent, and the frame that catches the focus ──

    /** container > WRAPPER > table: the positioned parent for anything that is not a cell. */
    public record hrg_wrap() implements CssClass<RelGridStyles> {
        @Override public String body() { return """
                position: relative;
                """;
        }
    }

    /**
     * A STOPGAP, NOT THE GRID'S: the grid wears none, its edge is its design's; the tree's hosts still frame a
     * tree with it, until the tree has a frame of its own. LIT, NOT LIFTED. A frame drawn OVER the table, taking no pointer, under
     * the editor (50) and the mask (60). At rest a hairline in the lattice's
     * colour, inset; lit, whatever {@code --hrg-frame-shadow} says. The
     * hairline is INSET — it lives on the edge and covers nothing — and any
     * glow is the design's, outward: an inner glow of any width sits over the
     * first characters of the first column, which is the one place a frame
     * must not be. A host that mounts the grid in a scrollport clips the
     * outward glow on the edges it clips, and loses nothing it could read.
     */
    public record hrg_frame() implements CssClass<RelGridStyles> {
        @Override public String pseudoState() { return "::after"; }
        @Override public String body() { return """
                content: "";
                position: absolute;
                left: 0; top: 0; right: 0; bottom: 0;
                pointer-events: none;
                z-index: 40;
                box-shadow: var(--hrg-frame-shadow, inset 0 0 0 1px %s);
                """.formatted(LATTICE_LINE);
        }
    }

    /**
     * A STOPGAP, NOT THE GRID'S: worn by the tree's hosts and the group's fence, until each has a frame of its own.
     * The grid that holds the focus is visibly the grid that holds the focus,
     * and it says so with the design's ring: the frame's hairline in the
     * ring's colour under the ring's glow, and a cursor at full strength.
     * Focus is {@code :focus-within} — the browser's own fact — so the
     * editor's overlay and the mask's panel count as the grid holding it.
     * Worn by the wrapper, by a host framing its own scrollport, and by a
     * group's fence when it is the stop.
     */
    public record hrg_lit() implements CssClass<RelGridStyles> {
        @Override public String pseudoState() { return ":focus-within"; }
        @Override public String body() { return """
                --hrg-cursor-color: %s;
                --hrg-frame-shadow: inset 0 0 0 1px %s, %s;
                """.formatted(RING_COLOUR, RING_BORDER, RING_GLOW);
        }
    }

    // ── the table and its parts ──

    /**
     * The table: the keyboard host, on the base surface in the body ink and
     * face at the label's size. Its width is 100% unless every column holds
     * one — then their sum at least. Borders SEPARATE, spacing none: a cell's
     * lattice lines are its own, so they stick with a sticky header, which a
     * collapsed border — the table's — never does.
     */
    public record hrg_table() implements CssClass<RelGridStyles> {
        @Override public String body() { return """
                border-collapse: separate;
                border-spacing: 0;
                width: var(--hrg-table-w, 100%);
                user-select: none;
                -webkit-user-select: none;
                outline: none;
                """;
        }
    }

    /** A header cell: the raised surface in the muted ink at the label's weight, drawing its share of the lattice. */
    public record hrg_th() implements CssClass<RelGridStyles> {
        @Override public String body() { return """
                position: relative;
                text-align: left;
                white-space: nowrap;
                """;
        }
    }

    /**
     * STICKY: a header cell that stays at the top of whatever scrolls the table,
     * over the rows passing under it. On the cell, not the row or the band —
     * that is what every browser sticks. Above a merged cell's host (30), under
     * the frame (40), the editor (50) and the mask (60). A host with a band of
     * its own above the table says how tall in {@code --hrg-sticky-top}, so the
     * header sticks under the band rather than under nothing.
     */
    public record hrg_sticky() implements CssClass<RelGridStyles> {
        @Override public String body() { return """
                position: sticky;
                top: var(--hrg-sticky-top, 0px);
                z-index: 35;
                """;
        }
    }

    /** The resize HANDLE: a real element on the header's right edge, so the pointer shows col-resize and the drag has a target. */
    public record hrg_resize_handle() implements CssClass<RelGridStyles> {
        @Override public String body() { return """
                position: absolute;
                top: 0; right: 0;
                width: 8px; height: 100%;
                cursor: col-resize;
                """;
        }
    }

    /** A column's width rides a custom property on its col; unsized columns share the remainder. */
    public record hrg_col() implements CssClass<RelGridStyles> {
        @Override public String body() { return """
                width: var(--hrg-col-w, auto);
                """;
        }
    }

    /** The guide a header drag draws: one segment per box of the extent, in fixed coordinates the drag sets, in the primary surface. */
    public record hrg_resize_guide() implements CssClass<RelGridStyles> {
        @Override public String body() { return """
                position: fixed;
                top: var(--hrg-guide-top);
                height: var(--hrg-guide-h);
                left: var(--hrg-guide-x);
                width: 2px;
                z-index: 99;
                pointer-events: none;
                """;
        }
    }

    /**
     * A slot: its share of the lattice — a trailing edge and a bottom edge,
     * as heavy as the design says. position:relative makes it a containing
     * block, so a cell may lay something OVER it instead of IN it;
     * overflow:hidden makes it a clip, so nothing a cell shows can widen a
     * column.
     */
    public record hrg_td() implements CssClass<RelGridStyles> {
        @Override public String body() { return """
                padding: 0;
                position: relative;
                vertical-align: middle;
                overflow: hidden;
                """;
        }
    }

    /**
     * A MERGED CELL's host, laid over the n slots it reaches across — opaque,
     * on the base surface, so the slots beneath and the lines between them are
     * covered; no pointer, so a click lands on the exact slot beneath. It
     * wears the group's state — cursor, selection — mirrored from the slots.
     */
    public record hrg_merge() implements CssClass<RelGridStyles> {
        @Override public String body() { return """
                position: absolute;
                z-index: 30;
                box-sizing: border-box;
                overflow: hidden;
                pointer-events: none;
                left: var(--hrg-left); top: var(--hrg-top);
                width: var(--hrg-width); height: var(--hrg-height);
                """;
        }
    }

    /**
     * An editor's anchor: laid over the slot, in the wrapper, OUTSIDE the
     * table — so an editor cannot widen a column, stretch a row, or be clipped
     * by its own cell, and a cell may open something larger than itself. On
     * the base surface, ringed by the design's focus ring: the one cell that
     * holds control is the one with the ring.
     */
    public record hrg_edit() implements CssClass<RelGridStyles> {
        @Override public String body() { return """
                position: absolute;
                z-index: 50;
                box-sizing: border-box;
                left: var(--hrg-left); top: var(--hrg-top);
                width: var(--hrg-width); height: var(--hrg-height);
                """;
        }
    }

    /**
     * THE GUTTER: a row's number, the grid's own, stuck to the left of whatever
     * scrolls the table — under the editor (50) and the mask (60), over the
     * cells it slides across. Muted, right-aligned, tabular so the digits line
     * up; the header's raised surface so it reads as chrome, not data; its
     * share of the lattice like any slot.
     */
    public record hrg_gutter() implements CssClass<RelGridStyles> {
        @Override public String body() { return """
                position: sticky;
                left: 0;
                z-index: 30;
                padding: 0 8px;
                text-align: right;
                font-variant-numeric: tabular-nums;
                white-space: nowrap;
                user-select: none;
                -webkit-user-select: none;
                cursor: default;
                """;
        }
    }

    /** The gutter's corner: the header's cell above the numbers, stuck both ways, above the sticky header (35). */
    public record hrg_gutter_head() implements CssClass<RelGridStyles> {
        @Override public String body() { return """
                z-index: 36;
                """;
        }
    }

    /** The gutter's col: as wide as the numbers need, {@code --hrg-gutter-w} to say otherwise. */
    public record hrg_gutter_col() implements CssClass<RelGridStyles> {
        @Override public String body() { return """
                width: var(--hrg-gutter-w, 44px);
                """;
        }
    }

    /**
     * THE MASK. Over the whole table, in the wrapper: the design's backdrop —
     * a wash heavy enough that the rows beneath read as unavailable rather
     * than current — and focusable so the keys stop here. It dims rather than
     * replaces: the context stays.
     */
    public record hrg_mask() implements CssClass<RelGridStyles> {
        @Override public String body() { return """
                position: absolute;
                left: 0; top: 0; right: 0; bottom: 0;
                z-index: 60;
                outline: none;
                """;
        }
    }

    /** THE PANEL: the grid's box for the domain's element. A raised plate under the overlay's shadow, scrolling inside itself. */
    public record hrg_panel() implements CssClass<RelGridStyles> {
        @Override public String body() { return """
                position: absolute;
                box-sizing: border-box;
                left: var(--hrg-left); top: var(--hrg-top);
                width: var(--hrg-width); height: var(--hrg-height);
                overflow: auto;
                outline: none;
                """;
        }
    }

    /** The copier's scratch: a textarea off-screen for the copy command to select. */
    public record hrg_scratch() implements CssClass<RelGridStyles> {
        @Override public String body() { return """
                position: fixed;
                left: -9999px;
                top: 0;
                """;
        }
    }

    // ── the states the grid paints: predicates a theme may style by ──

    /** Engaged once any explicit width exists, so unsized columns keep sharing the remainder. */
    public record hrg_fixed() implements CssClass<RelGridStyles> {
        @Override public String body() { return """
                table-layout: fixed;
                """;
        }
    }

    /**
     * What a slot asks of content too wide for it: an ellipsis. Ellipsis by
     * default, because wrapping makes one row six lines tall while its
     * neighbours stay at one. nowrap is inherited by every cell; the ellipsis
     * itself is the cell's to draw, from {@code --hrg-text-overflow} — the
     * stock text cell does — because the text is the cell's element, not the
     * slot's. It engages only once a column has a definite width.
     */
    public record hrg_ov_ellipsis() implements CssClass<RelGridStyles> {
        @Override public String body() { return """
                --hrg-text-overflow: ellipsis;
                white-space: nowrap;
                """;
        }
    }

    /** Clip without an ellipsis. */
    public record hrg_ov_clip() implements CssClass<RelGridStyles> {
        @Override public String body() { return """
                white-space: nowrap;
                """;
        }
    }

    /** Wrap — what plain CSS does. A predicate with nothing to add. */
    public record hrg_ov_wrap() implements CssClass<RelGridStyles> {
        @Override public String body() { return ""; }
    }

    /** The leading slot of a merged cell. A fact: the lattice is shed from it, and the group's host draws the rules. */
    public record hrg_lead() implements CssClass<RelGridStyles> {
        @Override public String body() { return ""; }
    }

    /** A slot a merged cell reaches over. A fact: the lattice is shed from it too. */
    public record hrg_covered() implements CssClass<RelGridStyles> {
        @Override public String body() { return ""; }
    }

    /** The last slot a merged cell reaches over. A fact. */
    public record hrg_group_end() implements CssClass<RelGridStyles> {
        @Override public String body() { return ""; }
    }

    /**
     * The selection, as a fact: on every slot the resolved list covers, and on a merged cell's host when any of its
     * slots is - reflected besides as aria-selected, which the design's selection fill shows by.
     */
    public record hrg_sel() implements CssClass<RelGridStyles> {
        @Override public String body() { return ""; }
    }

    /**
     * The cursor, as a fact: on the slot, never on the cell. What it looks like is the leaf the slot wears beside it -
     * the cursor while the keys are elsewhere, while the grid holds them, while its cell holds control.
     */
    public record hrg_cursor() implements CssClass<RelGridStyles> {
        @Override public String body() { return ""; }
    }

    /** A cell holds control. A fact, worn by the wrapper and the table; the cursor wears the lent leaf meanwhile. */
    public record hrg_deep() implements CssClass<RelGridStyles> {
        @Override public String body() { return ""; }
    }

    /** A question is pending and the mask is up. A predicate with nothing to add. */
    public record hrg_masked() implements CssClass<RelGridStyles> {
        @Override public String body() { return ""; }
    }

    @Override public List<CssClass<RelGridStyles>> cssClasses() {
        return List.of(
                // looks
                new hrg_wrap(), new hrg_frame(), new hrg_lit(),
                new hrg_table(), new hrg_th(), new hrg_sticky(), new hrg_resize_handle(), new hrg_col(), new hrg_resize_guide(),
                new hrg_td(), new hrg_merge(), new hrg_edit(), new hrg_gutter(), new hrg_gutter_head(), new hrg_gutter_col(),
                new hrg_mask(), new hrg_panel(), new hrg_scratch(),
                // states, after the looks they qualify
                new hrg_fixed(), new hrg_ov_ellipsis(), new hrg_ov_clip(), new hrg_ov_wrap(),
                new hrg_lead(), new hrg_covered(), new hrg_group_end(),
                new hrg_sel(), new hrg_cursor(), new hrg_deep(), new hrg_masked());
    }
}
