# tcli

A CLI tool for coding agents to convert any file format to text and extract metadata. Built with Apache Tika 3.3.0 and Tesseract OCR, compiled to a self-contained native binary via GraalVM.

## Features

- **Universal file parsing** — PDF, DOCX, XLSX, PPTX, HTML, RTF, XML, CSV, EPUB, and [hundreds more](https://tika.apache.org/3.3.0/formats.html) via Apache Tika
- **OCR support** — Extract text from images and scanned PDFs via Tesseract
- **PDF OCR without AWT** — Custom raw pixel extraction pipeline bypasses Java AWT limitations in GraalVM native-image
- **Hierarchical formats** — ZIP, TAR, GZIP, 7z with configurable depth
- **Self-contained binary** — Single native executable per platform (no JVM required)
- **Agent-friendly** — Plain text or JSON output, designed for LLM/coding agent consumption
- **Skill installation** — Register as a tool for coding agents via `tcli install --skill`

## Install

### Native binary (recommended)

Download the latest release for your platform from [Releases](../../releases), or build from source:

```bash
# Requires GraalVM JDK 21+
export JAVA_HOME=/path/to/graalvm
./gradlew nativeCompile
cp build/native/nativeCompile/tcli /usr/local/bin/
```

### Fat JAR (all platforms)

```bash
./gradlew fatJar
java -jar build/libs/tcli-0.1.0-all.jar parse <file>
```

## Usage

```
tcli parse <file> [options]
```

### Options

| Flag | Description | Default |
|---|---|---|
| `--format <txt\|json>` | Output format | `txt` |
| `--ocr` | Enable OCR for images and scanned PDFs | off |
| `--depth <n>` | Max recursion depth for archives (ZIP, TAR) | `1` |
| `--metadata` | Include file metadata in output | off |

### Examples

```bash
# Extract text from a PDF
tcli parse document.pdf

# OCR a scanned PDF
tcli parse scanned.pdf --ocr

# Parse with metadata as JSON
tcli parse report.docx --format json --metadata

# Extract text from a ZIP archive (2 levels deep)
tcli parse archive.zip --depth 2

# OCR an image
tcli parse screenshot.png --ocr

# Pipe from stdin
cat document.html | tcli parse
```

### JSON output

```json
{
  "file": "report.pdf",
  "mimeType": "application/pdf",
  "metadata": {
    "Content-Type": "application/pdf",
    "X-TIKA:Parsed-By": "org.apache.tika.parser.DefaultParser"
  },
  "text": "Extracted text content...",
  "children": []
}
```

## Skill Installation

Register tcli as a tool for coding agents:

```bash
tcli install --skill
```

This writes a skill manifest to `~/.claude/skills/tcli.json` describing the tool's capabilities, supported formats, and usage patterns.

## Building

### Prerequisites

- GraalVM JDK 25 (for native binary) or any JDK 21+ (for JAR)
- Gradle 9.x (wrapper included)
- Tesseract OCR (for `--ocr` support): `brew install tesseract`

### Build commands

```bash
# Run tests
./gradlew test

# Build JAR
./gradlew build

# Build fat JAR (all dependencies included)
./gradlew fatJar

# Build native binary
./gradlew nativeCompile
```

### Cross-platform builds

The GitHub Actions workflow builds native binaries for:
- Linux x86_64
- macOS x86_64 + ARM64
- Windows x86_64

Tag a release (`git tag v0.1.0 && git push --tags`) to trigger builds.

## Architecture

```
tcli/
├── src/main/java/com/tcli/
│   ├── App.java                          # Entry point, picocli CLI
│   ├── cli/
│   │   ├── ParseCommand.java             # parse subcommand
│   │   └── InstallCommand.java           # install --skill subcommand
│   ├── parser/
│   │   ├── TikaParser.java               # Core parsing (Tika + OCR routing)
│   │   ├── PdfImageOcrExtractor.java      # AWT-free PDF image OCR
│   │   ├── OcrParser.java                # Tesseract OCR config
│   │   └── HierarchicalParser.java       # Archive utilities
│   ├── output/
│   │   ├── TextFormatter.java            # Plain text output
│   │   └── JsonFormatter.java            # JSON output
│   ├── metadata/
│   │   └── MetadataExtractor.java        # Metadata utilities
│   └── skill/
│       └── SkillManifest.java            # Skill manifest generation
└── src/main/resources/META-INF/native-image/
    ├── reflect-config.json               # GraalVM reflection config
    ├── resource-config.json              # Embedded resources config
    ├── jni-config.json                   # JNI config for AWT
    └── native-image.properties           # Build-time init config
```

## How PDF OCR works without AWT

GraalVM native-image on macOS cannot load AWT native libraries (`libawt.dylib`). tcli works around this:

1. **Text extraction** — AWT classes are initialized at GraalVM build time (when the JDK's AWT IS available), baking the static state into the binary
2. **PDF OCR** — Instead of rendering pages through `BufferedImage` (which needs AWT), tcli extracts raw pixel data from PDF image streams via `PDImageXObject.createInputStream()`, writes them as PGM/PPM (Netpbm format), and calls `tesseract` CLI directly

## Dependencies

| Library | Version | Purpose |
|---|---|---|
| Apache Tika | 3.3.0 | File format detection and parsing |
| Apache PDFBox | 3.x | PDF text extraction |
| Tess4J | 5.13.0 | Tesseract OCR integration |
| picocli | 4.7.6 | CLI framework (GraalVM-friendly) |
| Jackson | 2.17.2 | JSON output |
| GraalVM native-image | 25.x | Native binary compilation |

## License

MIT
