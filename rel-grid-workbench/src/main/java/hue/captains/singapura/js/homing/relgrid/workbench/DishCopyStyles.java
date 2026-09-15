package hue.captains.singapura.js.homing.relgrid.workbench;

import hue.captains.singapura.js.homing.core.CssClass;
import hue.captains.singapura.js.homing.core.CssGroup;

import java.util.List;

/**
 * The copy panel's looks, typed. Laid out for the SMALLEST box the grid will
 * mint — 320 × 198 — because a wide, short table binds the panel's height at
 * a fifth of a short host, and a panel that only works in a generous box is
 * not a panel for a bench.
 */
public record DishCopyStyles() implements CssGroup<DishCopyStyles> {

    public static final DishCopyStyles INSTANCE = new DishCopyStyles();

    public record wb_copy() implements CssClass<DishCopyStyles> {
        @Override public String body() { return """
                display: flex;
                flex-direction: column;
                gap: 6px;
                height: 100%;
                box-sizing: border-box;
                padding: 10px 14px;
                font: 12px sans-serif;
                outline: none;
                """;
        }
    }

    public record wb_copy_head() implements CssClass<DishCopyStyles> {
        @Override public String body() { return """
                display: flex;
                align-items: baseline;
                gap: 8px;
                white-space: nowrap;
                overflow: hidden;
                """;
        }
    }

    public record wb_copy_title() implements CssClass<DishCopyStyles> {
        @Override public String body() { return """
                font-size: 14px;
                font-weight: 600;
                """;
        }
    }

    public record wb_copy_sub() implements CssClass<DishCopyStyles> {
        @Override public String body() { return """
                color: var(--color-text-muted);
                font-size: 11px;
                overflow: hidden;
                text-overflow: ellipsis;
                """;
        }
    }

    public record wb_copy_opts() implements CssClass<DishCopyStyles> {
        @Override public String body() { return """
                display: flex;
                gap: 6px;
                """;
        }
    }

    public record wb_copy_opt() implements CssClass<DishCopyStyles> {
        @Override public String body() { return """
                flex: 1 1 0;
                padding: 5px 0;
                border: 1px solid var(--color-border);
                border-radius: 5px;
                background: var(--color-surface);
                color: var(--color-text-primary);
                cursor: pointer;
                font: inherit;
                font-weight: 600;
                letter-spacing: 0.5px;
                text-align: center;
                """;
        }
    }

    /** A format under the pointer or the focus. Worn beside wb_copy_opt. */
    public record wb_copy_opt_hot() implements CssClass<DishCopyStyles> {
        @Override public String pseudoState() { return ":is(:hover, :focus)"; }
        @Override public String body() { return """
                border-color: var(--color-accent);
                outline: none;
                box-shadow: 0 0 0 2px color-mix(in srgb, var(--color-accent) 35%, transparent);
                """;
        }
    }

    /** One line, for whichever format is under the focus or the pointer. */
    public record wb_copy_hint() implements CssClass<DishCopyStyles> {
        @Override public String body() { return """
                color: var(--color-text-muted);
                font-size: 11px;
                white-space: nowrap;
                overflow: hidden;
                text-overflow: ellipsis;
                """;
        }
    }

    public record wb_copy_preview() implements CssClass<DishCopyStyles> {
        @Override public String body() { return """
                flex: 1 1 auto;
                min-height: 0;
                overflow: hidden;
                margin: 0;
                padding: 4px 8px;
                border: 1px dashed var(--color-border);
                border-radius: 4px;
                font: 10px/1.35 monospace;
                white-space: pre;
                color: var(--color-text-muted);
                """;
        }
    }

    public record wb_copy_foot() implements CssClass<DishCopyStyles> {
        @Override public String body() { return """
                display: flex;
                align-items: center;
                gap: 10px;
                color: var(--color-text-muted);
                font-size: 10px;
                white-space: nowrap;
                """;
        }
    }

    public record wb_copy_label() implements CssClass<DishCopyStyles> {
        @Override public String body() { return """
                display: flex;
                align-items: center;
                gap: 4px;
                cursor: pointer;
                color: var(--color-text-primary);
                font-size: 11px;
                """;
        }
    }

    public record wb_copy_check() implements CssClass<DishCopyStyles> {
        @Override public String body() { return """
                margin: 0;
                """;
        }
    }

    public record wb_copy_keys() implements CssClass<DishCopyStyles> {
        @Override public String body() { return """
                flex: 1 1 auto;
                text-align: right;
                overflow: hidden;
                text-overflow: ellipsis;
                """;
        }
    }

    public record wb_copy_cancel() implements CssClass<DishCopyStyles> {
        @Override public String body() { return """
                font: inherit;
                font-size: 11px;
                padding: 2px 9px;
                border: 1px solid var(--color-border);
                border-radius: 4px;
                background: transparent;
                color: var(--color-text-primary);
                cursor: pointer;
                """;
        }
    }

    @Override public List<CssClass<DishCopyStyles>> cssClasses() {
        return List.of(new wb_copy(), new wb_copy_head(), new wb_copy_title(), new wb_copy_sub(), new wb_copy_opts(),
                       new wb_copy_opt(), new wb_copy_opt_hot(), new wb_copy_hint(), new wb_copy_preview(), new wb_copy_foot(),
                       new wb_copy_label(), new wb_copy_check(), new wb_copy_keys(), new wb_copy_cancel());
    }
}
