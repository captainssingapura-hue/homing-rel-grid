package hue.captains.singapura.js.homing.relgrid.workbench;

import hue.captains.singapura.js.homing.core.CssClass;
import hue.captains.singapura.js.homing.core.CssGroup;
import hue.captains.singapura.js.homing.core.Wearable;

import java.util.List;

import static hue.captains.singapura.js.homing.design.DesignClass.of;
import static hue.captains.singapura.js.homing.design.Target.*;
import static hue.captains.singapura.js.homing.design.Box.*;
import static hue.captains.singapura.js.homing.design.Emphasis.*;
import static hue.captains.singapura.js.homing.design.Feedback.*;
import static hue.captains.singapura.js.homing.design.Layer.*;
import static hue.captains.singapura.js.homing.design.Structure.*;
import static hue.captains.singapura.js.homing.design.Text.*;

/**
 * The Colour Extents bench's looks: a table whose rows are scaled words and
 * whose columns are extents, and a card driven by one slider. Every cell
 * class wears one colour pair and names it in {@code extents()}, which is
 * the whole point of the bench: six pairs the workbench claims an extent on,
 * so every design registered here must anchor all six at −1, 0 and 1 —
 * the table has no empty cells because {@code DesignCompletenessTest} does
 * not let a design leave one.
 */
public record ExtentStyles() implements CssGroup<ExtentStyles> {

    public static final ExtentStyles INSTANCE = new ExtentStyles();

    /** The table: a label column, then one column per extent. */
    public record wb_extent_table() implements CssClass<ExtentStyles> {
        @Override public String body() { return """
                display: grid;
                grid-template-columns: max-content repeat(9, minmax(0, 1fr));
                gap: 4px 3px;
                align-items: stretch;
                margin: 8px 0 16px 0;
                """;
        }
    }

