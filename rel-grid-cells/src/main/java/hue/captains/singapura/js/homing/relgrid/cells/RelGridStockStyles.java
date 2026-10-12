package hue.captains.singapura.js.homing.relgrid.cells;

import hue.captains.singapura.js.homing.core.CssClass;
import hue.captains.singapura.js.homing.core.CssGroup;

import java.util.List;

/**
 * RFC 0050 · Episode 2 — Appendix: The Frame — the stock cells' own room and their overflow, typed. A slot has no
 * room of its own: the cell pads itself, and says what it does with text too wide for its column. What the cells
 * say of ink, face and weight is not here: it is the house's, worn as a table's cells wear it ({@code DataCell},
 * {@code HeaderCell}), so a stock cell reads in every house style as a table's cell does. DOMAIN-side, as the cells
 * are: the grid never imports these.
 */
public record RelGridStockStyles() implements CssGroup<RelGridStockStyles> {

    public static final RelGridStockStyles INSTANCE = new RelGridStockStyles();

    /**
     * The text cell's element: its room, and a clip, so its text never widens the column - the slot only places it.
     * The editor wears the same room, so nothing jumps when a cell is entered; a label above wears it too, so the
     * header's words stand over the column's.
     */
    public record hrg_text() implements CssClass<RelGridStockStyles> {
        @Override public String body() { return """
                padding: 0 10px;
                overflow: hidden;
                """;
        }
    }

    /**
     * A cell that cannot edit names the property (map 16, law 116): an uneditable cell has no resting affordance to
     * be missing. A fact; whether it is painted is a design's business.
     */
    public record hrg_text_ro() implements CssClass<RelGridStockStyles> {
        @Override public String body() { return ""; }
    }

    /**
     * The editor: fills the anchor the grid placed it in, which is already laid over the slot and OUTSIDE the
     * table, on the anchor's surface, with the same room as the text it replaces.
     */
    public record hrg_text_edit() implements CssClass<RelGridStockStyles> {
        @Override public String body() { return """
                box-sizing: border-box;
                width: 100%;
                height: 100%;
                border: 0;
                padding: 0 10px;
                background: transparent;
                color: inherit;
                font: inherit;
                user-select: text;
                -webkit-user-select: text;
                """;
        }
    }

    /** The label header cell's element: the header's room, the cell's sideways, so a label stands over its column. */
    public record hrg_label() implements CssClass<RelGridStockStyles> {
        @Override public String body() { return """
                padding: 6px 10px;
                overflow: hidden;
                """;
        }
    }

    /**
     * Text too wide for the column ends in an ellipsis: the default, because wrapping makes one row six lines tall
     * while its neighbours stay at one. It engages once the column has a definite width.
     */
    public record hrg_text_ellipsis() implements CssClass<RelGridStockStyles> {
        @Override public String body() { return """
                white-space: nowrap;
                text-overflow: ellipsis;
                """;
        }
    }

    /** Text too wide for the column is clipped, without an ellipsis. */
    public record hrg_text_clip() implements CssClass<RelGridStockStyles> {
        @Override public String body() { return """
                white-space: nowrap;
                text-overflow: clip;
                """;
        }
    }

    /** Text too wide for the column wraps, and the row grows. */
    public record hrg_text_wrap() implements CssClass<RelGridStockStyles> {
        @Override public String body() { return """
                white-space: normal;
                overflow-wrap: anywhere;
                """;
        }
    }

    @Override public List<CssClass<RelGridStockStyles>> cssClasses() {
        return List.of(new hrg_text(), new hrg_text_ro(), new hrg_text_edit(), new hrg_label(),
                new hrg_text_ellipsis(), new hrg_text_clip(), new hrg_text_wrap());
    }
}
