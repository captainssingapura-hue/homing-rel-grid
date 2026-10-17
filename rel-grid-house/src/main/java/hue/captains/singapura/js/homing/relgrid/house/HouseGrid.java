package hue.captains.singapura.js.homing.relgrid.house;

import hue.captains.singapura.js.homing.design.AnimationEffect;
import hue.captains.singapura.js.homing.design.State;
import hue.captains.singapura.js.homing.design.Target;
import hue.captains.singapura.js.homing.design.color.Paint;
import hue.captains.singapura.js.homing.design.color.SemanticColor;
import hue.captains.singapura.js.homing.design.semantic.Answer;
import hue.captains.singapura.js.homing.design.semantic.DesignStyle;
import hue.captains.singapura.js.homing.design.local.ConcreteComponent;
import hue.captains.singapura.js.homing.design.local.LocalStyle;
import hue.captains.singapura.js.homing.design.semantic.Drawing;
import hue.captains.singapura.js.homing.design.semantic.Drawn;
import hue.captains.singapura.js.homing.relgrid.frame.GridFrame;
import hue.captains.singapura.js.homing.ui.styles.BaseScheme;
import hue.captains.singapura.js.homing.ui.styles.BaseColours;
import hue.captains.singapura.js.homing.ui.styles.BaseColours.Accent;
import hue.captains.singapura.js.homing.ui.styles.BaseColours.AccentEdge;
import hue.captains.singapura.js.homing.ui.styles.BaseColours.AccentTint;
import hue.captains.singapura.js.homing.ui.styles.BaseColours.AccentTintFaint;
import hue.captains.singapura.js.homing.ui.styles.BaseColours.AccentWashStrong;
import hue.captains.singapura.js.homing.ui.styles.BaseColours.Border;
import hue.captains.singapura.js.homing.ui.styles.BaseColours.Clear;
import hue.captains.singapura.js.homing.ui.styles.BaseColours.Raised;
import hue.captains.singapura.js.homing.ui.styles.BaseColours.Text;
import hue.captains.singapura.js.homing.ui.styles.HouseEffects;

import java.util.List;

/**
 * The house's local style of the grid's frame (RFC 0050 · Episode 2 — Appendix: The Frame; RFC 0067, appendix Local
 * Styles): a sheet's lines, every rule drawn, in every house style - one sheet a style, the same in every palette. The grid stands on the page's ground inside a hairline edge that lights in
 * the accent while the keys are with it; the lattice rules each cell off at its end and its foot; the cursor is a ring,
 * quiet while the keys are elsewhere, in the accent while they act on it, broken while its cell holds control; the
 * selection a wash of the accent; the header and the gutter bands on the raised layer, the header ruled heavier; what
 * was last copied, marching ants in the accent round it - and, in the styles of light and motion, a band of the accent
 * sweeping across it.
 *
 * <p>Every colour is the house's own (RFC 0067, D43): an extension invents none, and gives its own meanings to the
 * house's colours - never a value, so the grid follows the palette worn, its palettable colours the palette's variables.</p>
 *
 * <p>The out-of-the-box local style: a grid a page makes without naming another draws in it. A document's table, which
 * rules its rows only, is a local style of its own ({@link HouseProse}).</p>
 */
public record HouseGrid() implements LocalStyle<BaseScheme, GridFrame> {

    public static final HouseGrid INSTANCE = new HouseGrid();

    @Override public BaseScheme scheme() { return BaseScheme.INSTANCE; }

    /** A concrete leaf of every leaf of the frame. */
    @Override public List<ConcreteComponent<?, GridFrame, ?>> concretes() {
        return List.of(Edge.INSTANCE, Lattice.INSTANCE, Cursor.INSTANCE, HeldCursor.INSTANCE, LentCursor.INSTANCE, Selection.INSTANCE,
                       Header.INSTANCE, Gutter.INSTANCE, Anchor.INSTANCE, Veil.INSTANCE, Panel.INSTANCE, Merged.INSTANCE, Guide.INSTANCE,
                       CopiedArea.INSTANCE, CopiedBorder.INSTANCE);
    }

