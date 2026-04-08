package com.tcli.output;

import com.tcli.parser.TikaParser.ParseResult;

public class TextFormatter {

    private final boolean includeMetadata;

    public TextFormatter(boolean includeMetadata) {
        this.includeMetadata = includeMetadata;
    }

    public String format(ParseResult result) {
        StringBuilder sb = new StringBuilder();
        formatResult(sb, result, 0);
        return sb.toString();
    }

    private void formatResult(StringBuilder sb, ParseResult result, int indentLevel) {
        if (includeMetadata) {
            appendIndent(sb, indentLevel);
            sb.append("--- ").append(result.getName());
            if (result.getMimeType() != null) {
                sb.append(" (").append(result.getMimeType()).append(")");
            }
            sb.append(" ---\n");

            if (!result.getMetadata().isEmpty()) {
                result.getMetadata().forEach((key, value) -> {
                    appendIndent(sb, indentLevel);
                    sb.append(key).append(": ").append(value).append("\n");
                });
                appendIndent(sb, indentLevel);
                sb.append("---\n");
            }
        }

        if (result.getText() != null && !result.getText().isEmpty()) {
            sb.append(result.getText());
            if (!result.getText().endsWith("\n")) {
                sb.append("\n");
            }
        }

        for (ParseResult child : result.getChildren()) {
            sb.append("\n");
            formatResult(sb, child, indentLevel + 1);
        }
    }

    private void appendIndent(StringBuilder sb, int level) {
        for (int i = 0; i < level; i++) {
            sb.append("  ");
        }
    }
}
