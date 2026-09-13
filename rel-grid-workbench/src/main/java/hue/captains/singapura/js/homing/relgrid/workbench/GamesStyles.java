package hue.captains.singapura.js.homing.relgrid.workbench;

import hue.captains.singapura.js.homing.core.CssClass;
import hue.captains.singapura.js.homing.core.CssGroup;
import hue.captains.singapura.js.homing.core.CssImportsFor;

import java.util.List;

/**
 * The Games Catalogue's header cells and their popovers, typed. A header cell
 * is a label that is a button, a caret, an order number and a funnel, laid
 * out to fill the header slot the grid places it in — the grid's resize
 * handle sits on the slot's right edge, over the funnel's margin. The popover
 * is fixed to the viewport, placed by the header's own rectangle through two
 * custom properties, above the sticky header and the grid's mask.
 */
public record GamesStyles() implements CssGroup<GamesStyles> {

    public static final GamesStyles INSTANCE = new GamesStyles();

    public record wb_gh() implements CssClass<GamesStyles> {
        @Override public String body() { return """
                display: flex;
                align-items: center;
                gap: 2px;
                min-width: 0;
                margin: -6px -10px;
                padding: 0 8px 0 0;
                font: inherit;
                """;
        }
    }

    /** The label is the sort control: the whole width, left-aligned, no chrome of a button's. */
    public record wb_gh_sort() implements CssClass<GamesStyles> {
        @Override public String body() { return """
                flex: 1 1 auto;
                min-width: 0;
                display: flex;
                align-items: baseline;
                gap: 4px;
                padding: 6px 4px 6px 10px;
                border: 0;
                background: transparent;
                color: inherit;
                font: inherit;
                text-align: left;
                cursor: pointer;
                border-radius: 3px;
                """;
        }
    }

    public record wb_gh_sort_hot() implements CssClass<GamesStyles> {
        @Override public String pseudoState() { return ":is(:hover, :focus-visible)"; }
        @Override public String body() { return """
                background: color-mix(in srgb, var(--color-accent) 12%, transparent);
                outline: none;
                """;
        }
    }

    public record wb_gh_sort_on() implements CssClass<GamesStyles> {
        @Override public String body() { return """
                color: var(--color-text-primary);
                """;
        }
    }

    public record wb_gh_label() implements CssClass<GamesStyles> {
        @Override public String body() { return """
                overflow: hidden;
                text-overflow: ellipsis;
                white-space: nowrap;
                """;
        }
    }

    public record wb_gh_caret() implements CssClass<GamesStyles> {
        @Override public String body() { return """
                flex: 0 0 auto;
                font-size: 9px;
                color: var(--color-accent);
                """;
        }
    }

    /** The key's place when several sort — a small superscript number after the caret. */
    public record wb_gh_order() implements CssClass<GamesStyles> {
        @Override public String body() { return """
                flex: 0 0 auto;
                font-size: 9px;
                vertical-align: super;
                color: var(--color-text-muted);
                """;
        }
    }

    /** The funnel: a small button that opens the popover; lit while a filter is held, boxed while open. */
    public record wb_gh_filter() implements CssClass<GamesStyles> {
        @Override public String body() { return """
                flex: 0 0 auto;
                width: 18px;
                height: 18px;
                padding: 0;
                border: 1px solid transparent;
                border-radius: 3px;
                background: transparent;
                color: var(--color-text-muted);
                font: 11px sans-serif;
                cursor: pointer;
                """;
        }
    }

    public record wb_gh_filter_hot() implements CssClass<GamesStyles> {
        @Override public String pseudoState() { return ":is(:hover, :focus-visible)"; }
        @Override public String body() { return """
                border-color: var(--color-border);
                color: var(--color-text-primary);
                outline: none;
                """;
        }
    }

    public record wb_gh_filter_on() implements CssClass<GamesStyles> {
        @Override public String body() { return """
                color: var(--color-accent);
                background: color-mix(in srgb, var(--color-accent) 14%, transparent);
                """;
        }
    }

