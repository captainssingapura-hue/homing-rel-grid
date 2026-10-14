package hue.captains.singapura.js.homing.relgrid.workbench;

import hue.captains.singapura.js.homing.component.taxonomy.Design;
import hue.captains.singapura.js.homing.design.Owns;
import hue.captains.singapura.js.homing.design.State;
import hue.captains.singapura.js.homing.design.Target;
import hue.captains.singapura.js.homing.design.color.ColorScheme;
import hue.captains.singapura.js.homing.design.semantic.Answer;
import hue.captains.singapura.js.homing.design.semantic.Drawing;
import hue.captains.singapura.js.homing.design.semantic.Drawn;
import hue.captains.singapura.js.homing.design.semantic.Dressed;
import hue.captains.singapura.js.homing.relgrid.frame.GridFrame;
import hue.captains.singapura.js.homing.ui.styles.HouseStyles;
import hue.captains.singapura.js.homing.ui.styles.Seeds;

import java.util.List;

/**
 * Minesweeper's Vista design of the grid's frame (RFC 0050 · Episode 2, appendix The Frame, "A worked example"): a
 * domain's design, beside the house's. The board stands in a rounded edge tinted by the palette, lit along its top, that
 * glows in the accent while the board holds the keys; the cursor a soft ring in the accent; a selection shown in the
 * air between the tiles; a question on a glass plate over a pale veil. The tiles bevel themselves - they are the game's
 * cells, with a design of their own - so no rule is drawn between them, and what the game never shows draws nothing.
 */
public record MinesweeperVista() implements Design<ColorScheme.Base, GridFrame> {

    public static final MinesweeperVista INSTANCE = new MinesweeperVista();

    private static final Target.Color.Background GROUND = Target.Color.Background.INSTANCE;
    private static final Target.Shape.Outline RING = Target.Shape.Outline.INSTANCE;
    private static final Target.Color.Outline RING_COLOUR = Target.Color.Outline.INSTANCE;

    /** The palette's seeds, in the theme worn. */
    static Seeds seeds(Dressed theme) { return HouseStyles.INSTANCE.seeds(theme.palette()); }

    /** One colour so much over another. */
    static String mix(String colour, int percent, String over) { return "color-mix(in srgb, " + colour + " " + percent + "%, " + over + ")"; }

    /** A ground said whole: a colour, and no ramp nor mark over it. */
    static String ground(String colour) {
        return "background-color: " + colour + "; --background-ramp-from: transparent; --background-ramp-via: transparent;"
                + " --background-ramp-to: transparent; --background-mark: transparent;";
    }

    /** Nothing, for every target a leaf owns: what the game never shows. */
    static List<Drawing<?>> nothing(Owns leaf) { return leaf.owns().stream().<Drawing<?>>map(t -> new Drawing.None<>(t)).toList(); }

    /** The board's edge: rounded, tinted by the palette, lit along its top and shaded below - and a glow in the accent while it holds the keys. */
    public record Edge() implements GridFrame.GridEdge.Concrete<MinesweeperVista>, Drawn {
        public static final Edge INSTANCE = new Edge();
        @Override public List<Drawing<?>> drawings(Dressed theme) {
            Seeds s = seeds(theme);
            String lit = mix(s.accent(), 18, "#ffffff"), side = mix(s.accent(), 45, s.border()), shade = mix(s.accent(), 65, s.border());
            return List.of(new Drawing.Own<>(GROUND, ground(mix(s.accent(), 12, s.raised()))),
                           new Drawing.Own<>(Target.Shape.Border.INSTANCE, "border-width: 1px; border-style: solid;"),
                           new Drawing.Own<>(Target.Color.Border.INSTANCE, "border-color: " + lit + " " + side + " " + shade + " " + side + ";"),
                           new Drawing.Own<>(RING, "outline-width: 3px; outline-style: solid; outline-offset: 2px;"),
                           new Drawing.Own<>(RING_COLOUR, new Answer.Own<Target.Color.Outline>("outline-color: transparent;")
                                   .at(State.CANDIDATE, "outline-color: " + mix(s.accent(), 25, "transparent") + ";")
                                   .at(State.LENT, "outline-color: " + mix(s.accent(), 45, "transparent") + ";")
                                   .at(State.HELD, "outline-color: " + mix(s.accent(), 45, "transparent") + ";")),
                           new Drawing.Own<>(Target.Shape.Corner.INSTANCE, "border-radius: 10px;"));
        }
    }

    /** No rule between the tiles: they bevel themselves. */
    public record Lattice() implements GridFrame.Lattice.Concrete<MinesweeperVista>, Drawn {
        public static final Lattice INSTANCE = new Lattice();
        @Override public List<Drawing<?>> drawings(Dressed theme) { return nothing(GridFrame.Lattice.INSTANCE); }
    }

    /** The cursor while the keys are elsewhere: a faint ring in the accent. */
    public record Cursor() implements GridFrame.CursorBorder.Concrete<MinesweeperVista>, Drawn {
        public static final Cursor INSTANCE = new Cursor();
        @Override public List<Drawing<?>> drawings(Dressed theme) {
            return List.of(new Drawing.Own<>(RING, "outline-width: 2px; outline-style: solid; outline-offset: -1px;"),
                           new Drawing.Own<>(RING_COLOUR, "outline-color: " + mix(seeds(theme).accent(), 35, "transparent") + ";"));
        }
    }

    /** The cursor's ring while the board holds the keys: the accent's, soft, where the next move lands. */
    static List<Drawing<?>> heldRing(Dressed theme) {
        return List.of(new Drawing.Own<>(RING, "outline-width: 2px; outline-style: solid; outline-offset: -1px;"),
                       new Drawing.Own<>(RING_COLOUR, "outline-color: " + mix(seeds(theme).accent(), 85, "#ffffff") + ";"));
    }

