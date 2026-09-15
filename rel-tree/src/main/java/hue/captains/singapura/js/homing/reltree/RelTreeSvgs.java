package hue.captains.singapura.js.homing.reltree;

import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.SvgBeing;
import hue.captains.singapura.js.homing.core.SvgGroup;

import java.util.List;

/**
 * The tree's typed SVG, every being a line icon in {@code currentColor}, so
 * the colour is the owning span's and so the theme's — the reason these are
 * SVG and not emoji, which no theme can reach. The rows module parses the
 * markup per row and places the result inside a span it owns; the state is a
 * class on that span.
 *
 * <p>{@link caret} — a stroked chevron pointing right, turned a quarter by the
 * open class; a leaf's span hides it. {@link folderClosed} and
 * {@link folderOpen} — the folder option's two states, one shown at a time,
 * a leaf showing neither. No gradient, no defs, no ids.</p>
 */
public record RelTreeSvgs() implements SvgGroup<RelTreeSvgs> {

    /** A chevron pointing right: closed. Rotated a quarter turn by the open class. */
    public record caret() implements SvgBeing<RelTreeSvgs> {}
    /** A closed folder: the tab and the body. */
    public record folderClosed() implements SvgBeing<RelTreeSvgs> {}
    /** An open folder: the back and the lifted front. */
    public record folderOpen() implements SvgBeing<RelTreeSvgs> {}

    public static final RelTreeSvgs INSTANCE = new RelTreeSvgs();

    @Override public List<SvgBeing<RelTreeSvgs>> svgBeings() { return List.of(new caret(), new folderClosed(), new folderOpen()); }

    @Override public ExportsOf<RelTreeSvgs> exports() { return new ExportsOf<>(this, List.copyOf(svgBeings())); }
}
