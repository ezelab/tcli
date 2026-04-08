package com.tcli.parser;

import org.apache.tika.Tika;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaCoreProperties;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.RecursiveParserWrapper;
import org.apache.tika.sax.BasicContentHandlerFactory;
import org.apache.tika.sax.BodyContentHandler;
import org.apache.tika.sax.RecursiveParserWrapperHandler;
import org.xml.sax.SAXException;

import java.io.*;
import java.util.*;

public class TikaParser {

    private final boolean ocrEnabled;
    private final int maxDepth;
    private final AutoDetectParser autoParser;

    public TikaParser(boolean ocrEnabled, int maxDepth) {
        this.ocrEnabled = ocrEnabled;
        this.maxDepth = maxDepth;
        this.autoParser = new AutoDetectParser();
    }

    public ParseResult parse(File file) throws IOException, TikaException, SAXException {
        try (InputStream is = new BufferedInputStream(new FileInputStream(file))) {
            ParseResult result = doParse(is, file.getName());

            if (!ocrEnabled) return result;

            String mime = result.getMimeType();
            if (mime == null) return result;

            if (mime.contains("pdf")) {
                // PDF OCR: extract raw image streams without AWT, then tesseract each
                try {
                    PdfImageOcrExtractor extractor = new PdfImageOcrExtractor();
                    String ocrText = extractor.extractOcrText(file);
                    if (ocrText != null && !ocrText.isBlank()) {
                        String combined = result.getText() + "\n" + ocrText;
                        return new ParseResult(result.getName(), mime,
                            combined, result.getMetadata(), result.getChildren());
                    }
                } catch (Exception | Error e) {
                    // Fall through with text-only result
                }
            } else if (mime.startsWith("image/")) {
                // Image OCR: re-parse with Tika OCR enabled (works without AWT)
                try (InputStream is2 = new BufferedInputStream(new FileInputStream(file))) {
                    Metadata metadata = new Metadata();
                    metadata.set(TikaCoreProperties.RESOURCE_NAME_KEY, file.getName());
                    BodyContentHandler handler = new BodyContentHandler(-1);
                    autoParser.parse(is2, handler, metadata, createOcrParseContext());
                    String ocrText = handler.toString();
                    if (ocrText != null && !ocrText.isBlank()) {
                        return new ParseResult(result.getName(), mime,
                            ocrText, result.getMetadata(), result.getChildren());
                    }
                } catch (Exception | Error e) {
                    // Fall through with non-OCR result
                }
            }

            return result;
        }
    }

    public ParseResult parse(InputStream inputStream, String name) throws IOException, TikaException, SAXException {
        return doParse(inputStream, name);
    }

    private ParseResult doParse(InputStream is, String name) throws IOException, TikaException, SAXException {
        if (maxDepth > 0) {
            return parseRecursive(is, name);
        } else {
            return parseFlat(is, name);
        }
    }

    private ParseResult parseFlat(InputStream is, String name) throws IOException, TikaException, SAXException {
        Metadata metadata = new Metadata();
        metadata.set(TikaCoreProperties.RESOURCE_NAME_KEY, name);
        BodyContentHandler handler = new BodyContentHandler(-1);
        ParseContext context = createParseContext();
        autoParser.parse(is, handler, metadata, context);

        String mimeType = metadata.get(Metadata.CONTENT_TYPE);
        return new ParseResult(name, mimeType, handler.toString(), toMap(metadata), Collections.emptyList());
    }

