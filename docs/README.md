Image Converter Plugin
-----------------------

[![Build Status](https://img.shields.io/badge/build-passing-brightgreen)]()  
[![License: LGPL v3](https://img.shields.io/badge/License-LGPL%20v3-blue.svg)](LICENSE.md)

A plugin for [ETERNA](https://github.com/ETERNA-earkiv/ETERNA) providing robust image format conversion for digital preservation workflows.

## Table of Contents

- [Supported Formats](#supported-formats)
- [Features](#features-v100)
- [What Gets Tested](#what-gets-tested)
- [Known Limitations](#known-limitations)
- [Prerequisites](#prerequisites)
- [How to Build and Run](#how-to-build-and-run)
- [Usage Example](#usage-example)
- [Configuration](#configuration)
- [Contributing](#contributing)
- [License](#license)
- [Future Improvements](#future-improvements)

## Supported Formats

- **Input:**
  - Most common raster image formats supported by Java ImageIO and TwelveMonkeys (e.g., BMP, PNG, TIFF, JPEG, GIF, PNM, PSD, etc.)
  - **SVG input is supported** and converted using Apache Batik (to PNG, JPG, or TIFF)
- **Output:**
  - `jpg`, `png`, `tiff`

### Output Format Quality Characteristics

- **JPG**: Lossy compression format ideal for photographs and complex images where some quality loss is acceptable for smaller file sizes. Not recommended for archival of images where quality is paramount.
- **PNG**: Lossless compression format well-suited for images with sharp lines, text, and graphics. It preserves image quality during compression and is a good choice for archival of digital art, logos, and screenshots.
- **TIFF**: A flexible image format that can store images with lossless compression (or no compression at all). It is ideal for archival purposes because it preserves maximum image quality and supports metadata. Results in larger file sizes.

## Features (v1.0.0)

- **Batch image format conversion**: Converts a wide range of raster image formats to preservation-friendly formats (`jpg`, `png`, `tiff`).
- **SVG conversion**: Converts SVG and SVGZ images to supported raster formats using Batik.
- **Automatic format detection**: Uses ImageIO for input detection and Siegfried for post-conversion format validation.
- **Preservation representation creation**: Each conversion creates a new representation for the converted files.
- **Unit tested**: Comprehensive test suite ensures correct conversion, exclusion, and validation logic.

## What Gets Tested

- Creation of AIP and representations
- Ingestion of all sample files
- Conversion for each supported output format
- Exclusion of unsupported and already-converted files
- Validation of output file extension and MIME type

## Known Limitations

- **Duplicate/overwrite behavior**: If multiple files with the same base name are converted to the same extension, only one may survive in the output (due to base plugin logic). Use unique file names for best results.
- **MIME type mapping**: Some advanced MIME type to extension mappings may require further configuration.
- **Plugin conversion logic**: Currently, all files may be copied to new representations; future versions will improve to only add converted files.
- **Quality degradation option not supported**: The plugin excludes files that would result in quality loss during conversion:
  - **Alpha channel preservation**: Files with transparency/alpha channels are excluded when converting to formats that don't support transparency (e.g., JPEG)
  - **Bit depth preservation**: Files with higher bit depth than the target format supports are excluded to prevent data loss
  - **Animation preservation**: Animated formats (e.g., GIF) are excluded to preserve animation frames and timing
- **No parallelization**: Conversion is currently single-threaded.
- **Error handling**: If a format is unsupported or conversion fails, an exception is thrown and logged; failed files are skipped.

## Prerequisites

- Java 21 or higher
- Maven 3.6+
- Docker (for containerized runs)

## How to build and run

To build, execute:

```shell
./build.sh
```

This will run with the latest ETERNA version. If you require a different ETERNA version, e.g. vX.X.X, update the `pom.xml` parent version and execute:

```shell
./build.sh vX.X.X
```

The build script will compile the plugin and create a Docker image with the base ETERNA plus the plugin installed.

To run, execute (replace `image-converter` with the project folder name if different):

```shell
docker run -p 8080:8080 image-converter:latest
```

Then open in your favorite browser: [http://localhost:8080](http://localhost:8080).

### Running Tests

To run the test suite:

```shell
mvn test
```

## Configuration

Edit `src/main/resources/config/image-converter.properties` to customize input/output formats and plugin behavior.

### Key Configuration Properties

- `core.tools.image-converter.inputFormatExtensions`: Space-separated list of supported input file extensions
- `core.tools.image-converter.outputFormats`: Space-separated list of supported output formats (`tiff`, `jpg`, `png`)
- `core.tools.image-converter.excludedExtensions`: Space-separated list of file extensions to exclude from conversion (e.g., `cur pict ico dds pfm hdr`)
- `core.tools.image-converter.inputFormatMimeTypes`: Supported MIME types for input files
- `core.tools.image-converter.inputFormatPronoms`: Supported PRONOM identifiers for input files

## Contributing

Contributions are welcome! Please see [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines.

## License

This project is licensed under the LGPL v3 License. See [LICENSE.md](LICENSE.md) for details.

## Future Improvements

- Improved MIME type mapping and configuration
- Smarter file handling to avoid unnecessary copies
- Support for more input/output formats
- Parallel/concurrent conversion support
- More granular error reporting and logging