    /** A column head: the extent, as a number. */
    public record wb_extent_head() implements CssClass<ExtentStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Kicker.class, Type.Scale.class), of(Kicker.class, Type.Weight.class), of(Muted.class, Color.Ink.class), of(Code.class, Type.Face.class)); }
        @Override public String body() { return """
                text-align: center;
                padding: 2px 0;
                """;
        }
    }

    /** A row label: the pair, in the code face. */
    public record wb_extent_label() implements CssClass<ExtentStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Code.class, Type.Face.class), of(Caption.class, Type.Scale.class), of(Muted.class, Color.Ink.class)); }
        @Override public String body() { return """
                display: flex;
                align-items: center;
                padding: 0 10px 0 0;
                white-space: nowrap;
                """;
        }
    }

    /** Every cell: a small box the pair's word colours; the extent is the element's. */
    public record wb_extent_cell() implements CssClass<ExtentStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Body.class, Type.Face.class), of(Caption.class, Type.Scale.class), of(Control.class, Shape.Corner.class)); }
        @Override public String body() { return """
                min-height: 30px;
                display: flex;
                align-items: center;
                justify-content: center;
                padding: 2px 6px;
                box-sizing: border-box;
                """;
        }
    }

    // ── the six words that scale, one class each, each claiming the extent ──

    /** A good mark, a bad one, an unremarkable one. */
    public record wb_x_success_ink() implements CssClass<ExtentStyles> {
        @Override public List<? extends Wearable> wears()   { return List.of(of(Success.class, Color.Ink.class)); }
        @Override public List<? extends Wearable> extents() { return List.of(of(Success.class, Color.Ink.class)); }
        @Override public String body() { return ""; }
    }

    /** A danger wash; turned the other way, success's; at zero, nothing. */
    public record wb_x_danger_surface() implements CssClass<ExtentStyles> {
        @Override public List<? extends Wearable> wears()   { return List.of(of(Danger.class, Color.Surface.class)); }
        @Override public List<? extends Wearable> extents() { return List.of(of(Danger.class, Color.Surface.class)); }
        @Override public String body() { return ""; }
    }

    /** The accent as a fill: a grey fill at −1, nothing at 0, the accent at 1. */
    public record wb_x_primary_surface() implements CssClass<ExtentStyles> {
        @Override public List<? extends Wearable> wears()   { return List.of(of(Primary.class, Color.Surface.class)); }
        @Override public List<? extends Wearable> extents() { return List.of(of(Primary.class, Color.Surface.class)); }
        @Override public String body() { return ""; }
    }

    /** The accent as emphasis: the muted ink at −1, the body ink at 0, the accent at 1. */
    public record wb_x_primary_ink() implements CssClass<ExtentStyles> {
        @Override public List<? extends Wearable> wears()   { return List.of(of(Primary.class, Color.Ink.class), of(Label.class, Type.Weight.class)); }
        @Override public List<? extends Wearable> extents() { return List.of(of(Primary.class, Color.Ink.class)); }
        @Override public String body() { return ""; }
    }

    /** Elevation as a number: recessed at −1, the page at 0, raised at 1 — with the raised edge, so the box shows where its surface is the page's. */
    public record wb_x_raised_surface() implements CssClass<ExtentStyles> {
        @Override public List<? extends Wearable> wears()   { return List.of(of(Raised.class, Color.Surface.class), of(Hairline.class, Color.Edge.class), of(Control.class, Shape.Rule.class)); }
        @Override public List<? extends Wearable> extents() { return List.of(of(Raised.class, Color.Surface.class)); }
        @Override public String body() { return ""; }
    }

    /** Calm to alarm on an edge: all clear at −1, the hairline at 0, the warning at 1. */
    public record wb_x_warning_edge() implements CssClass<ExtentStyles> {
        @Override public List<? extends Wearable> wears()   { return List.of(of(Warning.class, Color.Edge.class), of(Control.class, Shape.Rule.class)); }
        @Override public List<? extends Wearable> extents() { return List.of(of(Warning.class, Color.Edge.class)); }
        @Override public String body() { return """
                border-width: 2px;
                """;
        }
    }

    // ── the card: one number, three words ──

    /**
     * A card wearing three scaled words — its surface, its edge, its ink — so
     * one extent moves all three in step. The extent is a registered number,
     * so a transition on it tweens the colours: no keyframes, no JavaScript
     * animation, the property itself interpolates.
     */
    public record wb_extent_card() implements CssClass<ExtentStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(
                of(Raised.class, Color.Surface.class), of(Warning.class, Color.Edge.class), of(Control.class, Shape.Rule.class), of(Raised.class, Shape.Corner.class),
                of(Primary.class, Color.Ink.class), of(Body.class, Type.Face.class), of(Label.class, Type.Scale.class)); }
        @Override public List<? extends Wearable> extents() { return List.of(of(Raised.class, Color.Surface.class), of(Warning.class, Color.Edge.class), of(Primary.class, Color.Ink.class)); }
        @Override public String body() { return """
                border-width: 2px;
                padding: 18px 20px;
                margin: 8px 0;
                transition: --extent 400ms ease;
                """;
        }
    }

    public record wb_extent_card_title() implements CssClass<ExtentStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Heading.class, Type.Face.class), of(Heading.class, Type.Scale.class), of(Heading.class, Type.Weight.class)); }
        @Override public String body() { return """
                margin: 0 0 6px 0;
                """;
        }
    }

    /** The slider row: the range input and the number it is at. */
    public record wb_extent_slider() implements CssClass<ExtentStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Code.class, Type.Face.class), of(Caption.class, Type.Scale.class), of(Body.class, Color.Ink.class)); }
        @Override public String body() { return """
                display: flex;
                align-items: center;
                gap: 12px;
                margin: 8px 0;
                """;
        }
    }

    public record wb_extent_range() implements CssClass<ExtentStyles> {
        @Override public String body() { return """
                flex: 1 1 auto;
                min-width: 160px;
                """;
        }
    }

    @Override public List<CssClass<ExtentStyles>> cssClasses() {
        return List.of(new wb_extent_table(), new wb_extent_head(), new wb_extent_label(), new wb_extent_cell(),
                       new wb_x_success_ink(), new wb_x_danger_surface(), new wb_x_primary_surface(), new wb_x_primary_ink(),
                       new wb_x_raised_surface(), new wb_x_warning_edge(),
                       new wb_extent_card(), new wb_extent_card_title(), new wb_extent_slider(), new wb_extent_range());
    }
}
