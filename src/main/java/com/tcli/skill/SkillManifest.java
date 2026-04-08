package com.tcli.skill;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class SkillManifest {

    private static final String SKILL_NAME = "tcli";
    private static final String VERSION = "0.1.0";

    public void install() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        ObjectNode manifest = mapper.createObjectNode();
        manifest.put("name", SKILL_NAME);
        manifest.put("version", VERSION);
        manifest.put("description", "Convert any file format to text and extract metadata using Apache Tika and Tesseract OCR.");

        // Commands
        ArrayNode commands = mapper.createArrayNode();

        ObjectNode parseCmd = mapper.createObjectNode();
        parseCmd.put("name", "parse");
        parseCmd.put("description", "Parse a file and extract text content");
        parseCmd.put("usage", "tcli parse <file> [--format txt|json] [--ocr] [--depth <n>] [--metadata]");

        ArrayNode parseOptions = mapper.createArrayNode();
        parseOptions.add(option(mapper, "--format", "Output format: txt or json", "txt"));
        parseOptions.add(option(mapper, "--ocr", "Enable OCR for images and scanned PDFs", "false"));
        parseOptions.add(option(mapper, "--depth", "Max depth for hierarchical formats", "1"));
        parseOptions.add(option(mapper, "--metadata", "Include file metadata in output", "false"));
        parseCmd.set("options", parseOptions);
        commands.add(parseCmd);

        manifest.set("commands", commands);

        // Supported formats
        ArrayNode formats = mapper.createArrayNode();
        for (String fmt : new String[]{"pdf", "docx", "xlsx", "pptx", "html", "rtf", "txt",
                "csv", "xml", "json", "epub", "odt", "ods", "odp", "zip", "tar", "gz",
                "7z", "png", "jpg", "tiff", "bmp", "gif"}) {
            formats.add(fmt);
        }
        manifest.set("supportedFormats", formats);

        Path skillDir = getSkillDirectory();
        Files.createDirectories(skillDir);
        Path manifestPath = skillDir.resolve("tcli.json");
        mapper.writeValue(manifestPath.toFile(), manifest);

        System.out.println("Skill manifest written to: " + manifestPath);
    }

    private ObjectNode option(ObjectMapper mapper, String name, String description, String defaultValue) {
        ObjectNode opt = mapper.createObjectNode();
        opt.put("name", name);
        opt.put("description", description);
        opt.put("default", defaultValue);
        return opt;
    }

    private Path getSkillDirectory() {
        String home = System.getProperty("user.home");
        return Path.of(home, ".claude", "skills");
    }
}
