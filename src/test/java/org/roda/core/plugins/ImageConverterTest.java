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
import org.apache.commons.io.FilenameUtils;
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
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.core.data.v2.ip.Permissions;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
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

import jodd.net.MimeTypes;

@Test(groups = { RodaConstants.TEST_GROUP_ALL, RodaConstants.TEST_GROUP_TRAVIS })
public class ImageConverterTest {
	private static final Logger LOGGER = LoggerFactory.getLogger(ImageConverterTest.class);

	private static Path basePath;
	private static ModelService model;
	private static IndexService index;

	private static StorageService corporaService;

	private static Path corporaPath;

	private Path tmpDir;
	private int sampleCount;
	private AIP aip;
	private Representation rep;

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

		// Copy all files from the Media directory using FileUtils
		Path mediaPath = corporaPath.resolve("Media");
		FileUtils.copyDirectory(mediaPath.toFile(), tmpDir.toFile());

		// Count the files in the temporary directory
		sampleCount = tmpDir.toFile().listFiles().length;

		aip = model.createAIP(
				null,
				RodaConstants.AIP_TYPE_MIXED,
				new Permissions(),
				RodaConstants.ADMIN);

		final String repId = IdUtils.createUUID();
		rep = model.createRepresentation(aip.getId(), repId, true,
				RodaConstants.AIP_TYPE_MIXED, true, RodaConstants.ADMIN);
		index.commitAIPs();

		for (File file : tmpDir.toFile().listFiles()) {
			ContentPayload payload = new FSPathContentPayload(file.toPath().toAbsolutePath());
			model.createFile(aip.getId(), repId,
					List.of(FilenameUtils.getExtension(file.getName())),
					file.getName(), payload,
					RodaConstants.ADMIN);
		}

		index.commitAIPs();

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
		// 1. Check AIP exists
		Filter aipFilter = new Filter();
		aipFilter.add(new SimpleFilterParameter("id", aip.getId()));
		IndexResult<IndexedAIP> aips = index.find(IndexedAIP.class, aipFilter, null, new Sublist(0, 1),
				Collections.emptyList());
		Assert.assertEquals(aips.getResults().size(), 1, "AIP should exist in the index");

		// 2. Check Representation exists
		Filter repFilter = new Filter();
		repFilter.add(new SimpleFilterParameter(RodaConstants.REPRESENTATION_AIP_ID,
				aip.getId()));
		repFilter.add(new SimpleFilterParameter(RodaConstants.REPRESENTATION_ID,
				rep.getId()));
		IndexResult<IndexedRepresentation> reps = index.find(IndexedRepresentation.class, repFilter, null,
				new Sublist(0, 10), List.of("id", "uuid", "aipId"));
		IndexedRepresentation repId2 = reps.getResults().get(0);
		Assert.assertEquals(reps.getResults().size(), 1, "Representation should exist in the index");

		// 3. Check all files are present in the Representation
		Filter repFilesFilter = new Filter();
		repFilesFilter.add(new SimpleFilterParameter(RodaConstants.FILE_AIP_ID, aip.getId()));
		repFilesFilter.add(new SimpleFilterParameter(RodaConstants.FILE_REPRESENTATION_ID, rep.getId()));
		repFilesFilter.add(new SimpleFilterParameter("isDirectory", "false"));
		IndexResult<IndexedFile> repFiles = index.find(
				IndexedFile.class, repFilesFilter, null,
				new Sublist(0, sampleCount + 10), List.of("id", "uuid", "originalName", "fileFormat", "extension"));
		Assert.assertEquals(repFiles.getResults().size(), sampleCount,
				"Should find all sample files in the representation");

		// TODO: build structure or clean up after each test
		String[] formatsToTest = new String[] { "jpg", "png", "tiff" };