    public record wb_gh_filter_open() implements CssClass<GamesStyles> {
        @Override public String body() { return """
                border-color: var(--color-accent);
                """;
        }
    }

    /** The popover: fixed to the viewport, where the header cell said; above the sticky header (35) and the mask (60). */
    public record wb_gpop() implements CssClass<GamesStyles> {
        @Override public String body() { return """
                position: fixed;
                left: var(--wb-gpop-left, 0px);
                top: var(--wb-gpop-top, 0px);
                z-index: 70;
                min-width: 200px;
                max-width: 280px;
                max-height: 60vh;
                overflow: auto;
                display: flex;
                flex-direction: column;
                gap: 6px;
                padding: 8px 10px 10px;
                box-sizing: border-box;
                background: var(--color-surface-raised);
                color: var(--color-text-primary);
                border: 1px solid var(--color-border);
                border-radius: 6px;
                box-shadow: 0 6px 20px color-mix(in srgb, var(--color-text-primary) 18%, transparent);
                font: 12px sans-serif;
                """;
        }
    }

    public record wb_gpop_title() implements CssClass<GamesStyles> {
        @Override public String body() { return """
                font-weight: 600;
                color: var(--color-text-muted);
                text-transform: uppercase;
                letter-spacing: 0.04em;
                font-size: 10px;
                """;
        }
    }

    public record wb_gpop_row() implements CssClass<GamesStyles> {
        @Override public String body() { return """
                display: flex;
                gap: 6px;
                """;
        }
    }

    public record wb_gpop_input() implements CssClass<GamesStyles> {
        @Override public String body() { return """
                flex: 1 1 0;
                min-width: 0;
                width: 100%;
                box-sizing: border-box;
                padding: 4px 6px;
                font: inherit;
                color: var(--color-text-primary);
                background: var(--color-surface);
                border: 1px solid var(--color-border);
                border-radius: 4px;
                """;
        }
    }

    public record wb_gpop_list() implements CssClass<GamesStyles> {
        @Override public String body() { return """
                display: flex;
                flex-direction: column;
                gap: 2px;
                max-height: 40vh;
                overflow: auto;
                """;
        }
    }

    public record wb_gpop_check() implements CssClass<GamesStyles> {
        @Override public String body() { return """
                display: flex;
                align-items: center;
                gap: 6px;
                padding: 2px 2px;
                cursor: pointer;
                white-space: nowrap;
                """;
        }
    }

    public record wb_gpop_actions() implements CssClass<GamesStyles> {
        @Override public String body() { return """
                display: flex;
                justify-content: flex-end;
                gap: 6px;
                padding-top: 4px;
                border-top: 1px solid var(--color-border);
                """;
        }
    }

    public record wb_gpop_btn() implements CssClass<GamesStyles> {
        @Override public String body() { return """
                padding: 3px 8px;
                cursor: pointer;
                font: inherit;
                background: var(--color-surface);
                color: var(--color-text-primary);
                border: 1px solid var(--color-border);
                border-radius: 4px;
                """;
        }
    }

    @Override public CssImportsFor<GamesStyles> cssImports() { return CssImportsFor.none(this); }

    @Override public List<CssClass<GamesStyles>> cssClasses() {
        return List.of(new wb_gh(), new wb_gh_sort(), new wb_gh_sort_hot(), new wb_gh_sort_on(), new wb_gh_label(),
                       new wb_gh_caret(), new wb_gh_order(), new wb_gh_filter(), new wb_gh_filter_hot(), new wb_gh_filter_on(),
                       new wb_gh_filter_open(), new wb_gpop(), new wb_gpop_title(), new wb_gpop_row(), new wb_gpop_input(),
                       new wb_gpop_list(), new wb_gpop_check(), new wb_gpop_actions(), new wb_gpop_btn());
    }
}
