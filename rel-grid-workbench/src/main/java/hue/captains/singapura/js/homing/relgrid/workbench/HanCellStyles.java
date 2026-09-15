package hue.captains.singapura.js.homing.relgrid.workbench;

import hue.captains.singapura.js.homing.core.CssClass;
import hue.captains.singapura.js.homing.core.CssGroup;

import java.util.List;

/**
 * The Han cell's looks, typed. A square whose ink is sized from its height
 * (container units), a half-square, and a run reaching over several squares.
 *
 * <p>What the old sheet said with a descendant selector — a run's ink in the
 * Latin serif, a half-square's box at full width — is said with a custom
 * property the kind sets on the square and the ink reads: {@code
 * --han-ink-font}, {@code --han-ink-tracking}, {@code --han-ink-space},
 * {@code --han-half-basis}. A host that wants a fixed side sets
 * {@code --han-side} on any ancestor, as before.</p>
 */
public record HanCellStyles() implements CssGroup<HanCellStyles> {

    public static final HanCellStyles INSTANCE = new HanCellStyles();

    /**
     * The square. Width from the column; height from the width — or from the
     * host's --han-side, which then wins over the ratio; the glyph measured
     * against the height. container-type: size is what makes cqh units refer
     * to THIS box.
     */
    public record han_glyph() implements CssClass<HanCellStyles> {
        @Override public String body() { return """
                width: 100%;
                height: var(--han-side, auto);
                aspect-ratio: 1 / 1;
                box-sizing: border-box;
                container-type: size;
                display: grid;
                place-items: center;
                overflow: hidden;
                """;
        }
    }

    /** The ink: 68% of the row, in the CJK serif unless the square says otherwise. */
    public record han_ink() implements CssClass<HanCellStyles> {
        @Override public String body() { return """
                font: var(--han-ink-font, 68cqh/1 'Noto Serif CJK SC', 'Source Han Serif SC', 'Songti SC', 'SimSun', 'PMingLiU', serif);
                letter-spacing: var(--han-ink-tracking, normal);
                white-space: var(--han-ink-space, normal);
                color: var(--color-text-primary);
                user-select: none;
                -webkit-user-select: none;
                """;
        }
    }

    /**
     * Two marks in one square: each in its half, half-width alternates on.
     * min-width:0 on the flex row too: as a grid item its automatic minimum
     * is its content's — two full-width marks — and it would widen past the
     * square. Listed after han_ink, which it qualifies.
     */
    public record han_punct() implements CssClass<HanCellStyles> {
        @Override public String body() { return """
                display: flex;
                width: 100%;
                height: 100%;
                min-width: 0;
                align-items: center;
                """;
        }
    }

    /** A half-width box for one mark; a half-square's box is the whole square wide. */
    public record han_half() implements CssClass<HanCellStyles> {
        @Override public String body() { return """
                flex: 0 0 var(--han-half-basis, 50%);
                width: var(--han-half-basis, 50%);
                min-width: 0;
                overflow: hidden;
                text-align: left;
                white-space: nowrap;
                font-feature-settings: 'halt' 1;
                """;
        }
    }

    /** An opener keeps its right: aligned right so the clip takes its left. Listed after han_half. */
    public record han_open() implements CssClass<HanCellStyles> {
        @Override public String body() { return """
                text-align: right;
                """;
        }
    }

    /** The half-square: half as wide as a square, as tall as one; its one mark's box is the whole of it. */
    public record han_narrow() implements CssClass<HanCellStyles> {
        @Override public String body() { return """
                aspect-ratio: 1 / 2;
                --han-half-basis: 100%;
                """;
        }
    }

    /**
     * A run: it fills the host the grid laid over its squares — n of them
     * wide, one tall — so it sizes itself to nothing, and its letters are in
     * the Latin serif, sized against that box's height: one square's.
     */
    public record han_run() implements CssClass<HanCellStyles> {
        @Override public String body() { return """
                width: 100%;
                height: 100%;
                aspect-ratio: auto;
                --han-ink-font: 68cqh/1 'Noto Serif', 'Georgia', 'Times New Roman', serif;
                --han-ink-tracking: 0.02em;
                --han-ink-space: pre;
                """;
        }
    }

    @Override public List<CssClass<HanCellStyles>> cssClasses() {
        return List.of(new han_glyph(), new han_ink(), new han_punct(), new han_half(), new han_open(),
                       new han_narrow(), new han_run());
    }
}
