package hue.captains.singapura.js.homing.relgrid.house;

import hue.captains.singapura.js.homing.component.taxonomy.Design;
import hue.captains.singapura.js.homing.design.Target;
import hue.captains.singapura.js.homing.design.semantic.Drawing;
import hue.captains.singapura.js.homing.design.semantic.Drawn;
import hue.captains.singapura.js.homing.design.semantic.Dressed;
import hue.captains.singapura.js.homing.relgrid.frame.GridFrame;
import hue.captains.singapura.js.homing.ui.taxonomy.HouseRegions;
import hue.captains.singapura.js.homing.ui.taxonomy.HouseText;

import java.util.List;

/**
 * The house's design of a document's table (RFC 0050 · Episode 2 — Appendix: The Frame): the grid's frame drawn as the
 * house draws a table in a document, and nothing of its own where the document has a word for it. Its rules are a data
 * cell's, taken from the theme ({@link Drawing.Of}) - so a style that rules its tables heavier rules this one heavier -
 * its header band a header cell's, its gutter a header cell's with a caption's ink and size; it has no edge and no
 * ground, and stands on the page as a document's table does. What a document has no word for - where the keys are, a
 * selection, the boxes laid over a slot - it draws as the house's grid does ({@link Drawing.As}); the cursor shows only
 * while the keys are with the table, so at rest it reads as prose.
 */
public record HouseProse() implements Design<GridFrame> {

    public static final HouseProse INSTANCE = new HouseProse();

    private static final Target.Color.Background GROUND = Target.Color.Background.INSTANCE;
    private static final Target.Shape.Border BORDER = Target.Shape.Border.INSTANCE;
    private static final Target.Color.Border BORDER_COLOUR = Target.Color.Border.INSTANCE;
    private static final Target.Shape.Outline RING = Target.Shape.Outline.INSTANCE;
    private static final Target.Color.Outline RING_COLOUR = Target.Color.Outline.INSTANCE;

    private static final HouseRegions.DataCell DATA_CELL = HouseRegions.DataCell.INSTANCE;
    private static final HouseRegions.HeaderCell HEADER_CELL = HouseRegions.HeaderCell.INSTANCE;
    private static final HouseText.Caption CAPTION = HouseText.Caption.INSTANCE;

    /** No edge, no ground, no corner, no light: a document's table stands on the page. */
    public record Edge() implements GridFrame.GridEdge.Concrete<HouseProse>, Drawn {
        public static final Edge INSTANCE = new Edge();
        @Override public List<Drawing<?>> drawings(Dressed theme) {
            return List.of(new Drawing.None<>(GROUND), new Drawing.None<>(BORDER), new Drawing.None<>(BORDER_COLOUR),
                           new Drawing.None<>(RING), new Drawing.None<>(RING_COLOUR), new Drawing.None<>(Target.Shape.Corner.INSTANCE));
        }
    }

    /** Every rule a data cell's, as the theme rules a document's table: under each row. */
    public record Lattice() implements GridFrame.Lattice.Concrete<HouseProse>, Drawn {
        public static final Lattice INSTANCE = new Lattice();
        @Override public List<Drawing<?>> drawings(Dressed theme) {
            return List.of(new Drawing.Of<>(BORDER, DATA_CELL), new Drawing.Of<>(BORDER_COLOUR, DATA_CELL));
        }
    }

    /** The cursor while the keys are elsewhere: nothing - at rest a document's table reads as prose. */
    public record Cursor() implements GridFrame.CursorBorder.Concrete<HouseProse>, Drawn {
        public static final Cursor INSTANCE = new Cursor();
        @Override public List<Drawing<?>> drawings(Dressed theme) { return List.of(new Drawing.None<>(RING), new Drawing.None<>(RING_COLOUR)); }
    }

    /** The cursor while the table holds the keys: the house grid's. */
    public record HeldCursor() implements GridFrame.HeldCursorBorder.Concrete<HouseProse>, Drawn {
        public static final HeldCursor INSTANCE = new HeldCursor();
        @Override public List<Drawing<?>> drawings(Dressed theme) {
            return List.of(new Drawing.As<>(RING, HouseGrid.HeldCursor.INSTANCE), new Drawing.As<>(RING_COLOUR, HouseGrid.HeldCursor.INSTANCE));
        }
    }

    /** The cursor while its cell holds control: the house grid's - a document's table lends it to none, but says how. */
    public record LentCursor() implements GridFrame.LentCursorBorder.Concrete<HouseProse>, Drawn {
        public static final LentCursor INSTANCE = new LentCursor();
        @Override public List<Drawing<?>> drawings(Dressed theme) {
            return List.of(new Drawing.As<>(RING, HouseGrid.LentCursor.INSTANCE), new Drawing.As<>(RING_COLOUR, HouseGrid.LentCursor.INSTANCE));
        }
    }

    /** The selection: the house grid's wash. */
    public record Selection() implements GridFrame.SelectionFill.Concrete<HouseProse>, Drawn {
        public static final Selection INSTANCE = new Selection();
        @Override public List<Drawing<?>> drawings(Dressed theme) { return List.of(new Drawing.As<>(GROUND, HouseGrid.Selection.INSTANCE)); }
    }

