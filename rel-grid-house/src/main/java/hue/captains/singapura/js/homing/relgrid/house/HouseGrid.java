package hue.captains.singapura.js.homing.relgrid.house;

import hue.captains.singapura.js.homing.component.taxonomy.Design;
import hue.captains.singapura.js.homing.design.AnimationEffect;
import hue.captains.singapura.js.homing.design.State;
import hue.captains.singapura.js.homing.design.Target;
import hue.captains.singapura.js.homing.design.color.ColorScheme;
import hue.captains.singapura.js.homing.design.semantic.Answer;
import hue.captains.singapura.js.homing.design.semantic.Drawing;
import hue.captains.singapura.js.homing.design.semantic.Drawn;
import hue.captains.singapura.js.homing.design.semantic.Dressed;
import hue.captains.singapura.js.homing.relgrid.frame.GridFrame;
import hue.captains.singapura.js.homing.ui.styles.HouseEffects;
import hue.captains.singapura.js.homing.ui.styles.Seeds;

import java.util.List;

/**
 * The house's design of the grid's frame (RFC 0050 · Episode 2 — Appendix: The Frame): a sheet's lines, every rule
 * drawn, in every house style and palette. The grid stands on the page's ground inside a hairline edge that lights in
 * the accent while the keys are with it; the lattice rules each cell off at its end and its foot; the cursor is a ring,
 * quiet while the keys are elsewhere, in the accent while they act on it, broken while its cell holds control; the
 * selection a wash of the accent; the header and the gutter bands on the raised layer, the header ruled heavier; what
 * was last copied, marching ants in the accent round it - and, in the styles of light and motion, a band of the accent
 * sweeping across it.
 *
 * <p>The out-of-the-box design: a grid a page makes without naming another draws in it. A document's table, which
 * rules its rows only, is a design of its own.</p>
 */
public record HouseGrid() implements Design<ColorScheme.Base, GridFrame> {

    public static final HouseGrid INSTANCE = new HouseGrid();

    private static final HouseGridLook LOOK = HouseGridLook.INSTANCE;

    private static final Target.Color.Background GROUND = Target.Color.Background.INSTANCE;
    private static final Target.Shape.Border BORDER = Target.Shape.Border.INSTANCE;
    private static final Target.Color.Border BORDER_COLOUR = Target.Color.Border.INSTANCE;
    private static final Target.Shape.Outline RING = Target.Shape.Outline.INSTANCE;
    private static final Target.Color.Outline RING_COLOUR = Target.Color.Outline.INSTANCE;

    /** The box: the page's ground, a hairline edge drawn inside it, lit in the accent while the keys are with the grid. */
    public record Edge() implements GridFrame.GridEdge.Concrete<HouseGrid>, Drawn {
        public static final Edge INSTANCE = new Edge();
        @Override public List<Drawing<?>> drawings(Dressed theme) {
            Seeds s = LOOK.seeds(theme);
            String rule = LOOK.rule(theme);
            return List.of(new Drawing.Own<>(GROUND, LOOK.ground(s.ground())),
                           new Drawing.None<>(BORDER),
                           new Drawing.None<>(BORDER_COLOUR),
                           new Drawing.Own<>(RING, "outline-width: " + rule + "; outline-style: solid; outline-offset: -" + rule + ";"),
                           new Drawing.Own<>(RING_COLOUR, new Answer.Own<Target.Color.Outline>("outline-color: " + s.border() + ";")
                                   .at(State.CANDIDATE, "outline-color: " + s.accentEdge() + ";")
                                   .at(State.LENT, "outline-color: " + s.accent() + ";")
                                   .at(State.HELD, "outline-color: " + s.accent() + ";")),
                           new Drawing.Own<>(Target.Shape.Corner.INSTANCE, "border-radius: " + LOOK.corner(theme) + ";"));
        }
    }

    /** Every rule: each cell ruled off at its end and at its foot, in the hairline. */
    public record Lattice() implements GridFrame.Lattice.Concrete<HouseGrid>, Drawn {
        public static final Lattice INSTANCE = new Lattice();
        @Override public List<Drawing<?>> drawings(Dressed theme) {
            String rule = LOOK.rule(theme);
            return List.of(new Drawing.Own<>(BORDER, "border-width: 0 " + rule + " " + rule + " 0; border-style: solid;"),
                           new Drawing.Own<>(BORDER_COLOUR, "border-color: " + LOOK.seeds(theme).border() + ";"));
        }
    }

    /** The cursor while the keys are elsewhere: a quiet ring, the ink halfway to the hairline. */
    public record Cursor() implements GridFrame.CursorBorder.Concrete<HouseGrid>, Drawn {
        public static final Cursor INSTANCE = new Cursor();
        @Override public List<Drawing<?>> drawings(Dressed theme) {
            Seeds s = LOOK.seeds(theme);
            return List.of(new Drawing.Own<>(RING, "outline-width: 2px; outline-style: solid; outline-offset: -2px;"),
                           new Drawing.Own<>(RING_COLOUR, "outline-color: " + LOOK.mix(s.text(), 35, s.border()) + ";"));
        }
    }

