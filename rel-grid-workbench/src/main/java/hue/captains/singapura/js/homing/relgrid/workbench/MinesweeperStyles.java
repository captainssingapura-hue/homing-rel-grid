package hue.captains.singapura.js.homing.relgrid.workbench;

import hue.captains.singapura.js.homing.core.CssClass;
import hue.captains.singapura.js.homing.core.CssGroup;
import hue.captains.singapura.js.homing.core.CssVar;

import java.util.List;
import java.util.Set;

/**
 * The Minesweeper tiles, Vista's: each mine works out its own design (RFC 0050 · Episode 2, appendix The Frame) - a
 * glossy blue tile while hidden, a pale one revealed, its count in the game's own inks, a mine on red where it went
 * off. The game's own colours, in every theme: the board's frame is the design's, tinted by the palette. And the
 * board's host and the band above it.
 */
public record MinesweeperStyles() implements CssGroup<MinesweeperStyles> {

    public static final MinesweeperStyles INSTANCE = new MinesweeperStyles();

    /** A tile: a small rounded square, its glyph or count centred, a little air round it for the frame's marks to show in. */
    public record ms_tile() implements CssClass<MinesweeperStyles> {
        @Override public String body() { return """
                display: flex;
                align-items: center;
                justify-content: center;
                width: 24px;
                height: 24px;
                margin: 2px;
                box-sizing: border-box;
                border-radius: 4px;
                border: 1px solid;
                font: 700 14px/1 "Segoe UI", system-ui, sans-serif;
                user-select: none;
                cursor: default;
                """;
        }
    }

    /** Hidden: Vista's glossy blue, lit from above, a little brighter under the hand. */
    public record ms_hidden() implements CssClass<MinesweeperStyles> {
        @Override public String body() { return """
                background: linear-gradient(180deg, #eef6ff 0%, #c6dcf5 46%, #a9c9ef 54%, #cfe2f8 100%);
                border-color: #7f9fca;
                box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.85), 0 1px 1px rgba(30, 60, 110, 0.18);
                color: transparent;
                &:hover { filter: brightness(1.07) saturate(1.1); }
                """;
        }
    }

    /** Flagged: the hidden tile, a red flag on it. */
    public record ms_flagged() implements CssClass<MinesweeperStyles> {
        @Override public String body() { return """
                background: linear-gradient(180deg, #eef6ff 0%, #c6dcf5 46%, #a9c9ef 54%, #cfe2f8 100%);
                border-color: #7f9fca;
                box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.85), 0 1px 1px rgba(30, 60, 110, 0.18);
                color: #c62424;
                """;
        }
    }

    /** Revealed: pale and pressed in. */
    public record ms_revealed() implements CssClass<MinesweeperStyles> {
        @Override public String body() { return """
                background: #f5f8fc;
                border-color: #d4dde9;
                box-shadow: inset 0 1px 2px rgba(30, 60, 110, 0.12);
                """;
        }
    }

    /** A mine shown when the board is lost. */
    public record ms_mine() implements CssClass<MinesweeperStyles> {
        @Override public String body() { return """
                background: #eef2f8;
                border-color: #c9d3e0;
                color: #1d2433;
                """;
        }
    }

    /** The mine that went off. */
    public record ms_boom() implements CssClass<MinesweeperStyles> {
        @Override public String body() { return """
                background: radial-gradient(circle, #ff8a7a 0%, #d8322a 72%);
                border-color: #a51f18;
                color: #ffffff;
                """;
        }
    }

    /** A flag that was wrong, shown when the board is lost. */
    public record ms_wrong() implements CssClass<MinesweeperStyles> {
        @Override public String body() { return """
                background: #f5f8fc;
                border-color: #d4dde9;
                color: #c62424;
                """;
        }
    }

    // ── the counts' inks, Minesweeper's own ───────────────────────────────

    public record ms_n1() implements CssClass<MinesweeperStyles> { @Override public String body() { return "color: #2a4fd6;\n"; } }
    public record ms_n2() implements CssClass<MinesweeperStyles> { @Override public String body() { return "color: #2b8a2b;\n"; } }
    public record ms_n3() implements CssClass<MinesweeperStyles> { @Override public String body() { return "color: #d0362a;\n"; } }
    public record ms_n4() implements CssClass<MinesweeperStyles> { @Override public String body() { return "color: #1c2d8f;\n"; } }
    public record ms_n5() implements CssClass<MinesweeperStyles> { @Override public String body() { return "color: #8a1f1f;\n"; } }
    public record ms_n6() implements CssClass<MinesweeperStyles> { @Override public String body() { return "color: #1f8686;\n"; } }
    public record ms_n7() implements CssClass<MinesweeperStyles> { @Override public String body() { return "color: #222222;\n"; } }
    public record ms_n8() implements CssClass<MinesweeperStyles> { @Override public String body() { return "color: #777777;\n"; } }

    // ── the board and its band ────────────────────────────────────────────

    /**
     * The board's host: the frame's edge is drawn on it, with room inside for the tiles to stand clear of it - exactly
     * as wide as its columns, so the table's 100% is their sum. The width is handed in by the game, which knows the board.
     */
    public record ms_board() implements CssClass<MinesweeperStyles> {
        @Override public Set<CssVar> runtimeVars() { return Set.of(new CssVar("--ms-board-width")); }
        @Override public String body() { return """
                flex: 0 0 auto;
                align-self: flex-start;
                width: var(--ms-board-width, auto);
                padding: 8px;
                box-sizing: content-box;
                """;
        }
    }

    /** The band over the board: the mines left, the state, the restart. */
    public record ms_band() implements CssClass<MinesweeperStyles> {
        @Override public String body() { return """
                display: flex;
                align-items: center;
                gap: 14px;
                font-variant-numeric: tabular-nums;
                """;
        }
    }

    @Override public List<CssClass<MinesweeperStyles>> cssClasses() {
        return List.of(new ms_tile(), new ms_hidden(), new ms_flagged(), new ms_revealed(), new ms_mine(), new ms_boom(), new ms_wrong(),
                       new ms_n1(), new ms_n2(), new ms_n3(), new ms_n4(), new ms_n5(), new ms_n6(), new ms_n7(), new ms_n8(),
                       new ms_board(), new ms_band());
    }
}
