package hue.captains.singapura.js.homing.relgrid.workbench;

import hue.captains.singapura.js.homing.design.color.ColorNode;
import hue.captains.singapura.js.homing.design.color.ColorScheme;
import hue.captains.singapura.js.homing.design.color.ConcreteColor;
import hue.captains.singapura.js.homing.design.color.Palettable;
import hue.captains.singapura.js.homing.design.semantic.ColorExtension;
import hue.captains.singapura.js.homing.design.semantic.ColorPalette;
import hue.captains.singapura.js.homing.design.semantic.ColorSource;
import hue.captains.singapura.js.homing.design.semantic.SubPalette;
import hue.captains.singapura.js.homing.ui.styles.Harbour;
import hue.captains.singapura.js.homing.ui.styles.HouseColours;
import hue.captains.singapura.js.homing.ui.styles.HousePalettes;

import java.util.List;
import java.util.Optional;

/**
 * Minesweeper's colours for its Vista design (RFC 0067, appendix Semantic Colours and Flavours: Minesweeper's colour
 * extension grafts its colours): the board's own mixes of the house's accent, grafted under the house's branches by
 * what each is for. Each palette's word on them is worked out from what that palette gives the house's colours, as the
 * house's own tints are, so the board follows every house palette; Harbour's is the last word. In the tree only where
 * this module is on the classpath.
 */
public record VistaColours() implements ColorExtension {

    /** Vista's colours: one sealed set, so a palette's word answers every one. */
    public sealed interface VistaColour permits VistaEdgeLit, VistaEdgeSide, VistaEdgeShade, VistaGlowCandidate, VistaGlowHeld,
            VistaCursor, VistaCursorHeld, VistaSelected, VistaSelectedCursor, VistaVeil, VistaPlate, VistaPlateRim, VistaPlateCast {}

    /** The board's edge along its top, lit: the accent at 18% over white. */
    public record VistaEdgeLit() implements Palettable<ColorScheme.Base, HouseColours.Lines>, VistaColour {}
    /** The board's edge at its sides: the accent at 45% over the hairline. */
    public record VistaEdgeSide() implements Palettable<ColorScheme.Base, HouseColours.Lines>, VistaColour {}
    /** The board's edge along its foot, in shade: the accent at 65% over the hairline. */
    public record VistaEdgeShade() implements Palettable<ColorScheme.Base, HouseColours.Lines>, VistaColour {}
    /** The board's glow while the keys may come to it: the accent at 25%. */
    public record VistaGlowCandidate() implements Palettable<ColorScheme.Base, HouseColours.Attention>, VistaColour {}
    /** The board's glow while it holds the keys: the accent at 45%. */
    public record VistaGlowHeld() implements Palettable<ColorScheme.Base, HouseColours.Attention>, VistaColour {}
    /** The cursor while the keys are elsewhere: a faint ring, the accent at 35%. */
    public record VistaCursor() implements Palettable<ColorScheme.Base, HouseColours.Attention>, VistaColour {}
    /** The cursor while the board holds the keys: the accent at 85% over white, soft. */
    public record VistaCursorHeld() implements Palettable<ColorScheme.Base, HouseColours.Attention>, VistaColour {}
    /** The tiles selected, in the air round them: the accent at 55%. */
    public record VistaSelected() implements Palettable<ColorScheme.Base, HouseColours.Attention>, VistaColour {}
    /** The cursor's own tile among those selected: the accent at 30%. */
    public record VistaSelectedCursor() implements Palettable<ColorScheme.Base, HouseColours.Attention>, VistaColour {}
    /** The veil a question puts over the board: the page's ground at 55%, the board showing through. */
    public record VistaVeil() implements Palettable<ColorScheme.Base, HouseColours.Surfaces>, VistaColour {}
    /** A question's glass plate: the raised layer at 78%. */
    public record VistaPlate() implements Palettable<ColorScheme.Base, HouseColours.Surfaces>, VistaColour {}
    /** The plate's rim of light: the accent at 20% over white. */
    public record VistaPlateRim() implements Palettable<ColorScheme.Base, HouseColours.Lines>, VistaColour {}
    /** What the plate casts: the accent at 45%. */
    public record VistaPlateCast() implements Palettable<ColorScheme.Base, HouseColours.Shadows>, VistaColour {}

