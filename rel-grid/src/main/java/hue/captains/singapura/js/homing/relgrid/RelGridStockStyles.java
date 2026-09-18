package hue.captains.singapura.js.homing.relgrid;

import hue.captains.singapura.js.homing.core.CssClass;
import hue.captains.singapura.js.homing.core.CssGroup;
import hue.captains.singapura.js.homing.core.Wearable;

import java.util.List;

import static hue.captains.singapura.js.homing.design.DesignClass.of;
import static hue.captains.singapura.js.homing.design.Target.*;
import static hue.captains.singapura.js.homing.design.Emphasis.*;
import static hue.captains.singapura.js.homing.design.Text.*;

/**
 * RFC 0050 · Episode 2 — the stock text cell's looks, typed. DOMAIN-side, as
 * the cell is: the grid never imports these. A separate group from the grid's
 * own so the two stay apart the way the two branches do. What it says of
 * colour and type is a design's word it wears (RFC 0065), never a value.
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
     * painted is the design's business: the muted ink.
     */
    public record hrg_text_ro() implements CssClass<RelGridStockStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Muted.class, Color.Ink.class)); }
        @Override public String body() { return ""; }
    }

    /**
     * The editor: fills the anchor the grid placed it in, which is already laid
     * over the slot and OUTSIDE the table — on the anchor's surface, in the body
     * ink and face at the label's size, the same as the text it replaces.
     */
    public record hrg_text_edit() implements CssClass<RelGridStockStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Body.class, Color.Ink.class), of(Body.class, Type.Face.class), of(Label.class, Type.Scale.class)); }
        @Override public String body() { return """
                box-sizing: border-box;
                width: 100%;
                height: 100%;
                border: 0;
                padding: 0 10px;
                background: transparent;
                user-select: text;
                -webkit-user-select: text;
                """;
        }
    }

    @Override public List<CssClass<RelGridStockStyles>> cssClasses() {
        return List.of(new hrg_text(), new hrg_text_ro(), new hrg_text_edit());
    }
}
