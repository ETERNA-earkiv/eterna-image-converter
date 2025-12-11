Image Converter
-----------------------

[![License: LGPL v3](https://img.shields.io/badge/License-LGPL%20v3-blue.svg)](LICENSE.md)

A plugin for [ETERNA](https://github.com/ETERNA-earkiv/ETERNA) providing robust image format conversion for digital preservation workflows.

## Table of Contents

- [Supported Formats](#supported-formats)
- [Features](#features-v100)
- [What Gets Tested](#what-gets-tested)
- [Known Limitations](#known-limitations)
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

## Features (v1.1.0)

- **Batch image format conversion**: Converts a wide range of raster image formats to preservation-friendly formats (`jpg`, `png`, `tiff`).
- **SVG conversion**: Converts SVG and SVGZ images to supported raster formats using Batik.
- **Automatic format detection**: Uses ImageIO for input detection and Siegfried for post-conversion format validation.
- **Preservation representation creation**: Each conversion creates a new representation for the converted files.
- **Lossy format normalization**: Includes color-space conversions, bit-depth down-sampling with dithering, and alpha channel removal for optimal preservation.
- **Enhanced multi-image file handling**: Explicit loading of first image from multi-image files (animated GIFs, ICO files with different dimensions).
- **Improved error handling**: More lenient image loading with better error recovery and logging.
- **Format-specific properties management**: Uses ImageFormatProperties record for managing format-specific conversion properties.
- **Unit tested**: Comprehensive test suite ensures correct conversion, exclusion, and validation logic.

## What Gets Tested

- Creation of AIP and representations
- Ingestion of all sample files
- Conversion for each supported output format
- Exclusion of unsupported and already-converted files
- Validation of output file extension and MIME type

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
