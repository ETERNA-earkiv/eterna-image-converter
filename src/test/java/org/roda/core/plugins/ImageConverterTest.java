/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE.md file at the root of the source
 * tree and available online at
 * <p>
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.io.FileUtils;
import org.roda.core.CorporaConstants;
import org.roda.core.RodaCoreFactory;
import org.roda.core.TestsHelper;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.filter.AllFilterParameter;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.index.select.SelectedItemsAll;
import org.roda.core.data.v2.index.select.SelectedItemsList;
import org.roda.core.data.v2.index.sublist.Sublist;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.core.data.v2.ip.Permissions;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.index.IndexService;
import org.roda.core.index.IndexTestUtils;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.base.characterization.SiegfriedPlugin;
import org.roda.core.plugins.external.ImageConverter;
import org.roda.core.storage.ContentPayload;
import org.roda.core.storage.DefaultStoragePath;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.fs.FSPathContentPayload;
import org.roda.core.storage.fs.FSUtils;
import org.roda.core.storage.fs.FileStorageService;
import org.roda.core.util.IdUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test(groups = { RodaConstants.TEST_GROUP_ALL, RodaConstants.TEST_GROUP_TRAVIS })
public class ImageConverterTest {
  private static final Logger LOGGER = LoggerFactory.getLogger(ImageConverterTest.class);

  private static Path basePath;
  private static ModelService model;
  private static IndexService index;

  private static StorageService corporaService;

  private static Path corporaPath;

  private Path tmpDir;

  @BeforeMethod
  public void setUp() throws Exception {

    basePath = TestsHelper.createBaseTempDir(this.getClass(), true);

    boolean deploySolr = true;
    boolean deployLdap = true;
    boolean deployFolderMonitor = true;
    boolean deployOrchestrator = true;
    boolean deployPluginManager = true;
    boolean deployDefaultResources = false;
    RodaCoreFactory.instantiateTest(deploySolr, deployLdap, deployFolderMonitor, deployOrchestrator,
    deployPluginManager, deployDefaultResources);
    model = RodaCoreFactory.getModelService();
    index = RodaCoreFactory.getIndexService();
    
    RodaCoreFactory.addConfiguration("image-converter.properties");
    RodaCoreFactory.getPluginManager().registerPlugin(new ImageConverter());

    URL corporaURL = ImageConverterTest.class.getResource("/corpora");
    corporaService = new FileStorageService(Paths.get(corporaURL.toURI()));
    corporaPath = Paths.get(corporaURL.toURI());
    FileUtils.deleteDirectory(new File("/tmp/test"));
    tmpDir = Files.createDirectory(Paths.get("/tmp/test"));
    Files.copy(corporaPath.resolve("Media").resolve("sample_640x426.tiff"),
        tmpDir.resolve("sample_640x426.tiff"));

    LOGGER.info("Running ImageConverter Plugin tests under storage {}", basePath);
  }

  @AfterMethod
  public void tearDown() throws Exception {
    IndexTestUtils.resetIndex();
    RodaCoreFactory.shutdown();
    FSUtils.deletePathQuietly(basePath);
  }

  @AfterMethod
  public void cleanUp() throws RODAException {
    try {
      Files.deleteIfExists(tmpDir);
    } catch (IOException e) {
      // do nothing.
    }
  }

