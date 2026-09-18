package hue.captains.singapura.js.homing.relgrid;

import hue.captains.singapura.js.homing.core.CssClass;
import hue.captains.singapura.js.homing.core.util.CssClassName;
import hue.captains.singapura.js.homing.core.CssGroup;
import hue.captains.singapura.js.homing.core.Wearable;

import java.util.List;

import static hue.captains.singapura.js.homing.design.DesignClass.of;
import static hue.captains.singapura.js.homing.design.Target.*;
import static hue.captains.singapura.js.homing.design.Emphasis.*;
import static hue.captains.singapura.js.homing.design.Interaction.*;
import static hue.captains.singapura.js.homing.design.Layer.*;
import static hue.captains.singapura.js.homing.design.Structure.*;
import static hue.captains.singapura.js.homing.design.Text.*;

/**
 * RFC 0050 · Episode 2 — the grid's LOOKS, typed. One class per thing the
 * layout mints and one per STATE it paints; nothing here is a descendant
 * selector, because the substrate renders one rule per class and the grid
 * paints its predicates itself.
 *
 * <h2>What a class wears, and what it reads</h2>
 *
 * <p>The grid says nothing about colour, type, depth or line weight of its
 * own: every such value is a <b>design's word</b> (RFC 0065). A class
 * {@code wears()} the pairs its element means — the table is the base
 * surface in the body ink, a header cell the raised surface in the muted
 * ink, a slot draws the {@code Lattice}, a panel is a raised plate under
 * the overlay's shadow — and the design fills them.</p>
 *
 * <p>Where the old sheet said <i>"a cursor inside a wrapper that holds the
 * focus is full accent"</i> with a descendant selector, this says it with a
 * custom property: {@code hrg_lit} — the wrapper, under {@code :focus-within}
 * — sets {@code --hrg-cursor-color}, and {@code hrg_cursor} draws its outline
 * in it. Custom properties inherit, so a state painted on an ancestor reaches
 * every slot beneath it without a selector that reaches down. The properties
 * are the grid's published vocabulary — a host or a group may set them on any
 * ancestor — and what they carry is the design's: a class that fills or
 * draws from a channel {@code reads()} the pair whose binding it names, so
 * the value is the design's ring, the design's selection, passed down.</p>
 *
 * <ul>
 *   <li>{@code --hrg-cursor-color} — the cursor's outline colour; the ring dimmed
 *       towards the lattice at rest, the ring while lit, transparent while dormant</li>
 *   <li>{@code --hrg-cursor-style} — the ring's style, or dashed while a cell holds control</li>
 *   <li>{@code --hrg-sel-color} — the selection surface; transparent while dormant</li>
 *   <li>{@code --hrg-frame-shadow} — the wrapper's frame: an inset hairline, and lit, the design's focus glow</li>
 *   <li>{@code --hrg-text-overflow} — what the slot asks of a cell's text: ellipsis
 *       under {@code hrg_ov_ellipsis}; a cell that shows text honours it</li>
 * </ul>
 *
 * <p>Geometry the layout measures — an overlay's box, the panel's, a merged
 * cell's, the table's least width — rides {@code --hrg-left / --hrg-top /
 * --hrg-width / --hrg-height / --hrg-table-w} on the element, never an inline
 * style. A column's width rides {@code --hrg-col-w} on its {@code <col>}.</p>
 *
 * <p>Order matters within one specificity: a state class listed after a look
 * wins over it, which is why the states come last. A state that must win
 * over a worn word — a merged cell dropping a lattice line — says so with a
 * nested selector one step more specific, since the design's sheet is the
 * later one.</p>
 */
public record RelGridStyles() implements CssGroup<RelGridStyles> {

    public static final RelGridStyles INSTANCE = new RelGridStyles();

    // ── the words the channels carry — the ring, the selection, the lattice — by name, for anything that draws from a channel ──

    public static final String RING_COLOUR  = of(Focus.class, Color.Edge.class).var("outline-color");
    public static final String RING_BORDER  = of(Focus.class, Color.Edge.class).var("border-color");
    public static final String RING_WIDTH   = of(Focus.class, Shape.Rule.class).var("outline-width");
    public static final String RING_STYLE   = of(Focus.class, Shape.Rule.class).var("outline-style");
    public static final String RING_OFFSET  = of(Focus.class, Shape.Rule.class).var("outline-offset");
    public static final String RING_GLOW    = of(Focus.class, Shape.Shadow.class).var();
    public static final String LATTICE_LINE = of(Lattice.class, Color.Edge.class).var("border-color");
    public static final String SEL_SURFACE  = of(Selected.class, Color.Surface.class).var("background-color");
    public static final String SEL_INK      = of(Selected.class, Color.Ink.class).var();

    // ── the wrapper: the positioned parent, and the frame that catches the focus ──

    /** container > WRAPPER > table: the positioned parent for anything that is not a cell. */
    public record hrg_wrap() implements CssClass<RelGridStyles> {
        @Override public String body() { return """
                position: relative;
                """;
        }
    }

