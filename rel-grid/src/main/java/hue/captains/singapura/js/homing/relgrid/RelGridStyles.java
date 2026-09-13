package hue.captains.singapura.js.homing.relgrid;

import hue.captains.singapura.js.homing.core.CssClass;
import hue.captains.singapura.js.homing.core.CssGroup;
import hue.captains.singapura.js.homing.core.CssImportsFor;

import java.util.List;

/**
 * RFC 0050 · Episode 2 — the grid's LOOKS, typed. One class per thing the
 * layout mints and one per STATE it paints; nothing here is a descendant
 * selector, because the substrate renders one rule per class and the grid
 * paints its predicates itself.
 *
 * <h2>The vocabulary</h2>
 *
 * <p>Where the old sheet said <i>"a cursor inside a wrapper that holds the
 * focus is full accent"</i> with a descendant selector, this says it with a
 * custom property: {@code hrg_lit} — the wrapper, under {@code :focus-within}
 * — sets {@code --hrg-cursor-color}, and {@code hrg_cursor} draws its outline
 * in {@code var(--hrg-cursor-color, <dim>)}. Custom properties inherit, so a
 * state painted on an ancestor reaches every slot beneath it without a
 * selector that reaches down. The properties are the grid's published
 * vocabulary — a host or a theme may set them on any ancestor:</p>
 *
 * <ul>
 *   <li>{@code --hrg-cursor-color} — the cursor's outline colour; dim towards the
 *       border at rest, the accent while lit, transparent while dormant</li>
 *   <li>{@code --hrg-cursor-style} — solid, or dashed while a cell holds control</li>
 *   <li>{@code --hrg-sel-color} — the selection wash; transparent while dormant</li>
 *   <li>{@code --hrg-frame-shadow} — the wrapper's inset frame; lit while focused</li>
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
 * wins over it, which is why the states come last.</p>
 */
public record RelGridStyles() implements CssGroup<RelGridStyles> {

    public static final RelGridStyles INSTANCE = new RelGridStyles();

    // ── the wrapper: the positioned parent, and the frame that catches the focus ──

    /** container > WRAPPER > table: the positioned parent for anything that is not a cell. */
    public record hrg_wrap() implements CssClass<RelGridStyles> {
        @Override public String body() { return """
                position: relative;
                """;
        }
    }

    /**
     * LIT, NOT LIFTED. A frame drawn OVER the table, inset — a host that mounts
     * the grid in a scrollport clips anything outside the box — taking no
     * pointer, under the editor (50) and the mask (60). At rest a hairline in
     * the border colour; lit, whatever {@code --hrg-frame-shadow} says.
     */
    public record hrg_frame() implements CssClass<RelGridStyles> {
        @Override public String pseudoState() { return "::after"; }
        @Override public String body() { return """
                content: "";
                position: absolute;
                left: 0; top: 0; right: 0; bottom: 0;
                pointer-events: none;
                z-index: 40;
                box-shadow: var(--hrg-frame-shadow, inset 0 0 0 1px var(--color-border));
                transition: box-shadow .18s ease;
                """;
        }
    }

    /**
     * The grid that holds the focus is visibly the grid that holds the focus,
     * and it says so with light: an accent-tinted hairline, a catch of light on
     * the inner top-left edge (white mixed into the raised surface, so a dark
     * theme gets a dim catch), a soft inner glow, and a cursor at full
     * strength. Focus is {@code :focus-within} — the browser's own fact — so
     * the editor's overlay and the mask's panel count as the grid holding it.
     * Worn by the wrapper, and by a group's fence when it is the stop.
     */
    public record hrg_lit() implements CssClass<RelGridStyles> {
        @Override public String pseudoState() { return ":focus-within"; }
        @Override public String body() { return """
                --hrg-cursor-color: var(--color-accent);
                --hrg-frame-shadow:
                    inset 0 0 0 1px color-mix(in srgb, var(--color-accent) 60%, var(--color-border)),
                    inset 1px 1px 0 1px color-mix(in srgb, white 35%, var(--color-surface-raised)),
                    inset 0 0 14px color-mix(in srgb, var(--color-accent) 18%, transparent);
                """;
        }
    }

    // ── the table and its parts ──

    /** The table: the keyboard host. Its width is 100% unless every column holds one — then their sum at least. */
    public record hrg_table() implements CssClass<RelGridStyles> {
        @Override public String body() { return """
                border-collapse: collapse;
                width: var(--hrg-table-w, 100%);
                background: var(--color-surface);
                color: var(--color-text-primary);
                font: 13px sans-serif;
                user-select: none;
                -webkit-user-select: none;
                outline: none;
                """;
        }
    }

