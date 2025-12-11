package org.roda.core.plugins.external;

import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.JPEGTranscoder;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.apache.batik.transcoder.image.TIFFTranscoder;
import org.roda.core.util.CommandException;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriter;
import javax.imageio.spi.ImageWriterSpi;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.SampleModel;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ImageConverter {
    private static final Map<String, ImageFormatProperties> imageFormatProperties;
    private static final Map<String, org.apache.batik.transcoder.Transcoder> TRANSCODER_MAP = new HashMap<>();

    static {
        imageFormatProperties = new HashMap<>();
        imageFormatProperties.put("jpg", new ImageFormatProperties(8, false));
        imageFormatProperties.put("png", new ImageFormatProperties(16, true));
        imageFormatProperties.put("tiff", new ImageFormatProperties(-1, true));

        JPEGTranscoder jpegTranscoder = new JPEGTranscoder();
        // Set high quality (95%) for preservation purposes - balances file size with
        // image quality
        // Higher than default (80%) to maintain visual fidelity while still providing
        // compression benefits
        // Note: This setting only affects SVG-to-JPEG conversions, not regular image
        // conversions
        jpegTranscoder.addTranscodingHint(JPEGTranscoder.KEY_QUALITY, 0.95f);

        TRANSCODER_MAP.put("png", new PNGTranscoder());
        TRANSCODER_MAP.put("jpg", jpegTranscoder);
        TRANSCODER_MAP.put("jpeg", jpegTranscoder);
        TRANSCODER_MAP.put("tiff", new TIFFTranscoder());
    }

    public static void convert(final Path inputPath, final Path outputPath, final String outputFormat) throws CommandException {
        // Check if input is SVG
        String inputLower = inputPath.toString().toLowerCase();
        if (inputLower.endsWith(".svg") || inputLower.endsWith(".svgz")) {
            convertSvg(inputPath, outputPath, outputFormat);
            return;
        }

        final BufferedImage srcImage = readImage(inputPath);
        final BufferedImage dstImage = normalizeImageForFormat(srcImage, outputFormat);

        boolean success;
        try {
            success = ImageIO.write(dstImage, outputFormat, outputPath.toFile());
        } catch (IOException e) {
            throw new CommandException("Could not write output image file: " + outputPath + ". Format '" + outputFormat
                    + "' might be unsupported by available writers.");
        }
        if (!success) {
            throw new CommandException("Could not write output image file: " + outputPath + ". Format '" + outputFormat
                    + "' might be unsupported by available writers.");
        }
    }

    private static void convertSvg(Path inputPath, Path outputPath, String outputFormat) throws CommandException {
        try {
            // Get the appropriate transcoder
            org.apache.batik.transcoder.Transcoder transcoder = TRANSCODER_MAP.get(outputFormat.toLowerCase());
            if (transcoder == null) {
                throw new CommandException("Unsupported output format for SVG conversion: " + outputFormat);
            }

            // Create transcoder input/output
            String svgURI = inputPath.toUri().toURL().toString();
            TranscoderInput input = new TranscoderInput(svgURI);

            try (OutputStream ostream = java.nio.file.Files.newOutputStream(outputPath)) {
                TranscoderOutput output = new TranscoderOutput(ostream);
                transcoder.transcode(input, output);
            }

            ImageConverterPlugin.LOGGER.info("Successfully converted SVG to {}", outputPath);
        } catch (Exception e) {
            ImageConverterPlugin.LOGGER.error("SVG conversion failed: {}", e.getMessage(), e);
            throw new CommandException(String.format("Error! Could not convert SVG to %s.", outputFormat.toUpperCase()));
        }
    }

    private static BufferedImage readImage(final Path inputPath) throws CommandException {
        if (inputPath == null) {
            throw new CommandException("Input image path is not defined.");
        }

        try (ImageInputStream imageInputStream = ImageIO.createImageInputStream(inputPath.toFile())) {
            if (imageInputStream == null) {
                throw new CommandException("Could not open image file.");
            }

            Iterator<ImageReader> readers = ImageIO.getImageReaders(imageInputStream);
            if (!readers.hasNext()) {
                throw new CommandException("Unsupported input image format.");
            }

            final ImageReader reader = readers.next();
            reader.setInput(imageInputStream);

            int width;
            int height;
            ImageTypeSpecifier srcImageTypeSpecifier;
            try {
                width = reader.getWidth(0);
                height = reader.getHeight(0);
                srcImageTypeSpecifier = reader.getRawImageType(0);
            } catch (Exception e) {
                throw new CommandException("Could not read image data.");
            }

            BufferedImage image;
            try {
                image = srcImageTypeSpecifier.createBufferedImage(width, height);
            } catch (Exception e) {
                if (width < 0) {
                    throw new CommandException("Input image has a negative width.");
                } else if (height < 0) {
                    throw new CommandException("Input image has a negative height.");
                } else if(((long)width) * ((long)height) > Integer.MAX_VALUE) {
                    throw new CommandException("Input image is too large.");
                } else {
                    throw new CommandException("Could not allocate memory for image conversion.");
                }
            }

            final ImageReadParam param = reader.getDefaultReadParam();
            param.setDestination(image);

            try {
                reader.read(0, param);
            } catch (Exception e) {
                throw new CommandException("Could not read input image file: " + inputPath + ". Format might be unsupported or file is corrupted.");
            }

            return image;
        } catch (IOException e) {
            throw new CommandException("Could not read input image file: " + inputPath.toFile() + ".");
        }
    }

    private static Boolean isImageSupportedByWriter(final BufferedImage image, final String formatName) {
        Iterator<ImageWriter> it = ImageIO.getImageWritersByFormatName(formatName);
        while (it.hasNext()) {
            ImageWriter imageWriter = it.next();
            ImageWriterSpi spi = imageWriter.getOriginatingProvider();

            if (spi.canEncodeImage(image)) {
                return true;
            }
        }
        return false;
    }

    private static int getMaximumBitDepth(final SampleModel sampleModel) {
        int[] sampleSize = sampleModel.getSampleSize();
        int bitDepth = sampleSize[0];
        for (int i = 1; i < sampleSize.length; i++) {
            if (sampleSize[i] > bitDepth) {
                bitDepth = sampleSize[i];
            }
        }

        return bitDepth;
    }

    private static BufferedImage convertARGB2RGB(final BufferedImage srcImage) {
        final BufferedImage dstImage = new BufferedImage(srcImage.getWidth(), srcImage.getHeight(), BufferedImage.TYPE_INT_RGB);
        final int height = srcImage.getHeight();
        final int width =  srcImage.getWidth();
        final int backgroundColor = 0xFFFFFF;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                final int argb = srcImage.getRGB(x, y);
                final int alpha = (argb >> 24) & 0xFF;

                if (alpha == 255) {
                    dstImage.setRGB(x, y, argb & 0xFFFFFF);
                } else if (alpha == 0) {
                    dstImage.setRGB(x, y, backgroundColor);
                } else {
                    int r = ((argb >> 16) & 0xFF);
                    int g = ((argb >> 8) & 0xFF);
                    int b = (argb & 0xFF);

                    int br = (backgroundColor >> 16) & 0xFF;
                    int bg = (backgroundColor >> 8) & 0xFF;
                    int bb = backgroundColor & 0xFF;

                    r = (r * alpha + br * (255 - alpha)) / 255;
                    g = (g * alpha + bg * (255 - alpha)) / 255;
                    b = (b * alpha + bb * (255 - alpha)) / 255;

                    dstImage.setRGB(x, y, (r << 16) | (g << 8) | b);
                }
            }
        }

        return dstImage;
    }

    private static BufferedImage normalizeImageForFormat(final BufferedImage srcImage, final String outputFormatName) {
        final ImageFormatProperties dstImageFormatProperties = imageFormatProperties.get(outputFormatName);
        if (isImageSupportedByWriter(srcImage, outputFormatName) && srcImage.getColorModel().hasAlpha() == dstImageFormatProperties.supportsAlpha()) {
            return srcImage;
        }

        final ColorModel srcColorModel = srcImage.getColorModel();
        final ColorSpace srcColorSpace = srcColorModel.getColorSpace();
        final SampleModel srcSampleModel = srcImage.getSampleModel();
        final int srcBitDepth = getMaximumBitDepth(srcSampleModel);
        final int dstMaximumBitDepth = dstImageFormatProperties.maximumBitDepth();

        BufferedImage image = srcImage;

        if (!srcColorSpace.isCS_sRGB() || srcSampleModel.getDataType() == DataBuffer.TYPE_FLOAT || (dstMaximumBitDepth != -1 && srcBitDepth > dstMaximumBitDepth)) {
            final int bufferedImageType = srcColorModel.hasAlpha() || srcColorModel.getTransparency() != Transparency.OPAQUE ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB;
            final ImageTypeSpecifier imageTypeSpecifier = ImageTypeSpecifier.createFromBufferedImageType(bufferedImageType);
            image = imageTypeSpecifier.createBufferedImage(srcImage.getWidth(), srcImage.getHeight());

            final Map<RenderingHints.Key, Object> renderingHints = new HashMap<>();
            renderingHints.put(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
            renderingHints.put(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
            renderingHints.put(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);

            ColorConvertOp op = new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_sRGB), new RenderingHints(renderingHints));
            op.filter(srcImage, image);
        }

        BufferedImage dstImage = image;
        if (srcColorModel.hasAlpha() && !dstImageFormatProperties.supportsAlpha()) {
            dstImage = convertARGB2RGB(image);
        }

        return dstImage;
    }
}
