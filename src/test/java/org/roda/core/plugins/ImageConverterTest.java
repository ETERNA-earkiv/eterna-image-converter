/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE.md file at the root of the source
 * tree and available online at
 * <p>
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.roda.core.CorporaConstants;
import org.roda.core.RodaCoreFactory;
import org.roda.core.TestsHelper;
import org.roda.core.common.monitor.TransferredResourcesScanner;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.IsStillUpdatingException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.index.select.SelectedItemsAll;
import org.roda.core.data.v2.index.select.SelectedItemsList;
import org.roda.core.data.v2.index.sublist.Sublist;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.core.data.v2.ip.Permissions;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.index.IndexService;
import org.roda.core.index.IndexTestUtils;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.base.AbstractConvertPluginDummy;
import org.roda.core.plugins.base.characterization.SiegfriedPlugin;
import org.roda.core.plugins.base.ingest.TransferredResourceToAIPPlugin;
import org.roda.core.plugins.external.ImageConverter;
import org.roda.core.storage.DefaultStoragePath;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.fs.FSUtils;
import org.roda.core.storage.fs.FileStorageService;
import org.roda.core.util.IdUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.AssertJUnit;
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

    URL corporaURL = getClass().getResource("/corpora");
    corporaService = new FileStorageService(Paths.get(corporaURL.toURI()));
    corporaPath = Paths.get(corporaURL.toURI());
    FileUtils.deleteDirectory(new File("/tmp/test"));
    tmpDir = Files.createDirectory(Paths.get("/tmp/test"));
    Files.copy(corporaPath.resolve("sample_files").resolve("sample_640x426.tiff"),
        tmpDir.resolve("sample_640x426.tiff"));
    // skapa aip,rep -> få in ex filer i rep

    LOGGER.info("Running internal convert plugins tests under storage {}", basePath);
  }

  @AfterMethod
  public void tearDown() throws Exception {
    // ta bort allt skapat
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

  // public List<TransferredResource> createCorpora() throws NotFoundException,
  // GenericException, AlreadyExistsException,
  // IsStillUpdatingException, AuthorizationDeniedException {
  // TransferredResourcesScanner f =
  // RodaCoreFactory.getTransferredResourcesScanner();

  // List<TransferredResource> resources = new ArrayList<>();

  // Path corpora =
  // corporaPath.resolve(RodaConstants.STORAGE_CONTAINER_AIP).resolve("AIP_1")
  // .resolve(RodaConstants.STORAGE_DIRECTORY_REPRESENTATIONS).resolve(CorporaConstants.REPRESENTATION_CONVERTER_ID_2)
  // .resolve(RodaConstants.STORAGE_DIRECTORY_DATA);

  // String transferredResourceId = "testt";
  // FSUtils.copy(corpora, f.getBasePath().resolve(transferredResourceId), true);

  // f.updateTransferredResources(Optional.empty(), true);
  // index.commit(TransferredResource.class);

  // resources
  // .add(index.retrieve(TransferredResource.class,
  // IdUtils.createUUID(transferredResourceId), new ArrayList<>()));
  // return resources;
  // }

  // public AIP ingestCorpora() throws RequestNotValidException,
  // NotFoundException, GenericException,
  // AlreadyExistsException, AuthorizationDeniedException,
  // IsStillUpdatingException {
  // String parentId = null;
  // String aipType = RodaConstants.AIP_TYPE_MIXED;
  // AIP root = model.createAIP(parentId, aipType, new Permissions(),
  // RodaConstants.ADMIN);

  // Map<String, String> parameters = new HashMap<>();
  // parameters.put(RodaConstants.PLUGIN_PARAMS_PARENT_ID, root.getId());

  // List<TransferredResource> transferredResources;
  // transferredResources = createCorpora();

  // AssertJUnit.assertEquals(1, transferredResources.size());

  // Job job = TestsHelper.executeJob(TransferredResourceToAIPPlugin.class,
  // parameters, PluginType.SIP_TO_AIP,
  // SelectedItemsList.create(TransferredResource.class,
  // transferredResources.stream().map(tr ->
  // tr.getUUID()).collect(Collectors.toList())));

  // TestsHelper.getJobReports(index, job, true);

  // index.commitAIPs();

  // IndexResult<IndexedAIP> find = index.find(IndexedAIP.class,
  // new Filter(new SimpleFilterParameter(RodaConstants.AIP_PARENT_ID,
  // root.getId())), null, new Sublist(0, 10),
  // new ArrayList<>());

  // AssertJUnit.assertEquals(1L, find.getTotalCount());
  // IndexedAIP indexedAIP = find.getResults().get(0);

  // return model.retrieveAIP(indexedAIP.getId());
  // }

  @Test
  public void testImageConverterPluginOnFile() throws RODAException {
    // - köra imageConvert jobbet
    // - köra fileidentifer(siegfried) på nya filen
    // - läsa rapporten från fileidentifier =? lyckat
    // - hämta nya filen, kontrollera metadata fält för filtyp = förväntat filtyp
    // vald outputformat
    // - testa mot filformat mot varje outputformat

    final String aipId = IdUtils.createUUID();

    model.createAIP(aipId, corporaService,
        DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_ID_EARK2S),
        RodaConstants.ADMIN);

    index.commitAIPs();
    final Map<String, String> parameters = new HashMap<>();
    parameters.put(RodaConstants.PLUGIN_PARAMS_REPRESENTATION_OR_DIP, "false");
    parameters.put(RodaConstants.PLUGIN_PARAMS_OUTPUT_FORMAT, "jpg");

    final Job job = TestsHelper.executeJob(ImageConverter.class, parameters, PluginType.AIP_TO_AIP,
        SelectedItemsAll.create(org.roda.core.data.v2.ip.File.class));

    index.commitAIPs();

    final Filter filterParentTheAIP = new Filter();
    filterParentTheAIP.add(new SimpleFilterParameter(RodaConstants.REPRESENTATION_AIP_ID, aipId));
    final IndexResult<IndexedRepresentation> indexResult = index.find(IndexedRepresentation.class, filterParentTheAIP,
        null, new Sublist(0, 10), Collections.emptyList());

    Assert.assertEquals(job.getJobStats().getCompletionPercentage(), 100);
    Assert.assertEquals(job.getJobStats().getSourceObjectsProcessedWithSuccess(), 1);
    Assert.assertEquals(indexResult.getResults().size(), 2);

    Map<String, String> siegfriedParams = new HashMap<>();
    siegfriedParams.put(RodaConstants.PLUGIN_PARAMS_REPRESENTATION_OR_DIP, "false");
    Job siegfriedJob = TestsHelper.executeJob(SiegfriedPlugin.class, siegfriedParams,
        PluginType.AIP_TO_AIP, SelectedItemsAll.create(org.roda.core.data.v2.ip.File.class));

    Assert.assertEquals(siegfriedJob.getJobStats().getCompletionPercentage(), 100);

    Filter fileFilter = new Filter();
    fileFilter.add(new SimpleFilterParameter(RodaConstants.FILE_AIP_ID, aipId));
    IndexResult<org.roda.core.data.v2.ip.IndexedFile> files = index.find(
        org.roda.core.data.v2.ip.IndexedFile.class, fileFilter, null,
        new Sublist(0, 10), Collections.emptyList());

    Optional<org.roda.core.data.v2.ip.IndexedFile> convertedFile = files.getResults().stream()
        .filter(f -> f.getId().toLowerCase().endsWith(".jpg"))
        .findFirst();

    Assert.assertTrue(convertedFile.isPresent());

    // Verify file format metadata from siegfried
    String formatFromSiegfried = convertedFile.get().getFileFormat().toString();
    Assert.assertNotNull(formatFromSiegfried, "File format metadata is missing");
    Assert.assertTrue(formatFromSiegfried.toLowerCase().contains("jpeg"),
        "File format should be JPEG but was: " + formatFromSiegfried);

    // Test conversion to other formats (PNG and TIFF)
    String[] formatsToTest = new String[] { "png", "tiff" };
    for (String format : formatsToTest) {
      parameters.put(RodaConstants.PLUGIN_PARAMS_OUTPUT_FORMAT, format);
      Job formatJob = TestsHelper.executeJob(ImageConverter.class, parameters,
          PluginType.AIP_TO_AIP, SelectedItemsAll.create(org.roda.core.data.v2.ip.File.class));

      Assert.assertEquals(formatJob.getJobStats().getCompletionPercentage(), 100,
          "Conversion to " + format + " failed");

      // Verify the converted file exists with correct format
      index.commitAIPs();
      IndexResult<org.roda.core.data.v2.ip.IndexedFile> formatFiles = index.find(
          org.roda.core.data.v2.ip.IndexedFile.class, fileFilter, null,
          new Sublist(0, 10), Collections.emptyList());

      Optional<org.roda.core.data.v2.ip.IndexedFile> formatFile = formatFiles.getResults().stream()
          .filter(f -> f.getId().toLowerCase().endsWith("." + format))
          .findFirst();

      Assert.assertTrue(formatFile.isPresent(),
          "Converted " + format + " file not found");
    }

    // AIP aip = ingestCorpora();

    // Map<String, String> parameters = new HashMap<>();
    // TestsHelper.executeJob(ExamplePlugin.class, parameters,
    // PluginType.AIP_TO_AIP, SelectedItemsAll.create(AIP.class));

    // AIP aip2 = model.retrieveAIP(aip.getId());
    // Assert.assertEquals(aip2.getRepresentations().size(),
    // aip.getRepresentations().size());

  }

}
