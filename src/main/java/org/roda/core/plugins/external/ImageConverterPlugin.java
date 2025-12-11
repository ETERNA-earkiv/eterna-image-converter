/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE.md file at the root of the source
 * tree
 */
package org.roda.core.plugins.external;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
import org.roda.core.plugins.base.conversion.AbstractConvertPlugin;
import org.roda.core.storage.StorageService;
import org.roda.core.util.CommandException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Plugin for converting image formats
 */
@SuppressWarnings("deprecation")
public class ImageConverterPlugin<T extends IsRODAObject> extends AbstractConvertPlugin<T> {

  protected static final Logger LOGGER = LoggerFactory.getLogger(ImageConverterPlugin.class);

  private static final String CONVERSION_PROFILE_PARAM_KEY = "parameter.conversion_profile";

  private static final Map<String, PluginParameter> pluginParameters = new LinkedHashMap<>();

  static {
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_REPRESENTATION_OR_DIP,
        PluginParameter.getBuilder(RodaConstants.PLUGIN_PARAMS_REPRESENTATION_OR_DIP, "Outcome",
            PluginParameterType.CONVERSION)
            .withDescription(
                "A conversion can create a representation or a dissemination. Please choose which option to output")
            .build());
  }

  public ImageConverterPlugin() {
    super();
  }

  @Override
  public void init() {
    System.out.println("ImageConverterPlugin initialized");
    LOGGER.info("ImageConverterPlugin initialized");
    // Ensure ImageIO plugins are registered
    // ImageIO.scanForPlugins();
    IIORegistry registry = IIORegistry.getDefaultInstance();
    registry.registerServiceProvider(new com.twelvemonkeys.imageio.plugins.xwd.XWDImageReaderSpi());
    registry.registerServiceProvider(new com.twelvemonkeys.imageio.plugins.bmp.CURImageReaderSpi());
    registry.registerServiceProvider(new com.twelvemonkeys.imageio.plugins.bmp.ICOImageReaderSpi());
    registry.registerServiceProvider(new com.twelvemonkeys.imageio.plugins.bmp.BMPImageReaderSpi());
    registry.registerServiceProvider(new com.twelvemonkeys.imageio.plugins.pict.PICTImageReaderSpi());
    registry.registerServiceProvider(new com.twelvemonkeys.imageio.plugins.pnm.PAMImageReaderSpi());
    registry.registerServiceProvider(new com.twelvemonkeys.imageio.plugins.icns.ICNSImageReaderSpi());
    registry.registerServiceProvider(new com.twelvemonkeys.imageio.plugins.hdr.HDRImageReaderSpi());
    // registry.registerServiceProvider(new com.twelvemonkeys.imageio.plugins.tiff.TIFFImageReaderSpi());
    registry.registerServiceProvider(new com.twelvemonkeys.imageio.plugins.tga.TGAImageReaderSpi());
    // registry.registerServiceProvider(new
    // com.twelvemonkeys.imageio.plugins.svg.SVGImageReaderSpi());
    registry.registerServiceProvider(new com.twelvemonkeys.imageio.plugins.sgi.SGIImageReaderSpi());
    registry.registerServiceProvider(new com.twelvemonkeys.imageio.plugins.pnm.PNMImageReaderSpi());
    registry.registerServiceProvider(new com.twelvemonkeys.imageio.plugins.pcx.PCXImageReaderSpi());
    registry.registerServiceProvider(new com.twelvemonkeys.imageio.plugins.dds.DDSImageReaderSpi());
    registry.registerServiceProvider(new com.twelvemonkeys.imageio.plugins.jpeg.JPEGImageReaderSpi());
    registry.registerServiceProvider(new com.twelvemonkeys.imageio.plugins.tiff.BigTIFFImageReaderSpi());
    registry.registerServiceProvider(new com.twelvemonkeys.imageio.plugins.psd.PSDImageReaderSpi());
    registry.registerServiceProvider(new com.twelvemonkeys.imageio.plugins.dcx.DCXImageReaderSpi());

    registry.registerServiceProvider(new com.twelvemonkeys.imageio.plugins.jpeg.JPEGImageWriterSpi());
    // registry.registerServiceProvider(new com.twelvemonkeys.imageio.plugins.tiff.TIFFImageWriterSpi());
    // registry.registerServiceProvider(new com.twelvemonkeys.imageio.plugins.tiff.BigTIFFImageWriterSpi());
  }

  @Override
  public String getName() {
    return "Image Converter";
  }

  @Override
  public String getDescription() {
    return "Image format conversion plugin that supports a wide range of input and output formats. " +
        "Uses the TwelveMonkeys ImageIO library to handle legacy and specialized image formats. " +
        "Includes specialized SVG conversion support using Apache Batik transcoders. " +
        "Ideal for digital preservation workflows and format migration.";
  }

  @Override
  public String getVersionImpl() {
    // Get from pom.xml <version>
    return getClass().getPackage().getImplementationVersion();
  }

  @Override
  public Plugin<T> cloneMe() {
    return new ImageConverterPlugin<>();
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
    return this.orderParameters(this.getDefaultParameters());
  }

  @Override
  protected List<PluginParameter> orderParameters(Map<String, PluginParameter> params) {
    return new ArrayList<>(this.getDefaultParameters().values());
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

  /**
   * Get the list of file extensions that should be excluded from conversion.
   * These are formats that are known to cause issues or are not supported.
   * Currently only used for testing purposes - not implemented in main conversion
   * logic.
   * 
   * @return List of excluded file extensions
   */
  public List<String> getExcludedExtensions() {
    String excludedExtensions = RodaCoreFactory.getRodaConfigurationAsString("core", "tools", "image-converter",
        "excludedExtensions");
    if (excludedExtensions == null || excludedExtensions.trim().isEmpty()) {
      return new ArrayList<>();
    }
    return Arrays.asList(excludedExtensions.split("\\s+"));
  }

  @Override
  public String executePlugin(Path inputPath, Path outputPath, String fileFormat) throws CommandException {
    LOGGER.info("Starting image conversion: {} -> {} ({})", inputPath, outputPath, fileFormat);

    final String outputFormat = super.getOutputFormat();
    if (outputFormat == null || outputFormat.trim().isEmpty()) {
      throw new CommandException("Output format was not set correctly in the plugin parameters.");
    }

    LOGGER.info("Executing image conversion: {} -> {} (Output Format: {})", inputPath, outputPath, outputFormat);

    ImageConverter.convert(inputPath, outputPath, outputFormat);

    LOGGER.info("Successfully converted image to {}", outputPath);

    return outputPath.toString();
  }
}
