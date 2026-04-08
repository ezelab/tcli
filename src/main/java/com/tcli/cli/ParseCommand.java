package com.tcli.cli;

import com.tcli.metadata.MetadataExtractor;
import com.tcli.output.JsonFormatter;
import com.tcli.output.TextFormatter;
import com.tcli.parser.TikaParser;
import com.tcli.parser.TikaParser.ParseResult;
import picocli.CommandLine;

import java.io.File;
import java.io.InputStream;
import java.util.concurrent.Callable;

@CommandLine.Command(
    name = "parse",
    mixinStandardHelpOptions = true,
    description = "Parse a file and extract its text content."
)
public class ParseCommand implements Callable<Integer> {

    @CommandLine.Parameters(index = "0", arity = "0..1", description = "File to parse (reads stdin if omitted)")
    private File file;

    @CommandLine.Option(names = "--format", defaultValue = "txt", description = "Output format: txt or json (default: txt)")
    private String format;

    @CommandLine.Option(names = "--ocr", defaultValue = "false", description = "Enable OCR for images and scanned PDFs")
    private boolean ocr;

    @CommandLine.Option(names = "--depth", defaultValue = "1", description = "Max depth for hierarchical formats (default: 1, 0 = no recursion)")
    private int depth;

    @CommandLine.Option(names = "--metadata", defaultValue = "false", description = "Include file metadata in output")
    private boolean metadata;

    @Override
    public Integer call() {
        try {
            TikaParser parser = new TikaParser(ocr, depth);
            ParseResult result;

            if (file != null) {
                if (!file.exists()) {
                    System.err.println("tcli: file not found: " + file.getPath());
                    return 1;
                }
                result = parser.parse(file);
            } else {
                InputStream stdin = System.in;
                if (System.in.available() == 0 && System.console() != null) {
                    System.err.println("tcli: no file specified and no stdin input");
                    return 1;
                }
                result = parser.parse(stdin, "stdin");
            }

            if ("json".equalsIgnoreCase(format)) {
                JsonFormatter formatter = new JsonFormatter(metadata);
                System.out.println(formatter.format(result));
            } else {
                TextFormatter formatter = new TextFormatter(metadata);
                System.out.print(formatter.format(result));
            }

            return 0;
        } catch (Exception | Error e) {
            Throwable cause = e;
            while (cause.getCause() != null) cause = cause.getCause();
            System.err.println("tcli: error parsing file: " + cause.getMessage());
            return 1;
        }
    }
}