    /** The cursor while the board holds the keys. */
    public record HeldCursor() implements GridFrame.HeldCursorBorder.Concrete<MinesweeperVista>, Drawn {
        public static final HeldCursor INSTANCE = new HeldCursor();
        @Override public List<Drawing<?>> drawings(Dressed theme) { return heldRing(theme); }
    }

    /** A tile never holds control - its moves are momentary - but were one to, the held ring. */
    public record LentCursor() implements GridFrame.LentCursorBorder.Concrete<MinesweeperVista>, Drawn {
        public static final LentCursor INSTANCE = new LentCursor();
        @Override public List<Drawing<?>> drawings(Dressed theme) { return heldRing(theme); }
    }

    /** The tiles selected - flagged or revealed at once - shown in the accent in the air round them; the cursor's own by its ring. */
    public record Selection() implements GridFrame.SelectionFill.Concrete<MinesweeperVista>, Drawn {
        public static final Selection INSTANCE = new Selection();
        @Override public List<Drawing<?>> drawings(Dressed theme) {
            Seeds s = seeds(theme);
            return List.of(new Drawing.Own<>(GROUND, new Answer.Own<Target.Color.Background>(ground("transparent"))
                    .at(State.SELECTED, ground(mix(s.accent(), 55, "transparent")))
                    .at(State.CURRENT, ground(mix(s.accent(), 30, "transparent")))));
        }
    }

    /** The board has no head. */
    public record Header() implements GridFrame.HeaderBand.Concrete<MinesweeperVista>, Drawn {
        public static final Header INSTANCE = new Header();
        @Override public List<Drawing<?>> drawings(Dressed theme) { return nothing(GridFrame.HeaderBand.INSTANCE); }
    }

    /** Nor rows numbered. */
    public record Gutter() implements GridFrame.GutterBand.Concrete<MinesweeperVista>, Drawn {
        public static final Gutter INSTANCE = new Gutter();
        @Override public List<Drawing<?>> drawings(Dressed theme) { return nothing(GridFrame.GutterBand.INSTANCE); }
    }

    /** Nor an editor: a move is momentary. */
    public record Anchor() implements GridFrame.EditorAnchor.Concrete<MinesweeperVista>, Drawn {
        public static final Anchor INSTANCE = new Anchor();
        @Override public List<Drawing<?>> drawings(Dressed theme) { return nothing(GridFrame.EditorAnchor.INSTANCE); }
    }

    /** The veil a question puts over the board: pale, the board showing through. */
    public record Veil() implements GridFrame.Mask.Concrete<MinesweeperVista>, Drawn {
        public static final Veil INSTANCE = new Veil();
        @Override public List<Drawing<?>> drawings(Dressed theme) {
            return List.of(new Drawing.Own<>(GROUND, ground(mix(seeds(theme).ground(), 55, "transparent"))));
        }
    }

    /** A question's plate: glass - the raised layer, half clear, rimmed in light, rounded, a soft cast below. */
    public record Panel() implements GridFrame.QuestionPanel.Concrete<MinesweeperVista>, Drawn {
        public static final Panel INSTANCE = new Panel();
        @Override public List<Drawing<?>> drawings(Dressed theme) {
            Seeds s = seeds(theme);
            return List.of(new Drawing.Own<>(GROUND, ground(mix(s.raised(), 78, "transparent"))),
                           new Drawing.Own<>(Target.Shape.Border.INSTANCE, "border-width: 1px; border-style: solid;"),
                           new Drawing.Own<>(Target.Color.Border.INSTANCE, "border-color: " + mix(s.accent(), 20, "#ffffff") + ";"),
                           new Drawing.Own<>(Target.Shape.Corner.INSTANCE, "border-radius: 12px;"),
                           new Drawing.Own<>(Target.Shape.Shadow.INSTANCE, "box-shadow: 0 12px 30px -10px var(--box-shadow-cast);"),
                           new Drawing.Own<>(Target.Color.BoxShadow.INSTANCE,
                                   "--box-shadow-cast: " + mix(s.accent(), 45, "transparent") + "; --box-shadow-lit: transparent; --box-shadow-glow: transparent;"),
                           new Drawing.Own<>(Target.Size.Inset.INSTANCE, "padding-block: 16px; padding-inline: 20px;"));
        }
    }

    /** No tile spans another. */
    public record Merged() implements GridFrame.MergeHost.Concrete<MinesweeperVista>, Drawn {
        public static final Merged INSTANCE = new Merged();
        @Override public List<Drawing<?>> drawings(Dressed theme) { return nothing(GridFrame.MergeHost.INSTANCE); }
    }

    /** No column is resized. */
    public record Guide() implements GridFrame.ResizeGuide.Concrete<MinesweeperVista>, Drawn {
        public static final Guide INSTANCE = new Guide();
        @Override public List<Drawing<?>> drawings(Dressed theme) { return nothing(GridFrame.ResizeGuide.INSTANCE); }
    }

    /** Nothing is copied from the board. */
    public record CopiedArea() implements GridFrame.CopiedArea.Concrete<MinesweeperVista>, Drawn {
        public static final CopiedArea INSTANCE = new CopiedArea();
        @Override public List<Drawing<?>> drawings(Dressed theme) { return nothing(GridFrame.CopiedArea.INSTANCE); }
    }

    public record CopiedBorder() implements GridFrame.CopiedBorder.Concrete<MinesweeperVista>, Drawn {
        public static final CopiedBorder INSTANCE = new CopiedBorder();
        @Override public List<Drawing<?>> drawings(Dressed theme) { return nothing(GridFrame.CopiedBorder.INSTANCE); }
    }
}
