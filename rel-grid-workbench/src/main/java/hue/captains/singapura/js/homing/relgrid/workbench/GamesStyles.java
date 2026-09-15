package hue.captains.singapura.js.homing.relgrid.workbench;

import hue.captains.singapura.js.homing.core.CssClass;
import hue.captains.singapura.js.homing.core.CssGroup;
import hue.captains.singapura.js.homing.core.CssImportsFor;

import java.util.List;

/**
 * The Games Catalogue's header cells and their column menus, typed. A header
 * cell is a label, an indication — caret, key number, filter mark — and one
 * control, the ▾, laid out to fill the header slot the grid places it in; the
 * grid's resize handle sits on the slot's right edge, over the ▾'s margin.
 * The menu is fixed to the viewport, placed by the header's own rectangle
 * through two custom properties, above the sticky header and the grid's mask.
 */
public record GamesStyles() implements CssGroup<GamesStyles> {

    public static final GamesStyles INSTANCE = new GamesStyles();

    // ── the header cell ───────────────────────────────────────────────────

    public record wb_gh() implements CssClass<GamesStyles> {
        @Override public String body() { return """
                display: flex;
                align-items: center;
                gap: 3px;
                min-width: 0;
                margin: -6px -10px;
                padding: 6px 10px 6px 10px;
                font: inherit;
                """;
        }
    }

