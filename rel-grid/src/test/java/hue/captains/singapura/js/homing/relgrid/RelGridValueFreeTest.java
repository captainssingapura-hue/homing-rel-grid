package hue.captains.singapura.js.homing.relgrid;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * RFC 0050 · Episode 2's root principle, made mechanical: <b>no grid module
 * touches a value</b>. Every JS module of the grid proper is scanned, comments
 * stripped, for the verbs and words by which a value could cross the seam.
 * The first one fails the build.
 *
 * <p>The stock cells are excluded on purpose — they are domain-side code that
 * ships with the grid, hold what they show, and are exactly where a value
 * belongs.</p>
 */
class RelGridValueFreeTest {

    private static final String DIR = "/homing/js/hue/captains/singapura/js/homing/relgrid/";

    /** The grid proper. Not RelGridStockCellsModule.js — cells are the domain's. */
    private static final List<String> GRID_MODULES = List.of(
            "RelGridViewMapsModule.js", "RelGridHeaderDragModule.js", "RelGridLayoutModule.js", "RelGridCellsModule.js",
            "RelGridWindowModule.js", "RelGridHeadersModule.js", "RelGridSpansModule.js", "RelGridModule.js");

    /** Anything that would let a value in, out, or through. */
    private static final Pattern FORBIDDEN = Pattern.compile(
            "relation\\.get\\b|adapter\\b|\\.update\\(|subscribe|updateCell|flushNow"
          + "|\\bvalue\\b|\\bvalues\\b|getValue|columnMeta|compare\\(|\\.sort\\(|\\.filter\\(|commit|preview|effectiveType");

    @Test
    void noGridModuleTouchesAValue() {
        var offences = new ArrayList<String>();
        for (String module : GRID_MODULES) {
            String code = stripComments(read(DIR + module));
            var m = FORBIDDEN.matcher(code);
            while (m.find()) offences.add(module + ": '" + m.group() + "'");
        }
        assertEquals(List.of(), offences,
                "a grid module reached for a value — the root principle forbids it");
    }

    private static String stripComments(String s) {
        return s.replaceAll("(?s)/\\*.*?\\*/", "").replaceAll("(?m)//.*$", "");
    }

    private static String read(String path) {
        try (InputStream in = RelGridValueFreeTest.class.getResourceAsStream(path)) {
            if (in == null) throw new IllegalArgumentException("resource not found: " + path);
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
