package hue.captains.singapura.js.homing.relgrid.workbench;

import hue.captains.singapura.js.homing.ui.styles.HouseStyles;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The golden sheets (RFC 0067, the plan Palettes as Functions, phase 1): every sheet this module renders per theme, as
 * it was before palettes became functions, compared on every build until the refactor is done - the same text in all
 * 56 house themes proves nothing looks different. A sheet that differs is written out, as captured and as rendered, to
 * {@code target/golden-diff}. Captured again only with {@code -Dgolden.capture=true}, and only on code known to draw
 * right; deleted with the refactor's end.
 */
class GoldenSheetsTest {

    private static final Path GOLDEN = Path.of("src", "test", "resources", "golden");
    private static final Path DIFF = Path.of("target", "golden-diff");
    private static final boolean CAPTURE = Boolean.getBoolean("golden.capture");

    @Test void theVistaSheet_isAsCaptured_inEveryHouseTheme() { check("minesweeper-vista", MinesweeperVistaSheet.INSTANCE::css); }

    private static void check(String sheet, Function<String, String> css) {
        Path dir = GOLDEN.resolve(sheet);
        Set<String> slugs = new TreeSet<>(HouseStyles.THEMES.keySet());
        if (CAPTURE) {
            for (String slug : slugs) write(dir.resolve(slug + ".css.gz"), css.apply(slug));
            return;
        }
        assertEquals(slugs, captured(dir), sheet + ": the themes captured are the house's themes");
        var differ = new ArrayList<String>();
        for (String slug : slugs) {
            String was = read(dir.resolve(slug + ".css.gz")), now = css.apply(slug);
            if (was.equals(now)) continue;
            differ.add(slug + ": " + firstDifference(was, now));
            write(DIFF.resolve(sheet).resolve(slug + ".captured.css"), was, false);
            write(DIFF.resolve(sheet).resolve(slug + ".rendered.css"), now, false);
        }
        assertEquals(List.of(), differ, sheet + " differs from what was captured; both are in " + DIFF.resolve(sheet));
    }

    /** The slugs captured, from the files there. */
    private static Set<String> captured(Path dir) {
        try (Stream<Path> files = Files.list(dir)) {
            return files.map(p -> p.getFileName().toString()).filter(n -> n.endsWith(".css.gz"))
                        .map(n -> n.substring(0, n.length() - ".css.gz".length())).collect(Collectors.toCollection(TreeSet::new));
        } catch (IOException e) { throw new UncheckedIOException("nothing captured in " + dir, e); }
    }

    /** Where two sheets part, by line: its number and both lines. */
    private static String firstDifference(String was, String now) {
        String[] a = was.split("\n", -1), b = now.split("\n", -1);
        for (int i = 0; i < Math.max(a.length, b.length); i++) {
            String x = i < a.length ? a[i] : "<end>", y = i < b.length ? b[i] : "<end>";
            if (!x.equals(y)) return "line " + (i + 1) + ": captured [" + clip(x) + "], rendered [" + clip(y) + "]";
        }
        return "same lines, different text";
    }

    private static String clip(String line) { return line.length() > 120 ? line.substring(0, 120) + "…" : line; }

    private static String read(Path file) {
        try (var in = new GZIPInputStream(Files.newInputStream(file))) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) { throw new UncheckedIOException("cannot read " + file, e); }
    }

    private static void write(Path file, String text) { write(file, text, true); }

    private static void write(Path file, String text, boolean gzip) {
        try {
            Files.createDirectories(file.getParent());
            byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
            if (!gzip) { Files.write(file, bytes); return; }
            try (var out = new GZIPOutputStream(Files.newOutputStream(file))) { out.write(bytes); }
        } catch (IOException e) { throw new UncheckedIOException("cannot write " + file, e); }
    }
}