    private static final HouseGridLook LOOK = HouseGridLook.INSTANCE;

    private static final Target.Color.Background GROUND = Target.Color.Background.INSTANCE;
    private static final Target.Shape.Border BORDER = Target.Shape.Border.INSTANCE;
    private static final Target.Color.Border BORDER_COLOUR = Target.Color.Border.INSTANCE;
    private static final Target.Shape.Outline RING = Target.Shape.Outline.INSTANCE;
    private static final Target.Color.Outline RING_COLOUR = Target.Color.Outline.INSTANCE;

    /** A ground said whole: a colour, and no ramp nor mark over it. */
    private static Paint.Ground<BaseScheme> ground(SemanticColor<? super BaseScheme> colour) { return new Paint.Ground<>(colour); }

    /** The box: the page's ground, a hairline edge drawn inside it, lit in the accent while the keys are with the grid. */
    public record Edge() implements GridFrame.GridEdge.Concrete<HouseGrid>, Drawn {
        public static final Edge INSTANCE = new Edge();
        @Override public List<Drawing<?>> drawings(DesignStyle<?> style) {
            String rule = LOOK.rule(style);
            return List.of(new Drawing.Painted<>(GROUND, ground(new BaseColours.Ground())),
                           new Drawing.None<>(BORDER),
                           new Drawing.None<>(BORDER_COLOUR),
                           new Drawing.Own<>(RING, "outline-width: " + rule + "; outline-style: solid; outline-offset: -" + rule + ";"),
                           new Drawing.Painted<>(RING_COLOUR, new Answer.Painted<BaseScheme, Target.Color.Outline>(new Paint.With<>(new Border()))
                                   .at(State.CANDIDATE, new Paint.With<>(new AccentEdge()))
                                   .at(State.LENT, new Paint.With<>(new Accent()))
                                   .at(State.HELD, new Paint.With<>(new Accent()))),
                           new Drawing.Own<>(Target.Shape.Corner.INSTANCE, "border-radius: " + LOOK.corner(style) + ";"));
        }
    }

    /** Every rule: each cell ruled off at its end and at its foot, in the hairline. */
    public record Lattice() implements GridFrame.Lattice.Concrete<HouseGrid>, Drawn {
        public static final Lattice INSTANCE = new Lattice();
        @Override public List<Drawing<?>> drawings(DesignStyle<?> style) {
            String rule = LOOK.rule(style);
            return List.of(new Drawing.Own<>(BORDER, "border-width: 0 " + rule + " " + rule + " 0; border-style: solid;"),
                           new Drawing.Painted<>(BORDER_COLOUR, new Paint.With<>(new Border())));
        }
    }

    /** The cursor while the keys are elsewhere: a quiet ring, the text a little into the hairline. */
    public record Cursor() implements GridFrame.CursorBorder.Concrete<HouseGrid>, Drawn {
        public static final Cursor INSTANCE = new Cursor();
        @Override public List<Drawing<?>> drawings(DesignStyle<?> style) {
            return List.of(new Drawing.Own<>(RING, "outline-width: 2px; outline-style: solid; outline-offset: -2px;"),
                           new Drawing.Painted<>(RING_COLOUR, new Paint.With<>(new BaseColours.GrooveEdge())));
        }
    }

    /** The cursor while the grid holds the keys: the ring in the accent. */
    public record HeldCursor() implements GridFrame.HeldCursorBorder.Concrete<HouseGrid>, Drawn {
        public static final HeldCursor INSTANCE = new HeldCursor();
        @Override public List<Drawing<?>> drawings(DesignStyle<?> style) {
            return List.of(new Drawing.Own<>(RING, "outline-width: 2px; outline-style: solid; outline-offset: -2px;"),
                           new Drawing.Painted<>(RING_COLOUR, new Paint.With<>(new Accent())));
        }
    }