    /** The header band: a header cell's ground and foot, as the theme draws a document's table's head. */
    public record Header() implements GridFrame.HeaderBand.Concrete<HouseProse>, Drawn {
        public static final Header INSTANCE = new Header();
        @Override public List<Drawing<?>> drawings(Dressed theme) {
            return List.of(new Drawing.Of<>(GROUND, HEADER_CELL), new Drawing.Of<>(BORDER, HEADER_CELL), new Drawing.Of<>(BORDER_COLOUR, HEADER_CELL));
        }
    }

    /** The gutter band: a header cell's ground and rules, its numbers in a caption's ink and size. */
    public record Gutter() implements GridFrame.GutterBand.Concrete<HouseProse>, Drawn {
        public static final Gutter INSTANCE = new Gutter();
        @Override public List<Drawing<?>> drawings(Dressed theme) {
            return List.of(new Drawing.Of<>(GROUND, HEADER_CELL), new Drawing.Of<>(BORDER, HEADER_CELL), new Drawing.Of<>(BORDER_COLOUR, HEADER_CELL),
                           new Drawing.Of<>(Target.Color.Ink.INSTANCE, CAPTION), new Drawing.Of<>(Target.Type.Scale.INSTANCE, CAPTION));
        }
    }

    /** An open editor's box: the house grid's. */
    public record Anchor() implements GridFrame.EditorAnchor.Concrete<HouseProse>, Drawn {
        public static final Anchor INSTANCE = new Anchor();
        @Override public List<Drawing<?>> drawings(Dressed theme) {
            return List.of(new Drawing.As<>(Target.Shape.Shadow.INSTANCE, HouseGrid.Anchor.INSTANCE),
                           new Drawing.As<>(Target.Color.BoxShadow.INSTANCE, HouseGrid.Anchor.INSTANCE));
        }
    }

    /** The veil: the house grid's. */
    public record Veil() implements GridFrame.Mask.Concrete<HouseProse>, Drawn {
        public static final Veil INSTANCE = new Veil();
        @Override public List<Drawing<?>> drawings(Dressed theme) { return List.of(new Drawing.As<>(GROUND, HouseGrid.Veil.INSTANCE)); }
    }

    /** The question's plate: the house grid's. */
    public record Panel() implements GridFrame.QuestionPanel.Concrete<HouseProse>, Drawn {
        public static final Panel INSTANCE = new Panel();
        @Override public List<Drawing<?>> drawings(Dressed theme) {
            var panel = HouseGrid.Panel.INSTANCE;
            return List.of(new Drawing.As<>(GROUND, panel), new Drawing.As<>(BORDER, panel), new Drawing.As<>(BORDER_COLOUR, panel),
                           new Drawing.As<>(Target.Shape.Corner.INSTANCE, panel), new Drawing.As<>(Target.Shape.Shadow.INSTANCE, panel),
                           new Drawing.As<>(Target.Color.BoxShadow.INSTANCE, panel), new Drawing.As<>(Target.Size.Inset.INSTANCE, panel));
        }
    }

    /** A merged cell's box: ruled as a data cell is, on the house grid's ground, which covers the slots it reaches over. */
    public record Merged() implements GridFrame.MergeHost.Concrete<HouseProse>, Drawn {
        public static final Merged INSTANCE = new Merged();
        @Override public List<Drawing<?>> drawings(Dressed theme) {
            return List.of(new Drawing.As<>(GROUND, HouseGrid.Merged.INSTANCE), new Drawing.Of<>(BORDER, DATA_CELL), new Drawing.Of<>(BORDER_COLOUR, DATA_CELL));
        }
    }

    /** The resize guide: the house grid's. */
    public record Guide() implements GridFrame.ResizeGuide.Concrete<HouseProse>, Drawn {
        public static final Guide INSTANCE = new Guide();
        @Override public List<Drawing<?>> drawings(Dressed theme) { return List.of(new Drawing.As<>(GROUND, HouseGrid.Guide.INSTANCE)); }
    }

    /** What was last copied: a document has no word for it - the house grid's box. */
    public record CopiedArea() implements GridFrame.CopiedArea.Concrete<HouseProse>, Drawn {
        public static final CopiedArea INSTANCE = new CopiedArea();
        @Override public List<Drawing<?>> drawings(Dressed theme) {
            return List.of(new Drawing.As<>(GROUND, HouseGrid.CopiedArea.INSTANCE),
                           new Drawing.As<>(Target.Animation.Effect.INSTANCE, HouseGrid.CopiedArea.INSTANCE));
        }
    }

    /** And the line round it: the house grid's marching ants. */
    public record CopiedBorder() implements GridFrame.CopiedBorder.Concrete<HouseProse>, Drawn {
        public static final CopiedBorder INSTANCE = new CopiedBorder();
        @Override public List<Drawing<?>> drawings(Dressed theme) {
            return List.of(new Drawing.As<>(Target.Color.Stroke.INSTANCE, HouseGrid.CopiedBorder.INSTANCE),
                           new Drawing.As<>(Target.Animation.Effect.INSTANCE, HouseGrid.CopiedBorder.INSTANCE));
        }
    }
}