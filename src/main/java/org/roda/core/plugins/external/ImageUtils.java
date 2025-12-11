package org.roda.core.plugins.external;

import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriter;
import javax.imageio.spi.ImageWriterSpi;
import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.SampleModel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ImageUtils {
    private static final Map<String, ImageFormatProperties> imageFormatProperties;

    static {
        imageFormatProperties = new HashMap<>();
        imageFormatProperties.put("jpg", new ImageFormatProperties(8, false));
        imageFormatProperties.put("png", new ImageFormatProperties(16, true));
        imageFormatProperties.put("tiff", new ImageFormatProperties(-1, true));
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

    public static BufferedImage convertARGB2RGB(final BufferedImage srcImage) {
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

    public static BufferedImage normalizeImageForFormat(final BufferedImage srcImage, final String outputFormatName) {
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
