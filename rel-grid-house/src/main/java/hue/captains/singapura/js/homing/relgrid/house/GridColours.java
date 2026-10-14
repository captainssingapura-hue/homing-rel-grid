package hue.captains.singapura.js.homing.relgrid.house;

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
 * The colours of the house's grid that the house's tree has not (RFC 0067, appendix Semantic Colours and Flavours): the
 * grid's own mixes of the house's colours, grafted under the house's branches by what each is for. Each palette's word
 * on them is worked out from what that palette gives the house's colours, as the house's own tints are, so the grid
 * follows every house palette; Harbour's is the last word. In the tree only where this module is on the classpath.
 */
public record GridColours() implements ColorExtension {

    /** The grid's colours: one sealed set, so a palette's word answers every one. */
    public sealed interface GridColour permits GridCursorRing, GridHeadRule, GridGutterInk, GridSelection, GridMergedSelected, GridCast, GridVeil {}

    /** The cursor's ring while the keys are elsewhere: the text a little into the hairline, at 35%. */
    public record GridCursorRing() implements Palettable<ColorScheme.Base, HouseColours.Lines>, GridColour {}
    /** The header band's foot: the text into the hairline at 30%, so the band heads its columns. */
    public record GridHeadRule() implements Palettable<ColorScheme.Base, HouseColours.Lines>, GridColour {}
    /** The gutter's numbers: the text at 60%, small and muted. */
    public record GridGutterInk() implements Palettable<ColorScheme.Base, HouseColours.Inks>, GridColour {}
    /** The selection's wash: the accent at 16%. */
    public record GridSelection() implements Palettable<ColorScheme.Base, HouseColours.Attention>, GridColour {}
    /** A merged cell selected: the accent at 16% over the page's ground, whole, since it covers the slots it reaches over. */
    public record GridMergedSelected() implements Palettable<ColorScheme.Base, HouseColours.Attention>, GridColour {}
    /** What an open editor and a question's plate cast: the inverted layer at 45%. */
    public record GridCast() implements Palettable<ColorScheme.Base, HouseColours.Shadows>, GridColour {}
    /** The veil a question puts over the grid: the page's ground at 60%. */
    public record GridVeil() implements Palettable<ColorScheme.Base, HouseColours.Surfaces>, GridColour {}

    private static final ConcreteColor.Srgb CLEAR = new ConcreteColor.Srgb("transparent");

    @Override public Class<? extends ColorSource> extending() { return HouseColours.class; }

    @Override public List<ColorNode<?, ?, ?>> nodes() {
        return List.of(new GridCursorRing(), new GridHeadRule(), new GridGutterInk(), new GridSelection(), new GridMergedSelected(),
                new GridCast(), new GridVeil());
    }

    /** A palette's word: each colour from what the palette gives the house's colours it mixes; none where it gives them none. */
    @Override public Optional<SubPalette> subPalette(ColorPalette<?> palette) { return Optional.of(c -> of(palette, c)); }

    /** The last word: Harbour's, the house's own palette. */
    @Override public Optional<SubPalette> baseSubPalette() { return Optional.of(c -> of(Harbour.INSTANCE, c)); }

    private static Optional<ConcreteColor> of(ColorPalette<?> palette, Palettable<?, ?> colour) {
        if (!(colour instanceof GridColour grid)) return Optional.empty();
        return switch (grid) {
            case GridCursorRing c -> mix(palette, new HouseColours.Text(), 0.35, new HouseColours.Border());
            case GridHeadRule c -> mix(palette, new HouseColours.Text(), 0.30, new HouseColours.Border());
            case GridGutterInk c -> faded(palette, new HouseColours.Text(), 0.60);
            case GridSelection c -> faded(palette, new HouseColours.Accent(), 0.16);
            case GridMergedSelected c -> mix(palette, new HouseColours.Accent(), 0.16, new HouseColours.Ground());
            case GridCast c -> faded(palette, new HouseColours.Inverted(), 0.45);
            case GridVeil c -> faded(palette, new HouseColours.Ground(), 0.60);
        };
    }

    /** One of the palette's colours so much over another of its. */
    private static Optional<ConcreteColor> mix(ColorPalette<?> palette, Palettable<?, ?> colour, double share, Palettable<?, ?> over) {
        return srgb(palette, colour).flatMap(a -> srgb(palette, over).map(b -> HousePalettes.INSTANCE.tint(a, share, b)));
    }

    /** One of the palette's colours at a share, the rest clear. */
    private static Optional<ConcreteColor> faded(ColorPalette<?> palette, Palettable<?, ?> colour, double share) {
        return srgb(palette, colour).map(a -> HousePalettes.INSTANCE.tint(a, share, CLEAR));
    }

    private static Optional<ConcreteColor.Srgb> srgb(ColorPalette<?> palette, Palettable<?, ?> colour) {
        return palette.color(colour).filter(ConcreteColor.Srgb.class::isInstance).map(ConcreteColor.Srgb.class::cast);
    }
}
