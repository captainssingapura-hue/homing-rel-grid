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

/** The bench widgets' chrome: a column root, a scrolling host, a hint, a button bar, a readout. */
public record WorkbenchStyles() implements CssGroup<WorkbenchStyles> {

    public static final WorkbenchStyles INSTANCE = new WorkbenchStyles();

    public record wb_root() implements CssClass<WorkbenchStyles> {
        @Override public String body() { return """
                display: flex;
                flex-direction: column;
                height: 100%;
                width: 100%;
                min-height: 0;
                padding: 10px;
                box-sizing: border-box;
                gap: 8px;
                """;
        }
    }

    /**
     * The scrollport — flex:1 so it takes the pane's leftover height and scrolls.
     * A floor, because the grid's copy panel is centred on what this shows of
     * the table and clipped by it: below about 240px a panel that can hold
     * anything no longer fits, which is the host's geometry and not the grid's.
     */
    public record wb_host() implements CssClass<WorkbenchStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Raised.class, Color.Edge.class), of(Raised.class, Shape.Rule.class), of(Raised.class, Shape.Corner.class)); }
        @Override public String body() { return """
                flex: 1;
                min-height: 240px;
                overflow: auto;
                """;
        }
    }

    /**
     * THE FRAME around a scrollport: a non-scrolling wrapper the port fills, so
     * the grid's frame — worn here, with its light — goes round the scrollbar
     * and not inside it. The grid inside is told to draw no frame of its own.
     */
    public record wb_frame() implements CssClass<WorkbenchStyles> {
        @Override public String body() { return """
                position: relative;
                flex: 1;
                display: flex;
                flex-direction: column;
                min-height: 240px;
                """;
        }
    }

    /** The scrollport inside a frame: the port scrolls, the frame is lit around it. */
    public record wb_port() implements CssClass<WorkbenchStyles> {
        @Override public String body() { return """
                flex: 1;
                min-height: 0;
                overflow: auto;
                """;
        }
    }

    public record wb_hint() implements CssClass<WorkbenchStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Body.class, Type.Face.class), of(Caption.class, Type.Scale.class), of(Muted.class, Color.Ink.class)); }
        @Override public String body() { return "";
        }
    }

    public record wb_bar() implements CssClass<WorkbenchStyles> {
        @Override public String body() { return """
                display: flex;
                gap: 8px;
                flex-wrap: wrap;
                """;
        }
    }

    public record wb_btn() implements CssClass<WorkbenchStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Body.class, Type.Face.class), of(Caption.class, Type.Scale.class), of(Raised.class, Color.Surface.class), of(Body.class, Color.Ink.class), of(Raised.class, Color.Edge.class), of(Raised.class, Shape.Rule.class), of(Control.class, Shape.Corner.class)); }
        @Override public String body() { return """
                padding: 4px 10px;
                cursor: pointer;
                """;
        }
    }

    /**
     * The Han Article's host: exactly as wide as the columns shown — nine
     * squares, and a half-square on either side when a row uses it — so the
     * table's 100% is their sum and every column gets exactly its width. The
     * width itself is set by the widget, which knows which columns are shown.
     * Content-tall rather than a scrollport — an article grows downward, and
     * the pane scrolls.
     */
    public record wb_han_host() implements CssClass<WorkbenchStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Raised.class, Color.Edge.class), of(Raised.class, Shape.Rule.class), of(Raised.class, Shape.Corner.class)); }
        @Override public String body() { return """
                flex: 0 1 auto;
                min-height: 0;
                align-self: flex-start;
                max-width: 100%;
                overflow: auto;
                """;
        }
    }

    /** A host exactly the table's size — the endless table's window: nothing scrolls natively, the wheel is the window's. */
    public record wb_window_host() implements CssClass<WorkbenchStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Raised.class, Color.Edge.class), of(Raised.class, Shape.Rule.class), of(Raised.class, Shape.Corner.class)); }
        @Override public String body() { return """
                flex: 0 0 auto;
                align-self: flex-start;
                max-width: 100%;
                overflow-x: auto;
                overflow-y: hidden;
                """;
        }
    }

    /** The Han Article's editor: plain text, as wide as the squares below it, in the same face. */
    public record wb_han_text() implements CssClass<WorkbenchStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Raised.class, Color.Edge.class), of(Raised.class, Shape.Rule.class), of(Raised.class, Shape.Corner.class), of(Raised.class, Color.Surface.class), of(Body.class, Color.Ink.class)); }
        @Override public String body() { return """
                flex: 0 0 auto;
                align-self: flex-start;
                width: 434px;
                max-width: 100%;
                box-sizing: border-box;
                padding: 8px 10px;
                font: 18px/1.6 'Noto Serif CJK SC', 'Source Han Serif SC', 'Songti SC', 'SimSun', 'PMingLiU', serif;
                resize: vertical;
                outline: none;
                """;
        }
    }

    /** The JSON Tree bench's input: a monospace textarea that fills the pane and scrolls. */
    public record wb_json_text() implements CssClass<WorkbenchStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Raised.class, Color.Edge.class), of(Raised.class, Shape.Rule.class), of(Raised.class, Shape.Corner.class), of(Raised.class, Color.Surface.class), of(Body.class, Color.Ink.class), of(Code.class, Type.Face.class), of(Code.class, Type.Scale.class)); }
        @Override public String body() { return """
                flex: 1 1 auto;
                min-height: 120px;
                width: 100%;
                box-sizing: border-box;
                padding: 8px 10px;
                resize: none;
                outline: none;
                white-space: pre;
                overflow: auto;
                """;
        }
    }

    /** A one-line text input on a bench's bar — a pointer to reveal. */
    public record wb_input() implements CssClass<WorkbenchStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Raised.class, Color.Edge.class), of(Raised.class, Shape.Rule.class), of(Control.class, Shape.Corner.class), of(Raised.class, Color.Surface.class), of(Body.class, Color.Ink.class), of(Code.class, Type.Face.class), of(Code.class, Type.Scale.class)); }
        @Override public String body() { return """
                padding: 3px 6px;
                min-width: 160px;
                """;
        }
    }

    public record wb_status() implements CssClass<WorkbenchStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Code.class, Type.Face.class), of(Code.class, Type.Scale.class), of(Muted.class, Color.Ink.class)); }
        @Override public String body() { return """
                white-space: pre-line;
                max-height: 90px;
                overflow: auto;
                """;
        }
    }

    @Override
    public List<CssClass<WorkbenchStyles>> cssClasses() {
        return List.of(new wb_root(), new wb_host(), new wb_frame(), new wb_port(), new wb_han_host(), new wb_window_host(), new wb_han_text(), new wb_hint(), new wb_bar(), new wb_btn(), new wb_json_text(), new wb_input(), new wb_status());
    }

}