    /** The cursor while its cell holds control: the accent's ring, broken. */
    public record LentCursor() implements GridFrame.LentCursorBorder.Concrete<HouseGrid>, Drawn {
        public static final LentCursor INSTANCE = new LentCursor();
        @Override public List<Drawing<?>> drawings(DesignStyle<?> style) {
            return List.of(new Drawing.Own<>(RING, "outline-width: 2px; outline-style: dashed; outline-offset: -2px;"),
                           new Drawing.Painted<>(RING_COLOUR, new Paint.With<>(new Accent())));
        }
    }

    /** The selection: a wash of the accent while a slot is selected, but the cursor's own slot - its ring shows it, as a sheet's active cell is. */
    public record Selection() implements GridFrame.SelectionFill.Concrete<HouseGrid>, Drawn {
        public static final Selection INSTANCE = new Selection();
        @Override public List<Drawing<?>> drawings(DesignStyle<?> style) {
            return List.of(new Drawing.Painted<>(GROUND, new Answer.Painted<BaseScheme, Target.Color.Background>(ground(new Clear()))
                    .at(State.SELECTED, ground(new BaseColours.AccentWashSoft()))
                    .at(State.CURRENT, ground(new Clear()))));
        }
    }

    /** The header band: the raised layer, its foot ruled heavier, the text's way, so the band heads its columns. */
    public record Header() implements GridFrame.HeaderBand.Concrete<HouseGrid>, Drawn {
        public static final Header INSTANCE = new Header();
        @Override public List<Drawing<?>> drawings(DesignStyle<?> style) {
            return List.of(new Drawing.Painted<>(GROUND, ground(new Raised())),
                           new Drawing.Own<>(BORDER, "border-width: 0 " + LOOK.rule(style) + " " + LOOK.headRule(style) + " 0; border-style: solid;"),
                           new Drawing.Painted<>(BORDER_COLOUR, new Paint.With<>(new BaseColours.GrooveEdge())));
        }
    }

    /** The gutter band: the raised layer, its numbers small and muted - full where the cursor's row is, washed where selected. */
    public record Gutter() implements GridFrame.GutterBand.Concrete<HouseGrid>, Drawn {
        public static final Gutter INSTANCE = new Gutter();
        @Override public List<Drawing<?>> drawings(DesignStyle<?> style) {
            String rule = LOOK.rule(style);
            return List.of(new Drawing.Painted<>(GROUND, new Answer.Painted<BaseScheme, Target.Color.Background>(ground(new Raised()))
                                   .at(State.SELECTED, ground(new AccentTint()))
                                   .at(State.CURRENT, ground(new AccentTintFaint()))),
                           new Drawing.Own<>(BORDER, "border-width: 0 " + rule + " " + rule + " 0; border-style: solid;"),
                           new Drawing.Painted<>(BORDER_COLOUR, new Paint.With<>(new Border())),
                           new Drawing.Painted<>(Target.Color.Text.INSTANCE, new Answer.Painted<BaseScheme, Target.Color.Text>(new Paint.With<>(new BaseColours.TextFaded()))
                                   .at(State.CURRENT, new Paint.With<>(new Text()))),
                           new Drawing.Own<>(Target.Type.Scale.INSTANCE, "font-size: 0.85em; line-height: inherit;"));
        }
    }

    /** An open editor's box: lifted off the grid by a soft cast shadow. */
    public record Anchor() implements GridFrame.EditorAnchor.Concrete<HouseGrid>, Drawn {
        public static final Anchor INSTANCE = new Anchor();
        @Override public List<Drawing<?>> drawings(DesignStyle<?> style) {
            return List.of(new Drawing.Own<>(Target.Shape.Shadow.INSTANCE, "box-shadow: 0 6px 18px -6px var(--box-shadow-cast);"),
                           new Drawing.Painted<>(Target.Color.BoxShadow.INSTANCE, new Paint.Cast<>(new BaseColours.ShadeDense())));
        }
    }

    /** The veil: the page's ground, half through. */
    public record Veil() implements GridFrame.Mask.Concrete<HouseGrid>, Drawn {
        public static final Veil INSTANCE = new Veil();
        @Override public List<Drawing<?>> drawings(DesignStyle<?> style) { return List.of(new Drawing.Painted<>(GROUND, ground(new BaseColours.Sheen()))); }
    }

