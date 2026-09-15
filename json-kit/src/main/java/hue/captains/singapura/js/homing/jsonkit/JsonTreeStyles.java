package hue.captains.singapura.js.homing.jsonkit;

import hue.captains.singapura.js.homing.core.CssClass;
import hue.captains.singapura.js.homing.core.CssGroup;
import hue.captains.singapura.js.homing.core.CssImportsFor;

import java.util.List;

/**
 * The JSON node cell's looks, typed: the line, the name, the colon, and one
 * class per kind of value, so a theme colours the kinds through its tokens.
 * Domain-side, as the cell is. No stylesheet anywhere: one rule per record.
 */
public record JsonTreeStyles() implements CssGroup<JsonTreeStyles> {

    public static final JsonTreeStyles INSTANCE = new JsonTreeStyles();

    /** The cell: one line that clips, filling the row after the caret. */
    public record jk_cell() implements CssClass<JsonTreeStyles> {
        @Override public String body() { return """
                flex: 1 1 auto;
                min-width: 0;
                overflow: hidden;
                white-space: nowrap;
                text-overflow: ellipsis;
                font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
                font-size: 12px;
                """;
        }
    }

    /** The current row's cell: weighted, as the stock cell's is. */
    public record jk_cell_current() implements CssClass<JsonTreeStyles> {
        @Override public String body() { return """
                font-weight: 600;
                """;
        }
    }

    /** A member's name. */
    public record jk_key() implements CssClass<JsonTreeStyles> {
        @Override public String body() { return """
                color: var(--color-text-primary);
                """;
        }
    }

    /** An element's index: quieter than a name, since it is a position. */
    public record jk_index() implements CssClass<JsonTreeStyles> {
        @Override public String body() { return """
                color: var(--color-text-muted);
                """;
        }
    }

    /** The colon. */
    public record jk_punct() implements CssClass<JsonTreeStyles> {
        @Override public String body() { return """
                color: var(--color-text-muted);
                """;
        }
    }

    /** A string, quoted. */
    public record jk_string() implements CssClass<JsonTreeStyles> {
        @Override public String body() { return """
                color: color-mix(in srgb, var(--color-accent) 70%, var(--color-text-primary));
                """;
        }
    }

    /** A number. */
    public record jk_number() implements CssClass<JsonTreeStyles> {
        @Override public String body() { return """
                color: var(--color-text-primary);
                """;
        }
    }

    /** true, false and null. */
    public record jk_literal() implements CssClass<JsonTreeStyles> {
        @Override public String body() { return """
                color: var(--color-accent);
                font-style: italic;
                """;
        }
    }

    /** A container, printed as a count: {3}, [5]. */
    public record jk_container() implements CssClass<JsonTreeStyles> {
        @Override public String body() { return """
                color: var(--color-text-muted);
                """;
        }
    }

    @Override public CssImportsFor<JsonTreeStyles> cssImports() { return CssImportsFor.none(this); }

    @Override public List<CssClass<JsonTreeStyles>> cssClasses() {
        return List.of(new jk_cell(), new jk_cell_current(), new jk_key(), new jk_index(), new jk_punct(),
                       new jk_string(), new jk_number(), new jk_literal(), new jk_container());
    }
}
