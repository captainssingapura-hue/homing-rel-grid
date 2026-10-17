package hue.captains.singapura.js.homing.relgrid.prose;

import hue.captains.singapura.js.homing.core.CssClass;
import hue.captains.singapura.js.homing.core.CssGroup;

import java.util.List;

/**
 * A document's table's own mechanics, typed: never its looks, which are the house's - the data and header cells, the
 * caption, the badges, worn - nor its rules, which its design draws on the slots. What stays here is what no style
 * could want otherwise: a wide table scrolling across within its box, a cell's text wrapping as prose does, a cell set
 * to its alignment, a badge's box.
 */
public record ProseStyles() implements CssGroup<ProseStyles> {

    public static final ProseStyles INSTANCE = new ProseStyles();

    /** The table: as wide as what holds it lets it be, never wider. */
    public record prose_table() implements CssClass<ProseStyles> {
        @Override public String body() { return "min-width: 0;\nmax-width: 100%;\n"; }
    }

    /** Its caption, above it, set off from it. */
    public record prose_caption() implements CssClass<ProseStyles> {
        @Override public String body() { return "margin: 0 0 6px;\ntext-align: left;\n"; }
    }

    /**
     * The box the grid stands in: a table wider than it scrolls across, inside it; and its rows set as a document's
     * table sets them - a cell's text at the top of its row, however tall a neighbour wraps, a head's at its foot.
     */
    public record prose_box() implements CssClass<ProseStyles> {
        @Override public String body() { return """
                overflow-x: auto;
                min-width: 0;
                & td { vertical-align: top; }
                & th { vertical-align: bottom; }
                """;
        }
    }

    /** A data cell: its text wraps as prose does, a long word broken rather than the column widened past the page. */
    public record prose_cell() implements CssClass<ProseStyles> {
        @Override public String body() { return "white-space: normal;\noverflow-wrap: break-word;\n"; }
    }

    /** A header cell: its title wraps as a document's table's head does. */
    public record prose_head() implements CssClass<ProseStyles> {
        @Override public String body() { return "white-space: normal;\noverflow-wrap: break-word;\n"; }
    }

    public record prose_left() implements CssClass<ProseStyles> { @Override public String body() { return "text-align: left;\n"; } }
    public record prose_center() implements CssClass<ProseStyles> { @Override public String body() { return "text-align: center;\n"; } }
    public record prose_right() implements CssClass<ProseStyles> { @Override public String body() { return "text-align: right;\n"; } }

    /** A badge's box: inline, a block of its own, so its room and its corner hold - and one line, never a pill broken in two. */
    public record prose_badge() implements CssClass<ProseStyles> {
        @Override public String body() { return "display: inline-block;\nwhite-space: nowrap;\n"; }
    }

    @Override public List<CssClass<ProseStyles>> cssClasses() {
        return List.of(new prose_table(), new prose_caption(), new prose_box(), new prose_cell(), new prose_head(),
                new prose_left(), new prose_center(), new prose_right(), new prose_badge());
    }
}
