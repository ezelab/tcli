package com.tcli.parser;

/**
 * Hierarchical parsing is handled by TikaParser via RecursiveParserWrapper.
 * This class provides utilities for archive-specific processing.
 */
public class HierarchicalParser {

    public static final int DEFAULT_MAX_DEPTH = 1;
    public static final int MAX_ENTRIES = 10_000;
    public static final long MAX_EXTRACTED_SIZE = 500 * 1024 * 1024; // 500 MB

    private HierarchicalParser() {}

    public static boolean isArchiveMimeType(String mimeType) {
        if (mimeType == null) return false;
        return mimeType.contains("zip") ||
               mimeType.contains("tar") ||
               mimeType.contains("gzip") ||
               mimeType.contains("x-7z") ||
               mimeType.contains("x-rar") ||
               mimeType.contains("x-bzip");
    }
}
