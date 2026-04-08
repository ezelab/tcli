package com.tcli.parser;

import org.apache.tika.parser.ocr.TesseractOCRConfig;
import org.apache.tika.parser.ParseContext;

/**
 * Configures Tesseract OCR integration for Tika parsing.
 */
public class OcrParser {

    public static void configureOcr(ParseContext context) {
        TesseractOCRConfig ocrConfig = new TesseractOCRConfig();
        ocrConfig.setSkipOcr(false);
        context.set(TesseractOCRConfig.class, ocrConfig);
    }

    public static void disableOcr(ParseContext context) {
        TesseractOCRConfig ocrConfig = new TesseractOCRConfig();
        ocrConfig.setSkipOcr(true);
        context.set(TesseractOCRConfig.class, ocrConfig);
    }
}
