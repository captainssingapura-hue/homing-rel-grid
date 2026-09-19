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
import static hue.captains.singapura.js.homing.design.Interaction.*;
import static hue.captains.singapura.js.homing.design.Layer.*;
import static hue.captains.singapura.js.homing.design.Structure.*;
import static hue.captains.singapura.js.homing.design.Text.*;

/** The stars cell's looks, typed: the stars in the slot, and the panel it edits with. */
public record DishStarsStyles() implements CssGroup<DishStarsStyles> {

    public static final DishStarsStyles INSTANCE = new DishStarsStyles();

    /**
     * The stars, in the success ink AT AN EXTENT: five stars is the meaning at
     * full, three is neutral, one is the meaning turned the other way — the
     * cell sets its extent from the rating, and the design's word for a good
     * mark, a bad one and an unremarkable one does the colouring. Naming the
     * pair in extents() is what obliges every design to anchor it at 0 and −1.
     */
    public record wb_stars() implements CssClass<DishStarsStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Success.class, Color.Ink.class), of(Body.class, Type.Face.class), of(Caption.class, Type.Scale.class)); }
        @Override public List<? extends Wearable> extents() { return List.of(of(Success.class, Color.Ink.class)); }
        @Override public String body() { return """
                letter-spacing: 2px;
                padding: 0 6px;
                """;
        }
    }

    public record wb_stars_ro() implements CssClass<DishStarsStyles> {
        @Override public String body() { return """
                opacity: 0.55;
                """;
        }
    }

    /**
     * The PANEL. Hung off the anchor the grid placed it in — which is exactly
     * the cell — so it is free to be much larger in both directions. Out of
     * the table it costs the table nothing: no column widens, no row grows.
     */
    public record wb_stars_panel() implements CssClass<DishStarsStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Raised.class, Color.Surface.class), of(Body.class, Color.Ink.class), of(Raised.class, Color.Edge.class), of(Raised.class, Shape.Rule.class), of(Raised.class, Shape.Corner.class), of(Overlay.class, Shape.Shadow.class)); }
        @Override public String body() { return """
                position: absolute;
                top: 100%;
                left: 0;
                min-width: 250px;
                padding: 12px 14px;
                box-sizing: border-box;
                outline: none;
                """;
        }
    }

    public record wb_stars_row() implements CssClass<DishStarsStyles> {
        @Override public String body() { return """
                display: flex;
                gap: 8px;
                font-size: 30px;
                line-height: 1;
                cursor: pointer;
                """;
        }
    }

    public record wb_star() implements CssClass<DishStarsStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Muted.class, Color.Ink.class)); }
        @Override public String body() { return """
                user-select: none;
                -webkit-user-select: none;
                opacity: 0.45;
                """;
        }
    }

    /** A star within the draft. Listed after wb_star, which it qualifies. */
    public record wb_star_on() implements CssClass<DishStarsStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Primary.class, Color.Ink.class)); }
        @Override public String body() { return """
                opacity: 1;
                """;
        }
    }

    public record wb_stars_hint() implements CssClass<DishStarsStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Muted.class, Color.Ink.class)); }
        @Override public String body() { return """
                margin-top: 10px;
                font: 11px sans-serif;
                white-space: nowrap;
                """;
        }
    }

    @Override public List<CssClass<DishStarsStyles>> cssClasses() {
        return List.of(new wb_stars(), new wb_stars_ro(), new wb_stars_panel(), new wb_stars_row(),
                       new wb_star(), new wb_star_on(), new wb_stars_hint());
    }
}