    private static final ConcreteColor.Srgb CLEAR = new ConcreteColor.Srgb("transparent");
    private static final ConcreteColor.Srgb WHITE = new ConcreteColor.Srgb("#ffffff");

    @Override public Class<? extends ColorSource> extending() { return HouseColours.class; }

    @Override public List<ColorNode<?, ?, ?>> nodes() {
        return List.of(new VistaEdgeLit(), new VistaEdgeSide(), new VistaEdgeShade(), new VistaGlowCandidate(), new VistaGlowHeld(),
                new VistaCursor(), new VistaCursorHeld(), new VistaSelected(), new VistaSelectedCursor(), new VistaVeil(),
                new VistaPlate(), new VistaPlateRim(), new VistaPlateCast());
    }

    /** A palette's word: each colour from what the palette gives the house's colours it mixes; none where it gives them none. */
    @Override public Optional<SubPalette> subPalette(ColorPalette<?> palette) { return Optional.of(c -> of(palette, c)); }

    /** The last word: Harbour's, the house's own palette. */
    @Override public Optional<SubPalette> baseSubPalette() { return Optional.of(c -> of(Harbour.INSTANCE, c)); }

    private static Optional<ConcreteColor> of(ColorPalette<?> palette, Palettable<?, ?> colour) {
        if (!(colour instanceof VistaColour vista)) return Optional.empty();
        var accent = new HouseColours.Accent();
        return switch (vista) {
            case VistaEdgeLit c -> over(palette, accent, 0.18, WHITE);
            case VistaEdgeSide c -> mix(palette, accent, 0.45, new HouseColours.Border());
            case VistaEdgeShade c -> mix(palette, accent, 0.65, new HouseColours.Border());
            case VistaGlowCandidate c -> over(palette, accent, 0.25, CLEAR);
            case VistaGlowHeld c -> over(palette, accent, 0.45, CLEAR);
            case VistaCursor c -> over(palette, accent, 0.35, CLEAR);
            case VistaCursorHeld c -> over(palette, accent, 0.85, WHITE);
            case VistaSelected c -> over(palette, accent, 0.55, CLEAR);
            case VistaSelectedCursor c -> over(palette, accent, 0.30, CLEAR);
            case VistaVeil c -> over(palette, new HouseColours.Ground(), 0.55, CLEAR);
            case VistaPlate c -> over(palette, new HouseColours.Raised(), 0.78, CLEAR);
            case VistaPlateRim c -> over(palette, accent, 0.20, WHITE);
            case VistaPlateCast c -> over(palette, accent, 0.45, CLEAR);
        };
    }

    /** One of the palette's colours so much over another of its. */
    private static Optional<ConcreteColor> mix(ColorPalette<?> palette, Palettable<?, ?> colour, double share, Palettable<?, ?> under) {
        return srgb(palette, under).flatMap(b -> over(palette, colour, share, b));
    }

    /** One of the palette's colours so much over a colour of the design's own, white or clear. */
    private static Optional<ConcreteColor> over(ColorPalette<?> palette, Palettable<?, ?> colour, double share, ConcreteColor.Srgb under) {
        return srgb(palette, colour).map(a -> HousePalettes.INSTANCE.tint(a, share, under));
    }

    private static Optional<ConcreteColor.Srgb> srgb(ColorPalette<?> palette, Palettable<?, ?> colour) {
        return palette.color(colour).filter(ConcreteColor.Srgb.class::isInstance).map(ConcreteColor.Srgb.class::cast);
    }
}
