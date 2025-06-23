/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE.md file at the root of the source
 * tree and available online at
 * <p>
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.external;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;
import javax.imageio.spi.IIORegistry;

import org.roda.core.RodaCoreFactory;
import org.roda.core.common.FileFormatUtils;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.v2.IsRODAObject;
import org.roda.core.data.v2.jobs.PluginParameter;
import org.roda.core.data.v2.jobs.PluginParameter.PluginParameterType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.base.conversion.AbstractConvertPlugin2;
import org.roda.core.storage.StorageService;
import org.roda.core.util.CommandException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.apache.batik.transcoder.image.JPEGTranscoder;
import org.apache.batik.transcoder.image.TIFFTranscoder;

/**
 * Plugin for converting image formats using TwelveMonkeys ImageIO
 */
@SuppressWarnings("deprecation")
public class ImageConverter<T extends IsRODAObject> extends AbstractConvertPlugin2<T> {

  private static final Logger LOGGER = LoggerFactory.getLogger(ImageConverter.class);

  private static final String CONVERSION_PROFILE_PARAM_KEY = "parameter.conversion_profile";

  private static Map<String, PluginParameter> pluginParameters = new LinkedHashMap<>();

  private static final Map<String, org.apache.batik.transcoder.Transcoder> TRANSCODER_MAP = new HashMap<>();