  @Test
  public void testImageConverterPluginOnFile() throws RODAException,
      IOException {
    final String repId = IdUtils.createUUID();
    // 1. Create AIP using corporaService
    AIP aip = model.createAIP(
        null,
        RodaConstants.AIP_TYPE_MIXED,
        new Permissions(),
        RodaConstants.ADMIN);

    // Create Representation
    Representation rep = model.createRepresentation(aip.getId(), repId, true,
        RodaConstants.AIP_TYPE_MIXED, false, RodaConstants.ADMIN);

    // 2. Create a file in the representation
    Path path = tmpDir.resolve("sample_640x426.tiff");
    ContentPayload payload = new FSPathContentPayload(path.toAbsolutePath());
    org.roda.core.data.v2.ip.File file = model.createFile(aip.getId(), repId,
        Collections.emptyList(),
        "sample_640x426.tiff", payload,
        RodaConstants.ADMIN);

    index.commitAIPs();

    // 4. Filter by originalName to find the test file
    Filter fileFilter = new Filter();
    fileFilter.add(new SimpleFilterParameter(RodaConstants.FILE_AIP_ID,
        aip.getId()));
    fileFilter.add(new SimpleFilterParameter("id",
        "sample_640x426.tiff"));
    IndexResult<org.roda.core.data.v2.ip.IndexedFile> files = index.find(
        org.roda.core.data.v2.ip.IndexedFile.class, fileFilter, null,
        new Sublist(0, 10), List.of("id", "uuid", "originalName", "fileFormat"));
    Assert.assertEquals(files.getResults().size(), 1, "Should find exactly one test file");
    String fileId = files.getResults().get(0).getUUID();

    // 4. Prepare parameters for initial conversion to JPG
    Map<String, String> parameters = new HashMap<>();
    parameters.put(RodaConstants.PLUGIN_PARAMS_REPRESENTATION_OR_DIP,
        "type=rep;value=mixed;markAsPreservation=true");
    parameters.put(RodaConstants.PLUGIN_PARAMS_CONVERSION_PROFILE, "jpg");

    // 5. Run ImageConverter plugin (to JPG) on just the test file
    Job job = TestsHelper.executeJob(ImageConverter.class, parameters,
        PluginType.AIP_TO_AIP,
        SelectedItemsList.create(org.roda.core.data.v2.ip.File.class, fileId));
    index.commitAIPs();

    Assert.assertEquals(job.getJobStats().getCompletionPercentage(), 100,
        "ImageConverter job did not complete");
    Assert.assertEquals(job.getJobStats().getSourceObjectsProcessedWithSuccess(),
        1, "Should process one file");

    Filter filtAIP = new Filter();
    // filterParentTheAIP.add(new
    // SimpleFilterParameter(RodaConstants.REPRESENTATION_AIP_ID, aip.getId()));
    filtAIP.add(new AllFilterParameter());
    IndexResult<IndexedFile> indexResult = index.find(IndexedFile.class, filtAIP,
        null, new Sublist(0, 10), Collections.emptyList());

    // 6. Check converted representation
    Filter filterParentTheAIP = new Filter();
    filterParentTheAIP.add(new SimpleFilterParameter(RodaConstants.REPRESENTATION_STATES, "PRESERVATION"));
    filterParentTheAIP.add(new AllFilterParameter());
    IndexResult<IndexedRepresentation> indexResult2 = index.find(IndexedRepresentation.class, filterParentTheAIP,
        null, new Sublist(0, 10), List.of(RodaConstants.REPRESENTATION_STATES));
    Assert.assertEquals(indexResult2.getResults().size(), 1, "Should have ONE conversion representation");
    IndexedRepresentation conversionRep = indexResult2.getResults().get(0);

    // 6a. Get ref for converted file
    Filter filterFile = new Filter();
    filterFile.add(new SimpleFilterParameter(RodaConstants.FILE_AIP_ID,
        aip.getId()));
    filterFile.add(new SimpleFilterParameter(RodaConstants.FILE_REPRESENTATION_ID,
        conversionRep.getId()));
    IndexResult<org.roda.core.data.v2.ip.IndexedFile> convfiles = index.find(
        org.roda.core.data.v2.ip.IndexedFile.class, filterFile, null,
        new Sublist(0, 10), List.of("id", "uuid", "originalName"));
    Assert.assertEquals(convfiles.getResults().size(), 1, "Should have one converted file");
    String convFileId = convfiles.getResults().get(0).getUUID();

    // 7. Run Siegfried on just the converted file
    Map<String, String> siegfriedParams = new HashMap<>();
    siegfriedParams.put(RodaConstants.PLUGIN_PARAMS_REPRESENTATION_OR_DIP,
        "false");
    Job siegfriedJob = TestsHelper.executeJob(SiegfriedPlugin.class,
        siegfriedParams,
        PluginType.AIP_TO_AIP,
        SelectedItemsList.create(org.roda.core.data.v2.ip.File.class, convFileId));
    Assert.assertEquals(siegfriedJob.getJobStats().getCompletionPercentage(),
        100,
        "Siegfried job did not complete");
    Assert.assertEquals(siegfriedJob.getJobStats().getSourceObjectsProcessedWithSuccess(),
        1,
        "Siegfried should process one file");

    // 8. Check that the converted file exists and has correct format
    Filter convFileFilter = new Filter();
    convFileFilter.add(new SimpleFilterParameter(RodaConstants.FILE_AIP_ID,
        aip.getId()));
    IndexResult<org.roda.core.data.v2.ip.IndexedFile> convFiles = index.find(
        org.roda.core.data.v2.ip.IndexedFile.class, convFileFilter, null,
        new Sublist(0, 10), List.of("id", "uuid", "originalName", "fileFormat"));
    Optional<org.roda.core.data.v2.ip.IndexedFile> convFile = convFiles.getResults().stream()
        .filter(f -> f.getId().toLowerCase().endsWith(".jpg"))
        .findFirst();
    Assert.assertTrue(convFile.isPresent(), "Converted JPG file not found");

    String formatFromSiegfried = convFile.get().getFileFormat().getMimeType();

    Assert.assertNotNull(formatFromSiegfried, "File format metadata is missing");
    Assert.assertTrue(formatFromSiegfried.toLowerCase().contains("jpeg"),
        "File format should be JPEG but was: " + formatFromSiegfried);

    // 9. Test conversion to other formats (PNG and TIFF)
    String[] formatsToTest = new String[] { "png", "tiff" };
    for (String format : formatsToTest) {
      parameters.put(RodaConstants.PLUGIN_PARAMS_OUTPUT_FORMAT, format);
      Job formatJob = TestsHelper.executeJob(ImageConverter.class, parameters,
          PluginType.AIP_TO_AIP,
          SelectedItemsList.create(org.roda.core.data.v2.ip.File.class, convFileId));
      Assert.assertEquals(formatJob.getJobStats().getCompletionPercentage(), 100,
          "Conversion to " + format + " failed");
      Assert.assertEquals(formatJob.getJobStats().getSourceObjectsProcessedWithSuccess(),
          1,
          "Should process one file for " + format);

      index.commitAIPs();
      IndexResult<org.roda.core.data.v2.ip.IndexedFile> formatFiles = index.find(
          org.roda.core.data.v2.ip.IndexedFile.class, convFileFilter, null,
          new Sublist(0, 10), Collections.emptyList());
      Optional<org.roda.core.data.v2.ip.IndexedFile> formatFile = formatFiles.getResults().stream()
          .filter(f -> f.getId().toLowerCase().endsWith("." + format))
          .findFirst();
      Assert.assertTrue(formatFile.isPresent(),
          "Converted " + format + " file not found");

      // Run Siegfried again on the new file
      Job formatSiegfriedJob = TestsHelper.executeJob(SiegfriedPlugin.class,
          siegfriedParams,
          PluginType.AIP_TO_AIP,
          SelectedItemsList.create(org.roda.core.data.v2.ip.File.class,
              formatFile.get().getId()));
      Assert.assertEquals(formatSiegfriedJob.getJobStats().getCompletionPercentage(),
          100);

      String formatFromSiegfried2 = formatFile.get().getFileFormat().toString();
      Assert.assertNotNull(formatFromSiegfried2, "File format metadata is missing for " + format);
      Assert.assertTrue(formatFromSiegfried2.toLowerCase().contains(format),
          "File format should be " + format.toUpperCase() + " but was: " +
              formatFromSiegfried2);
    }
  }

}
