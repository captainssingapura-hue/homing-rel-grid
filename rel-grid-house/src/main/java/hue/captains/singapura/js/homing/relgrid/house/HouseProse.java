package hue.captains.singapura.js.homing.relgrid.house;

import hue.captains.singapura.js.homing.design.Target;
import hue.captains.singapura.js.homing.design.color.Paint;
import hue.captains.singapura.js.homing.design.semantic.DesignStyle;
import hue.captains.singapura.js.homing.design.local.ConcreteComponent;
import hue.captains.singapura.js.homing.design.local.LocalStyle;
import hue.captains.singapura.js.homing.design.semantic.Drawing;
import hue.captains.singapura.js.homing.design.semantic.Drawn;
import hue.captains.singapura.js.homing.relgrid.frame.GridFrame;
import hue.captains.singapura.js.homing.ui.styles.BaseScheme;
import hue.captains.singapura.js.homing.ui.styles.BaseColours.Border;
import hue.captains.singapura.js.homing.ui.styles.BaseColours.Clear;
import hue.captains.singapura.js.homing.ui.styles.BaseColours.TextFaded;
import hue.captains.singapura.js.homing.ui.styles.NeoBrutalism;
import hue.captains.singapura.js.homing.ui.styles.NeoFuturism;
import hue.captains.singapura.js.homing.ui.styles.Sketchy;

import java.util.List;

/**
 * The house's local style of a document's table (RFC 0050 · Episode 2 — Appendix: The Frame): the grid's frame drawn as the
 * house draws a table in a document. Its lines are its own, never its cells': a cell is a container, its room and no line
 * (the owner, 15 Oct). Each row ruled underneath in the hairline - heavier in the styles that draw heavy lines - its head
 * unruled but where a style rules a table's head; its gutter's numbers in the faded ink, at a caption's measure; it has
 * no edge and no ground, and stands on the page as a document's table does. What a document has no word for - where the keys are, a
 * selection, the boxes laid over a slot - it draws as the house's grid does ({@link Drawing.As}); the cursor shows only
 * while the keys are with the table, so at rest it reads as prose.
 */
public record HouseProse() implements LocalStyle<BaseScheme, GridFrame> {

    public static final HouseProse INSTANCE = new HouseProse();

    @Override public BaseScheme scheme() { return BaseScheme.INSTANCE; }

    /** A concrete leaf of every leaf of the frame. */
    @Override public List<ConcreteComponent<?, GridFrame, ?>> concretes() {
        return List.of(Edge.INSTANCE, Lattice.INSTANCE, Cursor.INSTANCE, HeldCursor.INSTANCE, LentCursor.INSTANCE, Selection.INSTANCE,
                       Header.INSTANCE, Gutter.INSTANCE, Anchor.INSTANCE, Veil.INSTANCE, Panel.INSTANCE, Merged.INSTANCE, Guide.INSTANCE,
                       CopiedArea.INSTANCE, CopiedBorder.INSTANCE);
    }

    private static final Target.Color.Background GROUND = Target.Color.Background.INSTANCE;
    private static final Target.Shape.Border BORDER = Target.Shape.Border.INSTANCE;
    private static final Target.Color.Border BORDER_COLOUR = Target.Color.Border.INSTANCE;
    private static final Target.Shape.Outline RING = Target.Shape.Outline.INSTANCE;
    private static final Target.Color.Outline RING_COLOUR = Target.Color.Outline.INSTANCE;

    /** A row's rule, along its foot, in the style worn: two pixels where the style draws heavy lines, else a hairline's one. */
    private static String rowRule(DesignStyle<?> s) {
        return foot(s instanceof NeoBrutalism || s instanceof Sketchy ? "2px" : "1px");
    }

    /** The head's rule: none, but where a style rules a table's head - neo-futurism's one pixel, sketchy's two. */
    private static String headRule(DesignStyle<?> s) {
        return s instanceof Sketchy ? foot("2px") : s instanceof NeoFuturism ? foot("1px") : "border-width: 0;";
    }

    private static String foot(String width) { return "border-width: 0 0 " + width + " 0; border-style: solid;"; }

    /** Its lines' colour: the hairline. */
    private static final Paint.With<BaseScheme, Target.Color.Border> HAIRLINE = new Paint.With<>(new Border());

