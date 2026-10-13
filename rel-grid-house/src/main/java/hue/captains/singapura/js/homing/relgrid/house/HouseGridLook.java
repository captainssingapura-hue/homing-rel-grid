package hue.captains.singapura.js.homing.relgrid.house;

import hue.captains.singapura.js.homing.design.semantic.DesignStyle;
import hue.captains.singapura.js.homing.design.semantic.Dressed;
import hue.captains.singapura.js.homing.ui.styles.Editorial;
import hue.captains.singapura.js.homing.ui.styles.Glassmorphism;
import hue.captains.singapura.js.homing.ui.styles.HouseStyles;
import hue.captains.singapura.js.homing.ui.styles.NeoBrutalism;
import hue.captains.singapura.js.homing.ui.styles.NeoFuturism;
import hue.captains.singapura.js.homing.ui.styles.Neumorphism;
import hue.captains.singapura.js.homing.ui.styles.RetroFuturism;
import hue.captains.singapura.js.homing.ui.styles.Seeds;
import hue.captains.singapura.js.homing.ui.styles.Sketchy;
import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

/**
 * What the house's grid is drawn in, theme by theme: the palette's seeds for every colour, and the few measures a
 * style sets apart - how heavy a rule is, how round a corner. A line is heavy where the style draws heavy lines
 * (neo-brutalism, sketchy), as the old lattice had it.
 */
record HouseGridLook() implements StatelessFunctionalObject {

    static final HouseGridLook INSTANCE = new HouseGridLook();

    /** The colours the worn palette is written from. */
    Seeds seeds(Dressed theme) { return HouseStyles.INSTANCE.seeds(theme.palette()); }

    /** A rule's width: a hairline, or two where the style draws heavy lines. */
    String rule(Dressed theme) { return heavy(theme.style()) ? "2px" : "1px"; }

    /** The header's rule: a rule's width and one more, so the band reads as the head of its columns. */
    String headRule(Dressed theme) { return heavy(theme.style()) ? "3px" : "2px"; }

    /** How round the grid's box and a panel are, in the style worn. */
    String corner(Dressed theme) {
        return switch (theme.style()) {
            case Editorial e -> "4px";
            case NeoBrutalism n -> "0";
            case NeoFuturism n -> "2px";
            case Neumorphism n -> "12px";
            case Glassmorphism g -> "12px";
            case RetroFuturism r -> "2px";
            case Sketchy s -> "6px";
            default -> "4px";
        };
    }

    /** A ground said whole: a colour, and no ramp nor mark over it. */
    String ground(String colour) {
        return "background-color: " + colour + "; --background-ramp-from: transparent; --background-ramp-via: transparent;"
                + " --background-ramp-to: transparent; --background-mark: transparent;";
    }

    /** No ground of its own, and a ramp an effect may sweep: clear at its ends, the colour in its middle. */
    String ramp(String via) {
        return "background-color: transparent; --background-ramp-from: transparent; --background-ramp-via: " + via + ";"
                + " --background-ramp-to: transparent; --background-mark: transparent;";
    }

    /** Whether a copied range's box sweeps a band of its tint across, in the style worn: the styles of light and motion. */
    boolean sweeps(Dressed theme) { return theme.style() instanceof NeoFuturism || theme.style() instanceof RetroFuturism; }

    /** One colour so much over another. */
    String mix(String colour, int percent, String over) { return "color-mix(in srgb, " + colour + " " + percent + "%, " + over + ")"; }

    /** A shadow's colours said whole: cast, and nothing lit nor glowing. */
    String cast(String colour) { return "--box-shadow-cast: " + colour + "; --box-shadow-lit: transparent; --box-shadow-glow: transparent;"; }

    private static boolean heavy(DesignStyle style) { return style instanceof NeoBrutalism || style instanceof Sketchy; }
}
