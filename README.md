# tcli

A CLI for coding agents to convert any file format to text and extract metadata. Built on [Apache Tika](https://tika.apache.org/) and [Tesseract OCR](https://github.com/tesseract-ocr/tesseract). Self-contained native binary — no JVM required.

## Features

- **Universal file parsing** — PDF, DOCX, XLSX, PPTX, HTML, RTF, XML, CSV, EPUB, and [hundreds more](https://tika.apache.org/3.3.0/formats.html)
- **OCR** — Extract text from images and scanned PDFs via Tesseract
- **Archives** — ZIP, TAR, GZIP, 7z with configurable depth
- **Output** — Plain text or structured JSON with optional metadata
- **Skill installation** — `tcli install --skill` registers with coding agents

## Platforms

| Platform | Binary |
|---|---|
| macOS ARM64 | `tcli-darwin-arm64` |
| Linux x86_64 | `tcli-linux-x86_64` |
| Windows x86_64 | `tcli-windows-x86_64.exe` |

Download from [Releases](../../releases).

## Usage

```bash
tcli parse document.pdf                          # extract text
tcli parse scanned.pdf --ocr                     # OCR a scanned PDF
tcli parse report.docx --format json --metadata  # JSON with metadata
tcli parse archive.zip --depth 2                 # extract from archive
tcli parse screenshot.png --ocr                  # OCR an image
```

### Options

| Flag | Description | Default |
|---|---|---|
| `--format <txt\|json>` | Output format | `txt` |
| `--ocr` | Enable OCR (requires [Tesseract](https://github.com/tesseract-ocr/tesseract)) | off |
| `--depth <n>` | Max recursion depth for archives | `1` |
| `--metadata` | Include file metadata | off |

## Building from source

Requires GraalVM JDK 25 and Gradle (wrapper included).

```bash
./gradlew nativeCompile   # native binary → build/native/nativeCompile/tcli
./gradlew fatJar          # fat JAR → build/libs/tcli-0.1.0-all.jar
```

## License

MIT — see [LICENSE](LICENSE) and [NOTICE](NOTICE).