    /** No edge, no ground, no corner, no light: a document's table stands on the page. */
    public record Edge() implements GridFrame.GridEdge.Concrete<HouseProse>, Drawn {
        public static final Edge INSTANCE = new Edge();
        @Override public List<Drawing<?>> drawings(DesignStyle<?> style) {
            return List.of(new Drawing.None<>(GROUND), new Drawing.None<>(BORDER), new Drawing.None<>(BORDER_COLOUR),
                           new Drawing.None<>(RING), new Drawing.None<>(RING_COLOUR), new Drawing.None<>(Target.Shape.Corner.INSTANCE));
        }
    }

    /** Every row ruled underneath, in the hairline. */
    public record Lattice() implements GridFrame.Lattice.Concrete<HouseProse>, Drawn {
        public static final Lattice INSTANCE = new Lattice();
        @Override public List<Drawing<?>> drawings(DesignStyle<?> style) {
            return List.of(new Drawing.Own<>(BORDER, rowRule(style)), new Drawing.Painted<>(BORDER_COLOUR, HAIRLINE));
        }
    }

    /** The cursor while the keys are elsewhere: nothing - at rest a document's table reads as prose. */
    public record Cursor() implements GridFrame.CursorBorder.Concrete<HouseProse>, Drawn {
        public static final Cursor INSTANCE = new Cursor();
        @Override public List<Drawing<?>> drawings(DesignStyle<?> style) { return List.of(new Drawing.None<>(RING), new Drawing.None<>(RING_COLOUR)); }
    }

    /** The cursor while the table holds the keys: the house grid's. */
    public record HeldCursor() implements GridFrame.HeldCursorBorder.Concrete<HouseProse>, Drawn {
        public static final HeldCursor INSTANCE = new HeldCursor();
        @Override public List<Drawing<?>> drawings(DesignStyle<?> style) {
            return List.of(new Drawing.As<>(RING, HouseGrid.HeldCursor.INSTANCE), new Drawing.As<>(RING_COLOUR, HouseGrid.HeldCursor.INSTANCE));
        }
    }

    /** The cursor while its cell holds control: the house grid's - a document's table lends it to none, but says how. */
    public record LentCursor() implements GridFrame.LentCursorBorder.Concrete<HouseProse>, Drawn {
        public static final LentCursor INSTANCE = new LentCursor();
        @Override public List<Drawing<?>> drawings(DesignStyle<?> style) {
            return List.of(new Drawing.As<>(RING, HouseGrid.LentCursor.INSTANCE), new Drawing.As<>(RING_COLOUR, HouseGrid.LentCursor.INSTANCE));
        }
    }

    /** The selection: the house grid's wash. */
    public record Selection() implements GridFrame.SelectionFill.Concrete<HouseProse>, Drawn {
        public static final Selection INSTANCE = new Selection();
        @Override public List<Drawing<?>> drawings(DesignStyle<?> style) { return List.of(new Drawing.As<>(GROUND, HouseGrid.Selection.INSTANCE)); }
    }

    /** The header band: no ground, its foot ruled where a style rules a table's head. */
    public record Header() implements GridFrame.HeaderBand.Concrete<HouseProse>, Drawn {
        public static final Header INSTANCE = new Header();
        @Override public List<Drawing<?>> drawings(DesignStyle<?> style) {
            return List.of(new Drawing.Painted<>(GROUND, new Paint.Ground<>(new Clear())), new Drawing.Own<>(BORDER, headRule(style)),
                           new Drawing.Painted<>(BORDER_COLOUR, HAIRLINE));
        }
    }

    /** The gutter band: ruled as the head is, its numbers in the faded ink, at a caption's measure. */
    public record Gutter() implements GridFrame.GutterBand.Concrete<HouseProse>, Drawn {
        public static final Gutter INSTANCE = new Gutter();
        @Override public List<Drawing<?>> drawings(DesignStyle<?> style) {
            return List.of(new Drawing.Painted<>(GROUND, new Paint.Ground<>(new Clear())), new Drawing.Own<>(BORDER, headRule(style)),
                           new Drawing.Painted<>(BORDER_COLOUR, HAIRLINE),
                           new Drawing.Painted<>(Target.Color.Text.INSTANCE, new Paint.With<>(new TextFaded())),
                           new Drawing.Own<>(Target.Type.Scale.INSTANCE, "font-size: calc(13px * pow(1.2, var(--size))); line-height: 1.5;"));
        }
    }

