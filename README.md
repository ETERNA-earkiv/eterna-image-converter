Image Converter
-----------------------

[![License: LGPL v3](https://img.shields.io/badge/License-LGPL%20v3-blue.svg)](LICENSE.md)

A plugin for [ETERNA](https://github.com/ETERNA-earkiv/ETERNA) providing image format conversion for digital preservation workflows.

## Features

- **Image format conversion**: Converts a wide range image formats to (`jpg`, `png`, `tiff`).
  - Most common raster image formats supported by Java ImageIO and TwelveMonkeys (e.g., BMP, PNG, TIFF, JPEG, GIF, PNM, PSD, etc.)
  - **SVG input is supported** and converted using Apache Batik (to PNG, JPG, or TIFF)
- **Lossy format normalization**: Includes color-space conversions, bit-depth down-sampling with dithering, and alpha channel removal. Explicit loading of first image from multi-image files (animated GIFs, ICO files with different dimensions).

### Output Format Quality Characteristics

- **JPG**: Lossy compression format ideal for photographs and complex images where some quality loss is acceptable for smaller file sizes. Not recommended for archival of images where quality is paramount.
- **PNG**: Lossless compression format well-suited for images with sharp lines, text, and graphics. It preserves image quality during compression and is a good choice for archival of digital art, logos, and screenshots.
- **TIFF**: A flexible image format that can store images with lossless compression (or no compression at all). It is ideal for archival purposes because it preserves maximum image quality and supports metadata. Results in larger file sizes.

### Known Limitations

- **Duplicate/overwrite behavior**: If multiple files with the same base name are converted to the same extension, only one may survive in the output (due to base plugin logic). Use unique file names for best results.
- **Quality degradation**: Some output formats don´t support all of the features of all input formats. Conversion can therefor lead to quality degradation.
**For example:** 
  - **Alpha channel preservation**: Files with transparency/alpha channels may lose their transparancy/Alpha channel if converted to a format that does not support it (e.g., JPEG)
  - **Bit depth preservation**: Files with higher bit depth than the target format supports are downsampled and dithered into their output formats maximum bit depth.
  - **Animation preservation**: Animated formats (e.g., GIF) only get their first frame converted into the output format.

## Installation

Download [image-converter.zip](http://github.com/ETERNA-earkiv/eterna-image-converter/releases/latest/download/image-converter.zip) and extract it into `/.roda/config/plugins/` and restart ETERNA.

## License

See [LICENSE.md](LICENSE.md) for details.
