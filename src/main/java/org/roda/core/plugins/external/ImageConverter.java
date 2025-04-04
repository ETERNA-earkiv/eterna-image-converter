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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import org.roda.core.RodaCoreFactory;
import org.roda.core.common.FileFormatUtils;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.common.RodaConstants.PreservationEventType;
import org.roda.core.data.v2.IsRODAObject;
import org.roda.core.data.v2.jobs.PluginParameter;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.base.conversion.AbstractConvertPlugin;
import org.roda.core.storage.StorageService;
import org.roda.core.util.CommandException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Plugin for converting image formats using TwelveMonkeys ImageIO
 */
@SuppressWarnings("deprecation")
public class ImageConverter<T extends IsRODAObject> extends AbstractConvertPlugin<T> {

  private static final Logger LOGGER = LoggerFactory.getLogger(ImageConverter.class);

  private static Map<String, PluginParameter> pluginParameters = new HashMap<>();

  static {

  }

  public ImageConverter() {
    super();
  }

  @Override
  public void init() throws PluginException {
    // Ensure ImageIO plugins are registered
    ImageIO.scanForPlugins();
  }

  @Override
  public String getName() {
    // Get from pom.xml <name>
    return getClass().getPackage().getImplementationTitle();
  }

  @Override
  public String getDescription() {
    return "Converts images from one format to another using TwelveMonkeys ImageIO";
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

  // @Override
  // protected List<PluginParameter> orderParameters(Map<String, PluginParameter> params) {
  //   List<PluginParameter> orderedList = new ArrayList<>();
  //   if
  //   orderedList.add(params.get(RodaConstants.PLUGIN_PARAMS_INPUT_FORMAT));
  //   orderedList.add(params.get(RodaConstants.PLUGIN_PARAMS_OUTPUT_FORMAT));
  //   if (params.containsKey(RodaConstants.PLUGIN_PARAMS_REPRESENTATION_OR_DIP)) {
  //     orderedList.add(params.get(RodaConstants.PLUGIN_PARAMS_REPRESENTATION_OR_DIP));
  //   }
  //   if (params.containsKey(RodaConstants.PLUGIN_PARAMS_DISSEMINATION_TITLE)) {
  //     orderedList.add(params.get(RodaConstants.PLUGIN_PARAMS_DISSEMINATION_TITLE));
  //   }
  //   if (params.containsKey(RodaConstants.PLUGIN_PARAMS_DISSEMINATION_DESCRIPTION)) {
  //     orderedList.add(params.get(RodaConstants.PLUGIN_PARAMS_DISSEMINATION_DESCRIPTION));
  //   }
  //   if (params.containsKey(RodaConstants.PLUGIN_PARAMS_REPRESENTATION_TYPE)) {
  //     orderedList.add(params.get(RodaConstants.PLUGIN_PARAMS_REPRESENTATION_TYPE));
  //   }
    
  //   return orderedList;
  // }

  // @Override
  // public List<PluginParameter> getParameters() {
  //   Map<String, PluginParameter> parameters = getDefaultParameters();
  //   return orderParameters(parameters);
  // }
  @Override
  protected Map<String, PluginParameter> getDefaultParameters() {
    Map<String, PluginParameter> defaultParameters = super.getDefaultParameters();
    defaultParameters.putAll(pluginParameters.entrySet().stream()
      .collect(Collectors.toMap(Map.Entry::getKey, e -> new PluginParameter(e.getValue()))));
    return defaultParameters;
  }

  @Override
  protected List<PluginParameter> orderParameters(Map<String, PluginParameter> params) {
    List<PluginParameter> orderedList = super.orderParameters(params);
    return orderedList;
  }

  @Override
  public List<PluginParameter> getParameters() {
    return this.orderParameters(this.getDefaultParameters());
  }

  @Override
  public boolean areParameterValuesValid() {
    return true;
  }

  @Override
  public Report beforeAllExecute(IndexService index, ModelService model, StorageService storage) {
    return null;
  }

  @Override
  public Report afterAllExecute(IndexService index, ModelService model, StorageService storage) {
    return null;
  }

  @Override
  public List<String> getApplicableTo() {
    return FileFormatUtils.getInputExtensions("image-converter");
  }

  @Override
  public List<String> getConvertableTo() {
    String outputFormats = RodaCoreFactory.getRodaConfigurationAsString("core", "tools", "image-converter", "outputFormats");
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

    System.out.println("inputPath: " + inputPath);
    System.out.println("outputPath: " + outputPath);
    System.out.println("fileFormat: " + fileFormat);

    BufferedImage image = null;
    boolean success = false;
    try {
      image = ImageIO.read(inputPath.toFile());

      if (image == null) {
        throw new IOException("Could not read input image file: " + inputPath + ". Format might be unsupported or file is corrupted.");
      }
      success = ImageIO.write(image, fileFormat, outputPath.toFile());

      if (!success) {
        throw new IOException("Could not write output image file: " + outputPath + ". Format '" + fileFormat + "' might be unsupported by available writers.");
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
}
