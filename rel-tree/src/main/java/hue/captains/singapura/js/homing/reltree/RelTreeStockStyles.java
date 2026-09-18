package hue.captains.singapura.js.homing.reltree;

import hue.captains.singapura.js.homing.core.CssClass;
import hue.captains.singapura.js.homing.core.CssGroup;
import hue.captains.singapura.js.homing.core.Wearable;

import java.util.List;

import static hue.captains.singapura.js.homing.design.DesignClass.of;
import static hue.captains.singapura.js.homing.design.Target.*;
import static hue.captains.singapura.js.homing.design.Emphasis.*;
import static hue.captains.singapura.js.homing.design.Text.*;

/**
 * The stock text cell's looks: a line that clips, the mode it is told, and the
 * busy ring. Domain-side, as the cell is.
 *
 * <h2>The ring, and the movement it names</h2>
 *
 * <p>{@link hrt_text_cell_busy} plays an animation — the class half of a CSS
 * keyframe animation. The other half, the {@code @keyframes} that defines the
 * movement, is not a class and cannot ride a {@link CssClass}: it is a
 * top-level at-rule, and the typed sheet has no primitive for one yet. Until it
 * does, the crate DECLARES the movement here as {@link #KEYFRAMES} — beside the
 * class that names it, so the two halves have one owner — and a DEPLOYMENT
 * INSTALLS it in every theme's globals (the workbench's fixtures do). A
 * deployment that forgets gets a ring that does not turn: CSS treats a missing
 * keyframes as nothing to play. A typed {@code CssKeyframes} the class depends
 * on, checked at compile time and themeable, is the framework wish this
 * proves out.</p>
 */
public record RelTreeStockStyles() implements CssGroup<RelTreeStockStyles> {

    public static final RelTreeStockStyles INSTANCE = new RelTreeStockStyles();

    /** The name the ring plays, group-prefixed by hand since keyframe names are one namespace for the whole page. */
    public static final String SPIN = "hrt-spin";

    /** THE MOVEMENT: a full turn. Raw CSS for a deployment's theme globals — the half the typed sheet cannot yet declare. */
    public static final String KEYFRAMES = """
            @keyframes hrt-spin {
                from { transform: rotate(0deg); }
                to   { transform: rotate(360deg); }
            }
            """;

    public record hrt_text_cell() implements CssClass<RelTreeStockStyles> {
        @Override public String body() { return """
                flex: 1 1 auto;
                min-width: 0;
                overflow: hidden;
                text-overflow: ellipsis;
                """;
        }
    }

    /** The cell was told it is current. Worn, not decided: the tree paints the row, the cell adds the label's weight. */
    public record hrt_text_cell_current() implements CssClass<RelTreeStockStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Label.class, Type.Weight.class)); }
        @Override public String body() { return ""; }
    }

    /** The busy ring, after the text: a dotted circle in the primary edge, turning when the movement is installed — the class half of the animation. */
    public record hrt_text_cell_busy() implements CssClass<RelTreeStockStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Primary.class, Color.Edge.class)); }
        @Override public String body() { return """
                display: inline-block;
                box-sizing: border-box;
                width: 11px;
                height: 11px;
                margin-left: 8px;
                vertical-align: -1px;
                border-width: 2px;
                border-style: dotted;
                border-radius: 50%;
                animation: hrt-spin .8s linear infinite;
                """;
        }
    }

    @Override public List<CssClass<RelTreeStockStyles>> cssClasses() {
        return List.of(new hrt_text_cell(), new hrt_text_cell_current(), new hrt_text_cell_busy());
    }
}
