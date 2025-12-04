# Image Converter - User Guide

## Overview

The image conversion job is designed to support digital preservation workflows in the ETERNA system. The tool converts various image formats to preservation-friendly formats to ensure long-term accessibility and archival stability for digital images.

## How to Use the Image Conversion Job

1. **Start conversion**: You can begin a conversion process by selecting an Intellectual entity, a Representation, or an individual file via the Catalog page or Search page and starting a new Preservation Job.
2. **Select output format**: Choose the desired output format from available options (JPG, PNG, or TIFF)
3. **Configure conversion**: Specify whether you want to create a new representation or dissemination copy
4. **Run conversion**: Run the tool to start the conversion process

### Supported Input Formats
The tool supports conversion from a wide range of image formats including:
- **Common formats**: BMP, PNG, TIFF, JPEG, GIF, PSD
- **Legacy formats**: PNM, PICT, ICO, CUR, DDS, HDR, TGA, PCX, DCX
- **Vector graphics**: SVG and SVGZ (converted with specialized processing)
- **etc.**

### Output Formats
You can convert images to three preservation formats:

- **JPG**: Lossy compression format suitable for photographs where some quality loss is acceptable for smaller file sizes
- **PNG**: Lossless compression format ideal for images with sharp lines, text, and graphics
- **TIFF**: Lossless format perfect for archiving, preserves maximum quality and supports metadata

### Conversion Process
1. **Format detection**: The tool automatically detects the input format
2. **Quality assessment**: Files are evaluated to ensure no quality loss occurs during conversion
3. **Conversion execution**: Images are converted using specialized libraries for optimal results
4. **Representation creation**: During conversion, the new files will be placed in the same intellectual entity as the originals, in a representation with the chosen representation type and status: `Preservation`. If such a representation does not already exist in the intellectual entity, a new one will be created.

## Known Limitations

### File Handling Notes
- Files that are already in the target format are not converted
- If multiple files have the same name, only one may be preserved due to system limitations

### Quality Settings
- SVG to JPG conversions use 95% quality setting to balance file size and visual fidelity
- Other conversions maintain the highest possible quality for the selected format

## Best Practices

1. **Choose appropriate formats**: Choose TIFF for maximum archival quality, PNG for lossless compression, JPG only when file size is critical
2. **Use unique filenames**: Ensure files have unique names to avoid potential overwrites
3. **Test conversions**: Run test conversions on sample files before processing large collections
4. **Review results**: Always verify that the converted files meet your preservation requirements
5. **Consider originals**: Keep original files available alongside converted versions

## Troubleshooting

- **Conversion fails**: Check that the input format is supported and that the file is not corrupted.  
    Other possible causes of quality degradation may include:
    - **Transparent images**: Files with alpha channels (transparency) are excluded when converting to JPG format
    - **High bit depth**: Images with bit depth higher than what the target format supports are excluded
    - **Animated formats**: Animated images (such as GIF) are excluded to preserve animation frames
    - **Unsupported formats**: Files with file extensions known to cause problems (cur, pict, ico, dds, pfm, hdr)
- **Files not converted**: Verify that the file is not in an excluded category or already in the target format
- **Quality issues**: Ensure you are using an appropriate output format for your content type
- **Large file sizes**: TIFF format will produce larger files but preserves maximum quality

For technical support or questions about specific formats, contact your system administrator.
