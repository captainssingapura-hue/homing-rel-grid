package hue.captains.singapura.js.homing.relgrid.selection;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Map 5 law 215, made mechanical: the selection is a pure algebra of positions
 * in a module that imports nothing.
 *
 * <p>Two scans, comments stripped. The first is the grid's own value-free law —
 * this module is grid-side, so it inherits it. The second is this module's
 * character: no DOM, and no reference to any of the objects the selection is
 * defined to be free of. A selection that learned about a view map could clamp;
 * one that learned about a layout could paint; one that held a cursor would
 * make law 39's independence a matter of care rather than of structure. The
 * cursor's POSITION arrives as an argument, which is why {@code cursorAt}
 * survives a scan that rejects {@code cursor}.</p>
 */
class RelGridSelectionIsolationTest {

    private static final String MODULE =
            "/homing/js/hue/captains/singapura/js/homing/relgrid/selection/RelGridSelectionModule.js";

    /** Anything that would let a value in, out, or through — the grid's root principle. */
    private static final Pattern FORBIDDEN_VALUE = Pattern.compile(
            "relation\\.get\\b|adapter\\b|\\.update\\(|subscribe|updateCell|flushNow"
          + "|\\bvalue\\b|\\bvalues\\b|getValue|columnMeta|compare\\(|\\.sort\\(|\\.filter\\(|commit|preview|effectiveType");

    /** Anything that would make this module something other than an algebra of positions. */
    private static final Pattern FORBIDDEN_REACH = Pattern.compile(
            "\\bdocument\\b|\\bwindow\\b|createElement|addEventListener|classList|\\bstyle\\b"
          + "|\\bimport\\b|\\brequire\\(|RelGridViewMaps|RelGridLayout|RelGridCells|\\brelation\\b"
          + "|\\bcellFor\\b|\\bpk\\b|\\bcolumn\\b|\\bcursor\\b|\\bpaint|\\bclamp");

    @Test
    void theSelectionTouchesNoValue() {
        assertEquals(List.of(), offences(FORBIDDEN_VALUE),
                "the selection reached for a value — the root principle forbids it");
    }

    @Test
    void theSelectionReachesForNothingAtAll() {
        assertEquals(List.of(), offences(FORBIDDEN_REACH),
                "the selection is an algebra of positions: no DOM, no maps, no layout, no cells, "
              + "no relation, and no cursor of its own");
    }

    @Test
    void theModuleDeclaresNoImports() {
        assertEquals(Map.of(), RelGridSelectionModule.INSTANCE.imports().getAllImports(),
                "law 215 is structural: the selection module imports nothing");
    }

    private static List<String> offences(Pattern forbidden) {
        var found = new ArrayList<String>();
        var m = forbidden.matcher(stripComments(read(MODULE)));
        while (m.find()) found.add("'" + m.group() + "'");
        return found;
    }

    private static String stripComments(String s) {
        return s.replaceAll("(?s)/\\*.*?\\*/", "").replaceAll("(?m)//.*$", "");
    }

    private static String read(String path) {
        try (InputStream in = RelGridSelectionIsolationTest.class.getResourceAsStream(path)) {
            if (in == null) throw new IllegalArgumentException("resource not found: " + path);
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
