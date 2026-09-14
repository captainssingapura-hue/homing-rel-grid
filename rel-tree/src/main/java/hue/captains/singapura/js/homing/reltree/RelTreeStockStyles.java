package hue.captains.singapura.js.homing.reltree;

import hue.captains.singapura.js.homing.core.CssClass;
import hue.captains.singapura.js.homing.core.CssGroup;
import hue.captains.singapura.js.homing.core.CssImportsFor;

import java.util.List;

/** The stock text cell's looks: a line that clips, and the mode it is told. Domain-side, as the cell is. */
public record RelTreeStockStyles() implements CssGroup<RelTreeStockStyles> {

    public static final RelTreeStockStyles INSTANCE = new RelTreeStockStyles();

    public record hrt_text_cell() implements CssClass<RelTreeStockStyles> {
        @Override public String body() { return """
                flex: 1 1 auto;
                min-width: 0;
                overflow: hidden;
                text-overflow: ellipsis;
                """;
        }
    }

    /** The cell was told it is current. Worn, not decided: the tree paints the row, the cell may add. */
    public record hrt_text_cell_current() implements CssClass<RelTreeStockStyles> {
        @Override public String body() { return """
                font-weight: 600;
                """;
        }
    }

    @Override public CssImportsFor<RelTreeStockStyles> cssImports() { return CssImportsFor.none(this); }

    @Override public List<CssClass<RelTreeStockStyles>> cssClasses() {
        return List.of(new hrt_text_cell(), new hrt_text_cell_current());
    }
}
