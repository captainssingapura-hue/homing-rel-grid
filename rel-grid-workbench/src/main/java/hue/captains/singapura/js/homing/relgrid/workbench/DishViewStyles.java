package hue.captains.singapura.js.homing.relgrid.workbench;

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

/** The view panel's looks, typed: a list of profiles, the held one ticked. */
public record DishViewStyles() implements CssGroup<DishViewStyles> {

    public static final DishViewStyles INSTANCE = new DishViewStyles();

    public record wb_view() implements CssClass<DishViewStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Body.class, Type.Face.class), of(Caption.class, Type.Scale.class)); }
        @Override public String body() { return """
                display: flex;
                flex-direction: column;
                gap: 6px;
                height: 100%;
                box-sizing: border-box;
                padding: 10px 14px;
                outline: none;
                """;
        }
    }

    public record wb_view_head() implements CssClass<DishViewStyles> {
        @Override public String body() { return """
                display: flex;
                align-items: baseline;
                gap: 8px;
                white-space: nowrap;
                overflow: hidden;
                """;
        }
    }

    public record wb_view_title() implements CssClass<DishViewStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Label.class, Type.Weight.class)); }
        @Override public String body() { return """
                font-size: 14px;
                """;
        }
    }

    public record wb_view_sub() implements CssClass<DishViewStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Muted.class, Color.Ink.class), of(Kicker.class, Type.Scale.class)); }
        @Override public String body() { return """
                overflow: hidden;
                text-overflow: ellipsis;
                """;
        }
    }

    /** The list fills what the head and foot leave, and scrolls if the box is short. */
    public record wb_view_list() implements CssClass<DishViewStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Raised.class, Color.Edge.class), of(Raised.class, Shape.Rule.class)); }
        @Override public String body() { return """
                flex: 1 1 auto;
                min-height: 0;
                overflow-y: auto;
                margin: 0;
                padding: 0;
                list-style: none;
                border-radius: 5px;
                """;
        }
    }

    public record wb_view_item() implements CssClass<DishViewStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Hairline.class, Color.Edge.class), of(Hairline.class, Shape.Rule.class)); }
        @Override public String body() { return """
                display: flex;
                align-items: baseline;
                gap: 8px;
                padding: 4px 10px;
                cursor: pointer;
                outline: none;
                """;
        }
    }

    /** The last item needs no rule below it: the list's border is there. Worn by every item. */
    public record wb_view_item_end() implements CssClass<DishViewStyles> {
        @Override public String pseudoState() { return ":last-child"; }
        @Override public String body() { return """
                border-bottom: 0;
                """;
        }
    }

    /** An item under the pointer or the focus. Worn by every item. */
    public record wb_view_item_hot() implements CssClass<DishViewStyles> {
        @Override public String pseudoState() { return ":is(:hover, :focus)"; }
        @Override public List<? extends Wearable> wears() { return List.of(of(Current.class, Color.Surface.class)); }
        @Override public String body() { return "";
        }
    }

    /** The profile this table is under. A predicate on the item; the tick is the label's. */
    public record wb_view_held() implements CssClass<DishViewStyles> {
        @Override public String body() { return ""; }
    }

    /** The held profile's label wears a tick. */
    public record wb_view_tick() implements CssClass<DishViewStyles> {
        @Override public String pseudoState() { return "::after"; }
        @Override public List<? extends Wearable> wears() { return List.of(of(Primary.class, Color.Ink.class)); }
        @Override public String body() { return """
                content: ' \\2713';
                """;
        }
    }

    public record wb_view_num() implements CssClass<DishViewStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Muted.class, Color.Ink.class), of(Kicker.class, Type.Scale.class)); }
        @Override public String body() { return """
                width: 1.2em;
                """;
        }
    }

    public record wb_view_label() implements CssClass<DishViewStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Label.class, Type.Weight.class)); }
        @Override public String body() { return """
                flex: 0 0 auto;
                """;
        }
    }

    public record wb_view_rule() implements CssClass<DishViewStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Muted.class, Color.Ink.class), of(Kicker.class, Type.Scale.class)); }
        @Override public String body() { return """
                flex: 1 1 auto;
                white-space: nowrap;
                overflow: hidden;
                text-overflow: ellipsis;
                """;
        }
    }

    public record wb_view_count() implements CssClass<DishViewStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Muted.class, Color.Ink.class), of(Kicker.class, Type.Scale.class)); }
        @Override public String body() { return """
                flex: 0 0 auto;
                font-variant-numeric: tabular-nums;
                """;
        }
    }

    public record wb_view_foot() implements CssClass<DishViewStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Muted.class, Color.Ink.class)); }
        @Override public String body() { return """
                display: flex;
                align-items: center;
                gap: 10px;
                font-size: 10px;
                white-space: nowrap;
                """;
        }
    }

    public record wb_view_keys() implements CssClass<DishViewStyles> {
        @Override public String body() { return """
                flex: 1 1 auto;
                text-align: right;
                overflow: hidden;
                text-overflow: ellipsis;
                """;
        }
    }

    public record wb_view_cancel() implements CssClass<DishViewStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Kicker.class, Type.Scale.class), of(Raised.class, Color.Edge.class), of(Raised.class, Shape.Rule.class), of(Control.class, Shape.Corner.class), of(Body.class, Color.Ink.class)); }
        @Override public String body() { return """
                font: inherit;
                padding: 2px 9px;
                background: transparent;
                cursor: pointer;
                """;
        }
    }

    @Override public List<CssClass<DishViewStyles>> cssClasses() {
        return List.of(new wb_view(), new wb_view_head(), new wb_view_title(), new wb_view_sub(), new wb_view_list(),
                       new wb_view_item(), new wb_view_item_end(), new wb_view_item_hot(), new wb_view_held(), new wb_view_tick(),
                       new wb_view_num(), new wb_view_label(), new wb_view_rule(), new wb_view_count(),
                       new wb_view_foot(), new wb_view_keys(), new wb_view_cancel());
    }
}
