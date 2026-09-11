package hue.captains.singapura.js.homing.relgrid.workbench;

import hue.captains.singapura.js.homing.core.CssClass;
import hue.captains.singapura.js.homing.core.CssGroup;
import hue.captains.singapura.js.homing.core.CssImportsFor;

import java.util.List;

/** The bench widgets' chrome: a column root, a scrolling host, a hint, a button bar, a readout. */
public record WorkbenchStyles() implements CssGroup<WorkbenchStyles> {

    public static final WorkbenchStyles INSTANCE = new WorkbenchStyles();

    public record wb_root() implements CssClass<WorkbenchStyles> {
        @Override public String body() { return """
                display: flex;
                flex-direction: column;
                height: 100%;
                width: 100%;
                min-height: 0;
                padding: 10px;
                box-sizing: border-box;
                gap: 8px;
                """;
        }
    }

    /**
     * The scrollport — flex:1 so it takes the pane's leftover height and scrolls.
     * A floor, because the grid's copy panel is centred on what this shows of
     * the table and clipped by it: below about 240px a panel that can hold
     * anything no longer fits, which is the host's geometry and not the grid's.
     */
    public record wb_host() implements CssClass<WorkbenchStyles> {
        @Override public String body() { return """
                flex: 1;
                min-height: 240px;
                overflow: auto;
                border: 1px solid var(--color-border);
                border-radius: 6px;
                """;
        }
    }

    public record wb_hint() implements CssClass<WorkbenchStyles> {
        @Override public String body() { return """
                font: 12px sans-serif;
                color: var(--color-text-muted);
                """;
        }
    }

    public record wb_bar() implements CssClass<WorkbenchStyles> {
        @Override public String body() { return """
                display: flex;
                gap: 8px;
                flex-wrap: wrap;
                """;
        }
    }

    public record wb_btn() implements CssClass<WorkbenchStyles> {
        @Override public String body() { return """
                padding: 4px 10px;
                cursor: pointer;
                font: 12px sans-serif;
                background: var(--color-surface-raised);
                color: var(--color-text-primary);
                border: 1px solid var(--color-border);
                border-radius: 4px;
                """;
        }
    }

    public record wb_status() implements CssClass<WorkbenchStyles> {
        @Override public String body() { return """
                font: 12px monospace;
                color: var(--color-text-muted);
                white-space: pre-line;
                max-height: 90px;
                overflow: auto;
                """;
        }
    }

    @Override
    public List<CssClass<WorkbenchStyles>> cssClasses() {
        return List.of(new wb_root(), new wb_host(), new wb_hint(), new wb_bar(), new wb_btn(), new wb_status());
    }

    @Override
    public CssImportsFor<WorkbenchStyles> cssImports() {
        return CssImportsFor.none(this);
    }
}