    /** The cursor while the grid holds the keys: the ring in the accent. */
    public record HeldCursor() implements GridFrame.HeldCursorBorder.Concrete<HouseGrid>, Drawn {
        public static final HeldCursor INSTANCE = new HeldCursor();
        @Override public List<Drawing<?>> drawings(Dressed theme) {
            return List.of(new Drawing.Own<>(RING, "outline-width: 2px; outline-style: solid; outline-offset: -2px;"),
                           new Drawing.Own<>(RING_COLOUR, "outline-color: " + LOOK.seeds(theme).accent() + ";"));
        }
    }

    /** The cursor while its cell holds control: the accent's ring, broken. */
    public record LentCursor() implements GridFrame.LentCursorBorder.Concrete<HouseGrid>, Drawn {
        public static final LentCursor INSTANCE = new LentCursor();
        @Override public List<Drawing<?>> drawings(Dressed theme) {
            return List.of(new Drawing.Own<>(RING, "outline-width: 2px; outline-style: dashed; outline-offset: -2px;"),
                           new Drawing.Own<>(RING_COLOUR, "outline-color: " + LOOK.seeds(theme).accent() + ";"));
        }
    }

    /** The selection: a wash of the accent while a slot is selected, but the cursor's own slot - its ring shows it, as a sheet's active cell is. */
    public record Selection() implements GridFrame.SelectionFill.Concrete<HouseGrid>, Drawn {
        public static final Selection INSTANCE = new Selection();
        @Override public List<Drawing<?>> drawings(Dressed theme) {
            Seeds s = LOOK.seeds(theme);
            return List.of(new Drawing.Own<>(GROUND, new Answer.Own<Target.Color.Background>(LOOK.ground("transparent"))
                    .at(State.SELECTED, LOOK.ground(LOOK.mix(s.accent(), 16, "transparent")))
                    .at(State.CURRENT, LOOK.ground("transparent"))));
        }
    }

    /** The header band: the raised layer, its foot ruled heavier, the ink's way, so the band heads its columns. */
    public record Header() implements GridFrame.HeaderBand.Concrete<HouseGrid>, Drawn {
        public static final Header INSTANCE = new Header();
        @Override public List<Drawing<?>> drawings(Dressed theme) {
            Seeds s = LOOK.seeds(theme);
            return List.of(new Drawing.Own<>(GROUND, LOOK.ground(s.raised())),
                           new Drawing.Own<>(BORDER, "border-width: 0 " + LOOK.rule(theme) + " " + LOOK.headRule(theme) + " 0; border-style: solid;"),
                           new Drawing.Own<>(BORDER_COLOUR, "border-color: " + LOOK.mix(s.text(), 30, s.border()) + ";"));
        }
    }

    /** The gutter band: the raised layer, its numbers small and muted - full where the cursor's row is, washed where selected. */
    public record Gutter() implements GridFrame.GutterBand.Concrete<HouseGrid>, Drawn {
        public static final Gutter INSTANCE = new Gutter();
        @Override public List<Drawing<?>> drawings(Dressed theme) {
            Seeds s = LOOK.seeds(theme);
            String rule = LOOK.rule(theme);
            return List.of(new Drawing.Own<>(GROUND, new Answer.Own<Target.Color.Background>(LOOK.ground(s.raised()))
                                   .at(State.SELECTED, LOOK.ground(LOOK.mix(s.accent(), 22, s.raised())))
                                   .at(State.CURRENT, LOOK.ground(LOOK.mix(s.accent(), 12, s.raised())))),
                           new Drawing.Own<>(BORDER, "border-width: 0 " + rule + " " + rule + " 0; border-style: solid;"),
                           new Drawing.Own<>(BORDER_COLOUR, "border-color: " + s.border() + ";"),
                           new Drawing.Own<>(Target.Color.Text.INSTANCE, new Answer.Own<Target.Color.Text>("color: " + LOOK.mix(s.text(), 60, "transparent") + ";")
                                   .at(State.CURRENT, "color: " + s.text() + ";")),
                           new Drawing.Own<>(Target.Type.Scale.INSTANCE, "font-size: 0.85em; line-height: inherit;"));
        }
    }

    /** An open editor's box: lifted off the grid by a soft cast shadow. */
    public record Anchor() implements GridFrame.EditorAnchor.Concrete<HouseGrid>, Drawn {
        public static final Anchor INSTANCE = new Anchor();
        @Override public List<Drawing<?>> drawings(Dressed theme) {
            return List.of(new Drawing.Own<>(Target.Shape.Shadow.INSTANCE, "box-shadow: 0 6px 18px -6px var(--box-shadow-cast);"),
                           new Drawing.Own<>(Target.Color.BoxShadow.INSTANCE, LOOK.cast(LOOK.mix(LOOK.seeds(theme).inverted(), 45, "transparent"))));
        }
    }

