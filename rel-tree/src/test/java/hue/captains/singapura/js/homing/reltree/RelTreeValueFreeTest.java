package hue.captains.singapura.js.homing.reltree;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Episode 2's root principle, held to by the tree as by the grid: <b>no tree
 * module touches a value</b>. Every JS module of the tree proper is scanned,
 * comments stripped, for the verbs and words by which a value could cross
 * the seam — and for the words a tree would reach for first: a node's text,
 * a label, a search. The first one fails the build.
 *
 * <p>The stock cell is excluded on purpose: it is domain-side code that ships
 * with the tree, holds what it shows, and is exactly where a value belongs.</p>
 */
class RelTreeValueFreeTest {

    private static final String DIR = "/homing/js/hue/captains/singapura/js/homing/reltree/";

    private static final List<String> TREE_MODULES = List.of(
            "RelTreePlacesModule.js", "RelTreeRowsModule.js", "RelTreeCellsModule.js", "RelTreeLayoutModule.js",
            "RelTreeCursorModule.js", "RelTreeChannelModule.js", "RelTreeGesturesModule.js", "RelTreeModule.js");

    /** Anything that would let a value in, out, or through — and the tree's own temptations. */
    private static final Pattern FORBIDDEN = Pattern.compile(
            "relation\\.get\\b|adapter\\b|\\.update\\(|subscribe|updateCell|flushNow"
          + "|\\bvalue\\b|\\bvalues\\b|getValue|compare\\(|\\.sort\\(|\\.filter\\(|commit|preview"
          + "|\\btext\\(|\\blabel\\(|\\bicon\\(|typeAhead|\\bfind\\(|\\bsearch\\b");

    @Test
    void noTreeModuleTouchesAValue() {
        var offences = new ArrayList<String>();
        for (String module : TREE_MODULES) {
            String code = stripComments(read(DIR + module));
            var m = FORBIDDEN.matcher(code);
            while (m.find()) offences.add(module + ": '" + m.group() + "'");
        }
        assertEquals(List.of(), offences, "a tree module reached for a value — the root principle forbids it");
    }

    private static String stripComments(String s) {
        return s.replaceAll("(?s)/\\*.*?\\*/", "").replaceAll("(?m)//.*$", "");
    }

    private static String read(String path) {
        try (InputStream in = RelTreeValueFreeTest.class.getResourceAsStream(path)) {
            if (in == null) throw new IllegalArgumentException("resource not found: " + path);
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
