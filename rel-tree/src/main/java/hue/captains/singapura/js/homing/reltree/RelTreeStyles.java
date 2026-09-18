package hue.captains.singapura.js.homing.reltree;

import hue.captains.singapura.js.homing.core.CssClass;
import hue.captains.singapura.js.homing.core.CssGroup;
import hue.captains.singapura.js.homing.core.Wearable;

import java.util.List;

import static hue.captains.singapura.js.homing.design.DesignClass.of;
import static hue.captains.singapura.js.homing.design.Target.*;
import static hue.captains.singapura.js.homing.design.Box.*;
import static hue.captains.singapura.js.homing.design.Emphasis.*;
import static hue.captains.singapura.js.homing.design.Interaction.*;
import static hue.captains.singapura.js.homing.design.Layer.*;
import static hue.captains.singapura.js.homing.design.Structure.*;
import static hue.captains.singapura.js.homing.design.Text.*;

/**
 * RFC 0050 · Episode 3-ext1 — the tree's LOOKS, typed. One class per thing the
 * layout mints and one per STATE it paints; no descendant selectors, because
 * the substrate renders one rule per class and the tree paints its predicates
 * itself.
 *
 * <p>The indent is a custom property: a row wears {@code --hrt-depth} and the
 * sheet turns it into padding with {@code --hrt-indent}, the unit — a theme's
 * or a host's to set on any ancestor. The cursor's colour rides
 * {@code --hrt-cursor-color}: the design's ring dimmed towards its hairline at
 * rest, the ring while the wrap holds the focus ({@code hrt_lit}), so a tree
 * that is not listening does not look like one that is.</p>
 *
 * <p>Every colour, face, size, corner and shadow here is a design's word (RFC
 * 0065): a class wears the pairs its element means, and where a value goes
 * through the cursor's channel the class reads the pair it passes down.</p>
 *
 * <p>Order matters within one specificity: a state class listed after a look
 * wins over it, which is why the states come last.</p>
 */
public record RelTreeStyles() implements CssGroup<RelTreeStyles> {

    public static final RelTreeStyles INSTANCE = new RelTreeStyles();

    // ── the ring, by name, for what draws from the cursor's channel ──
    static final String RING_COLOUR = of(Focus.class, Color.Edge.class).var("outline-color");
    static final String RING_WIDTH  = of(Focus.class, Shape.Rule.class).var("outline-width");
    static final String RING_STYLE  = of(Focus.class, Shape.Rule.class).var("outline-style");
    static final String RING_OFFSET = of(Focus.class, Shape.Rule.class).var("outline-offset");
    static final String HAIRLINE    = of(Hairline.class, Color.Edge.class).var("border-color");

    // ── the wrapper: the positioned parent, and the frame that catches the focus ──

    /** container > WRAP > tree: the positioned parent for the mask and the panel. */
    public record hrt_wrap() implements CssClass<RelTreeStyles> {
        @Override public String body() { return """
                position: relative;
                """;
        }
    }

    /** Lit while something in the wrap holds the focus: the cursor takes the design's ring. */
    public record hrt_lit() implements CssClass<RelTreeStyles> {
        @Override public String pseudoState() { return ":focus-within"; }
        @Override public List<? extends Wearable> reads() { return List.of(of(Focus.class, Color.Edge.class)); }
        @Override public String body() { return """
                --hrt-cursor-color: %s;
                """.formatted(RING_COLOUR);
        }
    }

