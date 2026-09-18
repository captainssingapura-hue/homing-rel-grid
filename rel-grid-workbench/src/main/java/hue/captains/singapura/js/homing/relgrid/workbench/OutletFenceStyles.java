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

/** The Outlets fences' looks, typed: a name, a fold toggle, published totals; the ledger's band below the last. */
public record OutletFenceStyles() implements CssGroup<OutletFenceStyles> {

    public static final OutletFenceStyles INSTANCE = new OutletFenceStyles();

    public record wb_fence() implements CssClass<OutletFenceStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Body.class, Type.Face.class), of(Caption.class, Type.Scale.class), of(Body.class, Color.Ink.class)); }
        @Override public String body() { return """
                display: flex;
                align-items: baseline;
                gap: 12px;
                padding: 10px 10px 4px;
                """;
        }
    }

    public record wb_fence_name() implements CssClass<OutletFenceStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Label.class, Type.Weight.class)); }
        @Override public String body() { return """
                font-size: 14px;
                """;
        }
    }

    /** The toggle: a triangle, down while the book shows and right while it is folded. */
    public record wb_fence_fold() implements CssClass<OutletFenceStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Kicker.class, Type.Scale.class), of(Muted.class, Color.Ink.class)); }
        @Override public String body() { return """
                border: 0;
                background: transparent;
                cursor: pointer;
                padding: 0 4px;
                margin: 0;
                font: inherit;
                line-height: 1;
                width: 1.4em;
                text-align: center;
                """;
        }
    }

    /** The toggle under the pointer. Worn beside wb_fence_fold. */
    public record wb_fence_fold_hot() implements CssClass<OutletFenceStyles> {
        @Override public String pseudoState() { return ":hover"; }
        @Override public List<? extends Wearable> wears() { return List.of(of(Primary.class, Color.Ink.class)); }
        @Override public String body() { return "";
        }
    }

    public record wb_fence_totals() implements CssClass<OutletFenceStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Muted.class, Color.Ink.class), of(Kicker.class, Type.Scale.class)); }
        @Override public String body() { return """
                font-variant-numeric: tabular-nums;
                """;
        }
    }

    /** The ledger's band: ruled off from the last book. Listed after wb_fence. */
    public record wb_fence_ledger() implements CssClass<OutletFenceStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Cap.class, Color.Edge.class), of(Cap.class, Shape.Rule.class)); }
        @Override public String body() { return """
                margin-top: 6px;
                padding-top: 8px;
                """;
        }
    }

    @Override public List<CssClass<OutletFenceStyles>> cssClasses() {
        return List.of(new wb_fence(), new wb_fence_name(), new wb_fence_fold(), new wb_fence_fold_hot(),
                       new wb_fence_totals(), new wb_fence_ledger());
    }
}
