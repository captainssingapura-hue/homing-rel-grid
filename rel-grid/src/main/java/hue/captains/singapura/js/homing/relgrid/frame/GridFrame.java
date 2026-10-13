package hue.captains.singapura.js.homing.relgrid.frame;

import hue.captains.singapura.js.homing.component.taxonomy.ConcreteComponent;
import hue.captains.singapura.js.homing.component.taxonomy.Design;
import hue.captains.singapura.js.homing.component.taxonomy.DesignBranch;
import hue.captains.singapura.js.homing.component.taxonomy.L2_ComponentBranch;
import hue.captains.singapura.js.homing.component.taxonomy.StateType;
import hue.captains.singapura.js.homing.component.taxonomy.VirtualComponent;
import hue.captains.singapura.js.homing.design.Owns;
import hue.captains.singapura.js.homing.design.Target;
import hue.captains.singapura.js.homing.ui.taxonomy.HouseBranches;
import hue.captains.singapura.js.homing.ui.taxonomy.HouseStates;

import java.util.List;

/**
 * The grid's frame (RFC 0050 · Episode 2 — Appendix: The Frame): everything the cells do not handle, as a design
 * branch of virtual leaves named from what the grid makes. A design of it - the house's, or a domain's - draws every
 * leaf; the grid wears a leaf through the design it is made with, and never learns a class. In the house's taxonomy
 * wherever the grid is on the classpath ({@link GridFrameExtension}).
 *
 * <p>A condition the grid alone has - the cursor while the grid holds the keys, while its cell holds control - is a
 * leaf of its own, which the grid wears in its place: the states a design answers are core's, and a component in a
 * state of its own wears a different class. What a leaf shows by the house's states, it declares.</p>
 *
 * <p>Leaves worn together on one element own different targets: on a body slot, the lattice its border, the
 * selection its ground, the cursor its ring.</p>
 */
public record GridFrame() implements L2_ComponentBranch<HouseBranches.Region>, DesignBranch {

    public static final GridFrame INSTANCE = new GridFrame();

    @Override public HouseBranches.Region parent() { return HouseBranches.Region.INSTANCE; }

    /** The grid's box: its ground, its edge and its corner, and its light while the keys are with it. */
    public record GridEdge() implements VirtualComponent<GridFrame>, Owns {
        public static final GridEdge INSTANCE = new GridEdge();
        @Override public GridFrame parent() { return GridFrame.INSTANCE; }
        @Override public List<StateType<?>> states() { return List.of(HouseStates.Keyboard.INSTANCE); }
        @Override public List<Target> owns() {
            return List.of(Target.Color.Background.INSTANCE, Target.Shape.Border.INSTANCE, Target.Color.Border.INSTANCE,
                           Target.Shape.Outline.INSTANCE, Target.Color.Outline.INSTANCE, Target.Shape.Corner.INSTANCE);
        }
        public interface Concrete<D extends Design<GridFrame>> extends ConcreteComponent<GridEdge, GridFrame, D> {}
    }

    /** A body slot's share of the lattice: the rules after it, along its row and down its column. */
    public record Lattice() implements VirtualComponent<GridFrame>, Owns {
        public static final Lattice INSTANCE = new Lattice();
        @Override public GridFrame parent() { return GridFrame.INSTANCE; }
        @Override public List<Target> owns() { return List.of(Target.Shape.Border.INSTANCE, Target.Color.Border.INSTANCE); }
        public interface Concrete<D extends Design<GridFrame>> extends ConcreteComponent<Lattice, GridFrame, D> {}
    }

    /** The cursor's slot, while the keys are elsewhere: the place the grid would go on from. */
    public record CursorBorder() implements VirtualComponent<GridFrame>, Owns {
        public static final CursorBorder INSTANCE = new CursorBorder();
        @Override public GridFrame parent() { return GridFrame.INSTANCE; }
        @Override public List<Target> owns() { return List.of(Target.Shape.Outline.INSTANCE, Target.Color.Outline.INSTANCE); }
        public interface Concrete<D extends Design<GridFrame>> extends ConcreteComponent<CursorBorder, GridFrame, D> {}
    }

    /** The cursor's slot, while the grid holds the keys: where they act. */
    public record HeldCursorBorder() implements VirtualComponent<GridFrame>, Owns {
        public static final HeldCursorBorder INSTANCE = new HeldCursorBorder();
        @Override public GridFrame parent() { return GridFrame.INSTANCE; }
        @Override public List<Target> owns() { return List.of(Target.Shape.Outline.INSTANCE, Target.Color.Outline.INSTANCE); }
        public interface Concrete<D extends Design<GridFrame>> extends ConcreteComponent<HeldCursorBorder, GridFrame, D> {}
    }

    /** The cursor's slot, while its cell holds control: the grid has handed the keys in. */
    public record LentCursorBorder() implements VirtualComponent<GridFrame>, Owns {
        public static final LentCursorBorder INSTANCE = new LentCursorBorder();
        @Override public GridFrame parent() { return GridFrame.INSTANCE; }
        @Override public List<Target> owns() { return List.of(Target.Shape.Outline.INSTANCE, Target.Color.Outline.INSTANCE); }
        public interface Concrete<D extends Design<GridFrame>> extends ConcreteComponent<LentCursorBorder, GridFrame, D> {}
    }

