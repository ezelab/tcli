package com.tcli.parser;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Extracts raw image streams from PDF using PDFBox without AWT,
 * then runs each through Tika's OCR parser (which calls tesseract CLI).
 * This bypasses the BufferedImage/AWT path entirely.
 */
public class PdfImageOcrExtractor {

    /**
     * Extract images from a PDF and OCR them, returning the combined text.
     */
    public String extractOcrText(File pdfFile) throws IOException {
        StringBuilder sb = new StringBuilder();
        List<Path> tempFiles = new ArrayList<>();

        try (PDDocument doc = Loader.loadPDF(pdfFile)) {
            int pageNum = 0;
            for (PDPage page : doc.getPages()) {
                pageNum++;
                PDResources resources = page.getResources();
                if (resources == null) continue;

                for (COSName name : resources.getXObjectNames()) {
                    PDXObject xobject;
                    try {
                        xobject = resources.getXObject(name);
                    } catch (Exception | Error e) {
                        continue;
                    }

                    if (xobject instanceof PDImageXObject image) {
                        String ocrText = ocrImage(image, tempFiles);
                        if (ocrText != null && !ocrText.isBlank()) {
                            sb.append(ocrText);
                            if (!ocrText.endsWith("\n")) sb.append("\n");
                        }
                    }
                }
            }
        } finally {
            for (Path tmp : tempFiles) {
                try { Files.deleteIfExists(tmp); } catch (IOException ignored) {}
            }
        }

        return sb.toString();
    }

    private String ocrImage(PDImageXObject image, List<Path> tempFiles) {
        try {
            int w = image.getWidth();
            int h = image.getHeight();
            int bpc = image.getBitsPerComponent();

            // Get decoded pixel data from PDFBox (no AWT needed)
            byte[] pixels;
            try (InputStream is = image.createInputStream()) {
                pixels = is.readAllBytes();
            }

            // Write as PGM/PPM (Netpbm format) which tesseract reads natively.
            int expectedGray = w * h * (bpc / 8);
            int channels = (pixels.length >= expectedGray * 3) ? 3 : 1;
            int maxVal = (1 << bpc) - 1;

            Path tmpImage;
            if (channels == 1) {
                tmpImage = Files.createTempFile("tcli-ocr-", ".pgm");
                String header = "P5\n" + w + " " + h + "\n" + maxVal + "\n";
                try (OutputStream os = Files.newOutputStream(tmpImage)) {
                    os.write(header.getBytes());
                    os.write(pixels, 0, Math.min(pixels.length, w * h));
                }
            } else {
                tmpImage = Files.createTempFile("tcli-ocr-", ".ppm");
                String header = "P6\n" + w + " " + h + "\n" + maxVal + "\n";
                try (OutputStream os = Files.newOutputStream(tmpImage)) {
                    os.write(header.getBytes());
                    os.write(pixels, 0, Math.min(pixels.length, w * h * 3));
                }
            }
            tempFiles.add(tmpImage);

            // Call tesseract CLI directly
            ProcessBuilder pb = new ProcessBuilder("tesseract",
                tmpImage.toAbsolutePath().toString(), "stdout", "-l", "eng");
            pb.redirectErrorStream(false);
            Process proc = pb.start();

            String text;
            try (InputStream is = proc.getInputStream()) {
                text = new String(is.readAllBytes());
            }
            proc.waitFor();

            return text;
        } catch (Exception | Error e) {
            return null;
        }
    }
}
