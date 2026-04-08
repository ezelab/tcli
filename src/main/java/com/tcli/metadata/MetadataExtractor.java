package com.tcli.metadata;

import com.tcli.parser.TikaParser.ParseResult;

import java.util.Map;

public class MetadataExtractor {

    public static String summarize(ParseResult result) {
        Map<String, String> meta = result.getMetadata();
        StringBuilder sb = new StringBuilder();
        sb.append("File: ").append(result.getName()).append("\n");
        if (result.getMimeType() != null) {
            sb.append("MIME Type: ").append(result.getMimeType()).append("\n");
        }
        for (Map.Entry<String, String> entry : meta.entrySet()) {
            sb.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }
        return sb.toString();
    }
}
