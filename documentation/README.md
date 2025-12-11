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
2. **Image normalization**: Images are automatically adjusted for the target format, including handling transparency, color spaces, and bit depths as needed
3. **Quality assessment**: Files are evaluated to ensure minimal quality loss occurs during conversion
4. **Conversion execution**: Images are converted using specialized libraries for optimal results
5. **Representation creation**: During conversion, the new files will be placed in the same intellectual entity as the originals, in a representation with the chosen representation type and status: `Preservation`. If such a representation does not already exist in the intellectual entity, a new one will be created.

## Known Limitations

### File Handling Notes
- Files that are already in the target format are not converted
- If multiple files have the same name, only one may be preserved due to system limitations

### Quality Settings
- SVG to JPG conversions use 95% quality setting to balance file size and visual fidelity
- Images with transparency are automatically blended with a white background when converting to JPG format
- Color spaces are automatically converted to sRGB standard when needed for compatibility
- Bit depths are adjusted to match format requirements (e.g., reducing high-bit depth images for JPG compatibility)
- Other conversions maintain the highest possible quality for the selected format

## Best Practices

1. **Choose appropriate formats**: Choose TIFF for maximum archival quality, PNG for lossless compression, JPG only when file size is critical
2. **Use unique filenames**: Ensure files have unique names to avoid potential overwrites
3. **Test conversions**: Run test conversions on sample files before processing large collections
4. **Review results**: Always verify that the converted files meet your preservation requirements
5. **Consider originals**: Keep original files available alongside converted versions

## Troubleshooting

### Common Issues

**Conversion Fails**
- **Cause**: Input format not supported or file corrupted
- **Solution**: Verify the file format is supported and check file integrity

**Files Not Converted**
- **Cause**: File already in target format or in excluded category
- **Solution**: Check if conversion is actually needed

**Unexpected Results**
- **Transparent images**: Alpha channels are blended with white background for JPG conversion
- **High bit depth images**: Automatically adjusted to match target format capabilities
- **Color variations**: Images converted to standard sRGB color space when needed

**Performance Issues**
- **Large file sizes**: TIFF format preserves maximum quality but creates larger files
- **Slow processing**: High-resolution or complex images take longer to process

### Additional Notes

- **Format selection**: Choose appropriate output format for your content type (TIFF for archiving, PNG for graphics, JPG for photos)
- **Unsupported formats**: Some file formats are not supported and cannot be converted

For technical support or questions about specific formats, contact your system administrator or raise an issue on our GitHub repository.
