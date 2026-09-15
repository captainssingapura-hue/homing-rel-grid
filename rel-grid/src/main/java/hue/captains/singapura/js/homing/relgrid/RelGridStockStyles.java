package hue.captains.singapura.js.homing.relgrid;

import hue.captains.singapura.js.homing.core.CssClass;
import hue.captains.singapura.js.homing.core.CssGroup;
import hue.captains.singapura.js.homing.core.CssImportsFor;

import java.util.List;

/**
 * RFC 0050 · Episode 2 — the stock text cell's looks, typed. DOMAIN-side, as
 * the cell is: the grid never imports these. A separate group from the grid's
 * own so the two stay apart the way the two branches do.
 */
public record RelGridStockStyles() implements CssGroup<RelGridStockStyles> {

    public static final RelGridStockStyles INSTANCE = new RelGridStockStyles();

    /**
     * The cell's element. It honours what the slot asks of its text: nowrap is
     * inherited from the table, and the ellipsis — {@code --hrg-text-overflow},
     * the grid's published property — is drawn here, because the text is this
     * element's and the slot only clips. The padding is the cell's too — the
     * slot has none — and it is the header's, so the text lines up under it;
     * the editor wears the same, so nothing jumps when a cell is entered.
     */
    public record hrg_text() implements CssClass<RelGridStockStyles> {
        @Override public String body() { return """
                padding: 0 10px;
                overflow: hidden;
                text-overflow: var(--hrg-text-overflow, clip);
                """;
        }
    }

    /**
     * A cell that cannot edit names the property (map 16, law 116): an
     * uneditable cell has no resting affordance to be missing. Whether it is
     * painted is the theme's business; this is the muted default.
     */
    public record hrg_text_ro() implements CssClass<RelGridStockStyles> {
        @Override public String body() { return """
                color: var(--color-text-muted);
                """;
        }
    }

    /** The editor: fills the anchor the grid placed it in, which is already laid over the slot and OUTSIDE the table. */
    public record hrg_text_edit() implements CssClass<RelGridStockStyles> {
        @Override public String body() { return """
                box-sizing: border-box;
                width: 100%;
                height: 100%;
                border: 0;
                padding: 0 10px;
                font: 13px sans-serif;
                background: transparent;
                color: var(--color-text-primary);
                user-select: text;
                -webkit-user-select: text;
                """;
        }
    }

    @Override public CssImportsFor<RelGridStockStyles> cssImports() { return CssImportsFor.none(this); }

    @Override public List<CssClass<RelGridStockStyles>> cssClasses() {
        return List.of(new hrg_text(), new hrg_text_ro(), new hrg_text_edit());
    }
}
