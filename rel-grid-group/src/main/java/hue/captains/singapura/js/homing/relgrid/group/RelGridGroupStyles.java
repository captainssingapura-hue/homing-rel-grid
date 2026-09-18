package hue.captains.singapura.js.homing.relgrid.group;

import hue.captains.singapura.js.homing.core.CssClass;
import hue.captains.singapura.js.homing.core.CssGroup;
import hue.captains.singapura.js.homing.core.Wearable;
import hue.captains.singapura.js.homing.relgrid.RelGridStyles;

import java.util.List;

import static hue.captains.singapura.js.homing.design.DesignClass.of;
import static hue.captains.singapura.js.homing.design.Target.*;
import static hue.captains.singapura.js.homing.design.Interaction.*;
import static hue.captains.singapura.js.homing.design.Structure.*;

/**
 * RFC 0050 · Episode 2 — the GROUP's looks, typed: the column of boxes, the
 * fence slots, and the states the group paints. Nothing here reaches into a
 * member's slots with a selector: ONE CURSOR is kept by painting a member
 * DORMANT — a class on its box that sets the grid's own published properties
 * ({@code --hrg-cursor-color}, {@code --hrg-sel-color}) to transparent, which
 * every slot beneath inherits. A fence that is the stop wears the grid's
 * {@code hrg_lit} as well, so its outline lights under the focus the same way
 * a table's cursor does. What the group draws of its own — the fence's
 * cursor — is the design's ring, read the way the grid reads it (RFC 0065).
 */
public record RelGridGroupStyles() implements CssGroup<RelGridGroupStyles> {

    public static final RelGridGroupStyles INSTANCE = new RelGridGroupStyles();

    /** The column: member boxes and fence slots, stacked. */
    public record hrg_group() implements CssClass<RelGridGroupStyles> {
        @Override public String body() { return """
                display: flex;
                flex-direction: column;
                align-items: stretch;
                """;
        }
    }

    /** A member's box: the grid it holds mounts inside. */
    public record hrg_member() implements CssClass<RelGridGroupStyles> {
        @Override public String body() { return """
                flex: 0 0 auto;
                """;
        }
    }

    /**
     * The group's header box STAYS at the top of whatever scrolls the group,
     * over the members passing under it. The box, not the header cells: a
     * cell sticks only within its own table, and the header's table is one
     * row tall — the box's containing block is the group's root, which is
     * every member long. Above a merged cell's host (30), under a member's
     * frame (40) and editor (50).
     */
    public record hrg_group_header_sticky() implements CssClass<RelGridGroupStyles> {
        @Override public String body() { return """
                position: sticky;
                top: var(--hrg-sticky-top, 0px);
                z-index: 35;
                """;
        }
    }

    /** The group's own header, in 'group' mode: a table with nothing to present, at the very top. */
    public record hrg_group_header() implements CssClass<RelGridGroupStyles> {
        @Override public String body() { return """
                flex: 0 0 auto;
                """;
        }
    }

    /** A fence slot: the domain's element is placed in it. A Tab stop by the group's hand, with no outline of its own. */
    public record hrg_fence() implements CssClass<RelGridGroupStyles> {
        @Override public String body() { return """
                flex: 0 0 auto;
                outline: none;
                """;
        }
    }

    /** A slot nobody filled takes no height. */
    public record hrg_fence_empty() implements CssClass<RelGridGroupStyles> {
        @Override public String body() { return """
                display: none;
                """;
        }
    }

    /** A folded member: its box hidden, the table inside untouched. */
    public record hrg_folded() implements CssClass<RelGridGroupStyles> {
        @Override public String body() { return """
                display: none;
                """;
        }
    }

    /** The fence above a folded member wears the fact, for a domain that draws its control from it. A predicate. */
    public record hrg_fence_folded() implements CssClass<RelGridGroupStyles> {
        @Override public String body() { return ""; }
    }

    /** The active member — the one whose cursor is the group's. A predicate. */
    public record hrg_active() implements CssClass<RelGridGroupStyles> {
        @Override public String body() { return ""; }
    }

    /**
     * A member that is not active, or any member while a fence is the stop:
     * its cursor and selection are still there and simply not shown, through
     * the grid's own properties, inherited by every slot beneath the box.
     */
    public record hrg_dormant() implements CssClass<RelGridGroupStyles> {
        @Override public String body() { return """
                --hrg-cursor-color: transparent;
                --hrg-sel-color: transparent;
                --hrg-sel-ink: currentColor;
                """;
        }
    }

    /**
     * A fence that is the cursor wears the cell's mark: the same outline a
     * slot wears, in the same colour — dim at rest, full accent with the focus
     * through {@code hrg_lit} — so the eye follows one mark down the group.
     */
    public record hrg_fence_cursor() implements CssClass<RelGridGroupStyles> {
        @Override public List<? extends Wearable> reads() { return List.of(of(Focus.class, Color.Edge.class), of(Focus.class, Shape.Rule.class), of(Lattice.class, Color.Edge.class)); }
        @Override public String body() { return """
                outline-width: %s;
                outline-style: var(--hrg-cursor-style, %s);
                outline-offset: %s;
                outline-color: var(--hrg-cursor-color, color-mix(in srgb, %s 45%%, %s));
                """.formatted(RelGridStyles.RING_WIDTH, RelGridStyles.RING_STYLE, RelGridStyles.RING_OFFSET, RelGridStyles.RING_COLOUR, RelGridStyles.LATTICE_LINE);
        }
    }

    /** The cursor is on a fence: worn by the root. A predicate. */
    public record hrg_on_fence() implements CssClass<RelGridGroupStyles> {
        @Override public String body() { return ""; }
    }

    @Override public List<CssClass<RelGridGroupStyles>> cssClasses() {
        return List.of(new hrg_group(), new hrg_member(), new hrg_group_header(), new hrg_group_header_sticky(), new hrg_fence(),
                       new hrg_fence_empty(), new hrg_folded(), new hrg_fence_folded(), new hrg_active(),
                       new hrg_dormant(), new hrg_fence_cursor(), new hrg_on_fence());
    }
}
