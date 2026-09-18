package hue.captains.singapura.js.homing.jsonkit;

import hue.captains.singapura.js.homing.core.CssClass;
import hue.captains.singapura.js.homing.core.CssGroup;
import hue.captains.singapura.js.homing.core.Wearable;

import java.util.List;

import static hue.captains.singapura.js.homing.design.DesignClass.of;
import static hue.captains.singapura.js.homing.design.Target.*;
import static hue.captains.singapura.js.homing.design.Emphasis.*;
import static hue.captains.singapura.js.homing.design.Text.*;

/**
 * The JSON node cell's looks, typed: the line, the name, the colon, and one
 * class per kind of value, each wearing the design's word for it (RFC 0065)
 * — the code face and size for the line, the body ink for a name and a
 * number, the muted ink for a position, a colon or a count, the primary ink
 * for a string, the kicker's for a literal — so a design colours the kinds
 * without knowing JSON exists. Domain-side, as the cell is. No stylesheet
 * anywhere: one rule per record.
 */
public record JsonTreeStyles() implements CssGroup<JsonTreeStyles> {

    public static final JsonTreeStyles INSTANCE = new JsonTreeStyles();

    /** The cell: one line that clips, filling the row after the caret, in the code face at the code's size. */
    public record jk_cell() implements CssClass<JsonTreeStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Code.class, Type.Face.class), of(Code.class, Type.Scale.class)); }
        @Override public String body() { return """
                flex: 1 1 auto;
                min-width: 0;
                overflow: hidden;
                white-space: nowrap;
                text-overflow: ellipsis;
                """;
        }
    }

    /** The current row's cell: weighted, as the stock cell's is. */
    public record jk_cell_current() implements CssClass<JsonTreeStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Label.class, Type.Weight.class)); }
        @Override public String body() { return ""; }
    }

    /** A member's name. */
    public record jk_key() implements CssClass<JsonTreeStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Body.class, Color.Ink.class)); }
        @Override public String body() { return ""; }
    }

    /** An element's index: quieter than a name, since it is a position. */
    public record jk_index() implements CssClass<JsonTreeStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Muted.class, Color.Ink.class)); }
        @Override public String body() { return ""; }
    }

    /** The colon. */
    public record jk_punct() implements CssClass<JsonTreeStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Muted.class, Color.Ink.class)); }
        @Override public String body() { return ""; }
    }

    /** A string, quoted: the primary ink. */
    public record jk_string() implements CssClass<JsonTreeStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Primary.class, Color.Ink.class)); }
        @Override public String body() { return ""; }
    }

    /** A number. */
    public record jk_number() implements CssClass<JsonTreeStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Body.class, Color.Ink.class)); }
        @Override public String body() { return ""; }
    }

    /** true, false and null: the kicker's ink, and italic. */
    public record jk_literal() implements CssClass<JsonTreeStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Kicker.class, Color.Ink.class)); }
        @Override public String body() { return """
                font-style: italic;
                """;
        }
    }

    /** A container, printed as a count: {3}, [5]. */
    public record jk_container() implements CssClass<JsonTreeStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Muted.class, Color.Ink.class)); }
        @Override public String body() { return ""; }
    }

    @Override public List<CssClass<JsonTreeStyles>> cssClasses() {
        return List.of(new jk_cell(), new jk_cell_current(), new jk_key(), new jk_index(), new jk_punct(),
                       new jk_string(), new jk_number(), new jk_literal(), new jk_container());
    }
}