  static {
    TRANSCODER_MAP.put("png", new PNGTranscoder());
    JPEGTranscoder jpegTranscoder = new JPEGTranscoder();
    jpegTranscoder.addTranscodingHint(JPEGTranscoder.KEY_QUALITY, 0.95f);
    TRANSCODER_MAP.put("jpg", jpegTranscoder);
    TRANSCODER_MAP.put("jpeg", jpegTranscoder);
    TRANSCODER_MAP.put("tiff", new TIFFTranscoder());

    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_IGNORE_OTHER_FILES,
        new PluginParameter(RodaConstants.PLUGIN_PARAMS_IGNORE_OTHER_FILES, "Ignore other files",
            PluginParameterType.BOOLEAN, "true", false, false,
            "Do not process files that have a different format from the indicated."));

    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_REPRESENTATION_OR_DIP,
        PluginParameter.getBuilder(RodaConstants.PLUGIN_PARAMS_REPRESENTATION_OR_DIP, "Outcome",
            PluginParameterType.CONVERSION)
            .withDescription(
                "A conversion can create a representation or a dissemination. Please choose which option to output")
            .build());
  }

  public ImageConverter() {
    super();
  }

  @Override
  public void init() throws PluginException {
    System.out.println("ImageConverter init2");
    // Ensure ImageIO plugins are registered
    ImageIO.scanForPlugins();
    IIORegistry registry = IIORegistry.getDefaultInstance();
    registry.registerServiceProvider(new com.twelvemonkeys.imageio.plugins.xwd.XWDImageReaderSpi());
    registry.registerServiceProvider(new com.twelvemonkeys.imageio.plugins.bmp.CURImageReaderSpi());
    registry.registerServiceProvider(new com.twelvemonkeys.imageio.plugins.bmp.ICOImageReaderSpi());
    registry.registerServiceProvider(new com.twelvemonkeys.imageio.plugins.bmp.BMPImageReaderSpi());
    registry.registerServiceProvider(new com.twelvemonkeys.imageio.plugins.pict.PICTImageReaderSpi());
    registry.registerServiceProvider(new com.twelvemonkeys.imageio.plugins.pnm.PAMImageReaderSpi());
    registry.registerServiceProvider(new com.twelvemonkeys.imageio.plugins.icns.ICNSImageReaderSpi());
    registry.registerServiceProvider(new com.twelvemonkeys.imageio.plugins.hdr.HDRImageReaderSpi());
    registry.registerServiceProvider(new com.twelvemonkeys.imageio.plugins.tiff.TIFFImageReaderSpi());
    registry.registerServiceProvider(new com.twelvemonkeys.imageio.plugins.tga.TGAImageReaderSpi());
    // registry.registerServiceProvider(new
    // com.twelvemonkeys.imageio.plugins.svg.SVGImageReaderSpi());
    registry.registerServiceProvider(new com.twelvemonkeys.imageio.plugins.sgi.SGIImageReaderSpi());
    registry.registerServiceProvider(new com.twelvemonkeys.imageio.plugins.pnm.PNMImageReaderSpi());
    registry.registerServiceProvider(new com.twelvemonkeys.imageio.plugins.pnm.PNMImageReaderSpi());
    registry.registerServiceProvider(new com.twelvemonkeys.imageio.plugins.pcx.PCXImageReaderSpi());
    registry.registerServiceProvider(new com.twelvemonkeys.imageio.plugins.dds.DDSImageReaderSpi());
    registry.registerServiceProvider(new com.twelvemonkeys.imageio.plugins.jpeg.JPEGImageReaderSpi());
    registry.registerServiceProvider(new com.twelvemonkeys.imageio.plugins.tiff.BigTIFFImageReaderSpi());
    registry.registerServiceProvider(new com.twelvemonkeys.imageio.plugins.psd.PSDImageReaderSpi());
    registry.registerServiceProvider(new com.twelvemonkeys.imageio.plugins.dcx.DCXImageReaderSpi());
    registry.registerServiceProvider(new com.twelvemonkeys.imageio.plugins.dcx.DCXImageReaderSpi());
  }

  @Override
  public String getName() {
    return "Image Converter";
  }

  @Override
  public String getDescription() {
    return "Converts images between various formats using the TwelveMonkeys ImageIO library for extended format support.";
  }

  @Override
  public String getVersionImpl() {
    // Get from pom.xml <version>
    return getClass().getPackage().getImplementationVersion();
  }

  @Override
  public Plugin<T> cloneMe() {
    return new ImageConverter<T>();
  }

  @Override
  protected Map<String, PluginParameter> getDefaultParameters() {
    return pluginParameters.entrySet().stream()
        .collect(Collectors.toMap(
            Map.Entry::getKey,
            e -> new PluginParameter(e.getValue()),
            (u, v) -> u,
            LinkedHashMap::new));
  }

  @Override
  public List<PluginParameter> getParameters() {
    // This now returns the filtered and ordered list
    return this.orderParameters(this.getDefaultParameters());
  }

  @Override
  protected List<PluginParameter> orderParameters(Map<String, PluginParameter> params) {
    return this.getDefaultParameters().values().stream().collect(Collectors.toList());
  }

  @Override
  public void setParameterValues(Map<String, String> parameters) throws InvalidParameterException {
    String profileValue = parameters.get(CONVERSION_PROFILE_PARAM_KEY);
    if (profileValue == null || profileValue.trim().isEmpty()) {
      LOGGER.warn("Conversion profile parameter '{}' is missing or empty in the provided parameters.",
          CONVERSION_PROFILE_PARAM_KEY);
      throw new InvalidParameterException(
          "Required conversion profile parameter '" + CONVERSION_PROFILE_PARAM_KEY + "' is missing.");
    }
    profileValue = profileValue.trim().toLowerCase();
    parameters.put("parameter.option." + profileValue, "[parameter.output_format]");
    parameters.put(RodaConstants.PLUGIN_PARAMS_OUTPUT_FORMAT, profileValue);
    LOGGER.debug("Setting output format from conversion profile parameter '{}': {}", CONVERSION_PROFILE_PARAM_KEY,
        profileValue);
    super.setParameterValues(parameters);
  }

  @Override
  public boolean areParameterValuesValid() {
    // Validate based on the parameter *after* setParameterValues has run
    Map<String, String> params = getParameterValues();

    // Check if the base class successfully stored the rep/dip choice
    boolean repDipSet = params.containsKey(RodaConstants.PLUGIN_PARAMS_REPRESENTATION_OR_DIP)
        && !params.get(RodaConstants.PLUGIN_PARAMS_REPRESENTATION_OR_DIP).isEmpty();

    // Check if the output format was successfully set
    boolean outputFormatSet = super.getOutputFormat() != null && !super.getOutputFormat().isEmpty();

    if (!repDipSet) {
      LOGGER.error("Validation failed: REPRESENTATION_OR_DIP parameter is missing or empty.");
    }
    if (!outputFormatSet) {
      LOGGER.error("Validation failed: Output format could not be determined (was {} parameter set correctly?).",
          CONVERSION_PROFILE_PARAM_KEY);
    }

    return repDipSet && outputFormatSet;
  }

  @Override
  public Report beforeAllExecute(IndexService index, ModelService model, StorageService storage) {
    return new Report();
  }

  @Override
  public Report afterAllExecute(IndexService index, ModelService model, StorageService storage) {
    return new Report();
  }

  @Override
  public List<String> getApplicableTo() {
    return FileFormatUtils.getInputExtensions("image-converter");
  }

  @Override
  public List<String> getConvertableTo() {
    String outputFormats = RodaCoreFactory.getRodaConfigurationAsString("core", "tools", "image-converter",
        "outputFormats");
    return Arrays.asList(outputFormats.split("\\s+"));
  }

  @Override
  public Map<String, List<String>> getPronomToExtension() {
    return FileFormatUtils.getPronomToExtension("image-converter");
  }

  @Override
  public Map<String, List<String>> getMimetypeToExtension() {
    return FileFormatUtils.getMimetypeToExtension("image-converter");
  }

  @Override
  public String executePlugin(java.nio.file.Path inputPath, java.nio.file.Path outputPath, String fileFormat)
      throws UnsupportedOperationException, IOException, CommandException {
    LOGGER.info("Starting image conversion: {} -> {} ({})", inputPath, outputPath, fileFormat);

    String outputFormat = super.getOutputFormat();

    if (outputFormat == null || outputFormat.trim().isEmpty()) {
      throw new CommandException("Output format was not set correctly in the plugin parameters.");
    }

    LOGGER.info("Executing image conversion: {} -> {} (Output Format: {})", inputPath, outputPath, outputFormat);

    // Check if input is SVG
    String inputLower = inputPath.toString().toLowerCase();
    if (inputLower.endsWith(".svg") || inputLower.endsWith(".svgz")) {
      return convertSvg(inputPath, outputPath, outputFormat);
    }

    BufferedImage image = null;
    boolean success = false;
    try {
      image = ImageIO.read(inputPath.toFile());

      if (image == null) {
        throw new IOException(
            "Could not read input image file: " + inputPath + ". Format might be unsupported or file is corrupted.");
      }
      success = ImageIO.write(image, outputFormat, outputPath.toFile());

      if (!success) {
        throw new IOException("Could not write output image file: " + outputPath + ". Format '" + outputFormat
            + "' might be unsupported by available writers.");
      }

      LOGGER.info("Successfully converted image to {}", outputPath);
      return outputPath.toString();

    } catch (IOException e) {
      LOGGER.error("Image conversion failed: {}", e.getMessage(), e);
      throw e;
    } catch (Exception e) {
      LOGGER.error("An unexpected error occurred during image conversion: {}", e.getMessage(), e);
      throw new IOException("Unexpected error during conversion: " + e.getMessage(), e);
    }
  }

  private String convertSvg(java.nio.file.Path inputPath, java.nio.file.Path outputPath, String outputFormat)
      throws IOException, CommandException {
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

      LOGGER.info("Successfully converted SVG to {}", outputPath);
      return outputPath.toString();

    } catch (Exception e) {
      LOGGER.error("SVG conversion failed: {}", e.getMessage(), e);
      throw new IOException("Error converting SVG: " + e.getMessage(), e);
    }
  }
}
