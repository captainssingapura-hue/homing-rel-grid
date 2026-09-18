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

/**
 * The Han fences' looks, typed. Drawn to sit over the squares' width: the
 * same serif as the squares, and the same grey as their hairlines, so the
 * fences read as the manuscript's own margins rather than as chrome around it.
 */
public record HanFenceStyles() implements CssGroup<HanFenceStyles> {

    public static final HanFenceStyles INSTANCE = new HanFenceStyles();

    public record wb_hanf() implements CssClass<HanFenceStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Body.class, Color.Ink.class)); }
        @Override public String body() { return """
                font-family: 'Noto Serif CJK SC', 'Source Han Serif SC', 'Songti SC', 'SimSun', 'PMingLiU', serif;
                padding: 6px 4px 4px;
                """;
        }
    }

    public record wb_hanf_title() implements CssClass<HanFenceStyles> {
        @Override public String body() { return """
                display: flex;
                align-items: baseline;
                gap: 10px;
                """;
        }
    }

    public record wb_hanf_t() implements CssClass<HanFenceStyles> {
        @Override public String body() { return """
                font-size: 18px;
                letter-spacing: 0.15em;
                """;
        }
    }

    public record wb_hanf_a() implements CssClass<HanFenceStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Caption.class, Type.Scale.class), of(Muted.class, Color.Ink.class)); }
        @Override public String body() { return """
                letter-spacing: 0.1em;
                """;
        }
    }

    public record wb_hanf_orn() implements CssClass<HanFenceStyles> {
        @Override public String body() { return """
                display: flex;
                flex-direction: column;
                align-items: center;
                gap: 2px;
                padding: 10px 0 8px;
                """;
        }
    }

    public record wb_hanf_g() implements CssClass<HanFenceStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Muted.class, Color.Ink.class)); }
        @Override public String body() { return """
                opacity: .45;
                font-size: 44px;
                line-height: 1;
                """;
        }
    }

    public record wb_hanf_n() implements CssClass<HanFenceStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Kicker.class, Type.Scale.class), of(Muted.class, Color.Ink.class)); }
        @Override public String body() { return """
                letter-spacing: 0.2em;
                """;
        }
    }

    public record wb_hanf_colophon() implements CssClass<HanFenceStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Caption.class, Type.Scale.class), of(Muted.class, Color.Ink.class)); }
        @Override public String body() { return """
                text-align: center;
                letter-spacing: 0.3em;
                padding: 10px 0 4px;
                """;
        }
    }

    @Override public List<CssClass<HanFenceStyles>> cssClasses() {
        return List.of(new wb_hanf(), new wb_hanf_title(), new wb_hanf_t(), new wb_hanf_a(),
                       new wb_hanf_orn(), new wb_hanf_g(), new wb_hanf_n(), new wb_hanf_colophon());
    }
}
