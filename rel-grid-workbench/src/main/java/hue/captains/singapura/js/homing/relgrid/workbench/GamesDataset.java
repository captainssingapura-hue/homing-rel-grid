package hue.captains.singapura.js.homing.relgrid.workbench;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * The Games Catalogue's dataset: seven hundred-odd releases, 1990 to 2020, one
 * row per release, pipe-separated, a resource beside this class. Annual series
 * — FIFA, Madden, NBA 2K, PES, Need for Speed, Call of Duty, Assassin's Creed,
 * Just Dance, F1 — have a row each year, as a real catalogue has them. Sales
 * are approximate lifetime units in millions as commonly reported, and blank
 * where free-to-play or not reported; scores are Metacritic where one exists.
 * The blanks are deliberate: a sort has to say what it does with absence.
 */
public final class GamesDataset {

    private GamesDataset() {}

    private static final String RESOURCE = "/hue/captains/singapura/js/homing/relgrid/workbench/games-1990-2020.psv";

    /** The catalogue's text, as the store reads it. */
    public static String psv() {
        try (InputStream in = GamesDataset.class.getResourceAsStream(RESOURCE)) {
            if (in == null) throw new IllegalStateException("the games catalogue is missing: " + RESOURCE);
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("the games catalogue could not be read", e);
        }
    }

    /** How many rows the catalogue has — the header and remarks aside. */
    public static int rows() {
        int n = 0;
        for (String line : psv().split("\r?\n")) if (!line.isBlank() && !line.startsWith("#") && !line.startsWith("id|")) n++;
        return n;
    }
}