    /** An open editor's box: the house grid's. */
    public record Anchor() implements GridFrame.EditorAnchor.Concrete<HouseProse>, Drawn {
        public static final Anchor INSTANCE = new Anchor();
        @Override public List<Drawing<?>> drawings(DesignStyle<?> style) {
            return List.of(new Drawing.As<>(Target.Shape.Shadow.INSTANCE, HouseGrid.Anchor.INSTANCE),
                           new Drawing.As<>(Target.Color.BoxShadow.INSTANCE, HouseGrid.Anchor.INSTANCE));
        }
    }

    /** The veil: the house grid's. */
    public record Veil() implements GridFrame.Mask.Concrete<HouseProse>, Drawn {
        public static final Veil INSTANCE = new Veil();
        @Override public List<Drawing<?>> drawings(DesignStyle<?> style) { return List.of(new Drawing.As<>(GROUND, HouseGrid.Veil.INSTANCE)); }
    }

    /** The question's plate: the house grid's. */
    public record Panel() implements GridFrame.QuestionPanel.Concrete<HouseProse>, Drawn {
        public static final Panel INSTANCE = new Panel();
        @Override public List<Drawing<?>> drawings(DesignStyle<?> style) {
            var panel = HouseGrid.Panel.INSTANCE;
            return List.of(new Drawing.As<>(GROUND, panel), new Drawing.As<>(BORDER, panel), new Drawing.As<>(BORDER_COLOUR, panel),
                           new Drawing.As<>(Target.Shape.Corner.INSTANCE, panel), new Drawing.As<>(Target.Shape.Shadow.INSTANCE, panel),
                           new Drawing.As<>(Target.Color.BoxShadow.INSTANCE, panel), new Drawing.As<>(Target.Size.Inset.INSTANCE, panel));
        }
    }

    /** A merged cell's box: ruled as a row is, on the house grid's ground, which covers the slots it reaches over. */
    public record Merged() implements GridFrame.MergeHost.Concrete<HouseProse>, Drawn {
        public static final Merged INSTANCE = new Merged();
        @Override public List<Drawing<?>> drawings(DesignStyle<?> style) {
            return List.of(new Drawing.As<>(GROUND, HouseGrid.Merged.INSTANCE), new Drawing.Own<>(BORDER, rowRule(style)),
                           new Drawing.Painted<>(BORDER_COLOUR, HAIRLINE));
        }
    }

    /** The resize guide: the house grid's. */
    public record Guide() implements GridFrame.ResizeGuide.Concrete<HouseProse>, Drawn {
        public static final Guide INSTANCE = new Guide();
        @Override public List<Drawing<?>> drawings(DesignStyle<?> style) { return List.of(new Drawing.As<>(GROUND, HouseGrid.Guide.INSTANCE)); }
    }

    /** What was last copied: a document has no word for it - the house grid's box. */
    public record CopiedArea() implements GridFrame.CopiedArea.Concrete<HouseProse>, Drawn {
        public static final CopiedArea INSTANCE = new CopiedArea();
        @Override public List<Drawing<?>> drawings(DesignStyle<?> style) {
            return List.of(new Drawing.As<>(GROUND, HouseGrid.CopiedArea.INSTANCE),
                           new Drawing.As<>(Target.Animation.Effect.INSTANCE, HouseGrid.CopiedArea.INSTANCE));
        }
    }

    /** And the line round it: the house grid's marching ants. */
    public record CopiedBorder() implements GridFrame.CopiedBorder.Concrete<HouseProse>, Drawn {
        public static final CopiedBorder INSTANCE = new CopiedBorder();
        @Override public List<Drawing<?>> drawings(DesignStyle<?> style) {
            return List.of(new Drawing.As<>(Target.Color.Stroke.INSTANCE, HouseGrid.CopiedBorder.INSTANCE),
                           new Drawing.As<>(Target.Animation.Effect.INSTANCE, HouseGrid.CopiedBorder.INSTANCE));
        }
    }
}