    /**
     * LIT, NOT LIFTED. A frame drawn OVER the table, taking no pointer, under
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
        @Override public List<? extends Wearable> reads() { return List.of(of(Lattice.class, Color.Edge.class)); }
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
        @Override public List<? extends Wearable> reads() { return List.of(of(Focus.class, Color.Edge.class), of(Focus.class, Shape.Shadow.class)); }
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
        @Override public List<? extends Wearable> wears() { return List.of(of(Base.class, Color.Surface.class), of(Body.class, Color.Ink.class), of(Body.class, Type.Face.class), of(Label.class, Type.Scale.class)); }
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
        @Override public List<? extends Wearable> wears() { return List.of(of(Raised.class, Color.Surface.class), of(Muted.class, Color.Ink.class), of(Label.class, Type.Weight.class), of(Lattice.class, Color.Edge.class), of(Lattice.class, Shape.Rule.class)); }
        @Override public String body() { return """
                position: relative;
                text-align: left;
                padding: 6px 10px;
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
        @Override public List<? extends Wearable> wears() { return List.of(of(Primary.class, Color.Surface.class)); }
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
        @Override public List<? extends Wearable> wears() { return List.of(of(Lattice.class, Color.Edge.class), of(Lattice.class, Shape.Rule.class)); }
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
        @Override public List<? extends Wearable> wears() { return List.of(of(Base.class, Color.Surface.class), of(Body.class, Color.Ink.class)); }
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
        @Override public List<? extends Wearable> wears() { return List.of(of(Base.class, Color.Surface.class), of(Body.class, Color.Ink.class), of(Focus.class, Color.Edge.class), of(Focus.class, Shape.Rule.class)); }
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
        @Override public List<? extends Wearable> wears() { return List.of(of(Raised.class, Color.Surface.class), of(Muted.class, Color.Ink.class), of(Lattice.class, Color.Edge.class), of(Lattice.class, Shape.Rule.class)); }
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
        @Override public List<? extends Wearable> wears() { return List.of(of(Backdrop.class, Color.Surface.class)); }
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
        @Override public List<? extends Wearable> wears() { return List.of(of(Raised.class, Color.Surface.class), of(Body.class, Color.Ink.class), of(Raised.class, Color.Edge.class), of(Raised.class, Shape.Rule.class), of(Raised.class, Shape.Corner.class), of(Overlay.class, Shape.Shadow.class)); }
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

    /**
     * The leading slot of a merged cell: the line it reaches across is
     * dropped. One step more specific than the lattice's word, which is in
     * the later sheet and would otherwise put the line back.
     */
    public record hrg_lead() implements CssClass<RelGridStyles> {
        @Override public String body() { return """
                &:is(td) { border-right-color: transparent; }
                """;
        }
    }

    /** A slot a merged cell reaches over: its line is dropped too — except the last one's, which is the group's edge. */
    public record hrg_covered() implements CssClass<RelGridStyles> {
        @Override public String body() { return """
                &:is(td):not(.%s) { border-right-color: transparent; }
                """.formatted(CssClassName.toCssName(hrg_group_end.class));
        }
    }

    /** The last slot a merged cell reaches over: its right line is the group's edge, and stays. A predicate. */
    public record hrg_group_end() implements CssClass<RelGridStyles> {
        @Override public String body() { return ""; }
    }

    /**
     * The selection: the design's selected surface and ink on every slot the
     * resolved list covers, and on a merged cell's host when any of its slots
     * is — through the channel, so a group can make a dormant member's
     * selection transparent without touching a slot.
     */
    public record hrg_sel() implements CssClass<RelGridStyles> {
        @Override public List<? extends Wearable> reads() { return List.of(of(Selected.class, Color.Surface.class), of(Selected.class, Color.Ink.class)); }
        @Override public String body() { return """
                background-color: var(--hrg-sel-color, %s);
                color: var(--hrg-sel-ink, %s);
                """.formatted(SEL_SURFACE, SEL_INK);
        }
    }

    /**
     * The cursor: painted on the slot, never on the cell. The design's focus
     * ring — its width, its style, how far in it sits — in the ring's colour
     * dimmed towards the lattice at rest, so a cursor in a table that is NOT
     * listening does not look like one that is; the ring itself while lit;
     * dashed while the cell is deep, so the handover is visible.
     */
    public record hrg_cursor() implements CssClass<RelGridStyles> {
        @Override public List<? extends Wearable> reads() { return List.of(of(Focus.class, Color.Edge.class), of(Focus.class, Shape.Rule.class), of(Lattice.class, Color.Edge.class)); }
        @Override public String body() { return """
                outline-width: %s;
                outline-style: var(--hrg-cursor-style, %s);
                outline-offset: %s;
                outline-color: var(--hrg-cursor-color, color-mix(in srgb, %s 45%%, %s));
                """.formatted(RING_WIDTH, RING_STYLE, RING_OFFSET, RING_COLOUR, LATTICE_LINE);
        }
    }

    /** A cell holds control: the cursor goes dashed. Worn by the wrapper and the table. */
    public record hrg_deep() implements CssClass<RelGridStyles> {
        @Override public String body() { return """
                --hrg-cursor-style: dashed;
                """;
        }
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
