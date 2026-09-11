package hue.captains.singapura.js.homing.relgrid.protocol;

/**
 * The domain's answer to {@link RelGridCopyRequested}: what to write — RFC
 * 0050 · Episode 2, map 6 laws 48 and 50.
 *
 * <p>Finished content, and nothing else. {@code text} is what every clipboard
 * takes; {@code html} is the richer form for the targets that take one — a
 * spreadsheet, a mail — and is absent when the domain has none to offer. The
 * grid writes both when it has both and the plain one otherwise, and reads
 * neither: it is the party inside the user's gesture, so it is the party that
 * writes, and that is the whole of its involvement.</p>
 *
 * <p>The alternative answer is <b>absence</b>: the promise resolves with
 * nothing, nothing is written, and the clipboard keeps what it had.</p>
 */
public record RelGridClipboardContent(String text, String html) {

    public RelGridClipboardContent {
        if (text == null) throw new IllegalArgumentException("RelGridClipboardContent needs text; html is optional");
    }

    public boolean hasHtml() { return html != null; }
}