    /** The label is an indication, not a control: it fills the width and says what the column is. */
    public record wb_gh_label() implements CssClass<GamesStyles> {
        @Override public String body() { return """
                flex: 1 1 auto;
                min-width: 0;
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

    /** The key's place when several sort — a small number after the caret. */
    public record wb_gh_order() implements CssClass<GamesStyles> {
        @Override public String body() { return """
                flex: 0 0 auto;
                font-size: 9px;
                color: var(--color-text-muted);
                """;
        }
    }

    /** A mark while a filter is held on the column: a magnifier — an emoji, so its colour is the platform's, not the theme's. */
    public record wb_gh_mark() implements CssClass<GamesStyles> {
        @Override public String body() { return """
                flex: 0 0 auto;
                font-size: 11px;
                line-height: 1;
                """;
        }
    }

    /** The one control: the ▾ that opens the column's menu; lit while the column sorts or filters, boxed while open. */
    public record wb_gh_menu() implements CssClass<GamesStyles> {
        @Override public String body() { return """
                flex: 0 0 auto;
                width: 18px;
                height: 18px;
                margin-right: 6px;
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

    public record wb_gh_menu_hot() implements CssClass<GamesStyles> {
        @Override public String pseudoState() { return ":is(:hover, :focus-visible)"; }
        @Override public String body() { return """
                border-color: var(--color-border);
                color: var(--color-text-primary);
                outline: none;
                """;
        }
    }

    public record wb_gh_menu_on() implements CssClass<GamesStyles> {
        @Override public String body() { return """
                color: var(--color-accent);
                """;
        }
    }

    public record wb_gh_menu_open() implements CssClass<GamesStyles> {
        @Override public String body() { return """
                border-color: var(--color-accent);
                background: color-mix(in srgb, var(--color-accent) 14%, transparent);
                """;
        }
    }

    // ── the column menu ───────────────────────────────────────────────────

    /** Fixed to the viewport, where the header cell said; above the sticky header (35) and the mask (60). */
    public record wb_gmenu() implements CssClass<GamesStyles> {
        @Override public String body() { return """
                position: fixed;
                left: var(--wb-gmenu-left, 0px);
                top: var(--wb-gmenu-top, 0px);
                z-index: 70;
                width: 280px;
                max-height: 70vh;
                display: flex;
                flex-direction: column;
                gap: 2px;
                padding: 6px;
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

    /** A menu item: the sort choices — a row that reads as a line of a menu, not a button. */
    public record wb_gmenu_item() implements CssClass<GamesStyles> {
        @Override public String body() { return """
                display: block;
                width: 100%;
                padding: 5px 8px;
                border: 0;
                border-radius: 4px;
                background: transparent;
                color: inherit;
                font: inherit;
                text-align: left;
                cursor: pointer;
                """;
        }
    }

    public record wb_gmenu_item_hot() implements CssClass<GamesStyles> {
        @Override public String pseudoState() { return ":is(:hover, :focus-visible)"; }
        @Override public String body() { return """
                background: color-mix(in srgb, var(--color-accent) 12%, transparent);
                outline: none;
                """;
        }
    }

    public record wb_gmenu_item_on() implements CssClass<GamesStyles> {
        @Override public String body() { return """
                color: var(--color-accent);
                font-weight: 600;
                """;
        }
    }

    /**
     * A checkbox row: "then by", "(select all)", and every value. A flex item of
     * the list, which is a column with a ceiling — so it must NOT shrink: with
     * overflow hidden its minimum height is nothing, and a row that may shrink
     * is shrunk to fit the ceiling instead of the list scrolling. flex none.
     */
    public record wb_gmenu_check() implements CssClass<GamesStyles> {
        @Override public String body() { return """
                flex: 0 0 auto;
                display: flex;
                align-items: center;
                gap: 6px;
                padding: 3px 8px;
                line-height: 18px;
                border-radius: 4px;
                cursor: pointer;
                white-space: nowrap;
                overflow: hidden;
                """;
        }
    }

    /** A value row the search does not match. Its own class, since a flex row would win over the hidden attribute. */
    public record wb_gmenu_hidden() implements CssClass<GamesStyles> {
        @Override public String body() { return """
                display: none;
                """;
        }
    }

    public record wb_gmenu_sep() implements CssClass<GamesStyles> {
        @Override public String body() { return """
                height: 1px;
                margin: 4px 2px;
                background: var(--color-border);
                """;
        }
    }

    public record wb_gmenu_search() implements CssClass<GamesStyles> {
        @Override public String body() { return """
                flex: 1 1 0;
                min-width: 0;
                width: 100%;
                box-sizing: border-box;
                margin: 2px 0;
                padding: 4px 8px;
                font: inherit;
                color: var(--color-text-primary);
                background: var(--color-surface);
                border: 1px solid var(--color-border);
                border-radius: 4px;
                """;
        }
    }

    public record wb_gmenu_range() implements CssClass<GamesStyles> {
        @Override public String body() { return """
                display: flex;
                gap: 6px;
                """;
        }
    }

    /** The values: scrolls on its own, so the sort items and the actions stay in reach. */
    public record wb_gmenu_list() implements CssClass<GamesStyles> {
        @Override public String body() { return """
                display: flex;
                flex-direction: column;
                gap: 1px;
                min-height: 60px;
                max-height: 36vh;
                overflow: auto;
                border: 1px solid var(--color-border);
                border-radius: 4px;
                padding: 2px;
                """;
        }
    }

    public record wb_gmenu_count() implements CssClass<GamesStyles> {
        @Override public String body() { return """
                margin-left: auto;
                padding-left: 8px;
                color: var(--color-text-muted);
                font-size: 11px;
                """;
        }
    }

    public record wb_gmenu_actions() implements CssClass<GamesStyles> {
        @Override public String body() { return """
                display: flex;
                justify-content: flex-end;
                gap: 6px;
                padding-top: 6px;
                margin-top: 2px;
                border-top: 1px solid var(--color-border);
                """;
        }
    }

    public record wb_gmenu_btn() implements CssClass<GamesStyles> {
        @Override public String body() { return """
                padding: 4px 10px;
                cursor: pointer;
                font: inherit;
                background: var(--color-surface);
                color: var(--color-text-primary);
                border: 1px solid var(--color-border);
                border-radius: 4px;
                """;
        }
    }

    public record wb_gmenu_btn_primary() implements CssClass<GamesStyles> {
        @Override public String body() { return """
                background: var(--color-accent);
                color: var(--color-surface);
                border-color: var(--color-accent);
                font-weight: 600;
                """;
        }
    }

    @Override public CssImportsFor<GamesStyles> cssImports() { return CssImportsFor.none(this); }

    @Override public List<CssClass<GamesStyles>> cssClasses() {
        return List.of(new wb_gh(), new wb_gh_label(), new wb_gh_caret(), new wb_gh_order(), new wb_gh_mark(),
                       new wb_gh_menu(), new wb_gh_menu_hot(), new wb_gh_menu_on(), new wb_gh_menu_open(),
                       new wb_gmenu(), new wb_gmenu_item(), new wb_gmenu_item_hot(), new wb_gmenu_item_on(), new wb_gmenu_check(),
                       new wb_gmenu_hidden(), new wb_gmenu_sep(), new wb_gmenu_search(), new wb_gmenu_range(), new wb_gmenu_list(),
                       new wb_gmenu_count(), new wb_gmenu_actions(), new wb_gmenu_btn(), new wb_gmenu_btn_primary());
    }
}
