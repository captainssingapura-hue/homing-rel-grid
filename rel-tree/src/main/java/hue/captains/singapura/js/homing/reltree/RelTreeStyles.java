package hue.captains.singapura.js.homing.reltree;

import hue.captains.singapura.js.homing.core.CssClass;
import hue.captains.singapura.js.homing.core.CssGroup;
import hue.captains.singapura.js.homing.core.CssImportsFor;

import java.util.List;

/**
 * RFC 0050 · Episode 3-ext1 — the tree's LOOKS, typed. One class per thing the
 * layout mints and one per STATE it paints; no descendant selectors, because
 * the substrate renders one rule per class and the tree paints its predicates
 * itself.
 *
 * <p>The indent is a custom property: a row wears {@code --hrt-depth} and the
 * sheet turns it into padding with {@code --hrt-indent}, the unit — a theme's
 * or a host's to set on any ancestor. The cursor's colour rides
 * {@code --hrt-cursor-color}: dim at rest, the accent while the wrap holds the
 * focus ({@code hrt_lit}), so a tree that is not listening does not look like
 * one that is.</p>
 *
 * <p>Order matters within one specificity: a state class listed after a look
 * wins over it, which is why the states come last.</p>
 */
public record RelTreeStyles() implements CssGroup<RelTreeStyles> {

    public static final RelTreeStyles INSTANCE = new RelTreeStyles();

    // ── the wrapper: the positioned parent, and the frame that catches the focus ──

    /** container > WRAP > tree: the positioned parent for the mask and the panel. */
    public record hrt_wrap() implements CssClass<RelTreeStyles> {
        @Override public String body() { return """
                position: relative;
                """;
        }
    }

    /** Lit while something in the wrap holds the focus: the cursor takes the accent. */
    public record hrt_lit() implements CssClass<RelTreeStyles> {
        @Override public String pseudoState() { return ":focus-within"; }
        @Override public String body() { return """
                --hrt-cursor-color: var(--color-accent);
                """;
        }
    }

    /** The tree: the keyboard host, a column of rows. */
    public record hrt_tree() implements CssClass<RelTreeStyles> {
        @Override public String body() { return """
                display: flex;
                flex-direction: column;
                background: var(--color-surface);
                color: var(--color-text-primary);
                font: 13px sans-serif;
                user-select: none;
                -webkit-user-select: none;
                outline: none;
                """;
        }
    }

    /**
     * A row: the caret, then the domain's cell, indented by depth. The indent is
     * {@code --hrt-depth} × {@code --hrt-indent}; the row paints the depth and the
     * sheet does the arithmetic.
     */
    public record hrt_row() implements CssClass<RelTreeStyles> {
        @Override public String body() { return """
                display: flex;
                align-items: center;
                gap: 4px;
                min-height: 24px;
                padding: 0 6px 0 calc(6px + var(--hrt-depth, 0) * var(--hrt-indent, 18px));
                border-radius: 4px;
                outline: 2px solid transparent;
                outline-offset: -2px;
                cursor: default;
                white-space: nowrap;
                """;
        }
    }

    /** The tree's caret: a fixed box so the cells align whether or not there is a glyph in it. */
    public record hrt_caret() implements CssClass<RelTreeStyles> {
        @Override public String body() { return """
                flex: 0 0 auto;
                width: 16px;
                text-align: center;
                font-size: 11px;
                color: var(--color-text-muted);
                cursor: pointer;
                """;
        }
    }

    /** A leaf's caret: the box stays, the glyph is gone, and there is nothing to press. */
    public record hrt_caret_leaf() implements CssClass<RelTreeStyles> {
        @Override public String body() { return """
                cursor: default;
                """;
        }
    }

    /** THE MASK. Over the rows, in the wrap: a wash that reads as unavailable, focusable so the keys stop here. */
    public record hrt_mask() implements CssClass<RelTreeStyles> {
        @Override public String body() { return """
                position: absolute;
                left: 0; top: 0; right: 0; bottom: 0;
                z-index: 60;
                outline: none;
                background: color-mix(in srgb, var(--color-surface) 64%, transparent);
                """;
        }
    }

    /** THE PANEL: the tree's box for the domain's element, centred by the sheet. */
    public record hrt_panel() implements CssClass<RelTreeStyles> {
        @Override public String body() { return """
                position: absolute;
                box-sizing: border-box;
                left: 50%; top: 50%;
                transform: translate(-50%, -50%);
                width: min(80%, 480px);
                max-height: 80%;
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

    // ── states, after the looks they qualify ──

    /** The current row: an outline in the cursor's colour, painted on the row and never on the cell. */
    public record hrt_current() implements CssClass<RelTreeStyles> {
        @Override public String body() { return """
                outline-color: var(--hrt-cursor-color, color-mix(in srgb, var(--color-accent) 45%, var(--color-border)));
                background: color-mix(in srgb, var(--color-accent) 10%, transparent);
                """;
        }
    }

    /** A question is pending and the mask is up. A predicate with nothing to add. */
    public record hrt_masked() implements CssClass<RelTreeStyles> {
        @Override public String body() { return ""; }
    }

    @Override public CssImportsFor<RelTreeStyles> cssImports() { return CssImportsFor.none(this); }

    @Override public List<CssClass<RelTreeStyles>> cssClasses() {
        return List.of(
                // looks
                new hrt_wrap(), new hrt_lit(), new hrt_tree(), new hrt_row(), new hrt_caret(), new hrt_caret_leaf(),
                new hrt_mask(), new hrt_panel(),
                // states
                new hrt_current(), new hrt_masked());
    }
}