		for (String format : formatsToTest) {

			List<String> fileIds = repFiles.getResults().stream()
					.filter(f -> !f.getFileFormat().getExtension().equals(format))
					.filter(f -> !f.getFileFormat().getExtension().equals("cur"))
					.filter(f -> !f.getFileFormat().getExtension().equals("pict"))
					.filter(f -> !f.getFileFormat().getExtension().equals("ico"))
					.filter(f -> !f.getFileFormat().getExtension().equals("dds"))
					.filter(f -> !f.getFileFormat().getExtension().equals("pfm"))
					.filter(f -> !f.getFileFormat().getExtension().equals("hdr")) // HDR bit
					.map(f -> f.getUUID()).toList();

			SelectedItemsList<IndexedFile> files = SelectedItemsList.create(IndexedFile.class, fileIds);

			// Prepare parameters
			Map<String, String> parameters = new HashMap<>();
			parameters.put(RodaConstants.PLUGIN_PARAMS_REPRESENTATION_OR_DIP,
					"type=rep;value=mixed;markAsPreservation=true");
			parameters.put(RodaConstants.PLUGIN_PARAMS_CONVERSION_PROFILE, format);

			// Run ImageConverter plugin
			Job job = TestsHelper.executeJob(ImageConverter.class, parameters,
					PluginType.AIP_TO_AIP,
					files);

			index.commitAIPs();

			Assert.assertEquals(job.getJobStats().getCompletionPercentage(), 100,
					"ImageConverter job did not complete");
			Assert.assertEquals(job.getJobStats().getSourceObjectsProcessedWithSuccess(),
					sampleCount - 7, "Should process all files");

			// ## to here, below todo ##
			// Filter filtAIP = new Filter();
			// filtAIP.add(new AllFilterParameter());

			// // 6. Check converted representation
			// Filter filterParentTheAIP = new Filter();
			// filterParentTheAIP.add(new
			// SimpleFilterParameter(RodaConstants.REPRESENTATION_STATES, "PRESERVATION"));
			// // filterParentTheAIP.add(new AllFilterParameter());
			// IndexResult<IndexedRepresentation> indexResult2 =
			// index.find(IndexedRepresentation.class,
			// filterParentTheAIP,
			// null, new Sublist(0, 10),
			// List.of(RodaConstants.REPRESENTATION_ID,
			// RodaConstants.REPRESENTATION_STATES));
			// Assert.assertEquals(indexResult2.getResults().size(), 1, "Should have ONE
			// conversion representation");
			// IndexedRepresentation conversionRep = indexResult2.getResults().get(0);

			// // 6a. Get ref for converted file
			// Filter filterFile = new Filter();
			// filterFile.add(new SimpleFilterParameter(RodaConstants.FILE_AIP_ID,
			// aip.getId()));
			// filterFile.add(new
			// SimpleFilterParameter(RodaConstants.FILE_REPRESENTATION_ID,
			// conversionRep.getId()));
			// IndexResult<org.roda.core.data.v2.ip.IndexedFile> convfiles = index.find(
			// org.roda.core.data.v2.ip.IndexedFile.class, filterFile, null,
			// new Sublist(0, 10), List.of("id", "uuid", "originalName"));
			// Assert.assertEquals(convfiles.getResults().size(), 1, "Should have one
			// converted file");
			// String convFileId = convfiles.getResults().get(0).getUUID();

			// // 7. Run Siegfried on just the converted file
			// // Map<String, String> siegfriedParams = new HashMap<>();
			// // siegfriedParams.put(RodaConstants.PLUGIN_PARAMS_REPRESENTATION_OR_DIP,
			// // "false");
			// Job siegfriedJob = TestsHelper.executeJob(SiegfriedPlugin.class,
			// Collections.emptyMap(),
			// PluginType.MISC,
			// SelectedItemsList.create(org.roda.core.data.v2.ip.File.class, convFileId));
			// List<Report> siegfriedReport = TestsHelper.getJobReports(index, siegfriedJob,
			// true);
			// Assert.assertEquals(siegfriedReport.size(), 1, "Should have one siegfried
			// report");
			// Assert.assertEquals(siegfriedJob.getJobStats().getCompletionPercentage(),
			// 100,
			// "Siegfried job did not complete");
			// Assert.assertEquals(siegfriedJob.getJobStats().getSourceObjectsProcessedWithSuccess(),
			// 1,
			// "Siegfried should process one file");

			// index.commitAIPs();

			// // 8. Check that the converted file exists and has correct format
			// Filter convFileFilter = new Filter();
			// convFileFilter.add(new SimpleFilterParameter(RodaConstants.FILE_AIP_ID,
			// aip.getId()));
			// IndexResult<org.roda.core.data.v2.ip.IndexedFile> convFiles = index.find(
			// org.roda.core.data.v2.ip.IndexedFile.class, convFileFilter, null,
			// new Sublist(0, 10),
			// List.of("id", "uuid", "originalName", "fileFormat", "formatMimetype",
			// "extension"));
			// Optional<org.roda.core.data.v2.ip.IndexedFile> convFile =
			// convFiles.getResults().stream()
			// .filter(f -> f.getId().toLowerCase().endsWith("." + format))
			// .findFirst();
			// Assert.assertTrue(convFile.isPresent(), "Converted file not found");

			// index.commitAIPs();

			// String formatFromSiegfried = convFile.get().getFileFormat().getMimeType();

			// String mimeType = MimeTypes.lookupMimeType(format);
			// Assert.assertNotNull(formatFromSiegfried, "File format metadata is missing");
			// Assert.assertTrue(formatFromSiegfried.toLowerCase().contains(mimeType),
			// "File format should be " + mimeType + " but was: " + formatFromSiegfried);
		}
	}

}