    /** A body slot's ground, shown while the slot is selected; the cursor's own slot is current, which a design may show by its ring alone. */
    public record SelectionFill() implements VirtualComponent<GridFrame>, Owns {
        public static final SelectionFill INSTANCE = new SelectionFill();
        @Override public GridFrame parent() { return GridFrame.INSTANCE; }
        @Override public List<StateType<?>> states() { return List.of(HouseStates.Selected.INSTANCE, HouseStates.Current.INSTANCE); }
        @Override public List<Target> owns() { return List.of(Target.Color.Background.INSTANCE); }
        public interface Concrete<D extends Design<GridFrame>> extends ConcreteComponent<SelectionFill, GridFrame, D> {}
    }

    /** A header slot: the band the columns' heads stand in, and its rules. */
    public record HeaderBand() implements VirtualComponent<GridFrame>, Owns {
        public static final HeaderBand INSTANCE = new HeaderBand();
        @Override public GridFrame parent() { return GridFrame.INSTANCE; }
        @Override public List<Target> owns() {
            return List.of(Target.Color.Background.INSTANCE, Target.Shape.Border.INSTANCE, Target.Color.Border.INSTANCE);
        }
        public interface Concrete<D extends Design<GridFrame>> extends ConcreteComponent<HeaderBand, GridFrame, D> {}
    }

    /** A gutter slot: the band the rows' numbers stand in, the numbers' ink and size, and its rules. */
    public record GutterBand() implements VirtualComponent<GridFrame>, Owns {
        public static final GutterBand INSTANCE = new GutterBand();
        @Override public GridFrame parent() { return GridFrame.INSTANCE; }
        @Override public List<StateType<?>> states() { return List.of(HouseStates.Current.INSTANCE, HouseStates.Selected.INSTANCE); }
        @Override public List<Target> owns() {
            return List.of(Target.Color.Background.INSTANCE, Target.Shape.Border.INSTANCE, Target.Color.Border.INSTANCE,
                           Target.Color.Ink.INSTANCE, Target.Type.Scale.INSTANCE);
        }
        public interface Concrete<D extends Design<GridFrame>> extends ConcreteComponent<GutterBand, GridFrame, D> {}
    }

    /** The box an open editor is laid in over its slot: the cell's editor is the cell's, the box the grid's. */
    public record EditorAnchor() implements VirtualComponent<GridFrame>, Owns {
        public static final EditorAnchor INSTANCE = new EditorAnchor();
        @Override public GridFrame parent() { return GridFrame.INSTANCE; }
        @Override public List<Target> owns() { return List.of(Target.Shape.Shadow.INSTANCE, Target.Color.BoxShadow.INSTANCE); }
        public interface Concrete<D extends Design<GridFrame>> extends ConcreteComponent<EditorAnchor, GridFrame, D> {}
    }

    /** The veil over the table while a question is put. */
    public record Mask() implements VirtualComponent<GridFrame>, Owns {
        public static final Mask INSTANCE = new Mask();
        @Override public GridFrame parent() { return GridFrame.INSTANCE; }
        @Override public List<Target> owns() { return List.of(Target.Color.Background.INSTANCE); }
        public interface Concrete<D extends Design<GridFrame>> extends ConcreteComponent<Mask, GridFrame, D> {}
    }

    /** The plate a question stands on over the veil: the domain's answer is placed in it. */
    public record QuestionPanel() implements VirtualComponent<GridFrame>, Owns {
        public static final QuestionPanel INSTANCE = new QuestionPanel();
        @Override public GridFrame parent() { return GridFrame.INSTANCE; }
        @Override public List<Target> owns() {
            return List.of(Target.Color.Background.INSTANCE, Target.Shape.Border.INSTANCE, Target.Color.Border.INSTANCE,
                           Target.Shape.Corner.INSTANCE, Target.Shape.Shadow.INSTANCE, Target.Color.BoxShadow.INSTANCE,
                           Target.Size.Inset.INSTANCE);
        }
        public interface Concrete<D extends Design<GridFrame>> extends ConcreteComponent<QuestionPanel, GridFrame, D> {}
    }

    /** The box a merged cell is placed in, over the slots it reaches across: its ground and its rules. */
    public record MergeHost() implements VirtualComponent<GridFrame>, Owns {
        public static final MergeHost INSTANCE = new MergeHost();
        @Override public GridFrame parent() { return GridFrame.INSTANCE; }
        @Override public List<StateType<?>> states() { return List.of(HouseStates.Current.INSTANCE, HouseStates.Selected.INSTANCE); }
        @Override public List<Target> owns() {
            return List.of(Target.Color.Background.INSTANCE, Target.Shape.Border.INSTANCE, Target.Color.Border.INSTANCE);
        }
        public interface Concrete<D extends Design<GridFrame>> extends ConcreteComponent<MergeHost, GridFrame, D> {}
    }

    /** The line a column's resizing draws, where its edge will land. */
    public record ResizeGuide() implements VirtualComponent<GridFrame>, Owns {
        public static final ResizeGuide INSTANCE = new ResizeGuide();
        @Override public GridFrame parent() { return GridFrame.INSTANCE; }
        @Override public List<Target> owns() { return List.of(Target.Color.Background.INSTANCE); }
        public interface Concrete<D extends Design<GridFrame>> extends ConcreteComponent<ResizeGuide, GridFrame, D> {}
    }
}
