package hue.captains.singapura.js.homing.relgrid.workbench;

import hue.captains.singapura.js.homing.core.CssClass;
import hue.captains.singapura.js.homing.core.CssGroup;
import hue.captains.singapura.js.homing.core.CssImportsFor;

import java.util.List;

/** The Outlets fences' looks, typed: a name, a fold toggle, published totals; the ledger's band below the last. */
public record OutletFenceStyles() implements CssGroup<OutletFenceStyles> {

    public static final OutletFenceStyles INSTANCE = new OutletFenceStyles();

    public record wb_fence() implements CssClass<OutletFenceStyles> {
        @Override public String body() { return """
                display: flex;
                align-items: baseline;
                gap: 12px;
                padding: 10px 10px 4px;
                font: 12px sans-serif;
                color: var(--color-text-primary);
                """;
        }
    }

    public record wb_fence_name() implements CssClass<OutletFenceStyles> {
        @Override public String body() { return """
                font-size: 14px;
                font-weight: 600;
                """;
        }
    }

    /** The toggle: a triangle, down while the book shows and right while it is folded. */
    public record wb_fence_fold() implements CssClass<OutletFenceStyles> {
        @Override public String body() { return """
                border: 0;
                background: transparent;
                cursor: pointer;
                padding: 0 4px;
                margin: 0;
                font: inherit;
                font-size: 11px;
                line-height: 1;
                color: var(--color-text-muted);
                width: 1.4em;
                text-align: center;
                """;
        }
    }

    /** The toggle under the pointer. Worn beside wb_fence_fold. */
    public record wb_fence_fold_hot() implements CssClass<OutletFenceStyles> {
        @Override public String pseudoState() { return ":hover"; }
        @Override public String body() { return """
                color: var(--color-accent);
                """;
        }
    }

    public record wb_fence_totals() implements CssClass<OutletFenceStyles> {
        @Override public String body() { return """
                color: var(--color-text-muted);
                font-size: 11px;
                font-variant-numeric: tabular-nums;
                """;
        }
    }

    /** The ledger's band: ruled off from the last book. Listed after wb_fence. */
    public record wb_fence_ledger() implements CssClass<OutletFenceStyles> {
        @Override public String body() { return """
                border-top: 2px solid var(--color-border);
                margin-top: 6px;
                padding-top: 8px;
                """;
        }
    }

    @Override public CssImportsFor<OutletFenceStyles> cssImports() { return CssImportsFor.none(this); }

    @Override public List<CssClass<OutletFenceStyles>> cssClasses() {
        return List.of(new wb_fence(), new wb_fence_name(), new wb_fence_fold(), new wb_fence_fold_hot(),
                       new wb_fence_totals(), new wb_fence_ledger());
    }
}
