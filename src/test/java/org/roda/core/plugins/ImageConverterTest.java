/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE.md file at the root of the source
 * tree
 */
package org.roda.core.plugins;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.roda.core.RodaCoreFactory;
import org.roda.core.TestsHelper;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
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
import org.roda.core.index.IndexService;
import org.roda.core.index.IndexTestUtils;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.base.characterization.SiegfriedPlugin;
import org.roda.core.plugins.external.ImageConverter;
import org.roda.core.storage.ContentPayload;
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

	@SuppressWarnings("unused")
	private static StorageService corporaService;

	private static Path corporaPath;

	private Path tmpDir;
	private int sampleCount;
	private AIP aip;
	private Representation rep;
	private ImageConverter<IndexedFile> imageConverter;
	private List<String> formatsToTest;
	// Extensions that should be excluded from conversion (e.g., unsupported
	// formats)
	private List<String> baseExcludedExtensions;

	@SuppressWarnings({ "unchecked", "rawtypes" })
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

		imageConverter = new ImageConverter<>();
		formatsToTest = imageConverter.getConvertableTo();
		baseExcludedExtensions = imageConverter.getExcludedExtensions();

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
		// Check AIP exists
		Filter filterIndexedAIP = new Filter();
		filterIndexedAIP.add(new SimpleFilterParameter(RodaConstants.AIP_ID, aip.getId()));
		IndexResult<IndexedAIP> indexedAIPResult = index.find(IndexedAIP.class,
				filterIndexedAIP, null, new Sublist(0, 100),
				List.of(RodaConstants.AIP_ID, "uuid"));
		Assert.assertEquals(indexedAIPResult.getResults().size(), 1, "Should have 1 indexed AIP");
		IndexedAIP indexedAIP = indexedAIPResult.getResults().get(0);

		// Check Representation exists
		Filter repFilter = new Filter();
		repFilter.add(new SimpleFilterParameter(RodaConstants.REPRESENTATION_AIP_ID,
				indexedAIP.getUUID()));
		repFilter.add(new SimpleFilterParameter(RodaConstants.REPRESENTATION_ID,
				rep.getId()));
		IndexResult<IndexedRepresentation> reps = index.find(IndexedRepresentation.class, repFilter, null,
				new Sublist(0, 10), List.of("id", "uuid", "aipId"));
		Assert.assertEquals(reps.getResults().size(), 1, "Should have 1 indexed representation");

		// Check all sample files are present in the Representation
		Filter repFilesFilter = new Filter();
		repFilesFilter.add(new SimpleFilterParameter(RodaConstants.FILE_AIP_ID, indexedAIP.getUUID()));
		repFilesFilter.add(new SimpleFilterParameter(RodaConstants.FILE_REPRESENTATION_ID, rep.getId()));
		repFilesFilter.add(new SimpleFilterParameter("isDirectory", "false"));
		IndexResult<IndexedFile> repFiles = index.find(
				IndexedFile.class, repFilesFilter, null,
				new Sublist(0, sampleCount + 10), List.of("id", "uuid", "originalName", "fileFormat", "extension"));
		Assert.assertEquals(repFiles.getResults().size(), sampleCount,
				"Should find all sample files in the representation");

		// Define excluded extensions once
		long baseExcludedCount = repFiles.getResults().stream()
				.filter(f -> baseExcludedExtensions.contains(f.getFileFormat().getExtension().toLowerCase()))
				.count();

		// Validate that we have files available for conversion
		Assert.assertTrue(baseExcludedCount < sampleCount,
				"All files are excluded from conversion. Check if test corpus contains only excluded formats: "
						+ baseExcludedExtensions);

		for (String format : formatsToTest) {

			List<String> allConvertedFileIds = new ArrayList<>();
			List<IndexedFile> allConvertedFiles = new ArrayList<>();

			// Calculate how many files will be excluded for this specific format
			// (base excluded + files that are already in the target format)
			long formatSpecificExcludedCount = repFiles.getResults().stream()
					.filter(f -> f.getFileFormat().getExtension().toLowerCase().equals(format.toLowerCase()))
					.count();
			long totalExcludedCount = baseExcludedCount + formatSpecificExcludedCount;

			// Skip this format if all files would be excluded
			if (totalExcludedCount >= sampleCount) {
				LOGGER.info(
						"Skipping format {} - all files would be excluded (base excluded: {}, format specific excluded: {}, total files: {})",
						format, baseExcludedCount, formatSpecificExcludedCount, sampleCount);
				continue;
			}

			List<String> fileIds = repFiles.getResults().stream()
					.filter(f -> !baseExcludedExtensions.contains(f.getFileFormat().getExtension().toLowerCase()))
					.filter(f -> !f.getFileFormat().getExtension().toLowerCase().equals(format.toLowerCase()))
					.map(f -> f.getUUID()).toList();

			SelectedItemsList<IndexedFile> files = SelectedItemsList.create(IndexedFile.class, fileIds);

			// Prepare parameters
			Map<String, String> parameters = new HashMap<>();
			parameters.put(RodaConstants.PLUGIN_PARAMS_REPRESENTATION_OR_DIP,
					"type=rep;value=mixed;markAsPreservation=true");
			parameters.put(RodaConstants.PLUGIN_PARAMS_CONVERSION_PROFILE, format);

			// Run ImageConverter plugin
			@SuppressWarnings("unchecked")
			Job job = TestsHelper.executeJob(ImageConverter.class, parameters,
					PluginType.AIP_TO_AIP,
					files);

			index.commitAIPs();

			Assert.assertEquals(job.getJobStats().getCompletionPercentage(), 100,
					"ImageConverter job did not complete");
			Assert.assertEquals(job.getJobStats().getSourceObjectsProcessedWithSuccess(),
					sampleCount - totalExcludedCount, "Should process all files");

			// Check converted representation
			Filter filterPreservationRep = new Filter();
			filterPreservationRep.add(new SimpleFilterParameter(RodaConstants.REPRESENTATION_STATES, "PRESERVATION"));
			filterPreservationRep
					.add(new SimpleFilterParameter(RodaConstants.REPRESENTATION_AIP_ID, indexedAIP.getUUID()));
			IndexResult<IndexedRepresentation> preservationReps = index.find(IndexedRepresentation.class,
					filterPreservationRep, null, new Sublist(0, 100),
					List.of(RodaConstants.REPRESENTATION_ID, RodaConstants.REPRESENTATION_STATES,
							"uuid"));

			for (IndexedRepresentation preservationRep : preservationReps.getResults()) {
				Filter convertedFilesFilter = new Filter();
				convertedFilesFilter.add(new SimpleFilterParameter(RodaConstants.FILE_AIP_ID, indexedAIP.getUUID()));
				convertedFilesFilter
						.add(new SimpleFilterParameter(RodaConstants.FILE_REPRESENTATION_ID, preservationRep.getId()));
				convertedFilesFilter.add(new SimpleFilterParameter("isDirectory", "false"));
				convertedFilesFilter.add(new SimpleFilterParameter("extension", format));
				IndexResult<IndexedFile> convertedFiles = index.find(IndexedFile.class, convertedFilesFilter, null,
						new Sublist(0, sampleCount + 10),
						List.of("id", "uuid", "originalName", "fileFormat", "formatMimetype", "extension"));

				allConvertedFiles.addAll(convertedFiles.getResults());
				allConvertedFileIds.addAll(convertedFiles.getResults().stream().map(f -> f.getUUID()).toList());
			}

			// Verify we have converted files
			Assert.assertTrue(allConvertedFiles.size() > 0, "Should have converted files");

			// Run Siegfried on converted files to populate file format metadata
			// This is necessary because the converted files need their format information
			// characterized
			@SuppressWarnings("unchecked")
			Job siegfriedJob = TestsHelper.executeJob(SiegfriedPlugin.class, Collections.emptyMap(),
					PluginType.MISC, SelectedItemsList.create(IndexedFile.class, allConvertedFileIds));

			Assert.assertEquals(siegfriedJob.getJobStats().getCompletionPercentage(), 100,
					"Siegfried job did not complete");
			Assert.assertEquals(siegfriedJob.getJobStats().getSourceObjectsProcessedWithSuccess(),
					allConvertedFileIds.size(),
					"Siegfried should process all converted files");

			index.commitAIPs();

			// Verify converted files have correct format using direct validation
			// This is more efficient than running Siegfried plugin
			for (IndexedFile convFile : allConvertedFiles) {
				// Only validate if the original file was NOT already in the target format
				String originalName = convFile.getOriginalName();
				if (originalName != null && originalName.toLowerCase().endsWith("." + format.toLowerCase())) {
					continue; // skip files that were already in the target format
				}
				// Direct file format retrieval using AbstractConvertPlugin2 pattern
				IndexedFile ifile = index.retrieve(IndexedFile.class, convFile.getUUID(),
						RodaConstants.FILE_FORMAT_FIELDS_TO_RETURN);
				String fileMimetype = ifile.getFileFormat().getMimeType();
				String filePronom = ifile.getFileFormat().getPronom();
				String fileFormat = ifile.getId().substring(ifile.getId().lastIndexOf('.') + 1);

				// Get plugin format information
				// List<String> applicableTo = imageConverter.getApplicableTo();
				List<String> convertableTo = imageConverter.getConvertableTo();
				// Map<String, List<String>> pronomToExtension =
				// imageConverter.getPronomToExtension();
				// Map<String, List<String>> mimetypeToExtension =
				// imageConverter.getMimetypeToExtension();

				// Validate the converted file format
				String expectedMimeType = MimeTypes.lookupMimeType(format);
				String actualMimeType = fileMimetype;
				String actualFormat = fileFormat.toLowerCase();

				// Check if the format is in the list of convertable formats
				Assert.assertTrue(convertableTo.contains(actualFormat),
						"File " + convFile.getId() + " should be in convertable formats list: " + convertableTo +
								" but was: " + actualFormat);

				// Check MIME type
				Assert.assertNotNull(actualMimeType, "File format metadata is missing for " + convFile.getId());
				Assert.assertTrue(
						actualMimeType.toLowerCase().contains(expectedMimeType.toLowerCase()),
						"File " + convFile.getId() + " should be " + expectedMimeType + " but was: " + actualMimeType);

				// Additional validation: check if the format is properly mapped
				// @SuppressWarnings("unused")
				// boolean formatMapped = false;
				// if (filePronom != null && pronomToExtension.containsKey(filePronom)) {
				// formatMapped = pronomToExtension.get(filePronom).contains(actualFormat);
				// } else if (fileMimetype != null &&
				// mimetypeToExtension.containsKey(fileMimetype)) {
				// formatMapped = mimetypeToExtension.get(fileMimetype).contains(actualFormat);
				// }

				// Log format information for debugging
				LOGGER.debug("Converted file {}: format={}, mimeType={}, pronom={}, expectedFormat={}",
						convFile.getId(), actualFormat, actualMimeType, filePronom, format);
			}
		}
	}

}
