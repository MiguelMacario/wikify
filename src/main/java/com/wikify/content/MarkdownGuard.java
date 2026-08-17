package com.wikify.content;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class MarkdownGuard {

    private static final Pattern DANGER_HTML = Pattern.compile(
            "<\\s*script"
                    + "|<\\s*iframe"
                    + "|<\\s*object"
                    + "|<\\s*embed"
                    + "|javascript\\s*:"
                    + "|[\\s/]on\\w+\\s*=",
            Pattern.CASE_INSENSITIVE);

    /** Blocos de código saem da checagem, senão documentar HTML seria impossível. */
    private static final Pattern CODE_BLOCK = Pattern.compile(
            "```[\\s\\S]*?```" +
                    "|`[^`\\n]*`");

    private MarkdownGuard() {
    }

    public static void rejectDangerousHtml(String content) {
        if (content == null) return;

        String semCodigo = CODE_BLOCK.matcher(content).replaceAll("");
        Matcher matcher = DANGER_HTML.matcher(semCodigo);

        if (matcher.find()) {
            throw new IllegalArgumentException(
                    "O conteúdo contém HTML não permitido: \"" + matcher.group().trim() + "\". "
                            + "Markdown é aceito normalmente; script, iframe e atributos de evento não.");
        }
    }
}
