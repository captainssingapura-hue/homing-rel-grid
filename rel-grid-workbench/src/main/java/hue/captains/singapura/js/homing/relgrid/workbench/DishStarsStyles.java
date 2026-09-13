package hue.captains.singapura.js.homing.relgrid.workbench;

import hue.captains.singapura.js.homing.core.CssClass;
import hue.captains.singapura.js.homing.core.CssGroup;
import hue.captains.singapura.js.homing.core.CssImportsFor;

import java.util.List;

/** The stars cell's looks, typed: the stars in the slot, and the panel it edits with. */
public record DishStarsStyles() implements CssGroup<DishStarsStyles> {

    public static final DishStarsStyles INSTANCE = new DishStarsStyles();

    public record wb_stars() implements CssClass<DishStarsStyles> {
        @Override public String body() { return """
                font: 13px sans-serif;
                letter-spacing: 2px;
                padding: 0 6px;
                """;
        }
    }

    public record wb_stars_ro() implements CssClass<DishStarsStyles> {
        @Override public String body() { return """
                opacity: 0.55;
                """;
        }
    }

    /**
     * The PANEL. Hung off the anchor the grid placed it in — which is exactly
     * the cell — so it is free to be much larger in both directions. Out of
     * the table it costs the table nothing: no column widens, no row grows.
     */
    public record wb_stars_panel() implements CssClass<DishStarsStyles> {
        @Override public String body() { return """
                position: absolute;
                top: 100%;
                left: 0;
                min-width: 250px;
                padding: 12px 14px;
                box-sizing: border-box;
                background: var(--color-surface-raised);
                color: var(--color-text-primary);
                border: 1px solid var(--color-border);
                border-radius: 6px;
                box-shadow: 0 8px 24px color-mix(in srgb, var(--color-text-primary) 28%, transparent);
                outline: none;
                """;
        }
    }

    public record wb_stars_row() implements CssClass<DishStarsStyles> {
        @Override public String body() { return """
                display: flex;
                gap: 8px;
                font-size: 30px;
                line-height: 1;
                cursor: pointer;
                """;
        }
    }

    public record wb_star() implements CssClass<DishStarsStyles> {
        @Override public String body() { return """
                user-select: none;
                -webkit-user-select: none;
                color: var(--color-text-muted);
                opacity: 0.45;
                """;
        }
    }

    /** A star within the draft. Listed after wb_star, which it qualifies. */
    public record wb_star_on() implements CssClass<DishStarsStyles> {
        @Override public String body() { return """
                color: var(--color-accent);
                opacity: 1;
                """;
        }
    }

    public record wb_stars_hint() implements CssClass<DishStarsStyles> {
        @Override public String body() { return """
                margin-top: 10px;
                font: 11px sans-serif;
                color: var(--color-text-muted);
                white-space: nowrap;
                """;
        }
    }

    @Override public CssImportsFor<DishStarsStyles> cssImports() { return CssImportsFor.none(this); }

    @Override public List<CssClass<DishStarsStyles>> cssClasses() {
        return List.of(new wb_stars(), new wb_stars_ro(), new wb_stars_panel(), new wb_stars_row(),
                       new wb_star(), new wb_star_on(), new wb_stars_hint());
    }
}
