package hue.captains.singapura.js.homing.reltree;

import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.SvgBeing;
import hue.captains.singapura.js.homing.core.SvgGroup;

import java.util.List;

/**
 * The tree's typed SVG: one being, the CARET — a stroked chevron pointing
 * right, in {@code currentColor}, so the colour is the caret span's and so the
 * theme's. The rows module parses the markup once per row and places the
 * result inside the caret span it owns; the closed/open state is a class on
 * that span, which turns the chevron, and a leaf's span hides it. No gradient,
 * no defs, no ids: a basic caret, and the typed-SVG path it rides on for
 * whatever comes after.
 */
public record RelTreeSvgs() implements SvgGroup<RelTreeSvgs> {

    /** A chevron pointing right: closed. Rotated a quarter turn by the open class. */
    public record caret() implements SvgBeing<RelTreeSvgs> {}

    public static final RelTreeSvgs INSTANCE = new RelTreeSvgs();

    @Override public List<SvgBeing<RelTreeSvgs>> svgBeings() { return List.of(new caret()); }

    @Override public ExportsOf<RelTreeSvgs> exports() { return new ExportsOf<>(this, List.copyOf(svgBeings())); }
}