    public record hrg_th() implements CssClass<RelGridStyles> {
        @Override public String body() { return """
                position: relative;
                text-align: left;
                padding: 6px 10px;
                background: var(--color-surface-raised);
                color: var(--color-text-muted);
                white-space: nowrap;
                box-shadow: inset -1px 0 0 var(--color-border),
                            inset 0 -2px 0 var(--color-border);
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

    /** The guide a header drag draws: one segment per box of the extent, in fixed coordinates the drag sets. */
    public record hrg_resize_guide() implements CssClass<RelGridStyles> {
        @Override public String body() { return """
                position: fixed;
                top: var(--hrg-guide-top);
                height: var(--hrg-guide-h);
                left: var(--hrg-guide-x);
                width: 2px;
                background: var(--color-accent);
                z-index: 99;
                pointer-events: none;
                """;
        }
    }

    /**
     * A slot. position:relative makes it a containing block, so a cell may
     * lay something OVER it instead of IN it; overflow:hidden makes it a
     * clip, so nothing a cell shows can widen a column.
     */
    public record hrg_td() implements CssClass<RelGridStyles> {
        @Override public String body() { return """
                padding: 0;
                position: relative;
                border-bottom: 1px solid var(--color-border);
                border-right: 1px solid color-mix(in srgb, var(--color-border) 50%, transparent);
                vertical-align: middle;
                overflow: hidden;
                """;
        }
    }

    /**
     * A MERGED CELL's host, laid over the n slots it reaches across — opaque,
     * so the slots beneath and the lines between them are covered; no
     * pointer, so a click lands on the exact slot beneath. It wears the
     * group's state — cursor, selection — mirrored from the slots.
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
                background: var(--color-surface);
                color: var(--color-text-primary);
                transition: outline-color .18s ease;
                """;
        }
    }

    /**
     * An editor's anchor: laid over the slot, in the wrapper, OUTSIDE the
     * table — so an editor cannot widen a column, stretch a row, or be clipped
     * by its own cell, and a cell may open something larger than itself.
     */
    public record hrg_edit() implements CssClass<RelGridStyles> {
        @Override public String body() { return """
                position: absolute;
                z-index: 50;
                box-sizing: border-box;
                left: var(--hrg-left); top: var(--hrg-top);
                width: var(--hrg-width); height: var(--hrg-height);
                background: var(--color-surface);
                color: var(--color-text-primary);
                outline: 2px solid var(--color-accent);
                outline-offset: -2px;
                """;
        }
    }

    /**
     * THE MASK. Over the whole table, in the wrapper: a wash heavy enough that
     * the rows beneath read as unavailable rather than current, and focusable
     * so the keys stop here. It dims rather than replaces — the context stays.
     */
    public record hrg_mask() implements CssClass<RelGridStyles> {
        @Override public String body() { return """
                position: absolute;
                left: 0; top: 0; right: 0; bottom: 0;
                z-index: 60;
                outline: none;
                background: color-mix(in srgb, var(--color-surface) 64%, transparent);
                """;
        }
    }

    /** THE PANEL: the grid's box for the domain's element. Raised, bordered, scrolling inside itself. */
    public record hrg_panel() implements CssClass<RelGridStyles> {
        @Override public String body() { return """
                position: absolute;
                box-sizing: border-box;
                left: var(--hrg-left); top: var(--hrg-top);
                width: var(--hrg-width); height: var(--hrg-height);
                overflow: auto;
                outline: none;
                background: var(--color-surface-raised);
                color: var(--color-text-primary);
                border: 1px solid var(--color-border);
                border-radius: 8px;
                box-shadow: 0 12px 36px color-mix(in srgb, var(--color-text-primary) 32%, transparent);
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

    /** The leading slot of a merged cell: the line it reaches across is dropped. */
    public record hrg_lead() implements CssClass<RelGridStyles> {
        @Override public String body() { return """
                border-right-color: transparent;
                """;
        }
    }

    /** A slot a merged cell reaches over. */
    public record hrg_covered() implements CssClass<RelGridStyles> {
        @Override public String body() { return """
                border-right-color: transparent;
                """;
        }
    }

    /** The last slot a merged cell reaches over: its right line is the group's edge. Listed after hrg_covered. */
    public record hrg_group_end() implements CssClass<RelGridStyles> {
        @Override public String body() { return """
                border-right-color: color-mix(in srgb, var(--color-border) 50%, transparent);
                """;
        }
    }

    /**
     * The selection: a wash on every slot the resolved list covers, and on a
     * merged cell's host when any of its slots is. Transparent while dormant.
     */
    public record hrg_sel() implements CssClass<RelGridStyles> {
        @Override public String body() { return """
                background: var(--hrg-sel-color, color-mix(in srgb, var(--color-accent) 12%, var(--color-surface)));
                """;
        }
    }

    /**
     * The cursor: painted on the slot, never on the cell. Dimmed towards the
     * border at rest, so a cursor in a table that is NOT listening does not
     * look like one that is; full accent while lit; dashed while the cell is
     * deep, so the handover is visible.
     */
    public record hrg_cursor() implements CssClass<RelGridStyles> {
        @Override public String body() { return """
                outline: 2px var(--hrg-cursor-style, solid)
                             var(--hrg-cursor-color, color-mix(in srgb, var(--color-accent) 45%, var(--color-border)));
                outline-offset: -2px;
                transition: outline-color .18s ease;
                """;
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

    @Override public CssImportsFor<RelGridStyles> cssImports() { return CssImportsFor.none(this); }

    @Override public List<CssClass<RelGridStyles>> cssClasses() {
        return List.of(
                // looks
                new hrg_wrap(), new hrg_frame(), new hrg_lit(),
                new hrg_table(), new hrg_th(), new hrg_sticky(), new hrg_resize_handle(), new hrg_col(), new hrg_resize_guide(),
                new hrg_td(), new hrg_merge(), new hrg_edit(), new hrg_mask(), new hrg_panel(), new hrg_scratch(),
                // states, after the looks they qualify
                new hrg_fixed(), new hrg_ov_ellipsis(), new hrg_ov_clip(), new hrg_ov_wrap(),
                new hrg_lead(), new hrg_covered(), new hrg_group_end(),
                new hrg_sel(), new hrg_cursor(), new hrg_deep(), new hrg_masked());
    }
}