    /** The tree: the keyboard host, a column of rows, on the base surface in the body ink and face at the label's size. */
    public record hrt_tree() implements CssClass<RelTreeStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Base.class, Color.Surface.class), of(Body.class, Color.Ink.class), of(Body.class, Type.Face.class), of(Label.class, Type.Scale.class)); }
        @Override public String body() { return """
                display: flex;
                flex-direction: column;
                user-select: none;
                -webkit-user-select: none;
                outline: none;
                """;
        }
    }

    /**
     * A row: the caret, then the domain's cell, indented by depth. The indent is
     * {@code --hrt-depth} × {@code --hrt-indent}; the row paints the depth and the
     * sheet does the arithmetic. Cornered as the design corners a control; the
     * ring is drawn on every row in the design's shape and no colour, so the
     * current row only has to colour it.
     */
    public record hrt_row() implements CssClass<RelTreeStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Control.class, Shape.Corner.class)); }
        @Override public List<? extends Wearable> reads() { return List.of(of(Focus.class, Shape.Rule.class)); }
        @Override public String body() { return """
                display: flex;
                align-items: center;
                gap: 4px;
                min-height: 24px;
                padding: 0 6px 0 calc(6px + var(--hrt-depth, 0) * var(--hrt-indent, 18px));
                outline-width: %s;
                outline-style: %s;
                outline-offset: %s;
                outline-color: transparent;
                cursor: default;
                white-space: nowrap;
                """.formatted(RING_WIDTH, RING_STYLE, RING_OFFSET);
        }
    }

    /**
     * The tree's caret: a fixed box holding the typed SVG chevron, so the cells
     * align whether or not there is anything to press. The chevron is drawn in
     * currentColor, so this colour — the design's muted ink — is the caret's.
     */
    public record hrt_caret() implements CssClass<RelTreeStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Muted.class, Color.Ink.class)); }
        @Override public String body() { return """
                flex: 0 0 auto;
                display: flex;
                align-items: center;
                justify-content: center;
                width: 16px;
                height: 16px;
                cursor: pointer;
                """;
        }
    }

    /** An open node's caret: the chevron turned a quarter to point at the children, in the primary ink. */
    public record hrt_caret_open() implements CssClass<RelTreeStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Primary.class, Color.Ink.class)); }
        @Override public String body() { return """
                transform: rotate(90deg);
                """;
        }
    }

    /** A leaf's caret: the box stays for alignment, the chevron is hidden, and there is nothing to press. */
    public record hrt_caret_leaf() implements CssClass<RelTreeStyles> {
        @Override public String body() { return """
                visibility: hidden;
                cursor: default;
                """;
        }
    }

    /**
     * The folder, after the caret: a fixed box holding the SVG folders — or a
     * host's text glyph — so a row with a folder and a row without align. The
     * folders are drawn in currentColor, so this colour is the design's muted ink.
     */
    public record hrt_folder() implements CssClass<RelTreeStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Muted.class, Color.Ink.class), of(Label.class, Type.Scale.class)); }
        @Override public String body() { return """
                flex: 0 0 auto;
                display: flex;
                align-items: center;
                justify-content: center;
                width: 20px;
                height: 16px;
                """;
        }
    }

    /** An open folder takes the primary ink, as the open caret does. */
    public record hrt_folder_open() implements CssClass<RelTreeStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Primary.class, Color.Ink.class)); }
        @Override public String body() { return ""; }
    }

    /** A leaf's folder box: kept for alignment; blank by default, and a leaf glyph the host chose is dimmed. */
    public record hrt_folder_leaf() implements CssClass<RelTreeStyles> {
        @Override public String body() { return """
                opacity: .55;
                """;
        }
    }

    /** The folder state not shown: the other of the two SVGs. */
    public record hrt_folder_off() implements CssClass<RelTreeStyles> {
        @Override public String body() { return """
                display: none;
                """;
        }
    }

    /** THE MASK. Over the rows, in the wrap: the design's backdrop — a wash that reads as unavailable — focusable so the keys stop here. */
    public record hrt_mask() implements CssClass<RelTreeStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Backdrop.class, Color.Surface.class)); }
        @Override public String body() { return """
                position: absolute;
                left: 0; top: 0; right: 0; bottom: 0;
                z-index: 60;
                outline: none;
                """;
        }
    }

    /** THE PANEL: the tree's box for the domain's element, centred by the sheet — a raised plate under the overlay's shadow. */
    public record hrt_panel() implements CssClass<RelTreeStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Raised.class, Color.Surface.class), of(Body.class, Color.Ink.class), of(Raised.class, Color.Edge.class), of(Raised.class, Shape.Rule.class), of(Raised.class, Shape.Corner.class), of(Overlay.class, Shape.Shadow.class)); }
        @Override public String body() { return """
                position: absolute;
                box-sizing: border-box;
                left: 50%; top: 50%;
                transform: translate(-50%, -50%);
                width: min(80%, 480px);
                max-height: 80%;
                overflow: auto;
                outline: none;
                """;
        }
    }

    // ── states, after the looks they qualify ──

    /** The current row: the design's current surface and ink, and the ring in the cursor's colour — painted on the row and never on the cell. */
    public record hrt_current() implements CssClass<RelTreeStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Current.class, Color.Surface.class), of(Current.class, Color.Ink.class)); }
        @Override public List<? extends Wearable> reads() { return List.of(of(Focus.class, Color.Edge.class), of(Hairline.class, Color.Edge.class)); }
        @Override public String body() { return """
                outline-color: var(--hrt-cursor-color, color-mix(in srgb, %s 45%%, %s));
                """.formatted(RING_COLOUR, HAIRLINE);
        }
    }

    /** A question is pending and the mask is up. A predicate with nothing to add. */
    public record hrt_masked() implements CssClass<RelTreeStyles> {
        @Override public String body() { return ""; }
    }

    @Override public List<CssClass<RelTreeStyles>> cssClasses() {
        return List.of(
                // looks
                new hrt_wrap(), new hrt_lit(), new hrt_tree(), new hrt_row(), new hrt_caret(), new hrt_caret_open(), new hrt_caret_leaf(),
                new hrt_folder(), new hrt_folder_open(), new hrt_folder_leaf(), new hrt_folder_off(),
                new hrt_mask(), new hrt_panel(),
                // states
                new hrt_current(), new hrt_masked());
    }
}