    /** The question's plate: the raised layer in a hairline, rounded as the style rounds, cast over the veil, with room inside. */
    public record Panel() implements GridFrame.QuestionPanel.Concrete<HouseGrid>, Drawn {
        public static final Panel INSTANCE = new Panel();
        @Override public List<Drawing<?>> drawings(DesignStyle<?> style) {
            return List.of(new Drawing.Painted<>(GROUND, ground(new Raised())),
                           new Drawing.Own<>(BORDER, "border-width: " + LOOK.rule(style) + "; border-style: solid;"),
                           new Drawing.Painted<>(BORDER_COLOUR, new Paint.With<>(new Border())),
                           new Drawing.Own<>(Target.Shape.Corner.INSTANCE, "border-radius: " + LOOK.corner(style) + ";"),
                           new Drawing.Own<>(Target.Shape.Shadow.INSTANCE, "box-shadow: 0 12px 32px -12px var(--box-shadow-cast);"),
                           new Drawing.Painted<>(Target.Color.BoxShadow.INSTANCE, new Paint.Cast<>(new BaseColours.ShadeDense())),
                           new Drawing.Own<>(Target.Size.Inset.INSTANCE, "padding-block: 16px; padding-inline: 20px;"));
        }
    }

    /** A merged cell's box: the page's ground, ruled as a cell is, washed while selected but not while the cursor is on it - its rule in the accent then. */
    public record Merged() implements GridFrame.MergeHost.Concrete<HouseGrid>, Drawn {
        public static final Merged INSTANCE = new Merged();
        @Override public List<Drawing<?>> drawings(DesignStyle<?> style) {
            return List.of(new Drawing.Painted<>(GROUND, new Answer.Painted<BaseScheme, Target.Color.Background>(ground(new BaseColours.Ground()))
                                   .at(State.SELECTED, ground(new BaseColours.AccentTintFaint()))
                                   .at(State.CURRENT, ground(new BaseColours.Ground()))),
                           new Drawing.Own<>(BORDER, "border-width: 0 " + LOOK.rule(style) + " " + LOOK.rule(style) + " 0; border-style: solid;"),
                           new Drawing.Painted<>(BORDER_COLOUR, new Answer.Painted<BaseScheme, Target.Color.Border>(new Paint.With<>(new Border()))
                                   .at(State.CURRENT, new Paint.With<>(new Accent()))));
        }
    }

    /** The resize guide: a line of the accent. */
    public record Guide() implements GridFrame.ResizeGuide.Concrete<HouseGrid>, Drawn {
        public static final Guide INSTANCE = new Guide();
        @Override public List<Drawing<?>> drawings(DesignStyle<?> style) { return List.of(new Drawing.Painted<>(GROUND, ground(new Accent()))); }
    }

    /**
     * A copied block's box: no ground of its own, the accent's tint the ramp an effect sweeps - a band of it moving
     * across in the styles of light and motion, nothing moving in the others.
     */
    public record CopiedArea() implements GridFrame.CopiedArea.Concrete<HouseGrid>, Drawn {
        public static final CopiedArea INSTANCE = new CopiedArea();
        @Override public List<Drawing<?>> drawings(DesignStyle<?> style) {
            return List.of(new Drawing.Painted<>(GROUND, new Paint.Drawn<>(new Clear(), new Clear(), new AccentWashStrong(), new Clear(), new Clear())),
                           new Drawing.Animated(LOOK.sweeps(style) ? HouseEffects.MovingGradient.INSTANCE : AnimationEffect.None.INSTANCE));
        }
    }

    /** The line round a copied block: marching ants, in the accent - a spreadsheet's mark of what was copied. */
    public record CopiedBorder() implements GridFrame.CopiedBorder.Concrete<HouseGrid>, Drawn {
        public static final CopiedBorder INSTANCE = new CopiedBorder();
        @Override public List<Drawing<?>> drawings(DesignStyle<?> style) {
            return List.of(new Drawing.Painted<>(Target.Color.Stroke.INSTANCE, new Paint.With<>(new Accent())),
                           new Drawing.Animated(HouseEffects.MarchingAnts.INSTANCE));
        }
    }
}