    private ParseResult parseRecursive(InputStream is, String name) throws IOException, TikaException, SAXException {
        Metadata metadata = new Metadata();
        metadata.set(TikaCoreProperties.RESOURCE_NAME_KEY, name);

        RecursiveParserWrapper wrapper = new RecursiveParserWrapper(autoParser);
        BasicContentHandlerFactory factory = new BasicContentHandlerFactory(
            BasicContentHandlerFactory.HANDLER_TYPE.TEXT, -1
        );
        RecursiveParserWrapperHandler handler = new RecursiveParserWrapperHandler(factory, maxDepth);
        ParseContext context = createParseContext();
        wrapper.parse(is, handler, metadata, context);

        List<org.apache.tika.metadata.Metadata> metadataList = handler.getMetadataList();

        if (metadataList.isEmpty()) {
            return new ParseResult(name, null, "", Collections.emptyMap(), Collections.emptyList());
        }

        // First entry is the container
        org.apache.tika.metadata.Metadata rootMeta = metadataList.get(0);
        String rootText = rootMeta.get(TikaCoreProperties.TIKA_CONTENT);
        String rootMime = rootMeta.get(Metadata.CONTENT_TYPE);

        List<ParseResult> children = new ArrayList<>();
        for (int i = 1; i < metadataList.size(); i++) {
            org.apache.tika.metadata.Metadata childMeta = metadataList.get(i);
            String childText = childMeta.get(TikaCoreProperties.TIKA_CONTENT);
            String childName = childMeta.get(TikaCoreProperties.RESOURCE_NAME_KEY);
            String childMime = childMeta.get(Metadata.CONTENT_TYPE);
            if (childName == null) {
                childName = "entry-" + i;
            }
            children.add(new ParseResult(
                childName, childMime,
                childText != null ? childText : "",
                toMap(childMeta),
                Collections.emptyList()
            ));
        }

        return new ParseResult(name, rootMime, rootText != null ? rootText : "", toMap(rootMeta), children);
    }

    private ParseContext createParseContext() {
        ParseContext context = new ParseContext();

        // Always disable OCR during the initial Tika parse to avoid AWT.
        // OCR is handled separately: PdfImageOcrExtractor for PDFs (raw streams),
        // and a second Tika pass for images.
        try {
            org.apache.tika.parser.ocr.TesseractOCRConfig ocrConfig =
                new org.apache.tika.parser.ocr.TesseractOCRConfig();
            ocrConfig.setSkipOcr(true);
            context.set(org.apache.tika.parser.ocr.TesseractOCRConfig.class, ocrConfig);
        } catch (NoClassDefFoundError e) {
            // TesseractOCRConfig not on classpath
        }

        try {
            org.apache.tika.parser.pdf.PDFParserConfig pdfConfig =
                new org.apache.tika.parser.pdf.PDFParserConfig();
            pdfConfig.setExtractInlineImages(false);
            pdfConfig.setOcrStrategy(
                org.apache.tika.parser.pdf.PDFParserConfig.OCR_STRATEGY.NO_OCR);
            context.set(org.apache.tika.parser.pdf.PDFParserConfig.class, pdfConfig);
        } catch (NoClassDefFoundError e) {
            // PDFParserConfig not on classpath
        }

        return context;
    }

    /**
     * Creates a parse context with OCR enabled, for use on non-PDF files
     * (images) where Tika's OCR pipeline works without AWT.
     */
    private ParseContext createOcrParseContext() {
        ParseContext context = new ParseContext();
        try {
            org.apache.tika.parser.ocr.TesseractOCRConfig ocrConfig =
                new org.apache.tika.parser.ocr.TesseractOCRConfig();
            ocrConfig.setSkipOcr(false);
            context.set(org.apache.tika.parser.ocr.TesseractOCRConfig.class, ocrConfig);
        } catch (NoClassDefFoundError e) {}
        return context;
    }

    private Map<String, String> toMap(Metadata metadata) {
        Map<String, String> map = new LinkedHashMap<>();
        for (String name : metadata.names()) {
            String value = metadata.get(name);
            // Skip the embedded content key from recursive parsing
            if (!name.equals(TikaCoreProperties.TIKA_CONTENT.getName())) {
                map.put(name, value);
            }
        }
        return map;
    }

    public static class ParseResult {
        private final String name;
        private final String mimeType;
        private final String text;
        private final Map<String, String> metadata;
        private final List<ParseResult> children;

        public ParseResult(String name, String mimeType, String text,
                           Map<String, String> metadata, List<ParseResult> children) {
            this.name = name;
            this.mimeType = mimeType;
            this.text = text;
            this.metadata = metadata;
            this.children = children;
        }

        public String getName() { return name; }
        public String getMimeType() { return mimeType; }
        public String getText() { return text; }
        public Map<String, String> getMetadata() { return metadata; }
        public List<ParseResult> getChildren() { return children; }
    }
}