    /** The veil: the page's ground, half through. */
    public record Veil() implements GridFrame.Mask.Concrete<HouseGrid>, Drawn {
        public static final Veil INSTANCE = new Veil();
        @Override public List<Drawing<?>> drawings(Dressed theme) {
            return List.of(new Drawing.Own<>(GROUND, LOOK.ground(LOOK.mix(LOOK.seeds(theme).ground(), 60, "transparent"))));
        }
    }

    /** The question's plate: the raised layer in a hairline, rounded as the style rounds, cast over the veil, with room inside. */
    public record Panel() implements GridFrame.QuestionPanel.Concrete<HouseGrid>, Drawn {
        public static final Panel INSTANCE = new Panel();
        @Override public List<Drawing<?>> drawings(Dressed theme) {
            Seeds s = LOOK.seeds(theme);
            return List.of(new Drawing.Own<>(GROUND, LOOK.ground(s.raised())),
                           new Drawing.Own<>(BORDER, "border-width: " + LOOK.rule(theme) + "; border-style: solid;"),
                           new Drawing.Own<>(BORDER_COLOUR, "border-color: " + s.border() + ";"),
                           new Drawing.Own<>(Target.Shape.Corner.INSTANCE, "border-radius: " + LOOK.corner(theme) + ";"),
                           new Drawing.Own<>(Target.Shape.Shadow.INSTANCE, "box-shadow: 0 12px 32px -12px var(--box-shadow-cast);"),
                           new Drawing.Own<>(Target.Color.BoxShadow.INSTANCE, LOOK.cast(LOOK.mix(s.inverted(), 45, "transparent"))),
                           new Drawing.Own<>(Target.Size.Inset.INSTANCE, "padding-block: 16px; padding-inline: 20px;"));
        }
    }

    /** A merged cell's box: the page's ground, ruled as a cell is, washed while selected but not while the cursor is on it - its rule in the accent then. */
    public record Merged() implements GridFrame.MergeHost.Concrete<HouseGrid>, Drawn {
        public static final Merged INSTANCE = new Merged();
        @Override public List<Drawing<?>> drawings(Dressed theme) {
            Seeds s = LOOK.seeds(theme);
            return List.of(new Drawing.Own<>(GROUND, new Answer.Own<Target.Color.Background>(LOOK.ground(s.ground()))
                                   .at(State.SELECTED, LOOK.ground(LOOK.mix(s.accent(), 16, s.ground())))
                                   .at(State.CURRENT, LOOK.ground(s.ground()))),
                           new Drawing.Own<>(BORDER, "border-width: 0 " + LOOK.rule(theme) + " " + LOOK.rule(theme) + " 0; border-style: solid;"),
                           new Drawing.Own<>(BORDER_COLOUR, new Answer.Own<Target.Color.Border>("border-color: " + s.border() + ";")
                                   .at(State.CURRENT, "border-color: " + s.accent() + ";")));
        }
    }

    /** The resize guide: a line of the accent. */
    public record Guide() implements GridFrame.ResizeGuide.Concrete<HouseGrid>, Drawn {
        public static final Guide INSTANCE = new Guide();
        @Override public List<Drawing<?>> drawings(Dressed theme) {
            return List.of(new Drawing.Own<>(GROUND, LOOK.ground(LOOK.seeds(theme).accent())));
        }
    }

    /**
     * A copied block's box: no ground of its own, the accent's tint the ramp an effect sweeps - a band of it moving
     * across in the styles of light and motion, nothing moving in the others.
     */
    public record CopiedArea() implements GridFrame.CopiedArea.Concrete<HouseGrid>, Drawn {
        public static final CopiedArea INSTANCE = new CopiedArea();
        @Override public List<Drawing<?>> drawings(Dressed theme) {
            Seeds s = LOOK.seeds(theme);
            return List.of(new Drawing.Own<>(GROUND, LOOK.ramp(LOOK.mix(s.accent(), 28, "transparent"))),
                           new Drawing.Animated(LOOK.sweeps(theme) ? HouseEffects.MovingGradient.INSTANCE : AnimationEffect.None.INSTANCE));
        }
    }

    /** The line round a copied block: marching ants, in the accent - a spreadsheet's mark of what was copied. */
    public record CopiedBorder() implements GridFrame.CopiedBorder.Concrete<HouseGrid>, Drawn {
        public static final CopiedBorder INSTANCE = new CopiedBorder();
        @Override public List<Drawing<?>> drawings(Dressed theme) {
            return List.of(new Drawing.Own<>(Target.Color.Stroke.INSTANCE, "stroke: " + LOOK.seeds(theme).accent() + ";"),
                           new Drawing.Animated(HouseEffects.MarchingAnts.INSTANCE));
        }
    }
}