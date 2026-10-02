package hue.captains.singapura.js.homing.relgrid.workbench;

/** Text as a JS string literal, for a module that carries data generated from Java: ASCII, quoted, escaped. */
public final class JsText {

    private JsText() {}

    /** {@code s} as a single-quoted JS string literal: quotes and backslashes escaped, everything outside printable ASCII as a unicode escape. */
    public static String literal(String s) {
        var sb = new StringBuilder("'");
        for (char c : s.toCharArray()) {
            if (c == '\\' || c == '\'') sb.append('\\').append(c);
            else if (c == '\n') sb.append("\\n");
            else if (c < 0x20 || c > 0x7e) sb.append(String.format("\\u%04x", (int) c));
            else sb.append(c);
        }
        return sb.append('\'').toString();
    }
}
